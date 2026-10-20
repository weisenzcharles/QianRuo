package com.ruoyi.rentalbusiness.service;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.rentalbusiness.domain.MerchantOrderRow;
import com.ruoyi.rentalbusiness.mapper.MerchantInventoryMapper;
import com.ruoyi.rentalbusiness.mapper.MerchantOrderMapper;
import com.ruoyi.rentalbusiness.model.MerchantOrderPackageDeviceBindCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderPackageDeviceOptionVO;
import com.ruoyi.system.api.model.LoginUser;

/** 订单套餐逐件设备选择和快照绑定测试。 */
public class MerchantOrderPackageDeviceServiceTest
{
    @After
    public void clearContext()
    {
        SecurityContextHolder.remove();
    }

    /** 逐件套餐明细应只返回当前商品可用的库存设备。 */
    @Test
    public void list_serialPackageItem_returnsDeviceOptions()
    {
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        MerchantOrderRow orderRow = orderRow();
        MerchantOrderMapper orderMapper = orderMapper(orderRow, new AtomicReference<String>());
        MerchantInventoryMapper inventoryMapper = inventoryMapper();
        MerchantOrderPackageDeviceService service = new MerchantOrderPackageDeviceService(
                new MerchantOrderService(orderMapper, null), orderMapper, inventoryMapper, new ObjectMapper());

        List<MerchantOrderPackageDeviceOptionVO> options = service.list(11L, 0);

        require(options.size() == 1, "serial package item should have one candidate device");
        require("SN-701".equals(options.get(0).serialNumber), "serial number should be returned");
    }

    /** 绑定成功后应把设备编码和序列号写回订单套餐 JSON。 */
    @Test
    public void bind_serialPackageItem_writesBindingToSnapshot()
    {
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        AtomicReference<String> savedJson = new AtomicReference<>();
        MerchantOrderRow orderRow = orderRow();
        MerchantOrderMapper orderMapper = orderMapper(orderRow, savedJson);
        MerchantOrderPackageDeviceService service = new MerchantOrderPackageDeviceService(
                new MerchantOrderService(orderMapper, null), orderMapper, inventoryMapper(), new ObjectMapper());
        MerchantOrderPackageDeviceBindCommand command = new MerchantOrderPackageDeviceBindCommand();
        command.itemIndex = 0;
        command.unitIds = Collections.singletonList(701L);

        service.bind(11L, command);

        require(savedJson.get() != null && savedJson.get().contains("\"unitIds\":[\"701\"]"),
                "bound unit id must be persisted in the package snapshot");
        require(savedJson.get().contains("\"serialNumbers\":[\"SN-701\"]"),
                "bound serial number must be persisted in the package snapshot");
    }

    private MerchantOrderRow orderRow()
    {
        MerchantOrderRow row = new MerchantOrderRow();
        row.orderId = 11L;
        row.orderNo = "OD0000000011";
        row.warehouseId = 101L;
        row.packageId = 301L;
        row.orderItemId = 401L;
        row.packageDetailsJson = "{\"items\":[{\"itemRole\":1,\"productId\":501,"
                + "\"stockMode\":\"sn\",\"quantity\":1,\"itemName\":\"EOS R50\"}]}";
        return row;
    }

    private MerchantOrderMapper orderMapper(MerchantOrderRow row, AtomicReference<String> savedJson)
    {
        return (MerchantOrderMapper) Proxy.newProxyInstance(
                MerchantOrderMapper.class.getClassLoader(), new Class<?>[] {MerchantOrderMapper.class},
                (proxy, method, args) -> {
                    switch (method.getName())
                    {
                        case "selectMerchantOrderById":
                            return row;
                        case "countAcceptedAllianceChildren":
                            return 0;
                        case "updateOrderPackageDetails":
                            savedJson.set((String) args[3]);
                            return 1;
                        case "toString":
                            return "MerchantOrderMapperTestProxy";
                        default:
                            if (method.getReturnType().equals(int.class)) return 0;
                            if (method.getReturnType().equals(long.class)) return 0L;
                            if (method.getReturnType().equals(boolean.class)) return false;
                            if (method.getReturnType().equals(List.class)) return Collections.emptyList();
                            return null;
                    }
                });
    }

    private MerchantInventoryMapper inventoryMapper()
    {
        Map<String, Object> product = new HashMap<>();
        product.put("stockMode", "sn");
        Map<String, Object> device = new HashMap<>();
        device.put("unitId", 701L);
        device.put("unitCode", "UNIT-701");
        device.put("serialNumber", "SN-701");
        device.put("productName", "EOS R50");
        device.put("modelName", "EOS R50");
        device.put("warehouseName", "北京仓");
        return (MerchantInventoryMapper) Proxy.newProxyInstance(
                MerchantInventoryMapper.class.getClassLoader(), new Class<?>[] {MerchantInventoryMapper.class},
                (proxy, method, args) -> {
                    switch (method.getName())
                    {
                        case "selectProduct":
                            return product;
                        case "selectOrderPackageDeviceOptions":
                        case "selectOrderPackageDeviceOptionsForUpdate":
                            return Collections.singletonList(device);
                        case "toString":
                            return "MerchantInventoryMapperTestProxy";
                        default:
                            if (method.getReturnType().equals(int.class)) return 0;
                            if (method.getReturnType().equals(long.class)) return 0L;
                            if (method.getReturnType().equals(boolean.class)) return false;
                            if (method.getReturnType().equals(List.class)) return Collections.emptyList();
                            return null;
                    }
                });
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
