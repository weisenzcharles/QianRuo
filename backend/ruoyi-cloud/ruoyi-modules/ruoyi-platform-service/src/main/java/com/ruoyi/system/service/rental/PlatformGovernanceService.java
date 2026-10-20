package com.ruoyi.system.service.rental;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.enums.PlatformKnowledgeStatus;
import com.ruoyi.system.enums.PlatformKnowledgeViolationAction;
import com.ruoyi.system.enums.PlatformRecycleStatus;
import com.ruoyi.system.enums.PlatformTicketEventType;
import com.ruoyi.system.enums.PlatformTicketPriority;
import com.ruoyi.system.enums.PlatformTicketStatus;
import com.ruoyi.system.enums.PlatformTicketType;
import com.ruoyi.system.mapper.rental.PlatformGovernanceMapper;
import com.ruoyi.system.api.model.PlatformAfterSalesTicketCommand;
import com.ruoyi.system.api.model.PlatformAfterSalesTicketView;
import com.ruoyi.system.model.rental.PlatformKnowledgeArticleCommand;
import com.ruoyi.system.model.rental.PlatformKnowledgeArticleVO;
import com.ruoyi.system.model.rental.PlatformKnowledgeAttachmentCommand;
import com.ruoyi.system.model.rental.PlatformKnowledgeCategoryVO;
import com.ruoyi.system.model.rental.PlatformKnowledgeDetailVO;
import com.ruoyi.system.model.rental.PlatformKnowledgeQuery;
import com.ruoyi.system.model.rental.PlatformKnowledgeViolationCommand;
import com.ruoyi.system.model.rental.PlatformKnowledgeVersionVO;
import com.ruoyi.system.model.rental.PlatformRecycleQuery;
import com.ruoyi.system.model.rental.PlatformRecycleRecordVO;
import com.ruoyi.system.model.rental.PlatformTicketAssignmentVO;
import com.ruoyi.system.model.rental.PlatformTicketAssigneeVO;
import com.ruoyi.system.model.rental.PlatformTicketCreateCommand;
import com.ruoyi.system.model.rental.PlatformTicketDetailVO;
import com.ruoyi.system.model.rental.PlatformTicketEventCommand;
import com.ruoyi.system.model.rental.PlatformTicketEventVO;
import com.ruoyi.system.model.rental.PlatformTicketQuery;
import com.ruoyi.system.model.rental.PlatformTicketVO;
import com.ruoyi.system.api.model.MerchantCreditAppealCommand;
import com.ruoyi.system.api.model.MerchantCreditAppealView;

/** 平台治理应用服务，统一处理工单时间线、知识版本和受支持对象的回收恢复。 */
@Service
public class PlatformGovernanceService
{
    private static final int MAX_KNOWLEDGE_ATTACHMENT_COUNT = 10;
    private static final int MAX_KNOWLEDGE_ATTACHMENT_NAME_LENGTH = 255;
    private static final int MAX_KNOWLEDGE_ATTACHMENT_URL_LENGTH = 1000;

    private final PlatformGovernanceMapper mapper;

    public PlatformGovernanceService(PlatformGovernanceMapper mapper)
    {
        this.mapper = mapper;
    }

    /** 查询平台工单列表，并补齐中文枚举名称。 */
    public List<PlatformTicketVO> tickets(PlatformTicketQuery query)
    {
        PlatformTicketQuery normalized = query == null ? new PlatformTicketQuery() : query;
        normalized.setTicketNo(trim(normalized.getTicketNo()));
        normalized.setMemberKeyword(trim(normalized.getMemberKeyword()));
        normalized.setKeyword(trim(normalized.getKeyword()));
        validateTicketFilters(normalized);
        List<PlatformTicketVO> rows = mapper.selectTickets(normalized);
        if (rows == null)
            return Collections.emptyList();
        for (PlatformTicketVO row : rows)
            decorateTicket(row);
        return rows;
    }

    /** 查询工单详情和追加时间线。 */
    public PlatformTicketDetailVO ticket(Long ticketId)
    {
        PlatformTicketVO ticket = requireTicket(ticketId);
        PlatformTicketDetailVO result = new PlatformTicketDetailVO();
        result.setTicket(ticket);
        result.setEvents(decorateEvents(mapper.selectTicketEvents(ticketId)));
        result.setAssignments(decorateAssignments(mapper.selectTicketAssignments(ticketId)));
        return result;
    }

