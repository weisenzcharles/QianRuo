package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.rentalbusiness.model.MerchantAfterSalesCaseVO;
import com.ruoyi.rentalbusiness.model.MerchantAfterSalesCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantAfterSalesEvidenceVO;
import com.ruoyi.rentalbusiness.model.MerchantAfterSalesEventVO;
import com.ruoyi.rentalbusiness.model.MerchantAfterSalesQuery;

/** 商户订单售后单数据访问。所有查询均限制租户、商户和软删除范围。 */
public interface MerchantAfterSalesMapper
{
    List<MerchantAfterSalesCaseVO> selectList(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("query") MerchantAfterSalesQuery query);

    MerchantAfterSalesCaseVO selectByCaseNoForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("caseNo") String caseNo);

    MerchantAfterSalesCaseVO selectProcessingByOrderForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("orderId") Long orderId);

    int countProcessingByOrder(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("orderId") Long orderId);

    int insertCase(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("orderId") Long orderId, @Param("caseNo") String caseNo,
            @Param("command") MerchantAfterSalesCreateCommand command, @Param("operatorId") Long operatorId);

    int updateStatusByCaseNo(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("caseNo") String caseNo, @Param("fromStatus") Integer fromStatus,
            @Param("toStatus") Integer toStatus, @Param("operatorId") Long operatorId,
            @Param("remark") String remark);

    /** 保存当前商户售后单关联的平台客服工单编码。 */
    int updatePlatformTicketNo(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("caseId") Long caseId, @Param("platformTicketNo") String platformTicketNo,
            @Param("operatorId") Long operatorId);

    /** 查询当前商户指定售后单的处理时间线。 */
    List<MerchantAfterSalesEventVO> selectEvents(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("caseId") Long caseId);

    /** 查询当前商户指定售后单的证据记录。 */
    List<MerchantAfterSalesEvidenceVO> selectEvidence(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("caseId") Long caseId);

    /** 追加售后处理事件。 */
    int insertEvent(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("caseId") Long caseId, @Param("eventType") Integer eventType,
            @Param("fromStatus") Integer fromStatus, @Param("toStatus") Integer toStatus,
            @Param("content") String content, @Param("operatorId") Long operatorId,
            @Param("operatorName") String operatorName);

    /** 追加售后证据。 */
    int insertEvidence(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("caseId") Long caseId, @Param("evidenceType") Integer evidenceType,
            @Param("fileUrl") String fileUrl, @Param("fileName") String fileName,
            @Param("fileMime") String fileMime, @Param("description") String description,
            @Param("operatorId") Long operatorId, @Param("operatorName") String operatorName);
}
