package com.ruoyi.rentalbusiness.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.log.service.MerchantBusinessAuditRecorder;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.rentalbusiness.mapper.RentalWorkflowMapper;
import com.ruoyi.rentalbusiness.model.InventoryReservationCommand;
import com.ruoyi.rentalbusiness.model.InventoryReservationVO;
import com.ruoyi.rentalbusiness.model.InventoryTransitionCommand;

/**
 * 库存状态机与预占应用服务。
 *
 * <p>所有涉及共享资源池的操作先锁定 tenant/member/pool/warehouse 组合行，再计算可用量，
 * 从而保证 Pocket 3/Pocket 4 共用配件时只扣一次容量。状态变更严格校验允许边，预占、释放、
 * 消耗和状态机变更都要求幂等键，并通过唯一索引与幂等表共同防止重复执行。</p>
 */
@Service
public class InventoryWorkflowService
{
    private static final Set<String> STATES = new HashSet<String>(Arrays.asList(
            "available", "reserved", "rented", "repairing", "lost", "retired"));

    private static final Map<String, Set<String>> TRANSITIONS = transitions();

    private final RentalWorkflowMapper mapper;
    private final CodeGeneratorClient codeGeneratorClient;
    private final MerchantBusinessAuditRecorder auditRecorder;

    /**
     * 注入库存工作流 Mapper、统一编码客户端和商户业务审计记录器。
     *
     * @param mapper 库存工作流数据访问对象
     * @param codeGeneratorClient 统一编码生成客户端
     * @param auditRecorder 商户业务审计记录器
     */
    @Autowired
    public InventoryWorkflowService(RentalWorkflowMapper mapper, CodeGeneratorClient codeGeneratorClient,
            MerchantBusinessAuditRecorder auditRecorder)
    {
        this.mapper = mapper;
        this.codeGeneratorClient = codeGeneratorClient;
        this.auditRecorder = auditRecorder;
    }

    /**
     * 保留无审计记录器构造方法，便于已有单元测试和离线工具构造库存服务。
     *
     * @param mapper 库存工作流数据访问对象
     * @param codeGeneratorClient 统一编码生成客户端
     */
    public InventoryWorkflowService(RentalWorkflowMapper mapper, CodeGeneratorClient codeGeneratorClient)
    {
        this(mapper, codeGeneratorClient, null);
    }

