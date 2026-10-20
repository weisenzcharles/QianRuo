package com.ruoyi.merchant.application;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.log.service.MerchantBusinessAuditRecorder;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.merchant.mapper.MerchantMarketPostMapper;
import com.ruoyi.merchant.model.MerchantMarketBoardType;
import com.ruoyi.merchant.model.MerchantMarketConditionLevel;
import com.ruoyi.merchant.model.MerchantMarketMessageCommand;
import com.ruoyi.merchant.model.MerchantMarketMessageRecord;
import com.ruoyi.merchant.model.MerchantMarketMessageVO;
import com.ruoyi.merchant.model.MerchantMarketPostRecord;
import com.ruoyi.merchant.model.MerchantMarketPostVO;
import com.ruoyi.merchant.model.MerchantMarketPostStatus;
import com.ruoyi.merchant.model.MerchantMarketPriceRange;
import com.ruoyi.merchant.model.MerchantMarketReadStatus;
import com.ruoyi.merchant.model.MerchantMarketReportCommand;
import com.ruoyi.merchant.model.MerchantMarketReportRecord;
import com.ruoyi.merchant.model.MerchantMarketReportType;
import com.ruoyi.merchant.model.MerchantMarketReportVO;

/**
 * 商户周转市场互动服务，负责公开浏览、站内私信和举报提交。
 *
 * <p>所有写操作都重新根据帖子编号查询发布者，避免前端提交商户或帖子主键造成越权。</p>
 */
@Service
public class MerchantMarketInteractionService
{
    private static final int MAX_MESSAGE_LENGTH = 1000;
    private static final int MAX_EVIDENCE_LENGTH = 1000;

    private final MerchantMarketPostMapper mapper;
    private final MerchantBusinessAuditRecorder auditRecorder;
    private final ObjectMapper objectMapper;

    public MerchantMarketInteractionService(MerchantMarketPostMapper mapper,
            MerchantBusinessAuditRecorder auditRecorder, ObjectMapper objectMapper)
    {
        this.mapper = mapper;
        this.auditRecorder = auditRecorder;
        this.objectMapper = objectMapper;
    }

    /** 查询其他商户已发布的市场帖子。 */
    public List<MerchantMarketPostVO> publicPosts(MerchantMarketBoardType boardType, String keyword, String city)
    {
        MerchantContextUtils.requireMerchantApproved();
        List<MerchantMarketPostRecord> records = mapper.selectPublicList(memberId(), trim(keyword),
                boardType == null ? null : boardType.getCode(), trim(city));
        if (records == null || records.isEmpty())
            return Collections.emptyList();
        return records.stream().map(this::toPostView).collect(Collectors.toList());
    }

    /** 查询一条公开帖子，仅用于发送私信或举报前的服务端校验。 */
    public MerchantMarketPostVO publicPost(String postNo)
    {
        MerchantContextUtils.requireMerchantApproved();
        MerchantMarketPostRecord record = mapper.selectPublicByPostNo(normalizePostNo(postNo));
        return record == null ? null : toPostView(record);
    }

    /** 查询当前商户参与的站内私信。 */
    public List<MerchantMarketMessageVO> messages(MerchantMarketReadStatus readStatus, String keyword)
    {
        MerchantContextUtils.requireMerchantApproved();
        List<MerchantMarketMessageRecord> records = mapper.selectMessages(memberId(),
                readStatus == null ? null : readStatus.getCode(), trim(keyword));
        if (records == null || records.isEmpty())
            return Collections.emptyList();
        return records.stream().map(this::toMessageView).collect(Collectors.toList());
    }