    /** 查询启用的平台用户，供工单分配下拉框使用。 */
    public List<PlatformTicketAssigneeVO> ticketAssignees()
    {
        List<PlatformTicketAssigneeVO> rows = mapper.selectTicketAssignees();
        return rows == null ? Collections.emptyList() : rows;
    }

    /** 创建平台工单并追加创建事件。 */
    @Transactional(rollbackFor = Exception.class)
    public PlatformTicketVO createTicket(PlatformTicketCreateCommand command)
    {
        if (command == null)
            throw new ServiceException("工单内容不能为空");
        Integer type = command.getTicketType() == null ? PlatformTicketType.OTHER.getCode() : command.getTicketType();
        Integer priority = command.getPriority() == null ? PlatformTicketPriority.NORMAL.getCode() : command.getPriority();
        if (PlatformTicketType.fromCode(type) == null)
            throw new ServiceException("工单类型不合法");
        if (PlatformTicketPriority.fromCode(priority) == null)
            throw new ServiceException("工单优先级不合法");
        String title = trim(command.getTitle());
        String content = trim(command.getContent());
        if (StringUtils.isBlank(title) || StringUtils.isBlank(content))
            throw new ServiceException("工单标题和内容不能为空");
        Long operator = SecurityUtils.getUserId();
        String ticketNo = "TKT" + UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase(Locale.ROOT);
        if (mapper.insertTicket(ticketNo, command.getMemberId(), type, trim(command.getRefType()), command.getRefId(),
                title, content, priority, operator) != 1)
            throw new ServiceException("工单创建失败");
        Long ticketId = mapper.selectLastInsertId();
        if (ticketId == null || mapper.insertTicketEvent(ticketId, PlatformTicketEventType.CREATED.getCode(), null,
                PlatformTicketStatus.UNASSIGNED.getCode(), "工单创建", operator) != 1)
            throw new ServiceException("工单创建事件写入失败");
        return requireTicket(ticketId);
    }

