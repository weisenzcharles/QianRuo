package com.ruoyi.rentalbusiness.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.enums.NumericCode;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.log.service.MerchantBusinessAuditRecorder;
import com.ruoyi.common.log.service.MerchantAuditDisplaySnapshot;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.merchant.api.RemoteMerchantUserService;
import com.ruoyi.merchant.api.model.MerchantUserSummaryQuery;
import com.ruoyi.merchant.api.model.MerchantUserSummary;
import com.ruoyi.system.api.RemoteAccessoryService;
import com.ruoyi.system.api.model.PlatformAccessoryOption;
import com.ruoyi.rentalbusiness.domain.MerchantOrderRow;
import com.ruoyi.rentalbusiness.domain.RegionWeatherRecord;
import com.ruoyi.rentalbusiness.integration.amap.AmapWeatherClient;
import com.ruoyi.rentalbusiness.integration.amap.AmapWeatherSnapshot;
import com.ruoyi.rentalbusiness.mapper.MerchantOrderMapper;
import com.ruoyi.rentalbusiness.mapper.MerchantAfterSalesMapper;
import com.ruoyi.rentalbusiness.mapper.MerchantPackageMapper;
import com.ruoyi.rentalbusiness.mapper.RegionWeatherMapper;
import com.ruoyi.rentalbusiness.model.RegionAddressModel;
import com.ruoyi.rentalbusiness.model.MerchantOrderListVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderDetailVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderQuery;
import com.ruoyi.rentalbusiness.model.MerchantOrderCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderNoteVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderNoteCreateCommand;
import com.ruoyi.rentalbusiness.enums.MerchantOrderDeliveryMethod;
import com.ruoyi.rentalbusiness.enums.MerchantOrderNoteType;
import com.ruoyi.rentalbusiness.model.MerchantOrderEditLogVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderSummaryVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderRecipientUpdateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderLogisticsUpdateCommand;
import com.ruoyi.rentalbusiness.model.MerchantAuditReferenceModel;
import com.ruoyi.rentalbusiness.model.MerchantOrderAllianceLinkModel;
import com.ruoyi.rentalbusiness.model.MerchantOrderActionCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderShipCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderReturnInspectionCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderReturnInspectionVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderReturnReadinessVO;
import com.ruoyi.rentalbusiness.model.MerchantAfterSalesCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantAfterSalesCaseVO;
import com.ruoyi.rentalbusiness.model.MerchantAfterSalesEvidenceCommand;
import com.ruoyi.rentalbusiness.model.MerchantAfterSalesEvidenceVO;
import com.ruoyi.rentalbusiness.model.MerchantAfterSalesEventCommand;
import com.ruoyi.rentalbusiness.model.MerchantAfterSalesEventVO;
import com.ruoyi.rentalbusiness.model.MerchantAfterSalesQuery;
import com.ruoyi.rentalbusiness.model.MerchantOrderForwardCommand;
import com.ruoyi.rentalbusiness.enums.MerchantAfterSalesCaseStatus;
import com.ruoyi.rentalbusiness.enums.MerchantAfterSalesCaseType;
import com.ruoyi.rentalbusiness.enums.MerchantAfterSalesEvidenceType;
import com.ruoyi.rentalbusiness.enums.MerchantAfterSalesEventType;
import com.ruoyi.rentalbusiness.enums.MerchantReturnInspectionResult;

/**
 * 商户订单列表应用服务。
 *
 * 订单属于 rental-business，服务只从可信商户登录上下文获取租户、商户和仓库
 * 范围，禁止使用请求参数扩大数据范围。订单列表是只读能力，未审核商户可以
 * 按默认只读菜单查看，但发货等后续状态操作必须另行校验认证状态和业务权限。
 */
@Service
public class MerchantOrderService
{
    private static final Logger log = LoggerFactory.getLogger(MerchantOrderService.class);

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final String OFFICIAL_ACCESSORY_TOKEN = "__OFFICIAL_ACCESSORY__:";

    private static final String OFFICIAL_ACCESSORY_LABEL = "官方配件";

    private static final String MERCHANT_ACCESSORY_LABEL = "自定义配件";

    private static final String NON_MAIN_PRODUCT_LABEL = "非主产品";

    private static final String PACKAGE_ITEM_LABEL = "套餐明细";

    private static final ZoneId BUSINESS_ZONE = DateUtils.DEFAULT_ZONE_ID;

    private final MerchantOrderMapper orderMapper;
    private final CodeGeneratorClient codeGeneratorClient;
    private final MerchantBusinessAuditRecorder auditRecorder;
    private final OrderRecipientHistoryService recipientHistoryService;
    private final RegionAddressParser regionAddressParser;
    private final AmapWeatherClient amapWeatherClient;
    private final RemoteMerchantUserService remoteMerchantUserService;
    private final RemoteAccessoryService remoteAccessoryService;
    private final ObjectMapper objectMapper;
    private final MerchantPackageMapper merchantPackageMapper;
    private final MerchantInventoryReservationService inventoryReservationService;
    private final RegionWeatherMapper regionWeatherMapper;
    private final LogisticsRouteSnapshotService logisticsRouteSnapshotService;
    private final MerchantAfterSalesMapper merchantAfterSalesMapper;
    private final MerchantExternalOrderCallbackService externalOrderCallbackService;

    /**
     * 注入商户订单查询数据访问对象。
     *
     * @param orderMapper 商户订单 Mapper
     * @param codeGeneratorClient 统一业务编码客户端
     * @param auditRecorder 商户业务审计记录器
     * @param recipientHistoryService 收件人历史快照服务
     * @param regionAddressParser 行政区划解析公共服务
     * @param amapWeatherClient 高德天气客户端
     * @param remoteMerchantUserService 商户用户内部服务，用于解析操作人昵称
     * @param remoteAccessoryService 平台官方配件内部服务，用于补齐历史订单快照中的配件名称
     * @param regionWeatherMapper 已落库的市级天气预报 Mapper，订单列表按日期批量读取
     * @param logisticsRouteSnapshotService 物流路线快照服务，快递信息变更时清理旧路线
     * @param merchantAfterSalesMapper 售后单数据访问对象
     */
    @Autowired
    public MerchantOrderService(MerchantOrderMapper orderMapper, CodeGeneratorClient codeGeneratorClient,
            MerchantBusinessAuditRecorder auditRecorder, OrderRecipientHistoryService recipientHistoryService,
            RegionAddressParser regionAddressParser, AmapWeatherClient amapWeatherClient,
            RemoteMerchantUserService remoteMerchantUserService, ObjectMapper objectMapper, MerchantPackageMapper merchantPackageMapper,
            MerchantInventoryReservationService inventoryReservationService, RemoteAccessoryService remoteAccessoryService,
            RegionWeatherMapper regionWeatherMapper, LogisticsRouteSnapshotService logisticsRouteSnapshotService,
            MerchantAfterSalesMapper merchantAfterSalesMapper,
            MerchantExternalOrderCallbackService externalOrderCallbackService)
    {
        this.orderMapper = orderMapper;
        this.codeGeneratorClient = codeGeneratorClient;
        this.auditRecorder = auditRecorder;
        this.recipientHistoryService = recipientHistoryService;
        this.regionAddressParser = regionAddressParser;
        this.amapWeatherClient = amapWeatherClient;
        this.remoteMerchantUserService = remoteMerchantUserService;
        this.remoteAccessoryService = remoteAccessoryService;
        this.objectMapper = objectMapper;
        this.merchantPackageMapper = merchantPackageMapper;
        this.inventoryReservationService = inventoryReservationService;
        this.regionWeatherMapper = regionWeatherMapper;
        this.logisticsRouteSnapshotService = logisticsRouteSnapshotService;
        this.merchantAfterSalesMapper = merchantAfterSalesMapper;
        this.externalOrderCallbackService = externalOrderCallbackService;
    }

    /** 保留天气改造前的完整测试构造器，未注入天气 Mapper 时继续使用天气缓存兜底。 */
    public MerchantOrderService(MerchantOrderMapper orderMapper, CodeGeneratorClient codeGeneratorClient,
            MerchantBusinessAuditRecorder auditRecorder, OrderRecipientHistoryService recipientHistoryService,
            RegionAddressParser regionAddressParser, AmapWeatherClient amapWeatherClient,
            RemoteMerchantUserService remoteMerchantUserService, ObjectMapper objectMapper, MerchantPackageMapper merchantPackageMapper,
            MerchantInventoryReservationService inventoryReservationService, RemoteAccessoryService remoteAccessoryService)
    {
        this(orderMapper, codeGeneratorClient, auditRecorder, recipientHistoryService, regionAddressParser,
                amapWeatherClient, remoteMerchantUserService, objectMapper, merchantPackageMapper,
                inventoryReservationService, remoteAccessoryService, null, null, null, null);
    }

