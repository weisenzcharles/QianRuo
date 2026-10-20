package com.ruoyi.rentalbusiness.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import com.ruoyi.rentalbusiness.mapper.MerchantAllianceDisputeMapper;
import com.ruoyi.rentalbusiness.enums.MerchantAllianceDisputeType;
import com.ruoyi.rentalbusiness.enums.MerchantAllianceEvidenceType;
import com.ruoyi.rentalbusiness.enums.MerchantAllianceDisputeStatus;
import com.ruoyi.rentalbusiness.enums.MerchantAllianceMediationStatus;
import com.ruoyi.rentalbusiness.model.MerchantAllianceDisputeCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantAllianceDisputeEvidenceCommand;
import com.ruoyi.merchant.api.RemoteMerchantDepositService;
import com.ruoyi.merchant.api.model.MerchantDepositBalance;
import com.ruoyi.merchant.api.model.MerchantDepositOperationCommand;

/** 联盟订单纠纷应用服务，负责商户范围校验和证据追加。 */
@Service
public class MerchantAllianceDisputeService {
    private final MerchantAllianceDisputeMapper mapper;
    private final MerchantAllianceDisputeLocalService localService;
    private final RemoteMerchantDepositService depositService;

    public MerchantAllianceDisputeService(MerchantAllianceDisputeMapper mapper,
            MerchantAllianceDisputeLocalService localService, RemoteMerchantDepositService depositService) {
        this.mapper = mapper;
        this.localService = localService;
        this.depositService = depositService;
    }