    /**
     * 创建或复用售后对应的平台客服工单。
     *
     * <p>工单编码按售后唯一编号生成，配合数据库唯一的工单编码实现并发重复提交幂等；
     * 已存在的售后引用直接返回原工单，不会追加重复创建事件。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public PlatformAfterSalesTicketView createAfterSalesTicket(PlatformAfterSalesTicketCommand command)
    {
        if (command == null || command.getMemberId() == null || command.getMemberId() <= 0
                || command.getCaseId() == null || command.getCaseId() <= 0
                || command.getOrderId() == null || command.getOrderId() <= 0
                || StringUtils.isBlank(command.getCaseNo()) || StringUtils.isBlank(command.getOrderNo()))
        {
            throw new ServiceException("售后平台工单参数不完整");
        }
        PlatformTicketVO existing = mapper.selectTicketByRef("after_sales_case", command.getCaseId());
        if (existing != null)
        {
            decorateTicket(existing);
            return toAfterSalesTicketView(existing);
        }

        String caseNo = trim(command.getCaseNo());
        String orderNo = trim(command.getOrderNo());
        String title = trim("售后介入：" + caseNo + " / " + orderNo);
        String content = buildAfterSalesTicketContent(command, caseNo, orderNo);
        String ticketNo = "AS-TKT-" + command.getCaseId();
        int inserted = mapper.insertAfterSalesTicket(ticketNo, command.getMemberId(), command.getCaseId(), title,
                content, PlatformTicketPriority.IMPORTANT.getCode());
        if (inserted == 1)
        {
            Long ticketId = mapper.selectLastInsertId();
            if (ticketId == null || mapper.insertTicketEvent(ticketId, PlatformTicketEventType.CREATED.getCode(),
                    null, PlatformTicketStatus.UNASSIGNED.getCode(), "商户申请平台介入", null) != 1)
            {
                throw new ServiceException("售后平台工单创建事件写入失败");
            }
        }

        PlatformTicketVO result = mapper.selectTicketByRef("after_sales_case", command.getCaseId());
        if (result == null)
        {
            throw new ServiceException("售后平台工单创建失败，请重试");
        }
        decorateTicket(result);
        return toAfterSalesTicketView(result);
    }

    /**
     * 创建或复用商户信用申诉工单。
     *
     * <p>申诉只创建客服工单，不直接改变信用分；平台审核通过后由风险服务追加冲正流水。</p>
     *
     * @param memberId 当前商户唯一编号
     * @param command 信用流水编码和申诉理由
     * @return 申诉工单摘要
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantCreditAppealView createCreditAppealTicket(Long memberId, MerchantCreditAppealCommand command)
    {
        if (memberId == null || memberId <= 0 || command == null
                || StringUtils.isBlank(command.getRecordNo()) || StringUtils.isBlank(command.getReason()))
        {
            throw new ServiceException("信用申诉参数不完整");
        }
        String recordNo = trim(command.getRecordNo());
        String reason = trim(command.getReason());
        if (recordNo == null || recordNo.length() > 48 || reason == null || reason.length() > 1000)
        {
            throw new ServiceException("信用申诉参数不合法");
        }
        Map<String, Object> creditRecord = mapper.selectCreditRecordForAppeal(memberId, recordNo);
        if (creditRecord == null)
        {
            throw new ServiceException("信用流水不存在或无权申诉");
        }
        if (!StringUtils.isBlank(stringValue(creditRecord.get("reversalOfRecordNo"))))
        {
            throw new ServiceException("申诉冲正流水不能再次申诉");
        }
        if (intValue(creditRecord.get("scoreAfter")) >= intValue(creditRecord.get("scoreBefore")))
        {
            throw new ServiceException("只有扣分流水可以提交申诉");
        }

        Long creditRecordId = longValue(creditRecord.get("creditRecordId"));
        if (creditRecordId == null)
        {
            throw new ServiceException("信用流水数据不完整，暂不能提交申诉");
        }
        PlatformTicketVO existing = mapper.selectTicketByRef("member_credit_record", creditRecordId);
        if (existing != null)
        {
            decorateTicket(existing);
            return toCreditAppealView(existing, recordNo);
        }

        String ticketNo = "CA-TKT-" + creditRecordId;
        String title = "信用申诉：" + recordNo;
        String content = "申诉信用流水：" + recordNo + "\n申诉理由：" + reason;
        if (mapper.insertMerchantCreditAppealTicket(ticketNo, memberId, creditRecordId, title, content,
                PlatformTicketPriority.IMPORTANT.getCode()) == 1)
        {
            Long ticketId = mapper.selectLastInsertId();
            if (ticketId == null || mapper.insertTicketEvent(ticketId, PlatformTicketEventType.CREATED.getCode(),
                    null, PlatformTicketStatus.UNASSIGNED.getCode(), "商户提交信用申诉", null) != 1)
            {
                throw new ServiceException("信用申诉工单创建事件写入失败");
            }
        }
        PlatformTicketVO result = mapper.selectTicketByRef("member_credit_record", creditRecordId);
        if (result == null)
        {
            throw new ServiceException("信用申诉工单创建失败，请重试");
        }
        decorateTicket(result);
        return toCreditAppealView(result, recordNo);
    }

    /** 追加工单事件，并按事件内容推进当前状态或处理人。 */
    @Transactional(rollbackFor = Exception.class)
    public void appendTicketEvent(Long ticketId, PlatformTicketEventCommand command)
    {
        if (command == null || StringUtils.isBlank(command.getDetail()))
            throw new ServiceException("工单事件详情不能为空");
        PlatformTicketVO ticket = requireTicket(ticketId);
        Integer eventType = command.getEventType() == null ? PlatformTicketEventType.NOTE.getCode() : command.getEventType();
        if (PlatformTicketEventType.fromCode(eventType) == null)
            throw new ServiceException("工单事件类型不合法");
        Integer toStatus = command.getToStatus() == null ? ticket.getTicketStatus() : command.getToStatus();
        if (PlatformTicketStatus.fromCode(toStatus) == null)
            throw new ServiceException("工单状态不合法");
        if (eventType == PlatformTicketEventType.ASSIGNED.getCode() && command.getAssigneeUserId() == null)
            throw new ServiceException("分配工单时必须选择处理人");
        if (eventType == PlatformTicketEventType.ASSIGNED.getCode()
                && mapper.countActiveTicketAssignee(command.getAssigneeUserId()) != 1)
            throw new ServiceException("处理人不存在或已停用");
        Long operator = SecurityUtils.getUserId();
        Long assigneeUserId = command.getAssigneeUserId();
        if (mapper.updateTicket(ticketId, toStatus, assigneeUserId, operator) != 1)
            throw new ServiceException("工单状态更新失败");
        if (assigneeUserId != null && mapper.insertTicketAssignment(ticketId, assigneeUserId, operator) != 1)
            throw new ServiceException("工单指派记录写入失败");
        if (mapper.insertTicketEvent(ticketId, eventType, ticket.getTicketStatus(), toStatus,
                trim(command.getDetail()), operator) != 1)
            throw new ServiceException("工单事件写入失败");
    }

