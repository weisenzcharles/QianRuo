package com.ruoyi.rentalbusiness.service;

import java.math.BigDecimal;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Test;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.merchant.api.RemoteMerchantAllianceService;
import com.ruoyi.rentalbusiness.mapper.MerchantInventoryMapper;
import com.ruoyi.rentalbusiness.model.MerchantInventoryBatchCommand;
import com.ruoyi.rentalbusiness.model.MerchantInventoryUnitTransferCommand;
import com.ruoyi.rentalbusiness.model.MerchantInventoryUnitUpdateCommand;
import com.ruoyi.system.api.model.LoginUser;

/** 商户库存序列号维护的订单关联和序列号约束测试。 */
public class MerchantInventoryServiceTest
{
    @After
    public void clearContext()
    {
        SecurityContextHolder.remove();
    }

    /** 已关联订单的库存单元必须拒绝删除。 */
    @Test
    public void deleteUnit_withOrderReference_rejectsDelete()
    {
        AtomicInteger deleteCalls = new AtomicInteger();
        MerchantInventoryMapper mapper = mapper(1, deleteCalls, 1);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());

        try
        {
            new MerchantInventoryService(mapper, (RemoteMerchantAllianceService) null).deleteUnit(301L);
            throw new AssertionError("associated inventory unit must not be deleted");
        }
        catch (ServiceException exception)
        {
            require("该设备已关联订单，不能删除".equals(exception.getMessage()), "unexpected delete error");
        }
        require(deleteCalls.get() == 0, "soft delete must not run for an order-associated unit");
    }

    /** 未关联订单的库存单元删除后应刷新库存汇总。 */
    @Test
    public void deleteUnit_withoutOrderReference_softDeletesAndRefreshesInventory()
    {
        AtomicInteger deleteCalls = new AtomicInteger();
        AtomicInteger refreshCalls = new AtomicInteger();
        MerchantInventoryMapper mapper = mapper(0, deleteCalls, refreshCalls);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());

        require(new MerchantInventoryService(mapper, (RemoteMerchantAllianceService) null).deleteUnit(301L),
                "unassociated inventory unit should be deleted");
        require(deleteCalls.get() == 1, "soft delete must run once");
        require(refreshCalls.get() == 1, "inventory totals must be refreshed once");
    }

    /** 修改只更新序列号，重复序列号必须被拒绝。 */
    @Test
    public void updateUnit_withDuplicateUnitCode_rejectsUpdate()
    {
        AtomicInteger updateCalls = new AtomicInteger();
        MerchantInventoryMapper mapper = mapper(0, new AtomicInteger(), new AtomicInteger(), 1, updateCalls);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        MerchantInventoryUnitUpdateCommand command = new MerchantInventoryUnitUpdateCommand();
        command.unitId = 301L;
        command.unitCode = "USED-001";

        try
        {
            new MerchantInventoryService(mapper, (RemoteMerchantAllianceService) null).updateUnit(command);
            throw new AssertionError("duplicate unit code must not be updated");
        }
        catch (ServiceException exception)
        {
            require("序列号已存在".equals(exception.getMessage()), "unexpected duplicate code error");
        }
        require(updateCalls.get() == 0, "duplicate unit code must not reach update SQL");
    }

    /** 编辑设备档案应保存详情，并在状态变化后刷新库存汇总。 */
    @Test
    public void updateUnit_withDetails_updatesUnitAndRefreshesInventory()
    {
        AtomicInteger updateCalls = new AtomicInteger();
        AtomicInteger refreshCalls = new AtomicInteger();
        MerchantInventoryMapper mapper = mapper(0, new AtomicInteger(), refreshCalls, 0, updateCalls);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        MerchantInventoryUnitUpdateCommand command = new MerchantInventoryUnitUpdateCommand();
        command.unitId = 301L;
        command.serialNumber = "SN-002";
        command.unitStatus = RentalCode.InventoryStatus.REPAIRING.getCode();
        command.memorySpec = "全画幅";
        command.purchaseCost = new BigDecimal("1999.00");
        command.conditionLevel = 95;
        command.insuranceUntil = "2027-01-01";
        command.insuranceAmount = new BigDecimal("88.00");
        command.remark = "定期保养";

        require(new MerchantInventoryService(mapper, (RemoteMerchantAllianceService) null).updateUnit(command),
                "unit details should be updated");
        require(updateCalls.get() == 1, "unit details should be updated once");
        require(refreshCalls.get() == 1, "inventory totals should refresh after status change");
    }

    /** 预占和租赁状态由订单流程维护，不能通过设备档案接口人工设置。 */
    @Test
    public void updateUnit_withWorkflowStatus_rejectsManualStatusChange()
    {
        AtomicInteger updateCalls = new AtomicInteger();
        MerchantInventoryMapper mapper = mapper(0, new AtomicInteger(), new AtomicInteger(), 0, updateCalls);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        MerchantInventoryUnitUpdateCommand command = new MerchantInventoryUnitUpdateCommand();
        command.unitId = 301L;
        command.serialNumber = "SN-002";
        command.unitStatus = RentalCode.InventoryStatus.RESERVED.getCode();

        try
        {
            new MerchantInventoryService(mapper, (RemoteMerchantAllianceService) null).updateUnit(command);
            throw new AssertionError("workflow status must not be changed manually");
        }
        catch (ServiceException exception)
        {
            require("已预占或租赁中的设备状态由订单流程管理，不能手动修改".equals(exception.getMessage()),
                    "unexpected workflow status error");
        }
        require(updateCalls.get() == 0, "workflow status rejection must not reach update SQL");
    }

    /** 成色必须使用系统字典约定的等级编码。 */
    @Test
    public void updateUnit_withUnsupportedConditionLevel_rejectsUpdate()
    {
        AtomicInteger updateCalls = new AtomicInteger();
        MerchantInventoryMapper mapper = mapper(0, new AtomicInteger(), new AtomicInteger(), 0, updateCalls);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        MerchantInventoryUnitUpdateCommand command = new MerchantInventoryUnitUpdateCommand();
        command.unitId = 301L;
        command.serialNumber = "SN-002";
        command.conditionLevel = 88;

        try
        {
            new MerchantInventoryService(mapper, (RemoteMerchantAllianceService) null).updateUnit(command);
            throw new AssertionError("unsupported condition level must be rejected");
        }
        catch (ServiceException exception)
        {
            require("成色只能选择全新、99新、95新、90新或80新".equals(exception.getMessage()),
                    "unexpected condition level error");
        }
        require(updateCalls.get() == 0, "unsupported condition level must not reach update SQL");
    }

    /** 可用且未关联订单的库存单元转仓后应更新归属并记录调货和两条库存流水。 */
    @Test
    public void transferUnit_withAvailableUnassociatedUnit_movesUnitAndWritesRecords()
    {
        AtomicInteger updateCalls = new AtomicInteger();
        AtomicInteger refreshCalls = new AtomicInteger();
        AtomicInteger transferCalls = new AtomicInteger();
        AtomicInteger transactionCalls = new AtomicInteger();
        MerchantInventoryMapper mapper = transferMapper(0, updateCalls, refreshCalls, transferCalls, transactionCalls);
        CodeGeneratorClient codeGeneratorClient = new CodeGeneratorClient(null)
        {
            @Override
            public String next(String codeType)
            {
                return "TR0000000001";
            }
        };
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        MerchantInventoryUnitTransferCommand command = new MerchantInventoryUnitTransferCommand();
        command.unitId = 301L;
        command.targetWarehouseId = 102L;

        require(new MerchantInventoryService(mapper, (RemoteMerchantAllianceService) null, codeGeneratorClient)
                .transferUnit(command), "available inventory unit should be transferred");
        require(updateCalls.get() == 1, "inventory unit should move once");
        require(refreshCalls.get() == 2, "source and target inventory totals should be refreshed");
        require(transferCalls.get() == 1, "completed transfer record should be written once");
        require(transactionCalls.get() == 2, "source and target inventory ledgers should be written");
    }

    /** 已关联订单的库存单元不能通过转仓绕过订单占用约束。 */
    @Test
    public void transferUnit_withOrderReference_rejectsTransfer()
    {
        AtomicInteger updateCalls = new AtomicInteger();
        MerchantInventoryMapper mapper = transferMapper(1, updateCalls, new AtomicInteger(), new AtomicInteger(),
                new AtomicInteger());
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        MerchantInventoryUnitTransferCommand command = new MerchantInventoryUnitTransferCommand();
        command.unitId = 301L;
        command.targetWarehouseId = 102L;

        try
        {
            new MerchantInventoryService(mapper, (RemoteMerchantAllianceService) null).transferUnit(command);
            throw new AssertionError("order-associated inventory unit must not be transferred");
        }
        catch (ServiceException exception)
        {
            require("该设备已关联订单，不能转仓".equals(exception.getMessage()), "unexpected transfer error");
        }
        require(updateCalls.get() == 0, "order-associated inventory unit must not reach update SQL");
    }

    /** 数量型商品按仓库初始化汇总库存，不应创建序列号明细。 */
    @Test
    public void addBatch_withQuantityProduct_initializesQuantityInventory()
    {
        AtomicInteger quantityInsertCalls = new AtomicInteger();
        AtomicInteger batchInsertCalls = new AtomicInteger();
        AtomicInteger batchUpdateCalls = new AtomicInteger();
        AtomicReference<Integer> insertedQuantity = new AtomicReference<>();
        AtomicReference<Boolean> batchCreated = new AtomicReference<>(false);
        Map<String, Object> product = new HashMap<>();
        product.put("stockMode", RentalCode.StockMode.QUANTITY.getCode());
        product.put("modelId", 501L);
        Map<String, Object> batch = new HashMap<>();
        batch.put("batchId", 701L);
        batch.put("totalQuantity", 12);
        MerchantInventoryMapper mapper = (MerchantInventoryMapper) Proxy.newProxyInstance(
                MerchantInventoryMapper.class.getClassLoader(), new Class<?>[] {MerchantInventoryMapper.class},
                (proxy, method, args) -> {
                    switch (method.getName())
                    {
                        case "selectWarehouse":
                            return Collections.singletonMap("warehouseName", "测试仓库");
                        case "selectProduct":
                            return product;
                        case "selectBatchByIdempotency":
                            return batchCreated.get() ? batch : null;
                        case "selectInventoryByScope":
                            return null;
                        case "insertBatch":
                            batchInsertCalls.incrementAndGet();
                            return 1;
                        case "selectLastInsertId":
                            return 701L;
                        case "insertQuantityInventory":
                            insertedQuantity.set((Integer) args[5]);
                            quantityInsertCalls.incrementAndGet();
                            return 1;
                        case "updateBatch":
                            batchCreated.set(true);
                            batchUpdateCalls.incrementAndGet();
                            return 1;
                        case "toString":
                            return "MerchantInventoryQuantityMapperTestProxy";
                        default:
                            if (method.getReturnType().equals(int.class)) return 0;
                            if (method.getReturnType().equals(long.class)) return 0L;
                            if (method.getReturnType().equals(boolean.class)) return false;
                            if (method.getReturnType().equals(java.util.List.class)) return Collections.emptyList();
                            return null;
                    }
                });
        CodeGeneratorClient codeGeneratorClient = new CodeGeneratorClient(null)
        {
            @Override
            public String next(String codeType)
            {
                return "IB0000000001";
            }
        };
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        MerchantInventoryBatchCommand command = new MerchantInventoryBatchCommand();
        command.productId = 401L;
        command.warehouseId = 101L;
        command.quantity = 12;

        Map<String, Object> result = new MerchantInventoryService(mapper, (RemoteMerchantAllianceService) null,
                codeGeneratorClient).addBatch(command);

        require(result == batch, "quantity inventory should return the completed batch");
        require(batchInsertCalls.get() == 1, "quantity inventory should create one batch");
        require(quantityInsertCalls.get() == 1, "quantity inventory summary should be inserted once");
        require(Integer.valueOf(12).equals(insertedQuantity.get()), "submitted quantity should reach inventory SQL");
        require(batchUpdateCalls.get() == 1, "quantity batch should be marked successful");
    }

    /** 数量库存详情只返回当前商户该库存仍在预占中的订单。 */
    @Test
    public void reservationOrders_returnsCurrentInventoryReservations()
    {
        AtomicReference<Long> requestedInventoryId = new AtomicReference<>();
        java.util.List<Map<String, Object>> expected = Collections.singletonList(
                Collections.singletonMap("orderNo", "OD0001"));
        MerchantInventoryMapper mapper = (MerchantInventoryMapper) Proxy.newProxyInstance(
                MerchantInventoryMapper.class.getClassLoader(), new Class<?>[] {MerchantInventoryMapper.class},
                (proxy, method, args) -> {
                    if ("selectQuantityReservationOrders".equals(method.getName()))
                    {
                        requestedInventoryId.set((Long) args[2]);
                        return expected;
                    }
                    if ("toString".equals(method.getName())) return "MerchantInventoryReservationOrderMapperTestProxy";
                    if (method.getReturnType().equals(int.class)) return 0;
                    if (method.getReturnType().equals(long.class)) return 0L;
                    if (method.getReturnType().equals(boolean.class)) return false;
                    if (method.getReturnType().equals(java.util.List.class)) return Collections.emptyList();
                    return null;
                });
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());

        java.util.List<Map<String, Object>> actual = new MerchantInventoryService(mapper,
                (RemoteMerchantAllianceService) null).reservationOrders(201L);

        require(actual == expected, "reservation order rows should be returned");
        require(Long.valueOf(201L).equals(requestedInventoryId.get()), "inventory scope must reach the mapper");
    }

    /** 归还已开始但尚未回库的订单仍计入预计归还趋势。 */
    @Test
    @SuppressWarnings("unchecked")
    public void inboundOutboundTrend_withReturningOrders_includesReturningStatus()
    {
        AtomicReference<java.util.List<String>> returnStatuses = new AtomicReference<>();
        AtomicReference<Boolean> showAllRequested = new AtomicReference<>();
        MerchantInventoryMapper mapper = (MerchantInventoryMapper) Proxy.newProxyInstance(
                MerchantInventoryMapper.class.getClassLoader(), new Class<?>[] {MerchantInventoryMapper.class},
                (proxy, method, args) -> {
                    if ("countPendingShipOrders".equals(method.getName()))
                    {
                        return 0L;
                    }
                    if ("selectInboundOutboundTrend".equals(method.getName()))
                    {
                        returnStatuses.set((java.util.List<String>) args[8]);
                        showAllRequested.set((Boolean) args[11]);
                        return Collections.emptyList();
                    }
                    return null;
                });
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());

        new MerchantInventoryService(mapper, (RemoteMerchantAllianceService) null)
                .inboundOutboundTrend("model", null, null, null, 30, true);

        require(returnStatuses.get() != null && returnStatuses.get().contains("45"),
                "return-in-progress status code must be included");
        require(returnStatuses.get().contains("returning"),
                "return-in-progress legacy status must be included");
        require(Boolean.TRUE.equals(showAllRequested.get()),
                "show-all selection must reach the trend query");
    }

    private MerchantInventoryMapper mapper(int orderReferences, AtomicInteger deleteCalls, int refreshCalls)
    {
        return mapper(orderReferences, deleteCalls, new AtomicInteger(refreshCalls));
    }

    private MerchantInventoryMapper mapper(int orderReferences, AtomicInteger deleteCalls, AtomicInteger refreshCalls)
    {
        return mapper(orderReferences, deleteCalls, refreshCalls, 0, new AtomicInteger());
    }

    private MerchantInventoryMapper mapper(int orderReferences, AtomicInteger deleteCalls, AtomicInteger refreshCalls,
            int duplicateCodes, AtomicInteger updateCalls)
    {
        Map<String, Object> unit = new HashMap<>();
        unit.put("unitId", 301L);
        unit.put("inventoryId", 201L);
        unit.put("warehouseId", 101L);
        unit.put("unitCode", "OLD-001");
        unit.put("unitStatus", RentalCode.InventoryStatus.AVAILABLE.getCode());
        return (MerchantInventoryMapper) Proxy.newProxyInstance(
                MerchantInventoryMapper.class.getClassLoader(), new Class<?>[] {MerchantInventoryMapper.class},
                (proxy, method, args) -> {
                    switch (method.getName())
                    {
                        case "selectUnitForUpdate":
                            return unit;
                        case "countSerialNumber":
                            return duplicateCodes;
                        case "countUnitOrderReferences":
                            return orderReferences;
                        case "countUnitCode":
                            return duplicateCodes;
                        case "softDeleteUnit":
                            deleteCalls.incrementAndGet();
                            return 1;
                        case "refreshInventoryCounts":
                            refreshCalls.incrementAndGet();
                            return 1;
                        case "updateUnitDetails":
                            updateCalls.incrementAndGet();
                            return 1;
                        case "toString":
                            return "MerchantInventoryMapperTestProxy";
                        default:
                            if (method.getReturnType().equals(int.class)) return 0;
                            if (method.getReturnType().equals(long.class)) return 0L;
                            if (method.getReturnType().equals(boolean.class)) return false;
                            if (method.getReturnType().equals(java.util.List.class)) return Collections.emptyList();
                            return null;
                    }
                });
    }

    private MerchantInventoryMapper transferMapper(int orderReferences, AtomicInteger updateCalls,
            AtomicInteger refreshCalls, AtomicInteger transferCalls, AtomicInteger transactionCalls)
    {
        Map<String, Object> unit = new HashMap<>();
        unit.put("unitId", 301L);
        unit.put("inventoryId", 201L);
        unit.put("warehouseId", 101L);
        unit.put("productId", 401L);
        unit.put("modelId", 501L);
        unit.put("unitStatus", 1);

        Map<String, Object> targetScope = new HashMap<>();
        targetScope.put("inventoryId", 202L);

        Map<String, Object> sourceBefore = inventory(201L, 2, 1);
        Map<String, Object> targetBefore = inventory(202L, 0, 1);
        Map<String, Object> sourceAfter = inventory(201L, 1, 1);
        Map<String, Object> targetAfter = inventory(202L, 1, 1);
        AtomicInteger sourceLocks = new AtomicInteger();
        AtomicInteger targetLocks = new AtomicInteger();
        return (MerchantInventoryMapper) Proxy.newProxyInstance(
                MerchantInventoryMapper.class.getClassLoader(), new Class<?>[] {MerchantInventoryMapper.class},
                (proxy, method, args) -> {
                    switch (method.getName())
                    {
                        case "selectUnitForUpdate":
                            return unit;
                        case "selectWarehouse":
                            return Collections.singletonMap("warehouseName", "测试仓库");
                        case "countUnitOrderReferences":
                            return orderReferences;
                        case "selectInventoryByScope":
                            return targetScope;
                        case "selectInventoryForUpdate":
                            Long warehouseId = ((Number) args[3]).longValue();
                            if (warehouseId == 101L)
                                return sourceLocks.incrementAndGet() > 1 ? sourceAfter : sourceBefore;
                            return targetLocks.incrementAndGet() > 1 ? targetAfter : targetBefore;
                        case "updateUnitWarehouse":
                            updateCalls.incrementAndGet();
                            return 1;
                        case "refreshInventoryCounts":
                            refreshCalls.incrementAndGet();
                            return 1;
                        case "insertTransferOrder":
                            transferCalls.incrementAndGet();
                            return 1;
                        case "insertInventoryTransaction":
                            transactionCalls.incrementAndGet();
                            return 1;
                        case "toString":
                            return "MerchantInventoryTransferMapperTestProxy";
                        default:
                            if (method.getReturnType().equals(int.class)) return 0;
                            if (method.getReturnType().equals(long.class)) return 0L;
                            if (method.getReturnType().equals(boolean.class)) return false;
                            if (method.getReturnType().equals(java.util.List.class)) return Collections.emptyList();
                            return null;
                    }
                });
    }

    private Map<String, Object> inventory(long inventoryId, int availableQuantity, int stockMode)
    {
        Map<String, Object> inventory = new HashMap<>();
        inventory.put("inventoryId", inventoryId);
        inventory.put("availableQuantity", availableQuantity);
        inventory.put("stockMode", stockMode);
        return inventory;
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
