package com.ruoyi.rentalbusiness.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.rentalbusiness.domain.MerchantOrderLifecycleCandidate;
import com.ruoyi.rentalbusiness.mapper.MerchantOrderLifecycleMapper;
import com.ruoyi.rentalbusiness.model.MerchantOrderAllianceLinkModel;

/**
 * 订单履约后台状态推进服务。
 *
 * <p>人工操作仍通过订单工作流完成；本服务只处理到达明确时间条件的自动推进，
 * 并以原状态作为更新条件，避免定时任务覆盖人工操作或重复写审计。</p>
 */
@Service
public class MerchantOrderLifecycleService
{
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final String STATUS_FIELD = "订单状态";
    private static final String SYSTEM_OPERATOR = "系统任务";
    private static final String CHANGE_REASON = "订单履约定时任务自动推进";

    private final MerchantOrderLifecycleMapper mapper;
    private final MerchantExternalOrderCallbackService callbackService;

    public MerchantOrderLifecycleService(MerchantOrderLifecycleMapper mapper)
    {
        this(mapper, null);
    }

    @Autowired
    public MerchantOrderLifecycleService(MerchantOrderLifecycleMapper mapper,
            MerchantExternalOrderCallbackService callbackService)
    {
        this.mapper = mapper;
        this.callbackService = callbackService;
    }

    /** 按上海业务时区刷新达到租期或发货时限的订单。 */
    @Transactional(rollbackFor = Exception.class)
    public int refresh()
    {
        return refresh(LocalDateTime.now(BUSINESS_ZONE));
    }

    /** 使用指定时间刷新，供调度入口和单元测试复用。 */
    @Transactional(rollbackFor = Exception.class)
    public int refresh(LocalDateTime now)
    {
        LocalDateTime effectiveNow = now == null ? LocalDateTime.now(BUSINESS_ZONE) : now;
        List<MerchantOrderLifecycleCandidate> candidates = mapper.selectCandidates(
                statusCodes(RentalCode.OrderStatus.SHIPPING, RentalCode.OrderStatus.SHIP_IN_TRANSIT,
                        RentalCode.OrderStatus.SHIPPED),
                statusCodes(RentalCode.OrderStatus.PENDING_SHIP, RentalCode.OrderStatus.PREPARED),
                Collections.singletonList(RentalCode.OrderStatus.RENTING.getCode()), effectiveNow,
                effectiveNow.toLocalDate());
        if (candidates == null || candidates.isEmpty())
        {
            return 0;
        }
        int changed = 0;
        for (MerchantOrderLifecycleCandidate candidate : candidates)
        {
            LifecycleTransition transition = resolveTransition(candidate == null ? null : candidate.orderStatus);
            if (candidate == null || transition == null || candidate.tenantId == null || candidate.memberId == null
                    || candidate.orderId == null)
            {
                continue;
            }
            if (mapper.updateOrderStatus(candidate.tenantId, candidate.memberId, candidate.orderId,
                    candidate.orderStatus, transition.targetStatus.getCode(), transition.timeEffectStatus.getCode(),
                    null) != 1)
            {
                continue;
            }
            mapper.updateOrderItemStatus(candidate.tenantId, candidate.memberId, candidate.orderId,
                    transition.targetStatus.getCode(), null);
            mapper.insertOrderEditLog(candidate.tenantId, candidate.memberId, candidate.orderId, STATUS_FIELD,
                    String.valueOf(candidate.orderStatus), String.valueOf(transition.targetStatus.getCode()),
                    label(candidate.orderStatus), transition.targetStatus.getLabel(), CHANGE_REASON,
                    null, SYSTEM_OPERATOR);
            if (callbackService != null)
            {
                callbackService.enqueueOrderStatusChanged(candidate.tenantId, candidate.memberId, candidate.orderId,
                        candidate.orderStatus, transition.targetStatus.getCode(), CHANGE_REASON);
            }
            synchronizeAllianceSource(candidate, transition);
            changed++;
        }
        return changed;
    }

    private void synchronizeAllianceSource(MerchantOrderLifecycleCandidate candidate, LifecycleTransition transition)
    {
        MerchantOrderAllianceLinkModel link = mapper.selectAllianceSourceOrderLink(candidate.tenantId,
                candidate.memberId, candidate.orderId);
        if (link != null && link.sourceMemberId != null && link.sourceOrderId != null)
        {
            mapper.updateAllianceSourceOrderStatus(candidate.tenantId, link.sourceMemberId, link.sourceOrderId,
                    transition.targetStatus.getCode(), transition.timeEffectStatus.getCode(), null);
        }
    }

    private LifecycleTransition resolveTransition(Integer currentStatus)
    {
        if (isOneOf(currentStatus, RentalCode.OrderStatus.SHIPPING, RentalCode.OrderStatus.SHIP_IN_TRANSIT,
                RentalCode.OrderStatus.SHIPPED))
        {
            return new LifecycleTransition(RentalCode.OrderStatus.RENTING, RentalCode.TimeEffectStatus.NORMAL);
        }
        if (isOneOf(currentStatus, RentalCode.OrderStatus.PENDING_SHIP, RentalCode.OrderStatus.PREPARED))
        {
            return new LifecycleTransition(RentalCode.OrderStatus.OVERDUE, RentalCode.TimeEffectStatus.OVERDUE);
        }
        if (isOneOf(currentStatus, RentalCode.OrderStatus.RENTING))
        {
            return new LifecycleTransition(RentalCode.OrderStatus.OVERDUE_RETURN,
                    RentalCode.TimeEffectStatus.OVERDUE);
        }
        return null;
    }

    private boolean isOneOf(Integer code, RentalCode.OrderStatus... values)
    {
        if (code == null)
        {
            return false;
        }
        for (RentalCode.OrderStatus value : values)
        {
            if (value.getCode().equals(code))
            {
                return true;
            }
        }
        return false;
    }

    private List<Integer> statusCodes(RentalCode.OrderStatus... values)
    {
        Integer[] codes = new Integer[values.length];
        for (int index = 0; index < values.length; index++)
        {
            codes[index] = values[index].getCode();
        }
        return Arrays.asList(codes);
    }

    private String label(Integer code)
    {
        if (code != null)
        {
            for (RentalCode.OrderStatus value : RentalCode.OrderStatus.values())
            {
                if (value.getCode().equals(code))
                {
                    return value.getLabel();
                }
            }
        }
        return "未知状态";
    }

    private static final class LifecycleTransition
    {
        private final RentalCode.OrderStatus targetStatus;
        private final RentalCode.TimeEffectStatus timeEffectStatus;

        private LifecycleTransition(RentalCode.OrderStatus targetStatus,
                RentalCode.TimeEffectStatus timeEffectStatus)
        {
            this.targetStatus = targetStatus;
            this.timeEffectStatus = timeEffectStatus;
        }
    }
}