    /** 查询知识分类。 */
    public List<PlatformKnowledgeCategoryVO> knowledgeCategories()
    {
        List<PlatformKnowledgeCategoryVO> rows = mapper.selectKnowledgeCategories();
        return rows == null ? Collections.emptyList() : rows;
    }

    /** 查询知识文章列表。 */
    public List<PlatformKnowledgeArticleVO> knowledgeArticles(PlatformKnowledgeQuery query)
    {
        PlatformKnowledgeQuery normalized = query == null ? new PlatformKnowledgeQuery() : query;
        normalized.setTitle(trim(normalized.getTitle()));
        if (normalized.getSourceType() != null && normalized.getSourceType().intValue() != 1)
            throw new ServiceException("平台知识库列表只允许查询平台公共文章");
        normalized.setSourceType(Integer.valueOf(1));
        if (normalized.getStatus() != null && PlatformKnowledgeStatus.fromCode(normalized.getStatus()) == null)
            throw new ServiceException("知识文章状态不合法");
        List<PlatformKnowledgeArticleVO> rows = mapper.selectKnowledgeArticles(normalized);
        if (rows == null)
            return Collections.emptyList();
        for (PlatformKnowledgeArticleVO row : rows)
            decorateKnowledge(row);
        return rows;
    }

    /** 查询商户知识合规列表，仅返回文章元数据和最近一次违规处置原因。 */
    public List<PlatformKnowledgeArticleVO> knowledgeComplianceArticles(PlatformKnowledgeQuery query)
    {
        PlatformKnowledgeQuery normalized = query == null ? new PlatformKnowledgeQuery() : query;
        normalized.setTitle(trim(normalized.getTitle()));
        normalized.setMemberKeyword(trim(normalized.getMemberKeyword()));
        if (normalized.getStatus() != null && PlatformKnowledgeStatus.fromCode(normalized.getStatus()) == null)
            throw new ServiceException("知识文章状态不合法");
        List<PlatformKnowledgeArticleVO> rows = mapper.selectKnowledgeComplianceArticles(normalized);
        if (rows == null)
            return Collections.emptyList();
        for (PlatformKnowledgeArticleVO row : rows)
            decorateKnowledge(row);
        return rows;
    }

    /** 查询文章详情及历史版本。 */
    public PlatformKnowledgeDetailVO knowledgeArticle(Long articleId)
    {
        PlatformKnowledgeArticleVO article = requireKnowledgeArticle(articleId);
        PlatformKnowledgeDetailVO result = new PlatformKnowledgeDetailVO();
        result.setArticle(article);
        List<PlatformKnowledgeVersionVO> versions = mapper.selectKnowledgeVersions(articleId);
        if (versions != null)
        {
            for (PlatformKnowledgeVersionVO version : versions)
            {
                decorateKnowledgeVersion(version);
                version.setAttachments(mapper.selectKnowledgeVersionAttachments(version.getVersionId()));
            }
        }
        result.setVersions(versions == null ? Collections.emptyList() : versions);
        return result;
    }

    /** 查询商户知识合规详情，正文仅由合规查看权限的接口返回。 */
    public PlatformKnowledgeDetailVO knowledgeComplianceArticle(Long articleId)
    {
        PlatformKnowledgeArticleVO article = requireKnowledgeComplianceArticle(articleId);
        PlatformKnowledgeDetailVO result = new PlatformKnowledgeDetailVO();
        result.setArticle(article);
        List<PlatformKnowledgeVersionVO> versions = mapper.selectKnowledgeComplianceVersions(articleId);
        if (versions != null)
        {
            for (PlatformKnowledgeVersionVO version : versions)
            {
                decorateKnowledgeVersion(version);
                version.setAttachments(mapper.selectKnowledgeVersionAttachments(version.getVersionId()));
            }
        }
        result.setVersions(versions == null ? Collections.emptyList() : versions);
        return result;
    }

