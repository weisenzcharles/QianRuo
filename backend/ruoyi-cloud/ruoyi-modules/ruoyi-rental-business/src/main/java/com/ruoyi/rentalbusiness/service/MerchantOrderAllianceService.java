package com.ruoyi.rentalbusiness.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.merchant.api.RemoteMerchantAllianceService;
import com.ruoyi.merchant.api.model.MerchantAllianceOption;
import com.ruoyi.merchant.api.model.MerchantAllianceOptionQuery;
import com.ruoyi.rentalbusiness.domain.MerchantOrderRow;
import com.ruoyi.rentalbusiness.enums.MerchantOrderAllianceTransferMode;
import com.ruoyi.rentalbusiness.enums.MerchantAllianceOrderStatus;
import com.ruoyi.rentalbusiness.enums.MerchantAllianceOrderEventType;
import com.ruoyi.rentalbusiness.enums.MerchantAllianceShippingMode;
import com.ruoyi.rentalbusiness.enums.MerchantAllianceShippingPayer;
import com.ruoyi.rentalbusiness.enums.MerchantAllianceTaskView;
import com.ruoyi.rentalbusiness.mapper.MerchantOrderAllianceMapper;
import com.ruoyi.rentalbusiness.mapper.MerchantOrderMapper;
import com.ruoyi.rentalbusiness.model.MerchantAllianceOptionVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderAllianceTransferCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderAllianceTransferResult;
import com.ruoyi.rentalbusiness.model.MerchantAllianceAcceptCommand;
import com.ruoyi.rentalbusiness.model.MerchantAllianceReassignCommand;
import com.ruoyi.rentalbusiness.model.InventoryReservationCommand;

/** 订单租商联盟转单应用服务，负责目标校验和订单脱敏快照。 */
@Service
public class MerchantOrderAllianceService {
    private final MerchantOrderMapper orderMapper;
    private final MerchantOrderAllianceMapper allianceMapper;
    private final RemoteMerchantAllianceService remoteAllianceService;
    private final ObjectMapper objectMapper;
    private final CodeGeneratorClient codeGeneratorClient;
    private final MerchantInventoryReservationService inventoryReservationService;

    public MerchantOrderAllianceService(MerchantOrderMapper orderMapper, MerchantOrderAllianceMapper allianceMapper,
            RemoteMerchantAllianceService remoteAllianceService, ObjectMapper objectMapper,
            CodeGeneratorClient codeGeneratorClient, MerchantInventoryReservationService inventoryReservationService) {
        this.orderMapper = orderMapper;
        this.allianceMapper = allianceMapper;
        this.remoteAllianceService = remoteAllianceService;
        this.objectMapper = objectMapper;
        this.codeGeneratorClient = codeGeneratorClient;
        this.inventoryReservationService = inventoryReservationService;
    }

    /** 查询当前商户同联盟的其他有效商户。页面固定显示“租商联盟”选项。 */
    public List<MerchantAllianceOptionVO> listOptions() {
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        if (tenantId == null || memberId == null)
            return Collections.emptyList();
        List<MerchantAllianceOptionVO> result = new ArrayList<>();
        for (MerchantAllianceOption option : remoteOptions(tenantId, memberId)) {
            if (option != null && option.getMemberId() != null)
                result.add(new MerchantAllianceOptionVO(option.getMemberId(), option.getMerchantName()));
        }
        return result;
    }

