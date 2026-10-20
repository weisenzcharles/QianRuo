package com.ruoyi.common.log.aspect;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.core.utils.ExceptionUtil;
import com.ruoyi.common.core.utils.ServletUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.utils.ip.IpUtils;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessStatus;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.log.enums.OperatorType;
import com.ruoyi.common.log.filter.PropertyPreExcludeFilter;
import com.ruoyi.common.log.service.AsyncLogService;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.api.domain.SysOperLog;
import com.ruoyi.system.api.model.LoginUser;

/**
 * 全局 REST 操作审计切面。
 *
 * <p>所有控制器公开方法都会进入审计链路；显式 {@link Log} 注解用于覆盖自动推断的标题、业务类型和参数策略。</p>
 */
@Aspect
@Component
public class LogAspect
{
    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    /** 所有请求和响应序列化时必须排除的敏感属性。 */
    public static final String[] EXCLUDE_PROPERTIES = {
            "password", "oldPassword", "newPassword", "confirmPassword", "token", "accessToken", "refreshToken",
            "access_token", "refresh_token", "authorization", "phone", "mobile", "email", "mainAccount",
            "contactPhone", "servicePhone", "contactEmail", "alipayRealName", "licenseNo", "idCard", "idNo",
            "alipayAccount", "licenseImageUrl", "legalRepresentativeName", "legalRepresentativeIdNo",
            "legalIdFrontUrl", "legalIdBackUrl", "contractFileUrl"
    };

    /** 单个请求参数或响应摘要的最大保存长度。 */
    private static final int PARAM_MAX_LENGTH = 2000;

    /** 网关或调用链传入的跟踪号请求头。 */
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    private final AsyncLogService asyncLogService;

    /**
     * 注入异步日志持久化服务，各业务服务可以通过主实现决定日志目标表。
     *
     * @param asyncLogService 当前服务使用的异步日志持久化实现
     */
    public LogAspect(AsyncLogService asyncLogService)
    {
        this.asyncLogService = asyncLogService;
    }

    /**
     * 环绕拦截所有 REST 控制器公开方法，统一记录成功、失败、HTTP 状态和耗时。
     *
     * @param joinPoint 控制器方法连接点
     * @return 控制器原始返回值
     * @throws Throwable 控制器执行产生的原始异常
     */
    @Around("within(@org.springframework.web.bind.annotation.RestController *) && execution(public * *(..))")
    public Object auditRestOperation(ProceedingJoinPoint joinPoint) throws Throwable
    {
        long startedAt = System.currentTimeMillis();
        Object result = null;
        Throwable failure = null;
        try
        {
            result = joinPoint.proceed();
            return result;
        }
        catch (Throwable throwable)
        {
            failure = throwable;
            throw throwable;
        }
        finally
        {
            recordSafely(joinPoint, result, failure, System.currentTimeMillis() - startedAt);
        }
    }

    /**
     * 在不影响主业务返回的前提下构造并异步保存操作日志，日志异常只写本地错误日志。
     *
     * @param joinPoint 控制器方法连接点
     * @param result 控制器返回值
     * @param failure 控制器异常，成功时为空
     * @param costTime 请求耗时毫秒数
     */
    private void recordSafely(ProceedingJoinPoint joinPoint, Object result, Throwable failure, long costTime)
    {
        try
        {
            HttpServletRequest request = ServletUtils.getRequest();
            if (request == null || isLogIngestionEndpoint(request))
            {
                return;
            }
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            Log controllerLog = method.getAnnotation(Log.class);
            SysOperLog operLog = createBaseLog(joinPoint, request, costTime);
            applyOperationDescription(joinPoint, controllerLog, operLog, result);
            applyResult(operLog, failure);
            asyncLogService.saveSysLog(operLog);
        }
        catch (Exception exception)
        {
            log.error("全局操作日志记录失败: {}", exception.getMessage(), exception);
        }
    }

