package com.ruoyi.rentalbusiness.controller;

import com.ruoyi.common.security.annotation.RequiresPermissions;
import java.lang.reflect.Method;
import org.junit.Assert;
import org.junit.Test;

/** 商户库存页面与接口权限码契约测试。 */
public class MerchantInventoryControllerPermissionTest
{
    /** 库存趋势菜单授权后必须能访问库存概况接口。 */
    @Test
    public void overview_shouldUseInventoryTrendMenuPermission() throws Exception
    {
        Method overview = MerchantInventoryController.class.getMethod("overview", String.class, Long.class,
                String.class, Long.class, Integer.class);
        RequiresPermissions permissions = overview.getAnnotation(RequiresPermissions.class);

        Assert.assertNotNull(permissions);
        Assert.assertArrayEquals(new String[] { "merchant:trend:inventory" }, permissions.value());
    }
}
