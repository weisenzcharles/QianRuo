package com.ruoyi.rentalbusiness.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.rentalbusiness.enums.MerchantInventoryReservationSource;
import com.ruoyi.rentalbusiness.enums.MerchantInventoryReservationStatus;
import com.ruoyi.rentalbusiness.mapper.MerchantInventoryMapper;
import com.ruoyi.rentalbusiness.mapper.MerchantPackageMapper;
import com.ruoyi.rentalbusiness.model.MerchantExternalInventoryReservationCommand;
import com.ruoyi.rentalbusiness.model.MerchantExternalInventoryReservationView;
import com.ruoyi.rentalbusiness.model.InventoryReservationCommand;
import com.ruoyi.rentalbusiness.model.InventoryReservationVO;
import com.ruoyi.rentalbusiness.model.InventoryTransitionCommand;

/** 基于商户套餐 BOM 和逐件库存单元的库存预占服务。 */
@Service
public class MerchantInventoryReservationService
{
    private static final int AVAILABLE = 1;
    private static final int RESERVED = 2;
    private static final int RELEASED = 2;
    private static final int QUANTITY_STOCK_MODE = 2;
    private static final int CONSUMED = 3;
    private static final int EXPIRED = 4;
    private static final int MAX_EXTERNAL_QUANTITY = 1000;
    private static final int MAX_EXTERNAL_RESERVATION_HOURS = 24 * 30;
    private static final int MAX_EXTERNAL_EXPIRY_BATCH_SIZE = 500;

    private final MerchantInventoryMapper inventoryMapper;
    private final MerchantPackageMapper packageMapper;
    private final CodeGeneratorClient codeGeneratorClient;

    public MerchantInventoryReservationService(MerchantInventoryMapper inventoryMapper,
            MerchantPackageMapper packageMapper, CodeGeneratorClient codeGeneratorClient)
    {
        this.inventoryMapper = inventoryMapper;
        this.packageMapper = packageMapper;
        this.codeGeneratorClient = codeGeneratorClient;
    }