    /** 向公开市场帖子的发布商户发送站内私信。 */
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(MerchantMarketMessageCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null)
            throw new ServiceException("私信参数不能为空");
        String postNo = normalizePostNo(command.getPostNo());
        String content = trim(command.getContent());
        if (blank(content))
            throw new ServiceException("私信内容不能为空");
        if (content.length() > MAX_MESSAGE_LENGTH)
            throw new ServiceException("私信内容不能超过1000个字符");
        MerchantMarketPostRecord post = mapper.selectPublicByPostNo(postNo);
        if (post == null)
            throw new ServiceException("市场帖子不存在或已下架");
        if (memberId().equals(post.getMemberId()))
            throw new ServiceException("不能给自己的市场帖子发送私信");
        if (mapper.insertMessage(MerchantContextUtils.getTenantId(), post.getPostId(), memberId(),
                post.getMemberId(), content) != 1)
        {
            throw new ServiceException("私信发送失败");
        }
        auditRecorder.record("周转市场", "MESSAGE", "MARKET_POST", postNo, displayName(post),
                "发送市场私信", null, null);
    }

    /** 将收到的私信标记为已读，发送方不能伪造已读操作。 */
    @Transactional(rollbackFor = Exception.class)
    public void markMessageRead(Long messageId)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (messageId == null || messageId.longValue() <= 0)
            throw new ServiceException("消息编号不合法");
        if (mapper.markMessageRead(messageId, memberId()) != 1)
            throw new ServiceException("消息不存在或无权操作");
    }

    /** 查询当前商户提交的市场举报。 */
    public List<MerchantMarketReportVO> reports()
    {
        MerchantContextUtils.requireMerchantApproved();
        List<MerchantMarketReportRecord> records = mapper.selectReports(memberId());
        if (records == null || records.isEmpty())
            return Collections.emptyList();
        return records.stream().map(this::toReportView).collect(Collectors.toList());
    }

    /** 提交市场帖子举报，并阻止同一商户重复提交相同待处理类型。 */
    @Transactional(rollbackFor = Exception.class)
    public void submitReport(MerchantMarketReportCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null || command.getReportType() == null)
            throw new ServiceException("举报类型不能为空");
        String postNo = normalizePostNo(command.getPostNo());
        String evidence = trim(command.getEvidence());
        if (evidence != null && evidence.length() > MAX_EVIDENCE_LENGTH)
            throw new ServiceException("举报说明不能超过1000个字符");
        MerchantMarketPostRecord post = mapper.selectPublicByPostNo(postNo);
        if (post == null)
            throw new ServiceException("市场帖子不存在或已下架");
        if (memberId().equals(post.getMemberId()))
            throw new ServiceException("不能举报自己的市场帖子");
        if (mapper.selectPendingReport(post.getPostId(), memberId(), command.getReportType().getCode()) != null)
            throw new ServiceException("相同类型的举报正在处理中，请勿重复提交");
        String evidenceJson = toEvidenceJson(evidence);
        if (mapper.insertReport(MerchantContextUtils.getTenantId(), post.getPostId(), memberId(),
                post.getMemberId(), command.getReportType().getCode(), evidenceJson) != 1)
        {
            throw new ServiceException("举报提交失败");
        }
        auditRecorder.record("周转市场", "REPORT", "MARKET_POST", postNo, displayName(post),
                "提交市场举报", null, null);
    }

    private MerchantMarketPostVO toPostView(MerchantMarketPostRecord record)
    {
        MerchantMarketPostVO view = new MerchantMarketPostVO();
        view.setPostNo(record.getPostNo());
        view.setPublisherName(record.getPublisherName());
        view.setBoardType(record.getBoardType());
        MerchantMarketBoardType boardType = MerchantMarketBoardType.fromCode(record.getBoardType());
        view.setBoardTypeName(boardType == null ? "未知板块" : boardType.getLabel());
        view.setCategory(record.getCategory());
        view.setBrand(record.getBrand());
        view.setModelName(record.getModelName());
        view.setMemorySpec(record.getMemorySpec());
        view.setConditionLevel(conditionLabel(record.getConditionLevel()));
        view.setQuantity(record.getQuantity());
        view.setPriceRangeCode(boardType == MerchantMarketBoardType.RENT
                ? null : MerchantMarketPriceRange.labelOf(record.getPriceRangeCode()));
        view.setCity(record.getCity());
        view.setServiceTags(record.getServiceTags());
        view.setRecent30dTurnover(record.getRecent30dTurnover());
        view.setIdleQty(record.getIdleQty());
        view.setRentedQty(record.getRentedQty());
        view.setLongIdleQty(record.getLongIdleQty());
        view.setPostStatus(record.getPostStatus());
        MerchantMarketPostStatus status = MerchantMarketPostStatus.fromCode(record.getPostStatus());
        view.setPostStatusName(status == null ? "未知状态" : status.getLabel());
        view.setDownRankFlag(record.getDownRankFlag());
        view.setCreateTime(record.getCreateTime());
        view.setUpdateTime(record.getUpdateTime());
        view.setRemark(record.getRemark());
        return view;
    }

    private String conditionLabel(String value)
    {
        MerchantMarketConditionLevel condition = MerchantMarketConditionLevel.fromValue(value);
        return condition == null ? value : condition.getLabel();
    }

    private MerchantMarketMessageVO toMessageView(MerchantMarketMessageRecord record)
    {
        MerchantMarketMessageVO view = new MerchantMarketMessageVO();
        view.setMessageId(record.getMessageId());
        view.setPostNo(record.getPostNo());
        view.setFromMerchantName(record.getFromMerchantName());
        view.setToMerchantName(record.getToMerchantName());
        view.setMessageContent(record.getMessageContent());
        view.setReadFlag(record.getReadFlag());
        MerchantMarketReadStatus readStatus = MerchantMarketReadStatus.fromCode(record.getReadFlag());
        view.setReadFlagName(readStatus == null ? "未知状态" : readStatus.getLabel());
        view.setRiskFlag(record.getRiskFlag());
        view.setCreateTime(record.getCreateTime());
        return view;
    }

    private MerchantMarketReportVO toReportView(MerchantMarketReportRecord record)
    {
        MerchantMarketReportVO view = new MerchantMarketReportVO();
        view.setPostNo(record.getPostNo());
        view.setReportedMerchantName(record.getReportedMerchantName());
        view.setReportType(record.getReportType());
        MerchantMarketReportType reportType = MerchantMarketReportType.fromCode(record.getReportType());
        view.setReportTypeName(reportType == null ? "未知类型" : reportType.getLabel());
        view.setReportStatus(record.getReportStatus());
        view.setReportStatusName(reportStatusName(record.getReportStatus()));
        view.setEvidence(record.getEvidence());
        view.setReviewResult(record.getReviewResult());
        view.setCreateTime(record.getCreateTime());
        view.setReviewTime(record.getReviewTime());
        return view;
    }

    private String toEvidenceJson(String evidence)
    {
        if (blank(evidence))
            return null;
        try
        {
            return objectMapper.writeValueAsString(Map.of("content", evidence));
        }
        catch (JsonProcessingException exception)
        {
            throw new ServiceException("举报说明保存失败");
        }
    }

    private String reportStatusName(Integer status)
    {
        if (status == null)
            return "未知状态";
        if (status.intValue() == 0)
            return "待处理";
        if (status.intValue() == 1)
            return "已处理";
        if (status.intValue() == 2)
            return "已驳回";
        return "未知状态";
    }

    private String displayName(MerchantMarketPostRecord post)
    {
        return (post.getBrand() == null ? "" : post.getBrand() + " ")
                + (post.getModelName() == null ? post.getPostNo() : post.getModelName());
    }

    private String normalizePostNo(String postNo)
    {
        String normalized = trim(postNo);
        if (blank(normalized) || normalized.length() > 64)
            throw new ServiceException("帖子编号不合法");
        return normalized;
    }

    private Long memberId() { return MerchantContextUtils.getMemberId(); }
    private String trim(String value) { return value == null ? null : value.trim(); }
    private boolean blank(String value) { return value == null || value.trim().isEmpty(); }
}