    /** 新增平台知识文章并生成第一版草稿。 */
    @Transactional(rollbackFor = Exception.class)
    public PlatformKnowledgeArticleVO createKnowledge(PlatformKnowledgeArticleCommand command)
    {
        validateKnowledgeCommand(command);
        Long operator = SecurityUtils.getUserId();
        if (mapper.insertKnowledgeArticle(null, command.getCategoryId(), trim(command.getTitle()), 1, 1,
                command.getPinned(), operator) != 1)
            throw new ServiceException("知识文章创建失败");
        Long articleId = mapper.selectLastInsertId();
        if (articleId == null || mapper.insertKnowledgeVersion(articleId, 1, command.getContent(), 0, null, operator) != 1)
            throw new ServiceException("知识文章版本写入失败");
        appendKnowledgeAttachments(articleId, 1, command.getAttachments(), operator);
        return requireKnowledgeArticle(articleId);
    }

    /** 修改平台知识文章，保留旧版本并把新版本置为草稿。 */
    @Transactional(rollbackFor = Exception.class)
    public PlatformKnowledgeArticleVO updateKnowledge(PlatformKnowledgeArticleCommand command)
    {
        validateKnowledgeCommand(command);
        if (command.getArticleId() == null)
            throw new ServiceException("知识文章唯一编号不能为空");
        PlatformKnowledgeArticleVO article = requireKnowledgeArticle(command.getArticleId());
        int versionNo = article.getCurrentVersionNo() == null ? 1 : article.getCurrentVersionNo() + 1;
        Long operator = SecurityUtils.getUserId();
        if (mapper.updateKnowledgeArticle(command.getArticleId(), command.getCategoryId(), trim(command.getTitle()),
                command.getPinned(), versionNo, operator) != 1
                || mapper.insertKnowledgeVersion(command.getArticleId(), versionNo, command.getContent(), 0, null, operator) != 1)
            throw new ServiceException("知识文章修改失败");
        appendKnowledgeAttachments(command.getArticleId(), versionNo, command.getAttachments(), operator);
        return requireKnowledgeArticle(command.getArticleId());
    }

    /** 发布平台知识文章当前草稿版本。 */
    @Transactional(rollbackFor = Exception.class)
    public void publishKnowledge(Long articleId)
    {
        changeKnowledgeStatus(articleId, PlatformKnowledgeStatus.PUBLISHED);
    }

    /** 下架平台知识文章，历史版本仍然保留。 */
    @Transactional(rollbackFor = Exception.class)
    public void offlineKnowledge(Long articleId)
    {
        changeKnowledgeStatus(articleId, PlatformKnowledgeStatus.OFFLINE);
    }

    /** 记录违规原因并下架商户文章，平台不修改、不删除商户正文。 */
    @Transactional(rollbackFor = Exception.class)
    public void offlineMerchantKnowledge(Long articleId, PlatformKnowledgeViolationCommand command)
    {
        PlatformKnowledgeArticleVO article = requireKnowledgeComplianceArticle(articleId);
        String reason = trim(command == null ? null : command.getReason());
        if (StringUtils.isBlank(reason) || reason.length() > 500)
            throw new ServiceException("违规原因不能为空且不能超过500个字符");
        if (article.getStatus() == null || article.getStatus().intValue() != PlatformKnowledgeStatus.PUBLISHED.getCode())
            throw new ServiceException("只有已发布商户文章可以违规下架");
        Long operator = SecurityUtils.getUserId();
        if (mapper.insertKnowledgeViolation(articleId, article.getMemberId(),
                PlatformKnowledgeViolationAction.OFFLINE.getCode(), reason, operator) != 1
                || mapper.updateMerchantKnowledgeStatus(articleId, article.getStatus(),
                        PlatformKnowledgeStatus.OFFLINE.getCode(), operator) != 1
                || mapper.updateMerchantKnowledgeVersionStatus(articleId, PlatformKnowledgeStatus.OFFLINE.getCode(), operator) < 1)
            throw new ServiceException("商户知识文章违规下架失败");
    }

    /** 删除平台知识文章并把删除前内容保留到 30 天回收站。商户原文不允许由平台删除。 */
    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledge(Long articleId)
    {
        PlatformKnowledgeArticleVO article = requireKnowledgeArticle(articleId);
        if (article.getSourceType() == null || article.getSourceType().intValue() != 1)
            throw new ServiceException("商户知识文章不能由平台删除");
        Long operator = SecurityUtils.getUserId();
        if (mapper.insertKnowledgeRecycle(articleId, operator) != 1
                || mapper.deleteKnowledgeArticle(articleId, operator) != 1)
            throw new ServiceException("知识文章删除失败，未生成可恢复快照");
    }

