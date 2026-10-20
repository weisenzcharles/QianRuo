package com.ruoyi.common.core.constant;

import org.junit.Test;

public class CacheConstantsTest
{
    @Test
    public void loginTokenKeysAreSeparatedByClientType()
    {
        String token = "same-token";
        String platformKey = CacheConstants.loginTokenKey(Constants.CLIENT_TYPE_PLATFORM, token);
        String merchantKey = CacheConstants.loginTokenKey(Constants.CLIENT_TYPE_MERCHANT, token);

        require(!platformKey.equals(merchantKey), "platform and merchant token keys must differ");
        require(platformKey.startsWith(CacheConstants.PLATFORM_LOGIN_TOKEN_KEY), "platform token prefix is invalid");
        require(merchantKey.startsWith(CacheConstants.MERCHANT_LOGIN_TOKEN_KEY), "merchant token prefix is invalid");
        assertUnsupportedClientType("admin", token);
    }

    private void assertUnsupportedClientType(String clientType, String token)
    {
        try
        {
            CacheConstants.loginTokenKey(clientType, token);
            throw new AssertionError("unsupported client type should be rejected: " + clientType);
        }
        catch (IllegalArgumentException expected)
        {
            // Expected.
        }
    }

    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }
}