    /**
     * 按套餐 BOM 预占一个仓库内的全部资源池容量。
     *
     * @param command 预占参数
     * @return 预占主记录及资源池明细
     */
    @Transactional(rollbackFor = Exception.class)
    public InventoryReservationVO reserve(InventoryReservationCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        Scope scope = scope();
        Long packageId = required(command == null ? null : command.packageId, "套餐ID不能为空");
        Long warehouseId = required(command == null ? null : command.warehouseId, "仓库ID不能为空");
        int quantity = positive(command == null ? null : command.quantity, "预占数量必须大于零");
        String key = required(command == null ? null : command.idempotencyKey, "预占幂等键不能为空");
        MerchantContextUtils.checkWarehouseAccess(warehouseId);
        boolean completed = beginIdempotency(scope, "inventory-reserve", key, reservationHash(command));
        Map<String, Object> existing = mapper.selectReservationByKeyForUpdate(scope.tenantId, scope.memberId, key);
        if (existing != null)
        {
            Long existingId = longValue(existing, "reservationId");
            mapper.completeIdempotent(scope.tenantId, scope.memberId, "inventory-reserve", key, existingId);
            return toReservation(existing, mapper.selectReservationItems(scope.tenantId, scope.memberId, existingId));
        }
        if (completed)
        {
            throw new ServiceException("幂等请求已完成，但库存预占记录不存在");
        }
        List<Map<String, Object>> components = mapper.selectActivePackageComponents(scope.tenantId, scope.memberId, packageId);
        if (components == null || components.isEmpty())
        {
            throw new ServiceException("套餐没有可用的生效 BOM");
        }
        components = new ArrayList<Map<String, Object>>(components);
        Collections.sort(components, (left, right) -> Long.compare(longValue(left, "poolId"), longValue(right, "poolId")));
        for (Map<String, Object> component : components)
        {
            Long poolId = longValue(component, "poolId");
            int requiredQty = safeInt(component, "requiredQty") * quantity;
            if (requiredQty <= 0)
            {
                throw new ServiceException("套餐资源池需求数量必须大于零");
            }
            lockPool(scope, poolId, warehouseId);
            int available = mapper.selectPoolAvailable(scope.tenantId, scope.memberId, poolId, warehouseId);
            int reserved = mapper.selectPoolReserved(scope.tenantId, scope.memberId, poolId, warehouseId);
            if (available - reserved < requiredQty)
            {
                throw new ServiceException("资源池 " + poolId + " 库存不足，无法预占");
            }
        }
        String reservationNo = codeGeneratorClient.next("inventory_reservation");
        mapper.insertReservation(scope.tenantId, scope.memberId, reservationNo,
                command.orderId, packageId, warehouseId, quantity, key, operator());
        Long reservationId = selectLastReservationId(scope, key);
        for (Map<String, Object> component : components)
        {
            Long poolId = longValue(component, "poolId");
            int requiredQty = safeInt(component, "requiredQty") * quantity;
            mapper.insertReservationItem(scope.tenantId, scope.memberId, reservationId, poolId, warehouseId,
                    requiredQty, operator());
            mapper.insertInventoryLedger(scope.tenantId, scope.memberId, warehouseId, null, "reserve", null,
                    "reserved", requiredQty, "reservation", String.valueOf(reservationId), command.reason, operator());
        }
        mapper.completeIdempotent(scope.tenantId, scope.memberId, "inventory-reserve", key, reservationId);
        InventoryReservationVO result = getReservation(reservationId);
        audit("库存管理", "CREATE", "INVENTORY_RESERVATION", String.valueOf(reservationId),
                "库存预占", "预占库存，预占ID：" + reservationId, null, result);
        return result;
    }

    /** 释放仍处于 reserved 状态的预占，释放后可再次接单。 */
    @Transactional(rollbackFor = Exception.class)
    public InventoryReservationVO release(Long reservationId, String reason, String idempotencyKey)
    {
        return changeReservationStatus(reservationId, "released", reason, idempotencyKey);
    }

    /** 将预占转为消耗状态，并从对应资源池的真实库存中扣减。 */
    @Transactional(rollbackFor = Exception.class)
    public InventoryReservationVO consume(Long reservationId, String reason, String idempotencyKey)
    {
        MerchantContextUtils.requireMerchantApproved();
        Scope scope = scope();
        String key = required(idempotencyKey, "消耗幂等键不能为空");
        Map<String, Object> reservation = mapper.selectReservationForUpdate(scope.tenantId, scope.memberId, reservationId);
        if (reservation == null)
        {
            throw new ServiceException("库存预占不存在或已删除");
        }
        String current = stringValue(reservation, "reservationStatus");
        if ("consumed".equals(current))
        {
            return getReservation(reservationId);
        }
        if (!"reserved".equals(current))
        {
            throw new ServiceException("只有 reserved 状态的预占可以消耗");
        }
        if (beginIdempotency(scope, "inventory-consume", key, reservationId + ":consume"))
        {
            return getReservation(reservationId);
        }
        List<Map<String, Object>> items = mapper.selectReservationItems(scope.tenantId, scope.memberId, reservationId);
        InventoryReservationVO before = toReservation(reservation, items);
        for (Map<String, Object> item : items)
        {
            Long poolId = longValue(item, "poolId");
            Long warehouseId = longValue(item, "warehouseId");
            int requiredQty = safeInt(item, "requiredQty");
            lockPool(scope, poolId, warehouseId);
            consumePool(scope, poolId, warehouseId, requiredQty, reservationId, reason);
        }
        mapper.updateReservationItemStatus(scope.tenantId, scope.memberId, reservationId, "reserved", "consumed", operator());
        mapper.updateReservationStatus(scope.tenantId, scope.memberId, reservationId, "reserved", "consumed", reason, operator());
        mapper.completeIdempotent(scope.tenantId, scope.memberId, "inventory-consume", key, reservationId);
        InventoryReservationVO result = getReservation(reservationId);
        audit("库存管理", "UPDATE", "INVENTORY_RESERVATION", String.valueOf(reservationId),
                "库存预占", "消耗库存预占，预占ID：" + reservationId, before, result);
        return result;
    }

