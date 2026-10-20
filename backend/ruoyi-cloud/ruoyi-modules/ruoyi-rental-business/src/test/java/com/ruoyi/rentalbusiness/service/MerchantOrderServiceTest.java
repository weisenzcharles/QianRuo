package com.ruoyi.rentalbusiness.service;

import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.merchant.api.RemoteMerchantUserService;
import com.ruoyi.merchant.api.model.MerchantUserSummary;
import com.ruoyi.rentalbusiness.domain.MerchantOrderRow;
import com.ruoyi.rentalbusiness.mapper.MerchantOrderMapper;
import com.ruoyi.rentalbusiness.mapper.MerchantPackageMapper;
import com.ruoyi.rentalbusiness.model.MerchantAuditReferenceModel;
import com.ruoyi.rentalbusiness.model.MerchantOrderCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderDetailVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderAllianceLinkModel;
import com.ruoyi.rentalbusiness.model.MerchantOrderNoteVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderEditLogVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderSummaryVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderRecipientUpdateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderLogisticsUpdateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderQuery;
import com.ruoyi.rentalbusiness.model.MerchantOrderShipCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderReturnInspectionCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderReturnInspectionVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderReturnReadinessVO;
import com.ruoyi.rentalbusiness.enums.MerchantReturnInspectionResult;
import com.ruoyi.system.api.RemoteAccessoryService;
import com.ruoyi.system.api.model.PlatformAccessoryOption;
import com.ruoyi.system.api.model.LoginUser;

/** 商户订单应用服务的租户、商户和仓库范围测试。 */
public class MerchantOrderServiceTest
{
    /** 清理线程级登录上下文，避免测试之间复用商户身份。 */
    @After
    public void clearContext()
    {
        SecurityContextHolder.remove();
    }

