package com.ruoyi.rentalbusiness.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.rentalbusiness.domain.MerchantOrderLifecycleCandidate;
import com.ruoyi.rentalbusiness.mapper.MerchantOrderLifecycleMapper;
import com.ruoyi.rentalbusiness.model.MerchantOrderAllianceLinkModel;

/** 订单履约后台自动状态推进测试。 */
public class MerchantOrderLifecycleServiceTest
{
    @Test
    public void refreshMovesShippedOrderToRentingAndSynchronizesRelatedData()
    {
        RecordingMapper mapper = new RecordingMapper(candidate(RentalCode.OrderStatus.SHIPPED));
        MerchantOrderAllianceLinkModel link = new MerchantOrderAllianceLinkModel();
        link.sourceMemberId = 28L;
        link.sourceOrderId = 8001L;
        mapper.link = link;

        int changed = new MerchantOrderLifecycleService(mapper).refresh(LocalDateTime.of(2026, 9, 27, 10, 0));

        require(changed == 1, "shipped order must be transitioned");
        require(RentalCode.OrderStatus.RENTING.getCode().equals(mapper.toStatus),
                "shipped order must become renting");
        require(RentalCode.TimeEffectStatus.NORMAL.getCode().equals(mapper.timeEffectStatus),
                "renting order must be normal");
        require(RentalCode.OrderStatus.RENTING.getCode().equals(mapper.itemStatus),
                "order items must follow order status");
        require(RentalCode.OrderStatus.RENTING.getCode().equals(mapper.allianceStatus),
                "alliance source order must follow child order");
        require("系统任务".equals(mapper.operatorName), "automatic transition must be audited as system task");
    }

    @Test
    public void refreshMovesPendingOrderToOverdue()
    {
        RecordingMapper mapper = new RecordingMapper(candidate(RentalCode.OrderStatus.PREPARED));

        int changed = new MerchantOrderLifecycleService(mapper).refresh(LocalDateTime.of(2026, 9, 27, 10, 0));

        require(changed == 1, "overdue shipment order must be transitioned");
        require(RentalCode.OrderStatus.OVERDUE.getCode().equals(mapper.toStatus),
                "pending shipment must become overdue");
        require(RentalCode.TimeEffectStatus.OVERDUE.getCode().equals(mapper.timeEffectStatus),
                "overdue shipment must have overdue time effect");
    }

    @Test
    public void refreshMovesRentingOrderToOverdueReturn()
    {
        RecordingMapper mapper = new RecordingMapper(candidate(RentalCode.OrderStatus.RENTING));

        int changed = new MerchantOrderLifecycleService(mapper).refresh(LocalDateTime.of(2026, 9, 27, 10, 0));

        require(changed == 1, "overdue return order must be transitioned");
        require(RentalCode.OrderStatus.OVERDUE_RETURN.getCode().equals(mapper.toStatus),
                "renting order must become overdue return");
    }

    @Test
    public void refreshSkipsOptimisticRaceWithoutWritingAudit()
    {
        RecordingMapper mapper = new RecordingMapper(candidate(RentalCode.OrderStatus.SHIPPED));
        mapper.updateCount = 0;

        int changed = new MerchantOrderLifecycleService(mapper).refresh(LocalDateTime.of(2026, 9, 27, 10, 0));

        require(changed == 0, "concurrent order update must be skipped");
        require(mapper.auditCount == 0, "concurrent order update must not write audit");
        require(mapper.itemStatus == null, "concurrent order update must not update items");
    }

    private MerchantOrderLifecycleCandidate candidate(RentalCode.OrderStatus status)
    {
        MerchantOrderLifecycleCandidate candidate = new MerchantOrderLifecycleCandidate();
        candidate.tenantId = 10001L;
        candidate.memberId = 27L;
        candidate.orderId = 9001L;
        candidate.orderNo = "OD0000009001";
        candidate.orderStatus = status.getCode();
        return candidate;
    }

    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }

    private static class RecordingMapper implements MerchantOrderLifecycleMapper
    {
        private final List<MerchantOrderLifecycleCandidate> candidates;
        private MerchantOrderAllianceLinkModel link;
        private int updateCount = 1;
        private int auditCount;
        private Integer toStatus;
        private Integer timeEffectStatus;
        private Integer itemStatus;
        private Integer allianceStatus;
        private String operatorName;

        private RecordingMapper(MerchantOrderLifecycleCandidate candidate)
        {
            this.candidates = Collections.singletonList(candidate);
        }

        @Override
        public List<MerchantOrderLifecycleCandidate> selectCandidates(List<Integer> shippingStatuses,
                List<Integer> shipOverdueStatuses, List<Integer> rentingStatuses, LocalDateTime now,
                java.time.LocalDate businessDate)
        {
            return candidates;
        }

        @Override
        public int updateOrderStatus(Long tenantId, Long memberId, Long orderId, Integer fromStatus,
                Integer toStatus, Integer timeEffectStatus, Long operatorId)
        {
            this.toStatus = toStatus;
            this.timeEffectStatus = timeEffectStatus;
            return updateCount;
        }

        @Override
        public int updateOrderItemStatus(Long tenantId, Long memberId, Long orderId, Integer itemStatus,
                Long operatorId)
        {
            this.itemStatus = itemStatus;
            return 1;
        }

        @Override
        public MerchantOrderAllianceLinkModel selectAllianceSourceOrderLink(Long tenantId, Long targetMemberId,
                Long childOrderId)
        {
            return link;
        }

        @Override
        public int updateAllianceSourceOrderStatus(Long tenantId, Long sourceMemberId, Long sourceOrderId,
                Integer orderStatus, Integer timeEffectStatus, Long operatorId)
        {
            this.allianceStatus = orderStatus;
            return 1;
        }

        @Override
        public int insertOrderEditLog(Long tenantId, Long memberId, Long orderId, String fieldName,
                String beforeValue, String afterValue, String beforeDisplayValue, String afterDisplayValue,
                String changeReason, Long operatorId, String operatorName)
        {
            auditCount++;
            this.operatorName = operatorName;
            return 1;
        }
    }
}
