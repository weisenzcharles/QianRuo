package com.ruoyi.system.rental;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.system.mapper.rental.PlatformGovernanceMapper;
import com.ruoyi.system.api.model.PlatformAfterSalesTicketCommand;
import com.ruoyi.system.api.model.PlatformAfterSalesTicketView;
import com.ruoyi.system.api.model.MerchantCreditAppealCommand;
import com.ruoyi.system.api.model.MerchantCreditAppealView;
import com.ruoyi.system.model.rental.PlatformKnowledgeArticleVO;
import com.ruoyi.system.model.rental.PlatformKnowledgeAttachmentCommand;
import com.ruoyi.system.model.rental.PlatformKnowledgeAttachmentVO;
import com.ruoyi.system.model.rental.PlatformKnowledgeCategoryVO;
import com.ruoyi.system.model.rental.PlatformKnowledgeQuery;
import com.ruoyi.system.model.rental.PlatformKnowledgeVersionVO;
import com.ruoyi.system.model.rental.PlatformKnowledgeViolationCommand;
import com.ruoyi.system.model.rental.PlatformOperationsRiskVO;
import com.ruoyi.system.model.rental.PlatformRecycleQuery;
import com.ruoyi.system.model.rental.PlatformRecycleRecordVO;
import com.ruoyi.system.model.rental.PlatformTicketAssignmentVO;
import com.ruoyi.system.model.rental.PlatformTicketAssigneeVO;
import com.ruoyi.system.model.rental.PlatformTicketCreateCommand;
import com.ruoyi.system.model.rental.PlatformTicketEventVO;
import com.ruoyi.system.model.rental.PlatformTicketQuery;
import com.ruoyi.system.model.rental.PlatformTicketVO;
import com.ruoyi.system.service.rental.PlatformGovernanceService;

/** 平台治理服务的关键状态转移测试。 */
public class PlatformGovernanceServiceTest
{
    private StubGovernanceMapper mapper;
    private PlatformGovernanceService service;

    @Before
    public void setUp()
    {
        SecurityContextHolder.setUserId("1");
        mapper = new StubGovernanceMapper();
        service = new PlatformGovernanceService(mapper);
    }

    @After
    public void tearDown()
    {
        SecurityContextHolder.remove();
    }

    @Test
    public void createTicket_writesCreationEvent()
    {
        PlatformTicketCreateCommand command = new PlatformTicketCreateCommand();
        command.setTitle("订单履约异常");
        command.setContent("需要平台协助处理");

        PlatformTicketVO result = service.createTicket(command);

        Assert.assertEquals(Long.valueOf(88L), result.getTicketId());
        Assert.assertEquals(Integer.valueOf(1), mapper.lastEventType);
        Assert.assertEquals(Integer.valueOf(0), mapper.lastEventStatus);
        Assert.assertEquals(Long.valueOf(1L), mapper.lastOperator);
    }

    @Test
    public void createAfterSalesTicket_reusesExistingReference()
    {
        PlatformAfterSalesTicketCommand command = new PlatformAfterSalesTicketCommand();
        command.setMemberId(27L);
        command.setCaseId(99L);
        command.setOrderId(7001L);
        command.setCaseNo("AS0000000099");
        command.setOrderNo("OD0000007001");
        command.setCaseTypeName("损坏");
        command.setProblemDesc("设备外观损坏");

        PlatformAfterSalesTicketView first = service.createAfterSalesTicket(command);
        PlatformAfterSalesTicketView second = service.createAfterSalesTicket(command);

        Assert.assertEquals("AS-TKT-99", first.getTicketNo());
        Assert.assertEquals(first.getTicketNo(), second.getTicketNo());
        Assert.assertEquals(1, mapper.afterSalesTicketInsertCount);
        Assert.assertEquals(1, mapper.afterSalesTicketEventCount);
    }

    @Test
    public void createCreditAppealTicket_reusesExistingReference()
    {
        mapper.creditRecord = Map.of(
                "creditRecordId", 101L,
                "recordNo", "CR-1",
                "scoreBefore", 100,
                "scoreAfter", 80,
                "reversalOfRecordNo", "");
        MerchantCreditAppealCommand command = new MerchantCreditAppealCommand();
        command.setRecordNo("CR-1");
        command.setReason("该扣分流水对应的订单已经完成复核");

        MerchantCreditAppealView first = service.createCreditAppealTicket(27L, command);
        MerchantCreditAppealView second = service.createCreditAppealTicket(27L, command);

        Assert.assertEquals("CA-TKT-101", first.getTicketNo());
        Assert.assertEquals(first.getTicketNo(), second.getTicketNo());
        Assert.assertEquals("CR-1", first.getRecordNo());
        Assert.assertEquals(1, mapper.merchantCreditAppealInsertCount);
    }