    /** 将预占标记为过期，过期和主动释放共用同一资源释放逻辑。 */
    @Transactional(rollbackFor = Exception.class)
    public InventoryReservationVO expire(Long reservationId, String idempotencyKey)
    {
        MerchantContextUtils.requireMerchantApproved();
        Scope scope = scope();
        String key = required(idempotencyKey, "过期处理幂等键不能为空");
        Map<String, Object> reservation = mapper.selectReservationForUpdate(scope.tenantId, scope.memberId, reservationId);
        if (reservation == null)
        {
            throw new ServiceException("库存预占不存在或已删除");
        }
        if ("expired".equals(stringValue(reservation, "reservationStatus")))
        {
            return getReservation(reservationId);
        }
        if (!"reserved".equals(stringValue(reservation, "reservationStatus")))
        {
            throw new ServiceException("只有 reserved 状态的预占可以过期释放");
        }
        if (beginIdempotency(scope, "inventory-expire", key, reservationId + ":expire"))
        {
            return getReservation(reservationId);
        }
        InventoryReservationVO before = toReservation(reservation,
                mapper.selectReservationItems(scope.tenantId, scope.memberId, reservationId));
        mapper.updateReservationItemStatus(scope.tenantId, scope.memberId, reservationId, "reserved", "expired", operator());
        mapper.updateReservationStatus(scope.tenantId, scope.memberId, reservationId, "reserved", "expired", "预占超时自动释放", operator());
        mapper.completeIdempotent(scope.tenantId, scope.memberId, "inventory-expire", key, reservationId);
        InventoryReservationVO result = getReservation(reservationId);
        audit("库存管理", "UPDATE", "INVENTORY_RESERVATION", String.valueOf(reservationId),
                "库存预占", "预占超时释放，预占ID：" + reservationId, before, result);
        return result;
    }

    /** 查询当前商户预占详情。 */
    @Transactional(readOnly = true)
    public InventoryReservationVO getReservation(Long reservationId)
    {
        Scope scope = scope();
        Map<String, Object> reservation = mapper.selectReservationForUpdate(scope.tenantId, scope.memberId, reservationId);
        if (reservation == null)
        {
            throw new ServiceException("库存预占不存在或已删除");
        }
        return toReservation(reservation, mapper.selectReservationItems(scope.tenantId, scope.memberId, reservationId));
    }

