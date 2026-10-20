package com.ruoyi.system.mapper.rental;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.model.rental.PlatformKnowledgeArticleVO;
import com.ruoyi.system.model.rental.PlatformKnowledgeAttachmentCommand;
import com.ruoyi.system.model.rental.PlatformKnowledgeAttachmentVO;
import com.ruoyi.system.model.rental.PlatformKnowledgeCategoryVO;
import com.ruoyi.system.model.rental.PlatformKnowledgeQuery;
import com.ruoyi.system.model.rental.PlatformKnowledgeVersionVO;
import com.ruoyi.system.model.rental.PlatformRecycleQuery;
import com.ruoyi.system.model.rental.PlatformRecycleRecordVO;
import com.ruoyi.system.model.rental.PlatformTicketAssignmentVO;
import com.ruoyi.system.model.rental.PlatformTicketAssigneeVO;
import com.ruoyi.system.model.rental.PlatformTicketEventVO;
import com.ruoyi.system.model.rental.PlatformTicketQuery;
import com.ruoyi.system.model.rental.PlatformTicketVO;
import com.ruoyi.system.model.rental.PlatformOperationsRiskVO;

/** 平台工单、知识库和回收站数据访问组件。 */
public interface PlatformGovernanceMapper
{
    /** 查询未关闭的工单数量，供平台运营风险概览使用。 */
    int countOpenTickets();

    /** 写入或刷新平台运营风险事件，事件状态由数据库保留。 */
    int upsertRiskEvent(@Param("riskEventKey") String riskEventKey, @Param("riskType") Integer riskType,
            @Param("riskLevel") Integer riskLevel, @Param("subject") String subject,
            @Param("riskCount") Long riskCount, @Param("detail") String detail,
            @Param("operator") Long operator);

    PlatformOperationsRiskVO selectRiskEventByKey(@Param("riskEventKey") String riskEventKey);

    /** 将本次检查未再次出现的待处理风险自动标记为已解除。 */
    int resolveMissingRiskEvents(@Param("riskEventKeys") List<String> riskEventKeys,
            @Param("operator") Long operator);

    PlatformOperationsRiskVO selectRiskEventById(@Param("riskEventId") Long riskEventId);

    int acknowledgeRiskEvent(@Param("riskEventId") Long riskEventId, @Param("operator") Long operator);

    List<PlatformTicketVO> selectTickets(@Param("query") PlatformTicketQuery query);

    PlatformTicketVO selectTicket(@Param("ticketId") Long ticketId);

    /** 按业务引用查询已有平台工单，用于售后申请平台介入幂等复用。 */
    PlatformTicketVO selectTicketByRef(@Param("refType") String refType, @Param("refId") Long refId);

    List<PlatformTicketEventVO> selectTicketEvents(@Param("ticketId") Long ticketId);

    List<PlatformTicketAssignmentVO> selectTicketAssignments(@Param("ticketId") Long ticketId);

    /** 查询启用的平台用户，供客服工单分配使用。 */
    List<PlatformTicketAssigneeVO> selectTicketAssignees();

    /** 校验工单处理人仍是启用的平台用户。 */
    int countActiveTicketAssignee(@Param("userId") Long userId);

    int insertTicket(@Param("ticketNo") String ticketNo, @Param("memberId") Long memberId,
            @Param("ticketType") Integer ticketType, @Param("refType") String refType, @Param("refId") Long refId,
            @Param("title") String title, @Param("content") String content, @Param("priority") Integer priority,
            @Param("requesterUserId") Long requesterUserId);

    /** 幂等新增售后平台工单，已存在同一售后引用时返回 0。 */
    int insertAfterSalesTicket(@Param("ticketNo") String ticketNo, @Param("memberId") Long memberId,
            @Param("refId") Long refId, @Param("title") String title, @Param("content") String content,
            @Param("priority") Integer priority);

    /** 查询商户申诉所指向的信用流水，返回内部唯一编号和业务快照。 */
    java.util.Map<String, Object> selectCreditRecordForAppeal(@Param("memberId") Long memberId,
            @Param("recordNo") String recordNo);

    /** 幂等新增商户信用申诉工单，工单来源标记为商户。 */
    int insertMerchantCreditAppealTicket(@Param("ticketNo") String ticketNo, @Param("memberId") Long memberId,
            @Param("refId") Long refId, @Param("title") String title, @Param("content") String content,
            @Param("priority") Integer priority);

    int insertTicketEvent(@Param("ticketId") Long ticketId, @Param("eventType") Integer eventType,
            @Param("fromStatus") Integer fromStatus, @Param("toStatus") Integer toStatus,
            @Param("detail") String detail, @Param("actorUserId") Long actorUserId);

    int updateTicket(@Param("ticketId") Long ticketId, @Param("toStatus") Integer toStatus,
            @Param("assigneeUserId") Long assigneeUserId, @Param("operator") Long operator);

    int insertTicketAssignment(@Param("ticketId") Long ticketId, @Param("assigneeUserId") Long assigneeUserId,
            @Param("assignedBy") Long assignedBy);

