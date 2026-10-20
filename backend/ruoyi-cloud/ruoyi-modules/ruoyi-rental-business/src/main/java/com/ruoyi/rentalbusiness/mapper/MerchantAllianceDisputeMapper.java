package com.ruoyi.rentalbusiness.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/** 联盟订单纠纷及证据数据访问。 */
public interface MerchantAllianceDisputeMapper {
    Map<String, Object> selectAllianceOrder(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("allianceOrderId") Long allianceOrderId);

    int insertDispute(@Param("tenantId") Long tenantId, @Param("allianceOrderId") Long allianceOrderId,
            @Param("initiatorMemberId") Long initiatorMemberId, @Param("respondentMemberId") Long respondentMemberId,
            @Param("disputeType") Integer disputeType, @Param("claimAmount") BigDecimal claimAmount,
            @Param("description") String description, @Param("idempotencyKey") String idempotencyKey,
            @Param("operatorId") Long operatorId);

    Long selectLastInsertId();

    Map<String, Object> selectByIdempotencyKey(@Param("tenantId") Long tenantId,
            @Param("idempotencyKey") String idempotencyKey);

    List<Map<String, Object>> selectList(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("disputeStatus") Integer disputeStatus);

    Map<String, Object> selectById(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("disputeId") Long disputeId);

    Map<String, Object> selectForUpdate(@Param("tenantId") Long tenantId, @Param("disputeId") Long disputeId);

    Map<String, Object> selectForPlatform(@Param("disputeId") Long disputeId);

    /** 查询调解期限已届满且仍处于处理中状态的纠纷。 */
    List<Map<String, Object>> selectExpiredForRelease();

    /** 幂等关闭已完成保证金解冻的纠纷。 */
    int closeExpired(@Param("tenantId") Long tenantId, @Param("disputeId") Long disputeId,
            @Param("resolution") String resolution, @Param("operatorId") Long operatorId);

    int insertEvidence(@Param("tenantId") Long tenantId, @Param("disputeId") Long disputeId,
            @Param("memberId") Long memberId, @Param("evidenceType") Integer evidenceType,
            @Param("fileUrl") String fileUrl, @Param("fileName") String fileName,
            @Param("content") String content, @Param("operatorId") Long operatorId);

    List<Map<String, Object>> selectEvidence(@Param("tenantId") Long tenantId, @Param("disputeId") Long disputeId);

    Map<String, Object> selectMediation(@Param("tenantId") Long tenantId, @Param("disputeId") Long disputeId);

    Map<String, Object> selectLatestMediation(@Param("tenantId") Long tenantId, @Param("disputeId") Long disputeId);

    int updateFrozenAmount(@Param("tenantId") Long tenantId, @Param("disputeId") Long disputeId,
            @Param("frozenAmount") BigDecimal frozenAmount, @Param("operatorId") Long operatorId);

    int markRejected(@Param("tenantId") Long tenantId, @Param("disputeId") Long disputeId,
            @Param("resolution") String resolution, @Param("operatorId") Long operatorId);

    int insertMediation(@Param("tenantId") Long tenantId, @Param("disputeId") Long disputeId,
            @Param("roundNo") Integer roundNo, @Param("mediatorUserId") Long mediatorUserId,
            @Param("proposalAmount") BigDecimal proposalAmount, @Param("proposalContent") String proposalContent,
            @Param("operatorId") Long operatorId);

    int updateDisputeForMediation(@Param("tenantId") Long tenantId, @Param("disputeId") Long disputeId,
            @Param("fromStatus") Integer fromStatus, @Param("toStatus") Integer toStatus,
            @Param("resolution") String resolution, @Param("awardedAmount") BigDecimal awardedAmount);

    int confirmMediation(@Param("tenantId") Long tenantId, @Param("disputeId") Long disputeId,
            @Param("memberRole") String memberRole, @Param("mediationStatus") Integer mediationStatus);

    int rejectMediation(@Param("tenantId") Long tenantId, @Param("disputeId") Long disputeId,
            @Param("memberRole") String memberRole, @Param("mediationStatus") Integer mediationStatus);

    int completeMediation(@Param("tenantId") Long tenantId, @Param("disputeId") Long disputeId,
            @Param("mediationStatus") Integer mediationStatus, @Param("disputeStatus") Integer disputeStatus,
            @Param("awardedAmount") BigDecimal awardedAmount);
}
