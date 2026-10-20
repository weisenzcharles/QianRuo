package com.ruoyi.merchant.security;

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

public class MerchantEndpointBoundaryFilterTest
{
    private final MerchantEndpointBoundaryFilter filter = new MerchantEndpointBoundaryFilter();

    @Test
    public void allowsMerchantEndpointForMerchantClient() throws Exception
    {
        AtomicBoolean continued = new AtomicBoolean(false);

        filter.doFilterInternal(request("/merchant/user/getInfo", Constants.CLIENT_TYPE_MERCHANT),
                response(new AtomicInteger()), (req, res) -> continued.set(true));

        require(continued.get(), "merchant request should continue through the filter");
    }

    @Test
    public void allowsMenuAndRoleEndpointsForMerchantClient() throws Exception
    {
        for (String path : new String[] {
                "/merchant/menu/getRouters", "/merchant/role/list", "/merchant/operlog/list" })
        {
            AtomicBoolean continued = new AtomicBoolean(false);
            filter.doFilterInternal(request(path, Constants.CLIENT_TYPE_MERCHANT), response(new AtomicInteger()),
                    (req, res) -> continued.set(true));
            require(continued.get(), path + " should continue through the filter");
        }
    }

    @Test
    public void rejectsPlatformClientOnMerchantEndpoint() throws Exception
    {
        AtomicInteger status = new AtomicInteger();

        filter.doFilterInternal(request("/merchant/user/getInfo", Constants.CLIENT_TYPE_PLATFORM), response(status),
                (req, res) -> { });

        require(HttpServletResponse.SC_FORBIDDEN == status.get(), "platform client should be rejected");
    }

    @Test
    public void rejectsPlatformClientOnMerchantRoleEndpoint() throws Exception
    {
        AtomicInteger status = new AtomicInteger();

        filter.doFilterInternal(request("/merchant/role/list", Constants.CLIENT_TYPE_PLATFORM), response(status),
                (req, res) -> { });

        require(HttpServletResponse.SC_FORBIDDEN == status.get(), "platform client should not access merchant roles");

    }

    @Test
    public void rejectsAdminClientType() throws Exception
    {
        AtomicInteger status = new AtomicInteger();
        filter.doFilterInternal(request("/merchant/user/getInfo", "admin"), response(status),
                (req, res) -> { });
        require(HttpServletResponse.SC_FORBIDDEN == status.get(), "admin client type should be rejected");
    }

    @Test
    public void rejectsPlatformEndpointEvenForMerchantClient() throws Exception
    {
        AtomicInteger status = new AtomicInteger();

        filter.doFilterInternal(request("/admin/user/list", Constants.CLIENT_TYPE_MERCHANT), response(status),
                (req, res) -> { });

        require(HttpServletResponse.SC_FORBIDDEN == status.get(), "platform path should be rejected");
    }

    @Test
    public void allowsTrustedInternalMerchantEndpoint() throws Exception
    {
        AtomicBoolean continued = new AtomicBoolean(false);

        filter.doFilterInternal(request("/internal/merchant/users/by-username/test",
                Constants.CLIENT_TYPE_MERCHANT, SecurityConstants.INNER), response(new AtomicInteger()),
                (req, res) -> continued.set(true));

        require(continued.get(), "trusted internal request should continue through the filter");
    }

    @Test
    public void rejectsUntrustedInternalMerchantEndpoint() throws Exception
    {
        AtomicInteger status = new AtomicInteger();

        filter.doFilterInternal(request("/internal/merchant/users/by-username/test",
                Constants.CLIENT_TYPE_MERCHANT, null), response(status), (req, res) -> { });

        require(HttpServletResponse.SC_FORBIDDEN == status.get(), "untrusted internal request should be rejected");
    }

    private HttpServletRequest request(String requestUri, String clientType)
    {
        return request(requestUri, clientType, null);
    }

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

    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }
}
