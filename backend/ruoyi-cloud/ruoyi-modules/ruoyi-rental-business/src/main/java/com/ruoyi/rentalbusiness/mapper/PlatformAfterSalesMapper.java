package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.api.model.PlatformAfterSalesEventView;
import com.ruoyi.system.api.model.PlatformAfterSalesEvidenceView;
import com.ruoyi.system.api.model.PlatformAfterSalesQuery;
import com.ruoyi.system.api.model.PlatformAfterSalesView;

/** 平台售后案件数据访问，负责跨商户查询和受控状态写入。 */
public interface PlatformAfterSalesMapper
{
    long countCases(@Param("query") PlatformAfterSalesQuery query);

    List<PlatformAfterSalesView> selectCases(@Param("query") PlatformAfterSalesQuery query,
            @Param("offset") int offset, @Param("pageSize") int pageSize);

    PlatformAfterSalesView selectCase(@Param("caseId") Long caseId);

    PlatformAfterSalesView selectCaseForUpdate(@Param("caseId") Long caseId);

    /** 查询指定订单的全部售后案件，订单详情只读页面使用。 */
    List<PlatformAfterSalesView> selectCasesByOrder(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("orderId") Long orderId);

    List<PlatformAfterSalesEventView> selectEvents(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("caseId") Long caseId);

    List<PlatformAfterSalesEvidenceView> selectEvidence(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("caseId") Long caseId);

    int updateCaseStatus(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("caseId") Long caseId, @Param("fromStatus") Integer fromStatus,
            @Param("toStatus") Integer toStatus, @Param("operatorId") Long operatorId,
            @Param("remark") String remark);

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

    int countProcessingByOrder(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("orderId") Long orderId);

    int updateOrderStatus(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("orderId") Long orderId, @Param("fromStatus") Integer fromStatus,
            @Param("toStatus") Integer toStatus, @Param("operatorId") Long operatorId);

    int insertOrderEditLog(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("orderId") Long orderId, @Param("fieldName") String fieldName,
            @Param("beforeValue") String beforeValue, @Param("afterValue") String afterValue,
            @Param("beforeDisplayValue") String beforeDisplayValue,
            @Param("afterDisplayValue") String afterDisplayValue,
            @Param("changeReason") String changeReason, @Param("operatorId") Long operatorId,
            @Param("operatorName") String operatorName);
}