    /** 创建转单任务；联盟目标会展开为每个有效接收商户的一条脱敏任务。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantOrderAllianceTransferResult transfer(MerchantOrderAllianceTransferCommand command) {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null || command.orderId == null)
            throw new ServiceException("订单不能为空");
        MerchantOrderAllianceTransferMode mode = MerchantOrderAllianceTransferMode.fromCode(command.transferMode);
        if (mode == null)
            throw new ServiceException("转单目标不合法");
        if (command.shippingMode != null && MerchantAllianceShippingMode.fromCode(command.shippingMode) == null)
            throw new ServiceException("配送方式不合法");
        if (command.shippingPayer != null && MerchantAllianceShippingPayer.fromCode(command.shippingPayer) == null)
            throw new ServiceException("快递费用承担方不合法");
        if (command.idempotencyKey == null || command.idempotencyKey.trim().isEmpty())
            command.idempotencyKey = UUID.randomUUID().toString();
        if (command.idempotencyKey.length() > 128)
            throw new ServiceException("转单请求标识长度不能超过128个字符");

        Long tenantId = MerchantContextUtils.getTenantId();
        Long sourceMemberId = MerchantContextUtils.getMemberId();
        MerchantOrderRow order = orderMapper.selectMerchantOrderById(tenantId, sourceMemberId, command.orderId);
        if (order == null)
            throw new ServiceException("订单不存在或不属于当前商户");

        Long allianceId = currentAllianceId(tenantId, sourceMemberId);
        List<Long> targetMemberIds = resolveTargets(command, mode, sourceMemberId, tenantId, allianceId);
        if (allianceId == null || targetMemberIds.isEmpty())
            throw new ServiceException("当前商户未加入有效租商联盟");
        String snapshot = maskedSnapshot(order);
        try {
            for (Long targetMemberId : targetMemberIds) {
                int inserted = allianceMapper.insertTransfer(tenantId, sourceMemberId, targetMemberId, command,
                        snapshot, allianceId, SecurityUtils.getUserId());
                if (inserted != 1)
                    throw new ServiceException("转单订单不存在或已删除");
                Long allianceOrderId = allianceMapper.selectLastInsertId();
                if (allianceOrderId == null || allianceMapper.insertEvent(tenantId, allianceOrderId,
                        MerchantAllianceOrderEventType.CREATED.getCode(), null,
                        MerchantAllianceOrderStatus.PENDING.getCode(), sourceMemberId, SecurityUtils.getUserId(),
                        snapshot) != 1)
                    throw new ServiceException("转单事件记录失败");
            }
        } catch (DuplicateKeyException exception) {
            throw new ServiceException("该订单已提交相同的转单请求，请勿重复提交");
        }
        MerchantOrderAllianceTransferResult result = new MerchantOrderAllianceTransferResult();
        result.createdCount = targetMemberIds.size();
        result.transferMode = mode.getCode();
        return result;
    }

    /** 查询当前商户发起或收到的协助订单，收到方在接受前只看到脱敏快照。 */
    public List<Map<String, Object>> listTasks(Integer status, String viewCode, String keyword,
            String deliveryCity, LocalDate leaseStartDate) {
        MerchantContextUtils.requireMerchantApproved();
        synchronizeStatuses(MerchantContextUtils.getTenantId());
        MerchantAllianceTaskView view = MerchantAllianceTaskView.fromCode(viewCode);
        if (view == null)
            throw new ServiceException("协助订单视图不合法");
        Long memberId = MerchantContextUtils.getMemberId();
        List<Long> memberIds = new ArrayList<>();
        Map<Long, String> merchantNames = new LinkedHashMap<>();
        for (MerchantAllianceOptionVO option : listOptions()) {
            memberIds.add(option.memberId);
            merchantNames.put(option.memberId, option.merchantName);
        }
        if (view == MerchantAllianceTaskView.PUBLIC_POOL && memberIds.isEmpty())
            return Collections.emptyList();
        String cityFilter = deliveryCity == null || deliveryCity.trim().isEmpty() ? null : deliveryCity.trim();
        if (cityFilter != null && cityFilter.length() > 80)
            throw new ServiceException("履约城市不能超过80个字符");
        List<Map<String, Object>> tasks = allianceMapper.selectTasks(MerchantContextUtils.getTenantId(), memberId,
                memberIds, status, view.getCode(), keyword == null || keyword.trim().isEmpty() ? null : keyword.trim(),
                MerchantAllianceOrderStatus.PUBLIC_POOL.getCode(),
                view == MerchantAllianceTaskView.PUBLIC_POOL ? cityFilter : null,
                view == MerchantAllianceTaskView.PUBLIC_POOL ? leaseStartDate : null);
        for (Map<String, Object> task : tasks) {
            Long sourceMemberId = number(task.get("sourceMemberId"));
            Long targetMemberId = number(task.get("targetMemberId"));
            Long merchantId = memberId.equals(sourceMemberId) ? targetMemberId : sourceMemberId;
            task.put("merchantName", merchantId == null ? "公众池" : merchantNames.getOrDefault(merchantId, "未知商户"));
        }
        return tasks;
    }

