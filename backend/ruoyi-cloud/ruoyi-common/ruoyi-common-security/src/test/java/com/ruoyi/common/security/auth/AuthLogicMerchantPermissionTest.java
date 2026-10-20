package com.ruoyi.common.security.auth;

import java.util.Collections;
import java.util.LinkedHashSet;
import com.ruoyi.system.api.model.LoginUser;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** 商户管理员角色放行规则测试。 */
public class AuthLogicMerchantPermissionTest
{
    @Test
    public void isMerchantAdministrator_whenMerchantAdminRole_returnsTrue()
    {
        LoginUser loginUser = merchantUser();
        loginUser.setRoles(new LinkedHashSet<String>(Collections.singleton("merchant_admin")));

        assertTrue(AuthLogic.isMerchantAdministrator(loginUser));
        assertTrue(AuthLogic.hasMerchantAdministratorPermission(loginUser, "merchant:user:account:edit"));
    }

    @Test
    public void isMerchantAdministrator_whenOwner_returnsTrue()
    {
        LoginUser loginUser = merchantUser();
        loginUser.setMemberRole("owner");

        assertTrue(AuthLogic.isMerchantAdministrator(loginUser));
        assertTrue(AuthLogic.hasMerchantAdministratorPermission(loginUser, "merchant:user:account:edit"));
    }

    @Test
    public void isMerchantAdministrator_whenPlatformOrStaff_returnsFalse()
    {
        LoginUser platformUser = new LoginUser();
        platformUser.setClientType("platform");
        platformUser.setRoles(new LinkedHashSet<String>(Collections.singleton("merchant_admin")));

        LoginUser staffUser = merchantUser();
        staffUser.setRoles(new LinkedHashSet<String>(Collections.singleton("merchant_staff")));

        assertFalse(AuthLogic.isMerchantAdministrator(platformUser));
        assertFalse(AuthLogic.isMerchantAdministrator(staffUser));
        assertFalse(AuthLogic.hasMerchantAdministratorPermission(platformUser, "merchant:user:account:edit"));
        assertFalse(AuthLogic.hasMerchantAdministratorPermission(staffUser, "merchant:user:account:edit"));
        assertFalse(AuthLogic.hasMerchantAdministratorPermission(staffUser, "system:user:edit"));
    }

    private LoginUser merchantUser()
    {
        LoginUser loginUser = new LoginUser();
        loginUser.setClientType("merchant");
        return loginUser;
    }
}