    /**
     * 构造所有操作日志共用的请求、身份、跟踪号和方法信息。
     *
     * @param joinPoint 控制器方法连接点
     * @param request 当前 HTTP 请求
     * @param costTime 请求耗时毫秒数
     * @return 已填充公共字段的操作日志
     */
    private SysOperLog createBaseLog(ProceedingJoinPoint joinPoint, HttpServletRequest request, long costTime)
    {
        SysOperLog operLog = new SysOperLog();
        operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
        operLog.setOperIp(IpUtils.getIpAddr());
        operLog.setOperUrl(StringUtils.substring(request.getRequestURI(), 0, 255));
        operLog.setRequestMethod(request.getMethod());
        operLog.setMethod(joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName() + "()");
        operLog.setCostTime(costTime);
        operLog.setHttpStatus(resolveHttpStatus());
        operLog.setTraceId(resolveTraceId(request));
        operLog.setIsDelete(0);

        String username = SecurityUtils.getUsername();
        if (StringUtils.isNotBlank(username))
        {
            operLog.setOperName(username);
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser != null)
        {
            operLog.setOperatorUserId(loginUser.getUserid());
            operLog.setClientType(loginUser.getClientType());
            operLog.setTenantId(loginUser.getTenantId());
            operLog.setMerchantId(loginUser.getMemberId());
        }
        else
        {
            operLog.setClientType(ServletUtils.getHeader(request, Constants.CLIENT_TYPE_HEADER));
        }
        return operLog;
    }

    /**
     * 根据显式日志注解或 HTTP 方法设置业务标题、类型、操作人类型和参数摘要。
     *
     * @param joinPoint 控制器方法连接点
     * @param controllerLog 控制器显式日志注解，可以为空
     * @param operLog 待补充的操作日志
     * @param result 控制器返回值
     * @throws Exception 参数序列化异常
     */
    private void applyOperationDescription(ProceedingJoinPoint joinPoint, Log controllerLog,
            SysOperLog operLog, Object result) throws Exception
    {
        if (controllerLog == null)
        {
            operLog.setBusinessType(inferBusinessType(operLog.getRequestMethod()).ordinal());
            operLog.setTitle(inferTitle(joinPoint));
            operLog.setOperatorType(OperatorType.MANAGE.ordinal());
            setRequestValue(joinPoint, operLog, new String[0]);
            return;
        }

        operLog.setBusinessType(controllerLog.businessType().ordinal());
        operLog.setTitle(controllerLog.title());
        operLog.setOperatorType(controllerLog.operatorType().ordinal());
        if (controllerLog.isSaveRequestData())
        {
            setRequestValue(joinPoint, operLog, controllerLog.excludeParamNames());
        }
        if (controllerLog.isSaveResponseData() && result != null)
        {
            operLog.setJsonResult(StringUtils.substring(
                    JSON.toJSONString(result, excludePropertyPreFilter(controllerLog.excludeParamNames())),
                    0, PARAM_MAX_LENGTH));
        }
    }

    /**
     * 根据异常更新业务状态、错误消息和 HTTP 状态，成功请求保留响应对象设置的状态码。
     *
     * @param operLog 待更新的操作日志
     * @param failure 控制器异常，成功时为空
     */
    private void applyResult(SysOperLog operLog, Throwable failure)
    {
        if (failure == null)
        {
            return;
        }
        operLog.setStatus(BusinessStatus.FAIL.ordinal());
        operLog.setHttpStatus(Math.max(operLog.getHttpStatus() == null ? 0 : operLog.getHttpStatus(), 500));
        operLog.setErrorMsg(StringUtils.substring(
                Convert.toStr(failure.getMessage(), ExceptionUtil.getExceptionMessage(failure)),
                0, PARAM_MAX_LENGTH));
    }

    /**
     * 保存查询参数或请求体摘要；所有属性统一经过敏感字段过滤器。
     *
     * @param joinPoint 控制器方法连接点
     * @param operLog 待补充的操作日志
     * @param excludeParamNames 注解额外指定的排除字段
     * @throws Exception 参数序列化异常
     */
    private void setRequestValue(ProceedingJoinPoint joinPoint, SysOperLog operLog,
            String[] excludeParamNames) throws Exception
    {
        String requestMethod = operLog.getRequestMethod();
        Map<?, ?> paramsMap = ServletUtils.getParamMap(ServletUtils.getRequest());
        if (StringUtils.isEmpty(paramsMap) && StringUtils.equalsAny(requestMethod,
                HttpMethod.PUT.name(), HttpMethod.POST.name(), HttpMethod.PATCH.name(), HttpMethod.DELETE.name()))
        {
            operLog.setOperParam(argsArrayToString(joinPoint.getArgs(), excludeParamNames));
        }
        else
        {
            operLog.setOperParam(StringUtils.substring(
                    JSON.toJSONString(paramsMap, excludePropertyPreFilter(excludeParamNames)),
                    0, PARAM_MAX_LENGTH));
        }
    }

