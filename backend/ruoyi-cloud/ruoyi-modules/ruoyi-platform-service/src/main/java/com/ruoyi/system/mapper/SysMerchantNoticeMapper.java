package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.api.model.MerchantNoticeDetail;
import com.ruoyi.system.api.model.MerchantNoticeSummary;
import com.ruoyi.system.domain.SysMerchantNotice;
import com.ruoyi.system.model.rental.MerchantNoticeEditVO;
import com.ruoyi.system.model.rental.MerchantNoticeListVO;
import com.ruoyi.system.model.rental.MerchantNoticeMemberOption;

/** 商户公告主表及接收范围数据访问层。 */
public interface SysMerchantNoticeMapper
{
    List<MerchantNoticeListVO> selectMerchantNoticeList(SysMerchantNotice notice);

    MerchantNoticeEditVO selectMerchantNoticeById(@Param("merchantNoticeId") Long merchantNoticeId);

    List<Long> selectTargetMemberIds(@Param("merchantNoticeId") Long merchantNoticeId);

    List<MerchantNoticeMemberOption> selectMemberOptions();

    int insertMerchantNotice(SysMerchantNotice notice);

    int insertTargetMembers(SysMerchantNotice notice);

    int updateMerchantNotice(SysMerchantNotice notice);

    int deleteTargetMembers(@Param("merchantNoticeId") Long merchantNoticeId);

    int deleteMerchantNotices(@Param("noticeIds") Long[] noticeIds, @Param("operator") Long operator);

    List<MerchantNoticeSummary> selectTopForMerchant(@Param("memberId") Long memberId,
            @Param("merchantUserId") Long merchantUserId, @Param("limit") Integer limit);

    int selectUnreadCountForMerchant(@Param("memberId") Long memberId,
            @Param("merchantUserId") Long merchantUserId);

    MerchantNoticeDetail selectDetailForMerchant(@Param("noticeId") Long noticeId,
            @Param("memberId") Long memberId, @Param("merchantUserId") Long merchantUserId);
}