    @Test
    public void offlineKnowledge_draftArticle_isRejected()
    {
        PlatformKnowledgeArticleVO article = new PlatformKnowledgeArticleVO();
        article.setArticleId(9L);
        article.setStatus(Integer.valueOf(0));
        mapper.article = article;

        try
        {
            service.offlineKnowledge(9L);
            Assert.fail("草稿文章不能直接下架");
        }
        catch (ServiceException expected)
        {
            Assert.assertEquals("只有已发布文章可以下架", expected.getMessage());
        }
    }

    @Test
    public void cleanupExpiredRecycleRecords_marksRowsWithSystemRemark()
    {
        mapper.expiredRows = 3;

        Assert.assertEquals(3, service.cleanupExpiredRecycleRecords());
        Assert.assertEquals(Long.valueOf(0L), mapper.expiredOperator);
        Assert.assertEquals("超过 30 天保留期限，系统自动清理", mapper.expiredRemark);
    }

    @Test
    public void ticketAssignees_returnsEnabledPlatformUsers()
    {
        PlatformTicketAssigneeVO assignee = new PlatformTicketAssigneeVO();
        assignee.setUserId(7L);
        assignee.setNickName("客服一号");
        mapper.assignees = Collections.singletonList(assignee);

        List<PlatformTicketAssigneeVO> result = service.ticketAssignees();

        Assert.assertEquals(1, result.size());
        Assert.assertEquals(Long.valueOf(7L), result.get(0).getUserId());
        Assert.assertEquals("客服一号", result.get(0).getNickName());
    }

    @Test
    public void deleteKnowledge_platformArticleCreatesRecycleBeforeSoftDelete()
    {
        PlatformKnowledgeArticleVO article = new PlatformKnowledgeArticleVO();
        article.setArticleId(9L);
        article.setSourceType(Integer.valueOf(1));
        article.setTitle("平台发货规则");
        mapper.article = article;

        service.deleteKnowledge(9L);

        Assert.assertEquals(1, mapper.knowledgeRecycleInsertCount);
        Assert.assertEquals(1, mapper.knowledgeDeleteCount);
        Assert.assertEquals(Long.valueOf(1L), mapper.knowledgeOperator);
    }

    @Test
    public void deleteKnowledge_merchantArticleIsRejected()
    {
        PlatformKnowledgeArticleVO article = new PlatformKnowledgeArticleVO();
        article.setArticleId(9L);
        article.setSourceType(Integer.valueOf(2));
        article.setTitle("商户内部规则");
        mapper.article = article;

        try
        {
            service.deleteKnowledge(9L);
            Assert.fail("平台不应删除商户原文");
        }
        catch (ServiceException expected)
        {
            Assert.assertEquals("商户知识文章不能由平台删除", expected.getMessage());
        }
        Assert.assertEquals(0, mapper.knowledgeRecycleInsertCount);
        Assert.assertEquals(0, mapper.knowledgeDeleteCount);
    }

    @Test
    public void offlineMerchantKnowledge_requiresReasonAndWritesViolation()
    {
        PlatformKnowledgeArticleVO article = new PlatformKnowledgeArticleVO();
        article.setArticleId(9L);
        article.setMemberId(27L);
        article.setSourceType(Integer.valueOf(2));
        article.setStatus(Integer.valueOf(1));
        mapper.complianceArticle = article;
        PlatformKnowledgeViolationCommand command = new PlatformKnowledgeViolationCommand();
        command.setReason("正文包含不适合公开传播的内容");

        service.offlineMerchantKnowledge(9L, command);

        Assert.assertEquals(1, mapper.knowledgeViolationCount);
        Assert.assertEquals("正文包含不适合公开传播的内容", mapper.knowledgeViolationReason);
        Assert.assertEquals(1, mapper.merchantKnowledgeStatusCount);
        Assert.assertEquals(1, mapper.merchantKnowledgeVersionStatusCount);
    }