    /** 查询当前商户可见协助订单的状态汇总。 */
    public Map<String, Object> taskSummary(String viewCode) {
        MerchantContextUtils.requireMerchantApproved();
        MerchantAllianceTaskView view = MerchantAllianceTaskView.fromCode(viewCode);
        if (view == null)
            throw new ServiceException("协助订单视图不合法");
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        List<Long> memberIds = new ArrayList<>();
        for (MerchantAllianceOptionVO option : listOptions())
            memberIds.add(option.memberId);
        if (view == MerchantAllianceTaskView.PUBLIC_POOL && memberIds.isEmpty())
            return Collections.emptyMap();
        return allianceMapper.selectTaskSummary(tenantId, memberId, memberIds, view.getCode(),
                MerchantAllianceOrderStatus.PENDING.getCode(), MerchantAllianceOrderStatus.ACCEPTED.getCode(),
                MerchantAllianceOrderStatus.IN_FULFILLMENT.getCode(), MerchantAllianceOrderStatus.COMPLETED.getCode(),
                MerchantAllianceOrderStatus.PUBLIC_POOL.getCode());
    }

    /** 接受协助订单并在当前商户下创建子订单。订单复制和任务状态更新必须在同一事务中完成。 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> accept(Long allianceOrderId, MerchantAllianceAcceptCommand command) {
        MerchantContextUtils.requireMerchantApproved();
        if (allianceOrderId == null)
            throw new ServiceException("协助订单不能为空");
        Long tenantId = MerchantContextUtils.getTenantId();
        Long targetMemberId = MerchantContextUtils.getMemberId();
        Long targetAllianceId = currentAllianceId(tenantId, targetMemberId);
        if (targetAllianceId == null)
            throw new ServiceException("当前商户未加入有效租商联盟");
        Map<String, Object> task = allianceMapper.selectTask(tenantId, targetMemberId, allianceOrderId);
        if (task != null && !targetAllianceId.equals(number(task.get("allianceId"))))
            throw new ServiceException("协助订单不属于当前租商联盟");
        if (task != null
                && MerchantAllianceOrderStatus.PUBLIC_POOL.getCode() == number(task.get("allianceOrderStatus"))) {
            if (allianceMapper.claimPublicPoolTask(tenantId, targetMemberId, allianceOrderId,
                    MerchantAllianceOrderStatus.PUBLIC_POOL.getCode(), MerchantAllianceOrderStatus.PENDING.getCode(),
                    SecurityUtils.getUserId()) != 1)
                throw new ServiceException("该公众池订单已被其他商户领取，请刷新页面");
            if (allianceMapper.insertEvent(tenantId, allianceOrderId,
                    MerchantAllianceOrderEventType.PUBLIC_POOL.getCode(),
                    MerchantAllianceOrderStatus.PUBLIC_POOL.getCode(), MerchantAllianceOrderStatus.PENDING.getCode(),
                    targetMemberId, SecurityUtils.getUserId(), null) != 1)
                throw new ServiceException("公众池领取事件记录失败");
            task = allianceMapper.selectTask(tenantId, targetMemberId, allianceOrderId);
        }
        if (task != null && !targetAllianceId.equals(number(task.get("allianceId"))))
            throw new ServiceException("协助订单不属于当前租商联盟");
        if (task == null || !targetMemberId.equals(number(task.get("targetMemberId"))))
            throw new ServiceException("协助订单不存在或无权操作");
        if (MerchantAllianceOrderStatus.PENDING.getCode() != number(task.get("allianceOrderStatus")))
            throw new ServiceException("协助订单已处理，请刷新页面");
        Long sourceMemberId = number(task.get("sourceMemberId"));
        Long sourceOrderId = number(task.get("sourceOrderId"));
        if (command == null || command.warehouseId == null)
            throw new ServiceException("请选择当前商户的履约仓库");
        MerchantContextUtils.checkWarehouseAccess(command.warehouseId);
        List<Map<String, Object>> fulfillments = allianceMapper.selectChildOrderFulfillment(tenantId, sourceMemberId,
                targetMemberId, sourceOrderId, command.warehouseId, targetAllianceId);
        if (fulfillments == null || fulfillments.isEmpty())
            throw new ServiceException("原订单没有可履约的商品明细");
        for (Map<String, Object> fulfillment : fulfillments) {
            if (number(fulfillment.get("targetProductId")) == null)
                throw new ServiceException("当前商户没有对应商品型号的可用库存");
            if (fulfillment.get("platformPackageId") != null
                    && number(fulfillment.get("targetPackageId")) == null)
                throw new ServiceException("当前商户尚未配置可履约的对应套餐");
        }
        String orderNo = codeGeneratorClient.next("order");
        if (allianceMapper.insertChildOrder(tenantId, sourceMemberId, targetMemberId, sourceOrderId, orderNo,
                SecurityUtils.getUserId(), command.warehouseId) != 1)
            throw new ServiceException("原订单不存在或无法复制");
        Long childOrderId = allianceMapper.selectLastInsertId();
        if (childOrderId == null || allianceMapper.insertChildOrderItems(tenantId, sourceMemberId, targetMemberId,
                sourceOrderId, childOrderId, SecurityUtils.getUserId()) < 1)
            throw new ServiceException("协助订单明细复制失败");
        for (int index = 0; index < fulfillments.size(); index++) {
            Map<String, Object> fulfillment = fulfillments.get(index);
            InventoryReservationCommand reservation = new InventoryReservationCommand();
            reservation.orderId = childOrderId;
            reservation.packageId = number(fulfillment.get("targetPackageId"));
            reservation.productId = number(fulfillment.get("targetProductId"));
            reservation.warehouseId = command.warehouseId;
            reservation.quantity = intNumber(fulfillment.get("quantity"), 1);
            reservation.idempotencyKey = "alliance:" + allianceOrderId + ":reserve:" + index;
            reservation.reason = "接受联盟协助订单自动预占";
            if (reservation.packageId == null)
                inventoryReservationService.reserveProduct(reservation);
            else
                inventoryReservationService.reserve(reservation);
        }
        String snapshot = task.get("orderSnapshotJson") == null ? String.valueOf(task.get("maskedSnapshotJson"))
                : String.valueOf(task.get("orderSnapshotJson"));
        if (allianceMapper.bindChildOrder(tenantId, targetMemberId, allianceOrderId, childOrderId, command.warehouseId,
                snapshot, SecurityUtils.getUserId()) != 1)
            throw new ServiceException("协助订单状态更新失败，请刷新重试");
        if (allianceMapper.insertAllianceItemSnapshot(tenantId, allianceOrderId, sourceMemberId, sourceOrderId,
                SecurityUtils.getUserId()) < 1)
            throw new ServiceException("协助订单明细快照保存失败");
        if (allianceMapper.insertEvent(tenantId, allianceOrderId, MerchantAllianceOrderEventType.ACCEPTED.getCode(),
                MerchantAllianceOrderStatus.PENDING.getCode(), MerchantAllianceOrderStatus.ACCEPTED.getCode(),
                targetMemberId, SecurityUtils.getUserId(), snapshot) != 1)
            throw new ServiceException("协助订单接受事件记录失败");
        if (allianceMapper.insertEvent(tenantId, allianceOrderId,
                MerchantAllianceOrderEventType.CHILD_ORDER_CREATED.getCode(),
                MerchantAllianceOrderStatus.ACCEPTED.getCode(), MerchantAllianceOrderStatus.ACCEPTED.getCode(),
                targetMemberId, SecurityUtils.getUserId(), childOrderId.toString()) != 1)
            throw new ServiceException("子订单事件记录失败");
        if (allianceMapper.insertSettlement(tenantId, allianceOrderId, SecurityUtils.getUserId()) != 1)
            throw new ServiceException("协助订单结算记录保存失败");
        R<Void> slotResponse = remoteAllianceService.reserveOrderSlot(tenantId, targetAllianceId, targetMemberId,
                allianceOrderId, SecurityConstants.INNER, Constants.CLIENT_TYPE_MERCHANT);
        if (slotResponse == null || !R.isSuccess(slotResponse))
            throw new ServiceException(slotResponse == null ? "商户联盟服务暂不可用" : slotResponse.getMsg());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("allianceOrderId", allianceOrderId);
        result.put("childOrderId", childOrderId);
        result.put("orderNo", orderNo);
        return result;
    }

    public Map<String, Object> accept(Long allianceOrderId) {
        return accept(allianceOrderId, null);
    }

    /**
     * 更换已接受协助订单的履约仓库。只有子订单仍处于待发货或已备货状态时允许操作，
     * 旧仓库预占会先释放，再依据原订单明细在新仓库重新预占。
     */
    @Transactional(rollbackFor = Exception.class)
    public void changeFulfillmentWarehouse(Long allianceOrderId, MerchantAllianceAcceptCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (allianceOrderId == null || command == null || command.warehouseId == null)
        {
            throw new ServiceException("请选择履约仓库");
        }
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        Map<String, Object> task = allianceMapper.selectTask(tenantId, memberId, allianceOrderId);
        if (task == null || !memberId.equals(number(task.get("targetMemberId")))
                || !Integer.valueOf(MerchantAllianceOrderStatus.ACCEPTED.getCode())
                        .equals(number(task.get("allianceOrderStatus")))
                || number(task.get("childOrderId")) == null)
        {
            throw new ServiceException("当前协助订单不允许修改履约仓库");
        }
        Integer childStatus = intValue(task.get("childOrderStatus"));
        if (!RentalCode.OrderStatus.PENDING_SHIP.getCode().equals(childStatus)
                && !RentalCode.OrderStatus.PREPARED.getCode().equals(childStatus))
        {
            throw new ServiceException("订单已开始履约，不能修改履约仓库");
        }
        Long oldWarehouseId = number(task.get("targetWarehouseId"));
        if (command.warehouseId.equals(oldWarehouseId))
        {
            return;
        }
        MerchantContextUtils.checkWarehouseAccess(command.warehouseId);
        Long childOrderId = number(task.get("childOrderId"));
        Long allianceId = number(task.get("allianceId"));
        Long sourceMemberId = number(task.get("sourceMemberId"));
        Long sourceOrderId = number(task.get("sourceOrderId"));
        List<Map<String, Object>> fulfillments = allianceMapper.selectChildOrderFulfillment(tenantId,
                sourceMemberId, memberId, sourceOrderId, command.warehouseId, allianceId);
        if (fulfillments == null || fulfillments.isEmpty())
        {
            throw new ServiceException("原订单没有可履约的商品明细");
        }
        for (Map<String, Object> fulfillment : fulfillments)
        {
            if (number(fulfillment.get("targetProductId")) == null)
            {
                throw new ServiceException("新履约仓库没有对应商品型号的可用库存");
            }
            if (fulfillment.get("platformPackageId") != null
                    && number(fulfillment.get("targetPackageId")) == null)
            {
                throw new ServiceException("新履约仓库没有可履约的对应套餐");
            }
        }
        inventoryReservationService.releaseActiveByOrder(childOrderId, "联盟订单更换履约仓库");
        for (int index = 0; index < fulfillments.size(); index++)
        {
            Map<String, Object> fulfillment = fulfillments.get(index);
            InventoryReservationCommand reservation = new InventoryReservationCommand();
            reservation.orderId = childOrderId;
            reservation.packageId = number(fulfillment.get("targetPackageId"));
            reservation.productId = number(fulfillment.get("targetProductId"));
            reservation.warehouseId = command.warehouseId;
            reservation.quantity = intNumber(fulfillment.get("quantity"), 1);
            reservation.idempotencyKey = "alliance:" + allianceOrderId + ":warehouse:" + command.warehouseId
                    + ":reserve:" + index;
            reservation.reason = "联盟订单更换履约仓库自动预占";
            if (reservation.packageId == null)
            {
                inventoryReservationService.reserveProduct(reservation);
            }
            else
            {
                inventoryReservationService.reserve(reservation);
            }
        }
        Long operatorId = SecurityUtils.getUserId();
        if (allianceMapper.updateChildOrderWarehouse(tenantId, memberId, childOrderId, command.warehouseId,
                operatorId) != 1 || allianceMapper.updateTargetWarehouse(tenantId, memberId, allianceOrderId,
                        command.warehouseId, operatorId) != 1)
        {
            throw new ServiceException("履约仓库更新失败，请刷新后重试");
        }
        if (allianceMapper.insertEvent(tenantId, allianceOrderId,
                MerchantAllianceOrderEventType.WAREHOUSE_CHANGED.getCode(),
                MerchantAllianceOrderStatus.ACCEPTED.getCode(), MerchantAllianceOrderStatus.ACCEPTED.getCode(),
                memberId, operatorId, String.valueOf(command.warehouseId)) != 1)
        {
            throw new ServiceException("履约仓库变更记录失败");
        }
    }