    /**
     * 执行数量库存或逐件库存状态迁移。
     *
     * @param command 状态迁移参数
     * @return 成功结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean transition(InventoryTransitionCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        Scope scope = scope();
        String from = required(command == null ? null : command.fromStatus, "原库存状态不能为空");
        String to = required(command == null ? null : command.toStatus, "目标库存状态不能为空");
        String key = required(command == null ? null : command.idempotencyKey, "状态迁移幂等键不能为空");
        validateTransition(from, to);
        if (beginIdempotency(scope, "inventory-transition", key, transitionHash(command)))
        {
            return true;
        }
        if (command.unitId != null || !StringUtils.isBlank(command.unitCode))
        {
            Map<String, Object> unit = mapper.selectInventoryUnitForUpdate(scope.tenantId, scope.memberId,
                    command.unitId, trim(command.unitCode));
            if (unit == null)
            {
                throw new ServiceException("逐件库存单元不存在或已删除");
            }
            MerchantContextUtils.checkWarehouseAccess(longValue(unit, "warehouseId"));
            ensureState(unit.get("unitStatus"), from);
            mapper.updateInventoryUnitStatus(scope.tenantId, scope.memberId, longValue(unit, "unitId"), from, to, operator());
            mapper.insertInventoryTransition(scope.tenantId, scope.memberId, longValue(unit, "productId"),
                    longValue(unit, "unitId"), longValue(unit, "warehouseId"), null, from, to, 1, key, command.reason,
                    SecurityUtils.getUserId(), operator());
            mapper.insertInventoryLedger(scope.tenantId, scope.memberId, longValue(unit, "warehouseId"),
                    longValue(unit, "productId"), "state-transition", from, to, 1, "inventory-unit",
                    String.valueOf(unit.get("unitId")), command.reason, operator());
            mapper.completeIdempotent(scope.tenantId, scope.memberId, "inventory-transition", key,
                    longValue(unit, "unitId"));
            Map<String, Object> after = new HashMap<String, Object>(unit);
            after.put("unitStatus", to);
            audit("库存管理", "UPDATE", "INVENTORY_UNIT", String.valueOf(unit.get("unitId")),
                    stringValue(unit, "unitCode"), "逐件库存状态迁移：" + from + " -> " + to, unit, after);
            return true;
        }
        if (command.snId != null || !StringUtils.isBlank(command.snCode))
        {
            Map<String, Object> sn = mapper.selectAssetSnForUpdate(scope.tenantId, scope.memberId,
                    command.snId, trim(command.snCode));
            if (sn == null)
            {
                throw new ServiceException("SN 实例不存在或已删除");
            }
            MerchantContextUtils.checkWarehouseAccess(longValue(sn, "warehouseId"));
            ensureState(sn.get("assetStatus"), from);
            if (mapper.updateAssetSnStatus(scope.tenantId, scope.memberId, longValue(sn, "snId"), from, to,
                    operator()) != 1)
            {
                throw new ServiceException("SN 状态已变化，请刷新后重试");
            }
            mapper.insertInventoryTransition(scope.tenantId, scope.memberId, longValue(sn, "productId"), null,
                    longValue(sn, "warehouseId"), longValue(sn, "snId"), from, to, 1, key, command.reason,
                    SecurityUtils.getUserId(), operator());
            mapper.insertInventoryLedger(scope.tenantId, scope.memberId, longValue(sn, "warehouseId"),
                    longValue(sn, "productId"), "state-transition", from, to, 1, "asset-sn",
                    String.valueOf(sn.get("snId")), command.reason, operator());
            mapper.completeIdempotent(scope.tenantId, scope.memberId, "inventory-transition", key,
                    longValue(sn, "snId"));
            Map<String, Object> after = new HashMap<String, Object>(sn);
            after.put("assetStatus", to);
            audit("库存管理", "UPDATE", "ASSET_SN", String.valueOf(sn.get("snId")),
                    stringValue(sn, "snCode"), "SN 状态迁移：" + from + " -> " + to, sn, after);
            return true;
        }
        Long productId = required(command == null ? null : command.productId, "商品ID不能为空");
        Long warehouseId = required(command == null ? null : command.warehouseId, "仓库ID不能为空");
        int quantity = positive(command.changeQty, "库存变更数量必须大于零");
        MerchantContextUtils.checkWarehouseAccess(warehouseId);
        if (from.equals(to))
        {
            throw new ServiceException("原状态和目标状态不能相同");
        }
        Map<String, Object> source = mapper.selectBalanceForUpdate(scope.tenantId, scope.memberId, productId, warehouseId, from);
        int sourceQty = source == null ? 0 : safeInt(source, "quantity");
        if (sourceQty < quantity)
        {
            throw new ServiceException("原状态库存不足，无法完成状态迁移");
        }
        mapper.updateBalance(scope.tenantId, scope.memberId, longValue(source, "id"), sourceQty - quantity,
                operator(), command.reason);
        Map<String, Object> destination = mapper.selectBalanceForUpdate(scope.tenantId, scope.memberId, productId, warehouseId, to);
        if (destination == null)
        {
            mapper.insertBalance(scope.tenantId, scope.memberId, productId, warehouseId, to, quantity, operator(), command.reason);
        }
        else
        {
            mapper.updateBalance(scope.tenantId, scope.memberId, longValue(destination, "id"),
                    safeInt(destination, "quantity") + quantity, operator(), command.reason);
        }
        mapper.insertInventoryTransition(scope.tenantId, scope.memberId, productId, null, warehouseId, null, from,
                to, quantity, key, command.reason, SecurityUtils.getUserId(), operator());
        mapper.insertInventoryLedger(scope.tenantId, scope.memberId, warehouseId, productId, "state-transition",
                from, to, quantity, "inventory-balance", String.valueOf(productId), command.reason, operator());
        mapper.completeIdempotent(scope.tenantId, scope.memberId, "inventory-transition", key, productId);
        Map<String, Object> before = new HashMap<String, Object>();
        before.put("productId", productId);
        before.put("warehouseId", warehouseId);
        before.put("status", from);
        before.put("quantity", quantity);
        Map<String, Object> after = new HashMap<String, Object>(before);
        after.put("status", to);
        audit("库存管理", "UPDATE", "INVENTORY_BALANCE", String.valueOf(productId),
                "数量库存", "数量库存状态迁移：" + from + " -> " + to, before, after);
        return true;
    }

    /** 执行预占状态变更的公共逻辑。 */
    private InventoryReservationVO changeReservationStatus(Long reservationId, String targetStatus,
            String reason, String idempotencyKey)
    {
        MerchantContextUtils.requireMerchantApproved();
        Scope scope = scope();
        String key = required(idempotencyKey, "释放幂等键不能为空");
        Map<String, Object> reservation = mapper.selectReservationForUpdate(scope.tenantId, scope.memberId, reservationId);
        if (reservation == null)
        {
            throw new ServiceException("库存预占不存在或已删除");
        }
        String current = stringValue(reservation, "reservationStatus");
        if (targetStatus.equals(current))
        {
            return getReservation(reservationId);
        }
        if (!"reserved".equals(current))
        {
            throw new ServiceException("只有 reserved 状态的预占可以释放");
        }
        if (beginIdempotency(scope, "inventory-" + targetStatus, key, reservationId + ":" + targetStatus))
        {
            return getReservation(reservationId);
        }
        InventoryReservationVO before = toReservation(reservation,
                mapper.selectReservationItems(scope.tenantId, scope.memberId, reservationId));
        mapper.updateReservationItemStatus(scope.tenantId, scope.memberId, reservationId, "reserved", targetStatus, operator());
        mapper.updateReservationStatus(scope.tenantId, scope.memberId, reservationId, "reserved", targetStatus, reason, operator());
        mapper.completeIdempotent(scope.tenantId, scope.memberId, "inventory-" + targetStatus, key, reservationId);
        InventoryReservationVO result = getReservation(reservationId);
        audit("库存管理", "UPDATE", "INVENTORY_RESERVATION", String.valueOf(reservationId),
                "库存预占", "库存预占状态变更：reserved -> " + targetStatus,
                before, result);
        return result;
    }

