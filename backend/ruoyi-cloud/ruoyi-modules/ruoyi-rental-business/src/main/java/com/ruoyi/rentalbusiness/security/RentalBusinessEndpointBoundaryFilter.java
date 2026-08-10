package com.ruoyi.rentalbusiness.security;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;

/**
 * 租赁核心业务服务的接口边界过滤器。
 *
 * 过滤器只负责校验请求命名空间、终端类型和内部调用来源；具体业务权限、商户
 * 数据范围和仓库范围必须继续由对应 Controller 与应用服务完成，不能把过滤器当成
 * 业务授权的替代品。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class RentalBusinessEndpointBoundaryFilter extends OncePerRequestFilter
{
    private static final String PLATFORM_PREFIX = "/platform";

    private static final String MERCHANT_PREFIX = "/merchant";

    private static final String INTERNAL_PREFIX = "/internal/rental-business";

    /**
     * 校验租赁业务服务只接收平台、商户或可信内部命名空间请求。
     *
     * @param request 当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 处理异常
     * @throws IOException 响应写入异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException
    {
        String requestUri = request.getRequestURI();
        if (isActuatorPath(requestUri))
        {
            filterChain.doFilter(request, response);
            return;
        }

        if (isInternalPath(requestUri))
        {
            if (!SecurityConstants.INNER.equals(request.getHeader(SecurityConstants.FROM_SOURCE)))
            {
                writeForbidden(response, "租赁业务内部接口仅允许服务间调用");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        String clientType = request.getHeader(Constants.CLIENT_TYPE_HEADER);
        if (isPlatformPath(requestUri) && Constants.CLIENT_TYPE_PLATFORM.equals(clientType))
        {
            filterChain.doFilter(request, response);
            return;
        }
        if (isMerchantPath(requestUri) && Constants.CLIENT_TYPE_MERCHANT.equals(clientType))
        {
            filterChain.doFilter(request, response);
            return;
        }
        writeForbidden(response, "租赁业务接口命名空间或终端类型不合法");
    }

    /**
     * 判断请求是否为 Spring Actuator 探活路径，探活不要求业务终端头。
     *
     * @param requestUri 请求路径
     * @return 是否为探活路径
     */
    private boolean isActuatorPath(String requestUri)
    {
        return "/actuator".equals(requestUri) || requestUri.startsWith("/actuator/");
    }

    /**
     * 判断请求是否为本服务内部契约路径。
     *
     * @param requestUri 请求路径
     * @return 是否为内部契约路径
     */
    private boolean isInternalPath(String requestUri)
    {
        return INTERNAL_PREFIX.equals(requestUri) || requestUri.startsWith(INTERNAL_PREFIX + "/");
    }

    /**
     * 判断请求是否属于平台业务命名空间。
     *
     * @param requestUri 请求路径
     * @return 是否为平台业务路径
     */
    private boolean isPlatformPath(String requestUri)
    {
        return PLATFORM_PREFIX.equals(requestUri) || requestUri.startsWith(PLATFORM_PREFIX + "/");
    }

    /**
     * 判断请求是否属于商户业务命名空间。
     *
     * @param requestUri 请求路径
     * @return 是否为商户业务路径
     */
    private boolean isMerchantPath(String requestUri)
    {
        return MERCHANT_PREFIX.equals(requestUri) || requestUri.startsWith(MERCHANT_PREFIX + "/");
    }

    /**
     * 返回统一 JSON 禁止响应，避免未匹配路径被 Spring 当作静态资源处理。
     *
     * @param response 当前 HTTP 响应
     * @param message 禁止原因
     * @throws IOException 响应写入异常
     */
    private void writeForbidden(HttpServletResponse response, String message) throws IOException
    {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(Constants.UTF8);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"msg\":\"" + message + "\"}");
    }
}
