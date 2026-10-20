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

    /** 验证平台租赁业务统一走 admin 命名空间，旧 platform 命名空间彻底拒绝。 */
    @Test
    public void onlyPlatformTokenCanAccessPlatformBusinessNamespace()
    {
        require(AuthFilter.matchesNamespace("/admin/rental/order-channel/list", Constants.CLIENT_TYPE_PLATFORM),
                "platform token should access platform business namespace");
        require(!AuthFilter.matchesNamespace("/admin/rental/order-channel/list", Constants.CLIENT_TYPE_MERCHANT),
                "merchant token must not access platform business namespace");
        require(!AuthFilter.matchesNamespace("/platform/order-channel/list", Constants.CLIENT_TYPE_PLATFORM),
                "legacy platform namespace must be rejected");
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
