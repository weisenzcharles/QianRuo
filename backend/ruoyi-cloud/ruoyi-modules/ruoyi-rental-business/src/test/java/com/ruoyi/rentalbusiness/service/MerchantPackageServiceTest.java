package com.ruoyi.rentalbusiness.service;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Test;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.rentalbusiness.mapper.MerchantPackageMapper;
import com.ruoyi.system.api.RemoteAccessoryService;
import com.ruoyi.system.api.model.PlatformAccessoryOption;
import com.ruoyi.system.api.model.LoginUser;

/** 商户套餐配件选项的主型号约束测试。 */
public class MerchantPackageServiceTest
{
    @After
    public void clearContext()
    {
        SecurityContextHolder.remove();
    }

    /** 未选择主型号时不得查询数据库或调用平台配件服务。 */
    @Test
    public void accessoryOptions_withoutModel_returnsEmptyWithoutQuerying()
    {
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        AtomicInteger mapperCalls = new AtomicInteger();
        MerchantPackageMapper mapper = (MerchantPackageMapper) Proxy.newProxyInstance(
                MerchantPackageMapper.class.getClassLoader(), new Class<?>[] {MerchantPackageMapper.class},
                (proxy, method, args) -> {
                    mapperCalls.incrementAndGet();
                    return null;
                });

        List<Map<String, Object>> result = new MerchantPackageService(mapper, null, null, null).accessoryOptions(null);

        require(result.isEmpty(), "accessory options must be empty without a main model");
        require(mapperCalls.get() == 0, "database must not be queried without a main model");
    }

    /** 详情接口将官方配件唯一编号解析为平台名称，避免展示通用占位文本。 */
    @Test
    public void detail_officialAccessory_resolvesName()
    {
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        MerchantPackageMapper mapper = (MerchantPackageMapper) Proxy.newProxyInstance(
                MerchantPackageMapper.class.getClassLoader(), new Class<?>[] {MerchantPackageMapper.class},
                (proxy, method, args) -> {
                    if ("selectById".equals(method.getName()))
                        return new HashMap<String, Object>(Map.of("packageId", 18L, "packageName", "演唱会套餐"));
                    if ("selectVersionById".equals(method.getName()))
                        return new HashMap<String, Object>(Map.of("packageVersionId", 44L));
                    if ("selectItems".equals(method.getName()))
                        return Collections.singletonList(new HashMap<String, Object>(Map.of(
                                "itemRole", 2, "accessoryId", 38L, "itemName", "官方配件", "quantity", 1)));
                    return null;
                });
        PlatformAccessoryOption officialAccessory = new PlatformAccessoryOption();
        officialAccessory.setAccessoryId(38L);
        officialAccessory.setAccessoryCode("ACC000000032");
        officialAccessory.setAccessoryName("R2004+G20KX");
        AtomicInteger remoteCalls = new AtomicInteger();
        RemoteAccessoryService remote = (RemoteAccessoryService) Proxy.newProxyInstance(
                RemoteAccessoryService.class.getClassLoader(), new Class<?>[] {RemoteAccessoryService.class},
                (proxy, method, args) -> {
                    if (!"listActive".equals(method.getName())) return null;
                    remoteCalls.incrementAndGet();
                    return R.ok(Collections.singletonList(officialAccessory));
                });

        Map<String, Object> result = new MerchantPackageService(mapper, null, remote, null).detail(18L);
        List<?> items = (List<?>) result.get("items");
        Map<?, ?> item = (Map<?, ?>) items.get(0);
        require(remoteCalls.get() == 1, "official accessory service must be called: " + remoteCalls);
        require("R2004+G20KX".equals(item.get("itemName")), "official accessory name must be resolved");
        require("ACC000000032".equals(item.get("itemCode")), "official accessory code must be resolved");
    }

    private LoginUser merchantLoginUser()
    {
        LoginUser loginUser = new LoginUser();
        loginUser.setClientType(Constants.CLIENT_TYPE_MERCHANT);
        loginUser.setTenantId(10001L);
        loginUser.setMemberId(27L);
        loginUser.setMerchantAuthStatus(RentalCode.AuthenticationStatus.VERIFIED.getCode());
        loginUser.setWarehouseScopeType(RentalCode.WarehouseScope.ALL.getCode());
        return loginUser;
    }

    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }
}
