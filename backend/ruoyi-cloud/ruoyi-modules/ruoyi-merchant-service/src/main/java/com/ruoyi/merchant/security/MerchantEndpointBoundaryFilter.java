package com.ruoyi.merchant.security;

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

/**
 * 商户服务入口边界的第一层校验。
 *
 * 业务 Controller 仍需校验可信 Token 中的商户身份和权限。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class MerchantEndpointBoundaryFilter extends OncePerRequestFilter
{
    private static final String MERCHANT_PREFIX = "/merchant";

    private static final String INTERNAL_PREFIX = "/internal/merchant";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException
    {
        String requestUri = request.getRequestURI();
        if (requestUri.equals("/actuator") || requestUri.startsWith("/actuator/"))
        {
            filterChain.doFilter(request, response);
            return;
        }

        boolean internalPath = requestUri.equals(INTERNAL_PREFIX)
                || requestUri.startsWith(INTERNAL_PREFIX + "/");
        if (internalPath)
        {
            if (!SecurityConstants.INNER.equals(request.getHeader(SecurityConstants.FROM_SOURCE)))
            {
                writeForbidden(response, "商户内部接口仅允许服务间调用");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        boolean merchantPath = requestUri.equals(MERCHANT_PREFIX)
                || requestUri.startsWith(MERCHANT_PREFIX + "/");
        if (!merchantPath)
        {
            writeForbidden(response, "商户服务仅允许 /merchant/** 接口");
            return;
        }
        String clientType = request.getHeader(Constants.CLIENT_TYPE_HEADER);
        if (!Constants.CLIENT_TYPE_MERCHANT.equals(clientType))
        {
            writeForbidden(response, "当前登录终端不能访问商户服务");
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