    /** 查询回收站快照。 */
    public List<PlatformRecycleRecordVO> recycleRecords(PlatformRecycleQuery query)
    {
        List<PlatformRecycleRecordVO> rows = mapper.selectRecycleRecords(query == null ? new PlatformRecycleQuery() : query);
        if (rows == null)
            return Collections.emptyList();
        for (PlatformRecycleRecordVO row : rows)
            row.setRestoreStatusName(label(PlatformRecycleStatus.fromCode(row.getRestoreStatus()), "未知状态"));
        return rows;
    }

    /** 恢复已经接入对象处理器的回收站记录。 */
    @Transactional(rollbackFor = Exception.class)
    public void restoreRecycle(Long recycleId)
    {
        PlatformRecycleRecordVO record = mapper.selectRecycleRecord(recycleId);
        if (record == null)
            throw new ServiceException("回收记录不存在");
        if (record.getRestoreStatus() != null && record.getRestoreStatus() != PlatformRecycleStatus.PENDING.getCode())
            throw new ServiceException("当前回收记录已处理");
        Long operator = SecurityUtils.getUserId();
        int affected;
        if ("knowledge_article".equals(record.getObjectType()))
            affected = mapper.restoreKnowledgeArticle(record.getObjectId(), operator);
        else if ("merchant_notice".equals(record.getObjectType()))
            affected = mapper.restoreMerchantNotice(record.getObjectId(), operator);
        else
            throw new ServiceException("当前对象类型尚未接入恢复处理");
        if ("merchant_notice".equals(record.getObjectType()))
        {
            if (mapper.restoreMerchantNoticeMembers(record.getObjectId(), operator) < 0
                    || mapper.restoreMerchantNoticeReads(record.getObjectId()) < 0)
            {
                throw new ServiceException("公告关联数据恢复失败");
            }
        }
        if (affected != 1 || mapper.markRecycleRestored(recycleId, operator, "平台治理恢复成功") != 1)
            throw new ServiceException("回收记录恢复失败，可能已过期或对象已不存在");
    }

    /** 将超过 30 天保留期限且仍待恢复的回收记录标记为已清理。 */
    @Transactional(rollbackFor = Exception.class)
    public int cleanupExpiredRecycleRecords()
    {
        return mapper.markExpiredRecycleRecords(0L, "超过 30 天保留期限，系统自动清理");
    }

    private void changeKnowledgeStatus(Long articleId, PlatformKnowledgeStatus target)
    {
        PlatformKnowledgeArticleVO article = requireKnowledgeArticle(articleId);
        if (target == PlatformKnowledgeStatus.PUBLISHED && article.getStatus() == target.getCode())
            throw new ServiceException("知识文章已经发布");
        if (target == PlatformKnowledgeStatus.OFFLINE && article.getStatus() != PlatformKnowledgeStatus.PUBLISHED.getCode())
            throw new ServiceException("只有已发布文章可以下架");
        if (mapper.updateKnowledgeStatus(articleId, article.getStatus(), target.getCode(), SecurityUtils.getUserId()) != 1
                || mapper.updateKnowledgeVersionStatus(articleId, target.getCode(), SecurityUtils.getUserId()) < 1)
            throw new ServiceException("知识文章状态更新失败");
    }

    private PlatformTicketVO requireTicket(Long ticketId)
    {
        if (ticketId == null || ticketId <= 0)
            throw new ServiceException("工单唯一编号不合法");
        PlatformTicketVO row = mapper.selectTicket(ticketId);
        if (row == null)
            throw new ServiceException("工单不存在或已删除");
        decorateTicket(row);
        return row;
    }

    private PlatformKnowledgeArticleVO requireKnowledgeArticle(Long articleId)
    {
        if (articleId == null || articleId <= 0)
            throw new ServiceException("知识文章唯一编号不合法");
        PlatformKnowledgeArticleVO row = mapper.selectKnowledgeArticle(articleId);
        if (row == null)
            throw new ServiceException("知识文章不存在或已删除");
        decorateKnowledge(row);
        return row;
    }

    private PlatformKnowledgeArticleVO requireKnowledgeComplianceArticle(Long articleId)
    {
        if (articleId == null || articleId <= 0)
            throw new ServiceException("知识文章唯一编号不合法");
        PlatformKnowledgeArticleVO row = mapper.selectKnowledgeComplianceArticle(articleId);
        if (row == null)
            throw new ServiceException("商户知识文章不存在或已删除");
        decorateKnowledge(row);
        return row;
    }

