package com.ruoyi.system.mapper.rental;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.model.rental.PlatformMarketPostVO;
import com.ruoyi.system.model.rental.PlatformMarketQuery;
import com.ruoyi.system.model.rental.PlatformMarketReportVO;

/** 平台周转市场治理数据访问，统一补齐商户名称并保留平台操作范围。 */
public interface PlatformMarketMapper
{
    List<PlatformMarketPostVO> selectPosts(@Param("query") PlatformMarketQuery query);

    PlatformMarketPostVO selectPost(@Param("postId") Long postId);

    List<PlatformMarketReportVO> selectReports(@Param("query") PlatformMarketQuery query);

    PlatformMarketReportVO selectReport(@Param("reportId") Long reportId);

    int reviewReport(@Param("reportId") Long reportId, @Param("reportStatus") Integer reportStatus,
            @Param("reviewResult") String reviewResult, @Param("reviewerId") Long reviewerId);

    int updatePostStatus(@Param("postId") Long postId, @Param("postStatus") Integer postStatus,
            @Param("operatorId") Long operatorId);

    int updatePostRank(@Param("postId") Long postId, @Param("downRankFlag") Integer downRankFlag,
            @Param("operatorId") Long operatorId);
}