    /**
     * 按商户上下文记录库存业务变更，平台调用不会写入商户日志。
     *
     * @param module 业务模块
     * @param operationType 操作类型
     * @param targetType 目标类型
     * @param targetId 目标标识
     * @param targetName 目标名称
     * @param description 业务描述
     * @param before 变更前内容
     * @param after 变更后内容
     */
    private void audit(String module, String operationType, String targetType, String targetId,
            String targetName, String description, Object before, Object after)
    {
        if (auditRecorder != null)
        {
            auditRecorder.record(module, operationType, targetType, targetId, targetName, description, before, after);
        }
    }

    /** 消耗一个资源池中的数量库存和逐件库存。 */
    private void consumePool(Scope scope, Long poolId, Long warehouseId, int requiredQty,
            Long reservationId, String reason)
    {
        int remaining = requiredQty;
        for (Map<String, Object> stock : mapper.selectPoolStocksForUpdate(scope.tenantId, scope.memberId, poolId, warehouseId))
        {
            if (remaining <= 0)
            {
                break;
            }
            int used = Math.min(remaining, safeInt(stock, "quantity"));
            if ("quantity".equals(stringValue(stock, "stockMode")))
            {
                mapper.updateBalance(scope.tenantId, scope.memberId, longValue(stock, "balanceId"),
                        safeInt(stock, "quantity") - used, operator(), reason);
            }
            else
            {
                for (int index = 0; index < used; index++)
                {
                    mapper.updateAssetSnStatus(scope.tenantId, scope.memberId, longValue(stock, "snId"),
                            "available", "rented", operator());
                }
            }
            mapper.insertInventoryLedger(scope.tenantId, scope.memberId, warehouseId, longValue(stock, "productId"),
                    "consume", "reserved", "rented", used, "reservation", String.valueOf(reservationId), reason, operator());
            remaining -= used;
        }
        if (remaining > 0)
        {
            throw new ServiceException("资源池实际库存不足，预占无法消耗");
        }
    }