    /** 保留历史构造方式，供仅需要订单读写能力的测试使用。 */
    public MerchantOrderService(MerchantOrderMapper orderMapper, CodeGeneratorClient codeGeneratorClient)
    {
        this(orderMapper, codeGeneratorClient, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    /** 保留快照改造前的完整测试构造器，旧测试不创建套餐订单时无需初始化快照依赖。 */
    public MerchantOrderService(MerchantOrderMapper orderMapper, CodeGeneratorClient codeGeneratorClient,
            MerchantBusinessAuditRecorder auditRecorder, OrderRecipientHistoryService recipientHistoryService,
            RegionAddressParser regionAddressParser, AmapWeatherClient amapWeatherClient,
            RemoteMerchantUserService remoteMerchantUserService)
    {
        this(orderMapper, codeGeneratorClient, auditRecorder, recipientHistoryService, regionAddressParser,
                amapWeatherClient, remoteMerchantUserService, null, null, null, null, null, null, null, null);
    }

    /** 供需要同时测试商户用户和平台配件名称解析的调用方使用。 */
    public MerchantOrderService(MerchantOrderMapper orderMapper, CodeGeneratorClient codeGeneratorClient,
            MerchantBusinessAuditRecorder auditRecorder, OrderRecipientHistoryService recipientHistoryService,
            RegionAddressParser regionAddressParser, AmapWeatherClient amapWeatherClient,
            RemoteMerchantUserService remoteMerchantUserService, RemoteAccessoryService remoteAccessoryService)
    {
        this(orderMapper, codeGeneratorClient, auditRecorder, recipientHistoryService, regionAddressParser,
                amapWeatherClient, remoteMerchantUserService, null, null, null, remoteAccessoryService, null, null, null, null);
    }

    /** 将当前登录商户套餐展示信息固化到订单主表，订单列表不再回查套餐目录。 */
    private void updatePackageDetails(MerchantOrderCreateCommand command)
    {
        updatePackageDetails(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                SecurityUtils.getUserId(), command);
    }

    /**
     * 按显式租户和商户生成套餐快照，供无登录态的受控外部订单入口复用。
     * 外部调用方只能提供套餐唯一编号，快照内容仍由商户套餐目录读取。
     */
    private void updatePackageDetails(Long tenantId, Long memberId, Long operatorId,
            MerchantOrderCreateCommand command)
    {
        String json = null;
        if (command.packageId != null)
        {
            if (merchantPackageMapper == null || objectMapper == null)
                throw new com.ruoyi.common.core.exception.ServiceException("套餐快照服务未初始化");
            Map<String, Object> packageView = merchantPackageMapper.selectById(tenantId, memberId, command.packageId);
            if (packageView == null)
                throw new com.ruoyi.common.core.exception.ServiceException("商户套餐不存在或已停用");
            Long packageModelId = numberValue(packageView, "modelId");
            if (packageModelId == null)
                throw new com.ruoyi.common.core.exception.ServiceException("商户套餐主商品型号不存在");
            if (command.modelId != null && !packageModelId.equals(command.modelId))
                throw new com.ruoyi.common.core.exception.ServiceException("主商品型号与套餐型号不一致");
            Map<String, Object> version = merchantPackageMapper.selectVersionById(tenantId, memberId, command.packageId);
            if (version == null)
                throw new com.ruoyi.common.core.exception.ServiceException("商户套餐没有生效版本");
            Long versionId = numberValue(version, "packageVersionId");
            List<Map<String, Object>> items = merchantPackageMapper.selectItems(versionId);
            if (items == null)
                items = Collections.emptyList();
            Set<Long> officialAccessoryIds = new LinkedHashSet<Long>();
            for (Map<String, Object> item : items)
            {
                Long itemRole = numberValue(item, "itemRole");
                if (itemRole != null && itemRole.intValue() == 2)
                {
                    Long accessoryId = numberValue(item, "accessoryId");
                    if (accessoryId != null)
                    {
                        officialAccessoryIds.add(accessoryId);
                    }
                }
            }
            Map<Long, PlatformAccessoryOption> officialAccessories = resolveOfficialAccessoryOptions(officialAccessoryIds);
            if (officialAccessories.size() != officialAccessoryIds.size())
            {
                throw new com.ruoyi.common.core.exception.ServiceException("官方配件详情读取失败，无法保存套餐订单");
            }
            List<Map<String, Object>> snapshotItems = new ArrayList<Map<String, Object>>();
            Map<String, Object> mainProduct = null;
            for (Map<String, Object> item : items)
            {
                Map<String, Object> snapshotItem = new LinkedHashMap<String, Object>(item);
                Long itemRoleValue = numberValue(item, "itemRole");
                Integer itemRole = itemRoleValue == null ? null : itemRoleValue.intValue();
                if (Integer.valueOf(2).equals(itemRole))
                {
                    PlatformAccessoryOption officialAccessory = officialAccessories.get(numberValue(item, "accessoryId"));
                    if (officialAccessory != null)
                    {
                        snapshotItem.put("itemName", officialAccessory.getAccessoryName());
                        snapshotItem.put("itemCode", officialAccessory.getAccessoryCode());
                        snapshotItem.put("itemSpecification", officialAccessory.getSpecification());
                        snapshotItem.put("itemUnit", officialAccessory.getUnit());
                    }
                }
                if (isBlank(stringValue(snapshotItem.get("itemName"))))
                {
                    snapshotItem.put("itemName", Integer.valueOf(2).equals(itemRole) ? "官方配件"
                            : Integer.valueOf(3).equals(itemRole) ? "商户配件"
                            : Integer.valueOf(4).equals(itemRole) ? NON_MAIN_PRODUCT_LABEL : PACKAGE_ITEM_LABEL);
                }
                snapshotItem.put("selectableDevice", Boolean.valueOf(isSelectableDevice(item, itemRole)));
                if (Integer.valueOf(1).equals(itemRole) && mainProduct == null)
                {
                    mainProduct = new LinkedHashMap<String, Object>();
                    mainProduct.put("productId", snapshotItem.get("productId"));
                    mainProduct.put("productCode", snapshotItem.get("itemCode"));
                    mainProduct.put("productName", snapshotItem.get("itemName"));
                    mainProduct.put("modelName", snapshotItem.get("itemModelName"));
                }
                snapshotItems.add(snapshotItem);
            }
            Map<String, Object> snapshot = new LinkedHashMap<String, Object>();
            snapshot.put("packageName", packageView.get("packageName"));
            snapshot.put("packageCode", packageView.get("packageCode"));
            snapshot.put("packageVersionId", versionId);
            snapshot.put("mainProduct", mainProduct);
            snapshot.put("items", snapshotItems);
            snapshot.put("extraAccessories", resolveExtraAccessories(command.accessoryItemIds,
                    tenantId, memberId, packageModelId));
            try
            {
                json = objectMapper.writeValueAsString(snapshot);
            }
            catch (JsonProcessingException exception)
            {
                throw new com.ruoyi.common.core.exception.ServiceException("订单套餐详情生成失败");
            }
        }
        int updated = orderMapper.updateOrderPackageDetails(tenantId, memberId, command.orderId, json, operatorId);
        if (updated == 0)
            throw new com.ruoyi.common.core.exception.ServiceException("订单套餐详情保存失败");
        command.packageDetailsJson = json;
    }

    private Long numberValue(Map<String, Object> value, String key)
    {
        Object item = value == null ? null : value.get(key);
        return item instanceof Number ? ((Number) item).longValue() : null;
    }

    private String stringValue(Object value)
    {
        return value == null ? null : String.valueOf(value);
    }

    /** 保留查询服务的轻量构造方式，供现有列表单元测试和只读调用使用。 */
    public MerchantOrderService(MerchantOrderMapper orderMapper)
    {
        this(orderMapper, null);
    }

    /** 校验商户录单并创建订单主表记录。 */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void add(MerchantOrderCreateCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("订单参数不能为空");
        }
        if (command.modelId == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("型号不能为空");
        }
        if (command.packageId == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("产品套餐不能为空");
        }
        if (command.channelId == null
                || command.leaseStartDate == null || command.leaseEndDate == null
                || command.leaseStartDate.compareTo(command.leaseEndDate) > 0)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("订单渠道和租赁日期范围不能为空或不正确");
        }
        normalizeCommandStatus(command);
        normalizeOrderDetails(command);
        validateExtraAccessories(command);
        validateChannel(command.channelId);
        if (command.warehouseId != null) MerchantContextUtils.checkWarehouseAccess(command.warehouseId);
        command.orderNo = codeGeneratorClient.next("order");
        orderMapper.insertMerchantOrder(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), command,
                com.ruoyi.common.security.utils.SecurityUtils.getUserId());
        if (command.productId != null || command.packageId != null || command.snId != null)
        {
            orderMapper.insertMerchantOrderItem(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), command,
                    com.ruoyi.common.security.utils.SecurityUtils.getUserId(),
                    RentalCode.OrderStatus.PENDING_SHIP.getCode());
            if (command.packageId != null) updatePackageDetails(command);
            if (command.packageId != null)
            {
                if (command.warehouseId == null)
                {
                    throw new com.ruoyi.common.core.exception.ServiceException("套餐订单必须指定发货仓库");
                }
                if (inventoryReservationService == null)
                {
                    throw new com.ruoyi.common.core.exception.ServiceException("库存预占服务未初始化");
                }
                com.ruoyi.rentalbusiness.model.InventoryReservationCommand reservation =
                        new com.ruoyi.rentalbusiness.model.InventoryReservationCommand();
                reservation.orderId = command.orderId;
                reservation.packageId = command.packageId;
                reservation.warehouseId = command.warehouseId;
                reservation.quantity = command.quantity == null ? 1 : command.quantity;
                reservation.idempotencyKey = "order:" + command.orderId + ":reserve";
                reservation.reason = "订单创建自动预占";
                inventoryReservationService.reserve(reservation);
            }
        }
        saveOrderLogistics(command, null);
        MerchantOrderCreateCommand created = get(command.orderId);
        recordRecipientSnapshot(created, com.ruoyi.common.security.utils.SecurityUtils.getUserId());
        audit("订单管理", "CREATE", "ORDER", command.orderId, created.orderNo, "创建订单", null, created);
    }

    /**
     * 按外部令牌已锁定的租户和商户创建正式订单。
     *
     * <p>该入口不读取商户登录上下文，也不创建第二张库存预占单；调用方必须先完成外部预占，
     * 随后由外层事务把预占绑定到本订单。</p>
     *
     * @param tenantId 令牌绑定的租户唯一编号
     * @param memberId 令牌绑定的商户唯一编号
     * @param command 已由外层服务补齐库存归属的订单命令
     * @return 已生成订单编码和唯一编号的命令
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public MerchantOrderCreateCommand addExternal(Long tenantId, Long memberId, MerchantOrderCreateCommand command)
    {
        if (tenantId == null || memberId == null || command == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("外部订单参数不完整");
        }
        if (command.productId == null && command.packageId == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("外部订单必须绑定商品或套餐");
        }
        if (command.channelId == null || command.leaseStartDate == null || command.leaseEndDate == null
                || command.leaseStartDate.compareTo(command.leaseEndDate) > 0)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("外部订单渠道和租赁日期范围不能为空或不正确");
        }
        command.orderStatus = null;
        command.payStatus = null;
        command.timeEffectStatus = null;
        command.weatherRiskFlag = null;
        command.priorityFlag = null;
        normalizeCommandStatus(command);
        normalizeOrderDetails(command);
        command.rentAmount = nonNegativeAmount(command.rentAmount, "租金不能小于零");
        command.depositAmount = nonNegativeAmount(command.depositAmount, "押金不能小于零");
        validateExtraAccessories(command);
        validateChannel(tenantId, memberId, command.channelId);
        command.orderNo = codeGeneratorClient.next("order");
        orderMapper.insertMerchantOrder(tenantId, memberId, command, null);
        orderMapper.insertMerchantOrderItem(tenantId, memberId, command, null,
                RentalCode.OrderStatus.PENDING_SHIP.getCode());
        if (command.packageId != null)
        {
            updatePackageDetails(tenantId, memberId, null, command);
        }
        saveOrderLogistics(command, null);
        recordRecipientSnapshot(tenantId, memberId, command, null);
        return command;
    }

    /** 校验并更新当前商户订单，避免跨商户修改订单数据。 */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void update(MerchantOrderCreateCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null || command.orderId == null || command.leaseStartDate == null
                || command.leaseEndDate == null || command.channelId == null
                || command.leaseStartDate.compareTo(command.leaseEndDate) > 0)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("订单编辑参数不完整或租赁日期范围不正确");
        }
        MerchantOrderCreateCommand before = get(command.orderId);
        if (command.orderStatus == null)
        {
            command.orderStatus = before.orderStatus;
        }
        MerchantOrderAllianceLinkModel allianceLink = findAllianceChildLink(command.orderId);
        boolean allianceChild = allianceLink != null || before.sourceOrderId != null;
        boolean allianceSourceLocked = !allianceChild
                && orderMapper.countAcceptedAllianceChildren(MerchantContextUtils.getTenantId(),
                        MerchantContextUtils.getMemberId(), command.orderId) > 0;
        if (allianceSourceLocked)
            throw new com.ruoyi.common.core.exception.ServiceException("原订单已有已接受的商户联盟订单，仅允许查看");
        if (allianceChild && !sameAllianceOrderFields(before, command))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("商户联盟子订单仅允许设置套餐");
        }
        if (allianceChild && command.packageId == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("商户联盟子订单必须设置套餐");
        }
        if (!allianceChild && command.modelId == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("型号不能为空");
        }
        // 编辑页面允许旧订单缺少新增字段，服务端统一补齐数据库非空标识，禁止 null 进入更新 SQL。
        normalizeCommandStatus(command);
        normalizeOrderDetails(command);
        validateExtraAccessories(command);
        validateStatusTransition(before.orderStatus, command.orderStatus);
        validateChannel(command.channelId);
        if (command.warehouseId != null) MerchantContextUtils.checkWarehouseAccess(command.warehouseId);
        int updated = 1;
        if (!allianceChild)
        {
            updated = orderMapper.updateMerchantOrder(MerchantContextUtils.getTenantId(),
                    MerchantContextUtils.getMemberId(), command, com.ruoyi.common.security.utils.SecurityUtils.getUserId());
        }
        else if (!Objects.equals(before.orderStatus, command.orderStatus))
        {
            updated = orderMapper.updateMerchantOrderStatus(MerchantContextUtils.getTenantId(),
                    MerchantContextUtils.getMemberId(), command.orderId, before.orderStatus,
                    command.orderStatus, com.ruoyi.common.security.utils.SecurityUtils.getUserId());
        }
        if (updated == 0) throw new com.ruoyi.common.core.exception.ServiceException("订单不存在或无权编辑");
        boolean packageChanged = !Objects.equals(before.packageId, command.packageId);
        boolean hasOrderItemData = packageChanged || command.productId != null || command.packageId != null
                || command.snId != null || command.accessoryItemIds != null
                && !command.accessoryItemIds.trim().isEmpty();
        if (hasOrderItemData)
        {
            int itemUpdated = orderMapper.updateMerchantOrderItemPackage(MerchantContextUtils.getTenantId(),
                    MerchantContextUtils.getMemberId(), command,
                    com.ruoyi.common.security.utils.SecurityUtils.getUserId());
            if (itemUpdated == 0)
            {
                // 历史订单可能只有订单主表没有明细，首次补选商品或套餐时补建主明细。
                command.orderItemId = null;
                int itemInserted = orderMapper.insertMerchantOrderItem(MerchantContextUtils.getTenantId(),
                        MerchantContextUtils.getMemberId(), command,
                        com.ruoyi.common.security.utils.SecurityUtils.getUserId(),
                        RentalCode.OrderStatus.PENDING_SHIP.getCode());
                if (itemInserted == 0)
                {
                    throw new com.ruoyi.common.core.exception.ServiceException("订单商品明细保存失败");
                }
            }
        }
        saveOrderLogistics(command, before);
        boolean extraAccessoriesChanged = !Objects.equals(before.accessoryItemIds, command.accessoryItemIds);
        if (packageChanged || extraAccessoriesChanged || (command.packageId != null && isBlank(before.packageDetailsJson)))
        {
            updatePackageDetails(command);
        }
        if (allianceChild && allianceLink != null)
        {
            synchronizeAllianceSourceOrderStatus(allianceLink, command.orderStatus);
        }
        MerchantOrderCreateCommand after = get(command.orderId);
        recordRecipientSnapshot(after, com.ruoyi.common.security.utils.SecurityUtils.getUserId());
        recordEditChanges(before, after, com.ruoyi.common.security.utils.SecurityUtils.getUserId());
        recordStatusChange(before, after, "编辑订单");
        audit("订单管理", "UPDATE", "ORDER", command.orderId, after.orderNo, "编辑订单", before, after);
    }

    /**
     * 将当前商户订单从待发货置为已备货。
     *
     * <p>备货是独立工作流动作，不能复用整单编辑命令，否则列表中的缺省展示值
     * 可能覆盖客户、仓库、租期、金额或套餐等订单字段。</p>
     *
     * @param orderId 订单唯一编号
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void prepare(Long orderId)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (orderId == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("订单唯一编号不能为空");
        }
        MerchantOrderCreateCommand before = get(orderId);
        Integer pendingShip = RentalCode.OrderStatus.PENDING_SHIP.getCode();
        Integer prepared = RentalCode.OrderStatus.PREPARED.getCode();
        MerchantOrderAllianceLinkModel allianceLink = findAllianceChildLink(orderId);
        boolean allianceChild = allianceLink != null || before.sourceOrderId != null;
        if (!allianceChild
                && orderMapper.countAcceptedAllianceChildren(MerchantContextUtils.getTenantId(),
                        MerchantContextUtils.getMemberId(), orderId) > 0)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("原订单已有已接受的商户联盟订单，仅允许查看");
        }
        if (prepared.equals(before.orderStatus))
        {
            return;
        }
        if (!pendingShip.equals(before.orderStatus))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("当前订单状态不允许备货");
        }
        int updated = orderMapper.updateMerchantOrderStatus(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), orderId, pendingShip, prepared, SecurityUtils.getUserId());
        if (updated == 0)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("订单状态已变化，请刷新后重试");
        }
        if (allianceChild && allianceLink != null)
        {
            synchronizeAllianceSourceOrderStatus(allianceLink, prepared);
        }
        MerchantOrderCreateCommand after = get(orderId);
        recordStatusChange(before, after, "订单备货");
        audit("订单管理", "UPDATE", "ORDER", orderId, after.orderNo, "订单备货", before, after);
    }

    /**
     * 极速核销台专用发货动作，只更新订单状态并同步联盟原订单，不覆盖订单其他字段。
     *
     * @param orderId 订单唯一编号
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void shipByVerification(Long orderId)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (orderId == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("订单唯一编号不能为空");
        }
        MerchantOrderCreateCommand before = get(orderId);
        Integer prepared = RentalCode.OrderStatus.PREPARED.getCode();
        Integer shipped = RentalCode.OrderStatus.SHIPPED.getCode();
        if (shipped.equals(before.orderStatus))
        {
            return;
        }
        if (!prepared.equals(before.orderStatus))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("当前订单状态不允许核销出库");
        }
        MerchantOrderAllianceLinkModel allianceLink = findAllianceChildLink(orderId);
        if (allianceLink == null && orderMapper.countAcceptedAllianceChildren(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), orderId) > 0)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("原订单已有已接受的商户联盟订单，仅允许查看");
        }
        if (orderMapper.updateMerchantOrderStatus(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), orderId, prepared, shipped, SecurityUtils.getUserId()) != 1)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("订单状态已变化，请刷新后重试");
        }
        if (allianceLink != null)
        {
            synchronizeAllianceSourceOrderStatus(allianceLink, shipped);
        }
        MerchantOrderCreateCommand after = get(orderId);
        recordStatusChange(before, after, "核销台发货");
        audit("订单管理", "UPDATE", "ORDER", orderId, after.orderNo, "核销台发货", before, after);
    }

    /**
     * 订单发货动作，同时保存发货快递信息并推进到已发货。
     *
     * <p>页面发货不能再调用整单编辑，避免列表投影中的默认值覆盖客户、租期或套餐。</p>
     *
     * @param orderId 订单唯一编号
     * @param command 发货快递信息
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void ship(Long orderId, MerchantOrderShipCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (orderId == null || command == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("发货参数不能为空");
        }
        MerchantOrderCreateCommand before = get(orderId);
        Integer shipped = RentalCode.OrderStatus.SHIPPED.getCode();
        if (shipped.equals(before.orderStatus))
        {
            return;
        }
        if (!RentalCode.OrderStatus.PREPARED.getCode().equals(before.orderStatus))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("当前订单状态不允许发货");
        }
        ensureOrderCanOperate(orderId);
        if (inventoryReservationService == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("库存预占服务未初始化");
        }
        // 普通发货没有扫码设备清单，整单消耗当前订单的有效预占，确保库存与履约状态同事务提交。
        inventoryReservationService.consumeActiveByOrder(orderId, Collections.emptySet());

        MerchantOrderLogisticsUpdateCommand logistics = new MerchantOrderLogisticsUpdateCommand();
        logistics.orderId = orderId;
        logistics.direction = RentalCode.DeliveryDirection.SHIP.getCode();
        logistics.carrier = command.carrier;
        logistics.trackingNo = command.trackingNo;
        updateLogistics(logistics);

        MerchantOrderAllianceLinkModel allianceLink = findAllianceChildLink(orderId);
        MerchantOrderCreateCommand after = transitionOrderStatus(before, shipped, "订单发货", allianceLink);
        audit("订单管理", "UPDATE", "ORDER", orderId, after.orderNo, "订单发货", before, after);
    }

    /**
     * 归还入库成功后推进订单到归还中，验机动作再决定完成或转售后。
     *
     * @param orderId 订单唯一编号
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void markReturning(Long orderId)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (orderId == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("订单唯一编号不能为空");
        }
        MerchantOrderCreateCommand before = get(orderId);
        Integer returning = RentalCode.OrderStatus.RETURNING.getCode();
        if (returning.equals(before.orderStatus)
                || RentalCode.OrderStatus.COMPLETED.getCode().equals(before.orderStatus)
                || RentalCode.OrderStatus.AFTER_SALES.getCode().equals(before.orderStatus))
        {
            return;
        }
        if (!isReturnedOrderStatus(before.orderStatus))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("当前订单状态不允许登记归还");
        }
        ensureOrderCanOperate(orderId);
        MerchantOrderAllianceLinkModel allianceLink = findAllianceChildLink(orderId);
        MerchantOrderCreateCommand after = transitionOrderStatus(before, returning, "订单归还入库", allianceLink);
        audit("订单管理", "UPDATE", "ORDER", orderId, after.orderNo, "订单归还入库", before, after);
    }

    /** 归还验机正常完成订单，异常则创建售后单并把订单置为售后中。 */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public MerchantAfterSalesCaseVO returnInspection(Long orderId, MerchantOrderReturnInspectionCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (orderId == null || command == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("归还验机参数不能为空");
        }
        MerchantReturnInspectionResult result = MerchantReturnInspectionResult.fromCode(command.result);
        if (result == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("请选择验机结果");
        }
        MerchantOrderCreateCommand before = get(orderId);
        if (!RentalCode.OrderStatus.RETURNING.getCode().equals(before.orderStatus)
                && !RentalCode.OrderStatus.OVERDUE_RETURN.getCode().equals(before.orderStatus))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("当前订单状态不允许归还验机");
        }
        ensureOrderCanOperate(orderId);
        ensureReturnDevicesReceived(before);
        recordReturnInspection(orderId, command);
        if (MerchantReturnInspectionResult.NORMAL.equals(result))
        {
            MerchantOrderAllianceLinkModel allianceLink = findAllianceChildLink(orderId);
            MerchantOrderCreateCommand after = transitionOrderStatus(before,
                    RentalCode.OrderStatus.COMPLETED.getCode(), "归还验机完成", allianceLink);
            audit("订单管理", "UPDATE", "ORDER", orderId, after.orderNo, "归还验机完成", before, after);
            return null;
        }

        MerchantAfterSalesCreateCommand afterSales = new MerchantAfterSalesCreateCommand();
        afterSales.caseType = command.caseType;
        afterSales.problemDesc = command.problemDesc;
        afterSales.lostItemsJson = command.lostItemsJson;
        afterSales.compensationAmount = command.compensationAmount;
        afterSales.customerContacted = command.customerContacted;
        afterSales.remark = command.remark;
        return createAfterSalesCase(before, afterSales, "归还验机转售后");
    }

    /** 完成订单；售后订单必须先完成所有处理中售后单。 */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void complete(Long orderId, MerchantOrderActionCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        MerchantOrderCreateCommand before = requireOrderActionOrder(orderId, "完成");
        if (RentalCode.OrderStatus.COMPLETED.getCode().equals(before.orderStatus))
        {
            return;
        }
        if (RentalCode.OrderStatus.AFTER_SALES.getCode().equals(before.orderStatus)
                && merchantAfterSalesMapper != null
                && merchantAfterSalesMapper.countProcessingByOrder(MerchantContextUtils.getTenantId(),
                        MerchantContextUtils.getMemberId(), orderId) > 0)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("请先完成处理中售后单");
        }
        boolean returning = RentalCode.OrderStatus.RETURNING.getCode().equals(before.orderStatus)
                || RentalCode.OrderStatus.OVERDUE_RETURN.getCode().equals(before.orderStatus);
        if (!returning && !RentalCode.OrderStatus.AFTER_SALES.getCode().equals(before.orderStatus))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("当前订单状态不允许完成");
        }
        if (returning && (before.returnInspection == null
                || !MerchantReturnInspectionResult.NORMAL.getCode().equals(before.returnInspection.result)))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("请先完成归还验机，验机正常后才能完成订单");
        }
        if (returning)
        {
            ensureReturnDevicesReceived(before);
        }
        ensureOrderCanOperate(orderId);
        MerchantOrderCreateCommand after = transitionOrderStatus(before,
                RentalCode.OrderStatus.COMPLETED.getCode(), "订单完成", findAllianceChildLink(orderId));
        audit("订单管理", "UPDATE", "ORDER", orderId, after.orderNo, "订单完成", before, after);
    }

    /** 取消未发货订单并释放仍有效的库存预占。 */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void cancel(Long orderId, MerchantOrderActionCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        MerchantOrderCreateCommand before = requireOrderActionOrder(orderId, "取消");
        if (RentalCode.OrderStatus.CANCELLED.getCode().equals(before.orderStatus))
        {
            return;
        }
        if (!RentalCode.OrderStatus.PENDING_SHIP.getCode().equals(before.orderStatus)
                && !RentalCode.OrderStatus.PREPARED.getCode().equals(before.orderStatus)
                && !RentalCode.OrderStatus.OVERDUE.getCode().equals(before.orderStatus))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("已发货订单不能取消");
        }
        ensureOrderCanOperate(orderId);
        if (inventoryReservationService == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("库存预占服务未初始化");
        }
        inventoryReservationService.releaseActiveByOrder(orderId, "订单取消释放预占");
        MerchantOrderCreateCommand after = transitionOrderStatus(before,
                RentalCode.OrderStatus.CANCELLED.getCode(), "订单取消", findAllianceChildLink(orderId));
        audit("订单管理", "UPDATE", "ORDER", orderId, after.orderNo, "订单取消", before, after);
    }

    /** 在租赁中或归还阶段创建售后单，并将订单推进到售后中。 */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public MerchantAfterSalesCaseVO createAfterSales(Long orderId, MerchantAfterSalesCreateCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        MerchantOrderCreateCommand before = requireOrderActionOrder(orderId, "创建售后单");
        if (RentalCode.OrderStatus.AFTER_SALES.getCode().equals(before.orderStatus))
        {
            return createAfterSalesCase(before, command, "创建售后单");
        }
        if (!RentalCode.OrderStatus.RENTING.getCode().equals(before.orderStatus)
                && !RentalCode.OrderStatus.RETURNING.getCode().equals(before.orderStatus)
                && !RentalCode.OrderStatus.OVERDUE_RETURN.getCode().equals(before.orderStatus))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("当前订单阶段不允许创建售后单");
        }
        ensureOrderCanOperate(orderId);
        return createAfterSalesCase(before, command, "创建售后单");
    }

    /** 查询当前商户售后单列表。 */
    public List<MerchantAfterSalesCaseVO> listAfterSales(MerchantAfterSalesQuery query)
    {
        if (merchantAfterSalesMapper == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("售后单服务未初始化");
        }
        List<MerchantAfterSalesCaseVO> rows = merchantAfterSalesMapper.selectList(
                MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), query);
        if (rows != null)
        {
            for (MerchantAfterSalesCaseVO row : rows)
            {
                decorateAfterSalesCase(row);
            }
        }
        return rows;
    }

    /** 查询当前商户售后单详情。 */
    public MerchantAfterSalesCaseVO getAfterSales(String caseNo)
    {
        MerchantAfterSalesCaseVO row = findAfterSalesCase(caseNo);
        decorateAfterSalesCase(row);
        return row;
    }

    /** 完成售后单；订单没有其他处理中售后单时同步完成原订单。 */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void completeAfterSales(String caseNo, MerchantOrderActionCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        MerchantAfterSalesCaseVO beforeCase = findAfterSalesCase(caseNo);
        Integer processing = MerchantAfterSalesCaseStatus.PROCESSING.getCode();
        if (MerchantAfterSalesCaseStatus.COMPLETED.getCode().equals(beforeCase.caseStatus))
        {
            return;
        }
        if (!processing.equals(beforeCase.caseStatus))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("当前售后单状态不允许完成");
        }
        String normalizedCaseNo = caseNo.trim();
        int updated = merchantAfterSalesMapper.updateStatusByCaseNo(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), normalizedCaseNo, processing,
                MerchantAfterSalesCaseStatus.COMPLETED.getCode(), SecurityUtils.getUserId(),
                command == null ? null : command.remark);
        if (updated != 1)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("售后单状态已变化，请刷新后重试");
        }
        appendAfterSalesEvent(beforeCase.caseId, MerchantAfterSalesEventType.COMPLETED,
                processing, MerchantAfterSalesCaseStatus.COMPLETED.getCode(),
                command == null ? "售后处理完成" : defaultText(command.remark, "售后处理完成"));
        MerchantOrderCreateCommand order = get(beforeCase.orderId);
        if (RentalCode.OrderStatus.AFTER_SALES.getCode().equals(order.orderStatus)
                && merchantAfterSalesMapper.countProcessingByOrder(MerchantContextUtils.getTenantId(),
                        MerchantContextUtils.getMemberId(), order.orderId) == 0)
        {
            MerchantOrderCreateCommand after = transitionOrderStatus(order,
                    RentalCode.OrderStatus.COMPLETED.getCode(), "售后处理完成", findAllianceChildLink(order.orderId));
            audit("订单管理", "UPDATE", "ORDER", order.orderId, after.orderNo, "售后处理完成", order, after);
        }
    }

    /** 订单状态动作只允许操作当前商户可见订单。 */
    private MerchantOrderCreateCommand requireOrderActionOrder(Long orderId, String action)
    {
        if (orderId == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("订单唯一编号不能为空");
        }
        MerchantOrderCreateCommand order = get(orderId);
        if (order == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("订单不存在或无权操作");
        }
        return order;
    }

    /** 原订单有已接受子订单时锁定所有会改变履约结果的动作。 */
    private void ensureOrderCanOperate(Long orderId)
    {
        if (findAllianceChildLink(orderId) == null
                && orderMapper.countAcceptedAllianceChildren(MerchantContextUtils.getTenantId(),
                        MerchantContextUtils.getMemberId(), orderId) > 0)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("原订单已有已接受的商户联盟订单，仅允许查看");
        }
    }

    private boolean isReturnedOrderStatus(Integer status)
    {
        return RentalCode.OrderStatus.SHIPPING.getCode().equals(status)
                || RentalCode.OrderStatus.SHIP_IN_TRANSIT.getCode().equals(status)
                || RentalCode.OrderStatus.SHIPPED.getCode().equals(status)
                || RentalCode.OrderStatus.RENTING.getCode().equals(status)
                || RentalCode.OrderStatus.OVERDUE_RETURN.getCode().equals(status)
                || RentalCode.OrderStatus.OVERDUE.getCode().equals(status);
    }

    private MerchantOrderCreateCommand transitionOrderStatus(MerchantOrderCreateCommand before, Integer targetStatus,
            String description, MerchantOrderAllianceLinkModel allianceLink)
    {
        if (Objects.equals(before.orderStatus, targetStatus))
        {
            return before;
        }
        if (orderMapper.updateMerchantOrderStatus(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                before.orderId, before.orderStatus, targetStatus, SecurityUtils.getUserId()) != 1)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("订单状态已变化，请刷新后重试");
        }
        if (allianceLink != null)
        {
            synchronizeAllianceSourceOrderStatus(allianceLink, targetStatus);
        }
        MerchantOrderCreateCommand after = get(before.orderId);
        recordStatusChange(before, after, description);
        return after;
    }

    /** 保存归还验机事实，避免订单状态推进后丢失验机结果和赔付依据。 */
    private void recordReturnInspection(Long orderId, MerchantOrderReturnInspectionCommand command)
    {
        int inserted = orderMapper.insertReturnInspection(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), orderId, command, SecurityUtils.getUserId());
        if (inserted != 1)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("归还验机记录保存失败");
        }
    }

    /** 逐件订单必须全部回库后才能进入归还验机和完成阶段；非逐件订单不启用此校验。 */
    private void ensureReturnDevicesReceived(MerchantOrderCreateCommand order)
    {
        MerchantOrderReturnReadinessVO readiness = orderMapper.selectOrderReturnReadiness(
                MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), order.orderId);
        if (readiness == null || readiness.boundUnitCount == null || readiness.boundUnitCount <= 0)
        {
            return;
        }
        int returned = readiness.returnedUnitCount == null ? 0 : readiness.returnedUnitCount;
        if (returned < readiness.boundUnitCount)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("逐件设备尚未全部归还，已入库 "
                    + returned + " / 共 " + readiness.boundUnitCount + " 台，不能继续验机");
        }
    }

    /** 将订单详情中的验机编码转换成稳定的中文业务文案。 */
    private void decorateReturnInspection(MerchantOrderReturnInspectionVO inspection)
    {
        if (inspection == null)
        {
            return;
        }
        MerchantReturnInspectionResult result = MerchantReturnInspectionResult.fromCode(inspection.result);
        MerchantAfterSalesCaseType caseType = MerchantAfterSalesCaseType.fromCode(inspection.caseType);
        inspection.resultLabel = result == null ? "未知" : result.getLabel();
        inspection.caseTypeLabel = caseType == null ? null : caseType.getLabel();
    }

    private MerchantAfterSalesCaseVO createAfterSalesCase(MerchantOrderCreateCommand before,
            MerchantAfterSalesCreateCommand command, String description)
    {
        if (merchantAfterSalesMapper == null || codeGeneratorClient == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("售后单服务未初始化");
        }
        validateAfterSalesCommand(command);
        MerchantAfterSalesCaseVO existing = merchantAfterSalesMapper.selectProcessingByOrderForUpdate(
                MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), before.orderId);
        if (existing != null)
        {
            if (!RentalCode.OrderStatus.AFTER_SALES.getCode().equals(before.orderStatus))
            {
                MerchantOrderCreateCommand after = transitionOrderStatus(before,
                        RentalCode.OrderStatus.AFTER_SALES.getCode(), description,
                        findAllianceChildLink(before.orderId));
                audit("订单管理", "UPDATE", "ORDER", before.orderId, after.orderNo, description, before, after);
            }
            decorateAfterSalesCase(existing);
            return existing;
        }
        String caseNo = codeGeneratorClient.next("after_sales_case");
        int inserted = merchantAfterSalesMapper.insertCase(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), before.orderId, caseNo, command, SecurityUtils.getUserId());
        if (inserted != 1)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("售后单创建失败");
        }
        MerchantOrderCreateCommand after = before;
        if (!RentalCode.OrderStatus.AFTER_SALES.getCode().equals(before.orderStatus))
        {
            after = transitionOrderStatus(before, RentalCode.OrderStatus.AFTER_SALES.getCode(), description,
                    findAllianceChildLink(before.orderId));
        }
        MerchantAfterSalesCaseVO result = merchantAfterSalesMapper.selectByCaseNoForUpdate(
                MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), caseNo);
        appendAfterSalesEvent(result.caseId, MerchantAfterSalesEventType.CREATED, null,
                MerchantAfterSalesCaseStatus.PROCESSING.getCode(), description);
        decorateAfterSalesCase(result);
        audit("订单管理", "UPDATE", "ORDER", before.orderId, after.orderNo, description, before, after);
        return result;
    }

    /** 查询当前商户售后单处理时间线。 */
    public List<MerchantAfterSalesEventVO> listAfterSalesEvents(String caseNo)
    {
        MerchantAfterSalesCaseVO caseRow = findAfterSalesCase(caseNo);
        List<MerchantAfterSalesEventVO> rows = merchantAfterSalesMapper.selectEvents(
                MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), caseRow.caseId);
        if (rows != null)
        {
            for (MerchantAfterSalesEventVO row : rows)
            {
                decorateAfterSalesEvent(row);
            }
        }
        return rows;
    }

    /** 查询当前商户售后单证据记录。 */
    public List<MerchantAfterSalesEvidenceVO> listAfterSalesEvidence(String caseNo)
    {
        MerchantAfterSalesCaseVO caseRow = findAfterSalesCase(caseNo);
        List<MerchantAfterSalesEvidenceVO> rows = merchantAfterSalesMapper.selectEvidence(
                MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), caseRow.caseId);
        if (rows != null)
        {
            for (MerchantAfterSalesEvidenceVO row : rows)
            {
                decorateAfterSalesEvidence(row);
            }
        }
        return rows;
    }

    /** 追加当前商户售后处理记录。 */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void addAfterSalesEvent(String caseNo, MerchantAfterSalesEventCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        MerchantAfterSalesCaseVO caseRow = findAfterSalesCase(caseNo);
        requireProcessingAfterSales(caseRow);
        if (command == null || isBlank(command.content))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("请填写处理记录");
        }
        String content = command.content.trim();
        if (content.length() > 2000)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("处理记录不能超过2000个字符");
        }
        appendAfterSalesEvent(caseRow.caseId, MerchantAfterSalesEventType.NOTE, null, null, content);
    }

    /** 追加当前商户售后证据，并同步写入时间线事件。 */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void addAfterSalesEvidence(String caseNo, MerchantAfterSalesEvidenceCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        MerchantAfterSalesCaseVO caseRow = findAfterSalesCase(caseNo);
        requireProcessingAfterSales(caseRow);
        if (command == null || MerchantAfterSalesEvidenceType.fromCode(command.evidenceType) == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("请选择证据类型");
        }
        if (isBlank(command.fileUrl))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("请上传证据文件");
        }
        String fileUrl = command.fileUrl.trim();
        String fileName = trimToLength(command.fileName, 255);
        String fileMime = trimToLength(command.fileMime, 128);
        String description = trimToLength(command.description, 1000);
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = defaultText(SecurityUtils.getUsername(), "系统");
        int inserted = merchantAfterSalesMapper.insertEvidence(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), caseRow.caseId, command.evidenceType, fileUrl, fileName,
                fileMime, description, operatorId, operatorName);
        if (inserted != 1)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("售后证据保存失败");
        }
        appendAfterSalesEvent(caseRow.caseId, MerchantAfterSalesEventType.EVIDENCE_ADDED, null, null,
                defaultText(fileName, "售后证据"));
    }

    private void requireProcessingAfterSales(MerchantAfterSalesCaseVO caseRow)
    {
        if (caseRow == null || !MerchantAfterSalesCaseStatus.PROCESSING.getCode().equals(caseRow.caseStatus))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("当前售后单已结束，不能继续修改");
        }
    }

    private void appendAfterSalesEvent(Long caseId, MerchantAfterSalesEventType eventType,
            Integer fromStatus, Integer toStatus, String content)
    {
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = defaultText(SecurityUtils.getUsername(), "系统");
        int inserted = merchantAfterSalesMapper.insertEvent(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), caseId, eventType.getCode(), fromStatus, toStatus,
                trimToLength(content, 2000), operatorId, operatorName);
        if (inserted != 1)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("售后处理记录保存失败");
        }
    }

    private void decorateAfterSalesEvent(MerchantAfterSalesEventVO row)
    {
        if (row == null)
        {
            return;
        }
        MerchantAfterSalesEventType type = MerchantAfterSalesEventType.fromCode(row.eventType);
        MerchantAfterSalesCaseStatus from = MerchantAfterSalesCaseStatus.fromCode(row.fromStatus);
        MerchantAfterSalesCaseStatus to = MerchantAfterSalesCaseStatus.fromCode(row.toStatus);
        row.eventTypeLabel = type == null ? "未知" : type.getLabel();
        row.fromStatusLabel = from == null ? null : from.getLabel();
        row.toStatusLabel = to == null ? null : to.getLabel();
    }

    private void decorateAfterSalesEvidence(MerchantAfterSalesEvidenceVO row)
    {
        if (row == null)
        {
            return;
        }
        MerchantAfterSalesEvidenceType type = MerchantAfterSalesEvidenceType.fromCode(row.evidenceType);
        row.evidenceTypeLabel = type == null ? "未知" : type.getLabel();
    }

    private void validateAfterSalesCommand(MerchantAfterSalesCreateCommand command)
    {
        if (command == null || MerchantAfterSalesCaseType.fromCode(command.caseType) == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("请选择售后类型");
        }
        if (isBlank(command.problemDesc))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("请填写问题描述");
        }
        if (command.compensationAmount != null && command.compensationAmount.signum() < 0)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("赔付金额不能小于零");
        }
    }

    private MerchantAfterSalesCaseVO findAfterSalesCase(String caseNo)
    {
        if (merchantAfterSalesMapper == null || isBlank(caseNo))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("售后单号不能为空");
        }
        MerchantAfterSalesCaseVO row = merchantAfterSalesMapper.selectByCaseNoForUpdate(
                MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), caseNo.trim());
        if (row == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("售后单不存在或已删除");
        }
        return row;
    }

    private void decorateAfterSalesCase(MerchantAfterSalesCaseVO row)
    {
        if (row == null)
        {
            return;
        }
        MerchantAfterSalesCaseType type = MerchantAfterSalesCaseType.fromCode(row.caseType);
        MerchantAfterSalesCaseStatus status = MerchantAfterSalesCaseStatus.fromCode(row.caseStatus);
        row.caseTypeLabel = type == null ? "未知" : type.getLabel();
        row.caseStatusLabel = status == null ? "未知" : status.getLabel();
    }

    /** 仅允许专用工作流将订单推进到发货或完结状态，普通编辑不能任意跳转状态。 */
    private void validateStatusTransition(Integer beforeStatus, Integer requestedStatus)
    {
        if (Objects.equals(beforeStatus, requestedStatus))
        {
            return;
        }
        boolean shipping = RentalCode.OrderStatus.PREPARED.getCode().equals(beforeStatus)
                && RentalCode.OrderStatus.SHIPPED.getCode().equals(requestedStatus);
        boolean completed = (RentalCode.OrderStatus.RETURNING.getCode().equals(beforeStatus)
                || RentalCode.OrderStatus.OVERDUE_RETURN.getCode().equals(beforeStatus)
                || RentalCode.OrderStatus.AFTER_SALES.getCode().equals(beforeStatus))
                && RentalCode.OrderStatus.COMPLETED.getCode().equals(requestedStatus);
        if (!shipping && !completed)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("订单状态不允许通过当前操作变更");
        }
    }

    /**
     * 保存订单发货和归还方向的快递信息；同一订单同一方向只维护最新一条记录。
     *
     * <p>整单编辑会回填已有快递字段。只有快递公司或单号真的变化时才清理路线快照，
     * 避免普通编辑动作导致下一次查看轨迹重复调用供应商。</p>
     */
    private void saveOrderLogistics(MerchantOrderCreateCommand command, MerchantOrderCreateCommand before)
    {
        if (command == null)
            return;
        saveLogistics(command, RentalCode.DeliveryDirection.SHIP.getCode(), command.warehouseId,
                command.expressCompany, command.expressTrackingNo,
                logisticsChanged(before == null ? null : before.expressCompany,
                        before == null ? null : before.expressTrackingNo,
                        command.expressCompany, command.expressTrackingNo));
        saveLogistics(command, RentalCode.DeliveryDirection.RETURN.getCode(), command.returnWarehouseId,
                command.returnExpressCompany, command.returnExpressTrackingNo,
                logisticsChanged(before == null ? null : before.returnExpressCompany,
                        before == null ? null : before.returnExpressTrackingNo,
                        command.returnExpressCompany, command.returnExpressTrackingNo));
    }

    /**
     * 更新当前商户订单指定方向的快递信息。
     *
     * <p>该入口与整单编辑、发货工作流分离，只更新物流表中的快递公司和单号，既不改变订单状态，
     * 也不在当前数据库事务内执行外部查询；物流信息变更后会清理旧路线快照，控制器在事务提交后
     * 初始化新路线。</p>
     *
     * @param command 物流更新命令
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void updateLogistics(MerchantOrderLogisticsUpdateCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null || command.orderId == null || command.direction == null
                || !isEditableLogisticsDirection(command.direction))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("物流方向不合法");
        }
        String carrier = command.carrier == null ? "" : command.carrier.trim();
        String trackingNo = command.trackingNo == null ? "" : command.trackingNo.trim();
        if (carrier.isEmpty() || trackingNo.isEmpty())
        {
            throw new com.ruoyi.common.core.exception.ServiceException(
                    RentalCode.DeliveryDirection.RETURN.getCode().equals(command.direction)
                            ? "归还快递公司和快递单号必须同时填写" : "快递公司和快递单号必须同时填写");
        }

        MerchantOrderCreateCommand order = get(command.orderId);
        if (orderMapper.countAcceptedAllianceChildren(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), command.orderId) > 0)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("原订单已有已接受的商户联盟订单，仅允许查看");
        }
        Long warehouseId = RentalCode.DeliveryDirection.SHIP.getCode().equals(command.direction)
                ? order.warehouseId : order.returnWarehouseId;
        if (warehouseId == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException(
                    RentalCode.DeliveryDirection.RETURN.getCode().equals(command.direction)
                            ? "订单未配置归还仓库" : "订单未配置发货仓库");
        }
        MerchantContextUtils.checkWarehouseAccess(warehouseId);

        Long tenantId = MerchantContextUtils.getTenantId();
        Long merchantId = MerchantContextUtils.getMemberId();
        Long operatorId = SecurityUtils.getUserId();
        String currentCarrier = command.direction.equals(RentalCode.DeliveryDirection.RETURN.getCode())
                ? order.returnExpressCompany : order.expressCompany;
        String currentTrackingNo = command.direction.equals(RentalCode.DeliveryDirection.RETURN.getCode())
                ? order.returnExpressTrackingNo : order.expressTrackingNo;
        boolean logisticsChanged = logisticsChanged(currentCarrier, currentTrackingNo, carrier, trackingNo);
        try
        {
            Long logisticsId = orderMapper.selectLatestMerchantOrderLogisticsId(tenantId, merchantId,
                    command.orderId, command.direction);
            int affected = logisticsId == null ? 0 : orderMapper.updateMerchantOrderLogisticsInfo(tenantId,
                    merchantId, logisticsId, carrier, trackingNo, operatorId);
            if (affected == 0)
            {
                orderMapper.insertMerchantOrderLogistics(tenantId, merchantId, command.orderId, warehouseId,
                        command.direction, carrier, trackingNo, operatorId);
                logisticsId = orderMapper.selectLatestMerchantOrderLogisticsId(tenantId, merchantId,
                        command.orderId, command.direction);
            }
            if (logisticsChanged)
            {
                resetLogisticsRouteSnapshot(tenantId, merchantId, logisticsId, operatorId);
            }
        }
        catch (org.springframework.dao.DuplicateKeyException exception)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("该快递单号已被其他订单使用，请更换后重试");
        }
    }

    /** 物流编辑只允许维护发货和归还两个方向，调货物流由对应工作流处理。 */
    private boolean isEditableLogisticsDirection(Integer direction)
    {
        return RentalCode.DeliveryDirection.SHIP.getCode().equals(direction)
                || RentalCode.DeliveryDirection.RETURN.getCode().equals(direction);
    }

    /** 查询当前订单是否为联盟子订单；即使历史订单主表漏写 source_order_id，也以联盟关系表为准。 */
    private MerchantOrderAllianceLinkModel findAllianceChildLink(Long orderId)
    {
        return orderMapper.selectAllianceSourceOrderLink(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), orderId);
    }

    /** 将子订单状态写回原订单；物流仍使用子订单唯一记录，原订单查询通过联盟关系读取该记录。 */
    private void synchronizeAllianceSourceOrderStatus(MerchantOrderAllianceLinkModel allianceLink,
            Integer childOrderStatus)
    {
        if (allianceLink == null || childOrderStatus == null || allianceLink.sourceMemberId == null
                || allianceLink.sourceOrderId == null)
        {
            return;
        }
        orderMapper.updateAllianceSourceOrderStatus(MerchantContextUtils.getTenantId(), allianceLink.sourceMemberId,
                allianceLink.sourceOrderId, childOrderStatus, SecurityUtils.getUserId());
    }

    /** 保存指定物流方向，保持物流表方向与订单表单字段一一对应。 */
    private void saveLogistics(MerchantOrderCreateCommand command, Integer direction, Long warehouseId,
            String companyValue, String trackingValue, boolean resetRouteSnapshot)
    {
        String companyText = companyValue == null ? "" : companyValue.trim();
        String trackingText = trackingValue == null ? "" : trackingValue.trim();
        if (companyText.isEmpty() && trackingText.isEmpty())
            return;
        if (companyText.isEmpty() || trackingText.isEmpty())
        {
            throw new com.ruoyi.common.core.exception.ServiceException(
                    direction.equals(RentalCode.DeliveryDirection.RETURN.getCode())
                            ? "归还快递公司和快递单号必须同时填写" : "快递公司和快递单号必须同时填写");
        }
        Long tenantId = MerchantContextUtils.getTenantId();
        Long merchantId = MerchantContextUtils.getMemberId();
        Long operatorId = com.ruoyi.common.security.utils.SecurityUtils.getUserId();
        try
        {
            Long logisticsId = orderMapper.selectLatestMerchantOrderLogisticsId(tenantId, merchantId,
                    command.orderId, direction);
            int affected = logisticsId == null ? 0 : orderMapper.updateMerchantOrderLogistics(tenantId, merchantId,
                    logisticsId, warehouseId, companyText, trackingText, operatorId);
            if (affected == 0)
            {
                orderMapper.insertMerchantOrderLogistics(tenantId, merchantId, command.orderId, warehouseId,
                        direction, companyText, trackingText, operatorId);
                logisticsId = orderMapper.selectLatestMerchantOrderLogisticsId(tenantId, merchantId,
                        command.orderId, direction);
            }
            if (resetRouteSnapshot)
            {
                resetLogisticsRouteSnapshot(tenantId, merchantId, logisticsId, operatorId);
            }
        }
        catch (org.springframework.dao.DuplicateKeyException exception)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("该快递单号已被其他订单使用，请更换后重试");
        }
    }

    /** 比较归一化后的快递字段，避免空白差异触发路线重新查询。 */
    private boolean logisticsChanged(String beforeCarrier, String beforeTrackingNo, String afterCarrier,
            String afterTrackingNo)
    {
        return !Objects.equals(normalizeLogisticsText(beforeCarrier), normalizeLogisticsText(afterCarrier))
                || !Objects.equals(normalizeLogisticsText(beforeTrackingNo), normalizeLogisticsText(afterTrackingNo));
    }

    private String normalizeLogisticsText(String value)
    {
        return value == null ? "" : value.trim();
    }

    /** 快递公司或单号发生变化时清理路线缓存，防止旧单号路线继续展示。 */
    private void resetLogisticsRouteSnapshot(Long tenantId, Long merchantId, Long logisticsId, Long operatorId)
    {
        if (logisticsRouteSnapshotService != null && logisticsId != null)
        {
            logisticsRouteSnapshotService.resetSnapshot(tenantId, merchantId, logisticsId, operatorId);
        }
    }

    /** 更新当前商户指定订单的客户和收件人信息，并记录快照及字段变更。 */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void updateRecipient(MerchantOrderRecipientUpdateCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null || command.orderId == null || command.customerName == null
                || command.customerName.trim().isEmpty() || command.customerPhone == null
                || command.customerPhone.trim().isEmpty() || command.recipientName == null
                || command.recipientName.trim().isEmpty() || command.recipientPhone == null
                || command.recipientPhone.trim().isEmpty() || command.recipientAddress == null
                || command.recipientAddress.trim().isEmpty())
        {
            throw new com.ruoyi.common.core.exception.ServiceException("客户和收件人信息不完整");
        }
        MerchantOrderCreateCommand before = get(command.orderId);
        Long operatorId = SecurityUtils.getUserId();
        String operatorName = defaultText(SecurityUtils.getUsername(), "系统");
        command.customerName = command.customerName.trim();
        command.customerPhone = command.customerPhone.trim();
        command.customerNickname = isBlank(command.customerNickname) ? null : command.customerNickname.trim();
        command.recipientName = command.recipientName.trim();
        command.recipientPhone = command.recipientPhone.trim();
        command.recipientAddress = command.recipientAddress.trim();
        command.remark = command.remark == null ? "" : command.remark.trim();
        int updated = orderMapper.updateMerchantOrderRecipient(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), command, SecurityUtils.getUserId());
        if (updated == 0)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("订单不存在或无权修改");
        }
        MerchantOrderCreateCommand after = get(command.orderId);
        recordRecipientSnapshot(after, SecurityUtils.getUserId());
        recordEditChange(after.orderId, "客户姓名", before.customerName, after.customerName,
                operatorId, operatorName);
        recordEditChange(after.orderId, "客户电话", before.customerPhone, after.customerPhone,
                operatorId, operatorName);
        recordEditChange(after.orderId, "买家昵称", before.customerNickname, after.customerNickname,
                operatorId, operatorName);
        recordEditChange(after.orderId, "收件人姓名", before.recipientName, after.recipientName,
                operatorId, operatorName);
        recordEditChange(after.orderId, "收件人电话", before.recipientPhone, after.recipientPhone,
                operatorId, operatorName);
        recordEditChange(after.orderId, "收货地址", before.recipientAddress, after.recipientAddress,
                operatorId, operatorName);
        recordEditChange(after.orderId, "备注", before.remark, after.remark,
                operatorId, operatorName);
    }

    /** 将运输中的订单转寄到新的收件地址，并保留转寄前后的快照记录。 */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void forward(MerchantOrderForwardCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null || command.orderId == null || isBlank(command.recipientName)
                || isBlank(command.recipientPhone) || isBlank(command.recipientAddress))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("收件人姓名、电话和地址不能为空");
        }
        MerchantOrderCreateCommand before = requireOrderActionOrder(command.orderId, "转寄");
        if (!isForwardableOrderStatus(before.orderStatus))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("只有运输中的订单可以转寄");
        }
        ensureOrderCanOperate(command.orderId);

        String recipientName = command.recipientName.trim();
        String recipientPhone = command.recipientPhone.trim();
        String recipientAddress = command.recipientAddress.trim();
        if (recipientName.length() > 128 || recipientPhone.length() > 32 || recipientAddress.length() > 500)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("新的收件信息长度超出限制");
        }

        MerchantOrderRecipientUpdateCommand recipient = new MerchantOrderRecipientUpdateCommand();
        recipient.orderId = command.orderId;
        recipient.customerName = before.customerName;
        recipient.customerPhone = before.customerPhone;
        recipient.customerNickname = before.customerNickname;
        recipient.recipientName = recipientName;
        recipient.recipientPhone = recipientPhone;
        recipient.recipientAddress = recipientAddress;
        recipient.remark = command.remark == null ? before.remark : command.remark.trim();
        updateRecipient(recipient);

        int inserted = orderMapper.insertOrderForwardRecord(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), command.orderId, before.recipientName,
                before.recipientPhone, before.recipientAddress, recipientName, recipientPhone,
                recipientAddress, recipient.remark, SecurityUtils.getUserId());
        if (inserted != 1)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("订单转寄记录保存失败");
        }
    }

    private boolean isForwardableOrderStatus(Integer status)
    {
        return RentalCode.OrderStatus.SHIPPING.getCode().equals(status)
                || RentalCode.OrderStatus.SHIP_IN_TRANSIT.getCode().equals(status)
                || RentalCode.OrderStatus.SHIPPED.getCode().equals(status);
    }

    /**
     * 校验订单渠道唯一编号是否属于当前商户可用范围。
     *
     * <p>平台启用的联盟渠道允许全部商户引用，商户自定义渠道只能由所属租户和商户引用；校验失败时
     * 在写入订单前终止请求，避免跨商户伪造 {@code channel_id} 关系。</p>
     *
     * @param channelId 待绑定的订单渠道唯一编号
     */
    private void validateChannel(Long channelId)
    {
        validateChannel(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), channelId);
    }

    private void validateChannel(Long tenantId, Long memberId, Long channelId)
    {
        if (channelId == null || orderMapper.countUsableOrderChannel(tenantId, memberId, channelId) != 1)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("订单渠道不存在、已停用或无权使用");
        }
    }

    /**
     * 查询当前商户订单列表，并将数据库投影转换为前端订单工作台结构。
     *
     * @param query 页面筛选条件；允许为空
     * @return 当前登录商户可访问的订单列表
     */
    public List<MerchantOrderListVO> list(MerchantOrderQuery query)
    {
        MerchantOrderQuery actualQuery = prepareQuery(query);

        List<MerchantOrderRow> rows = orderMapper.selectMerchantOrderList(
                MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), actualQuery);
        // 当前页只发起一次批量摘要查询，避免逐订单调用商户服务；远程失败时仍返回订单数据。
        Set<Long> operatorIds = new LinkedHashSet<Long>();
        for (MerchantOrderRow row : rows)
        {
            if (row == null)
            {
                continue;
            }
            if (isPositiveUserId(row.createBy))
            {
                operatorIds.add(row.createBy);
            }
            if (isPositiveUserId(row.updateBy))
            {
                operatorIds.add(row.updateBy);
            }
        }
        Map<Long, String> operatorNames = resolveOperatorNames(operatorIds);
        Set<Long> officialAccessoryIds = collectOfficialAccessoryIds(rows);
        Map<Long, String> officialAccessoryNames = resolveOfficialAccessoryNames(officialAccessoryIds);
        Map<String, AmapWeatherSnapshot> weatherSnapshots = new LinkedHashMap<String, AmapWeatherSnapshot>();
        Map<String, RegionWeatherRecord> regionWeatherSnapshots = loadRegionWeatherSnapshots();
        // VO 组装会创建新列表；列表总数由 Controller 使用独立 count SQL 覆盖，避免解析套餐明细投影。
        Page<MerchantOrderListVO> result = new Page<MerchantOrderListVO>();
        result.setTotal(new PageInfo<MerchantOrderRow>(rows).getTotal());
        for (MerchantOrderRow row : rows)
        {
            if (row != null)
            {
                result.add(toView(row, operatorNames, weatherSnapshots, regionWeatherSnapshots,
                        officialAccessoryNames));
            }
        }
        return result;
    }

    /**
     * 批量加载今天和明天的市级天气预报，避免订单列表按订单逐条查天气或触发第三方网络请求。
     *
     * @return 按“市级 adcode|日期”索引的天气记录；读取失败时返回空结果，由页面稳定显示待查询
     */
    private Map<String, RegionWeatherRecord> loadRegionWeatherSnapshots()
    {
        Map<String, RegionWeatherRecord> result = new LinkedHashMap<String, RegionWeatherRecord>();
        if (regionWeatherMapper == null)
        {
            return result;
        }
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        for (LocalDate weatherDate : Arrays.asList(today, today.plusDays(1)))
        {
            try
            {
                List<RegionWeatherRecord> records = regionWeatherMapper.selectByDate(
                        java.sql.Date.valueOf(weatherDate));
                if (records == null)
                {
                    continue;
                }
                for (RegionWeatherRecord record : records)
                {
                    if (record == null || isBlank(record.regionCode) || record.weatherDate == null)
                    {
                        continue;
                    }
                    result.put(weatherKey(record.regionCode, record.weatherDate.toLocalDate()), record);
                }
            }
            catch (RuntimeException exception)
            {
                log.warn("订单列表天气预报读取失败，weatherDate={}", weatherDate, exception);
            }
        }
        return result;
    }

    /** 构造市级天气预报的稳定索引键。 */
    private String weatherKey(String regionCode, LocalDate weatherDate)
    {
        return regionCode == null ? "" : regionCode.trim() + "|" + weatherDate;
    }

    /**
     * 查询当前商户订单总数，使用不包含套餐明细投影的独立 SQL。
     *
     * @param query 页面筛选条件；允许为空
     * @return 当前登录商户可访问的订单总数
     */
    public long count(MerchantOrderQuery query)
    {
        MerchantOrderQuery actualQuery = prepareQuery(query);
        return orderMapper.selectMerchantOrderCount(
                MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), actualQuery);
    }

    /** 将客户端筛选条件与可信商户、租户及仓库范围合并。 */
    private MerchantOrderQuery prepareQuery(MerchantOrderQuery query)
    {
        MerchantOrderQuery actualQuery = query == null ? new MerchantOrderQuery() : query;
        actualQuery.setStatus(normalizeStatus(actualQuery.getStatus()));
        actualQuery.setOrderScope(normalizeOrderScope(actualQuery.getOrderScope()));
        actualQuery.setDateStart(normalizeQueryDate(actualQuery.getDateStart()));
        actualQuery.setDateEnd(normalizeQueryDate(actualQuery.getDateEnd()));
        if (actualQuery.getDateStart() != null && actualQuery.getDateEnd() != null
                && LocalDate.parse(actualQuery.getDateStart()).isAfter(LocalDate.parse(actualQuery.getDateEnd())))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("日期范围不正确");
        }
        if ((actualQuery.getDateStart() != null || actualQuery.getDateEnd() != null)
                && actualQuery.getDateType() == null)
        {
            actualQuery.setDateType(MerchantOrderQuery.OrderDateType.SHIP.getCode());
        }
        if (actualQuery.getSortType() == null)
        {
            actualQuery.setSortType(MerchantOrderQuery.OrderSortType.CREATE.getCode());
        }
        if (actualQuery.getSortDirection() == null)
        {
            actualQuery.setSortDirection(MerchantOrderQuery.OrderSortDirection.DESC.getCode());
        }
        actualQuery.setAllWarehouseAccess(MerchantContextUtils.hasAllWarehouseAccess());
        Set<Long> warehouseIds = MerchantContextUtils.getWarehouseIds();
        actualQuery.setWarehouseIds(warehouseIds);
        return actualQuery;
    }

    /** 将订单日期筛选规范化为 ISO 日期，避免将未校验的日期直接交给 SQL。 */
    private String normalizeQueryDate(String value)
    {
        if (isBlank(value))
        {
            return null;
        }
        try
        {
            return LocalDate.parse(value.trim()).toString();
        }
        catch (DateTimeParseException exception)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("日期格式不正确，应为 yyyy-MM-dd");
        }
    }

    /** 只允许菜单预置的订单快捷视图进入 SQL，避免客户端构造未知筛选语义。 */
    private String normalizeOrderScope(String orderScope)
    {
        if (orderScope == null || orderScope.trim().isEmpty())
        {
            return null;
        }
        String normalized = orderScope.trim();
        if (MerchantOrderQuery.SCOPE_TODAY_SHIPPING.equals(normalized)
                || MerchantOrderQuery.SCOPE_TODAY_RETURN.equals(normalized)
                || MerchantOrderQuery.SCOPE_TODAY_ARRIVAL.equals(normalized)
                || MerchantOrderQuery.SCOPE_TOMORROW_ARRIVAL.equals(normalized)
                || MerchantOrderQuery.SCOPE_TODAY_CREATED.equals(normalized)
                || MerchantOrderQuery.SCOPE_TODAY_COMPLETED.equals(normalized))
        {
            return normalized;
        }
        return null;
    }

    /** 查询当前商户订单中心顶部概览，独立于订单列表分页和筛选条件。 */
    public MerchantOrderSummaryVO summary()
    {
        MerchantOrderQuery query = new MerchantOrderQuery();
        query.setAllWarehouseAccess(MerchantContextUtils.hasAllWarehouseAccess());
        query.setWarehouseIds(MerchantContextUtils.getWarehouseIds());
        return orderMapper.selectMerchantOrderSummary(
                MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), query);
    }

    /** 查询当前商户订单详情，编辑页面只能读取当前登录上下文可见的订单。 */
    public MerchantOrderDetailVO get(Long orderId)
    {
        MerchantOrderRow row = orderMapper.selectMerchantOrderById(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), orderId);
        if (row == null || row.orderId == null || !MerchantContextUtils.hasAllWarehouseAccess()
                && row.warehouseId != null && !MerchantContextUtils.hasWarehouseAccess(row.warehouseId))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("订单不存在或无权查看");
        }
        MerchantOrderDetailVO command = new MerchantOrderDetailVO();
        command.orderId = row.orderId;
        command.sourceOrderId = row.sourceOrderId;
        command.orderNo = row.orderNo;
        command.externalOrderNo = row.externalOrderNo;
        command.channelId = row.channelId;
        command.shopName = row.shopName;
        command.customerName = row.customerName;
        command.customerNickname = row.customerNickname;
        command.customerPhone = row.customerPhone;
        command.recipientName = defaultText(row.recipientName, row.customerName);
        command.recipientPhone = defaultText(row.recipientPhone, row.customerPhone);
        command.recipientAddress = row.recipientAddress;
        command.warehouseId = row.warehouseId;
        command.returnWarehouseId = row.returnWarehouseId;
        command.leaseStartDate = formatDate(row.leaseStartDate);
        command.leaseEndDate = formatDate(row.leaseEndDate);
        command.shipDueTime = formatDateTime(row.shipDueTime);
        command.shipTimeSlot = row.shipTimeSlot;
        command.returnDueTime = formatDateTime(row.returnDueTime);
        command.deliveryMethod = row.deliveryMethod;
        command.returnDeliveryMethod = row.returnDeliveryMethod;
        command.deliveryAmount = row.deliveryAmount;
        command.insuranceAmount = row.insuranceAmount;
        command.leaseDays = row.leaseDays;
        command.arrivalDate = formatDate(row.arrivalDate);
        command.rentAmount = row.rentAmount;
        command.depositAmount = row.depositAmount;
        command.orderStatus = row.orderStatus;
        command.payStatus = row.payStatus;
        command.timeEffectStatus = row.timeEffectStatus;
        command.weatherRiskFlag = row.weatherRiskFlag;
        command.priorityFlag = row.priorityFlag;
        command.sourceTextSnapshot = row.sourceTextSnapshot;
        command.productId = row.productId;
        command.orderItemId = row.orderItemId;
        command.packageId = row.packageId;
        command.packageDetailsJson = row.packageDetailsJson;
        command.snId = row.snId;
        command.accessoryItemIds = row.accessoryItemIds;
        command.quantity = row.quantity;
        command.version = row.version;
        command.remark = row.remark;
        command.expressCompany = row.shippingCarrier;
        command.expressTrackingNo = row.shippingTrackingNo;
        command.returnExpressCompany = row.returnCarrier;
        command.returnExpressTrackingNo = row.returnTrackingNo;
        command.returnInspection = orderMapper.selectLatestReturnInspection(
                MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), orderId);
        decorateReturnInspection(command.returnInspection);

        // 详情弹窗需要展示名称和套餐快照，不能把订单表中的外键编号直接渲染到页面。
        Map<Long, String> officialAccessoryNames = resolveOfficialAccessoryNames(
                collectOfficialAccessoryIds(Collections.singletonList(row)));
        PackageDetails packageDetails = parsePackageDetails(row.packageDetailsJson, officialAccessoryNames);
        command.channel = defaultText(row.channelName, defaultText(row.channelCode, "-"));
        command.warehouseName = defaultText(row.warehouseName, "未分配仓库");
        command.returnWarehouseName = defaultText(row.returnWarehouseName, "未分配仓库");
        command.model = defaultText(packageDetails.productName,
                defaultText(packageDetails.modelName,
                        defaultText(row.productName, defaultText(row.modelName, "商品待维护"))));
        command.productName = defaultText(packageDetails.productName, row.productName);
        command.productCode = defaultText(packageDetails.productCode,
                defaultText(row.productCode, "待维护"));
        command.packageName = defaultText(packageDetails.packageName,
                defaultText(row.packageName, "未绑定套餐"));
        command.rentalPeriod = formatDate(row.leaseStartDate) + " ~ " + formatDate(row.leaseEndDate);
        command.returnDate = formatDate(row.leaseEndDate);
        command.createdAt = formatDateTime(row.createTime);
        command.packageItems = packageDetails.itemNames;
        command.packageDetail = toPackageDetail(packageDetails, command.packageName,
                command.productCode, command.model);
        command.addOns = mergeAddOns(splitAddOns(row.addonNames), packageDetails.extraAccessoryNames);
        return command;
    }

    /**
     * 查询当前商户指定订单的批注历史。
     *
     * <p>先复用订单详情的租户、商户和仓库范围校验，再读取批注，防止仅凭订单唯一编号
     * 越权查看其他商户的历史记录。</p>
     *
     * @param orderId 订单唯一编号
     * @return 按创建时间倒序排列的批注历史
     */
    public List<MerchantOrderNoteVO> listNotes(Long orderId)
    {
        get(orderId);
        List<MerchantOrderNoteVO> notes = orderMapper.selectMerchantOrderNotes(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), orderId);
        fillOperatorNames(notes);
        return notes;
    }

    /**
     * 新增当前商户指定订单的批注。
     *
     * @param orderId 订单唯一编号
     * @param command 批注内容
     */
    @org.springframework.transaction.annotation.Transactional
    public void addNote(Long orderId, MerchantOrderNoteCreateCommand command)
    {
        if (command == null || isBlank(command.noteContent))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("批注内容不能为空");
        }
        if (command.noteType == null || !MerchantOrderNoteType.contains(command.noteType))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("请选择批注类型");
        }
        if (command.noteContent.trim().length() > 1000)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("批注内容不能超过1000个字符");
        }
        get(orderId);
        int inserted = orderMapper.insertMerchantOrderNote(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), orderId, command.noteContent.trim(), command.noteType,
                SecurityUtils.getUserId());
        if (inserted == 0)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("批注添加失败");
        }
    }

    /**
     * 查询当前商户指定订单的字段编辑历史，按创建时间倒序返回。
     *
     * <p>编辑日志属于执行面业务记录，查询前复用订单详情的租户、商户和仓库校验，
     * 用户昵称通过商户内部契约批量解析，不跨库 JOIN 商户用户表。</p>
     *
     * @param orderId 订单唯一编号
     * @return 订单字段变更历史
     */
    public List<MerchantOrderEditLogVO> listEditLogs(Long orderId)
    {
        get(orderId);
        List<MerchantOrderEditLogVO> logs = orderMapper.selectMerchantOrderEditLogs(
                MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), orderId);
        fillEditLogOperatorNames(logs);
        return logs;
    }

    /** 将订单编辑记录中的用户唯一编号解析为当前商户用户昵称。 */
    private void fillEditLogOperatorNames(List<MerchantOrderEditLogVO> logs)
    {
        if (logs == null || logs.isEmpty())
        {
            return;
        }
        Set<Long> operatorIds = new LinkedHashSet<Long>();
        for (MerchantOrderEditLogVO logRecord : logs)
        {
            if (logRecord != null && logRecord.operatorId != null)
            {
                operatorIds.add(logRecord.operatorId);
            }
        }
        Map<Long, String> operatorNames = resolveOperatorNames(operatorIds);
        for (MerchantOrderEditLogVO logRecord : logs)
        {
            if (logRecord == null)
            {
                continue;
            }
            String resolvedName = logRecord.operatorId == null ? null : operatorNames.get(logRecord.operatorId);
            logRecord.operatorName = isBlank(resolvedName)
                    ? defaultText(logRecord.operatorName, "系统") : resolvedName;
        }
    }

    /**
     * 将订单编辑前后的可编辑字段逐项写入执行面编辑日志。
     *
     * <p>只记录订单表单允许修改的字段，避免把服务端补齐的状态默认值误记成用户操作；
     * 每条记录保留前值和后值，后续可以直接用于详情弹层和问题追踪。</p>
     */
    private void recordEditChanges(MerchantOrderCreateCommand before, MerchantOrderCreateCommand after,
            Long operatorId)
    {
        if (before == null || after == null)
        {
            return;
        }
        String operatorName = defaultText(com.ruoyi.common.security.utils.SecurityUtils.getUsername(), "系统");
        Set<Long> warehouseIds = new LinkedHashSet<Long>();
        addReferenceId(warehouseIds, before.warehouseId);
        addReferenceId(warehouseIds, after.warehouseId);
        addReferenceId(warehouseIds, before.returnWarehouseId);
        addReferenceId(warehouseIds, after.returnWarehouseId);
        Set<Long> channelIds = new LinkedHashSet<Long>();
        addReferenceId(channelIds, before.channelId);
        addReferenceId(channelIds, after.channelId);
        Map<Long, String> warehouseNames = resolveReferenceNames(warehouseIds, true);
        Map<Long, String> channelNames = resolveReferenceNames(channelIds, false);

        recordEditChange(after.orderId, "订单渠道", before.channelId, after.channelId,
                displayReference(channelNames, before.channelId, "已删除渠道"),
                displayReference(channelNames, after.channelId, "已删除渠道"), operatorId, operatorName);
        recordEditChange(after.orderId, "外部订单编号", before.externalOrderNo, after.externalOrderNo, operatorId, operatorName);
        recordEditChange(after.orderId, "客户姓名", before.customerName, after.customerName, operatorId, operatorName);
        recordEditChange(after.orderId, "买家昵称", before.customerNickname, after.customerNickname, operatorId, operatorName);
        recordEditChange(after.orderId, "客户电话", before.customerPhone, after.customerPhone, operatorId, operatorName);
        recordEditChange(after.orderId, "收件人姓名", before.recipientName, after.recipientName, operatorId, operatorName);
        recordEditChange(after.orderId, "收件人电话", before.recipientPhone, after.recipientPhone, operatorId, operatorName);
        recordEditChange(after.orderId, "收货地址", before.recipientAddress, after.recipientAddress, operatorId, operatorName);
        recordEditChange(after.orderId, "发货仓库", before.warehouseId, after.warehouseId,
                displayReference(warehouseNames, before.warehouseId, "已删除仓库"),
                displayReference(warehouseNames, after.warehouseId, "已删除仓库"), operatorId, operatorName);
        recordEditChange(after.orderId, "归还仓库", before.returnWarehouseId, after.returnWarehouseId,
                displayReference(warehouseNames, before.returnWarehouseId, "已删除仓库"),
                displayReference(warehouseNames, after.returnWarehouseId, "已删除仓库"), operatorId, operatorName);
        recordEditChange(after.orderId, "租赁开始", before.leaseStartDate, after.leaseStartDate, operatorId, operatorName);
        recordEditChange(after.orderId, "租赁结束", before.leaseEndDate, after.leaseEndDate, operatorId, operatorName);
        recordEditChange(after.orderId, "应发货时间", before.shipDueTime, after.shipDueTime, operatorId, operatorName);
        recordEditChange(after.orderId, "发货时段", before.shipTimeSlot, after.shipTimeSlot, operatorId, operatorName);
        recordEditChange(after.orderId, "应归还时间", before.returnDueTime, after.returnDueTime, operatorId, operatorName);
        recordEditChange(after.orderId, "配送方式", deliveryMethodLabel(before.deliveryMethod),
                deliveryMethodLabel(after.deliveryMethod), operatorId, operatorName);
        recordEditChange(after.orderId, "归还配送方式", deliveryMethodLabel(before.returnDeliveryMethod),
                deliveryMethodLabel(after.returnDeliveryMethod), operatorId, operatorName);
        recordEditChange(after.orderId, "配送费用", before.deliveryAmount, after.deliveryAmount, operatorId, operatorName);
        recordEditChange(after.orderId, "保险费用", before.insuranceAmount, after.insuranceAmount, operatorId, operatorName);
        recordEditChange(after.orderId, "租金", before.rentAmount, after.rentAmount, operatorId, operatorName);
        recordEditChange(after.orderId, "押金", before.depositAmount, after.depositAmount, operatorId, operatorName);
        recordEditChange(after.orderId, "订单备注", before.remark, after.remark, operatorId, operatorName);
    }

    /** 写入单个字段变化；值相同或订单唯一编号缺失时不产生空日志。 */
    private void recordEditChange(Long orderId, String fieldName, Object beforeValue, Object afterValue,
            Long operatorId, String operatorName)
    {
        recordEditChange(orderId, fieldName, beforeValue, afterValue, beforeValue, afterValue, operatorId, operatorName,
                "订单编辑");
    }

    /** 写入同时包含原始值和商户可读值的单个字段变化。 */
    private void recordEditChange(Long orderId, String fieldName, Object beforeValue, Object afterValue,
            Object beforeDisplayValue, Object afterDisplayValue, Long operatorId, String operatorName)
    {
        recordEditChange(orderId, fieldName, beforeValue, afterValue, beforeDisplayValue, afterDisplayValue,
                operatorId, operatorName, "订单编辑");
    }

    /** 写入带业务原因的字段变化，履约状态和普通表单编辑共用同一张日志表。 */
    private void recordEditChange(Long orderId, String fieldName, Object beforeValue, Object afterValue,
            Object beforeDisplayValue, Object afterDisplayValue, Long operatorId, String operatorName,
            String changeReason)
    {
        String beforeText = auditValue(beforeValue);
        String afterText = auditValue(afterValue);
        if (orderId == null || Objects.equals(beforeText, afterText))
        {
            return;
        }
        String beforeDisplayText = auditValue(beforeDisplayValue);
        String afterDisplayText = auditValue(afterDisplayValue);
        orderMapper.insertMerchantOrderEditLog(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                orderId, fieldName, beforeText, afterText, beforeDisplayText, afterDisplayText,
                defaultText(changeReason, "订单编辑"), operatorId,
                operatorName, null);
    }

    /** 将订单状态动作写入履约时间线，状态展示统一走订单状态枚举。 */
    private void recordStatusChange(MerchantOrderCreateCommand before, MerchantOrderCreateCommand after,
            String changeReason)
    {
        if (before == null || after == null || Objects.equals(before.orderStatus, after.orderStatus))
        {
            return;
        }
        Long operatorId = SecurityUtils.getUserId();
        recordEditChange(after.orderId, "订单状态", before.orderStatus, after.orderStatus,
                orderStatusLabel(before.orderStatus), orderStatusLabel(after.orderStatus), operatorId,
                defaultText(SecurityUtils.getUsername(), "系统"), changeReason);
        if (externalOrderCallbackService != null)
        {
            externalOrderCallbackService.enqueueOrderStatusChanged(MerchantContextUtils.getTenantId(),
                    MerchantContextUtils.getMemberId(), after.orderId, before.orderStatus, after.orderStatus,
                    changeReason);
        }
    }

    private String orderStatusLabel(Integer status)
    {
        if (status != null)
        {
            for (RentalCode.OrderStatus value : RentalCode.OrderStatus.values())
            {
                if (value.getCode().equals(status))
                {
                    return value.getLabel();
                }
            }
        }
        return "未知状态";
    }

    /** 批量收集关联对象唯一编号，名称解析失败时也不能把编号作为展示兜底。 */
    private void addReferenceId(Set<Long> referenceIds, Long referenceId)
    {
        if (referenceId != null && referenceId > 0)
        {
            referenceIds.add(referenceId);
        }
    }

    /** 按关联类型批量解析审计展示名称，避免逐条记录查询数据库。 */
    private Map<Long, String> resolveReferenceNames(Set<Long> referenceIds, boolean warehouse)
    {
        Map<Long, String> names = new LinkedHashMap<Long, String>();
        if (referenceIds == null || referenceIds.isEmpty() || orderMapper == null)
        {
            return names;
        }
        try
        {
            List<MerchantAuditReferenceModel> references = warehouse
                    ? orderMapper.selectWarehouseNames(MerchantContextUtils.getTenantId(),
                            MerchantContextUtils.getMemberId(), new ArrayList<Long>(referenceIds))
                    : orderMapper.selectOrderChannelNames(MerchantContextUtils.getTenantId(),
                            MerchantContextUtils.getMemberId(), new ArrayList<Long>(referenceIds));
            if (references != null)
            {
                for (MerchantAuditReferenceModel reference : references)
                {
                    if (reference != null && reference.referenceId != null && !isBlank(reference.referenceName))
                    {
                        names.put(reference.referenceId, reference.referenceName);
                    }
                }
            }
        }
        catch (RuntimeException exception)
        {
            log.warn("订单审计关联名称解析失败，使用安全展示文本：类型={}, 原因={}",
                    warehouse ? "仓库" : "订单渠道", exception.getMessage());
        }
        return names;
    }

    /** 将关联对象唯一编号转换为名称或不含编号的安全占位文本。 */
    private String displayReference(Map<Long, String> names, Long referenceId, String missingText)
    {
        if (referenceId == null)
        {
            return "";
        }
        return defaultText(names == null ? null : names.get(referenceId), missingText);
    }

    /** 将变更值转换为稳定文本，避免将 null 写成字符串 null。 */
    private String auditValue(Object value)
    {
        if (value == null)
        {
            return "";
        }
        if (value instanceof BigDecimal)
        {
            return ((BigDecimal) value).toPlainString();
        }
        return String.valueOf(value);
    }

    /**
     * 通过商户用户服务解析批注操作人昵称，避免 rental-business 跨库访问商户用户表。
     * 查询失败时保留批注内容并使用系统标识，不能因为昵称服务短暂不可用而阻断历史记录查看。
     *
     * @param notes 已按时间倒序查询的批注历史
     */
    private void fillOperatorNames(List<MerchantOrderNoteVO> notes)
    {
        if (notes == null || notes.isEmpty())
        {
            return;
        }
        Set<Long> operatorIds = new LinkedHashSet<Long>();
        for (MerchantOrderNoteVO note : notes)
        {
            if (note != null && note.operatorId != null)
            {
                operatorIds.add(note.operatorId);
            }
        }
        Map<Long, String> operatorNames = resolveOperatorNames(operatorIds);
        for (MerchantOrderNoteVO note : notes)
        {
            if (note == null)
            {
                continue;
            }
            String operatorName = note.operatorId == null ? null : operatorNames.get(note.operatorId);
            note.operatorName = isBlank(operatorName) ? "系统" : operatorName;
        }
    }

    /** 通过商户服务一次批量解析用户昵称，避免订单列表和批注历史逐条发起远程请求。 */
    private Map<Long, String> resolveOperatorNames(Set<Long> operatorIds)
    {
        Map<Long, String> names = new LinkedHashMap<Long, String>();
        if (operatorIds.isEmpty() || remoteMerchantUserService == null)
        {
            return names;
        }
        try
        {
            MerchantUserSummaryQuery query = new MerchantUserSummaryQuery();
            query.setTenantId(MerchantContextUtils.getTenantId());
            query.setMerchantId(MerchantContextUtils.getMemberId());
            query.setUserIds(operatorIds);
            R<List<MerchantUserSummary>> response = remoteMerchantUserService.getSummaries(query,
                    SecurityConstants.INNER, Constants.CLIENT_TYPE_MERCHANT);
            if (response == null || !R.isSuccess(response))
            {
                log.warn("商户用户名称解析失败，商户用户服务返回异常: code={}, message={}",
                        response == null ? null : response.getCode(), response == null ? null : response.getMsg());
                return names;
            }
            List<MerchantUserSummary> summaries = response.getData();
            if (summaries == null)
            {
                return names;
            }
            for (MerchantUserSummary summary : summaries)
            {
                if (summary == null || summary.getUserId() == null)
                {
                    continue;
                }
                String displayName = isBlank(summary.getNickname()) ? summary.getUsername() : summary.getNickname();
                if (!isBlank(displayName))
                {
                    names.put(summary.getUserId(), displayName);
                }
            }
        }
        catch (RuntimeException exception)
        {
            log.warn("商户用户名称解析失败，订单列表和批注历史降级显示", exception);
        }
        return names;
    }

    /** 判断展示字段是否为空白，避免将空昵称渲染到列表。 */
    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }

    /** 统一清理可选文本，空白文本按未填写处理。 */
    private String normalizeText(String value)
    {
        if (value == null)
        {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * 校验订单状态筛选值。
     *
     * @param status 前端传入的订单状态数值编码
     * @return 可用于数据库筛选的订单状态编码；为空时返回 null
     */
    private Integer normalizeStatus(Integer status)
    {
        return code(status, RentalCode.OrderStatus.values(), null, "订单状态编码不合法");
    }

    /**
     * 统一补齐并校验订单的状态字段，避免空值或散落的默认字符串进入数据库。
     *
     * @param command 商户订单写入命令
     */
    private void normalizeCommandStatus(MerchantOrderCreateCommand command)
    {
        command.orderStatus = code(command.orderStatus, RentalCode.OrderStatus.values(),
                RentalCode.OrderStatus.PENDING_SHIP.getCode(), "订单状态编码不合法");
        command.payStatus = code(command.payStatus, RentalCode.PaymentStatus.values(),
                RentalCode.PaymentStatus.UNPAID.getCode(), "支付状态编码不合法");
        command.timeEffectStatus = code(command.timeEffectStatus, RentalCode.TimeEffectStatus.values(),
                RentalCode.TimeEffectStatus.NORMAL.getCode(), "订单时效状态编码不合法");
        command.weatherRiskFlag = code(command.weatherRiskFlag, RentalCode.YesNo.values(),
                RentalCode.YesNo.NO.getCode(), "天气风险标识不合法");
        command.priorityFlag = code(command.priorityFlag, RentalCode.YesNo.values(),
                RentalCode.YesNo.NO.getCode(), "优先处理标识不合法");
    }

    /** 统一校验配送字段并固化可由租期推导的订单快照。 */
    private void normalizeOrderDetails(MerchantOrderCreateCommand command)
    {
        command.customerName = normalizeText(command.customerName);
        command.customerPhone = normalizeText(command.customerPhone);
        command.recipientName = isBlank(command.recipientName)
                ? command.customerName : normalizeText(command.recipientName);
        command.recipientPhone = isBlank(command.recipientPhone)
                ? command.customerPhone : normalizeText(command.recipientPhone);
        command.recipientAddress = normalizeText(command.recipientAddress);
        if (isBlank(command.recipientName) || isBlank(command.recipientPhone)
                || isBlank(command.recipientAddress))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("收件人信息不能为空");
        }
        command.customerNickname = isBlank(command.customerNickname) ? null : command.customerNickname.trim();
        command.shipTimeSlot = isBlank(command.shipTimeSlot) ? null : command.shipTimeSlot.trim();
        MerchantOrderDeliveryMethod deliveryMethod = isBlank(command.deliveryMethod)
                ? MerchantOrderDeliveryMethod.EXPRESS
                : MerchantOrderDeliveryMethod.fromCode(command.deliveryMethod);
        if (deliveryMethod == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("配送方式不合法");
        }
        command.deliveryMethod = deliveryMethod.getCode();
        MerchantOrderDeliveryMethod returnDeliveryMethod = isBlank(command.returnDeliveryMethod)
                ? MerchantOrderDeliveryMethod.EXPRESS
                : MerchantOrderDeliveryMethod.fromCode(command.returnDeliveryMethod);
        if (returnDeliveryMethod == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("归还配送方式不合法");
        }
        command.returnDeliveryMethod = returnDeliveryMethod.getCode();
        command.deliveryAmount = nonNegativeAmount(command.deliveryAmount, "配送费用不能小于零");
        command.insuranceAmount = nonNegativeAmount(command.insuranceAmount, "保险费用不能小于零");

        Date leaseStart = DateUtils.parseDate(command.leaseStartDate);
        Date leaseEnd = DateUtils.parseDate(command.leaseEndDate);
        if (leaseStart == null || leaseEnd == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("租赁日期格式不正确");
        }
        command.leaseDays = Math.toIntExact(ChronoUnit.DAYS.between(toBusinessDate(leaseStart),
                toBusinessDate(leaseEnd)) + 1);

        Date shipDueTime = isBlank(command.shipDueTime) ? null : DateUtils.parseDate(command.shipDueTime);
        Date returnDueTime = isBlank(command.returnDueTime) ? null : DateUtils.parseDate(command.returnDueTime);
        if (shipDueTime == null || returnDueTime == null)
        {
            command.arrivalDate = null;
            return;
        }
        long dayDifference = ChronoUnit.DAYS.between(toBusinessDate(shipDueTime), toBusinessDate(leaseStart));
        command.arrivalDate = toBusinessDate(returnDueTime).plusDays(dayDifference).toString();
    }

    /** 费用未填写时按零处理，避免空值进入订单金额字段。 */
    private BigDecimal nonNegativeAmount(BigDecimal amount, String errorMessage)
    {
        if (amount != null && amount.signum() < 0)
        {
            throw new com.ruoyi.common.core.exception.ServiceException(errorMessage);
        }
        return amount == null ? BigDecimal.ZERO : amount;
    }

    /** 将配送方式编码转换为用户可读文本。 */
    private String deliveryMethodLabel(String code)
    {
        MerchantOrderDeliveryMethod method = MerchantOrderDeliveryMethod.fromCode(code);
        return method == null ? MerchantOrderDeliveryMethod.EXPRESS.getLabel() : method.getLabel();
    }

    /**
     * 校验并补齐受控数值编码，所有状态、类型和标志位都通过枚举集中维护。
     *
     * @param value 前端或数据库传入的数值编码
     * @param values 当前字段可用的枚举值
     * @param fallback 值为空时使用的默认编码，可为空
     * @param errorMessage 非法编码的业务异常文本
     * @param <E> 数值编码枚举类型
     * @return 已校验的数值编码
     */
    private <E extends Enum<E> & NumericCode> Integer code(Integer value, E[] values, Integer fallback,
            String errorMessage)
    {
        Integer actualValue = value == null ? fallback : value;
        if (actualValue == null)
        {
            return null;
        }
        for (E candidate : values)
        {
            if (candidate.getCode().equals(actualValue))
            {
                return actualValue;
            }
        }
        throw new com.ruoyi.common.core.exception.ServiceException(errorMessage);
    }

    /**
     * 将内部查询行转换为商户端订单列表响应。
     *
     * @param row 数据库查询行
     * @param operatorNames 当前商户用户唯一编号到昵称的批量解析结果
     * @param weatherSnapshots 当前列表天气缓存，供旧环境天气表不可用时兜底
     * @param regionWeatherSnapshots 当前列表已批量读取的今日和明天天气预报
     * @param officialAccessoryNames 当前页待补齐的官方配件名称
     * @return 前端订单列表响应
     */
    private MerchantOrderListVO toView(MerchantOrderRow row, Map<Long, String> operatorNames,
            Map<String, AmapWeatherSnapshot> weatherSnapshots,
            Map<String, RegionWeatherRecord> regionWeatherSnapshots,
            Map<Long, String> officialAccessoryNames)
    {
        MerchantOrderListVO view = new MerchantOrderListVO();
        view.orderId = row.orderId;
        view.isAllianceChild = row.sourceOrderId != null;
        view.isAllianceSourceLocked = row.allianceSourceLocked;
        view.leaseStartDate = formatDate(row.leaseStartDate);
        view.leaseEndDate = formatDate(row.leaseEndDate);
        view.leaseDays = row.leaseDays;
        view.arrivalDate = formatDate(row.arrivalDate);
        view.externalOrderNo = row.externalOrderNo;
        view.orderNo = row.orderNo;
        view.warehouseId = row.warehouseId;
        view.returnWarehouseId = row.returnWarehouseId;
        view.returnWarehouseName = row.returnWarehouseName;
        view.productId = row.productId;
        view.packageId = row.packageId;
        PackageDetails packageDetails = parsePackageDetails(row.packageDetailsJson, officialAccessoryNames);
        view.model = defaultText(packageDetails.productName,
                defaultText(packageDetails.modelName,
                        defaultText(row.productName, defaultText(row.modelName, "商品待维护"))));
        view.packageName = defaultText(packageDetails.packageName, defaultText(row.packageName, "未绑定套餐"));
        view.rentalPeriod = formatDate(row.leaseStartDate) + " ~ " + formatDate(row.leaseEndDate);
        view.returnDate = formatDate(row.leaseEndDate);
        view.rent = row.rentAmount == null ? BigDecimal.ZERO : row.rentAmount;
        view.deliveryAmount = row.deliveryAmount == null ? BigDecimal.ZERO : row.deliveryAmount;
        view.insuranceAmount = row.insuranceAmount == null ? BigDecimal.ZERO : row.insuranceAmount;
        view.channelId = row.channelId;
        view.channel = defaultText(row.channelName, defaultText(row.channelCode, "-"));
        view.merchant = defaultText(row.shopName, "-");
        view.recipientName = defaultText(row.recipientName, row.customerName);
        view.recipientPhone = defaultText(row.recipientPhone, row.customerPhone);
        view.recipientAddress = defaultText(row.recipientAddress, "未填写收货地址");
        view.customer = toCustomer(row);
        view.shipping = toShipping(row);
        view.weather = toWeather(row, weatherSnapshots, regionWeatherSnapshots);
        view.delivery = toDelivery(row);
        view.status = row.orderStatus;
        view.expressCompany = row.shippingCarrier;
        view.trackingNo = defaultText(row.shippingTrackingNo, "待安排");
        view.returnExpressCompany = row.returnCarrier;
        view.returnNo = defaultText(row.returnTrackingNo, "待安排");
        view.createdAt = formatDateTime(row.createTime);
        view.productCode = defaultText(packageDetails.productCode, defaultText(row.productCode, "待维护"));
        view.packageItems = packageDetails.itemNames;
        view.packageDetail = toPackageDetail(packageDetails, view.packageName, view.productCode, view.model);
        view.addOns = mergeAddOns(splitAddOns(row.addonNames), packageDetails.extraAccessoryNames);
        view.attention = row.attention;
        view.operator = resolveDisplayName(row.createBy, operatorNames, "系统");
        view.coordinator = resolveDisplayName(row.updateBy, operatorNames, "待分配");
        return view;
    }

    /**
     * 收集当前页仍需要解析的官方配件唯一编号；只解析占位名称，已有历史名称的订单不发起远程调用。
     */
    private Set<Long> collectOfficialAccessoryIds(List<MerchantOrderRow> rows)
    {
        Set<Long> accessoryIds = new LinkedHashSet<Long>();
        if (rows == null || rows.isEmpty())
        {
            return accessoryIds;
        }
        ObjectMapper parser = objectMapper == null ? new ObjectMapper() : objectMapper;
        for (MerchantOrderRow row : rows)
        {
            if (row == null || isBlank(row.packageDetailsJson))
            {
                continue;
            }
            try
            {
                JsonNode root = parser.readTree(row.packageDetailsJson);
                JsonNode items = packageItemsNode(root);
                if (items == null || !items.isArray())
                {
                    continue;
                }
                for (JsonNode item : items)
                {
                    Integer itemRole = integerValue(item, "itemRole", "item_role");
                    String itemName = textAny(item, "itemName", "item_name");
                    Long accessoryId = longValue(item, "accessoryId", "accessory_id");
                    if (Integer.valueOf(2).equals(itemRole) && accessoryId != null
                            && isOfficialAccessoryPlaceholder(itemName))
                    {
                        accessoryIds.add(accessoryId);
                    }
                }
            }
            catch (JsonProcessingException exception)
            {
                log.warn("订单套餐详情快照解析失败，跳过官方配件名称补齐");
            }
        }
        return accessoryIds;
    }

    /** 当前页只批量读取一次平台官方配件选项，平台暂不可用时保留可识别的占位名称。 */
    private Map<Long, String> resolveOfficialAccessoryNames(Set<Long> accessoryIds)
    {
        Map<Long, String> names = new LinkedHashMap<Long, String>();
        for (PlatformAccessoryOption option : resolveOfficialAccessoryOptions(accessoryIds).values())
        {
            if (!isBlank(option.getAccessoryName()))
            {
                names.put(option.getAccessoryId(), option.getAccessoryName());
            }
        }
        return names;
    }

    /** 一次读取指定官方配件，供套餐快照写入和历史快照名称补齐共用。 */
    private Map<Long, PlatformAccessoryOption> resolveOfficialAccessoryOptions(Set<Long> accessoryIds)
    {
        Map<Long, PlatformAccessoryOption> options = new LinkedHashMap<Long, PlatformAccessoryOption>();
        if (accessoryIds == null || accessoryIds.isEmpty() || remoteAccessoryService == null)
        {
            return options;
        }
        try
        {
            R<List<PlatformAccessoryOption>> response = remoteAccessoryService.listActive(SecurityConstants.INNER);
            if (response == null || !R.isSuccess(response) || response.getData() == null)
            {
                log.warn("订单套餐官方配件详情读取失败，保留已有快照信息");
                return options;
            }
            for (PlatformAccessoryOption option : response.getData())
            {
                if (option == null || option.getAccessoryId() == null
                        || !accessoryIds.contains(option.getAccessoryId()))
                {
                    continue;
                }
                options.put(option.getAccessoryId(), option);
            }
        }
        catch (RuntimeException exception)
        {
            log.warn("订单套餐官方配件详情读取异常，保留已有快照信息", exception);
        }
        return options;
    }

    /**
     * 校验订单额外配件必须与所选套餐的主型号兼容，并固化为不含技术编号的展示快照。
     */
    private List<Map<String, Object>> resolveExtraAccessories(String accessoryItemIds, Long tenantId,
            Long memberId, Long modelId)
    {
        if (isBlank(accessoryItemIds))
        {
            return Collections.emptyList();
        }
        try
        {
            JsonNode values = objectMapper.readTree(accessoryItemIds);
            if (values == null || !values.isArray())
            {
                throw new com.ruoyi.common.core.exception.ServiceException("额外配件数据格式不正确");
            }
            Map<Long, Map<String, Object>> customAccessories = new LinkedHashMap<Long, Map<String, Object>>();
            for (Map<String, Object> item : merchantPackageMapper.selectMerchantAccessoryOptions(tenantId, memberId, modelId))
            {
                Long accessoryId = numberValue(item, "merchantAccessoryId");
                if (accessoryId != null)
                {
                    customAccessories.put(accessoryId, item);
                }
            }
            Set<Long> officialIds = new LinkedHashSet<Long>();
            for (JsonNode value : values)
            {
                if ("official".equals(textAny(value, "sourceType")))
                {
                    Long accessoryId = longValue(value, "accessoryId");
                    if (accessoryId == null || accessoryId <= 0)
                    {
                        throw new com.ruoyi.common.core.exception.ServiceException("额外官方配件不存在");
                    }
                    officialIds.add(accessoryId);
                }
            }
            Map<Long, PlatformAccessoryOption> officialAccessories = resolveOfficialAccessoryOptions(officialIds);
            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            for (JsonNode value : values)
            {
                String sourceType = textAny(value, "sourceType");
                Integer quantity = integerValue(value, "quantity");
                if (quantity == null || quantity <= 0)
                {
                    throw new com.ruoyi.common.core.exception.ServiceException("额外配件数量必须大于零");
                }
                Map<String, Object> snapshot = new LinkedHashMap<String, Object>();
                snapshot.put("quantity", quantity);
                if ("official".equals(sourceType))
                {
                    PlatformAccessoryOption accessory = officialAccessories.get(longValue(value, "accessoryId"));
                    if (accessory == null || accessory.getModelIds() == null
                            || !accessory.getModelIds().contains(modelId))
                    {
                        throw new com.ruoyi.common.core.exception.ServiceException("额外官方配件与套餐主型号不兼容");
                    }
                    snapshot.put("itemName", accessory.getAccessoryName());
                    snapshot.put("itemCode", accessory.getAccessoryCode());
                }
                else if ("custom".equals(sourceType))
                {
                    Long accessoryId = longValue(value, "merchantAccessoryId");
                    Map<String, Object> accessory = customAccessories.get(accessoryId);
                    if (accessory == null || !compatibleWithModel(accessory, modelId))
                    {
                        throw new com.ruoyi.common.core.exception.ServiceException("额外自定义配件与套餐主型号不兼容");
                    }
                    snapshot.put("itemName", stringValue(accessory.get("productName")));
                    snapshot.put("itemCode", stringValue(accessory.get("productCode")));
                }
                else
                {
                    throw new com.ruoyi.common.core.exception.ServiceException("额外配件来源不正确");
                }
                result.add(snapshot);
            }
            return result;
        }
        catch (JsonProcessingException exception)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("额外配件数据格式不正确");
        }
    }

    private boolean compatibleWithModel(Map<String, Object> accessory, Long modelId)
    {
        String modelIds = stringValue(accessory.get("compatibleModelIds"));
        if (isBlank(modelIds) || modelId == null)
        {
            return false;
        }
        for (String item : modelIds.split(","))
        {
            try
            {
                if (modelId.equals(Long.valueOf(item.trim())))
                {
                    return true;
                }
            }
            catch (NumberFormatException ignored)
            {
                // 配件目录中的异常兼容型号值不应放宽校验。
            }
        }
        return false;
    }

    /** 从订单自身的套餐 JSON 快照读取展示字段，避免订单列表回查套餐、商品和配件目录。 */
    private PackageDetails parsePackageDetails(String packageDetailsJson, Map<Long, String> officialAccessoryNames)
    {
        PackageDetails result = new PackageDetails();
        if (isBlank(packageDetailsJson))
        {
            return result;
        }
        try
        {
            ObjectMapper parser = objectMapper == null ? new ObjectMapper() : objectMapper;
            JsonNode root = parser.readTree(packageDetailsJson);
            if (root == null)
            {
                return result;
            }
            result.packageName = textAny(root, "packageName", "package_name");
            result.packageCode = textAny(root, "packageCode", "package_code");
            JsonNode items = packageItemsNode(root);
            JsonNode mainProduct = root.isObject() ? root.path("mainProduct") : null;
            if (mainProduct == null || !mainProduct.isObject())
            {
                mainProduct = firstMainItem(items);
            }
            result.productName = textAny(mainProduct, "productName", "product_name", "itemName", "item_name");
            result.productCode = textAny(mainProduct, "productCode", "product_code", "itemCode", "item_code");
            result.modelName = textAny(mainProduct, "modelName", "model_name", "itemModelName", "item_model_name");
            if (items != null && items.isArray())
            {
                for (JsonNode item : items)
                {
                    Integer itemRole = integerValue(item, "itemRole", "item_role");
                    String itemName = textAny(item, "itemName", "item_name");
                    Long accessoryId = longValue(item, "accessoryId", "accessory_id");
                    if (Integer.valueOf(2).equals(itemRole))
                    {
                        String officialName = officialAccessoryNames == null ? null : officialAccessoryNames.get(accessoryId);
                        if (!isBlank(officialName))
                        {
                            itemName = officialName;
                        }
                    }
                    if (isOfficialAccessoryPlaceholder(itemName))
                    {
                        itemName = OFFICIAL_ACCESSORY_LABEL;
                    }
                    if (isBlank(itemName))
                    {
                        itemName = itemRoleLabel(itemRole);
                    }
                    Integer quantity = integerValue(item, "quantity");
                    Integer requiredFlag = integerValue(item, "requiredFlag", "required_flag");
                    PackageItemDetail detail = new PackageItemDetail();
                    detail.itemIndex = result.itemDetails.size();
                    detail.itemType = itemRoleLabel(itemRole);
                    detail.name = itemName;
                    detail.code = textAny(item, "itemCode", "item_code", "accessoryCode", "accessory_code");
                    detail.model = textAny(item, "itemModelName", "item_model_name", "modelName", "model_name");
                    detail.quantity = quantity == null || quantity <= 0 ? 1 : quantity;
                    detail.requiredText = Integer.valueOf(0).equals(requiredFlag) ? "可选" : "必选";
                    detail.selectableDevice = isSelectableDevice(item, itemRole);
                    detail.unitIds = jsonLongList(item, "unitIds", "unit_ids");
                    Long unitId = longValue(item, "unitId", "unit_id");
                    if (detail.unitIds.isEmpty() && unitId != null)
                    {
                        detail.unitIds.add(unitId);
                    }
                    detail.serialNumbers = jsonTextList(item, "serialNumbers", "serial_numbers");
                    String serialNumber = textAny(item, "serialNumber", "serial_number");
                    if (detail.serialNumbers.isEmpty() && !isBlank(serialNumber))
                    {
                        detail.serialNumbers.add(serialNumber);
                    }
                    result.itemNames.add(itemName);
                    result.itemDetails.add(detail);
                    if (Integer.valueOf(1).equals(itemRole) && isBlank(result.productName))
                    {
                        result.productName = itemName;
                        result.productCode = detail.code;
                        result.modelName = detail.model;
                    }
                }
            }
            JsonNode extraAccessories = root.isObject() ? root.path("extraAccessories") : null;
            if (extraAccessories.isArray())
            {
                for (JsonNode item : extraAccessories)
                {
                    String itemName = textAny(item, "itemName", "item_name");
                    Integer quantity = integerValue(item, "quantity");
                    if (!isBlank(itemName))
                    {
                        result.extraAccessoryNames.add(itemName + " x" + (quantity == null || quantity <= 0 ? 1 : quantity));
                    }
                }
            }
        }
        catch (JsonProcessingException exception)
        {
            log.warn("订单套餐详情快照解析失败，降级为空明细");
        }
        return result;
    }

    /** 将内部快照转换为不含技术主键的套餐详情响应。 */
    private MerchantOrderListVO.PackageDetail toPackageDetail(PackageDetails source, String packageName,
            String productCode, String productName)
    {
        MerchantOrderListVO.PackageDetail result = new MerchantOrderListVO.PackageDetail();
        result.packageName = defaultText(source.packageName, packageName);
        result.packageCode = source.packageCode;
        if (!isBlank(source.productName) || !isBlank(source.productCode) || !isBlank(source.modelName)
                || !isBlank(productName) || !isBlank(productCode))
        {
            result.mainProduct = new MerchantOrderListVO.MainProduct();
            result.mainProduct.name = defaultText(source.productName, productName);
            result.mainProduct.code = defaultText(source.productCode, productCode);
            result.mainProduct.model = source.modelName;
        }
        result.items = new ArrayList<MerchantOrderListVO.Item>();
        for (PackageItemDetail item : source.itemDetails)
        {
            MerchantOrderListVO.Item detail = new MerchantOrderListVO.Item();
            detail.itemType = item.itemType;
            detail.itemIndex = item.itemIndex;
            detail.name = item.name;
            detail.code = item.code;
            detail.model = item.model;
            detail.quantity = item.quantity;
            detail.requiredText = item.requiredText;
            detail.selectableDevice = item.selectableDevice;
            if (item.serialNumbers != null && !item.serialNumbers.isEmpty())
            {
                detail.serialNumbers = item.serialNumbers;
            }
            if (item.unitIds != null && !item.unitIds.isEmpty())
            {
                detail.unitIds = item.unitIds;
            }
            result.items.add(detail);
        }
        result.extraAccessories = source.extraAccessoryNames;
        return result;
    }

    private JsonNode packageItemsNode(JsonNode root)
    {
        if (root == null)
        {
            return null;
        }
        return root.isArray() ? root : root.path("items");
    }

    private JsonNode firstMainItem(JsonNode items)
    {
        if (items == null || !items.isArray())
        {
            return null;
        }
        for (JsonNode item : items)
        {
            if (Integer.valueOf(1).equals(integerValue(item, "itemRole", "item_role")))
            {
                return item;
            }
        }
        return null;
    }

    private String itemRoleLabel(Integer itemRole)
    {
        if (Integer.valueOf(1).equals(itemRole))
        {
            return "主商品";
        }
        if (Integer.valueOf(2).equals(itemRole))
        {
            return OFFICIAL_ACCESSORY_LABEL;
        }
        if (Integer.valueOf(3).equals(itemRole))
        {
            return MERCHANT_ACCESSORY_LABEL;
        }
        if (Integer.valueOf(4).equals(itemRole))
        {
            return NON_MAIN_PRODUCT_LABEL;
        }
        return PACKAGE_ITEM_LABEL;
    }

    private boolean isOfficialAccessoryPlaceholder(String itemName)
    {
        return isBlank(itemName) || OFFICIAL_ACCESSORY_LABEL.equals(itemName)
                || (itemName != null && itemName.startsWith(OFFICIAL_ACCESSORY_TOKEN));
    }

    private String textAny(JsonNode node, String... fieldNames)
    {
        if (node == null || !node.isObject() || fieldNames == null)
        {
            return null;
        }
        for (String fieldName : fieldNames)
        {
            String value = text(node, fieldName);
            if (!isBlank(value))
            {
                return value;
            }
        }
        return null;
    }

    private String text(JsonNode node, String fieldName)
    {
        if (node == null || !node.isObject() || fieldName == null || node.path(fieldName).isMissingNode()
                || node.path(fieldName).isNull())
        {
            return null;
        }
        String value = node.path(fieldName).asText();
        return isBlank(value) ? null : value;
    }

    private Integer integerValue(JsonNode node, String... fieldNames)
    {
        String value = textAny(node, fieldNames);
        if (isBlank(value))
        {
            return null;
        }
        try
        {
            return Integer.valueOf(value);
        }
        catch (NumberFormatException exception)
        {
            return null;
        }
    }

    /** 逐件设备明细才允许从当前发货仓库绑定库存单元。 */
    private boolean isSelectableDevice(JsonNode item, Integer itemRole)
    {
        if (!Integer.valueOf(1).equals(itemRole) && !Integer.valueOf(4).equals(itemRole))
        {
            return false;
        }
        JsonNode selectableDevice = item == null ? null : item.get("selectableDevice");
        if (selectableDevice != null && !selectableDevice.isNull())
        {
            return selectableDevice.asBoolean();
        }
        String stockMode = textAny(item, "stockMode", "stock_mode");
        return isSerialStockMode(stockMode);
    }

    /** 将套餐查询行中的库存模式固化为订单快照标识。 */
    private boolean isSelectableDevice(Map<String, Object> item, Integer itemRole)
    {
        if (!Integer.valueOf(1).equals(itemRole) && !Integer.valueOf(4).equals(itemRole))
        {
            return false;
        }
        Object selectableDevice = item == null ? null : item.get("selectableDevice");
        if (selectableDevice != null)
        {
            if (selectableDevice instanceof Boolean)
            {
                return ((Boolean) selectableDevice).booleanValue();
            }
            return isSerialStockMode(selectableDevice);
        }
        Object stockMode = item == null ? null : item.get("stockMode");
        return isSerialStockMode(stockMode);
    }

    /** 统一识别数据库数字编码和历史快照中的文本库存模式。 */
    private boolean isSerialStockMode(Object value)
    {
        if (value == null)
        {
            return false;
        }
        String text = String.valueOf(value).trim();
        return String.valueOf(RentalCode.StockMode.SERIAL.getCode()).equals(text)
                || "sn".equalsIgnoreCase(text) || "serial".equalsIgnoreCase(text)
                || "逐件".equals(text) || "逐件库存".equals(text);
    }

    private List<Long> jsonLongList(JsonNode node, String... fieldNames)
    {
        List<Long> values = new ArrayList<Long>();
        if (node == null || !node.isObject())
        {
            return values;
        }
        for (String fieldName : fieldNames)
        {
            JsonNode list = node.path(fieldName);
            if (!list.isArray())
            {
                continue;
            }
            for (JsonNode value : list)
            {
                Long parsed = longValue(value);
                if (parsed != null)
                {
                    values.add(parsed);
                }
            }
            if (!values.isEmpty())
            {
                break;
            }
        }
        return values;
    }

    private List<String> jsonTextList(JsonNode node, String... fieldNames)
    {
        List<String> values = new ArrayList<String>();
        if (node == null || !node.isObject())
        {
            return values;
        }
        for (String fieldName : fieldNames)
        {
            JsonNode list = node.path(fieldName);
            if (!list.isArray())
            {
                continue;
            }
            for (JsonNode value : list)
            {
                if (!value.isNull() && !isBlank(value.asText()))
                {
                    values.add(value.asText());
                }
            }
            if (!values.isEmpty())
            {
                break;
            }
        }
        return values;
    }

    private Long longValue(JsonNode node)
    {
        if (node == null || node.isNull())
        {
            return null;
        }
        try
        {
            return Long.valueOf(node.asText());
        }
        catch (NumberFormatException exception)
        {
            return null;
        }
    }

    private Long longValue(JsonNode node, String... fieldNames)
    {
        String value = textAny(node, fieldNames);
        if (isBlank(value))
        {
            return null;
        }
        try
        {
            return Long.valueOf(value);
        }
        catch (NumberFormatException exception)
        {
            return null;
        }
    }

    private static final class PackageDetails
    {
        private String packageName;
        private String packageCode;
        private String productCode;
        private String productName;
        private String modelName;
        private final List<String> itemNames = new ArrayList<String>();
        private final List<PackageItemDetail> itemDetails = new ArrayList<PackageItemDetail>();
        private final List<String> extraAccessoryNames = new ArrayList<String>();
    }

    private static final class PackageItemDetail
    {
        private Integer itemIndex;
        private String itemType;
        private String name;
        private String code;
        private String model;
        private Integer quantity;
        private String requiredText;
        private boolean selectableDevice;
        private List<Long> unitIds = new ArrayList<Long>();
        private List<String> serialNumbers = new ArrayList<String>();
    }

    /** 联盟子订单只允许修改套餐，其他字段必须保持复制时的值。 */
    private boolean sameAllianceOrderFields(MerchantOrderCreateCommand before, MerchantOrderCreateCommand command)
    {
        return java.util.Objects.equals(before.channelId, command.channelId)
                && java.util.Objects.equals(before.externalOrderNo, command.externalOrderNo)
                && java.util.Objects.equals(before.shopName, command.shopName)
                && java.util.Objects.equals(before.customerName, command.customerName)
                && java.util.Objects.equals(before.customerNickname, command.customerNickname)
                && java.util.Objects.equals(before.customerPhone, command.customerPhone)
                && java.util.Objects.equals(before.recipientName, command.recipientName)
                && java.util.Objects.equals(before.recipientPhone, command.recipientPhone)
                && java.util.Objects.equals(before.recipientAddress, command.recipientAddress)
                && java.util.Objects.equals(before.warehouseId, command.warehouseId)
                && java.util.Objects.equals(before.returnWarehouseId, command.returnWarehouseId)
                && java.util.Objects.equals(before.leaseStartDate, command.leaseStartDate)
                && java.util.Objects.equals(before.leaseEndDate, command.leaseEndDate)
                && java.util.Objects.equals(before.shipTimeSlot, command.shipTimeSlot)
                && java.util.Objects.equals(before.deliveryMethod, command.deliveryMethod)
                && java.util.Objects.equals(before.returnDeliveryMethod, command.returnDeliveryMethod)
                && java.util.Objects.equals(before.deliveryAmount, command.deliveryAmount)
                && java.util.Objects.equals(before.insuranceAmount, command.insuranceAmount)
                && java.util.Objects.equals(before.rentAmount, command.rentAmount)
                && java.util.Objects.equals(before.depositAmount, command.depositAmount)
                && java.util.Objects.equals(before.payStatus, command.payStatus)
                && java.util.Objects.equals(before.timeEffectStatus, command.timeEffectStatus)
                && java.util.Objects.equals(before.weatherRiskFlag, command.weatherRiskFlag)
                && java.util.Objects.equals(before.priorityFlag, command.priorityFlag)
                && java.util.Objects.equals(before.remark, command.remark);
    }

    /**
     * 将商户用户唯一编号转换为列表展示名称，避免把数据库 ID 直接暴露到页面。
     * 用户已被删除或昵称服务暂时不可用时使用明确的降级文本，不回退为数字 ID。
     *
     * @param userId 商户用户唯一编号
     * @param operatorNames 批量解析的用户名称
     * @param nullDisplayName 唯一编号为空时的展示文本
     * @return 用户昵称或稳定的降级文本
     */
    private String resolveDisplayName(Long userId, Map<Long, String> operatorNames, String nullDisplayName)
    {
        if (!isPositiveUserId(userId))
        {
            return nullDisplayName;
        }
        String displayName = operatorNames.get(userId);
        return isBlank(displayName) ? "未知用户" : displayName;
    }

    /** 商户用户主键从 1 开始，0 或负数只表示历史系统占位值，不能拿去关联用户表。 */
    private boolean isPositiveUserId(Long userId)
    {
        return userId != null && userId.longValue() > 0L;
    }

    /**
     * 转换客户信息，并对缺失的快照字段提供稳定的展示值。
     *
     * @param row 数据库查询行
     * @return 客户展示对象
     */
    private MerchantOrderListVO.Customer toCustomer(MerchantOrderRow row)
    {
        MerchantOrderListVO.Customer customer = new MerchantOrderListVO.Customer();
        customer.nickname = defaultText(row.customerNickname, row.customerName);
        customer.name = defaultText(row.customerName, "未填写");
        customer.phone = defaultText(row.customerPhone, "未填写");
        customer.address = defaultText(row.recipientAddress, "未填写收货地址");
        // 编辑收件人时会复用该字段，不能把列表展示用的占位文案写入业务数据。
        customer.note = row.customerNote;
        return customer;
    }

    /**
     * 转换发货安排信息，实际可选配送方式由后续订单状态接口补充。
     *
     * @param row 数据库查询行
     * @return 发货安排展示对象
     */
    private MerchantOrderListVO.Shipping toShipping(MerchantOrderRow row)
    {
        MerchantOrderListVO.Shipping shipping = new MerchantOrderListVO.Shipping();
        shipping.warehouse = defaultText(row.warehouseName, "未分配仓库");
        shipping.shipDate = formatDate(row.shipDueTime == null ? row.createTime : row.shipDueTime);
        shipping.method = deliveryMethodLabel(row.deliveryMethod);
        shipping.timeSlot = row.shipTimeSlot;
        shipping.doorTime = row.shipDueTime == null ? "待安排" : formatDateTime(row.shipDueTime);
        shipping.note = defaultText(row.customerNote, "暂无发货备注");
        return shipping;
    }

    /**
     * 转换气象风险信息。订单列表只读取已经落库的今天和明天天气预报，不在同步请求中访问
     * 高德接口；这样同一页订单不会因天气网络请求变慢或失败而影响列表响应。
     *
     * @param row 数据库查询行
     * @param weatherSnapshots 旧环境天气缓存，天气表未注入时供测试和兼容场景使用
     * @param regionWeatherSnapshots 当前列表已批量读取的天气预报
     * @return 气象风险展示对象
     */
    private MerchantOrderListVO.Weather toWeather(MerchantOrderRow row,
            Map<String, AmapWeatherSnapshot> weatherSnapshots,
            Map<String, RegionWeatherRecord> regionWeatherSnapshots)
    {
        MerchantOrderListVO.Weather weather = new MerchantOrderListVO.Weather();
        RegionAddressModel departureRegion = regionAddressParser == null ? null
                : regionAddressParser.parse(row.warehouseCity);
        RegionAddressModel arrivalRegion = recipientRegion(row);
        String departureAdcode = weatherAdcode(departureRegion);
        String arrivalAdcode = weatherAdcode(arrivalRegion);
        String departureCity = defaultText(departureRegion == null ? null : departureRegion.cityName,
                defaultText(row.warehouseCity, "发货地"));
        String arrivalCity = defaultText(row.recipientCityName,
                defaultText(arrivalRegion == null ? null : arrivalRegion.cityName, "收货地"));
        weather.route = departureCity + " → " + arrivalCity;

        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        LocalDate tomorrow = today.plusDays(1);
        if (regionWeatherMapper != null)
        {
            RegionWeatherRecord departureToday = regionWeather(departureAdcode, today,
                    regionWeatherSnapshots);
            RegionWeatherRecord arrivalToday = regionWeather(arrivalAdcode, today,
                    regionWeatherSnapshots);
            RegionWeatherRecord departureTomorrow = regionWeather(departureAdcode, tomorrow,
                    regionWeatherSnapshots);
            RegionWeatherRecord arrivalTomorrow = regionWeather(arrivalAdcode, tomorrow,
                    regionWeatherSnapshots);
            weather.today = toForecast(departureToday, arrivalToday);
            weather.tomorrow = toForecast(departureTomorrow, arrivalTomorrow);
            weather.risk = RentalCode.YesNo.YES.getCode().equals(row.weatherRiskFlag)
                    || weatherRisk(departureToday) || weatherRisk(arrivalToday)
                    || weatherRisk(departureTomorrow) || weatherRisk(arrivalTomorrow);
        }
        else
        {
            // 仅保留给未注入新天气 Mapper 的旧单元测试，生产 Spring Bean 始终走落库预报。
            AmapWeatherSnapshot departureWeather = cachedWeather(departureAdcode, weatherSnapshots);
            AmapWeatherSnapshot arrivalWeather = cachedWeather(arrivalAdcode, weatherSnapshots);
            weather.today = toCachedForecast(departureWeather, arrivalWeather);
            weather.tomorrow = toCachedForecast(null, null);
            weather.risk = RentalCode.YesNo.YES.getCode().equals(row.weatherRiskFlag)
                    || departureWeather.risk || arrivalWeather.risk;
        }
        return weather;
    }

    /** 从批量天气结果中读取指定行政区和日期的记录。 */
    private RegionWeatherRecord regionWeather(String regionCode, LocalDate weatherDate,
            Map<String, RegionWeatherRecord> regionWeatherSnapshots)
    {
        if (isBlank(regionCode) || regionWeatherSnapshots == null)
        {
            return null;
        }
        return regionWeatherSnapshots.get(weatherKey(regionCode, weatherDate));
    }

    /** 将两地同一天的天气记录转换为前端日预报节点。 */
    private MerchantOrderListVO.DayForecast toForecast(RegionWeatherRecord departure,
            RegionWeatherRecord arrival)
    {
        MerchantOrderListVO.DayForecast forecast = new MerchantOrderListVO.DayForecast();
        forecast.depart = weatherText(departure);
        forecast.arrive = weatherText(arrival);
        forecast.departIcon = weatherIcon(departure);
        forecast.arriveIcon = weatherIcon(arrival);
        return forecast;
    }

    /** 将旧高德缓存转换为日预报节点，仅用于兼容未注入天气 Mapper 的测试场景。 */
    private MerchantOrderListVO.DayForecast toCachedForecast(AmapWeatherSnapshot departure,
            AmapWeatherSnapshot arrival)
    {
        MerchantOrderListVO.DayForecast forecast = new MerchantOrderListVO.DayForecast();
        forecast.depart = weatherText(departure);
        forecast.arrive = weatherText(arrival);
        forecast.departIcon = departure == null ? "cloudy" : defaultText(departure.icon, "cloudy");
        forecast.arriveIcon = arrival == null ? "cloudy" : defaultText(arrival.icon, "cloudy");
        return forecast;
    }

    /**
     * 在当前列表请求内复用同一行政区划的缓存结果，避免重复访问 Redis；绝不触发天气网络查询。
     *
     * @param adcode 市级行政区划 adcode
     * @param weatherSnapshots 当前列表天气快照缓存
     * @return 天气快照或不可用结果
     */
    private AmapWeatherSnapshot cachedWeather(String adcode,
            Map<String, AmapWeatherSnapshot> weatherSnapshots)
    {
        if (amapWeatherClient == null)
        {
            return AmapWeatherSnapshot.unavailable(adcode, "天气服务未启用");
        }
        if (adcode == null || adcode.trim().isEmpty())
        {
            return amapWeatherClient.queryCached(adcode);
        }
        String normalizedAdcode = adcode.trim();
        if (weatherSnapshots.containsKey(normalizedAdcode))
        {
            return weatherSnapshots.get(normalizedAdcode);
        }
        AmapWeatherSnapshot snapshot = amapWeatherClient.queryCached(normalizedAdcode);
        weatherSnapshots.put(normalizedAdcode, snapshot);
        return snapshot;
    }

    /**
     * 取得订单收件地址的行政区划结果，历史快照缺失时兼容解析当前订单地址。
     *
     * @param row 订单查询行
     * @return 收件地址解析结果
     */
    private RegionAddressModel recipientRegion(MerchantOrderRow row)
    {
        RegionAddressModel result = new RegionAddressModel();
        result.provinceCode = row.recipientProvinceCode;
        result.cityCode = row.recipientCityCode;
        result.districtCode = row.recipientDistrictCode;
        result.amapCityCode = row.recipientAmapCityCode;
        result.provinceName = row.recipientProvinceName;
        result.cityName = row.recipientCityName;
        result.districtName = row.recipientDistrictName;
        if (result.weatherAdcode() == null && regionAddressParser != null)
        {
            return regionAddressParser.parse(row.recipientAddress);
        }
        return result;
    }

    /**
     * 取得天气服务应使用的行政区划 adcode，禁止把 citycode 误作为天气接口参数。
     *
     * @param region 地址解析结果
     * @return 区县、市或省级 adcode；解析结果为空时返回 null
     */
    private String weatherAdcode(RegionAddressModel region)
    {
        return region == null ? null : region.weatherAdcode();
    }

    /** 将已落库的天气记录转换为简短天气文字；失败记录统一显示待查询。 */
    private String weatherText(RegionWeatherRecord record)
    {
        if (record == null || !RentalCode.WeatherQueryStatus.SUCCESS.getCode().equals(record.queryStatus))
        {
            return "-";
        }
        String weather = defaultText(record.dayWeather, record.nightWeather);
        return isBlank(weather) ? "-" : weather;
    }

    /** 读取天气记录图标，缺少图标时使用稳定的阴天天气图标。 */
    private String weatherIcon(RegionWeatherRecord record)
    {
        return record == null || !RentalCode.WeatherQueryStatus.SUCCESS.getCode().equals(record.queryStatus)
                ? "cloudy" : defaultText(record.weatherIcon, "cloudy");
    }

    /** 风险标记只对查询成功的天气记录生效，避免失败状态沿用默认值误报。 */
    private boolean weatherRisk(RegionWeatherRecord record)
    {
        return record != null
                && RentalCode.WeatherQueryStatus.SUCCESS.getCode().equals(record.queryStatus)
                && RentalCode.YesNo.YES.getCode().equals(record.riskFlag);
    }

    /**
     * 将高德天气快照转换为订单列表中的简短天气文本。
     *
     * @param snapshot 天气快照
     * @return 天气展示文本
     */
    private String weatherText(AmapWeatherSnapshot snapshot)
    {
        if (snapshot == null || !snapshot.available || snapshot.weather == null || snapshot.weather.trim().isEmpty())
        {
            return "待查询";
        }
        return snapshot.weather.trim();
    }

    /**
     * 在订单主表保存成功后记录收件人历史快照。
     *
     * @param command 已保存订单命令
     * @param operatorId 操作人用户唯一编号
     */
    private void recordRecipientSnapshot(MerchantOrderCreateCommand command, Long operatorId)
    {
        recordRecipientSnapshot(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), command,
                operatorId);
    }

    private void recordRecipientSnapshot(Long tenantId, Long memberId, MerchantOrderCreateCommand command,
            Long operatorId)
    {
        if (recipientHistoryService != null && command != null)
        {
            recipientHistoryService.record(tenantId, memberId, command.orderId, command.orderNo, command.customerName,
                    command.customerPhone,
                    command.customerNickname, command.recipientName, command.recipientPhone,
                    command.recipientAddress, command.remark, operatorId);
        }
    }

    /**
     * 转换物流配送方案，未创建物流单时返回空方案而不是虚构物流信息。
     *
     * @param row 数据库查询行
     * @return 配送方案展示对象
     */
    private MerchantOrderListVO.Delivery toDelivery(MerchantOrderRow row)
    {
        MerchantOrderListVO.Delivery delivery = new MerchantOrderListVO.Delivery();
        if (row.shippingCarrier != null && !row.shippingCarrier.trim().isEmpty())
        {
            MerchantOrderListVO.DeliveryPlan plan = new MerchantOrderListVO.DeliveryPlan();
            plan.carrier = row.shippingCarrier;
            plan.name = "已创建物流单";
            plan.price = row.shippingFee == null ? BigDecimal.ZERO : row.shippingFee;
            delivery.plans = Collections.singletonList(plan);
        }
        else
        {
            delivery.plans = Collections.emptyList();
        }
        delivery.expectedDate = formatDate(row.arrivalDate == null
                ? calculateExpectedArriveTime(row) : row.arrivalDate);
        return delivery;
    }

    /**
     * 按订单租期计算归还设备的预计到库日期。
     *
     * <p>预计到库时间 = 应归还时间 + (租赁开始日期 - 应发货日期的日历天数差)。
     * 采用业务时区的 LocalDate 计算日期差，避免直接用毫秒差在跨日或夏令时环境下产生偏差，
     * 同时保留应归还时间的时分秒。</p>
     *
     * @param row 订单查询行
     * @return 预计到库时间，必要时间缺失时返回 null
     */
    private Date calculateExpectedArriveTime(MerchantOrderRow row)
    {
        if (row.returnDueTime == null || row.leaseStartDate == null || row.shipDueTime == null)
        {
            return null;
        }
        LocalDate leaseStartDate = toBusinessDate(row.leaseStartDate);
        LocalDate shipDueDate = toBusinessDate(row.shipDueTime);
        long dayDifference = ChronoUnit.DAYS.between(shipDueDate, leaseStartDate);
        ZonedDateTime expectedArrive = row.returnDueTime.toInstant()
                .atZone(BUSINESS_ZONE)
                .plusDays(dayDifference);
        return Date.from(expectedArrive.toInstant());
    }

    /** 将数据库 Date 转为业务时区日期，参与预计到库日历天数计算。 */
    private LocalDate toBusinessDate(Date date)
    {
        return date.toInstant().atZone(BUSINESS_ZONE).toLocalDate();
    }

    /**
     * 将数据库中的增购商品拼接值拆分为前端商品清单数组。
     *
     * @param addonNames 数据库 GROUP_CONCAT 结果
     * @return 去除空白项后的增购商品名称集合
     */
    private List<String> splitAddOns(String addonNames)
    {
        if (addonNames == null || addonNames.trim().isEmpty())
        {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<String>();
        for (String item : Arrays.asList(addonNames.split("、")))
        {
            if (!item.trim().isEmpty())
            {
                result.add(item.trim());
            }
        }
        return result;
    }

    /** 合并订单历史增购项与套餐快照中的额外配件，避免列表回查商品目录。 */
    private List<String> mergeAddOns(List<String> addonNames, List<String> extraAccessoryNames)
    {
        LinkedHashSet<String> values = new LinkedHashSet<String>();
        if (addonNames != null)
        {
            values.addAll(addonNames);
        }
        if (extraAccessoryNames != null)
        {
            values.addAll(extraAccessoryNames);
        }
        return new ArrayList<String>(values);
    }

    /** 额外配件只允许与已选择的套餐一起提交。 */
    private void validateExtraAccessories(MerchantOrderCreateCommand command)
    {
        if (!isBlank(command.accessoryItemIds) && command.packageId == null)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("额外配件必须随产品套餐选择");
        }
    }

    /**
     * 按项目统一时区格式化日期字段。
     *
     * @param date 日期对象
     * @return yyyy-MM-dd 格式日期或空字符串
     */
    private String formatDate(Date date)
    {
        return date == null ? "" : DateUtils.parseDateToStr(DATE_PATTERN, date);
    }

    /**
     * 按项目统一时区格式化日期时间字段。
     *
     * @param date 日期对象
     * @return yyyy-MM-dd HH:mm:ss 格式日期时间或空字符串
     */
    private String formatDateTime(Date date)
    {
        return date == null ? "" : DateUtils.parseDateToStr(DATETIME_PATTERN, date);
    }

    /**
     * 为页面字段提供非空展示文本。
     *
     * @param value 原始文本
     * @param fallback 缺失时的默认文本
     * @return 原始文本或默认文本
     */
    private String defaultText(String value, String fallback)
    {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    /** 截断外部文本，避免上传文件元数据超过数据库字段长度。 */
    private String trimToLength(String value, int maxLength)
    {
        if (value == null)
        {
            return null;
        }
        String normalized = value.trim();
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength);
    }

    /**
     * 构造订单审计的商户展示快照。
     *
     * <p>订单原始快照仍保留内部编号用于追溯；展示快照改用渠道和仓库名称，并由通用处理器再次
     * 清理所有未显式解析的技术编号字段。</p>
     */
    private Object orderAuditDisplay(Object value, Map<Long, String> warehouseNames,
            Map<Long, String> channelNames)
    {
        if (!(value instanceof MerchantOrderCreateCommand command))
        {
            return value;
        }
        try
        {
            ObjectMapper mapper = objectMapper == null ? new ObjectMapper() : objectMapper;
            Map<String, Object> snapshot = mapper.convertValue(command,
                    new TypeReference<LinkedHashMap<String, Object>>() { });
            snapshot.keySet().removeIf(MerchantAuditDisplaySnapshot::isTechnicalIdentifier);
            if (command.channelId != null)
            {
                snapshot.put("channelName", displayReference(channelNames, command.channelId, "已删除渠道"));
            }
            if (command.warehouseId != null)
            {
                snapshot.put("warehouseName", displayReference(warehouseNames, command.warehouseId, "已删除仓库"));
            }
            if (command.returnWarehouseId != null)
            {
                snapshot.put("returnWarehouseName",
                        displayReference(warehouseNames, command.returnWarehouseId, "已删除仓库"));
            }
            return snapshot;
        }
        catch (IllegalArgumentException exception)
        {
            log.warn("订单审计展示快照构造失败，使用安全空快照：原因={}", exception.getMessage());
            return Collections.emptyMap();
        }
    }

    /** 收集订单审计中需要解析名称的仓库唯一编号。 */
    private Set<Long> orderWarehouseIds(Object before, Object after)
    {
        Set<Long> ids = new LinkedHashSet<Long>();
        addOrderWarehouseIds(ids, before);
        addOrderWarehouseIds(ids, after);
        return ids;
    }

    private void addOrderWarehouseIds(Set<Long> ids, Object value)
    {
        if (value instanceof MerchantOrderCreateCommand command)
        {
            addReferenceId(ids, command.warehouseId);
            addReferenceId(ids, command.returnWarehouseId);
        }
    }

    /** 收集订单审计中需要解析名称的订单渠道唯一编号。 */
    private Set<Long> orderChannelIds(Object before, Object after)
    {
        Set<Long> ids = new LinkedHashSet<Long>();
        addOrderChannelId(ids, before);
        addOrderChannelId(ids, after);
        return ids;
    }

    private void addOrderChannelId(Set<Long> ids, Object value)
    {
        if (value instanceof MerchantOrderCreateCommand command)
        {
            addReferenceId(ids, command.channelId);
        }
    }

    /**
     * 写入订单业务审计；单元测试未提供记录器时跳过审计，不改变订单主流程。
     *
     * @param module 业务模块
     * @param operationType 操作类型
     * @param targetType 业务对象类型
     * @param targetId 订单唯一编号
     * @param targetName 订单编号
     * @param description 业务描述
     * @param before 变更前订单
     * @param after 变更后订单
     */
    private void audit(String module, String operationType, String targetType, Long targetId,
            String targetName, String description, Object before, Object after)
    {
        if (auditRecorder != null)
        {
            Map<Long, String> warehouseNames = resolveReferenceNames(orderWarehouseIds(before, after), true);
            Map<Long, String> channelNames = resolveReferenceNames(orderChannelIds(before, after), false);
            auditRecorder.record(module, operationType, targetType, String.valueOf(targetId), targetName,
                    description, before, after, orderAuditDisplay(before, warehouseNames, channelNames),
                    orderAuditDisplay(after, warehouseNames, channelNames));
        }
    }

}