    /** 查询当前商户参与的纠纷。 */
    public List<Map<String, Object>> list(Integer disputeStatus) {
        MerchantContextUtils.requireMerchantApproved();
        return mapper.selectList(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), disputeStatus);
    }

    /** 查询当前商户可见的纠纷和证据。 */
    public Map<String, Object> get(Long disputeId) {
        MerchantContextUtils.requireMerchantApproved();
        Map<String, Object> result = mapper.selectById(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), disputeId);
        if (result == null) throw new ServiceException("纠纷不存在或无权查看");
        result.put("evidence", mapper.selectEvidence(MerchantContextUtils.getTenantId(), disputeId));
        result.put("mediation", mapper.selectLatestMediation(MerchantContextUtils.getTenantId(), disputeId));
        return result;
    }

    /** 释放调解期限届满纠纷的冻结保证金，并幂等关闭纠纷。 */
    public int releaseExpired() {
        List<Map<String, Object>> disputes = mapper.selectExpiredForRelease();
        if (disputes == null || disputes.isEmpty()) return 0;
        Long operatorId = SecurityUtils.getUserId();
        int closed = 0;
        for (Map<String, Object> dispute : disputes) {
            Long tenantId = number(dispute.get("tenantId"));
            Long disputeId = number(dispute.get("disputeId"));
            Long allianceId = number(dispute.get("allianceId"));
            Long respondentMemberId = number(dispute.get("respondentMemberId"));
            BigDecimal frozenAmount = numberDecimal(dispute.get("frozenAmount"));
            if (frozenAmount.signum() > 0) {
                callDeposit("release", depositCommand(tenantId, allianceId, respondentMemberId, frozenAmount,
                        "dispute", disputeId, "dispute:" + disputeId + ":expiry-release", operatorId));
            }
            if (localService.closeExpired(tenantId, disputeId, "调解期限届满，保证金已自动解冻", operatorId)) closed++;
        }
        return closed;
    }

    /** 发起纠纷；同一幂等键不能创建重复纠纷。 */
    public Long create(MerchantAllianceDisputeCreateCommand command) {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null || command.allianceOrderId == null) throw new ServiceException("联盟订单不能为空");
        if (command.disputeType == null || !isDisputeType(command.disputeType)) throw new ServiceException("纠纷类型不合法");
        if (command.description == null || command.description.trim().isEmpty() || command.description.length() > 2000)
            throw new ServiceException("请填写纠纷说明，且不能超过2000个字符");
        BigDecimal claimAmount = command.claimAmount == null ? BigDecimal.ZERO : command.claimAmount;
        if (claimAmount.signum() < 0) throw new ServiceException("申请赔付金额不能为负数");
        String idempotencyKey = command.idempotencyKey == null || command.idempotencyKey.trim().isEmpty()
                ? UUID.randomUUID().toString() : command.idempotencyKey.trim();
        if (idempotencyKey.length() > 128) throw new ServiceException("幂等请求标识不能超过128个字符");
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        Map<String, Object> order = mapper.selectAllianceOrder(tenantId, memberId, command.allianceOrderId);
        if (order == null) throw new ServiceException("联盟订单不存在或无权发起纠纷");
        Long sourceMemberId = number(order.get("sourceMemberId"));
        Long targetMemberId = number(order.get("targetMemberId"));
        Long respondent = memberId.equals(sourceMemberId) ? targetMemberId : sourceMemberId;
        Long disputeId;
        try {
            disputeId = localService.create(tenantId, command.allianceOrderId, memberId, respondent,
                    command.disputeType, claimAmount, command.description.trim(), idempotencyKey, SecurityUtils.getUserId());
        } catch (DuplicateKeyException exception) {
            Map<String, Object> existing = mapper.selectByIdempotencyKey(tenantId, idempotencyKey);
            if (existing != null && existing.get("disputeId") instanceof Number)
                return ((Number) existing.get("disputeId")).longValue();
            throw new ServiceException("该纠纷请求已提交，请刷新后重试");
        }
        if (claimAmount.signum() > 0) {
            try {
                callDeposit("freeze", depositCommand(tenantId, number(order.get("allianceId")), respondent,
                        claimAmount, "dispute", disputeId, "dispute:" + disputeId + ":freeze"));
                localService.updateFrozenAmount(tenantId, disputeId, claimAmount, SecurityUtils.getUserId());
            } catch (RuntimeException exception) {
                try {
                    callDeposit("release", depositCommand(tenantId, number(order.get("allianceId")), respondent,
                            claimAmount, "dispute", disputeId, "dispute:" + disputeId + ":freeze-compensation"));
                } catch (RuntimeException compensationException) {
                    exception.addSuppressed(compensationException);
                }
                localService.markRejected(tenantId, disputeId, "保证金冻结失败，纠纷未生效", SecurityUtils.getUserId());
                throw exception;
            }
        }
        return disputeId;
    }

    /** 向当前商户可见的纠纷追加证据。 */
    @Transactional(rollbackFor = Exception.class)
    public void addEvidence(Long disputeId, MerchantAllianceDisputeEvidenceCommand command) {
        MerchantContextUtils.requireMerchantApproved();
        if (disputeId == null || command == null || command.evidenceType == null || !isEvidenceType(command.evidenceType))
            throw new ServiceException("证据类型不合法");
        MerchantAllianceEvidenceType evidenceType = MerchantAllianceEvidenceType.fromCode(command.evidenceType);
        if (evidenceType == MerchantAllianceEvidenceType.TEXT
                && (command.content == null || command.content.trim().isEmpty()))
            throw new ServiceException("文字证据不能为空");
        if (evidenceType != MerchantAllianceEvidenceType.TEXT
                && (command.fileUrl == null || command.fileUrl.trim().isEmpty()))
            throw new ServiceException("请上传证据文件");
        Map<String, Object> dispute = mapper.selectById(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), disputeId);
        if (dispute == null) throw new ServiceException("纠纷不存在或无权提交证据");
        MerchantAllianceDisputeStatus disputeStatus = MerchantAllianceDisputeStatus
                .fromCode(intValue(dispute.get("disputeStatus")));
        if (disputeStatus == null || (disputeStatus != MerchantAllianceDisputeStatus.PENDING
                && disputeStatus != MerchantAllianceDisputeStatus.EVIDENCE
                && disputeStatus != MerchantAllianceDisputeStatus.MEDIATING
                && disputeStatus != MerchantAllianceDisputeStatus.CONFIRMING))
            throw new ServiceException("当前纠纷已结束，不能继续提交证据");
        mapper.insertEvidence(MerchantContextUtils.getTenantId(), disputeId, MerchantContextUtils.getMemberId(),
                command.evidenceType, command.fileUrl, command.fileName, command.content, SecurityUtils.getUserId());
    }

    /** 平台提交新一轮调解方案，并将纠纷置为双方确认状态。 */
    @Transactional(rollbackFor = Exception.class)
    public void proposeMediation(Long disputeId, BigDecimal proposalAmount, String proposalContent, Long mediatorUserId) {
        if (disputeId == null || proposalContent == null || proposalContent.trim().isEmpty()
                || proposalContent.length() > 2000) throw new ServiceException("请填写调解方案，且不能超过2000个字符");
        BigDecimal amount = proposalAmount == null ? BigDecimal.ZERO : proposalAmount;
        if (amount.signum() < 0) throw new ServiceException("调解赔付金额不能为负数");
        Map<String, Object> dispute = mapper.selectForPlatform(disputeId);
        if (dispute == null) throw new ServiceException("纠纷不存在");
        Long tenantId = number(dispute.get("tenantId"));
        int status = intValue(dispute.get("disputeStatus"));
        if (status != MerchantAllianceDisputeStatus.PENDING.getCode()
                && status != MerchantAllianceDisputeStatus.EVIDENCE.getCode()
                && status != MerchantAllianceDisputeStatus.MEDIATING.getCode())
            throw new ServiceException("当前纠纷状态不允许发起调解");
        Map<String, Object> latest = mapper.selectMediation(tenantId, disputeId);
        int round = latest == null ? 1 : intValue(latest.get("roundNo")) + 1;
        mapper.insertMediation(tenantId, disputeId, round, mediatorUserId, amount, proposalContent.trim(), mediatorUserId);
        if (mapper.updateDisputeForMediation(tenantId, disputeId, status,
                MerchantAllianceDisputeStatus.CONFIRMING.getCode(), "平台已提交调解方案，待双方确认", amount) != 1)
            throw new ServiceException("纠纷状态已变化，请刷新后重试");
    }

    /** 商户确认或拒绝当前调解方案；双方确认后将纠纷结案并记录最终赔付金额。 */
    public void confirmMediation(Long disputeId, com.ruoyi.rentalbusiness.model.MerchantAllianceMediationConfirmCommand command) {
        MerchantContextUtils.requireMerchantApproved();
        if (disputeId == null || command == null || command.accepted == null)
            throw new ServiceException("请选择接受或拒绝调解方案");
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        Map<String, Object> dispute = mapper.selectById(tenantId, memberId, disputeId);
        if (dispute == null) throw new ServiceException("纠纷不存在或无权处理");
        if (intValue(dispute.get("disputeStatus")) != MerchantAllianceDisputeStatus.CONFIRMING.getCode())
            throw new ServiceException("当前纠纷不在待确认状态");
        Long initiator = number(dispute.get("initiatorMemberId"));
        Long respondent = number(dispute.get("respondentMemberId"));
        String role = memberId.equals(initiator) ? "initiator" : memberId.equals(respondent) ? "respondent" : null;
        if (role == null) throw new ServiceException("当前商户不是纠纷参与方");
        Map<String, Object> mediation = mapper.selectMediation(tenantId, disputeId);
        if (mediation == null || intValue(mediation.get("mediationStatus"))
                != MerchantAllianceMediationStatus.PENDING_CONFIRM.getCode())
            throw new ServiceException("当前没有待确认的调解方案");
        boolean alreadyConfirmed = "initiator".equals(role)
                ? isEnabled(intValue(mediation.get("initiatorConfirmed")))
                : isEnabled(intValue(mediation.get("respondentConfirmed")));
        if (alreadyConfirmed) throw new ServiceException("当前商户已确认该调解方案");
        if (!command.accepted.booleanValue()) {
            BigDecimal frozen = numberDecimal(dispute.get("frozenAmount"));
            if (frozen.signum() > 0) callDeposit("release", depositCommand(tenantId, number(dispute.get("allianceId")),
                    respondent, frozen, "dispute", disputeId, "dispute:" + disputeId + ":reject-release"));
            localService.rejectMediation(tenantId, disputeId, role, command.remark);
            return;
        }
        boolean otherConfirmed = "initiator".equals(role)
                ? isEnabled(intValue(mediation.get("respondentConfirmed")))
                : isEnabled(intValue(mediation.get("initiatorConfirmed")));
        if (!otherConfirmed) {
            localService.confirmMediation(tenantId, disputeId, role, false, BigDecimal.ZERO);
            return;
        }
        BigDecimal awarded = numberDecimal(mediation.get("proposalAmount"));
        settleDeposit(tenantId, number(dispute.get("allianceId")), respondent, disputeId,
                numberDecimal(dispute.get("frozenAmount")), awarded);
        localService.confirmMediation(tenantId, disputeId, role, true, awarded);
    }

    private void settleDeposit(Long tenantId, Long allianceId, Long memberId, Long disputeId,
            BigDecimal frozen, BigDecimal awarded) {
        if (awarded.compareTo(frozen) > 0) {
            BigDecimal extra = awarded.subtract(frozen);
            callDeposit("freeze", depositCommand(tenantId, allianceId, memberId, extra, "dispute", disputeId,
                    "dispute:" + disputeId + ":settle-extra-freeze"));
            frozen = awarded;
        }
        if (awarded.signum() > 0) callDeposit("deduct", depositCommand(tenantId, allianceId, memberId, awarded,
                "dispute", disputeId, "dispute:" + disputeId + ":settle-deduct"));
        BigDecimal release = frozen.subtract(awarded);
        if (release.signum() > 0) callDeposit("release", depositCommand(tenantId, allianceId, memberId, release,
                "dispute", disputeId, "dispute:" + disputeId + ":settle-release"));
    }

    private MerchantDepositOperationCommand depositCommand(Long tenantId, Long allianceId, Long memberId,
            BigDecimal amount, String refType, Long refId, String idempotencyKey) {
        return depositCommand(tenantId, allianceId, memberId, amount, refType, refId, idempotencyKey,
                SecurityUtils.getUserId());
    }

    private MerchantDepositOperationCommand depositCommand(Long tenantId, Long allianceId, Long memberId,
            BigDecimal amount, String refType, Long refId, String idempotencyKey, Long operatorId) {
        MerchantDepositOperationCommand command = new MerchantDepositOperationCommand();
        command.setTenantId(tenantId); command.setAllianceId(allianceId); command.setMemberId(memberId);
        command.setAmount(amount); command.setRefType(refType); command.setRefId(refId);
        command.setIdempotencyKey(idempotencyKey); command.setOperatorId(operatorId);
        command.setRemark("联盟纠纷保证金处理");
        return command;
    }

    private MerchantDepositBalance callDeposit(String operation, MerchantDepositOperationCommand command) {
        R<MerchantDepositBalance> result;
        switch (operation) {
            case "freeze" -> result = depositService.freeze(command, SecurityConstants.INNER, Constants.CLIENT_TYPE_MERCHANT);
            case "release" -> result = depositService.release(command, SecurityConstants.INNER, Constants.CLIENT_TYPE_MERCHANT);
            case "deduct" -> result = depositService.deduct(command, SecurityConstants.INNER, Constants.CLIENT_TYPE_MERCHANT);
            default -> throw new ServiceException("不支持的保证金操作");
        }
        if (result == null || !R.isSuccess(result))
            throw new ServiceException(result == null || result.getMsg() == null ? "保证金服务暂不可用" : result.getMsg());
        return result.getData();
    }

    private boolean isDisputeType(Integer value) {
        MerchantAllianceDisputeType type = MerchantAllianceDisputeType.fromCode(value);
        return type != null && type != MerchantAllianceDisputeType.GENERAL;
    }

    private boolean isEvidenceType(Integer value) {
        return MerchantAllianceEvidenceType.fromCode(value) != null;
    }

    private boolean isEnabled(Integer value) {
        return RentalCode.YesNo.YES.getCode().equals(value);
    }
    private Long number(Object value) { return value instanceof Number ? ((Number) value).longValue() : null; }
    private BigDecimal numberDecimal(Object value) { return value instanceof BigDecimal ? (BigDecimal) value : value instanceof Number ? new BigDecimal(value.toString()) : BigDecimal.ZERO; }
    private int intValue(Object value) { return value instanceof Number ? ((Number) value).intValue() : -1; }
}
