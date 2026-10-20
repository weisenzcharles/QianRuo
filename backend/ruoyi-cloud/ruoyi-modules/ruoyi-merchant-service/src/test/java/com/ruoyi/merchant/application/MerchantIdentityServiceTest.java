package com.ruoyi.merchant.application;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.merchant.api.model.MerchantLoginIdentity;
import com.ruoyi.merchant.domain.MerchantAccount;
import com.ruoyi.merchant.domain.MerchantBinding;
import com.ruoyi.merchant.mapper.MerchantIdentityMapper;

/** 商户账号主体认证状态装配测试。 */
public class MerchantIdentityServiceTest
{
    @Test
    public void loadByUsername_子账号用户名与手机号不同_两种标识均可登录() throws Exception
    {
        String username = System.getenv().getOrDefault("MERCHANT_LOGIN_TEST_USERNAME", "warehouse-user");
        MerchantAccount account = new MerchantAccount();
        account.setUserId(101L);
        account.setTenantId(7L);
        account.setMerchantId(29L);
        account.setUsername(username);
        account.setPhone("13600000000");
        account.setStatus(0);
        account.setIsDelete(0);

        MerchantBinding binding = new MerchantBinding();
        binding.setTenantId(7L);
        binding.setMerchantId(29L);
        binding.setUserId(101L);
        binding.setMemberRole("staff");
        binding.setWarehouseScopeType(RentalCode.WarehouseScope.ALL.getCode());

        MerchantIdentityMapper mapper = (MerchantIdentityMapper) Proxy.newProxyInstance(
                MerchantIdentityMapper.class.getClassLoader(), new Class<?>[] { MerchantIdentityMapper.class },
                (proxy, method, args) -> {
                    switch (method.getName())
                    {
                        case "selectByUsername":
                            return account.getUsername().equals(args[0]) ? account : null;
                        case "selectByPhone":
                            return account.getPhone().equals(args[0]) ? account : null;
                        case "countActiveBindings":
                            return 1;
                        case "selectActiveBinding":
                            return binding;
                        case "selectMemberAuthStatus":
                            Assert.assertEquals(Long.valueOf(7L), args[0]);
                            Assert.assertEquals(Long.valueOf(29L), args[1]);
                            return RentalCode.AuthenticationStatus.VERIFIED.getCode();
                        case "selectWarehouseIds":
                            return Collections.emptyList();
                        case "selectRoleKeys":
                            return Collections.singletonList("merchant_staff");
                        case "selectPermissions":
                            return Collections.singletonList("merchant:orders:list");
                        default:
                            throw new UnsupportedOperationException(method.getName());
                    }
                });

        MerchantIdentityService service = new MerchantIdentityService();
        Field mapperField = MerchantIdentityService.class.getDeclaredField("identityMapper");
        mapperField.setAccessible(true);
        mapperField.set(service, mapper);

        MerchantLoginIdentity identity = service.loadByUsername(account.getUsername());
        MerchantLoginIdentity phoneIdentity = service.loadByUsername(account.getPhone());

        Assert.assertEquals(RentalCode.AuthenticationStatus.VERIFIED.getCode(), identity.getMerchantAuthStatus());
        Assert.assertEquals(username, identity.getUsername());
        Assert.assertTrue(identity.getRoles().contains("merchant_staff"));
        Assert.assertTrue(identity.getPermissions().contains("merchant:orders:list"));
        Assert.assertEquals(account.getUserId(), phoneIdentity.getUserId());
    }

    @Test
    public void loadByUsername_用户名和手机号均未命中_在密码校验前返回通用错误() throws Exception
    {
        MerchantIdentityMapper mapper = (MerchantIdentityMapper) Proxy.newProxyInstance(
                MerchantIdentityMapper.class.getClassLoader(), new Class<?>[] { MerchantIdentityMapper.class },
                (proxy, method, args) -> {
                    if ("selectByUsername".equals(method.getName()))
                    {
                        Assert.assertEquals("unknown-account", args[0]);
                        return null;
                    }
                    if ("selectByPhone".equals(method.getName()))
                    {
                        Assert.assertEquals("unknown-account", args[0]);
                        return null;
                    }
                    Assert.fail("账号未命中后不应继续查询：" + method.getName());
                    return null;
                });

        MerchantIdentityService service = new MerchantIdentityService();
        Field mapperField = MerchantIdentityService.class.getDeclaredField("identityMapper");
        mapperField.setAccessible(true);
        mapperField.set(service, mapper);

        ServiceException exception = Assert.assertThrows(ServiceException.class,
                () -> service.loadByUsername("unknown-account"));

        Assert.assertEquals("商户账号或密码错误", exception.getMessage());
    }
}
