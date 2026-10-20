package com.ruoyi.system.security;

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
import com.ruoyi.common.core.context.ClientContextHolder;
import com.ruoyi.common.core.utils.StringUtils;

/**
 * 强制平台端和商户端使用独立接口命名空间。
 *
 * platform-service 只承载平台接口，任何商户终端或商户命名空间请求都不得进入本服务。
 *
 * @author qianruo
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class PlatformNamespaceFilter extends OncePerRequestFilter
{
    private static final String MERCHANT_PREFIX = "/merchant";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException
    {
        if (SecurityConstants.INNER.equals(request.getHeader(SecurityConstants.FROM_SOURCE)))
        {
            filterChain.doFilter(request, response);
            return;
        }
        String requestUri = request.getRequestURI();
        if (requestUri.equals("/actuator") || requestUri.startsWith("/actuator/"))
        {
            filterChain.doFilter(request, response);
            return;
        }
        String clientTypeHeader = request.getHeader(Constants.CLIENT_TYPE_HEADER);
        if (StringUtils.isEmpty(clientTypeHeader))
        {
            writeForbidden(response, "平台接口缺少可信终端标识");
            return;
        }
        String clientType = clientTypeHeader;
        boolean merchantPath = requestUri.equals(MERCHANT_PREFIX) || requestUri.startsWith(MERCHANT_PREFIX + "/");

        if (!Constants.CLIENT_TYPE_PLATFORM.equals(clientType))
        {
            writeForbidden(response, "当前登录终端不能访问平台服务");
            return;
        }

        if (merchantPath)
        {
            writeForbidden(response, "平台端不允许访问商户接口");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void writeForbidden(HttpServletResponse response, String message) throws IOException
    {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(Constants.UTF8);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"msg\":\"" + message + "\"}");
    }
}