    /** 将待接受任务转派给同一有效联盟中的其他商户。 */
    @Transactional(rollbackFor = Exception.class)
    public void reassign(Long allianceOrderId, MerchantAllianceReassignCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (allianceOrderId == null || command == null || command.targetMemberId == null)
            throw new ServiceException("请选择转派商户");
        Long tenantId = MerchantContextUtils.getTenantId();
        Long currentMemberId = MerchantContextUtils.getMemberId();
        if (currentMemberId.equals(command.targetMemberId))
            throw new ServiceException("转派商户不能为当前商户");
        boolean validTarget = listOptions().stream()
                .anyMatch(option -> command.targetMemberId.equals(option.memberId));
        if (!validTarget)
            throw new ServiceException("转派商户不在当前有效租商联盟中");
        if (allianceMapper.reassignTask(tenantId, currentMemberId, command.targetMemberId, allianceOrderId,
                SecurityUtils.getUserId()) != 1)
            throw new ServiceException("协助订单不存在、已处理或无权转派");
        if (allianceMapper.insertEvent(tenantId, allianceOrderId, MerchantAllianceOrderEventType.TRANSFERRED.getCode(),
                MerchantAllianceOrderStatus.PENDING.getCode(), MerchantAllianceOrderStatus.PENDING.getCode(),
                currentMemberId, SecurityUtils.getUserId(), null) != 1)
            throw new ServiceException("协助订单转派事件记录失败");
    }

