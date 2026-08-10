package com.ruoyi.rentalbusiness.security;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** 租赁业务服务接口命名空间与终端边界测试。 */
public class RentalBusinessEndpointBoundaryFilterTest
{
    private final RentalBusinessEndpointBoundaryFilter filter = new RentalBusinessEndpointBoundaryFilter();

    /** 验证平台终端可以进入平台业务命名空间。 */
    @Test
    public void allowsPlatformBusinessPathForPlatformClient() throws Exception
    {
        AtomicBoolean continued = new AtomicBoolean(false);
        filter.doFilterInternal(request("/platform/product", Constants.CLIENT_TYPE_PLATFORM, null),
                response(new AtomicInteger()), (req, res) -> continued.set(true));
        require(continued.get(), "platform business request should continue");
    }

    /** 验证商户终端可以进入商户业务命名空间。 */
    @Test
    public void allowsMerchantBusinessPathForMerchantClient() throws Exception
    {
        AtomicBoolean continued = new AtomicBoolean(false);
        filter.doFilterInternal(request("/merchant/order", Constants.CLIENT_TYPE_MERCHANT, null),
                response(new AtomicInteger()), (req, res) -> continued.set(true));
        require(continued.get(), "merchant business request should continue");
    }

    /** 验证内部调用必须携带统一内部来源标识。 */
    @Test
    public void allowsTrustedInternalPath() throws Exception
    {
        AtomicBoolean continued = new AtomicBoolean(false);
        filter.doFilterInternal(request("/internal/rental-business/ping", null, SecurityConstants.INNER),
                response(new AtomicInteger()), (req, res) -> continued.set(true));
        require(continued.get(), "trusted internal request should continue");
    }

    /** 验证未携带内部来源标识的内部请求被拒绝。 */
    @Test
    public void rejectsUntrustedInternalPath() throws Exception
    {
        AtomicInteger status = new AtomicInteger();
        filter.doFilterInternal(request("/internal/rental-business/ping", Constants.CLIENT_TYPE_PLATFORM, null),
                response(status), (req, res) -> { });
        require(HttpServletResponse.SC_FORBIDDEN == status.get(), "untrusted internal request should be rejected");
    }

    /** 验证旧的 admin 或未定义路径不会绕过新业务服务边界。 */
    @Test
    public void rejectsLegacyOrUnknownPath() throws Exception
    {
        for (String path : new String[] { "/admin/order", "/system/order", "/unknown" })
        {
            AtomicInteger status = new AtomicInteger();
            filter.doFilterInternal(request(path, Constants.CLIENT_TYPE_PLATFORM, null), response(status),
                    (req, res) -> { });
            require(HttpServletResponse.SC_FORBIDDEN == status.get(), path + " should be rejected");
        }
    }

    /** 验证 Actuator 探活不依赖浏览器终端标识。 */
    @Test
    public void allowsActuatorPathWithoutClientType() throws Exception
    {
        AtomicBoolean continued = new AtomicBoolean(false);
        filter.doFilterInternal(request("/actuator/health", null, null), response(new AtomicInteger()),
                (req, res) -> continued.set(true));
        require(continued.get(), "actuator request should continue");
    }

    /** 创建只实现过滤器所需方法的请求代理，避免引入额外 Web 测试依赖。 */
    private HttpServletRequest request(String requestUri, String clientType, String source)
    {
        return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] { HttpServletRequest.class }, (proxy, method, args) -> {
                    if ("getRequestURI".equals(method.getName()))
                    {
                        return requestUri;
                    }
                    if ("getHeader".equals(method.getName()))
                    {
                        if (Constants.CLIENT_TYPE_HEADER.equals(args[0]))
                        {
                            return clientType;
                        }
                        return SecurityConstants.FROM_SOURCE.equals(args[0]) ? source : null;
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    /** 创建记录 HTTP 状态码的响应代理，用于断言过滤器拒绝结果。 */
    private HttpServletResponse response(AtomicInteger status)
    {
        StringWriter body = new StringWriter();
        return (HttpServletResponse) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] { HttpServletResponse.class }, (proxy, method, args) -> {
                    if ("setStatus".equals(method.getName()))
                    {
                        status.set((Integer) args[0]);
                        return null;
                    }
                    if ("getWriter".equals(method.getName()))
                    {
                        return new PrintWriter(body);
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    /** 返回代理方法的基础默认值，保持测试代理对 Servlet 接口的兼容性。 */
    private Object defaultValue(Class<?> returnType)
    {
        if (!returnType.isPrimitive())
        {
            return null;
        }
        if (boolean.class.equals(returnType))
        {
            return false;
        }
        if (char.class.equals(returnType))
        {
            return '\0';
        }
        return 0;
    }

    /** 使用明确异常快速终止失败断言，保持当前项目 JUnit 4 测试风格一致。 */
    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }
}
