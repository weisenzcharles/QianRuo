package com.ruoyi.rentalbusiness.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.merchant.api.RemoteMerchantAllianceService;
import com.ruoyi.merchant.api.model.MerchantAllianceOption;
import com.ruoyi.merchant.api.model.MerchantAllianceOptionQuery;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.rentalbusiness.enums.MerchantInventoryConditionLevel;
import com.ruoyi.rentalbusiness.enums.MerchantInventoryTransactionType;
import com.ruoyi.rentalbusiness.mapper.MerchantInventoryMapper;
import com.ruoyi.rentalbusiness.model.MerchantInventoryBatchCommand;
import com.ruoyi.rentalbusiness.model.MerchantInventoryUnitTransferCommand;
import com.ruoyi.rentalbusiness.model.MerchantInventoryUnitUpdateCommand;
import com.ruoyi.rentalbusiness.model.MerchantInboundOutboundTrendRowVO;
import com.ruoyi.rentalbusiness.model.MerchantInboundOutboundTrendVO;
import com.ruoyi.rentalbusiness.model.MerchantInboundOutboundOrderQuery;
import com.ruoyi.rentalbusiness.model.MerchantInboundOutboundOrderVO;

/** 商户新架构库存服务，支持逐件序列号和数量库存。 */
@Service
public class MerchantInventoryService
{
    private static final int MAX_BATCH_SIZE = 5000;
    private static final List<String> EXCLUDED_FORECAST_ORDER_STATUSES = List.of(
            String.valueOf(RentalCode.OrderStatus.COMPLETED.getCode()), "completed",
            String.valueOf(RentalCode.OrderStatus.CANCELLED.getCode()), "cancelled");
    private final MerchantInventoryMapper mapper;
    private final RemoteMerchantAllianceService remoteAllianceService;
    private final CodeGeneratorClient codeGeneratorClient;

    @org.springframework.beans.factory.annotation.Autowired
    public MerchantInventoryService(MerchantInventoryMapper mapper, RemoteMerchantAllianceService remoteAllianceService,
            CodeGeneratorClient codeGeneratorClient)
    {
        this.mapper = mapper;
        this.remoteAllianceService = remoteAllianceService;
        this.codeGeneratorClient = codeGeneratorClient;
    }

    /** 保留旧构造方式，供不涉及编码分配的单元测试使用。 */
    public MerchantInventoryService(MerchantInventoryMapper mapper, RemoteMerchantAllianceService remoteAllianceService)
    {
        this(mapper, remoteAllianceService, null);
    }

    /** 查询联盟成员已明确开启共享的库存，并将所有者转换为商户名称供页面展示。 */
    public List<Map<String, Object>> shared(String keyword, Long warehouseId, Integer productType)
    {
        MerchantContextUtils.requireMerchantApproved();
        R<List<MerchantAllianceOption>> response = remoteAllianceService.listSharedMembers(
                new MerchantAllianceOptionQuery(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId()),
                SecurityConstants.INNER, Constants.CLIENT_TYPE_MERCHANT);
        List<Long> memberIds = new ArrayList<>();
        Long allianceId = null;
        if (response != null && response.getData() != null)
            for (MerchantAllianceOption option : response.getData())
                if (option != null && option.getMemberId() != null) {
                    memberIds.add(option.getMemberId());
                    if (allianceId == null) allianceId = option.getAllianceId();
                }
        if (memberIds.isEmpty()) return new ArrayList<>();
        List<Map<String, Object>> inventory = mapper.selectSharedInventory(MerchantContextUtils.getTenantId(), allianceId, memberIds,
                trim(keyword), warehouseId, productType);
        Map<Long, String> merchantNames = new java.util.HashMap<>();
        if (response != null && response.getData() != null)
            for (MerchantAllianceOption option : response.getData())
                if (option != null && option.getMemberId() != null)
                    merchantNames.put(option.getMemberId(), option.getMerchantName());
        for (Map<String, Object> row : inventory)
            row.put("ownerMerchantName", merchantNames.getOrDefault(row.get("ownerMemberId"), "未知商户"));
        return inventory;
    }