    private void validateTicketFilters(PlatformTicketQuery query)
    {
        if (query.getTicketType() != null && PlatformTicketType.fromCode(query.getTicketType()) == null)
            throw new ServiceException("工单类型不合法");
        if (query.getTicketStatus() != null && PlatformTicketStatus.fromCode(query.getTicketStatus()) == null)
            throw new ServiceException("工单状态不合法");
        if (query.getPriority() != null && PlatformTicketPriority.fromCode(query.getPriority()) == null)
            throw new ServiceException("工单优先级不合法");
    }

    private void validateKnowledgeCommand(PlatformKnowledgeArticleCommand command)
    {
        if (command == null || command.getCategoryId() == null || StringUtils.isBlank(command.getTitle())
                || StringUtils.isBlank(command.getContent()))
            throw new ServiceException("知识分类、标题和正文不能为空");
        if (command.getPinned() != null && command.getPinned() != 0 && command.getPinned() != 1)
            throw new ServiceException("置顶标识不合法");
        if (command.getAttachments() != null && command.getAttachments().size() > MAX_KNOWLEDGE_ATTACHMENT_COUNT)
            throw new ServiceException("知识文章最多添加10个附件");
        if (command.getAttachments() != null)
        {
            Map<String, Boolean> seen = new LinkedHashMap<>();
            for (PlatformKnowledgeAttachmentCommand attachment : command.getAttachments())
            {
                if (attachment == null)
                    throw new ServiceException("知识附件不能为空");
                String url = trim(attachment.getFileUrl());
                String name = trim(attachment.getAttachmentName());
                String lowerUrl = url == null ? "" : url.toLowerCase(Locale.ROOT);
                if (StringUtils.isBlank(url) || url.length() > MAX_KNOWLEDGE_ATTACHMENT_URL_LENGTH
                        || lowerUrl.startsWith("javascript:") || lowerUrl.startsWith("data:"))
                    throw new ServiceException("知识附件地址不合法");
                if (StringUtils.isBlank(name) || name.length() > MAX_KNOWLEDGE_ATTACHMENT_NAME_LENGTH)
                    throw new ServiceException("知识附件名称不能为空且不能超过255个字符");
                if (attachment.getFileSize() != null
                        && (attachment.getFileSize() < 0 || attachment.getFileSize() > 20L * 1024 * 1024))
                    throw new ServiceException("知识附件大小不能超过20MB");
                if (seen.put(url, Boolean.TRUE) != null)
                    throw new ServiceException("知识附件不能重复");
            }
        }
    }

    private void appendKnowledgeAttachments(Long articleId, Integer versionNo,
            List<PlatformKnowledgeAttachmentCommand> attachments, Long operator)
    {
        if (attachments == null || attachments.isEmpty())
            return;
        Long versionId = mapper.selectKnowledgeVersionId(articleId, versionNo);
        if (versionId == null || mapper.insertKnowledgeVersionAttachments(versionId, attachments, operator) != attachments.size())
            throw new ServiceException("知识文章附件保存失败");
    }

    private void decorateTicket(PlatformTicketVO row)
    {
        row.setTicketTypeName(label(PlatformTicketType.fromCode(row.getTicketType()), "未知类型"));
        row.setTicketStatusName(label(PlatformTicketStatus.fromCode(row.getTicketStatus()), "未知状态"));
        row.setPriorityName(label(PlatformTicketPriority.fromCode(row.getPriority()), "未知优先级"));
        row.setSourceTypeName(row.getSourceType() == null ? "未知来源" : row.getSourceType() == 1 ? "平台" : row.getSourceType() == 2 ? "商户" : "系统");
    }

    private String buildAfterSalesTicketContent(PlatformAfterSalesTicketCommand command, String caseNo,
            String orderNo)
    {
        StringBuilder content = new StringBuilder(256);
        content.append("售后单：").append(caseNo).append('\n');
        content.append("订单：").append(orderNo).append('\n');
        content.append("售后类型：").append(defaultText(command.getCaseTypeName(), "其他")).append('\n');
        content.append("问题描述：").append(defaultText(command.getProblemDesc(), "未填写")).append('\n');
        if (command.getCompensationAmount() != null)
        {
            content.append("赔付金额：¥").append(command.getCompensationAmount()).append('\n');
        }
        if (!StringUtils.isBlank(command.getRemark()))
        {
            content.append("备注：").append(command.getRemark().trim());
        }
        return content.length() > 4000 ? content.substring(0, 4000) : content.toString();
    }

