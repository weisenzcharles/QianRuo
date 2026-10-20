package com.ruoyi.system.mapper.rental;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.api.model.MerchantInvitationRecordVO;
import com.ruoyi.system.domain.rental.MerchantLevel;

/** 商户等级、注册支付和邀请推广数据访问接口。 */
public interface MerchantPromotionMapper
{
    /** @return 未删除的商户等级 */
    List<MerchantLevel> selectLevelList();
    /** @param levelId 等级唯一编号 @return 等级详情 */
    MerchantLevel selectLevelById(Long levelId);
    /** @return 默认注册等级 */
    MerchantLevel selectDefaultLevel();
    /** @param level 等级 @return 影响行数 */
    int insertLevel(MerchantLevel level);
    /** @param level 等级 @return 影响行数 */
    int updateLevel(MerchantLevel level);
    /** @param levelId 等级唯一编号 @param operator 操作人 @return 影响行数 */
    int softDeleteLevel(@Param("levelId") Long levelId, @Param("operator") Long operator);
    /** @param levelId 等级唯一编号 @return 使用该等级的商户数量 */
    int countMembersByLevel(Long levelId);
    /** @param inviteCode 邀请码 @return 邀请码及等级配置 */
    java.util.Map<String, Object> selectInviteCodeForUpdate(String inviteCode);
    /** @param tenantId 租户唯一编号 @param memberId 商户唯一编号 @return 邀请码及等级概览 */
    java.util.Map<String, Object> selectInvitationOverview(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId);
    /** @param tenantId 租户唯一编号 @param memberId 商户唯一编号 @return 邀请记录 */
    List<MerchantInvitationRecordVO> selectInvitations(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId);
    /** @param tenantId 租户唯一编号 @param memberId 商户唯一编号 @param code 邀请码 @param operator 操作人 @return 影响行数 */
    int insertInviteCode(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("code") String code, @Param("operator") Long operator);
    /** @param inviteCodeId 邀请码ID @param inviteLimit 邀请上限 @return 影响行数 */
    int consumeInviteQuota(@Param("inviteCodeId") Long inviteCodeId, @Param("inviteLimit") Integer inviteLimit);
    /** @param tenantId 租户唯一编号 @param inviteCodeId 邀请码ID @param code 邀请码 @param inviterId 邀请人ID @param inviteeId 被邀请人ID @param phone 手机号 @return 影响行数 */
    int insertInvitation(@Param("tenantId") Long tenantId, @Param("inviteCodeId") Long inviteCodeId,
            @Param("code") String code, @Param("inviterId") Long inviterId,
            @Param("inviteeId") Long inviteeId, @Param("phone") String phone);
    /** @param memberId 商户唯一编号 @param paymentNo 支付单号 @param amount 金额 @return 影响行数 */
    int insertRegistrationPayment(@Param("memberId") Long memberId, @Param("paymentNo") String paymentNo,
            @Param("amount") BigDecimal amount);
    /** @param paymentNo 支付单号 @return 支付单及商户信息 */
    java.util.Map<String, Object> selectPaymentForUpdate(String paymentNo);
    /** @param memberId 商户唯一编号 @return 商户当前注册支付单 */
    java.util.Map<String, Object> selectPaymentByMember(Long memberId);
    /** @param paymentNo 支付单号 @return 影响行数 */
    int markPaymentPaid(String paymentNo);
    /** @param memberId 商户唯一编号 @return 影响行数 */
    int completePaidRegistration(Long memberId);
    /** @return 平台推广管理列表 */
    List<java.util.Map<String, Object>> selectPromotionList();
}