    /** 按当前生效套餐 BOM 预占逐件库存，幂等键重复提交直接返回原记录。 */
    @Transactional(rollbackFor = Exception.class)
    public InventoryReservationVO reserve(InventoryReservationCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null || command.packageId == null || command.warehouseId == null
                || command.orderId == null || blank(command.idempotencyKey))
        {
            throw new ServiceException("订单、套餐、仓库和预占幂等键不能为空");
        }
        MerchantContextUtils.checkWarehouseAccess(command.warehouseId);
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        Map<String, Object> existing = inventoryMapper.selectReservationByKeyForUpdate(tenantId, memberId,
                command.idempotencyKey.trim());
        if (existing != null)
        {
            return toView(existing);
        }
        Map<String, Object> packageRow = packageMapper.selectById(tenantId, memberId, command.packageId);
        Map<String, Object> version = packageMapper.selectVersionById(tenantId, memberId, command.packageId);
        if (packageRow == null || version == null || !Integer.valueOf(0).equals(packageRow.get("status")))
        {
            throw new ServiceException("套餐不存在、已停用或没有生效版本");
        }
        Long versionId = number(version, "packageVersionId");
        List<Map<String, Object>> items = packageMapper.selectItems(versionId);
        if (items == null || items.isEmpty())
        {
            throw new ServiceException("套餐没有可用的生效明细");
        }
        Long operator = SecurityUtils.getUserId();
        String key = command.idempotencyKey.trim();
        String reservationNo = codeGeneratorClient.next("inventory_reservation");
        inventoryMapper.insertReservation(tenantId, memberId, command.orderId, command.packageId, versionId,
                command.warehouseId, reservationNo, key, operator, command.reason);
        Map<String, Object> reservation = inventoryMapper.selectReservationByKeyForUpdate(tenantId, memberId, key);
        if (reservation == null)
        {
            throw new ServiceException("库存预占单创建失败");
        }
        Long reservationId = number(reservation, "reservationId");
        for (Map<String, Object> item : items)
        {
            if (!Integer.valueOf(1).equals(item.get("requiredFlag")))
            {
                continue;
            }
            Long productId = number(item, "productId");
            if (productId == null)
            {
                productId = number(item, "accessoryItemProductId");
            }
            if (productId == null)
            {
                throw new ServiceException("套餐配件尚未绑定商户商品，无法预占库存");
            }
            int packageQuantity = command.quantity == null ? 1 : command.quantity;
            if (packageQuantity <= 0)
            {
                throw new ServiceException("订单套餐数量必须大于零");
            }
            int quantity = positive(item.get("quantity")) * packageQuantity;
            reserveInventory(tenantId, memberId, productId, command.warehouseId, quantity, reservationId, operator);
        }
        List<Map<String, Object>> reservationItems = inventoryMapper.selectReservationUnits(reservationId);
        reservationItems.addAll(inventoryMapper.selectReservationQuantities(reservationId));
        reservation.put("units", reservationItems);
        return toView(reservation);
    }

    /** 按目标商户的具体商品预占库存，用于没有可匹配套餐的联盟子订单。 */
    @Transactional(rollbackFor = Exception.class)
    public InventoryReservationVO reserveProduct(InventoryReservationCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null || command.productId == null || command.warehouseId == null
                || command.orderId == null || blank(command.idempotencyKey))
        {
            throw new ServiceException("订单、商品、仓库和预占幂等键不能为空");
        }
        MerchantContextUtils.checkWarehouseAccess(command.warehouseId);
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        String key = command.idempotencyKey.trim();
        Map<String, Object> existing = inventoryMapper.selectReservationByKeyForUpdate(tenantId, memberId, key);
        if (existing != null)
        {
            return toView(existing);
        }
        int quantity = command.quantity == null ? 1 : command.quantity;
        if (quantity <= 0)
        {
            throw new ServiceException("商品数量必须大于零");
        }
        Long operator = SecurityUtils.getUserId();
        inventoryMapper.insertReservation(tenantId, memberId, command.orderId, null, null, command.warehouseId,
                codeGeneratorClient.next("inventory_reservation"), key, operator, command.reason);
        Map<String, Object> reservation = inventoryMapper.selectReservationByKeyForUpdate(tenantId, memberId, key);
        if (reservation == null)
        {
            throw new ServiceException("库存预占单创建失败");
        }
        Long reservationId = number(reservation, "reservationId");
        reserveInventory(tenantId, memberId, command.productId, command.warehouseId, quantity, reservationId, operator);
        List<Map<String, Object>> reservationItems = inventoryMapper.selectReservationUnits(reservationId);
        reservationItems.addAll(inventoryMapper.selectReservationQuantities(reservationId));
        reservation.put("units", reservationItems);
        return toView(reservation);
    }

    /**
     * 创建外部平台库存预占。外部请求不创建正式订单，只锁定现有套餐或商品库存，
     * 后续由外部订单接入流程补齐客户和履约信息。
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantExternalInventoryReservationView reserveExternal(Long tenantId, Long memberId, Long tokenId,
            MerchantExternalInventoryReservationCommand command, String requestId)
    {
        if (tenantId == null || memberId == null || tokenId == null || command == null)
        {
            throw new ServiceException("外部库存预占参数不完整");
        }
        String normalizedRequestId = requiredExternalText(requestId, "请求幂等号", 128);
        String externalOrderNo = requiredExternalText(command.externalOrderNo, "外部订单标识", 128);
        if ((command.packageId == null) == (command.productId == null))
        {
            throw new ServiceException("套餐和商品只能选择一个");
        }
        if (command.warehouseId == null)
        {
            throw new ServiceException("外部库存预占必须指定仓库");
        }
        int quantity = command.quantity == null ? 1 : command.quantity;
        if (quantity <= 0 || quantity > MAX_EXTERNAL_QUANTITY)
        {
            throw new ServiceException("预占数量必须在1到1000之间");
        }
        LocalDateTime expireTime = command.expireTime;
        LocalDateTime now = LocalDateTime.now();
        if (expireTime == null || !expireTime.isAfter(now)
                || expireTime.isAfter(now.plusHours(MAX_EXTERNAL_RESERVATION_HOURS)))
        {
            throw new ServiceException("预占过期时间必须晚于当前时间且不超过30天");
        }
        String idempotencyKey = externalIdempotencyKey(tokenId, normalizedRequestId);
        String payloadHash = sha256(command.canonicalValue());
        Map<String, Object> existing = inventoryMapper.selectReservationByKeyForUpdate(tenantId, memberId,
                idempotencyKey);
        if (existing != null)
        {
            if (!payloadHash.equals(string(existing, "externalPayloadHash")))
            {
                throw new ServiceException("同一请求幂等号不能提交不同的库存预占参数");
            }
            return toExternalView(existing, command, true);
        }

        Long packageVersionId = null;
        List<Map<String, Object>> packageItems = null;
        if (command.packageId != null)
        {
            Map<String, Object> packageRow = packageMapper.selectById(tenantId, memberId, command.packageId);
            Map<String, Object> version = packageMapper.selectVersionById(tenantId, memberId, command.packageId);
            if (packageRow == null || version == null || !Integer.valueOf(0).equals(packageRow.get("status")))
            {
                throw new ServiceException("套餐不存在、已停用或没有生效版本");
            }
            packageVersionId = number(version, "packageVersionId");
            packageItems = packageMapper.selectItems(packageVersionId);
            if (packageItems == null || packageItems.isEmpty())
            {
                throw new ServiceException("套餐没有可用的生效明细");
            }
        }
        if (inventoryMapper.insertExternalReservation(tenantId, memberId, command.packageId, packageVersionId,
                command.warehouseId, codeGeneratorClient.next("inventory_reservation"), idempotencyKey, tokenId,
                externalOrderNo, command.productId, quantity, payloadHash, expireTime, trim(command.remark)) != 1)
        {
            throw new ServiceException("外部库存预占单创建失败");
        }
        Map<String, Object> reservation = inventoryMapper.selectReservationByKeyForUpdate(tenantId, memberId,
                idempotencyKey);
        if (reservation == null)
        {
            throw new ServiceException("外部库存预占单创建失败");
        }
        Long reservationId = number(reservation, "reservationId");
        if (command.productId != null)
        {
            reserveInventory(tenantId, memberId, command.productId, command.warehouseId, quantity, reservationId, null);
        }
        else
        {
            int reservedItems = 0;
            for (Map<String, Object> item : packageItems)
            {
                if (!Integer.valueOf(1).equals(item.get("requiredFlag")))
                {
                    continue;
                }
                Long productId = number(item, "productId");
                if (productId == null)
                {
                    productId = number(item, "accessoryItemProductId");
                }
                if (productId == null)
                {
                    throw new ServiceException("套餐配件尚未绑定商户商品，无法预占库存");
                }
                reserveInventory(tenantId, memberId, productId, command.warehouseId,
                        positive(item.get("quantity")) * quantity, reservationId, null);
                reservedItems++;
            }
            if (reservedItems == 0)
            {
                throw new ServiceException("套餐没有可预占的必选库存明细");
            }
        }
        return toExternalView(reservation, command, false);
    }

    /** 释放外部平台创建的库存预占，只允许原令牌操作自己的预占。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantExternalInventoryReservationView releaseExternal(Long tenantId, Long memberId, Long tokenId,
            String reservationNo)
    {
        String normalizedReservationNo = requiredExternalText(reservationNo, "预占单号", 64);
        Map<String, Object> reservation = inventoryMapper.selectReservationByNoForUpdate(tenantId, memberId,
                normalizedReservationNo);
        if (reservation == null
                || !Integer.valueOf(MerchantInventoryReservationSource.EXTERNAL.getCode())
                        .equals(intValue(reservation.get("reservationSource")))
                || !tokenId.equals(number(reservation, "externalTokenId")))
        {
            throw new ServiceException("外部库存预占不存在或不属于当前令牌");
        }
        if (Integer.valueOf(RELEASED).equals(intValue(reservation.get("reservationStatus"))))
        {
            return toExternalView(reservation, null, true);
        }
        if (!Integer.valueOf(RESERVED).equals(intValue(reservation.get("reservationStatus"))))
        {
            throw new ServiceException("当前外部库存预占状态不允许释放");
        }
        changeForScope(tenantId, memberId, number(reservation, "reservationId"), RELEASED,
                "外部平台释放库存预占", null);
        Map<String, Object> released = inventoryMapper.selectReservationForUpdate(tenantId, memberId,
                number(reservation, "reservationId"));
        return toExternalView(released, null, false);
    }

    /**
     * 将外部预占绑定到正式订单。绑定不会提前消耗库存，后续仍由发货/核销工作流消费或释放。
     *
     * @return 已绑定的订单唯一编号；重复调用返回原订单唯一编号
     */
    @Transactional(rollbackFor = Exception.class)
    public Long bindExternalReservationOrder(Long tenantId, Long memberId, Long tokenId, String reservationNo,
            String externalOrderNo, Long orderId)
    {
        if (tenantId == null || memberId == null || tokenId == null || orderId == null
                || blank(reservationNo) || blank(externalOrderNo))
        {
            throw new ServiceException("外部预占订单绑定参数不完整");
        }
        Map<String, Object> reservation = inventoryMapper.selectReservationByNoForUpdate(tenantId, memberId,
                reservationNo.trim());
        if (reservation == null
                || !Integer.valueOf(MerchantInventoryReservationSource.EXTERNAL.getCode())
                        .equals(intValue(reservation.get("reservationSource")))
                || !tokenId.equals(number(reservation, "externalTokenId"))
                || !externalOrderNo.trim().equals(string(reservation, "externalOrderNo")))
        {
            throw new ServiceException("外部库存预占不存在或不属于当前令牌");
        }
        Long boundOrderId = number(reservation, "orderId");
        if (boundOrderId != null)
        {
            if (!boundOrderId.equals(orderId))
            {
                throw new ServiceException("外部库存预占已经绑定其他订单");
            }
            return boundOrderId;
        }
        if (!Integer.valueOf(MerchantInventoryReservationStatus.RESERVED.getCode())
                .equals(intValue(reservation.get("reservationStatus"))))
        {
            throw new ServiceException("只有已预占状态的库存可以绑定正式订单");
        }
        if (inventoryMapper.bindExternalReservationOrder(tenantId, memberId, number(reservation, "reservationId"),
                tokenId, externalOrderNo.trim(), orderId) != 1)
        {
            throw new ServiceException("外部库存预占绑定订单失败，请刷新后重试");
        }
        return orderId;
    }

    /** 查询并锁定可创建正式订单的外部预占，调用方必须在同一事务内完成订单绑定。 */
    public Map<String, Object> requireExternalReservationForOrder(Long tenantId, Long memberId, Long tokenId,
            String reservationNo, String externalOrderNo)
    {
        if (tenantId == null || memberId == null || tokenId == null || blank(reservationNo)
                || blank(externalOrderNo))
        {
            throw new ServiceException("外部预占订单参数不完整");
        }
        Map<String, Object> reservation = inventoryMapper.selectReservationByNoForUpdate(tenantId, memberId,
                reservationNo.trim());
        if (reservation == null
                || !Integer.valueOf(MerchantInventoryReservationSource.EXTERNAL.getCode())
                        .equals(intValue(reservation.get("reservationSource")))
                || !tokenId.equals(number(reservation, "externalTokenId"))
                || !externalOrderNo.trim().equals(string(reservation, "externalOrderNo")))
        {
            throw new ServiceException("外部库存预占不存在或不属于当前令牌");
        }
        if (number(reservation, "orderId") != null)
        {
            return reservation;
        }
        if (!Integer.valueOf(MerchantInventoryReservationStatus.RESERVED.getCode())
                .equals(intValue(reservation.get("reservationStatus"))))
        {
            throw new ServiceException("只有已预占状态的库存可以创建正式订单");
        }
        return reservation;
    }

    /** 批量释放已到期的外部库存预占，恢复逐件和数量库存的可用额度。 */
    @Transactional(rollbackFor = Exception.class)
    public int expireExternalReservations()
    {
        List<Map<String, Object>> expiredReservations = inventoryMapper
                .selectExpiredExternalReservationsForUpdate(MAX_EXTERNAL_EXPIRY_BATCH_SIZE);
        int expiredCount = 0;
        for (Map<String, Object> reservation : expiredReservations)
        {
            if (!Integer.valueOf(MerchantInventoryReservationSource.EXTERNAL.getCode())
                    .equals(intValue(reservation.get("reservationSource")))
                    || !Integer.valueOf(MerchantInventoryReservationStatus.RESERVED.getCode())
                            .equals(intValue(reservation.get("reservationStatus"))))
            {
                continue;
            }
            Long tenantId = number(reservation, "tenantId");
            Long memberId = number(reservation, "memberId");
            Long reservationId = number(reservation, "reservationId");
            if (tenantId == null || memberId == null || reservationId == null)
            {
                throw new ServiceException("外部库存预占缺少租户、商户或预占唯一编号");
            }
            changeForScope(tenantId, memberId, reservationId,
                    MerchantInventoryReservationStatus.EXPIRED.getCode(), "外部库存预占到期自动释放", null);
            expiredCount++;
        }
        return expiredCount;
    }

    /** 查询当前商户的预占单。 */
    @Transactional(readOnly = true)
    public InventoryReservationVO get(Long reservationId)
    {
        Map<String, Object> reservation = inventoryMapper.selectReservationForUpdate(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), reservationId);
        if (reservation == null)
        {
            throw new ServiceException("库存预占不存在或已删除");
        }
        List<Map<String, Object>> items = inventoryMapper.selectReservationUnits(reservationId);
        items.addAll(inventoryMapper.selectReservationQuantities(reservationId));
        reservation.put("units", items);
        return toView(reservation);
    }

    /** 按业务预占单号查询，避免商户页面直接输入技术主键。 */
    @Transactional(rollbackFor = Exception.class)
    public InventoryReservationVO getByNo(String reservationNo)
    {
        Map<String, Object> reservation = findByNo(reservationNo, false);
        if (reservation == null)
        {
            throw new ServiceException("预占单不存在或已删除");
        }
        List<Map<String, Object>> items = inventoryMapper.selectReservationUnits(number(reservation, "reservationId"));
        items.addAll(inventoryMapper.selectReservationQuantities(number(reservation, "reservationId")));
        reservation.put("units", items);
        return toView(reservation);
    }

    /** 释放预占，恢复序列号为可用。 */
    @Transactional(rollbackFor = Exception.class)
    public InventoryReservationVO change(Long reservationId, int targetStatus, String reason)
    {
        MerchantContextUtils.requireMerchantApproved();
        return changeForScope(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), reservationId,
                targetStatus, reason, SecurityUtils.getUserId());
    }

    private InventoryReservationVO changeForScope(Long tenantId, Long memberId, Long reservationId, int targetStatus,
            String reason, Long operator)
    {
        Map<String, Object> reservation = inventoryMapper.selectReservationForUpdate(tenantId, memberId, reservationId);
        if (reservation == null)
        {
            throw new ServiceException("库存预占不存在或已删除");
        }
        if (Integer.valueOf(targetStatus).equals(reservation.get("reservationStatus")))
        {
            List<Map<String, Object>> items = inventoryMapper.selectReservationUnits(reservationId);
            items.addAll(inventoryMapper.selectReservationQuantities(reservationId));
            reservation.put("units", items);
            return toView(reservation);
        }
        if (!Integer.valueOf(1).equals(reservation.get("reservationStatus")))
        {
            throw new ServiceException("只有已预占状态的库存可以释放或消耗");
        }
        List<Map<String, Object>> units = inventoryMapper.selectReservationUnits(reservationId);
        int unitTarget = targetStatus == CONSUMED ? 3 : AVAILABLE;
        for (Map<String, Object> unit : units)
        {
            if (inventoryMapper.updateUnitStatus(tenantId, memberId, number(unit, "unitId"), RESERVED, unitTarget,
                    operator) != 1)
            {
                throw new ServiceException("序列号库存状态已变化，请刷新后重试");
            }
            unit.put("unitStatus", targetStatus);
        }
        List<Map<String, Object>> quantities = inventoryMapper.selectReservationQuantities(reservationId);
        for (Map<String, Object> quantity : quantities)
        {
            int amount = intValue(quantity.get("quantity"));
            int changed = targetStatus == CONSUMED
                    ? inventoryMapper.consumeQuantity(tenantId, memberId, number(quantity, "inventoryId"), amount, operator)
                    : inventoryMapper.releaseQuantity(tenantId, memberId, number(quantity, "inventoryId"), amount, operator);
            if (changed != 1)
            {
                throw new ServiceException("数量库存状态已变化，请刷新后重试");
            }
        }
        inventoryMapper.updateReservationUnitStatus(reservationId, 1, targetStatus, operator);
        inventoryMapper.updateReservationQuantityStatus(reservationId, 1, targetStatus, operator);
        inventoryMapper.updateReservationStatus(tenantId, memberId, reservationId, 1, targetStatus, operator, reason);
        reservation.put("reservationStatus", targetStatus);
        units.addAll(quantities);
        reservation.put("units", units);
        return toView(reservation);
    }

    /** 按业务预占单号执行生命周期动作，主键仅保留在服务内部使用。 */
    @Transactional(rollbackFor = Exception.class)
    public InventoryReservationVO changeByNo(String reservationNo, int targetStatus, String reason)
    {
        Map<String, Object> reservation = findByNo(reservationNo, true);
        if (reservation == null)
        {
            throw new ServiceException("预占单不存在或已删除");
        }
        return change(number(reservation, "reservationId"), targetStatus, reason);
    }

    /** 释放订单当前仍生效的全部预占，换仓时由外层事务继续在新仓库重建预占。 */
    @Transactional(rollbackFor = Exception.class)
    public void releaseActiveByOrder(Long orderId, String reason)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (orderId == null)
        {
            throw new ServiceException("订单唯一编号不能为空");
        }
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        List<Map<String, Object>> reservations = inventoryMapper.selectActiveReservationsByOrderForUpdate(tenantId,
                memberId, orderId);
        for (Map<String, Object> reservation : reservations)
        {
            change(number(reservation, "reservationId"), 2, reason);
        }
    }

    /**
     * 消耗订单预占；逐件库存只消耗扫码选中的设备，未选中的预占设备释放回可用状态。
     *
     * <p>一个订单的预占记录可能同时包含逐件设备和数量库存，不能简单地把整张预占单
     * 直接切换为消耗，否则扫描出库时会把未绑定的设备一并标记为租赁中。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public void consumeActiveByOrder(Long orderId, Set<Long> selectedUnitIds)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (orderId == null)
        {
            throw new ServiceException("订单唯一编号不能为空");
        }
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        Long operator = SecurityUtils.getUserId();
        Set<Long> selected = selectedUnitIds == null ? new HashSet<Long>() : new HashSet<Long>(selectedUnitIds);
        List<Map<String, Object>> reservations = inventoryMapper.selectActiveReservationsByOrderForUpdate(tenantId,
                memberId, orderId);
        Set<Long> inventoryIds = new HashSet<Long>();
        for (Map<String, Object> reservation : reservations)
        {
            Long reservationId = number(reservation, "reservationId");
            List<Map<String, Object>> units = inventoryMapper.selectReservationUnits(reservationId);
            for (Map<String, Object> unit : units)
            {
                Long unitId = number(unit, "unitId");
                boolean consume = selected.isEmpty() || selected.contains(unitId);
                int targetUnitStatus = consume ? 3 : AVAILABLE;
                int targetReservationStatus = consume ? CONSUMED : RELEASED;
                if (inventoryMapper.updateUnitStatus(tenantId, memberId, unitId, RESERVED,
                        targetUnitStatus, operator) != 1)
                {
                    throw new ServiceException("预占设备状态已变化，请刷新后重试");
                }
                if (inventoryMapper.updateReservationUnitStatusById(number(unit, "reservationUnitId"), 1,
                        targetReservationStatus, operator) != 1)
                {
                    throw new ServiceException("订单预占明细状态已变化，请刷新后重试");
                }
                inventoryIds.add(number(unit, "inventoryId"));
            }
            List<Map<String, Object>> quantities = inventoryMapper.selectReservationQuantities(reservationId);
            for (Map<String, Object> quantity : quantities)
            {
                int amount = intValue(quantity.get("quantity"));
                if (inventoryMapper.consumeQuantity(tenantId, memberId, number(quantity, "inventoryId"), amount,
                        operator) != 1)
                {
                    throw new ServiceException("数量库存状态已变化，请刷新后重试");
                }
            }
            if (!quantities.isEmpty()
                    && inventoryMapper.updateReservationQuantityStatus(reservationId, 1, CONSUMED, operator) == 0)
            {
                throw new ServiceException("订单数量预占状态已变化，请刷新后重试");
            }
            if (inventoryMapper.updateReservationStatus(tenantId, memberId, reservationId, 1, CONSUMED,
                    operator, "核销台完成订单出库") != 1)
            {
                throw new ServiceException("订单预占状态更新失败，请刷新后重试");
            }
        }
        for (Long inventoryId : inventoryIds)
        {
            if (inventoryId != null)
            {
                inventoryMapper.refreshInventoryCounts(inventoryId, operator);
            }
        }
    }

    private Map<String, Object> findByNo(String reservationNo, boolean requireApproved)
    {
        if (requireApproved)
        {
            MerchantContextUtils.requireMerchantApproved();
        }
        if (blank(reservationNo))
        {
            throw new ServiceException("预占单号不能为空");
        }
        return inventoryMapper.selectReservationByNoForUpdate(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), reservationNo.trim());
    }

    /** 按库存模式预占库存：序列号逐件锁定，数量库存原子扣减可用量。 */
    private void reserveInventory(Long tenantId, Long memberId, Long productId, Long warehouseId, int quantity,
            Long reservationId, Long operator)
    {
        Map<String, Object> inventory = inventoryMapper.selectInventoryForUpdate(tenantId, memberId, productId,
                warehouseId);
        if (inventory == null)
        {
            throw new ServiceException("所选商品在所选仓库没有可用库存");
        }
        if (Integer.valueOf(QUANTITY_STOCK_MODE).equals(inventory.get("stockMode")))
        {
            if (inventoryMapper.reserveQuantity(tenantId, memberId, number(inventory, "inventoryId"), quantity,
                    operator) != 1)
            {
                throw new ServiceException("所选商品可用数量库存不足");
            }
            inventoryMapper.insertReservationQuantity(reservationId, number(inventory, "inventoryId"), productId,
                    quantity, operator);
            return;
        }
        List<Map<String, Object>> units = inventoryMapper.selectAvailableUnitsForUpdate(tenantId, memberId,
                productId, warehouseId, quantity);
        if (units == null || units.size() != quantity)
        {
            throw new ServiceException("所选商品可用序列号库存不足");
        }
        for (Map<String, Object> unit : units)
        {
            Long unitId = number(unit, "unitId");
            if (inventoryMapper.updateUnitStatus(tenantId, memberId, unitId, AVAILABLE, RESERVED, operator) != 1)
            {
                throw new ServiceException("序列号库存状态已变化，请刷新后重试");
            }
            inventoryMapper.insertReservationUnit(reservationId, number(unit, "inventoryId"), unitId, operator);
        }
    }

    /** 在新库存单元模型上执行状态迁移。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean transition(InventoryTransitionCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null || (command.unitId == null && blank(command.unitCode)))
        {
            throw new ServiceException("新库存状态迁移必须指定库存单元唯一编号或序列号");
        }
        if (command.snId != null || !blank(command.snCode) || command.productId != null)
        {
            throw new ServiceException("旧 SN 或数量库存接口已停用，请使用新库存单元唯一编号");
        }
        if (command.fromStatus == null || command.toStatus == null || command.fromStatus.equals(command.toStatus)
                || command.fromStatus < 1 || command.fromStatus > 6 || command.toStatus < 1 || command.toStatus > 6)
        {
            throw new ServiceException("库存状态不合法或原状态与目标状态相同");
        }
        Map<String, Object> unit = inventoryMapper.selectUnitForUpdate(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), command.unitId, trim(command.unitCode));
        if (unit == null)
        {
            throw new ServiceException("库存单元不存在或已删除");
        }
        MerchantContextUtils.checkWarehouseAccess(number(unit, "warehouseId"));
        if (!Integer.valueOf(command.fromStatus).equals(unit.get("unitStatus")))
        {
            throw new ServiceException("库存单元当前状态已变化，请刷新后重试");
        }
        if (inventoryMapper.updateUnitStatus(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                number(unit, "unitId"), command.fromStatus, command.toStatus, SecurityUtils.getUserId()) != 1)
        {
            throw new ServiceException("库存单元状态更新失败，请刷新后重试");
        }
        inventoryMapper.refreshInventoryCounts(number(unit, "inventoryId"), SecurityUtils.getUserId());
        return true;
    }

    private InventoryReservationVO toView(Map<String, Object> row)
    {
        InventoryReservationVO view = new InventoryReservationVO();
        view.reservationId = number(row, "reservationId");
        view.orderId = number(row, "orderId");
        view.packageId = number(row, "packageId");
        view.warehouseId = number(row, "warehouseId");
        view.packageVersionId = number(row, "packageVersionId");
        view.reservationNo = (String) row.get("reservationNo");
        view.reservationStatus = intValue(row.get("reservationStatus"));
        view.idempotencyKey = (String) row.get("idempotencyKey");
        Object rawUnits = row.get("units");
        if (rawUnits instanceof List)
        {
            for (Object raw : (List<?>) rawUnits)
            {
                if (raw instanceof Map)
                {
                    Map<?, ?> unit = (Map<?, ?>) raw;
                    InventoryReservationVO.InventoryReservationItemVO item = new InventoryReservationVO.InventoryReservationItemVO();
                    item.inventoryId = number(unit, "inventoryId");
                    item.unitId = number(unit, "unitId");
                    item.quantity = intValue(unit.get("quantity"));
                    item.unitStatus = intValue(unit.get("unitStatus"));
                    view.items.add(item);
                }
            }
        }
        return view;
    }

    private MerchantExternalInventoryReservationView toExternalView(Map<String, Object> row,
            MerchantExternalInventoryReservationCommand command, boolean replayed)
    {
        MerchantExternalInventoryReservationView view = new MerchantExternalInventoryReservationView();
        view.externalOrderNo = string(row, "externalOrderNo");
        view.reservationNo = string(row, "reservationNo");
        view.packageId = number(row, "packageId");
        view.productId = command == null ? number(row, "externalProductId") : command.productId;
        view.warehouseId = number(row, "warehouseId");
        view.quantity = command == null ? intValue(row.get("externalQuantity"))
                : (command.quantity == null ? 1 : command.quantity);
        view.reservationStatus = intValue(row.get("reservationStatus"));
        MerchantInventoryReservationStatus status = MerchantInventoryReservationStatus.fromCode(view.reservationStatus);
        view.reservationStatusLabel = status == null ? "未知" : status.getLabel();
        Object expireTime = row.get("expireTime");
        if (expireTime instanceof LocalDateTime)
        {
            view.expireTime = (LocalDateTime) expireTime;
        }
        view.replayed = replayed;
        return view;
    }

    private static Long number(Map<?, ?> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        return value instanceof Number ? ((Number) value).longValue() : null;
    }

    private static String string(Map<?, ?> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private static int intValue(Object value)
    {
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    private static int positive(Object value)
    {
        int result = intValue(value);
        if (result <= 0)
        {
            throw new ServiceException("套餐明细数量必须大于零");
        }
        return result;
    }

    private static boolean blank(String value)
    {
        return value == null || value.trim().isEmpty();
    }

    private static String trim(String value)
    {
        return blank(value) ? null : value.trim();
    }

    private static String requiredExternalText(String value, String fieldName, int maxLength)
    {
        String normalized = trim(value);
        if (normalized == null || normalized.length() > maxLength)
        {
            throw new ServiceException(fieldName + "不能为空且不能超过" + maxLength + "个字符");
        }
        return normalized;
    }

    private static String externalIdempotencyKey(Long tokenId, String requestId)
    {
        return "ext:" + tokenId + ":" + sha256(requestId);
    }

    private static String sha256(String value)
    {
        try
        {
            return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        }
        catch (java.security.NoSuchAlgorithmException exception)
        {
            throw new IllegalStateException("SHA-256不可用", exception);
        }
    }
}
