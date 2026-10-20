package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

/** 商户公告已读状态数据访问层。 */
public interface SysMerchantNoticeReadMapper
{
    int insertRead(@Param("noticeId") Long noticeId, @Param("memberId") Long memberId,
            @Param("merchantUserId") Long merchantUserId);

    int insertReadAll(@Param("memberId") Long memberId, @Param("merchantUserId") Long merchantUserId,
            @Param("noticeIds") List<Long> noticeIds);

    int deleteByNoticeIds(@Param("noticeIds") Long[] noticeIds);
}
