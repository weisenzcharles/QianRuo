package com.ruoyi.merchant.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.merchant.model.MerchantMarketMessageRecord;
import com.ruoyi.merchant.model.MerchantMarketReportRecord;
import com.ruoyi.merchant.model.MerchantMarketPostRecord;

/** 商户周转市场帖子数据访问，所有查询都带当前租户和商户范围。 */
public interface MerchantMarketPostMapper
{
    List<MerchantMarketPostRecord> selectList(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("keyword") String keyword,
            @Param("boardType") Integer boardType, @Param("postStatus") Integer postStatus,
            @Param("city") String city);

    List<MerchantMarketPostRecord> selectPublicList(@Param("memberId") Long memberId,
            @Param("keyword") String keyword, @Param("boardType") Integer boardType,
            @Param("city") String city);

    MerchantMarketPostRecord selectPublicByPostNo(@Param("postNo") String postNo);

    MerchantMarketPostRecord selectByPostNo(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("postNo") String postNo);

    MerchantMarketPostRecord selectByPostNoForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("postNo") String postNo);

    int insert(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("postNo") String postNo, @Param("boardType") Integer boardType,
            @Param("category") String category, @Param("brand") String brand,
            @Param("modelName") String modelName, @Param("memorySpec") String memorySpec,
            @Param("conditionLevel") String conditionLevel, @Param("quantity") Integer quantity,
            @Param("priceRangeCode") String priceRangeCode, @Param("city") String city,
            @Param("serviceTags") String serviceTags, @Param("remark") String remark,
            @Param("operatorId") Long operatorId);

    Long selectLastInsertId();

    List<Long> selectInventoryUnitIds(@Param("postId") Long postId);

    int removeInventoryBindings(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("postId") Long postId, @Param("operatorId") Long operatorId);

    int insertInventoryBinding(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("postId") Long postId, @Param("unitId") Long unitId, @Param("unitCode") String unitCode,
            @Param("serialNumber") String serialNumber, @Param("operatorId") Long operatorId);

    int updateDraft(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("postNo") String postNo, @Param("boardType") Integer boardType,
            @Param("category") String category, @Param("brand") String brand,
            @Param("modelName") String modelName, @Param("memorySpec") String memorySpec,
            @Param("conditionLevel") String conditionLevel, @Param("quantity") Integer quantity,
            @Param("priceRangeCode") String priceRangeCode, @Param("city") String city,
            @Param("serviceTags") String serviceTags, @Param("remark") String remark,
            @Param("operatorId") Long operatorId);

    int updateStatus(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("postNo") String postNo, @Param("postStatus") Integer postStatus,
            @Param("operatorId") Long operatorId);

    List<MerchantMarketMessageRecord> selectMessages(@Param("memberId") Long memberId,
            @Param("readFlag") Integer readFlag, @Param("keyword") String keyword);

    int insertMessage(@Param("tenantId") Long tenantId, @Param("postId") Long postId,
            @Param("fromMemberId") Long fromMemberId, @Param("toMemberId") Long toMemberId,
            @Param("content") String content);

    int markMessageRead(@Param("messageId") Long messageId, @Param("memberId") Long memberId);

    List<MerchantMarketReportRecord> selectReports(@Param("memberId") Long memberId);

    MerchantMarketReportRecord selectPendingReport(@Param("postId") Long postId,
            @Param("reporterMemberId") Long reporterMemberId, @Param("reportType") String reportType);

    int insertReport(@Param("tenantId") Long tenantId, @Param("postId") Long postId,
            @Param("reporterMemberId") Long reporterMemberId, @Param("reportedMemberId") Long reportedMemberId,
            @Param("reportType") String reportType, @Param("evidenceJson") String evidenceJson);
}