    /** 验证客户端提交的仓库范围会被可信登录上下文覆盖。 */
    @Test
    public void listUsesAuthenticatedMerchantAndWarehouseScope()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderService service = new MerchantOrderService(mapper);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());

        MerchantOrderQuery query = new MerchantOrderQuery();
        query.setWarehouseIds(Collections.singleton(999L));
        query.setAllWarehouseAccess(true);

        service.list(query);

        require(Long.valueOf(10001L).equals(mapper.tenantId), "tenant must come from login context");
        require(Long.valueOf(27L).equals(mapper.merchantId), "merchant must come from login context");
        require(!mapper.query.isAllWarehouseAccess(), "client must not expand warehouse scope");
        require(mapper.query.getWarehouseIds().equals(Collections.singleton(101L)),
                "warehouse ids must come from login context");
    }

    /** 验证独立订单总数查询同样只能使用登录上下文中的商户和仓库范围。 */
    @Test
    public void countUsesAuthenticatedMerchantAndWarehouseScope()
    {
        CapturingMapper mapper = new CapturingMapper();
        mapper.count = 14L;
        MerchantOrderService service = new MerchantOrderService(mapper);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());

        MerchantOrderQuery query = new MerchantOrderQuery();
        query.setWarehouseIds(Collections.singleton(999L));
        query.setAllWarehouseAccess(true);

        long count = service.count(query);

        require(count == 14L, "merchant order count must be returned");
        require(Long.valueOf(10001L).equals(mapper.countTenantId),
                "count tenant must come from login context");
        require(Long.valueOf(27L).equals(mapper.countMerchantId),
                "count merchant must come from login context");
        require(!mapper.countQuery.isAllWarehouseAccess(),
                "count query must not trust client warehouse scope");
        require(mapper.countQuery.getWarehouseIds().equals(Collections.singleton(101L)),
                "count warehouse ids must come from login context");
    }

    /** 验证订单快捷视图只接受预置编码，未知编码不会进入查询条件。 */
    @Test
    public void listNormalizesOrderScope()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderService service = new MerchantOrderService(mapper);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());

        MerchantOrderQuery query = new MerchantOrderQuery();
        query.setOrderScope(" today-shipping ");
        service.list(query);
        require(MerchantOrderQuery.SCOPE_TODAY_SHIPPING.equals(mapper.query.getOrderScope()),
                "supported order scope must be normalized");

        query.setOrderScope("unknown-scope");
        service.list(query);
        require(mapper.query.getOrderScope() == null,
                "unknown order scope must be ignored");
    }

    /** 验证订单日期范围和排序参数会被规范化为受控编码，并默认倒序创建时间。 */
    @Test
    public void listNormalizesDateRangeAndSortOptions()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderService service = new MerchantOrderService(mapper);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());

        MerchantOrderQuery query = new MerchantOrderQuery();
        query.setDateStart(" 2026-09-01 ");
        query.setDateEnd("2026-09-07");
        query.setDateType("return");
        query.setSortType("status");
        query.setSortDirection("asc");

        service.list(query);

        require("2026-09-01".equals(mapper.query.getDateStart()),
                "date start must be normalized");
        require("2026-09-07".equals(mapper.query.getDateEnd()),
                "date end must be normalized");
        require("return".equals(mapper.query.getDateTypeCode()),
                "date type must be whitelisted");
        require("status".equals(mapper.query.getSortTypeCode()),
                "sort type must be whitelisted");
        require("asc".equals(mapper.query.getSortDirectionCode()),
                "sort direction must be whitelisted");

        query.setSortType("unknown");
        query.setSortDirection("unknown");
        service.list(query);
        require("create".equals(mapper.query.getSortTypeCode()),
                "unknown sort type must use create time");
        require("desc".equals(mapper.query.getSortDirectionCode()),
                "unknown sort direction must use descending order");
    }

    /** 验证批注历史先校验订单归属，再通过一次用户摘要调用解析操作人昵称。 */
    @Test
    public void listNotesResolvesOperatorNicknameWithoutCrossDatabaseJoin()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderRow order = new MerchantOrderRow();
        order.orderId = 9001L;
        mapper.detailRow = order;
        MerchantOrderNoteVO note = new MerchantOrderNoteVO();
        note.noteId = 1L;
        note.operatorId = 3001L;
        note.noteContent = "待确认收件地址";
        mapper.notes = Collections.singletonList(note);

        MerchantUserSummary summary = new MerchantUserSummary();
        summary.setUserId(3001L);
        summary.setUsername("user-3001");
        summary.setNickname("豆豆");
        RemoteMerchantUserService remote = (RemoteMerchantUserService) Proxy.newProxyInstance(
                RemoteMerchantUserService.class.getClassLoader(),
                new Class<?>[] {RemoteMerchantUserService.class},
                (proxy, method, args) -> "getSummaries".equals(method.getName())
                        ? R.ok(Collections.singletonList(summary)) : null);

        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        MerchantOrderService service = new MerchantOrderService(mapper, null, null, null, null, null, remote);
        List<MerchantOrderNoteVO> result = service.listNotes(9001L);

        require(result.size() == 1, "order note history must be returned");
        require("豆豆".equals(result.get(0).operatorName),
                "order note history must display the merchant user nickname");
    }

    /** 验证订单列表按当前页批量解析录入人和对接人名称，不跨库 JOIN 用户表。 */
    @Test
    public void listResolvesCreatorAndCoordinatorNamesFromMerchantUsers()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderRow order = new MerchantOrderRow();
        order.orderId = 9002L;
        order.orderNo = "ORD-9002";
        order.createBy = 3001L;
        order.updateBy = 3002L;
        mapper.rows = Collections.singletonList(order);

        MerchantUserSummary creator = new MerchantUserSummary();
        creator.setUserId(3001L);
        creator.setUsername("creator-3001");
        creator.setNickname("录入员");
        MerchantUserSummary coordinator = new MerchantUserSummary();
        coordinator.setUserId(3002L);
        coordinator.setUsername("coordinator-3002");
        coordinator.setNickname("对接员");
        List<MerchantUserSummary> summaries = Arrays.asList(creator, coordinator);
        final int[] remoteCallCount = {0};
        RemoteMerchantUserService remote = (RemoteMerchantUserService) Proxy.newProxyInstance(
                RemoteMerchantUserService.class.getClassLoader(),
                new Class<?>[] {RemoteMerchantUserService.class},
                (proxy, method, args) -> {
                    if ("getSummaries".equals(method.getName()))
                    {
                        remoteCallCount[0]++;
                        return R.ok(summaries);
                    }
                    return null;
                });

        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        MerchantOrderService service = new MerchantOrderService(mapper, null, null, null, null, null, remote);
        List<com.ruoyi.rentalbusiness.model.MerchantOrderListVO> result = service.list(new MerchantOrderQuery());

        require(result.size() == 1, "merchant order list must return the mapped order");
        require("录入员".equals(result.get(0).operator),
                "order creator must display the nickname resolved from create_by");
        require("对接员".equals(result.get(0).coordinator),
                "order coordinator must display the nickname resolved from update_by");
        require(remoteCallCount[0] == 1, "order list must batch call merchant user service once per page");
    }

    /** 验证订单列表不会同步调用平台服务，且不会把官方配件唯一编号返回到页面。 */
    @Test
    public void listMasksOfficialPackageAccessoryTokensWithoutRemoteLookup()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderRow order = new MerchantOrderRow();
        order.orderId = 9006L;
        order.orderNo = "ORD-9006";
        order.packageDetailsJson = "{\"packageName\":\"Pocket 3 套餐\",\"mainProduct\":{\"productName\":\"Pocket 3\"},\"items\":[{\"itemRole\":1,\"itemName\":\"Pocket 3\"},{\"itemRole\":2,\"itemName\":\"__OFFICIAL_ACCESSORY__:1\"},{\"itemRole\":3,\"itemName\":\"备用电池\"}]}";
        mapper.rows = Collections.singletonList(order);

        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        MerchantOrderService service = new MerchantOrderService(mapper, null);
        List<com.ruoyi.rentalbusiness.model.MerchantOrderListVO> result = service.list(new MerchantOrderQuery());

        require(result.size() == 1, "merchant order list must return the mapped order");
        require(result.get(0).packageItems.equals(Arrays.asList("Pocket 3", "官方配件", "备用电池")),
                "official package accessory token must not expose its technical id");
    }

    /** 验证订单列表会补齐官方配件名称，并返回套餐主商品和明细的结构化展示数据。 */
    @Test
    public void listResolvesOfficialAccessoryNameAndPackageDetail()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderRow order = new MerchantOrderRow();
        order.orderId = 9007L;
        order.orderNo = "ORD-9007";
        order.packageDetailsJson = "{\"packageName\":\"Pocket 3 套餐\",\"packageCode\":\"PKG-001\","
                + "\"mainProduct\":{\"productName\":\"Pocket 3\",\"productCode\":\"P-001\","
                + "\"modelName\":\"Pocket 3\"},\"items\":["
                + "{\"itemRole\":1,\"itemName\":\"Pocket 3\",\"itemCode\":\"P-001\","
                + "\"itemModelName\":\"Pocket 3\",\"quantity\":1,\"requiredFlag\":1,"
                + "\"selectableDevice\":true},"
                + "{\"itemRole\":2,\"accessoryId\":1,\"itemName\":\"官方配件\","
                + "\"quantity\":2,\"requiredFlag\":0}],"
                + "\"extraAccessories\":[{\"itemName\":\"备用电池\",\"quantity\":2}]}";
        mapper.rows = Collections.singletonList(order);

        PlatformAccessoryOption accessory = new PlatformAccessoryOption();
        accessory.setAccessoryId(1L);
        accessory.setAccessoryName("DJI Mic 2");
        RemoteAccessoryService remote = (RemoteAccessoryService) Proxy.newProxyInstance(
                RemoteAccessoryService.class.getClassLoader(),
                new Class<?>[] {RemoteAccessoryService.class},
                (proxy, method, args) -> "listActive".equals(method.getName())
                        ? R.ok(Collections.singletonList(accessory)) : null);

        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        MerchantOrderService service = new MerchantOrderService(mapper, null, null, null, null, null, null, remote);
        List<com.ruoyi.rentalbusiness.model.MerchantOrderListVO> result = service.list(new MerchantOrderQuery());

        require(result.size() == 1, "merchant order list must return the mapped order");
        require(result.get(0).packageItems.equals(Arrays.asList("Pocket 3", "DJI Mic 2")),
                "official package accessory must display its configured name");
        require("PKG-001".equals(result.get(0).packageDetail.packageCode),
                "package detail must display the package code");
        require("DJI Mic 2".equals(result.get(0).packageDetail.items.get(1).name),
                "package detail must display the official accessory name");
        require("官方配件".equals(result.get(0).packageDetail.items.get(1).itemType),
                "package detail must identify the official accessory type");
        require(Integer.valueOf(2).equals(result.get(0).packageDetail.items.get(1).quantity),
                "package detail must display the configured quantity");
        require("可选".equals(result.get(0).packageDetail.items.get(1).requiredText),
                "package detail must display the required flag");
        require(result.get(0).packageDetail.items.get(0).selectableDevice,
                "package detail must expose the serial-device selection flag");
        require(result.get(0).packageDetail.extraAccessories.equals(Collections.singletonList("备用电池 x2")),
                "package detail must expose extra accessories");
    }

    /** 验证编辑订单切换套餐时会把官方配件详情固化到订单 JSON 快照。 */
    @Test
    public void updatePackageDetailsStoresOfficialAccessoryDetailsInSnapshot()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantPackageMapper packageMapper = (MerchantPackageMapper) Proxy.newProxyInstance(
                MerchantPackageMapper.class.getClassLoader(), new Class<?>[] {MerchantPackageMapper.class},
                (proxy, method, args) -> {
                    if ("selectById".equals(method.getName()))
                    {
                        return Map.of("packageName", "Pocket 3 标准套餐", "packageCode", "PKG-003", "modelId", 4L);
                    }
                    if ("selectVersionById".equals(method.getName()))
                    {
                        return Map.of("packageVersionId", 33L);
                    }
                    if ("selectItems".equals(method.getName()))
                    {
                        return Arrays.asList(
                                Map.of("itemRole", 1, "productId", 19L, "itemCode", "P-019",
                                        "itemName", "Pocket 3", "itemModelName", "Pocket 3", "quantity", 1,
                                        "stockMode", 1),
                                Map.of("itemRole", 2, "accessoryId", 7L, "itemName", "官方配件", "quantity", 2));
                    }
                    return null;
                });
        PlatformAccessoryOption officialAccessory = new PlatformAccessoryOption();
        officialAccessory.setAccessoryId(7L);
        officialAccessory.setAccessoryCode("ACC-007");
        officialAccessory.setAccessoryName("DJI Mic 2");
        officialAccessory.setSpecification("一拖二");
        officialAccessory.setUnit("套");
        RemoteAccessoryService remote = (RemoteAccessoryService) Proxy.newProxyInstance(
                RemoteAccessoryService.class.getClassLoader(), new Class<?>[] {RemoteAccessoryService.class},
                (proxy, method, args) -> "listActive".equals(method.getName())
                        ? R.ok(Collections.singletonList(officialAccessory)) : null);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        MerchantOrderService service = new MerchantOrderService(mapper, null, null, null, null, null, null,
                new ObjectMapper(), packageMapper, null, remote);
        MerchantOrderCreateCommand command = new MerchantOrderCreateCommand();
        command.orderId = 9008L;
        command.packageId = 13L;

        try
        {
            Method method = MerchantOrderService.class.getDeclaredMethod("updatePackageDetails",
                    MerchantOrderCreateCommand.class);
            method.setAccessible(true);
            method.invoke(service, command);
            JsonNode snapshot = new ObjectMapper().readTree(mapper.packageDetailsJson);
            JsonNode mainItem = snapshot.path("items").get(0);
            require(mainItem.path("selectableDevice").asBoolean(),
                    "snapshot must store the serial-device selection flag");
            JsonNode officialItem = snapshot.path("items").get(1);
            require("DJI Mic 2".equals(officialItem.path("itemName").asText()),
                    "snapshot must store the official accessory name");
            require("ACC-007".equals(officialItem.path("itemCode").asText()),
                    "snapshot must store the official accessory code");
            require("一拖二".equals(officialItem.path("itemSpecification").asText()),
                    "snapshot must store the official accessory specification");
            require("套".equals(officialItem.path("itemUnit").asText()),
                    "snapshot must store the official accessory unit");
        }
        catch (ReflectiveOperationException exception)
        {
            throw new AssertionError("package snapshot should be generated", exception);
        }
        catch (java.io.IOException exception)
        {
            throw new AssertionError("package snapshot should be valid JSON", exception);
        }
    }

    /** 验证预计到库按应归还时间加发货到租赁开始的日历天数差计算。 */
    @Test
    public void listCalculatesExpectedArriveDateFromLeaseSchedule()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderRow order = new MerchantOrderRow();
        order.orderId = 9004L;
        order.orderNo = "ORD-9004";
        order.leaseStartDate = parseDate("2026-09-03");
        order.shipDueTime = parseDate("2026-09-01 09:00:00");
        order.returnDueTime = parseDate("2026-09-10 18:00:00");
        mapper.rows = Collections.singletonList(order);

        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        MerchantOrderService service = new MerchantOrderService(mapper, null);
        List<com.ruoyi.rentalbusiness.model.MerchantOrderListVO> result = service.list(new MerchantOrderQuery());

        require("2026-09-12".equals(result.get(0).delivery.expectedDate),
                "expected arrive date must add the lease-start and ship-due calendar day difference");
    }

    /** 备货只更新状态，不调用整单更新，避免覆盖订单其他字段。 */
    @Test
    public void prepareUpdatesOnlyOrderStatus()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderRow order = new MerchantOrderRow();
        order.orderId = 9005L;
        order.orderNo = "ORD-9005";
        order.orderStatus = RentalCode.OrderStatus.PENDING_SHIP.getCode();
        mapper.detailRow = order;

        LoginUser loginUser = merchantLoginUser();
        loginUser.setMerchantAuthStatus(RentalCode.AuthenticationStatus.VERIFIED.getCode());
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);
        MerchantOrderService service = new MerchantOrderService(mapper);

        service.prepare(order.orderId);

        require(Long.valueOf(9005L).equals(mapper.updatedOrderId),
                "prepare must update the requested order only");
        require(RentalCode.OrderStatus.PENDING_SHIP.getCode().equals(mapper.updatedFromStatus),
                "prepare must require the pending-ship status");
        require(RentalCode.OrderStatus.PREPARED.getCode().equals(mapper.updatedToStatus),
                "prepare must move the order to prepared");
        require(mapper.genericOrderUpdateCount == 0,
                "prepare must not call the full order update mapper");
    }

    /** 归还验机正常时必须先保存验机事实，再推进订单到已完成。 */
    @Test
    public void returnInspectionPersistsNormalResultBeforeCompletion()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderRow order = new MerchantOrderRow();
        order.orderId = 9013L;
        order.orderNo = "ORD-9013";
        order.orderStatus = RentalCode.OrderStatus.RETURNING.getCode();
        mapper.detailRow = order;

        LoginUser loginUser = merchantLoginUser();
        loginUser.setMerchantAuthStatus(RentalCode.AuthenticationStatus.VERIFIED.getCode());
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);

        MerchantOrderReturnInspectionCommand command = new MerchantOrderReturnInspectionCommand();
        command.result = 1;
        command.customerContacted = 1;
        command.remark = "设备外观正常";

        new MerchantOrderService(mapper).returnInspection(order.orderId, command);

        require(mapper.inspectionOrderId.equals(order.orderId),
                "return inspection must be stored against the current order");
        require(mapper.insertedReturnInspection == command,
                "return inspection must persist the submitted inspection fields");
        require(RentalCode.OrderStatus.COMPLETED.getCode().equals(mapper.updatedToStatus),
                "normal return inspection must complete the order");
    }

    /** 逐件订单部分归还时不能提交整单验机。 */
    @Test
    public void returnInspectionRejectsPartialSerialDeviceReturn()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderRow order = new MerchantOrderRow();
        order.orderId = 9017L;
        order.orderNo = "ORD-9017";
        order.orderStatus = RentalCode.OrderStatus.RETURNING.getCode();
        mapper.detailRow = order;
        mapper.returnReadiness = new MerchantOrderReturnReadinessVO();
        mapper.returnReadiness.boundUnitCount = 2;
        mapper.returnReadiness.returnedUnitCount = 1;

        LoginUser loginUser = merchantLoginUser();
        loginUser.setMerchantAuthStatus(RentalCode.AuthenticationStatus.VERIFIED.getCode());
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);

        MerchantOrderReturnInspectionCommand command = new MerchantOrderReturnInspectionCommand();
        command.result = MerchantReturnInspectionResult.NORMAL.getCode();
        try
        {
            new MerchantOrderService(mapper).returnInspection(order.orderId, command);
            throw new AssertionError("partial serial device return must not enter inspection");
        }
        catch (ServiceException expected)
        {
            require(expected.getMessage().contains("已入库 1 / 共 2 台"),
                    "partial return must report the received and required counts");
        }
        require(mapper.insertedReturnInspection == null,
                "partial return must not persist a completed inspection record");
        require(mapper.updatedToStatus == null, "partial return must not complete the order");
    }

    /** 归还入库后不能跳过验机直接完成订单。 */
    @Test
    public void completeRequiresNormalReturnInspectionAfterInbound()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderRow order = new MerchantOrderRow();
        order.orderId = 9015L;
        order.orderNo = "ORD-9015";
        order.orderStatus = RentalCode.OrderStatus.RETURNING.getCode();
        mapper.detailRow = order;

        LoginUser loginUser = merchantLoginUser();
        loginUser.setMerchantAuthStatus(RentalCode.AuthenticationStatus.VERIFIED.getCode());
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);

        try
        {
            new MerchantOrderService(mapper).complete(order.orderId, null);
            throw new AssertionError("returning order must not bypass return inspection");
        }
        catch (ServiceException expected)
        {
            require("请先完成归还验机，验机正常后才能完成订单".equals(expected.getMessage()),
                    "completion must explain the required inspection step");
        }
        require(mapper.updatedToStatus == null, "blocked completion must not update order status");
    }

    /** 已有正常验机记录时允许归还中订单完成。 */
    @Test
    public void completeAllowsReturningOrderWithNormalInspection()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderRow order = new MerchantOrderRow();
        order.orderId = 9016L;
        order.orderNo = "ORD-9016";
        order.orderStatus = RentalCode.OrderStatus.RETURNING.getCode();
        mapper.detailRow = order;
        MerchantOrderReturnInspectionVO inspection = new MerchantOrderReturnInspectionVO();
        inspection.result = MerchantReturnInspectionResult.NORMAL.getCode();
        mapper.returnInspection = inspection;

        LoginUser loginUser = merchantLoginUser();
        loginUser.setMerchantAuthStatus(RentalCode.AuthenticationStatus.VERIFIED.getCode());
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);

        new MerchantOrderService(mapper).complete(order.orderId, null);

        require(RentalCode.OrderStatus.COMPLETED.getCode().equals(mapper.updatedToStatus),
                "normal inspection must allow completion");
    }

    /** 订单详情必须读取最近一次验机记录，并将状态编码转换成中文。 */
    @Test
    public void getReturnsLatestReturnInspectionWithChineseLabels()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderRow order = new MerchantOrderRow();
        order.orderId = 9014L;
        mapper.detailRow = order;
        MerchantOrderReturnInspectionVO inspection = new MerchantOrderReturnInspectionVO();
        inspection.result = 2;
        inspection.caseType = 3;
        mapper.returnInspection = inspection;

        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        MerchantOrderCreateCommand result = new MerchantOrderService(mapper).get(order.orderId);

        require(result.returnInspection == inspection,
                "order detail must expose the latest return inspection");
        require("转售后".equals(inspection.resultLabel),
                "inspection result must be displayed in Chinese");
        require("赔付".equals(inspection.caseTypeLabel),
                "inspection case type must be displayed in Chinese");
    }

    /** 订单详情必须返回套餐快照和弹窗所需的展示名称，不能只返回外键编号。 */
    @Test
    public void getBuildsOrderDetailDisplayFieldsAndPackageItems()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderRow order = new MerchantOrderRow();
        order.orderId = 9017L;
        order.orderNo = "ORD-9017";
        order.channelCode = "self";
        order.channelName = "自有渠道";
        order.warehouseName = "北京仓";
        order.returnWarehouseName = "杭州仓";
        order.productName = "Osmo Pocket 4";
        order.modelName = "Pocket 4";
        order.productCode = "POCKET4";
        order.packageName = "标准套餐";
        order.quantity = 1;
        order.packageDetailsJson = "{\"packageName\":\"标准套餐\","
                + "\"packageCode\":\"PKG-01\","
                + "\"items\":["
                + "{\"itemRole\":1,\"itemName\":\"Osmo Pocket 4\","
                + "\"itemCode\":\"POCKET4\",\"itemModelName\":\"Pocket 4\",\"quantity\":1},"
                + "{\"itemRole\":3,\"itemName\":\"三脚架\",\"quantity\":1}]}";
        mapper.detailRow = order;

        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        MerchantOrderDetailVO result = new MerchantOrderService(mapper).get(order.orderId);

        require("自有渠道".equals(result.channel), "detail must expose the channel name");
        require("北京仓".equals(result.warehouseName), "detail must expose the warehouse name");
        require(result.packageDetail != null && result.packageDetail.items.size() == 2,
                "detail must expose all package snapshot items");
        require("Osmo Pocket 4".equals(result.packageDetail.items.get(0).name),
                "main product name must come from the package snapshot");
        require("三脚架".equals(result.packageDetail.items.get(1).name),
                "package accessory name must come from the package snapshot");
    }

    /** 验证普通发货会在状态变更前消耗订单预占，避免已发货订单继续占用可用库存。 */
    @Test
    public void shipConsumesActiveReservationBeforeStatusTransition()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderRow order = new MerchantOrderRow();
        order.orderId = 9009L;
        order.orderNo = "ORD-9009";
        order.orderStatus = RentalCode.OrderStatus.PREPARED.getCode();
        order.warehouseId = 101L;
        mapper.detailRow = order;

        RecordingReservationService reservationService = new RecordingReservationService();
        LoginUser loginUser = merchantLoginUser();
        loginUser.setMerchantAuthStatus(RentalCode.AuthenticationStatus.VERIFIED.getCode());
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);

        MerchantOrderShipCommand command = new MerchantOrderShipCommand();
        command.carrier = "SF";
        command.trackingNo = "SF5156972752636";
        new MerchantOrderService(mapper, null, null, null, null, null, null, null, null,
                reservationService, null).ship(order.orderId, command);

        require(reservationService.consumedOrderId != null
                && reservationService.consumedOrderId.equals(order.orderId),
                "ship must consume the active reservation for the order");
        require(reservationService.selectedUnitIdsEmpty,
                "normal ship must consume the complete reservation without a scan selection");
        require(RentalCode.OrderStatus.SHIPPED.getCode().equals(mapper.updatedToStatus),
                "ship must move the prepared order to shipped after reservation consumption");
    }

    /** 物流编辑只更新物流记录，不能调用整单更新或重置订单状态。 */
    @Test
    public void updateLogisticsUpdatesExistingRecordWithoutChangingOrder()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderRow order = new MerchantOrderRow();
        order.orderId = 9012L;
        order.orderNo = "ORD-9012";
        order.orderStatus = RentalCode.OrderStatus.SHIPPING.getCode();
        order.warehouseId = 101L;
        mapper.detailRow = order;
        mapper.latestLogisticsId = 88L;

        LoginUser loginUser = merchantLoginUser();
        loginUser.setMerchantAuthStatus(RentalCode.AuthenticationStatus.VERIFIED.getCode());
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);

        MerchantOrderLogisticsUpdateCommand command = new MerchantOrderLogisticsUpdateCommand();
        command.orderId = order.orderId;
        command.direction = RentalCode.DeliveryDirection.SHIP.getCode();
        command.carrier = "SF";
        command.trackingNo = "SF5156972752636";

        new MerchantOrderService(mapper).updateLogistics(command);

        require(mapper.logisticsInfoUpdateCount == 1,
                "logistics edit must update the existing logistics record");
        require("SF".equals(mapper.logisticsCarrier)
                && "SF5156972752636".equals(mapper.logisticsTrackingNo),
                "logistics edit must pass the submitted carrier and tracking number");
        require(mapper.genericOrderUpdateCount == 0,
                "logistics edit must not update the order main table");
    }

    /** 验证历史漏写 source_order_id 时仍按联盟关系识别子订单，并同步原订单状态。 */
    @Test
    public void updateAllianceChildSynchronizesSourceOrderStatusFromRelation()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderRow order = new MerchantOrderRow();
        order.orderId = 9011L;
        order.orderNo = "ORD-9011";
        order.sourceOrderId = null;
        order.channelId = 2L;
        order.orderStatus = RentalCode.OrderStatus.PREPARED.getCode();
        order.packageId = 13L;
        order.productId = 19L;
        order.packageDetailsJson = "{}";
        order.customerName = "客户";
        order.customerPhone = "13000000000";
        order.recipientName = "收件人";
        order.recipientPhone = "13100000000";
        order.recipientAddress = "收货地址";
        order.leaseStartDate = parseDate("2026-09-11");
        order.leaseEndDate = parseDate("2026-09-16");
        mapper.detailRow = order;
        MerchantOrderAllianceLinkModel link = new MerchantOrderAllianceLinkModel();
        link.sourceMemberId = 1L;
        link.sourceOrderId = 9L;
        link.childOrderId = order.orderId;
        mapper.allianceLink = link;

        LoginUser loginUser = merchantLoginUser();
        loginUser.setMerchantAuthStatus(RentalCode.AuthenticationStatus.VERIFIED.getCode());
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);

        MerchantOrderCreateCommand command = new MerchantOrderCreateCommand();
        command.orderId = order.orderId;
        command.channelId = order.channelId;
        command.customerName = order.customerName;
        command.customerPhone = order.customerPhone;
        command.recipientName = order.recipientName;
        command.recipientPhone = order.recipientPhone;
        command.recipientAddress = order.recipientAddress;
        command.leaseStartDate = "2026-09-11";
        command.leaseEndDate = "2026-09-16";
        command.packageId = order.packageId;
        command.productId = order.productId;
        command.orderStatus = RentalCode.OrderStatus.SHIPPED.getCode();

        new MerchantOrderService(mapper).update(command);

        require(RentalCode.OrderStatus.PREPARED.getCode().equals(mapper.updatedFromStatus),
                "alliance child update must use the child current status");
        require(RentalCode.OrderStatus.SHIPPED.getCode().equals(mapper.updatedToStatus),
                "alliance child update must persist the requested status");
        require(Long.valueOf(1L).equals(mapper.sourceStatusMemberId)
                && Long.valueOf(9L).equals(mapper.sourceStatusOrderId),
                "source status sync must use the alliance relation rather than source_order_id");
        require(RentalCode.OrderStatus.SHIPPED.getCode().equals(mapper.sourceStatus),
                "source order status must follow the child order status");
    }

    /** 验证客户信息弹窗提交的七个订单快照字段都会传入更新 Mapper。 */
    @Test
    public void updateRecipientUpdatesCustomerAndRecipientSnapshotFields()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderRow order = new MerchantOrderRow();
        order.orderId = 9010L;
        order.orderNo = "ORD-9010";
        order.customerName = "旧客户";
        order.customerPhone = "13000000000";
        order.customerNickname = "旧昵称";
        order.recipientName = "旧收件人";
        order.recipientPhone = "13100000000";
        order.recipientAddress = "旧地址";
        order.remark = "旧备注";
        mapper.detailRow = order;

        LoginUser loginUser = merchantLoginUser();
        loginUser.setMerchantAuthStatus(RentalCode.AuthenticationStatus.VERIFIED.getCode());
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);

        MerchantOrderRecipientUpdateCommand command = new MerchantOrderRecipientUpdateCommand();
        command.orderId = order.orderId;
        command.customerName = " 新客户 ";
        command.customerPhone = " 13200000000 ";
        command.customerNickname = " 新昵称 ";
        command.recipientName = " 新收件人 ";
        command.recipientPhone = " 13300000000 ";
        command.recipientAddress = " 新地址 ";
        command.remark = " 新备注 ";

        new MerchantOrderService(mapper).updateRecipient(command);

        require(mapper.recipientCommand == command,
                "customer edit must use the submitted update command");
        require("新客户".equals(command.customerName), "customer name must be trimmed");
        require("13200000000".equals(command.customerPhone), "customer phone must be trimmed");
        require("新昵称".equals(command.customerNickname), "customer nickname must be trimmed");
        require("新收件人".equals(command.recipientName), "recipient name must be trimmed");
        require("13300000000".equals(command.recipientPhone), "recipient phone must be trimmed");
        require("新地址".equals(command.recipientAddress), "recipient address must be trimmed");
        require("新备注".equals(command.remark), "remark must be trimmed");
    }

    /** 使用项目统一时区解析测试日期，避免测试机默认时区影响日期差。 */
    private Date parseDate(String value)
    {
        String format = value.length() == 10 ? "yyyy-MM-dd" : "yyyy-MM-dd HH:mm:ss";
        return DateUtils.dateTime(format, value);
    }

    /** 验证订单编辑历史使用商户用户昵称展示操作人，且查询前校验订单归属。 */
    @Test
    public void listEditLogsResolvesOperatorNickname()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderRow order = new MerchantOrderRow();
        order.orderId = 9003L;
        mapper.detailRow = order;
        MerchantOrderEditLogVO editLog = new MerchantOrderEditLogVO();
        editLog.logId = 1L;
        editLog.operatorId = 3001L;
        editLog.operatorName = "旧名称";
        editLog.fieldName = "客户姓名";
        editLog.beforeValue = "甲";
        editLog.afterValue = "乙";
        mapper.editLogs = Collections.singletonList(editLog);

        MerchantUserSummary summary = new MerchantUserSummary();
        summary.setUserId(3001L);
        summary.setUsername("user-3001");
        summary.setNickname("豆豆");
        RemoteMerchantUserService remote = (RemoteMerchantUserService) Proxy.newProxyInstance(
                RemoteMerchantUserService.class.getClassLoader(),
                new Class<?>[] {RemoteMerchantUserService.class},
                (proxy, method, args) -> "getSummaries".equals(method.getName())
                        ? R.ok(Collections.singletonList(summary)) : null);

        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        MerchantOrderService service = new MerchantOrderService(mapper, null, null, null, null, null, remote);
        List<MerchantOrderEditLogVO> result = service.listEditLogs(9003L);

        require(result.size() == 1, "order edit history must be returned");
        require("豆豆".equals(result.get(0).operatorName),
                "order edit history must display the merchant user nickname");
    }

    /** 构造带有指定仓库权限的商户登录上下文。 */
    private LoginUser merchantLoginUser()
    {
        LoginUser loginUser = new LoginUser();
        loginUser.setClientType(Constants.CLIENT_TYPE_MERCHANT);
        loginUser.setTenantId(10001L);
        loginUser.setMemberId(27L);
        loginUser.setWarehouseScopeType(RentalCode.WarehouseScope.SPECIFIED.getCode());
        loginUser.setWarehouseIds(Collections.singleton(101L));
        return loginUser;
    }

    /** 执行无第三方断言库依赖的测试断言。 */
    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }

    /** 捕获普通发货对库存预占的调用，不访问真实库存数据库。 */
    private static class RecordingReservationService extends MerchantInventoryReservationService
    {
        private Long consumedOrderId;
        private boolean selectedUnitIdsEmpty;

        RecordingReservationService()
        {
            super(null, null, null);
        }

        @Override
        public void consumeActiveByOrder(Long orderId, Set<Long> selectedUnitIds)
        {
            consumedOrderId = orderId;
            selectedUnitIdsEmpty = selectedUnitIds != null && selectedUnitIds.isEmpty();
        }
    }

    /** 捕获订单查询参数的 Mapper 测试替身。 */
    private static class CapturingMapper implements MerchantOrderMapper
    {
        private Long tenantId;
        private Long merchantId;
        private MerchantOrderQuery query;
        private Long countTenantId;
        private Long countMerchantId;
        private MerchantOrderQuery countQuery;
        private long count;
        private MerchantOrderRow detailRow;
        private List<MerchantOrderRow> rows = Collections.emptyList();
        private List<MerchantOrderNoteVO> notes = Collections.emptyList();
        private List<MerchantOrderEditLogVO> editLogs = Collections.emptyList();
        private Long updatedOrderId;
        private Integer updatedFromStatus;
        private Integer updatedToStatus;
        private int genericOrderUpdateCount;
        private String packageDetailsJson;
        private MerchantOrderRecipientUpdateCommand recipientCommand;
        private MerchantOrderAllianceLinkModel allianceLink;
        private Long sourceStatusMemberId;
        private Long sourceStatusOrderId;
        private Integer sourceStatus;
        private Long latestLogisticsId;
        private int logisticsInfoUpdateCount;
        private String logisticsCarrier;
        private String logisticsTrackingNo;
        private MerchantOrderReturnInspectionVO returnInspection;
        private MerchantOrderReturnReadinessVO returnReadiness;
        private Long inspectionOrderId;
        private MerchantOrderReturnInspectionCommand insertedReturnInspection;

        /** 模拟订单支付目标查询，订单范围测试不访问真实数据库。 */
        @Override
        public Map<String, Object> selectMerchantOrderPaymentTarget(Long tenantId, Long merchantId, Long orderId)
        {
            return Collections.emptyMap();
        }

        /** 模拟订单支付状态更新，订单范围测试不访问真实数据库。 */
        @Override
        public int updateMerchantOrderPayStatus(Long tenantId, Long merchantId, Long orderId,
                Integer fromStatus, Integer toStatus, Integer cancelledStatus)
        {
            return 1;
        }

        /** 捕获订单详情查询参数，测试替身不访问真实数据库。 */
        @Override
        public MerchantOrderRow selectMerchantOrderById(Long tenantId, Long merchantId, Long orderId)
        {
            return detailRow != null && detailRow.orderId.equals(orderId) ? detailRow : null;
        }

        /** 模拟订单最近一次归还验机记录查询。 */
        @Override
        public MerchantOrderReturnInspectionVO selectLatestReturnInspection(Long tenantId, Long merchantId,
                Long orderId)
        {
            return returnInspection;
        }

        /** 模拟逐件设备归还完整性查询，测试替身不访问真实数据库。 */
        @Override
        public MerchantOrderReturnReadinessVO selectOrderReturnReadiness(Long tenantId, Long merchantId, Long orderId)
        {
            return returnReadiness;
        }

        /** 捕获归还验机记录写入，测试替身不访问真实数据库。 */
        @Override
        public int insertReturnInspection(Long tenantId, Long merchantId, Long orderId,
                MerchantOrderReturnInspectionCommand command, Long operatorId)
        {
            inspectionOrderId = orderId;
            insertedReturnInspection = command;
            return 1;
        }

        /** 模拟订单批注历史查询，测试范围类用例不访问真实数据库。 */
        @Override
        public List<MerchantOrderNoteVO> selectMerchantOrderNotes(Long tenantId, Long merchantId, Long orderId)
        {
            return notes;
        }

        /** 模拟订单批注写入，测试替身不访问真实数据库。 */
        @Override
        public int insertMerchantOrderNote(Long tenantId, Long merchantId, Long orderId, String noteContent,
                Integer noteType, Long operatorId)
        {
            return 1;
        }

        /** 模拟订单字段编辑历史查询，测试用户名称解析不访问真实数据库。 */
        @Override
        public List<MerchantOrderEditLogVO> selectMerchantOrderEditLogs(Long tenantId, Long merchantId, Long orderId)
        {
            return editLogs;
        }

        /** 模拟订单字段编辑历史写入，测试查询范围时不执行写入。 */
        @Override
        public int insertMerchantOrderEditLog(Long tenantId, Long merchantId, Long orderId, String fieldName,
                String beforeValue, String afterValue, String beforeDisplayValue, String afterDisplayValue,
                String changeReason, Long operatorId, String operatorName, String operatorIp)
        {
            return 1;
        }

        /** 模拟订单审计中的仓库名称解析，测试范围类用例不访问真实数据库。 */
        @Override
        public List<MerchantAuditReferenceModel> selectWarehouseNames(Long tenantId, Long merchantId,
                List<Long> referenceIds)
        {
            return Collections.emptyList();
        }

        /** 模拟订单审计中的渠道名称解析，测试范围类用例不访问真实数据库。 */
        @Override
        public List<MerchantAuditReferenceModel> selectOrderChannelNames(Long tenantId, Long merchantId,
                List<Long> referenceIds)
        {
            return Collections.emptyList();
        }

        /** 模拟当前商户可以使用指定订单渠道。 */
        @Override
        public int countUsableOrderChannel(Long tenantId, Long merchantId, Long channelId)
        {
            return 1;
        }

        /** 模拟订单主表新增，测试查询范围时不执行写入。 */
        @Override
        public int insertMerchantOrder(Long tenantId, Long merchantId, MerchantOrderCreateCommand command,
                Long operatorId)
        {
            return 1;
        }

        /** 模拟订单主表更新，测试查询范围时不执行写入。 */
        @Override
        public int updateMerchantOrder(Long tenantId, Long merchantId, MerchantOrderCreateCommand command,
                Long operatorId)
        {
            genericOrderUpdateCount++;
            return 1;
        }

        /** 捕获备货的窄状态更新参数，测试替身不访问真实数据库。 */
        @Override
        public int updateMerchantOrderStatus(Long tenantId, Long merchantId, Long orderId,
                Integer fromStatus, Integer toStatus, Long operatorId)
        {
            updatedOrderId = orderId;
            updatedFromStatus = fromStatus;
            updatedToStatus = toStatus;
            return 1;
        }

        /** 模拟联盟子订单关联查询，覆盖历史主表漏写 source_order_id 的情况。 */
        @Override
        public MerchantOrderAllianceLinkModel selectAllianceSourceOrderLink(Long tenantId, Long targetMemberId,
                Long childOrderId)
        {
            return allianceLink;
        }

        /** 捕获原订单状态同步参数，测试替身不访问真实数据库。 */
        @Override
        public int updateAllianceSourceOrderStatus(Long tenantId, Long sourceMemberId, Long sourceOrderId,
                Integer orderStatus, Long operatorId)
        {
            sourceStatusMemberId = sourceMemberId;
            sourceStatusOrderId = sourceOrderId;
            sourceStatus = orderStatus;
            return 1;
        }

        /** 模拟已接受联盟子订单查询，默认测试订单没有被联盟锁定。 */
        @Override
        public int countAcceptedAllianceChildren(Long tenantId, Long merchantId, Long orderId)
        {
            return 0;
        }

        /** 模拟订单套餐明细更新，测试替身不访问真实数据库。 */
        @Override
        public int updateMerchantOrderItemPackage(Long tenantId, Long merchantId,
                MerchantOrderCreateCommand command, Long operatorId)
        {
            return 1;
        }

        /** 模拟当前订单最新物流记录查询，测试替身不访问真实数据库。 */
        @Override
        public Long selectLatestMerchantOrderLogisticsId(Long tenantId, Long merchantId, Long orderId,
                Integer direction)
        {
            return latestLogisticsId;
        }

        /** 模拟订单物流更新，测试替身不访问真实数据库。 */
        @Override
        public int updateMerchantOrderLogistics(Long tenantId, Long merchantId, Long logisticsId,
                Long warehouseId, String carrier, String trackingNo, Long operatorId)
        {
            return 1;
        }

        /** 捕获仅更新快递信息的调用，确保不触发整单更新。 */
        @Override
        public int updateMerchantOrderLogisticsInfo(Long tenantId, Long merchantId, Long logisticsId,
                String carrier, String trackingNo, Long operatorId)
        {
            logisticsInfoUpdateCount++;
            logisticsCarrier = carrier;
            logisticsTrackingNo = trackingNo;
            return 1;
        }

        /** 模拟订单物流新增，测试替身不访问真实数据库。 */
        @Override
        public int insertMerchantOrderLogistics(Long tenantId, Long merchantId, Long orderId,
                Long warehouseId, Integer direction, String carrier, String trackingNo, Long operatorId)
        {
            return 1;
        }

        /** 模拟收件人信息更新，测试查询范围时不执行写入。 */
        @Override
        public int updateMerchantOrderRecipient(Long tenantId, Long merchantId,
                MerchantOrderRecipientUpdateCommand command, Long operatorId)
        {
            recipientCommand = command;
            return 1;
        }

        /** 模拟订单转寄快照写入，查询范围测试不访问真实数据库。 */
        @Override
        public int insertOrderForwardRecord(Long tenantId, Long merchantId, Long orderId,
                String oldRecipientName, String oldRecipientPhone, String oldRecipientAddress,
                String newRecipientName, String newRecipientPhone, String newRecipientAddress,
                String remark, Long operatorId)
        {
            return 1;
        }

        /** 模拟订单明细新增，测试查询范围时不执行写入。 */
        @Override
        public int insertMerchantOrderItem(Long tenantId, Long merchantId, MerchantOrderCreateCommand command,
                Long operatorId, Integer itemStatus)
        {
            return 1;
        }

        /** 模拟订单自身套餐详情快照更新，测试替身不访问真实数据库。 */
        @Override
        public int updateOrderPackageDetails(Long tenantId, Long merchantId, Long orderId,
                String packageDetailsJson, Long operatorId)
        {
            this.packageDetailsJson = packageDetailsJson;
            return 1;
        }

        /** 捕获应用服务传递给数据库层的可信范围和筛选条件。 */
        @Override
        public List<MerchantOrderRow> selectMerchantOrderList(Long tenantId, Long merchantId,
                MerchantOrderQuery query)
        {
            this.tenantId = tenantId;
            this.merchantId = merchantId;
            this.query = query;
            return rows;
        }

        /** 捕获独立订单总数查询参数，测试替身不访问真实数据库。 */
        @Override
        public long selectMerchantOrderCount(Long tenantId, Long merchantId, MerchantOrderQuery query)
        {
            this.countTenantId = tenantId;
            this.countMerchantId = merchantId;
            this.countQuery = query;
            return count;
        }

        /** 模拟天气风险批处理候选订单查询，订单应用服务测试不执行任务查询。 */
        @Override
        public List<com.ruoyi.rentalbusiness.domain.WeatherRiskOrderCandidate> selectWeatherRiskCandidates(
                java.sql.Date shipDate, List<Integer> orderStatuses)
        {
            return Collections.emptyList();
        }

        /** 模拟天气风险批处理更新，订单应用服务测试不执行任务写入。 */
        @Override
        public int updateWeatherRiskFlag(Long tenantId, Long memberId, Long orderId, java.sql.Date shipDate,
                List<Integer> orderStatuses, Integer weatherRiskFlag, Long operatorId)
        {
            return 0;
        }

        /** 模拟订单中心顶部概览查询，默认返回空统计。 */
        @Override
        public MerchantOrderSummaryVO selectMerchantOrderSummary(Long tenantId, Long merchantId,
                MerchantOrderQuery query)
        {
            return new MerchantOrderSummaryVO();
        }
    }
}