    @Test
    public void offlineMerchantKnowledge_draftArticleIsRejected()
    {
        PlatformKnowledgeArticleVO article = new PlatformKnowledgeArticleVO();
        article.setArticleId(9L);
        article.setMemberId(27L);
        article.setSourceType(Integer.valueOf(2));
        article.setStatus(Integer.valueOf(0));
        mapper.complianceArticle = article;
        PlatformKnowledgeViolationCommand command = new PlatformKnowledgeViolationCommand();
        command.setReason("违规原因");

        try
        {
            service.offlineMerchantKnowledge(9L, command);
            Assert.fail("草稿商户文章不能违规下架");
        }
        catch (ServiceException expected)
        {
            Assert.assertEquals("只有已发布商户文章可以违规下架", expected.getMessage());
        }
        Assert.assertEquals(0, mapper.knowledgeViolationCount);
    }

    private static class StubGovernanceMapper implements PlatformGovernanceMapper
    {
        private final PlatformTicketVO ticket = ticket();
        private PlatformKnowledgeArticleVO article;
        private PlatformKnowledgeArticleVO complianceArticle;
        private Integer lastEventType;
        private Integer lastEventStatus;
        private Long lastOperator;
        private int expiredRows;
        private Long expiredOperator;
        private String expiredRemark;
        private List<PlatformTicketAssigneeVO> assignees = Collections.emptyList();
        private PlatformTicketVO ticketByRef;
        private int afterSalesTicketInsertCount;
        private int afterSalesTicketEventCount;
        private Map<String, Object> creditRecord;
        private int merchantCreditAppealInsertCount;
        private int knowledgeRecycleInsertCount;
        private int knowledgeDeleteCount;
        private Long knowledgeOperator;
        private int knowledgeViolationCount;
        private int merchantKnowledgeStatusCount;
        private int merchantKnowledgeVersionStatusCount;
        private String knowledgeViolationReason;

        private static PlatformTicketVO ticket()
        {
            PlatformTicketVO value = new PlatformTicketVO();
            value.setTicketId(88L);
            value.setTicketStatus(Integer.valueOf(0));
            value.setTicketNo("AS-TKT-99");
            return value;
        }

        @Override public int countOpenTickets() { return 0; }
        @Override public int upsertRiskEvent(String riskEventKey, Integer riskType, Integer riskLevel, String subject,
                Long riskCount, String detail, Long operator) { return 1; }
        @Override public PlatformOperationsRiskVO selectRiskEventByKey(String riskEventKey) { return null; }
        @Override public int resolveMissingRiskEvents(List<String> riskEventKeys, Long operator) { return 1; }
        @Override public PlatformOperationsRiskVO selectRiskEventById(Long riskEventId) { return null; }
        @Override public int acknowledgeRiskEvent(Long riskEventId, Long operator) { return 1; }

