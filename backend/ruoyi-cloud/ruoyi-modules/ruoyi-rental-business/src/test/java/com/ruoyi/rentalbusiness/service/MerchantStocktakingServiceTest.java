package com.ruoyi.rentalbusiness.service;

import java.lang.reflect.Proxy;
import java.util.Map;
import org.junit.After;
import org.junit.Test;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.rentalbusiness.mapper.MerchantStocktakingMapper;
import com.ruoyi.rentalbusiness.model.MerchantStocktakingScanCommand;
import com.ruoyi.system.api.model.LoginUser;

/** 商户盘点状态及方式从数据库数值映射后的校验测试。 */
public class MerchantStocktakingServiceTest
{
    @After
    public void clearContext()
    {
        SecurityContextHolder.remove();
    }

    /** 新建扫码盘点为进行中时允许扫描，即使查询结果中的状态以 Long 返回。 */
    @Test
    public void scan_processingStatusReturnedAsLong_recordsScan()
    {
        setMerchantContext();
        MerchantStocktakingMapper mapper = mapper((method, args) -> {
            if ("selectStocktakingForUpdate".equals(method))
                return Map.of("warehouseId", 8L, "mode", 1L, "status", 0L);
            if ("countDuplicateScan".equals(method))
                return 0;
            if ("selectUnitByCode".equals(method))
                return null;
            if ("insertScanItem".equals(method) || "incrementAbnormalCount".equals(method))
                return 1;
            return null;
        });
        MerchantStocktakingScanCommand command = new MerchantStocktakingScanCommand();
        command.code = "UNKNOWN-SN";

        Map<String, Object> result = new MerchantStocktakingService(mapper, null, null).scan(12L, command);

        require(Boolean.FALSE.equals(result.get("duplicate")), "new scan stocktaking must accept a scan");
    }

    /** 手工盘点未录入完数量时不能完成，且查询结果中的模式为 Long 时仍按手工盘点处理。 */
    @Test
    public void complete_manualProcessingStatusReturnedAsLong_rejectsUncountedItems()
    {
        setMerchantContext();
        MerchantStocktakingMapper mapper = mapper((method, args) -> {
            if ("selectStocktakingForUpdate".equals(method))
                return Map.of("warehouseId", 8L, "mode", 0L, "status", 0L);
            if ("countUncountedItems".equals(method))
                return 1;
            return null;
        });

        try
        {
            new MerchantStocktakingService(mapper, null, null).complete(12L);
            throw new AssertionError("incomplete manual stocktaking must not be completed");
        }
        catch (ServiceException exception)
        {
            require("请先填写全部商品的实盘数量".equals(exception.getMessage()),
                    "manual completion must reject uncounted items: " + exception.getMessage());
        }
    }

    private void setMerchantContext()
    {
        LoginUser loginUser = new LoginUser();
        loginUser.setClientType(Constants.CLIENT_TYPE_MERCHANT);
        loginUser.setTenantId(10001L);
        loginUser.setMemberId(27L);
        loginUser.setMerchantAuthStatus(RentalCode.AuthenticationStatus.VERIFIED.getCode());
        loginUser.setWarehouseScopeType(RentalCode.WarehouseScope.ALL.getCode());
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);
    }

    private MerchantStocktakingMapper mapper(MapperCall mapperCall)
    {
        return (MerchantStocktakingMapper) Proxy.newProxyInstance(MerchantStocktakingMapper.class.getClassLoader(),
                new Class<?>[] {MerchantStocktakingMapper.class},
                (proxy, method, args) -> mapperCall.invoke(method.getName(), args));
    }

    private void require(boolean condition, String message)
    {
        if (!condition)
            throw new AssertionError(message);
    }

    @FunctionalInterface
    private interface MapperCall
    {
        Object invoke(String method, Object[] args);
    }
}
