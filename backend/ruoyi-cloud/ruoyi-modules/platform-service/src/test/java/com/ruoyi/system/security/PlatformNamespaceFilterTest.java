package com.ruoyi.system.security;

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

public class PlatformNamespaceFilterTest
{
    private final PlatformNamespaceFilter filter = new PlatformNamespaceFilter();

    @Test
    public void allowsPlatformClient() throws Exception
    {
        AtomicBoolean continued = new AtomicBoolean(false);
        filter.doFilterInternal(request("/user/getInfo", Constants.CLIENT_TYPE_PLATFORM, null),
                response(new AtomicInteger()), (req, res) -> continued.set(true));
        require(continued.get(), "platform request should continue through the filter");
    }

    @Test
    public void rejectsMerchantClient() throws Exception
    {
        AtomicInteger status = new AtomicInteger();
        filter.doFilterInternal(request("/user/getInfo", Constants.CLIENT_TYPE_MERCHANT, null), response(status),
                (req, res) -> { });
        require(HttpServletResponse.SC_FORBIDDEN == status.get(), "merchant client should be rejected");
    }

    @Test
    public void rejectsAdminClientType() throws Exception
    {
        AtomicInteger status = new AtomicInteger();
        filter.doFilterInternal(request("/user/getInfo", "admin", null), response(status), (req, res) -> { });
        require(HttpServletResponse.SC_FORBIDDEN == status.get(), "admin client type should be rejected");
    }

    @Test
    public void rejectsMissingClientType() throws Exception
    {
        AtomicInteger status = new AtomicInteger();
        filter.doFilterInternal(request("/user/getInfo", null, null), response(status), (req, res) -> { });
        require(HttpServletResponse.SC_FORBIDDEN == status.get(), "missing client type should be rejected");
    }

    @Test
    public void allowsActuatorWithoutClientType() throws Exception
    {
        AtomicBoolean continued = new AtomicBoolean(false);
        filter.doFilterInternal(request("/actuator/health", null, null), response(new AtomicInteger()),
                (req, res) -> continued.set(true));
        require(continued.get(), "actuator health request should continue through the filter");
    }

    @Test
    public void rejectsMerchantNamespaceForPlatformClient() throws Exception
    {
        AtomicInteger status = new AtomicInteger();
        filter.doFilterInternal(request("/merchant/user/getInfo", Constants.CLIENT_TYPE_PLATFORM, null), response(status),
                (req, res) -> { });
        require(HttpServletResponse.SC_FORBIDDEN == status.get(), "merchant path should be rejected");
    }

    @Test
    public void allowsTrustedInternalCall() throws Exception
    {
        AtomicBoolean continued = new AtomicBoolean(false);
        filter.doFilterInternal(request("/operlog", Constants.CLIENT_TYPE_MERCHANT, SecurityConstants.INNER),
                response(new AtomicInteger()), (req, res) -> continued.set(true));
        require(continued.get(), "trusted internal request should continue through the filter");
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