    /** 锁定资源池仓库组合。 */
    private void lockPool(Scope scope, Long poolId, Long warehouseId)
    {
        mapper.ensurePoolLock(scope.tenantId, scope.memberId, poolId, warehouseId);
        if (mapper.selectPoolLockForUpdate(scope.tenantId, scope.memberId, poolId, warehouseId) == null)
        {
            throw new ServiceException("库存并发锁创建失败");
        }
    }

    /** 创建或锁定幂等请求占位记录。 */
    private boolean beginIdempotency(Scope scope, String requestScope, String key, String requestHash)
    {
        String requestDigest = sha256(requestHash);
        mapper.insertIdempotentIgnore(scope.tenantId, scope.memberId, requestScope, key, requestDigest,
                new Date(System.currentTimeMillis() + 24L * 60L * 60L * 1000L));
        Map<String, Object> existing = mapper.selectIdempotentForUpdate(scope.tenantId, scope.memberId, requestScope, key);
        if (existing != null && !requestDigest.equals(stringValue(existing, "requestHash")))
        {
            throw new ServiceException("幂等键已被其他请求占用，请更换幂等键");
        }
        if (existing != null && "succeeded".equals(stringValue(existing, "requestStatus")))
        {
            return true;
        }
        return false;
    }

    /** 根据幂等键重新读取预占主记录的自增主键。 */
    private Long selectLastReservationId(Scope scope, String key)
    {
        Map<String, Object> reservation = mapper.selectReservationByKeyForUpdate(scope.tenantId, scope.memberId, key);
        if (reservation == null)
        {
            throw new ServiceException("库存预占创建失败");
        }
        return longValue(reservation, "reservationId");
    }

    /** 校验库存状态机允许的迁移边。 */
    private void validateTransition(String from, String to)
    {
        if (!STATES.contains(from) || !STATES.contains(to)
                || !TRANSITIONS.getOrDefault(from, java.util.Collections.<String>emptySet()).contains(to))
        {
            throw new ServiceException("库存状态迁移不合法: " + from + " -> " + to);
        }
    }

    /** 校验数据库当前状态和请求原状态一致。 */
    private void ensureState(Object actual, String expected)
    {
        if (!expected.equals(actual == null ? null : String.valueOf(actual)))
        {
            throw new ServiceException("库存状态已变化，请刷新后重试");
        }
    }