        @Override public List<PlatformTicketVO> selectTickets(PlatformTicketQuery query) { return Collections.emptyList(); }
        @Override public PlatformTicketVO selectTicket(Long ticketId) { return ticket; }
        @Override public PlatformTicketVO selectTicketByRef(String refType, Long refId) { return ticketByRef; }
        @Override public List<PlatformTicketEventVO> selectTicketEvents(Long ticketId) { return Collections.emptyList(); }
        @Override public List<PlatformTicketAssignmentVO> selectTicketAssignments(Long ticketId) { return Collections.emptyList(); }
        @Override public List<PlatformTicketAssigneeVO> selectTicketAssignees() { return assignees; }
        @Override public int insertTicket(String ticketNo, Long memberId, Integer ticketType, String refType, Long refId, String title, String content, Integer priority, Long requesterUserId) { return 1; }
        @Override public int insertAfterSalesTicket(String ticketNo, Long memberId, Long refId, String title,
                String content, Integer priority)
        {
            afterSalesTicketInsertCount++;
            ticketByRef = ticket();
            return 1;
        }
        @Override public Map<String, Object> selectCreditRecordForAppeal(Long memberId, String recordNo)
        {
            return creditRecord;
        }
        @Override public int insertMerchantCreditAppealTicket(String ticketNo, Long memberId, Long refId,
                String title, String content, Integer priority)
        {
            merchantCreditAppealInsertCount++;
            PlatformTicketVO value = ticket();
            value.setTicketNo(ticketNo);
            ticketByRef = value;
            return 1;
        }
        @Override public int insertTicketEvent(Long ticketId, Integer eventType, Integer fromStatus, Integer toStatus, String detail, Long actorUserId)
        {
            if ("商户申请平台介入".equals(detail))
                afterSalesTicketEventCount++;
            lastEventType = eventType;
            lastEventStatus = toStatus;
            lastOperator = actorUserId;
            return 1;
        }
        @Override public int updateTicket(Long ticketId, Integer toStatus, Long assigneeUserId, Long operator) { return 1; }
        @Override public int insertTicketAssignment(Long ticketId, Long assigneeUserId, Long assignedBy) { return 1; }
        @Override public int countActiveTicketAssignee(Long userId) { return 1; }
        @Override public List<PlatformKnowledgeCategoryVO> selectKnowledgeCategories() { return Collections.emptyList(); }
        @Override public List<PlatformKnowledgeArticleVO> selectKnowledgeArticles(PlatformKnowledgeQuery query) { return Collections.emptyList(); }
        @Override public PlatformKnowledgeArticleVO selectKnowledgeArticle(Long articleId) { return article; }
        @Override public List<PlatformKnowledgeArticleVO> selectKnowledgeComplianceArticles(PlatformKnowledgeQuery query) { return Collections.emptyList(); }
        @Override public PlatformKnowledgeArticleVO selectKnowledgeComplianceArticle(Long articleId) { return complianceArticle; }
        @Override public List<PlatformKnowledgeVersionVO> selectKnowledgeVersions(Long articleId) { return Collections.emptyList(); }
        @Override public List<PlatformKnowledgeVersionVO> selectKnowledgeComplianceVersions(Long articleId) { return Collections.emptyList(); }
        @Override public int insertKnowledgeArticle(Long memberId, Long categoryId, String title, Integer sourceType, Integer audienceType, Integer pinned, Long operator) { return 1; }
        @Override public Long selectLastInsertId() { return 88L; }
        @Override public int insertKnowledgeVersion(Long articleId, Integer versionNo, String content, Integer versionStatus, String retentionUntil, Long operator) { return 1; }
        @Override public List<PlatformKnowledgeAttachmentVO> selectKnowledgeVersionAttachments(Long versionId) { return Collections.emptyList(); }
        @Override public Long selectKnowledgeVersionId(Long articleId, Integer versionNo) { return 88L; }
        @Override public int insertKnowledgeVersionAttachments(Long versionId, List<PlatformKnowledgeAttachmentCommand> attachments, Long operator) { return attachments == null ? 0 : attachments.size(); }
        @Override public int updateKnowledgeArticle(Long articleId, Long categoryId, String title, Integer pinned, Integer versionNo, Long operator) { return 1; }
        @Override public int updateKnowledgeStatus(Long articleId, Integer fromStatus, Integer toStatus, Long operator) { return 1; }
        @Override public int updateKnowledgeVersionStatus(Long articleId, Integer versionStatus, Long operator) { return 1; }
        @Override public int updateMerchantKnowledgeStatus(Long articleId, Integer fromStatus, Integer toStatus, Long operator) { merchantKnowledgeStatusCount++; return 1; }
        @Override public int updateMerchantKnowledgeVersionStatus(Long articleId, Integer versionStatus, Long operator) { merchantKnowledgeVersionStatusCount++; return 1; }
        @Override public int insertKnowledgeViolation(Long articleId, Long memberId, Integer actionType, String reason, Long operator)
        {
            knowledgeViolationCount++;
            knowledgeViolationReason = reason;
            return 1;
        }
        @Override public int insertKnowledgeRecycle(Long articleId, Long operator)
        {
            knowledgeRecycleInsertCount++;
            knowledgeOperator = operator;
            return 1;
        }
        @Override public int deleteKnowledgeArticle(Long articleId, Long operator)
        {
            knowledgeDeleteCount++;
            knowledgeOperator = operator;
            return 1;
        }
        @Override public List<PlatformRecycleRecordVO> selectRecycleRecords(PlatformRecycleQuery query) { return Collections.emptyList(); }
        @Override public PlatformRecycleRecordVO selectRecycleRecord(Long recycleId) { return null; }
        @Override public int restoreKnowledgeArticle(Long articleId, Long operator) { return 1; }
        @Override public int restoreMerchantNotice(Long noticeId, Long operator) { return 1; }
        @Override public int restoreMerchantNoticeMembers(Long noticeId, Long operator) { return 1; }
        @Override public int restoreMerchantNoticeReads(Long noticeId) { return 1; }
        @Override public int insertMerchantNoticeRecycle(Long noticeId, Long operator) { return 1; }
        @Override public int markRecycleRestored(Long recycleId, Long operator, String remark) { return 1; }
        @Override public int markExpiredRecycleRecords(Long operator, String remark)
        {
            expiredOperator = operator;
            expiredRemark = remark;
            return expiredRows;
        }
    }
}