    /** 发起方取消尚未被接受的协助订单，已接受或进入履约的订单不可通过此动作取消。 */
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long allianceOrderId) {
        MerchantContextUtils.requireMerchantApproved();
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        Map<String, Object> task = allianceMapper.selectTask(tenantId, memberId, allianceOrderId);
        if (task == null || !memberId.equals(number(task.get("sourceMemberId"))))
            throw new ServiceException("协助订单不存在或无权取消");
        Integer fromStatus = intValue(task.get("allianceOrderStatus"));
        if (!isCancellableStatus(fromStatus))
            throw new ServiceException("当前协助订单状态不允许取消");
        if (allianceMapper.cancelTask(tenantId, memberId, allianceOrderId, SecurityUtils.getUserId()) != 1)
            throw new ServiceException("协助订单已被处理，请刷新页面");
        if (allianceMapper.insertEvent(tenantId, allianceOrderId, MerchantAllianceOrderEventType.CANCELLED.getCode(),
                fromStatus, MerchantAllianceOrderStatus.CANCELLED.getCode(), memberId, SecurityUtils.getUserId(), null) != 1)
            throw new ServiceException("协助订单取消事件记录失败");
    }

    boolean isCancellableStatus(Integer status) {
        return Integer.valueOf(MerchantAllianceOrderStatus.PENDING.getCode()).equals(status)
                || Integer.valueOf(MerchantAllianceOrderStatus.PUBLIC_POOL.getCode()).equals(status);
    }

    /** 将已创建子订单的履约状态同步到联盟协作订单，并为每次变更写入不可变事件。 */
    @Transactional(rollbackFor = Exception.class)
    public int synchronizeStatuses(Long tenantId) {
        int changed = 0;
        for (Map<String, Object> row : allianceMapper.selectChildOrderStatusChanges(tenantId)) {
            Integer current = intValue(row.get("allianceOrderStatus"));
            Integer childOrderStatus = intValue(row.get("childOrderStatus"));
            Long rowTenantId = tenantId == null ? longValue(row.get("tenantId")) : tenantId;
            Long sourceMemberId = longValue(row.get("sourceMemberId"));
            Long sourceOrderId = longValue(row.get("sourceOrderId"));
            if (rowTenantId != null && sourceMemberId != null && sourceOrderId != null && childOrderStatus != null)
            {
                orderMapper.updateAllianceSourceOrderStatus(rowTenantId, sourceMemberId, sourceOrderId,
                        childOrderStatus, null);
            }
            Integer target = allianceStatusForChildOrder(childOrderStatus, current);
            if (target == null || target.equals(current)) {
                continue;
            }
            Long allianceOrderId = longValue(row.get("allianceOrderId"));
            if (rowTenantId == null || allianceMapper.updateStatusFromChildOrder(rowTenantId, allianceOrderId,
                    current, target, null) != 1) {
                continue;
            }
            MerchantAllianceOrderEventType eventType = eventTypeForStatus(target);
            allianceMapper.insertEvent(rowTenantId, allianceOrderId, eventType.getCode(), current, target,
                    longValue(row.get("targetMemberId")), null, null);
            if (target.equals(MerchantAllianceOrderStatus.COMPLETED.getCode())
                    || target.equals(MerchantAllianceOrderStatus.CANCELLED.getCode())
                    || target.equals(MerchantAllianceOrderStatus.EXCEPTION.getCode()))
            {
                R<Void> releaseResponse = remoteAllianceService.releaseOrderSlot(rowTenantId,
                        longValue(row.get("targetMemberId")), allianceOrderId, SecurityConstants.INNER,
                        Constants.CLIENT_TYPE_MERCHANT);
                if (releaseResponse == null || !R.isSuccess(releaseResponse))
                    throw new ServiceException(releaseResponse == null ? "商户联盟服务暂不可用" : releaseResponse.getMsg());
            }
            changed++;
        }
        return changed;
    }

    Integer allianceStatusForChildOrder(Integer childStatus, Integer current) {
        if (childStatus == null)
            return current;
        if (RentalCode.OrderStatus.COMPLETED.getCode().equals(childStatus))
            return MerchantAllianceOrderStatus.COMPLETED.getCode();
        if (RentalCode.OrderStatus.CANCELLED.getCode().equals(childStatus))
            return MerchantAllianceOrderStatus.CANCELLED.getCode();
        if (RentalCode.OrderStatus.OVERDUE_RETURN.getCode().equals(childStatus)
                || RentalCode.OrderStatus.OVERDUE.getCode().equals(childStatus)
                || RentalCode.OrderStatus.AFTER_SALES.getCode().equals(childStatus))
            return MerchantAllianceOrderStatus.EXCEPTION.getCode();
        if (RentalCode.OrderStatus.SHIPPING.getCode().equals(childStatus)
                || RentalCode.OrderStatus.SHIP_IN_TRANSIT.getCode().equals(childStatus)
                || RentalCode.OrderStatus.SHIPPED.getCode().equals(childStatus)
                || RentalCode.OrderStatus.RENTING.getCode().equals(childStatus)
                || RentalCode.OrderStatus.RETURNING.getCode().equals(childStatus))
            return MerchantAllianceOrderStatus.IN_FULFILLMENT.getCode();
        return MerchantAllianceOrderStatus.ACCEPTED.getCode();
    }

    private MerchantAllianceOrderEventType eventTypeForStatus(Integer status) {
        if (Integer.valueOf(MerchantAllianceOrderStatus.COMPLETED.getCode()).equals(status))
            return MerchantAllianceOrderEventType.COMPLETED;
        if (Integer.valueOf(MerchantAllianceOrderStatus.CANCELLED.getCode()).equals(status))
            return MerchantAllianceOrderEventType.CANCELLED;
        if (Integer.valueOf(MerchantAllianceOrderStatus.EXCEPTION.getCode()).equals(status))
            return MerchantAllianceOrderEventType.EXCEPTION;
        return MerchantAllianceOrderEventType.SHIPPED;
    }

    /** 拒绝待接收协助订单。拒绝不会修改原订单。 */
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long allianceOrderId) {
        MerchantContextUtils.requireMerchantApproved();
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        if (allianceMapper.updateTaskStatus(tenantId, memberId,
                allianceOrderId, MerchantAllianceOrderStatus.PENDING.getCode(),
                MerchantAllianceOrderStatus.REJECTED.getCode(),
                SecurityUtils.getUserId()) != 1)
            throw new ServiceException("协助订单不存在、已处理或无权操作");
        if (allianceMapper.insertEvent(tenantId, allianceOrderId, MerchantAllianceOrderEventType.REJECTED.getCode(),
                MerchantAllianceOrderStatus.PENDING.getCode(), MerchantAllianceOrderStatus.REJECTED.getCode(),
                memberId, SecurityUtils.getUserId(), null) != 1)
            throw new ServiceException("协助订单拒绝事件记录失败");
    }

    /** 发起方将已拒绝的协助订单重新发布到公众池。 */
    @Transactional(rollbackFor = Exception.class)
    public void publishPublicPool(Long allianceOrderId) {
        MerchantContextUtils.requireMerchantApproved();
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        if (allianceMapper.publishPublicPool(tenantId, memberId, allianceOrderId,
                MerchantAllianceOrderStatus.REJECTED.getCode(), MerchantAllianceOrderStatus.PUBLIC_POOL.getCode(),
                SecurityUtils.getUserId()) != 1)
            throw new ServiceException("协助订单不存在、状态不允许或无权操作");
        if (allianceMapper.insertEvent(tenantId, allianceOrderId, MerchantAllianceOrderEventType.PUBLIC_POOL.getCode(),
                MerchantAllianceOrderStatus.REJECTED.getCode(), MerchantAllianceOrderStatus.PUBLIC_POOL.getCode(),
                memberId, SecurityUtils.getUserId(), null) != 1)
            throw new ServiceException("公众池发布事件记录失败");
    }

    private Long number(Object value) {
        return value instanceof Number ? ((Number) value).longValue() : null;
    }

    private Long longValue(Object value) {
        return number(value);
    }

    private Integer intValue(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : null;
    }

    private int intNumber(Object value, int fallback) {
        return value instanceof Number ? ((Number) value).intValue() : fallback;
    }

    private List<Long> resolveTargets(MerchantOrderAllianceTransferCommand command,
            MerchantOrderAllianceTransferMode mode, Long sourceMemberId, Long tenantId, Long allianceId) {
        if (mode == MerchantOrderAllianceTransferMode.PUBLIC_POOL) {
            command.targetMemberId = null;
            return Collections.singletonList(null);
        }
        if (allianceId == null)
            return Collections.emptyList();
        List<Long> candidateMemberIds = new ArrayList<>();
        for (MerchantAllianceOption option : remoteOptions(tenantId, sourceMemberId)) {
            if (option != null && option.getMemberId() != null && hasAcceptCapacity(option))
                candidateMemberIds.add(option.getMemberId());
        }
        List<Long> eligibleMemberIds = candidateMemberIds.isEmpty() ? Collections.emptyList()
                : allianceMapper.selectEligibleTargetMemberIds(tenantId, allianceId, sourceMemberId,
                        command.orderId, candidateMemberIds);
        if (mode == MerchantOrderAllianceTransferMode.DESIGNATED_MERCHANT) {
            if (command.targetMemberId == null || sourceMemberId.equals(command.targetMemberId))
                throw new ServiceException("请选择其他联盟商户");
            if (!eligibleMemberIds.contains(command.targetMemberId))
                throw new ServiceException("目标商户当前没有符合共享范围或可用库存的履约能力");
            return Collections.singletonList(command.targetMemberId);
        }
        command.targetMemberId = null;
        if (eligibleMemberIds.isEmpty())
            throw new ServiceException("当前没有符合共享范围或可用库存的联盟商户");
        return eligibleMemberIds;
    }

    private List<MerchantAllianceOption> remoteOptions(Long tenantId, Long memberId)
    {
        R<List<MerchantAllianceOption>> response = remoteAllianceService.listOptions(
                new MerchantAllianceOptionQuery(tenantId, memberId), SecurityConstants.INNER,
                Constants.CLIENT_TYPE_MERCHANT);
        return response == null || response.getData() == null ? Collections.emptyList() : response.getData();
    }

    /** 接单上限属于商户治理库，转单前先过滤已停用共享或已达到上限的商户。 */
    private boolean hasAcceptCapacity(MerchantAllianceOption option)
    {
        if (option.getShareStockEnabled() != null && option.getShareStockEnabled() != 1)
            return false;
        Integer limit = option.getAcceptOrderLimit();
        Integer current = option.getCurrentAcceptOrderCount();
        return limit == null || limit <= 0 || current == null || current < limit;
    }

    private Long currentAllianceId(Long tenantId, Long memberId) {
        R<Map<String, Object>> response = remoteAllianceService.current(
                new MerchantAllianceOptionQuery(tenantId, memberId), SecurityConstants.INNER,
                Constants.CLIENT_TYPE_MERCHANT);
        if (response == null || response.getData() == null)
            return null;
        return number(response.getData().get("allianceId"));
    }

    private String maskedSnapshot(MerchantOrderRow order) {
        JsonNode packageDetails = readPackageDetails(order.packageDetailsJson);
        JsonNode mainProduct = packageDetails == null ? null : packageDetails.path("mainProduct");
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("orderNo", order.orderNo);
        snapshot.put("productName", firstNonBlank(text(mainProduct, "productName"), order.productName));
        snapshot.put("modelName", firstNonBlank(text(mainProduct, "modelName"), order.modelName));
        snapshot.put("packageName", firstNonBlank(text(packageDetails, "packageName"), order.packageName));
        snapshot.put("leaseStartDate", order.leaseStartDate);
        snapshot.put("leaseEndDate", order.leaseEndDate);
        snapshot.put("warehouseCity", order.warehouseCity);
        snapshot.put("deliveryCity", order.recipientCityName);
        snapshot.put("quantity", order.quantity);
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException exception) {
            throw new ServiceException("转单脱敏信息生成失败");
        }
    }

    /** 读取订单自身的套餐快照；转单时不回查套餐目录，解析失败则交由旧字段兜底。 */
    private JsonNode readPackageDetails(String packageDetailsJson) {
        if (packageDetailsJson == null || packageDetailsJson.trim().isEmpty())
            return null;
        try {
            return (objectMapper == null ? new ObjectMapper() : objectMapper).readTree(packageDetailsJson);
        } catch (JsonProcessingException exception) {
            return null;
        }
    }

    private String text(JsonNode node, String fieldName) {
        if (node == null || !node.isObject() || node.path(fieldName).isMissingNode()
                || node.path(fieldName).isNull())
            return null;
        String value = node.path(fieldName).asText();
        return value == null || value.trim().isEmpty() ? null : value;
    }

    private String firstNonBlank(String first, String fallback) {
        return first == null || first.trim().isEmpty() ? fallback : first;
    }
}
