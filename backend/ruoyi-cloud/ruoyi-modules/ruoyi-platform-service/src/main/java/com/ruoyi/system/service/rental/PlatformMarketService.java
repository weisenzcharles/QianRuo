package com.ruoyi.system.service.rental;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.enums.PlatformMarketPostStatus;
import com.ruoyi.system.enums.PlatformMarketReportStatus;
import com.ruoyi.system.mapper.rental.PlatformMarketMapper;
import com.ruoyi.system.model.rental.PlatformMarketPostVO;
import com.ruoyi.system.model.rental.PlatformMarketQuery;
import com.ruoyi.system.model.rental.PlatformMarketReportReviewCommand;
import com.ruoyi.system.model.rental.PlatformMarketReportVO;

/** 平台周转市场治理服务，负责帖子审核、举报处理和风险降权。 */
@Service
public class PlatformMarketService
{
    private final PlatformMarketMapper mapper;

    public PlatformMarketService(PlatformMarketMapper mapper)
    {
        this.mapper = mapper;
    }

    /** 查询市场帖子并补齐板块、状态中文名称。 */
    public List<PlatformMarketPostVO> posts(PlatformMarketQuery query)
    {
        PlatformMarketQuery normalized = normalize(query);
        validate(normalized);
        List<PlatformMarketPostVO> rows = mapper.selectPosts(normalized);
        if (rows == null)
            return Collections.emptyList();
        for (PlatformMarketPostVO row : rows)
            decoratePost(row);
        return rows;
    }

    /** 查询市场举报并补齐处理状态中文名称。 */
    public List<PlatformMarketReportVO> reports(PlatformMarketQuery query)
    {
        PlatformMarketQuery normalized = normalize(query);
        validate(normalized);
        List<PlatformMarketReportVO> rows = mapper.selectReports(normalized);
        if (rows == null)
            return Collections.emptyList();
        for (PlatformMarketReportVO row : rows)
            decorateReport(row);
        return rows;
    }

    /** 处理待处理举报，可在认定违规时联动下架对应帖子。 */
    @Transactional(rollbackFor = Exception.class)
    public void reviewReport(Long reportId, PlatformMarketReportReviewCommand command)
    {
        if (reportId == null || reportId.longValue() <= 0 || command == null)
            throw new ServiceException("举报处理参数不合法");
        PlatformMarketReportStatus target = PlatformMarketReportStatus.fromCode(command.getReportStatus());
        if (target == null || target == PlatformMarketReportStatus.PENDING)
            throw new ServiceException("举报处理状态不合法");
        String result = trim(command.getReviewResult());
        if (StringUtils.isBlank(result))
            throw new ServiceException("处理结果不能为空");
        if (result.length() > 1000)
            throw new ServiceException("处理结果不能超过1000个字符");
        PlatformMarketReportVO current = mapper.selectReport(reportId);
        if (current == null)
            throw new ServiceException("举报记录不存在或已删除");
        if (!Integer.valueOf(PlatformMarketReportStatus.PENDING.getCode()).equals(current.getReportStatus()))
            throw new ServiceException("当前举报已经处理");
        Long operator = SecurityUtils.getUserId();
        if (mapper.reviewReport(reportId, target.getCode(), result, operator) != 1)
            throw new ServiceException("举报处理失败，记录可能已被其他人员处理");
        if (Boolean.TRUE.equals(command.getOfflinePost()))
        {
            if (current.getPostId() == null || mapper.updatePostStatus(current.getPostId(),
                    PlatformMarketPostStatus.OFF_SHELF.getCode(), operator) != 1)
                throw new ServiceException("举报已处理，但帖子下架失败");
        }
    }

    /** 平台强制下架市场帖子。 */
    @Transactional(rollbackFor = Exception.class)
    public void offlinePost(Long postId)
    {
        PlatformMarketPostVO current = requirePost(postId);
        if (PlatformMarketPostStatus.OFF_SHELF.getCode() == current.getPostStatus())
            throw new ServiceException("帖子已经下架");
        if (PlatformMarketPostStatus.CLOSED.getCode() == current.getPostStatus())
            throw new ServiceException("已关闭帖子不能重复下架");
        if (mapper.updatePostStatus(postId, PlatformMarketPostStatus.OFF_SHELF.getCode(), SecurityUtils.getUserId()) != 1)
            throw new ServiceException("帖子下架失败");
    }

    /** 设置或取消市场帖子降权。 */
    @Transactional(rollbackFor = Exception.class)
    public void updateRank(Long postId, Integer downRankFlag)
    {
        requirePost(postId);
        if (downRankFlag == null || (downRankFlag.intValue() != 0 && downRankFlag.intValue() != 1))
            throw new ServiceException("降权标识不合法");
        if (mapper.updatePostRank(postId, downRankFlag, SecurityUtils.getUserId()) != 1)
            throw new ServiceException("帖子降权状态更新失败");
    }

    private PlatformMarketPostVO requirePost(Long postId)
    {
        if (postId == null || postId.longValue() <= 0)
            throw new ServiceException("帖子唯一编号不合法");
        PlatformMarketPostVO row = mapper.selectPost(postId);
        if (row == null)
            throw new ServiceException("市场帖子不存在或已删除");
        decoratePost(row);
        return row;
    }

    private PlatformMarketQuery normalize(PlatformMarketQuery query)
    {
        PlatformMarketQuery result = query == null ? new PlatformMarketQuery() : query;
        result.setKeyword(trim(result.getKeyword()));
        result.setCity(trim(result.getCity()));
        return result;
    }

    private void validate(PlatformMarketQuery query)
    {
        if (query.getBoardType() != null && (query.getBoardType() < 1 || query.getBoardType() > 3))
            throw new ServiceException("市场板块不合法");
        if (query.getPostStatus() != null && PlatformMarketPostStatus.fromCode(query.getPostStatus()) == null)
            throw new ServiceException("帖子状态不合法");
        if (query.getReportStatus() != null && PlatformMarketReportStatus.fromCode(query.getReportStatus()) == null)
            throw new ServiceException("举报状态不合法");
    }

    private void decoratePost(PlatformMarketPostVO row)
    {
        if (row == null)
            return;
        row.setBoardTypeName(boardName(row.getBoardType()));
        PlatformMarketPostStatus status = PlatformMarketPostStatus.fromCode(row.getPostStatus());
        row.setPostStatusName(status == null ? "未知状态" : status.getLabel());
    }

    private void decorateReport(PlatformMarketReportVO row)
    {
        if (row == null)
            return;
        PlatformMarketReportStatus status = PlatformMarketReportStatus.fromCode(row.getReportStatus());
        row.setReportStatusName(status == null ? "未知状态" : status.getLabel());
    }

    private String boardName(Integer code)
    {
        if (code == null)
            return "未知板块";
        if (code.intValue() == 1)
            return "出售";
        if (code.intValue() == 2)
            return "出租";
        if (code.intValue() == 3)
            return "求购";
        return "未知板块";
    }

    private String trim(String value) { return value == null ? null : value.trim(); }
}
