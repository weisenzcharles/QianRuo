package com.ruoyi.rentalbusiness.service;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.rentalbusiness.mapper.MerchantAllianceDisputeMapper;
import com.ruoyi.rentalbusiness.enums.MerchantAllianceDisputeStatus;
import com.ruoyi.rentalbusiness.enums.MerchantAllianceMediationStatus;

/** 纠纷本地事务边界；跨服务保证金调用由上层编排，避免远程调用进入数据库事务。 */
@Service
public class MerchantAllianceDisputeLocalService
{
    private final MerchantAllianceDisputeMapper mapper;

    public MerchantAllianceDisputeLocalService(MerchantAllianceDisputeMapper mapper)
    {
        this.mapper = mapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(Long tenantId, Long allianceOrderId, Long initiatorMemberId, Long respondentMemberId,
            Integer disputeType, BigDecimal claimAmount, String description, String idempotencyKey, Long operatorId)
    {
        mapper.insertDispute(tenantId, allianceOrderId, initiatorMemberId, respondentMemberId, disputeType,
                claimAmount, description, idempotencyKey, operatorId);
        return mapper.selectLastInsertId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateFrozenAmount(Long tenantId, Long disputeId, BigDecimal frozenAmount, Long operatorId)
    {
        if (mapper.updateFrozenAmount(tenantId, disputeId, frozenAmount, operatorId) != 1)
            throw new IllegalStateException("纠纷冻结金额更新失败");
    }

    @Transactional(rollbackFor = Exception.class)
    public void markRejected(Long tenantId, Long disputeId, String resolution, Long operatorId)
    {
        mapper.markRejected(tenantId, disputeId, resolution, operatorId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void rejectMediation(Long tenantId, Long disputeId, String role, String remark)
    {
        if (mapper.rejectMediation(tenantId, disputeId, role,
                MerchantAllianceMediationStatus.PENDING_CONFIRM.getCode()) != 1)
            throw new IllegalStateException("调解方案已被其他操作处理");
        if (mapper.updateDisputeForMediation(tenantId, disputeId,
                MerchantAllianceDisputeStatus.CONFIRMING.getCode(), MerchantAllianceDisputeStatus.REJECTED.getCode(),
                remark, BigDecimal.ZERO) != 1)
            throw new IllegalStateException("纠纷状态已变化，请刷新后重试");
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmMediation(Long tenantId, Long disputeId, String role, boolean complete, BigDecimal awardedAmount)
    {
        if (mapper.confirmMediation(tenantId, disputeId, role,
                MerchantAllianceMediationStatus.PENDING_CONFIRM.getCode()) != 1)
            throw new IllegalStateException("调解方案已被其他操作处理");
        if (complete)
        {
            if (mapper.completeMediation(tenantId, disputeId,
                    MerchantAllianceMediationStatus.PENDING_CONFIRM.getCode(),
                    MerchantAllianceDisputeStatus.RESOLVED.getCode(), awardedAmount) != 1)
                throw new IllegalStateException("调解方案完成失败，请刷新后重试");
            if (mapper.updateDisputeForMediation(tenantId, disputeId,
                    MerchantAllianceDisputeStatus.CONFIRMING.getCode(), MerchantAllianceDisputeStatus.RESOLVED.getCode(),
                    "双方已确认调解方案", awardedAmount) != 1)
                throw new IllegalStateException("纠纷状态已变化，请刷新后重试");
        }
    }

    /** 在保证金已由外层幂等解冻后关闭到期纠纷。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean closeExpired(Long tenantId, Long disputeId, String resolution, Long operatorId)
    {
        return mapper.closeExpired(tenantId, disputeId, resolution, operatorId) == 1;
    }
}