    /** 查询库存汇总。 */
    public List<Map<String, Object>> list(String keyword, Long warehouseId, Integer unitStatus,
            Long classId, Integer productType, String brand, Long excludeWarehouseId)
    {
        MerchantContextUtils.requireMerchantApproved();
        checkWarehouse(warehouseId);
        checkWarehouse(excludeWarehouseId);
        return mapper.selectInventory(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                trim(keyword), warehouseId, unitStatus, classId, productType, trim(brand), excludeWarehouseId,
                MerchantContextUtils.getWarehouseIds(), MerchantContextUtils.hasAllWarehouseAccess());
    }

    /** 返回近 30 天库存变化和当前可用库存概况。 */
    public List<Map<String, Object>> overview(String mode, Long warehouseId, String brand, Long modelId, Integer days)
    {
        MerchantContextUtils.requireMerchantApproved();
        int displayDays = days == null ? 30 : Math.max(1, Math.min(days, 30));
        if ("package".equalsIgnoreCase(mode))
        {
            return mapper.selectPackageOverview(MerchantContextUtils.getTenantId(),
                    MerchantContextUtils.getMemberId(), warehouseId, displayDays,
                    EXCLUDED_FORECAST_ORDER_STATUSES);
        }
        return mapper.selectModelOverview(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), warehouseId, trim(brand), modelId, displayDays,
                EXCLUDED_FORECAST_ORDER_STATUSES);
    }

    /** 查询未来指定天数内的预计发货、归还数量及当前待发货订单数。 */
    public MerchantInboundOutboundTrendVO inboundOutboundTrend(String mode, Long warehouseId, String brand,
            Long modelId, Integer days, boolean showAll)
    {
        MerchantContextUtils.requireMerchantApproved();
        String normalizedMode = mode == null ? "model" : mode.trim().toLowerCase(Locale.ROOT);
        if (!"model".equals(normalizedMode) && !"package".equals(normalizedMode))
        {
            throw new ServiceException("收发趋势类型不合法");
        }
        checkWarehouse(warehouseId);
        int displayDays = days == null ? 30 : Math.max(1, Math.min(days, 30));
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        Set<Long> warehouseIds = MerchantContextUtils.getWarehouseIds();
        boolean allWarehouseAccess = MerchantContextUtils.hasAllWarehouseAccess();
        List<String> shipmentStatusValues = statusValues(RentalCode.OrderStatus.PENDING_SHIP,
                RentalCode.OrderStatus.PREPARED);
        List<String> returnStatusValues = statusValues(RentalCode.OrderStatus.SHIPPING,
                RentalCode.OrderStatus.SHIP_IN_TRANSIT, RentalCode.OrderStatus.SHIPPED,
                RentalCode.OrderStatus.RENTING, RentalCode.OrderStatus.RETURNING,
                RentalCode.OrderStatus.OVERDUE_RETURN,
                RentalCode.OrderStatus.OVERDUE);

        MerchantInboundOutboundTrendVO result = new MerchantInboundOutboundTrendVO();
        Long pendingShipTotal = mapper.countPendingShipOrders(tenantId, memberId, normalizedMode, warehouseId,
                trim(brand), modelId, shipmentStatusValues, warehouseIds, allWarehouseAccess);
        result.pendingShipTotal = pendingShipTotal == null ? 0L : pendingShipTotal;
        result.rows = mapper.selectInboundOutboundTrend(tenantId, memberId, normalizedMode, warehouseId,
                trim(brand), modelId, displayDays, shipmentStatusValues, returnStatusValues,
                warehouseIds, allWarehouseAccess, showAll);
        return result;
    }

    /** 查询收发趋势某个数字对应的订单明细，并复用趋势统计的状态和仓库范围。 */
    public List<MerchantInboundOutboundOrderVO> inboundOutboundOrders(String mode, Long warehouseId, Long itemId,
            String trendDate, String direction, String source)
    {
        MerchantContextUtils.requireMerchantApproved();
        MerchantInboundOutboundOrderQuery.Mode normalizedMode = MerchantInboundOutboundOrderQuery.Mode.fromCode(mode);
        MerchantInboundOutboundOrderQuery.Direction normalizedDirection =
                MerchantInboundOutboundOrderQuery.Direction.fromCode(direction);
        MerchantInboundOutboundOrderQuery.Source normalizedSource =
                MerchantInboundOutboundOrderQuery.Source.fromCode(source);
        if (normalizedMode == null || normalizedDirection == null || normalizedSource == null || itemId == null)
        {
            throw new ServiceException("收发趋势订单查询条件不合法");
        }
        LocalDate normalizedDate;
        try
        {
            normalizedDate = LocalDate.parse(trendDate == null ? "" : trendDate.trim());
        }
        catch (DateTimeParseException exception)
        {
            throw new ServiceException("收发趋势日期格式不正确");
        }
        checkWarehouse(warehouseId);
        List<String> shipmentStatusValues = statusValues(RentalCode.OrderStatus.PENDING_SHIP,
                RentalCode.OrderStatus.PREPARED);
        List<String> returnStatusValues = statusValues(RentalCode.OrderStatus.SHIPPING,
                RentalCode.OrderStatus.SHIP_IN_TRANSIT, RentalCode.OrderStatus.SHIPPED,
                RentalCode.OrderStatus.RENTING, RentalCode.OrderStatus.RETURNING,
                RentalCode.OrderStatus.OVERDUE_RETURN, RentalCode.OrderStatus.OVERDUE);
        return mapper.selectInboundOutboundOrders(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), normalizedMode.getCode(), warehouseId, itemId,
                normalizedDate.toString(), normalizedDirection.getCode(), normalizedSource.getCode(),
                shipmentStatusValues, returnStatusValues, MerchantContextUtils.getWarehouseIds(),
                MerchantContextUtils.hasAllWarehouseAccess());
    }

    private List<String> statusValues(RentalCode.OrderStatus... statuses)
    {
        List<String> values = new ArrayList<String>();
        for (RentalCode.OrderStatus status : statuses)
        {
            values.add(String.valueOf(status.getCode()));
            values.add(status.name().toLowerCase(Locale.ROOT));
        }
        return values;
    }

    /** 查询当前商户已配置的设备和配件商品，供库存入库选择。 */
    public List<Map<String, Object>> productOptions(Long classId, Integer productType, String brand, String keyword)
    {
        MerchantContextUtils.requireMerchantApproved();
        return mapper.selectProductOptions(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                classId, productType, trim(brand), trim(keyword));
    }

    /** 查询库存序列号明细。 */
    public List<Map<String, Object>> units(Long inventoryId, Long productId, Long warehouseId,
            String keyword, Integer unitStatus, Boolean transferableOnly)
    {
        MerchantContextUtils.requireMerchantApproved();
        checkWarehouse(warehouseId);
        return mapper.selectUnits(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), inventoryId,
                productId, warehouseId, trim(keyword), unitStatus, Boolean.TRUE.equals(transferableOnly));
    }

    /** 查询数量库存当前仍在预占中的订单。 */
    public List<Map<String, Object>> reservationOrders(Long inventoryId)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (inventoryId == null)
        {
            throw new ServiceException("库存不能为空");
        }
        return mapper.selectQuantityReservationOrders(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), inventoryId);
    }

    /** 修改当前商户库存单元的可编辑档案，并在状态变化后刷新库存汇总。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUnit(MerchantInventoryUnitUpdateCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null || command.unitId == null)
        {
            throw new ServiceException("库存单元不能为空");
        }
        String serialNumber = trim(command.serialNumber);
        if (serialNumber == null)
        {
            // 兼容旧版商户端在字段改名期间提交的 unitCode，数据库仍只更新序列号。
            serialNumber = trim(command.unitCode);
        }
        if (serialNumber == null)
        {
            throw new ServiceException("序列号不能为空");
        }
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        Map<String, Object> existing = mapper.selectUnitForUpdate(tenantId, memberId, command.unitId, null);
        if (existing == null)
        {
            throw new ServiceException("设备不存在或已删除");
        }
        MerchantContextUtils.checkWarehouseAccess(value(existing, "warehouseId"));
        if (mapper.countSerialNumber(memberId, serialNumber, command.unitId) > 0)
        {
            throw new ServiceException("序列号已存在");
        }
        Integer currentStatus = integerValue(existing, "unitStatus");
        Integer unitStatus = normalizeUnitStatus(command.unitStatus, currentStatus);
        if (!Objects.equals(currentStatus, unitStatus)
                && (isWorkflowStatus(currentStatus) || isWorkflowStatus(unitStatus)))
        {
            throw new ServiceException("已预占或租赁中的设备状态由订单流程管理，不能手动修改");
        }
        BigDecimal purchaseCost = nonNegative(command.purchaseCost, "购入价格不能小于零");
        BigDecimal insuranceAmount = nonNegative(command.insuranceAmount, "保险费用不能小于零");
        Integer conditionLevel = normalizeConditionLevel(command.conditionLevel);
        String insuranceUntil = normalizeDate(command.insuranceUntil);
        String serialPhotoUrl = trim(command.serialPhotoUrl);
        if (serialPhotoUrl != null && serialPhotoUrl.length() > 512)
        {
            throw new ServiceException("实物照片地址不能超过512个字符");
        }
        Long operator = SecurityUtils.getUserId();
        if (mapper.updateUnitDetails(tenantId, memberId, command.unitId, serialNumber, unitStatus,
                trim(command.memorySpec), purchaseCost, conditionLevel, insuranceUntil, insuranceAmount,
                serialPhotoUrl,
                trim(command.remark), operator) != 1)
        {
            throw new ServiceException("设备信息更新失败，请刷新后重试");
        }
        if (!Objects.equals(currentStatus, unitStatus))
        {
            mapper.refreshInventoryCounts(value(existing, "inventoryId"), operator);
        }
        return true;
    }

    /** 删除当前商户未关联任何订单的库存单元，删除采用软删除。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUnit(Long unitId)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (unitId == null)
        {
            throw new ServiceException("库存单元不能为空");
        }
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        Map<String, Object> existing = mapper.selectUnitForUpdate(tenantId, memberId, unitId, null);
        if (existing == null)
        {
            throw new ServiceException("设备不存在或已删除");
        }
        MerchantContextUtils.checkWarehouseAccess(value(existing, "warehouseId"));
        if (mapper.countUnitOrderReferences(tenantId, memberId, unitId) > 0)
        {
            throw new ServiceException("该设备已关联订单，不能删除");
        }
        Long operator = SecurityUtils.getUserId();
        if (mapper.softDeleteUnit(tenantId, memberId, unitId, operator) != 1)
        {
            throw new ServiceException("设备删除失败，请刷新后重试");
        }
        mapper.refreshInventoryCounts(value(existing, "inventoryId"), operator);
        return true;
    }

    /** 将可用且未关联订单的库存单元即时转移到目标仓库，并留下调货及库存流水。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean transferUnit(MerchantInventoryUnitTransferCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null || command.unitId == null || command.targetWarehouseId == null)
        {
            throw new ServiceException("库存单元和目标仓库不能为空");
        }
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        Long operator = SecurityUtils.getUserId();
        Map<String, Object> unit = mapper.selectUnitForUpdate(tenantId, memberId, command.unitId, null);
        if (unit == null)
        {
            throw new ServiceException("设备不存在或已删除");
        }
        Long sourceWarehouseId = value(unit, "warehouseId");
        Long sourceInventoryId = value(unit, "inventoryId");
        Long productId = value(unit, "productId");
        if (sourceWarehouseId == null || sourceInventoryId == null || productId == null)
        {
            throw new ServiceException("库存单元数据不完整，无法转仓");
        }
        if (sourceWarehouseId.equals(command.targetWarehouseId))
        {
            throw new ServiceException("目标仓库不能与当前仓库相同");
        }
        checkWarehouse(sourceWarehouseId);
        checkWarehouse(command.targetWarehouseId);
        if (!Long.valueOf(RentalCode.InventoryStatus.AVAILABLE.getCode().longValue()).equals(value(unit, "unitStatus")))
        {
            throw new ServiceException("仅可转移可用状态的设备");
        }
        if (mapper.countUnitOrderReferences(tenantId, memberId, command.unitId) > 0)
        {
            throw new ServiceException("该设备已关联订单，不能转仓");
        }

        // 目标汇总不存在时先创建；目标仓库尚无该商品库存，不会形成相反方向的库存锁竞争。
        Map<String, Object> targetScope = mapper.selectInventoryByScope(memberId, command.targetWarehouseId, productId);
        if (targetScope == null)
        {
            if (mapper.insertInventory(tenantId, memberId, command.targetWarehouseId, productId,
                    value(unit, "modelId"), operator) < 1)
            {
                throw new ServiceException("目标仓库库存初始化失败");
            }
            targetScope = mapper.selectInventoryByScope(memberId, command.targetWarehouseId, productId);
        }
        Long targetInventoryId = value(targetScope, "inventoryId");
        if (targetInventoryId == null)
        {
            throw new ServiceException("目标仓库库存不存在或已停用");
        }

        // 两个汇总行按自增编号顺序加锁，避免相反方向转仓形成死锁。
        Long firstWarehouseId = sourceInventoryId < targetInventoryId ? sourceWarehouseId : command.targetWarehouseId;
        Long secondWarehouseId = sourceInventoryId < targetInventoryId ? command.targetWarehouseId : sourceWarehouseId;
        Map<String, Object> firstInventory = mapper.selectInventoryForUpdate(tenantId, memberId, productId, firstWarehouseId);
        Map<String, Object> secondInventory = mapper.selectInventoryForUpdate(tenantId, memberId, productId, secondWarehouseId);
        Map<String, Object> sourceInventory = sourceWarehouseId.equals(firstWarehouseId) ? firstInventory : secondInventory;
        Map<String, Object> targetInventory = command.targetWarehouseId.equals(firstWarehouseId) ? firstInventory : secondInventory;
        if (sourceInventory == null || targetInventory == null)
        {
            throw new ServiceException("源仓库或目标仓库库存不可用");
        }
        if (!sourceInventoryId.equals(value(sourceInventory, "inventoryId"))
                || !targetInventoryId.equals(value(targetInventory, "inventoryId")))
        {
            throw new ServiceException("库存数据已变化，请刷新后重试");
        }
        Long sourceAvailable = value(sourceInventory, "availableQuantity");
        if (sourceAvailable == null || sourceAvailable < 1)
        {
            throw new ServiceException("源仓库没有可用库存");
        }
        if (!Long.valueOf(RentalCode.StockMode.SERIAL.getCode().longValue()).equals(value(targetInventory, "stockMode")))
        {
            throw new ServiceException("目标仓库库存模式不支持逐件转仓");
        }
        if (codeGeneratorClient == null)
        {
            throw new ServiceException("平台统一编码服务暂不可用");
        }

        String transferNo = codeGeneratorClient.next("transfer");
        if (mapper.updateUnitWarehouse(tenantId, memberId, command.unitId, sourceWarehouseId, sourceInventoryId,
                command.targetWarehouseId, targetInventoryId, operator) != 1)
        {
            throw new ServiceException("设备转仓失败，请刷新后重试");
        }
        mapper.refreshInventoryCounts(sourceInventoryId, operator);
        mapper.refreshInventoryCounts(targetInventoryId, operator);
        Map<String, Object> sourceAfter = mapper.selectInventoryForUpdate(tenantId, memberId, productId, sourceWarehouseId);
        Map<String, Object> targetAfter = mapper.selectInventoryForUpdate(tenantId, memberId, productId, command.targetWarehouseId);
        if (sourceAfter == null || targetAfter == null)
        {
            throw new ServiceException("转仓后库存汇总刷新失败");
        }
        if (mapper.insertTransferOrder(tenantId, memberId, transferNo, sourceWarehouseId,
                command.targetWarehouseId, command.unitId, RentalCode.TransferStatus.COMPLETED.getCode(), operator,
                "设备明细转仓") != 1)
        {
            throw new ServiceException("调货记录保存失败");
        }
        if (mapper.insertInventoryTransaction(tenantId, memberId, sourceInventoryId, sourceWarehouseId, productId,
                MerchantInventoryTransactionType.TRANSFER.getCode(), -1, sourceAvailable.intValue(),
                value(sourceAfter, "availableQuantity").intValue(), null, transferNo + ":source", "设备转仓出库", operator) != 1)
        {
            throw new ServiceException("源仓库库存流水保存失败");
        }
        if (mapper.insertInventoryTransaction(tenantId, memberId, targetInventoryId, command.targetWarehouseId, productId,
                MerchantInventoryTransactionType.TRANSFER.getCode(), 1, value(targetInventory, "availableQuantity").intValue(),
                value(targetAfter, "availableQuantity").intValue(), null, transferNo + ":target", "设备转仓入库", operator) != 1)
        {
            throw new ServiceException("目标仓库库存流水保存失败");
        }
        return true;
    }

    /** 批量录入序列号并刷新库存汇总。 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> addBatch(MerchantInventoryBatchCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null || command.productId == null || command.warehouseId == null)
        {
            throw new ServiceException("商品和仓库不能为空");
        }
        checkWarehouse(command.warehouseId);
        Map<String, Object> product = mapper.selectProduct(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), command.productId);
        if (product == null)
        {
            throw new ServiceException("商品不存在或不属于当前商户");
        }
        if (Integer.valueOf(2).equals(product.get("stockMode")))
        {
            return addQuantityBatch(command, product);
        }
        if (!Integer.valueOf(1).equals(product.get("stockMode")))
        {
            throw new ServiceException("当前阶段只支持逐件/SN库存商品");
        }
        List<String> serialNumbers = normalize(command.serialNumbers);
        if (serialNumbers.isEmpty() || serialNumbers.size() > MAX_BATCH_SIZE)
        {
            throw new ServiceException("序列号数量必须为 1-" + MAX_BATCH_SIZE + " 个");
        }
        String idempotencyKey = trim(command.idempotencyKey);
        if (idempotencyKey == null)
        {
            idempotencyKey = UUID.randomUUID().toString();
        }
        Map<String, Object> existingBatch = mapper.selectBatchByIdempotency(MerchantContextUtils.getMemberId(), idempotencyKey);
        if (existingBatch != null)
        {
            return existingBatch;
        }
        if (mapper.countSerialNumbers(MerchantContextUtils.getMemberId(), serialNumbers) > 0)
        {
            throw new ServiceException("提交的序列号中存在已入库序列号");
        }
        Long operator = SecurityUtils.getUserId();
        String batchNo = codeGeneratorClient.next("inventory_batch");
        mapper.insertBatch(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), command.warehouseId,
                command.productId, value(product, "modelId"), batchNo, serialNumbers.size(),
                idempotencyKey, operator, trim(command.remark));
        Long batchId = lastInsertId();
        mapper.insertInventory(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), command.warehouseId,
                command.productId, value(product, "modelId"), operator);
        Map<String, Object> inventory = mapper.selectInventoryByScope(MerchantContextUtils.getMemberId(), command.warehouseId,
                command.productId);
        Long inventoryId = value(inventory, "inventoryId");
        for (String serialNumber : serialNumbers)
        {
            String unitCode = codeGeneratorClient.next("inventory_unit");
            mapper.insertUnit(inventoryId, MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                    command.warehouseId, command.productId, value(product, "modelId"), unitCode, serialNumber, batchId, operator);
            mapper.insertBatchItem(batchId, MerchantContextUtils.getMemberId(), serialNumber, operator);
        }
        mapper.refreshInventoryCounts(inventoryId, operator);
        mapper.updateBatch(batchId, serialNumbers.size(), 0, 2, operator);
        return mapper.selectBatchByIdempotency(MerchantContextUtils.getMemberId(), idempotencyKey);
    }

    /** 按数量和仓库初始化数量库存，不创建无意义的序列号库存单元。 */
    private Map<String, Object> addQuantityBatch(MerchantInventoryBatchCommand command, Map<String, Object> product)
    {
        Integer quantity = command.quantity;
        if (quantity == null || quantity < 1 || quantity > MAX_BATCH_SIZE)
        {
            throw new ServiceException("库存数量必须为 1-" + MAX_BATCH_SIZE + " 个");
        }
        Long memberId = MerchantContextUtils.getMemberId();
        Long productId = command.productId;
        String idempotencyKey = trim(command.idempotencyKey);
        if (idempotencyKey == null)
        {
            idempotencyKey = UUID.randomUUID().toString();
        }
        Map<String, Object> existingBatch = mapper.selectBatchByIdempotency(memberId, idempotencyKey);
        if (existingBatch != null)
        {
            return existingBatch;
        }
        if (mapper.selectInventoryByScope(memberId, command.warehouseId, productId) != null)
        {
            throw new ServiceException("该商品在所选仓库已存在库存");
        }
        Long operator = SecurityUtils.getUserId();
        String batchNo = codeGeneratorClient.next("inventory_batch");
        mapper.insertBatch(MerchantContextUtils.getTenantId(), memberId, command.warehouseId, productId,
                value(product, "modelId"), batchNo, quantity, idempotencyKey, operator, trim(command.remark));
        Long batchId = lastInsertId();
        if (mapper.insertQuantityInventory(MerchantContextUtils.getTenantId(), memberId, command.warehouseId,
                productId, value(product, "modelId"), quantity, operator) != 1)
        {
            throw new ServiceException("数量库存初始化失败");
        }
        mapper.updateBatch(batchId, quantity, 0, 2, operator);
        return mapper.selectBatchByIdempotency(memberId, idempotencyKey);
    }

    private void checkWarehouse(Long warehouseId)
    {
        if (warehouseId != null)
        {
            MerchantContextUtils.checkWarehouseAccess(warehouseId);
            if (mapper.selectWarehouse(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), warehouseId) == null)
            {
                throw new ServiceException("仓库不存在或不属于当前商户");
            }
        }
    }

    private Long lastInsertId()
    {
        Long id = mapper.selectLastInsertId();
        if (id == null)
        {
            throw new ServiceException("库存批次编号生成失败");
        }
        return id;
    }

    private List<String> normalize(List<String> source)
    {
        Set<String> values = new LinkedHashSet<>();
        if (source != null)
        {
            for (String value : source)
            {
                String normalized = trim(value);
                if (normalized != null)
                {
                    values.add(normalized);
                }
            }
        }
        return new ArrayList<>(values);
    }

    private Long value(Map<String, Object> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        return value == null ? null : ((Number) value).longValue();
    }

    private String trim(String value)
    {
        if (value == null)
        {
            return null;
        }
        String result = value.trim();
        return result.isEmpty() ? null : result;
    }

    private Integer integerValue(Map<String, Object> row, String key)
    {
        Long value = value(row, key);
        return value == null ? null : value.intValue();
    }

    private Integer normalizeUnitStatus(Integer value, Integer fallback)
    {
        Integer candidate = value == null ? fallback : value;
        if (candidate == null)
        {
            throw new ServiceException("设备状态不能为空");
        }
        for (RentalCode.InventoryStatus status : RentalCode.InventoryStatus.values())
        {
            if (status.getCode().equals(candidate))
            {
                return candidate;
            }
        }
        throw new ServiceException("设备状态不合法");
    }

    private boolean isWorkflowStatus(Integer status)
    {
        return RentalCode.InventoryStatus.RESERVED.getCode().equals(status)
                || RentalCode.InventoryStatus.RENTED.getCode().equals(status);
    }

    private Integer normalizeConditionLevel(Integer value)
    {
        if (value != null && !MerchantInventoryConditionLevel.isSupported(value))
        {
            throw new ServiceException("成色只能选择全新、99新、95新、90新或80新");
        }
        return value;
    }

    private BigDecimal nonNegative(BigDecimal value, String message)
    {
        if (value != null && value.signum() < 0)
        {
            throw new ServiceException(message);
        }
        return value;
    }

    private String normalizeDate(String value)
    {
        String normalized = trim(value);
        if (normalized == null)
        {
            return null;
        }
        try
        {
            return LocalDate.parse(normalized).toString();
        }
        catch (DateTimeParseException exception)
        {
            throw new ServiceException("保险到期日期格式不正确，应为 yyyy-MM-dd");
        }
    }
}
