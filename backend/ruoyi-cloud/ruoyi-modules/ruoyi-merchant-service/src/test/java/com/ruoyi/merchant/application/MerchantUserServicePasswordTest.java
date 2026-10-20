package com.ruoyi.merchant.application;

import org.junit.Assert;
import org.junit.Test;

/** 商户用户临时密码生成测试。 */
public class MerchantUserServicePasswordTest
{
    @Test
    public void generateInitialPassword_连续调用_生成不同随机值()
    {
        String first = MerchantUserService.generateInitialPassword();
        String second = MerchantUserService.generateInitialPassword();

        Assert.assertNotEquals(first, second);
        Assert.assertEquals(36, first.length());
        Assert.assertEquals(36, second.length());
    }
}