    List<PlatformKnowledgeCategoryVO> selectKnowledgeCategories();

    List<PlatformKnowledgeArticleVO> selectKnowledgeArticles(@Param("query") PlatformKnowledgeQuery query);

    /** 查询商户知识合规列表，只返回文章元数据，不返回正文。 */
    List<PlatformKnowledgeArticleVO> selectKnowledgeComplianceArticles(@Param("query") PlatformKnowledgeQuery query);

    PlatformKnowledgeArticleVO selectKnowledgeArticle(@Param("articleId") Long articleId);

    /** 查询单篇商户知识合规详情，只有合规权限接口可以调用。 */
    PlatformKnowledgeArticleVO selectKnowledgeComplianceArticle(@Param("articleId") Long articleId);

    List<PlatformKnowledgeVersionVO> selectKnowledgeVersions(@Param("articleId") Long articleId);

    /** 查询商户知识版本正文，调用方必须已经通过合规权限校验。 */
    List<PlatformKnowledgeVersionVO> selectKnowledgeComplianceVersions(@Param("articleId") Long articleId);

    List<PlatformKnowledgeAttachmentVO> selectKnowledgeVersionAttachments(@Param("versionId") Long versionId);

    int insertKnowledgeArticle(@Param("memberId") Long memberId, @Param("categoryId") Long categoryId,
            @Param("title") String title, @Param("sourceType") Integer sourceType,
            @Param("audienceType") Integer audienceType, @Param("pinned") Integer pinned,
            @Param("operator") Long operator);

    Long selectLastInsertId();

    int insertKnowledgeVersion(@Param("articleId") Long articleId, @Param("versionNo") Integer versionNo,
            @Param("content") String content, @Param("versionStatus") Integer versionStatus,
            @Param("retentionUntil") String retentionUntil, @Param("operator") Long operator);

    Long selectKnowledgeVersionId(@Param("articleId") Long articleId, @Param("versionNo") Integer versionNo);

    int insertKnowledgeVersionAttachments(@Param("versionId") Long versionId,
            @Param("attachments") List<PlatformKnowledgeAttachmentCommand> attachments,
            @Param("operator") Long operator);

    int updateKnowledgeArticle(@Param("articleId") Long articleId, @Param("categoryId") Long categoryId,
            @Param("title") String title, @Param("pinned") Integer pinned, @Param("versionNo") Integer versionNo,
            @Param("operator") Long operator);

    int updateKnowledgeStatus(@Param("articleId") Long articleId, @Param("fromStatus") Integer fromStatus,
            @Param("toStatus") Integer toStatus, @Param("operator") Long operator);

    int updateKnowledgeVersionStatus(@Param("articleId") Long articleId, @Param("versionStatus") Integer versionStatus,
            @Param("operator") Long operator);

    /** 将商户文章标记为平台违规下架，不允许修改商户文章正文。 */
    int updateMerchantKnowledgeStatus(@Param("articleId") Long articleId, @Param("fromStatus") Integer fromStatus,
            @Param("toStatus") Integer toStatus, @Param("operator") Long operator);

    int updateMerchantKnowledgeVersionStatus(@Param("articleId") Long articleId,
            @Param("versionStatus") Integer versionStatus, @Param("operator") Long operator);

    /** 追加不可变的违规处置记录。 */
    int insertKnowledgeViolation(@Param("articleId") Long articleId, @Param("memberId") Long memberId,
            @Param("actionType") Integer actionType, @Param("reason") String reason, @Param("operator") Long operator);

    /** 删除平台知识文章前写入可恢复快照。 */
    int insertKnowledgeRecycle(@Param("articleId") Long articleId, @Param("operator") Long operator);

    /** 软删除平台知识文章，历史版本保留用于恢复和审计。 */
    int deleteKnowledgeArticle(@Param("articleId") Long articleId, @Param("operator") Long operator);

    List<PlatformRecycleRecordVO> selectRecycleRecords(@Param("query") PlatformRecycleQuery query);

    PlatformRecycleRecordVO selectRecycleRecord(@Param("recycleId") Long recycleId);

    int restoreKnowledgeArticle(@Param("articleId") Long articleId, @Param("operator") Long operator);

    int restoreMerchantNotice(@Param("noticeId") Long noticeId, @Param("operator") Long operator);

    /** 恢复商户公告的历史接收范围，避免公告恢复后丢失定向商户。 */
    int restoreMerchantNoticeMembers(@Param("noticeId") Long noticeId, @Param("operator") Long operator);

    /** 恢复商户公告的已读关系，保持恢复前的阅读状态。 */
    int restoreMerchantNoticeReads(@Param("noticeId") Long noticeId);

    /** 删除商户公告前保存回收站快照。 */
    int insertMerchantNoticeRecycle(@Param("noticeId") Long noticeId, @Param("operator") Long operator);

    int markRecycleRestored(@Param("recycleId") Long recycleId, @Param("operator") Long operator,
            @Param("remark") String remark);

    /** 将已超过保留期限的待恢复记录标记为已清理。 */
    int markExpiredRecycleRecords(@Param("operator") Long operator, @Param("remark") String remark);
}
