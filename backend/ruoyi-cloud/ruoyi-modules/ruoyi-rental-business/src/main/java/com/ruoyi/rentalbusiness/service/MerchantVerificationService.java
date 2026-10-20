package com.ruoyi.rentalbusiness.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.rentalbusiness.enums.MerchantVerificationMode;
import com.ruoyi.rentalbusiness.enums.MerchantVerificationSource;
import com.ruoyi.rentalbusiness.enums.MerchantVerificationTransactionType;
import com.ruoyi.rentalbusiness.mapper.MerchantInventoryMapper;
import com.ruoyi.rentalbusiness.model.MerchantInventoryBatchCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderPackageDeviceBindCommand;
import com.ruoyi.rentalbusiness.model.MerchantVerificationCommitCommand;
import com.ruoyi.rentalbusiness.model.MerchantVerificationCommitVO;
import com.ruoyi.rentalbusiness.model.MerchantVerificationScanCommand;
import com.ruoyi.rentalbusiness.model.MerchantVerificationScanVO;

/** 极速核销台应用服务，负责扫码校验以及入库、出库的原子提交。 */
@Service
public class MerchantVerificationService
{
    private static final int AVAILABLE = RentalCode.InventoryStatus.AVAILABLE.getCode();
    private static final int RESERVED = RentalCode.InventoryStatus.RESERVED.getCode();
    private static final int RENTED = RentalCode.InventoryStatus.RENTED.getCode();
    private static final int REPAIRING = RentalCode.InventoryStatus.REPAIRING.getCode();
    private static final int PREPARED = RentalCode.OrderStatus.PREPARED.getCode();
    private static final int SHIPPED = RentalCode.OrderStatus.SHIPPED.getCode();
    private static final int SERIAL_STOCK = RentalCode.StockMode.SERIAL.getCode();
    private static final int MAX_CODE_COUNT = 5000;

    private final MerchantInventoryMapper inventoryMapper;
    private final MerchantInventoryService inventoryService;
    private final MerchantInventoryReservationService reservationService;
    private final MerchantOrderPackageDeviceService packageDeviceService;
    private final MerchantOrderService orderService;
    private final ObjectMapper objectMapper;

