package com.ruoyi.gateway.filter;

import org.junit.Test;
import com.ruoyi.common.core.constant.Constants;

public class AuthFilterNamespaceTest
{
    @Test
    public void onlyAdminTokenCanAccessAdminNamespace()
    {
        require(AuthFilter.matchesNamespace("/admin/user/list", Constants.CLIENT_TYPE_PLATFORM),
                "platform token should access admin namespace");
        require(!AuthFilter.matchesNamespace("/admin/user/list", Constants.CLIENT_TYPE_MERCHANT),
                "merchant token must not access admin namespace");
    }

    @Test
    public void onlyMerchantTokenCanAccessMerchantNamespace()
    {
        require(AuthFilter.matchesNamespace("/merchant/user/getInfo", Constants.CLIENT_TYPE_MERCHANT),
                "merchant token should access merchant namespace");
        require(!AuthFilter.matchesNamespace("/merchant/user/getInfo", Constants.CLIENT_TYPE_PLATFORM),
                "platform token must not access merchant namespace");
    }

    @Test
    public void rejectsUnknownClientType()
    {
        require(!AuthFilter.isValidClientType(null), "missing client type must be rejected");
        require(!AuthFilter.isValidClientType("admin"), "admin client type must be rejected");
        require(!AuthFilter.isValidClientType("all"), "shared client type must be rejected");
    }

    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }
}