    /** 将数据库预占行转换为前端对象。 */
    private InventoryReservationVO toReservation(Map<String, Object> row, List<Map<String, Object>> items)
    {
        InventoryReservationVO view = new InventoryReservationVO();
        view.reservationId = longValue(row, "reservationId");
        view.orderId = longValue(row, "orderId");
        view.packageId = longValue(row, "packageId");
        view.warehouseId = longValue(row, "warehouseId");
        view.quantity = safeInt(row, "quantity");
        view.reservationStatus = stringValue(row, "reservationStatus");
        view.idempotencyKey = stringValue(row, "idempotencyKey");
        for (Map<String, Object> item : items)
        {
            InventoryReservationVO.InventoryReservationItemVO itemView = new InventoryReservationVO.InventoryReservationItemVO();
            itemView.poolId = longValue(item, "poolId");
            itemView.requiredQty = safeInt(item, "requiredQty");
            itemView.reservationStatus = stringValue(item, "reservationStatus");
            view.items.add(itemView);
        }
        return view;
    }

    /** 构造允许的状态迁移表。 */
    private static Map<String, Set<String>> transitions()
    {
        Map<String, Set<String>> result = new HashMap<String, Set<String>>();
        result.put("available", new HashSet<String>(Arrays.asList("reserved", "rented", "repairing", "lost")));
        result.put("reserved", new HashSet<String>(Arrays.asList("available", "rented")));
        result.put("rented", new HashSet<String>(Arrays.asList("available", "repairing", "lost")));
        result.put("repairing", new HashSet<String>(Arrays.asList("available", "retired")));
        result.put("lost", new HashSet<String>(Arrays.asList("available", "retired")));
        result.put("retired", new HashSet<String>());
        return result;
    }

    /** 获取当前商户范围。 */
    private Scope scope()
    {
        return new Scope(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId());
    }

    /** 校验必填主键。 */
    private Long required(Long value, String message)
    {
        if (value == null || value <= 0)
        {
            throw new ServiceException(message);
        }
        return value;
    }

    /** 校验正整数。 */
    private int positive(Integer value, String message)
    {
        if (value == null || value <= 0)
        {
            throw new ServiceException(message);
        }
        return value;
    }

    /** 校验必填文本。 */
    private String required(String value, String message)
    {
        if (StringUtils.isBlank(value))
        {
            throw new ServiceException(message);
        }
        return value.trim();
    }

    /** 生成预占请求哈希输入。 */
    private String reservationHash(InventoryReservationCommand command)
    {
        return String.valueOf(command.orderId) + ':' + command.packageId + ':' + command.warehouseId + ':' + command.quantity;
    }

    /** 生成状态迁移请求哈希输入。 */
    private String transitionHash(InventoryTransitionCommand command)
    {
        return String.valueOf(command.productId) + ':' + command.unitId + ':' + command.unitCode + ':'
                + command.warehouseId + ':' + command.fromStatus + ':' + command.toStatus + ':' + command.changeQty;
    }

    /** 生成 SHA-256 请求摘要，供幂等审计核对。 */
    private String sha256(String value)
    {
        try
        {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(64);
            for (byte item : digest)
            {
                result.append(String.format("%02x", item));
            }
            return result.toString();
        }
        catch (NoSuchAlgorithmException exception)
        {
            throw new IllegalStateException("JDK 缺少 SHA-256 算法", exception);
        }
    }

    /** 获取操作人名称。 */
    private String operator()
    {
        return StringUtils.defaultIfEmpty(SecurityUtils.getUsername(), "system");
    }

    /** 去除可选字符串空白。 */
    private String trim(String value)
    {
        return value == null ? null : value.trim();
    }

    /** 从 Map 中读取文本。 */
    private String stringValue(Map<String, Object> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        return value == null ? null : String.valueOf(value);
    }

    /** 从 Map 中读取长整型。 */
    private Long longValue(Map<String, Object> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        return value == null ? null : ((Number) value).longValue();
    }

    /** 从 Map 中读取整型。 */
    private int safeInt(Map<String, Object> row, String key)
    {
        Object value = row == null ? null : row.get(key);
        return value == null ? 0 : ((Number) value).intValue();
    }

    /** 商户数据范围。 */
    private static final class Scope
    {
        private final Long tenantId;
        private final Long memberId;

        private Scope(Long tenantId, Long memberId)
        {
            this.tenantId = tenantId;
            this.memberId = memberId;
        }
    }
}