    public MerchantVerificationService(MerchantInventoryMapper inventoryMapper,
            MerchantInventoryService inventoryService, MerchantInventoryReservationService reservationService,
            MerchantOrderPackageDeviceService packageDeviceService, MerchantOrderService orderService,
            ObjectMapper objectMapper)
    {
        this.inventoryMapper = inventoryMapper;
        this.inventoryService = inventoryService;
        this.reservationService = reservationService;
        this.packageDeviceService = packageDeviceService;
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    /** 校验单次扫描，不改变库存和订单状态。 */
    @Transactional(readOnly = true)
    public MerchantVerificationScanVO scan(MerchantVerificationScanCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        validateScanCommand(command);
        if (MerchantVerificationMode.INBOUND.equals(command.mode))
        {
            return scanInbound(command);
        }
        MerchantOrderCreateCommand order = requireOutboundOrder(command.orderId, command.warehouseId);
        return scanOutbound(command, order);
    }

    /** 批量提交扫码入库。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantVerificationCommitVO commitInbound(MerchantVerificationCommitCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        validateCommitMode(command, MerchantVerificationMode.INBOUND);
        if (command.source == null)
        {
            throw new ServiceException("请选择入库来源");
        }
        Long warehouseId = requireWarehouse(command.warehouseId);
        List<String> codes = normalizeCodes(command.codes);
        String idempotencyKey = normalizeIdempotencyKey(command.idempotencyKey);
        if (MerchantVerificationSource.NEW_STOCK.equals(command.source))
        {
            if (command.productId == null)
            {
                throw new ServiceException("采购入库必须选择商品");
            }
            MerchantInventoryBatchCommand batch = new MerchantInventoryBatchCommand();
            batch.productId = command.productId;
            batch.warehouseId = warehouseId;
            batch.serialNumbers = codes;
            batch.idempotencyKey = "verification-inbound:" + idempotencyKey;
            batch.remark = trim(command.remark);
            Map<String, Object> result = inventoryService.addBatch(batch);
            MerchantVerificationCommitVO response = commitResult(MerchantVerificationMode.INBOUND,
                    codes.size(), 0, "入库批次已提交");
            if (result != null && result.get("successQuantity") != null)
            {
                response.successCount = integer(result.get("successQuantity"));
                response.skippedCount = response.successCount == codes.size() ? 0 : codes.size() - response.successCount;
            }
            return response;
        }
        if (codes.isEmpty())
        {
            throw new ServiceException("请至少扫描一件设备");
        }
        int successCount = 0;
        int skippedCount = 0;
        Long orderId = command.orderId;
        for (String code : codes)
        {
            String transactionKey = verificationKey("inbound", idempotencyKey, code);
            if (inventoryMapper.selectInventoryTransactionByIdempotency(MerchantContextUtils.getMemberId(),
                    transactionKey) != null)
            {
                skippedCount++;
                continue;
            }
            Map<String, Object> unit = inventoryMapper.selectVerificationUnitForUpdate(
                    MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), warehouseId, code, null);
            validateReturnUnit(unit, command.source);
            int beforeAvailable = integer(unit.get("availableQuantity"));
            Long unitId = number(unit, "unitId");
            Long inventoryId = number(unit, "inventoryId");
            if (inventoryMapper.updateUnitStatus(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                    unitId, command.source.getExpectedStatus(), AVAILABLE, SecurityUtils.getUserId()) != 1)
            {
                throw new ServiceException("设备状态已变化，请刷新后重试");
            }
            inventoryMapper.refreshInventoryCounts(inventoryId, SecurityUtils.getUserId());
            int afterAvailable = availableQuantity(unit);
            inventoryMapper.insertInventoryTransaction(MerchantContextUtils.getTenantId(),
                    MerchantContextUtils.getMemberId(), inventoryId, warehouseId, number(unit, "productId"),
                    MerchantVerificationTransactionType.RETURN.getCode(), 1, beforeAvailable, afterAvailable,
                    orderId, transactionKey, trim(command.remark), SecurityUtils.getUserId());
            successCount++;
        }
        if (orderId != null
                && (MerchantVerificationSource.RENTAL_RETURN.equals(command.source)
                        || MerchantVerificationSource.ALLIANCE_RETURN.equals(command.source))
                && successCount + skippedCount > 0)
        {
            orderService.markReturning(orderId);
        }
        return commitResult(MerchantVerificationMode.INBOUND, successCount, skippedCount, "扫码入库成功");
    }

    /** 批量提交扫码出库，绑定套餐设备、消耗预占并推进订单发货状态。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantVerificationCommitVO commitOutbound(MerchantVerificationCommitCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        validateCommitMode(command, MerchantVerificationMode.OUTBOUND);
        MerchantOrderCreateCommand order = requireOutboundOrder(command == null ? null : command.orderId,
                command == null ? null : command.warehouseId);
        if (SHIPPED == integer(order.orderStatus))
        {
            return commitResult(MerchantVerificationMode.OUTBOUND, 0, 0, "订单已完成出库");
        }
        List<VerificationSlot> slots = packageSlots(order);
        List<String> codes = normalizeCodes(command.codes);
        int requiredCount = slots.stream().mapToInt(slot -> slot.quantity).sum();
        if (codes.size() != requiredCount)
        {
            throw new ServiceException("请扫描套餐所需的全部逐件设备（需要 " + requiredCount + " 件）");
        }
        String idempotencyKey = normalizeIdempotencyKey(command.idempotencyKey);
        Map<String, LockedUnit> lockedUnits = new LinkedHashMap<String, LockedUnit>();
        Set<Long> selectedUnitIds = new LinkedHashSet<Long>();
        for (String code : codes)
        {
            String transactionKey = verificationKey("outbound", idempotencyKey, code);
            if (inventoryMapper.selectInventoryTransactionByIdempotency(MerchantContextUtils.getMemberId(),
                    transactionKey) != null)
            {
                throw new ServiceException("本批扫码记录已提交，请刷新订单状态");
            }
            Map<String, Object> unit = inventoryMapper.selectVerificationUnitForUpdate(
                    MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), order.warehouseId,
                    code, order.orderId);
            String validation = validateOutboundUnit(unit, slots);
            if (validation != null)
            {
                throw new ServiceException(validation);
            }
            Long unitId = number(unit, "unitId");
            if (!selectedUnitIds.add(unitId))
            {
                throw new ServiceException("本批存在重复设备，请重新扫描");
            }
            lockedUnits.put(code, new LockedUnit(unit, transactionKey));
        }
        Map<Integer, List<Long>> slotUnitIds = matchUnitsToSlots(slots, lockedUnits.values());
        for (VerificationSlot slot : slots)
        {
            MerchantOrderPackageDeviceBindCommand bind = new MerchantOrderPackageDeviceBindCommand();
            bind.itemIndex = slot.itemIndex;
            bind.unitIds = slotUnitIds.get(slot.itemIndex);
            packageDeviceService.bind(order.orderId, bind);
        }
        reservationService.consumeActiveByOrder(order.orderId, selectedUnitIds);
        for (LockedUnit locked : lockedUnits.values())
        {
            Map<String, Object> unit = locked.row;
            int unitStatus = integer(unit.get("unitStatus"));
            int beforeAvailable = integer(unit.get("availableQuantity"));
            if (unitStatus == AVAILABLE
                    && inventoryMapper.updateUnitStatus(MerchantContextUtils.getTenantId(),
                            MerchantContextUtils.getMemberId(), number(unit, "unitId"), AVAILABLE, RENTED,
                            SecurityUtils.getUserId()) != 1)
            {
                throw new ServiceException("设备状态已变化，请刷新后重试");
            }
            inventoryMapper.refreshInventoryCounts(number(unit, "inventoryId"), SecurityUtils.getUserId());
            int afterAvailable = availableQuantity(unit);
            inventoryMapper.insertInventoryTransaction(MerchantContextUtils.getTenantId(),
                    MerchantContextUtils.getMemberId(), number(unit, "inventoryId"), order.warehouseId,
                    number(unit, "productId"), MerchantVerificationTransactionType.OUTBOUND.getCode(), 1,
                    beforeAvailable, afterAvailable, order.orderId, locked.transactionKey,
                    "核销台扫码出库", SecurityUtils.getUserId());
        }
        orderService.shipByVerification(order.orderId);
        return commitResult(MerchantVerificationMode.OUTBOUND, codes.size(), 0, "扫码出库成功");
    }

    private MerchantVerificationScanVO scanInbound(MerchantVerificationScanCommand command)
    {
        Long warehouseId = requireWarehouse(command.warehouseId);
        MerchantVerificationScanVO response = new MerchantVerificationScanVO();
        response.code = trim(command.code);
        response.sourceLabel = command.source == null ? null : command.source.getLabel();
        if (MerchantVerificationSource.NEW_STOCK.equals(command.source))
        {
            if (command.productId == null)
            {
                return invalid(response, "请选择采购入库商品");
            }
            Map<String, Object> product = inventoryMapper.selectProduct(MerchantContextUtils.getTenantId(),
                    MerchantContextUtils.getMemberId(), command.productId);
            if (product == null)
            {
                return invalid(response, "商品不存在或已停用");
            }
            if (!Integer.valueOf(SERIAL_STOCK).equals(product.get("stockMode")))
            {
                return invalid(response, "当前商品不是逐件库存，不能使用扫码入库");
            }
            Map<String, Object> existing = inventoryMapper.selectVerificationUnit(MerchantContextUtils.getTenantId(),
                    MerchantContextUtils.getMemberId(), null, command.code, null);
            if (existing != null)
            {
                return invalid(response, "序列号已存在，不能重复入库");
            }
            fillProduct(response, product, null, warehouseId);
            response.valid = true;
            response.validationLabel = "可入库";
            response.statusLabel = "待入库";
            return response;
        }
        Map<String, Object> unit = inventoryMapper.selectVerificationUnit(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), warehouseId, command.code, null);
        if (unit == null)
        {
            return invalid(response, "未找到当前仓库的库存设备");
        }
        fillUnit(response, unit);
        if (command.source == null)
        {
            return invalid(response, "请选择入库来源");
        }
        if (integer(unit.get("unitStatus")) != command.source.getExpectedStatus())
        {
            return invalid(response, "当前状态为" + inventoryStatusLabel(integer(unit.get("unitStatus")))
                    + "，不符合" + command.source.getLabel() + "要求");
        }
        response.valid = true;
        response.validationLabel = "可入库";
        return response;
    }

    private MerchantVerificationScanVO scanOutbound(MerchantVerificationScanCommand command,
            MerchantOrderCreateCommand order)
    {
        MerchantVerificationScanVO response = new MerchantVerificationScanVO();
        response.code = trim(command.code);
        response.orderNo = order.orderNo;
        response.packageName = packageName(order.packageDetailsJson);
        response.rentalPeriod = order.leaseStartDate + " 至 " + order.leaseEndDate;
        response.returnDate = order.returnDueTime;
        Map<String, Object> unit = inventoryMapper.selectVerificationUnit(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), order.warehouseId, command.code, order.orderId);
        if (unit == null)
        {
            return invalid(response, "未找到当前发货仓库的库存设备");
        }
        fillUnit(response, unit);
        String validation = validateOutboundUnit(unit, packageSlots(order));
        if (validation != null)
        {
            return invalid(response, validation);
        }
        response.valid = true;
        response.validationLabel = "可出库";
        return response;
    }

    private MerchantOrderCreateCommand requireOutboundOrder(Long orderId, Long warehouseId)
    {
        if (orderId == null)
        {
            throw new ServiceException("请选择出库订单");
        }
        MerchantOrderCreateCommand order = orderService.get(orderId);
        if (order.warehouseId == null || !order.warehouseId.equals(warehouseId))
        {
            throw new ServiceException("当前扫描仓库与订单发货仓库不一致");
        }
        if (order.orderStatus == null || (order.orderStatus.intValue() != PREPARED
                && order.orderStatus.intValue() != SHIPPED))
        {
            throw new ServiceException("只有已备货订单可以扫码出库");
        }
        if (order.packageId == null || trim(order.packageDetailsJson) == null)
        {
            throw new ServiceException("订单尚未生成套餐明细，不能扫码出库");
        }
        return order;
    }

    private Long requireWarehouse(Long warehouseId)
    {
        if (warehouseId == null)
        {
            throw new ServiceException("请选择当前仓库");
        }
        MerchantContextUtils.checkWarehouseAccess(warehouseId);
        if (inventoryMapper.selectWarehouse(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                warehouseId) == null)
        {
            throw new ServiceException("仓库不存在或不属于当前商户");
        }
        return warehouseId;
    }

    private void validateScanCommand(MerchantVerificationScanCommand command)
    {
        if (command == null || command.mode == null)
        {
            throw new ServiceException("核销台操作方向不能为空");
        }
        if (trim(command.code) == null)
        {
            throw new ServiceException("请扫描或输入库存编码");
        }
    }

    private void validateCommitMode(MerchantVerificationCommitCommand command, MerchantVerificationMode mode)
    {
        if (command == null || !mode.equals(command.mode))
        {
            throw new ServiceException(mode.getLabel() + "提交参数不正确");
        }
    }

    private List<String> normalizeCodes(List<String> values)
    {
        if (values == null || values.isEmpty())
        {
            return new ArrayList<String>();
        }
        if (values.size() > MAX_CODE_COUNT)
        {
            throw new ServiceException("单批最多扫描 " + MAX_CODE_COUNT + " 件设备");
        }
        Set<String> unique = new LinkedHashSet<String>();
        for (String value : values)
        {
            String code = trim(value);
            if (code == null)
            {
                throw new ServiceException("扫描编码不能为空");
            }
            if (!unique.add(code))
            {
                throw new ServiceException("本批存在重复扫描编码：" + code);
            }
        }
        return new ArrayList<String>(unique);
    }

    private String normalizeIdempotencyKey(String value)
    {
        String key = trim(value);
        if (key == null)
        {
            key = UUID.randomUUID().toString();
        }
        if (key.length() > 64)
        {
            throw new ServiceException("幂等键不能超过64个字符");
        }
        return key;
    }

    private String verificationKey(String direction, String idempotencyKey, String code)
    {
        String suffix = direction + ":" + idempotencyKey + ":" + code;
        if (suffix.length() <= 120)
        {
            return "verification:" + suffix;
        }
        return "verification:" + direction + ":" + idempotencyKey + ":" + Integer.toHexString(code.hashCode());
    }

    private void validateReturnUnit(Map<String, Object> unit, MerchantVerificationSource source)
    {
        if (unit == null)
        {
            throw new ServiceException("未找到当前仓库的库存设备");
        }
        if (source.getExpectedStatus() == null || integer(unit.get("unitStatus")) != source.getExpectedStatus())
        {
            throw new ServiceException("设备当前状态为" + inventoryStatusLabel(integer(unit.get("unitStatus")))
                    + "，不能作为" + source.getLabel() + "入库");
        }
    }

    private String validateOutboundUnit(Map<String, Object> unit, List<VerificationSlot> slots)
    {
        if (unit == null)
        {
            return "未找到当前发货仓库的库存设备";
        }
        if (integer(unit.get("reservedByOtherOrder")) == 1 || integer(unit.get("boundByOtherOrder")) == 1)
        {
            return "设备已被其他订单占用";
        }
        int status = integer(unit.get("unitStatus"));
        if (status != AVAILABLE && !(status == RESERVED && integer(unit.get("reservedByOrder")) == 1))
        {
            return "设备当前状态为" + inventoryStatusLabel(status) + "，不能出库";
        }
        Long productId = number(unit, "productId");
        for (VerificationSlot slot : slots)
        {
            if (productId != null && productId.equals(slot.productId))
            {
                return null;
            }
        }
        return "设备不属于当前订单套餐明细";
    }

    private List<VerificationSlot> packageSlots(MerchantOrderCreateCommand order)
    {
        if (order == null || trim(order.packageDetailsJson) == null)
        {
            throw new ServiceException("订单套餐明细为空");
        }
        try
        {
            JsonNode root = objectMapper.readTree(order.packageDetailsJson);
            JsonNode items = root == null ? null : root.path("items");
            if (items == null || !items.isArray())
            {
                throw new ServiceException("订单套餐明细格式不正确");
            }
            List<VerificationSlot> slots = new ArrayList<VerificationSlot>();
            for (int index = 0; index < items.size(); index++)
            {
                JsonNode item = items.get(index);
                if (item == null || !item.isObject() || !isSelectable(item))
                {
                    continue;
                }
                Long productId = longValue(item, "productId", "product_id");
                if (productId == null)
                {
                    throw new ServiceException("套餐逐件商品未关联商户商品");
                }
                int quantity = integer(item, "quantity");
                if (quantity <= 0)
                {
                    throw new ServiceException("套餐逐件商品数量不正确");
                }
                slots.add(new VerificationSlot(index, productId, quantity));
            }
            return slots;
        }
        catch (JsonProcessingException exception)
        {
            throw new ServiceException("订单套餐明细格式不正确");
        }
    }

    private Map<Integer, List<Long>> matchUnitsToSlots(List<VerificationSlot> slots,
            Iterable<LockedUnit> lockedUnits)
    {
        Map<Integer, List<Long>> matched = new LinkedHashMap<Integer, List<Long>>();
        Map<Long, Integer> used = new LinkedHashMap<Long, Integer>();
        for (VerificationSlot slot : slots)
        {
            matched.put(slot.itemIndex, new ArrayList<Long>());
        }
        for (LockedUnit locked : lockedUnits)
        {
            Long productId = number(locked.row, "productId");
            VerificationSlot target = null;
            for (VerificationSlot slot : slots)
            {
                if (productId.equals(slot.productId) && used.getOrDefault(slot.productId, 0) < slot.quantity)
                {
                    target = slot;
                    break;
                }
            }
            if (target == null)
            {
                throw new ServiceException("扫描设备与套餐明细数量不匹配");
            }
            matched.get(target.itemIndex).add(number(locked.row, "unitId"));
            used.put(target.productId, used.getOrDefault(target.productId, 0) + 1);
        }
        for (VerificationSlot slot : slots)
        {
            if (matched.get(slot.itemIndex).size() != slot.quantity)
            {
                throw new ServiceException("套餐明细设备未扫描完整");
            }
        }
        return matched;
    }

    private boolean isSelectable(JsonNode item)
    {
        JsonNode selectable = item.get("selectableDevice");
        if (selectable != null && !selectable.isNull())
        {
            return selectable.asBoolean();
        }
        Integer role = integer(item, "itemRole", "item_role");
        return (role != null && (role == 1 || role == 4))
                && SERIAL_STOCK == integer(item, "stockMode", "stock_mode");
    }

    private void fillProduct(MerchantVerificationScanVO response, Map<String, Object> product, String status,
            Long warehouseId)
    {
        response.productName = text(product, "productName");
        response.modelName = text(product, "modelName");
        response.productTypeLabel = productTypeLabel(integer(product.get("productType")));
        response.warehouseName = warehouseName(warehouseId);
        response.statusLabel = status;
    }

    private void fillUnit(MerchantVerificationScanVO response, Map<String, Object> unit)
    {
        response.unitCode = text(unit, "unitCode");
        response.serialNumber = text(unit, "serialNumber");
        response.productName = text(unit, "productName");
        response.modelName = text(unit, "modelName");
        response.productTypeLabel = productTypeLabel(integer(unit.get("productType")));
        response.warehouseName = text(unit, "warehouseName");
        response.statusLabel = inventoryStatusLabel(integer(unit.get("unitStatus")));
    }

    private MerchantVerificationScanVO invalid(MerchantVerificationScanVO response, String reason)
    {
        response.valid = false;
        response.validationLabel = reason;
        return response;
    }

    private MerchantVerificationCommitVO commitResult(MerchantVerificationMode mode, int successCount,
            int skippedCount, String message)
    {
        MerchantVerificationCommitVO response = new MerchantVerificationCommitVO();
        response.success = true;
        response.modeLabel = mode.getLabel();
        response.successCount = successCount;
        response.skippedCount = skippedCount;
        response.message = message;
        return response;
    }

    private String packageName(String json)
    {
        try
        {
            JsonNode root = objectMapper.readTree(json);
            return root == null ? null : text(root, "packageName", "package_name");
        }
        catch (JsonProcessingException exception)
        {
            return null;
        }
    }

    private String warehouseName(Long warehouseId)
    {
        Map<String, Object> warehouse = inventoryMapper.selectWarehouse(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), warehouseId);
        return text(warehouse, "warehouseName");
    }

    private int availableQuantity(Map<String, Object> unit)
    {
        Map<String, Object> inventory = inventoryMapper.selectInventoryForUpdate(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), number(unit, "productId"), number(unit, "warehouseId"));
        return inventory == null ? 0 : integer(inventory.get("availableQuantity"));
    }

    private String inventoryStatusLabel(int value)
    {
        for (RentalCode.InventoryStatus status : RentalCode.InventoryStatus.values())
        {
            if (status.getCode() == value)
            {
                return status.getLabel();
            }
        }
        return "未知状态";
    }

    private String productTypeLabel(int value)
    {
        for (RentalCode.ProductType type : RentalCode.ProductType.values())
        {
            if (type.getCode() == value)
            {
                return type.getLabel();
            }
        }
        return "商品";
    }

    private static Long number(Map<String, Object> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        return value instanceof Number ? ((Number) value).longValue() : longValue(value);
    }

    private static Long longValue(JsonNode node, String... names)
    {
        if (node == null || names == null)
        {
            return null;
        }
        for (String name : names)
        {
            JsonNode value = node.get(name);
            if (value != null && !value.isNull())
            {
                try
                {
                    return value.isNumber() ? value.longValue() : Long.valueOf(value.asText().trim());
                }
                catch (NumberFormatException ignored)
                {
                    return null;
                }
            }
        }
        return null;
    }

    private static Long longValue(Object value)
    {
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        if (value == null)
        {
            return null;
        }
        try
        {
            return Long.valueOf(String.valueOf(value).trim());
        }
        catch (NumberFormatException ignored)
        {
            return null;
        }
    }

    private static int integer(Object value)
    {
        if (value instanceof Number)
        {
            return ((Number) value).intValue();
        }
        try
        {
            return value == null ? 0 : Integer.parseInt(String.valueOf(value));
        }
        catch (NumberFormatException ignored)
        {
            return 0;
        }
    }

    private static int integer(JsonNode node, String... names)
    {
        Long value = longValue(node, names);
        return value == null ? 0 : value.intValue();
    }

    private static String text(Map<String, Object> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        return value == null ? null : trim(String.valueOf(value));
    }

    private static String text(JsonNode node, String... names)
    {
        if (node == null || names == null)
        {
            return null;
        }
        for (String name : names)
        {
            JsonNode value = node.get(name);
            if (value != null && !value.isNull() && !value.asText().trim().isEmpty())
            {
                return value.asText().trim();
            }
        }
        return null;
    }

    private static String trim(String value)
    {
        if (value == null || value.trim().isEmpty())
        {
            return null;
        }
        return value.trim();
    }

    private static final class VerificationSlot
    {
        private final int itemIndex;
        private final Long productId;
        private final int quantity;

        private VerificationSlot(int itemIndex, Long productId, int quantity)
        {
            this.itemIndex = itemIndex;
            this.productId = productId;
            this.quantity = quantity;
        }
    }

    private static final class LockedUnit
    {
        private final Map<String, Object> row;
        private final String transactionKey;

        private LockedUnit(Map<String, Object> row, String transactionKey)
        {
            this.row = row;
            this.transactionKey = transactionKey;
        }
    }
}