    private PlatformAfterSalesTicketView toAfterSalesTicketView(PlatformTicketVO ticket)
    {
        PlatformAfterSalesTicketView result = new PlatformAfterSalesTicketView();
        result.setTicketNo(ticket.getTicketNo());
        result.setTicketStatus(ticket.getTicketStatus());
        result.setTicketStatusName(ticket.getTicketStatusName());
        return result;
    }

    private MerchantCreditAppealView toCreditAppealView(PlatformTicketVO ticket, String recordNo)
    {
        MerchantCreditAppealView result = new MerchantCreditAppealView();
        result.setTicketNo(ticket.getTicketNo());
        result.setRecordNo(recordNo);
        result.setTicketStatus(ticket.getTicketStatus());
        result.setTicketStatusName(ticket.getTicketStatusName());
        result.setCreateTime(ticket.getCreateTime());
        return result;
    }

    private int intValue(Object value)
    {
        if (value instanceof Number)
            return ((Number) value).intValue();
        try
        {
            return value == null ? 0 : Integer.parseInt(String.valueOf(value));
        }
        catch (NumberFormatException ex)
        {
            return 0;
        }
    }

    private Long longValue(Object value)
    {
        if (value instanceof Number)
            return Long.valueOf(((Number) value).longValue());
        try
        {
            return value == null ? null : Long.valueOf(Long.parseLong(String.valueOf(value)));
        }
        catch (NumberFormatException ex)
        {
            return null;
        }
    }

    private String stringValue(Object value)
    {
        return value == null || StringUtils.isBlank(String.valueOf(value)) ? null : String.valueOf(value);
    }

    private String defaultText(String value, String fallback)
    {
        return StringUtils.isBlank(value) ? fallback : value.trim();
    }

    private List<PlatformTicketEventVO> decorateEvents(List<PlatformTicketEventVO> rows)
    {
        if (rows == null)
            return Collections.emptyList();
        for (PlatformTicketEventVO row : rows)
        {
            row.setEventTypeName(label(PlatformTicketEventType.fromCode(row.getEventType()), "未知事件"));
            row.setFromStatusName(label(PlatformTicketStatus.fromCode(row.getFromStatus()), "-") );
            row.setToStatusName(label(PlatformTicketStatus.fromCode(row.getToStatus()), "-") );
        }
        return rows;
    }

    private List<PlatformTicketAssignmentVO> decorateAssignments(List<PlatformTicketAssignmentVO> rows)
    {
        if (rows == null)
            return Collections.emptyList();
        for (PlatformTicketAssignmentVO row : rows)
            row.setAssignmentStatusName(row.getAssignmentStatus() == null ? "未知状态"
                    : row.getAssignmentStatus() == 1 ? "处理中" : row.getAssignmentStatus() == 2 ? "已完成" : "已转派");
        return rows;
    }

    private void decorateKnowledge(PlatformKnowledgeArticleVO row)
    {
        row.setSourceTypeName(row.getSourceType() != null && row.getSourceType() == 1 ? "平台" : "商户");
        row.setStatusName(label(PlatformKnowledgeStatus.fromCode(row.getStatus()), "未知状态"));
    }

    private void decorateKnowledgeVersion(PlatformKnowledgeVersionVO row)
    {
        row.setVersionStatusName(row.getVersionStatus() != null && row.getVersionStatus() == 1 ? "已发布"
                : row.getVersionStatus() != null && row.getVersionStatus() == 2 ? "违规下架" : "草稿");
    }

    private <T> String label(T value, String fallback)
    {
        if (value instanceof PlatformTicketType)
            return ((PlatformTicketType) value).getLabel();
        if (value instanceof PlatformTicketStatus)
            return ((PlatformTicketStatus) value).getLabel();
        if (value instanceof PlatformTicketPriority)
            return ((PlatformTicketPriority) value).getLabel();
        if (value instanceof PlatformTicketEventType)
            return ((PlatformTicketEventType) value).getLabel();
        if (value instanceof PlatformRecycleStatus)
            return ((PlatformRecycleStatus) value).getLabel();
        return fallback;
    }

    private String trim(String value)
    {
        return value == null ? null : value.trim().isEmpty() ? null : value.trim();
    }
}
