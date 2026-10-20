package com.ruoyi.merchant.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.merchant.model.MerchantInternalNoticeCommand;
import com.ruoyi.merchant.model.MerchantInternalNoticeQuery;
import com.ruoyi.merchant.model.MerchantInternalNoticeReadStatVO;
import com.ruoyi.merchant.model.MerchantInternalNoticeRoleOptionVO;
import com.ruoyi.merchant.model.MerchantInternalNoticeVO;

/** 商户内部通知数据访问。 */
public interface MerchantInternalNoticeMapper
{
    List<MerchantInternalNoticeVO> selectManagementList(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("query") MerchantInternalNoticeQuery query);

    MerchantInternalNoticeVO selectManagementById(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("noticeId") Long noticeId);

    List<MerchantInternalNoticeReadStatVO> selectReadStats(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("noticeId") Long noticeId);

    List<MerchantInternalNoticeVO> selectVisibleTop(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("merchantUserId") Long merchantUserId,
            @Param("limit") int limit);

    MerchantInternalNoticeVO selectVisibleById(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("merchantUserId") Long merchantUserId,
            @Param("noticeId") Long noticeId);

    int selectUnreadCount(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("merchantUserId") Long merchantUserId);

    List<MerchantInternalNoticeRoleOptionVO> selectRoleOptions(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId);

    int countValidRoles(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("roleIds") Long[] roleIds);

    int insertNotice(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("command") MerchantInternalNoticeCommand command, @Param("operator") Long operator);

    Long selectLastInsertId();

    int updateNotice(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("command") MerchantInternalNoticeCommand command, @Param("operator") Long operator);

    int updateStatus(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("noticeId") Long noticeId, @Param("status") Integer status,
            @Param("effectiveTime") java.util.Date effectiveTime, @Param("offlineTime") java.util.Date offlineTime,
            @Param("operator") Long operator);

    int softDelete(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("noticeId") Long noticeId, @Param("operator") Long operator);

    List<Long> selectTargetRoleIds(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("noticeId") Long noticeId);

    int deleteTargetRoles(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("noticeId") Long noticeId, @Param("operator") Long operator);

    int insertTargetRoles(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("noticeId") Long noticeId, @Param("roleIds") Long[] roleIds, @Param("operator") Long operator);

    int insertRead(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("merchantUserId") Long merchantUserId, @Param("noticeId") Long noticeId);

    int insertReadAll(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("merchantUserId") Long merchantUserId, @Param("noticeIds") Long[] noticeIds);
}