    /**
     * 把可审计的方法参数拼装为长度受控的 JSON 摘要，文件和 Servlet 对象不会进入日志。
     *
     * @param paramsArray 方法参数数组
     * @param excludeParamNames 注解额外指定的排除字段
     * @return 请求参数摘要
     */
    private String argsArrayToString(Object[] paramsArray, String[] excludeParamNames)
    {
        StringBuilder params = new StringBuilder();
        if (paramsArray == null)
        {
            return params.toString();
        }
        for (Object value : paramsArray)
        {
            if (value == null || isFilterObject(value))
            {
                continue;
            }
            try
            {
                params.append(JSON.toJSONString(value, excludePropertyPreFilter(excludeParamNames))).append(' ');
                if (params.length() >= PARAM_MAX_LENGTH)
                {
                    return StringUtils.substring(params.toString(), 0, PARAM_MAX_LENGTH);
                }
            }
            catch (Exception exception)
            {
                log.error("请求参数拼装失败: {}", exception.getMessage(), exception);
            }
        }
        return params.toString();
    }

    /**
     * 合并全局敏感属性和方法注解排除属性，生成 JSON 属性过滤器。
     *
     * @param excludeParamNames 方法额外排除属性
     * @return JSON 属性过滤器
     */
    public PropertyPreExcludeFilter excludePropertyPreFilter(String[] excludeParamNames)
    {
        return new PropertyPreExcludeFilter().addExcludes(ArrayUtils.addAll(EXCLUDE_PROPERTIES, excludeParamNames));
    }

    /**
     * 判断参数是否属于文件、Servlet 或参数校验对象等不可序列化、不可审计类型。
     *
     * @param value 待判断参数
     * @return 需要过滤时返回 true
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(Object value)
    {
        Class<?> clazz = value.getClass();
        if (clazz.isArray())
        {
            return MultipartFile.class.isAssignableFrom(clazz.getComponentType());
        }
        if (Collection.class.isAssignableFrom(clazz))
        {
            for (Object item : (Collection) value)
            {
                if (item instanceof MultipartFile)
                {
                    return true;
                }
            }
        }
        if (Map.class.isAssignableFrom(clazz))
        {
            for (Object item : ((Map) value).values())
            {
                if (item instanceof MultipartFile)
                {
                    return true;
                }
            }
        }
        return value instanceof MultipartFile || value instanceof HttpServletRequest
                || value instanceof HttpServletResponse || value instanceof BindingResult;
    }

    /**
     * 根据 HTTP 方法推断通用业务类型，显式日志注解优先于该规则。
     *
     * @param requestMethod HTTP 请求方法
     * @return 通用业务类型
     */
    private BusinessType inferBusinessType(String requestMethod)
    {
        if (HttpMethod.POST.matches(requestMethod))
        {
            return BusinessType.INSERT;
        }
        if (HttpMethod.PUT.matches(requestMethod) || HttpMethod.PATCH.matches(requestMethod))
        {
            return BusinessType.UPDATE;
        }
        if (HttpMethod.DELETE.matches(requestMethod))
        {
            return BusinessType.DELETE;
        }
        return BusinessType.OTHER;
    }

    /**
     * 为未显式声明日志标题的接口生成稳定的方法级标题。
     *
     * @param joinPoint 控制器方法连接点
     * @return 控制器类名与方法名组成的标题
     */
    private String inferTitle(ProceedingJoinPoint joinPoint)
    {
        return joinPoint.getTarget().getClass().getSimpleName() + "." + joinPoint.getSignature().getName();
    }

    /**
     * 从响应对象读取最终 HTTP 状态码，响应不可用时使用成功默认值 200。
     *
     * @return HTTP 状态码
     */
    private int resolveHttpStatus()
    {
        HttpServletResponse response = ServletUtils.getResponse();
        return response == null ? 200 : response.getStatus();
    }

    /**
     * 优先使用网关传入的调用链跟踪号；缺失时生成新的 UUID，保证每条日志均可追踪。
     *
     * @param request 当前 HTTP 请求
     * @return 调用链跟踪号
     */
    private String resolveTraceId(HttpServletRequest request)
    {
        String traceId = ServletUtils.getHeader(request, TRACE_ID_HEADER);
        return StringUtils.isBlank(traceId) ? UUID.randomUUID().toString().replace("-", "") : traceId;
    }

    /**
     * 识别平台日志接收接口，原始业务日志已经包含该调用，跳过接收动作可避免审计日志成倍增长。
     *
     * @param request 当前 HTTP 请求
     * @return 平台日志接收接口返回 true
     */
    private boolean isLogIngestionEndpoint(HttpServletRequest request)
    {
        return HttpMethod.POST.matches(request.getMethod()) && "/operlog".equals(request.getRequestURI());
    }
}
