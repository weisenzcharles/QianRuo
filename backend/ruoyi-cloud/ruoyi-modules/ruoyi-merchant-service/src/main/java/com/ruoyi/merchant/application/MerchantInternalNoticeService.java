package com.ruoyi.merchant.application;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.log.service.MerchantBusinessAuditRecorder;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.merchant.mapper.MerchantInternalNoticeMapper;
import com.ruoyi.merchant.model.MerchantInternalNoticeCommand;
import com.ruoyi.merchant.model.MerchantInternalNoticeQuery;
import com.ruoyi.merchant.model.MerchantInternalNoticeReadStatVO;
import com.ruoyi.merchant.model.MerchantInternalNoticeReadStatus;
import com.ruoyi.merchant.model.MerchantInternalNoticeRoleOptionVO;
import com.ruoyi.merchant.model.MerchantInternalNoticeScope;
import com.ruoyi.merchant.model.MerchantInternalNoticeStatus;
import com.ruoyi.merchant.model.MerchantInternalNoticeTopVO;
import com.ruoyi.merchant.model.MerchantInternalNoticeType;
import com.ruoyi.merchant.model.MerchantInternalNoticeVO;

/**
 * 商户内部通知应用服务。
 *
 * <p>通知严格按当前商户隔离，指定员工组时复用商户角色关系；平台公告仍由平台服务独立管理。</p>
 */
@Service
public class MerchantInternalNoticeService
{
    private static final int MAX_TITLE_LENGTH = 160;
    private static final int MAX_CONTENT_LENGTH = 100000;
    private static final int TOP_LIMIT = 20;

    private final MerchantInternalNoticeMapper mapper;
    private final MerchantBusinessAuditRecorder auditRecorder;

    public MerchantInternalNoticeService(MerchantInternalNoticeMapper mapper,
            MerchantBusinessAuditRecorder auditRecorder)
    {
        this.mapper = mapper;
        this.auditRecorder = auditRecorder;
    }

    /** 查询当前商户的通知管理列表。 */
    public List<MerchantInternalNoticeVO> list(MerchantInternalNoticeQuery query)
    {
        requireApproved();
        MerchantInternalNoticeQuery safeQuery = query == null ? new MerchantInternalNoticeQuery() : query;
        safeQuery.setNoticeTitle(trim(safeQuery.getNoticeTitle()));
        validateQuery(safeQuery);
        return emptyIfNull(mapper.selectManagementList(tenantId(), memberId(), safeQuery));
    }

    /** 查询可作为员工组投放范围的角色选项。 */
    public List<MerchantInternalNoticeRoleOptionVO> roleOptions()
    {
        requireApproved();
        return emptyIfNull(mapper.selectRoleOptions(tenantId(), memberId()));
    }

    /** 查询当前商户通知管理详情。 */
    public MerchantInternalNoticeVO managementDetail(Long noticeId)
    {
        requireApproved();
        MerchantInternalNoticeVO notice = requireManagementNotice(noticeId);
        notice.setRoleIds(toRoleArray(mapper.selectTargetRoleIds(tenantId(), memberId(), noticeId)));
        return notice;
    }

    /** 查询当前商户通知对实际可见员工的阅读统计。 */
    public List<MerchantInternalNoticeReadStatVO> readStats(Long noticeId)
    {
        requireApproved();
        requireManagementNotice(noticeId);
        List<MerchantInternalNoticeReadStatVO> stats = mapper.selectReadStats(tenantId(), memberId(), noticeId);
        if (stats == null)
        {
            return Collections.emptyList();
        }
        for (MerchantInternalNoticeReadStatVO stat : stats)
        {
            if (stat.getReadStatusName() == null)
            {
                stat.setReadStatusName(MerchantInternalNoticeReadStatus.labelOf(stat.getReadStatus()));
            }
        }
        return stats;
    }

    /** 查询当前登录用户顶部可见的内部通知。 */
    public MerchantInternalNoticeTopVO top()
    {
        MerchantInternalNoticeTopVO result = new MerchantInternalNoticeTopVO();
        List<MerchantInternalNoticeVO> notices = mapper.selectVisibleTop(tenantId(), memberId(), userId(), TOP_LIMIT);
        result.setNotices(emptyIfNull(notices));
        result.setUnreadCount(mapper.selectUnreadCount(tenantId(), memberId(), userId()));
        return result;
    }

    /** 查询当前登录用户可见的通知详情。 */
    public MerchantInternalNoticeVO readableDetail(Long noticeId)
    {
        MerchantInternalNoticeVO notice = mapper.selectVisibleById(tenantId(), memberId(), userId(), noticeId);
        if (notice == null)
        {
            throw new ServiceException("内部通知不存在、已下架或当前员工组不可见");
        }
        return notice;
    }

    /** 新增商户内部通知，默认保存为草稿。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantInternalNoticeVO create(MerchantInternalNoticeCommand command)
    {
        requireApproved();
        MerchantInternalNoticeCommand normalized = normalize(command, false);
        validate(normalized);
        if (mapper.insertNotice(tenantId(), memberId(), normalized, userId()) != 1)
        {
            throw new ServiceException("内部通知保存失败");
        }
        Long noticeId = normalized.getInternalNoticeId();
        if (noticeId == null)
        {
            noticeId = mapper.selectLastInsertId();
        }
        syncTargetRoles(noticeId, normalized.getTargetScope(), normalized.getRoleIds());
        MerchantInternalNoticeVO result = managementDetail(noticeId);
        audit("内部通知", "CREATE", noticeId, result.getNoticeTitle(), "创建商户内部通知", null, result);
        return result;
    }

    /** 修改草稿或已下架通知，已发布通知必须先下架，避免已读用户看到内容被静默替换。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantInternalNoticeVO update(Long noticeId, MerchantInternalNoticeCommand command)
    {
        requireApproved();
        MerchantInternalNoticeVO before = requireManagementNotice(noticeId);
        if (MerchantInternalNoticeStatus.PUBLISHED.getCode() == before.getStatus())
        {
            throw new ServiceException("已发布通知不能直接修改，请先下架");
        }
        MerchantInternalNoticeCommand normalized = normalize(command, true);
        normalized.setInternalNoticeId(noticeId);
        validate(normalized);
        if (mapper.updateNotice(tenantId(), memberId(), normalized, userId()) != 1)
        {
            throw new ServiceException("内部通知不存在或已删除");
        }
        syncTargetRoles(noticeId, normalized.getTargetScope(), normalized.getRoleIds());
        MerchantInternalNoticeVO result = managementDetail(noticeId);
        audit("内部通知", "UPDATE", noticeId, result.getNoticeTitle(), "修改商户内部通知", before, result);
        return result;
    }

    /** 发布通知，发布时写入生效时间并校验员工组。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantInternalNoticeVO publish(Long noticeId)
    {
        requireApproved();
        MerchantInternalNoticeVO before = requireManagementNotice(noticeId);
        MerchantInternalNoticeCommand command = commandFrom(before);
        validate(command);
        Date effectiveTime = before.getEffectiveTime() == null ? new Date() : before.getEffectiveTime();
        if (before.getOfflineTime() != null && !before.getOfflineTime().after(effectiveTime))
        {
            throw new ServiceException("下架时间必须晚于生效时间");
        }
        if (mapper.updateStatus(tenantId(), memberId(), noticeId, MerchantInternalNoticeStatus.PUBLISHED.getCode(),
                effectiveTime, before.getOfflineTime(), userId()) != 1)
        {
            throw new ServiceException("内部通知发布失败，请刷新后重试");
        }
        MerchantInternalNoticeVO result = managementDetail(noticeId);
        audit("内部通知", "PUBLISH", noticeId, result.getNoticeTitle(), "发布商户内部通知", before, result);
        return result;
    }

    /** 下架通知，保留下架时间和历史已读记录。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantInternalNoticeVO offline(Long noticeId)
    {
        requireApproved();
        MerchantInternalNoticeVO before = requireManagementNotice(noticeId);
        if (MerchantInternalNoticeStatus.PUBLISHED.getCode() != before.getStatus())
        {
            throw new ServiceException("只有已发布通知可以下架");
        }
        Date offlineTime = new Date();
        if (mapper.updateStatus(tenantId(), memberId(), noticeId, MerchantInternalNoticeStatus.OFFLINE.getCode(),
                before.getEffectiveTime(), offlineTime, userId()) != 1)
        {
            throw new ServiceException("内部通知下架失败，请刷新后重试");
        }
        MerchantInternalNoticeVO result = managementDetail(noticeId);
        audit("内部通知", "OFFLINE", noticeId, result.getNoticeTitle(), "下架商户内部通知", before, result);
        return result;
    }

    /** 删除草稿或已下架通知，已发布通知必须先下架。 */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long noticeId)
    {
        requireApproved();
        MerchantInternalNoticeVO before = requireManagementNotice(noticeId);
        if (MerchantInternalNoticeStatus.PUBLISHED.getCode() == before.getStatus())
        {
            throw new ServiceException("已发布通知不能删除，请先下架");
        }
        if (mapper.softDelete(tenantId(), memberId(), noticeId, userId()) != 1)
        {
            throw new ServiceException("内部通知删除失败，请刷新后重试");
        }
        audit("内部通知", "DELETE", noticeId, before.getNoticeTitle(), "删除商户内部通知", before, null);
    }

    /** 标记一条当前用户可见通知已读。 */
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long noticeId)
    {
        if (mapper.insertRead(tenantId(), memberId(), userId(), noticeId) <= 0)
        {
            throw new ServiceException("内部通知不存在、已下架或当前员工组不可见");
        }
    }

    /** 批量标记当前顶部列表中的通知已读。 */
    @Transactional(rollbackFor = Exception.class)
    public void markReadAll(Long[] noticeIds)
    {
        Long[] normalized = normalizeIds(noticeIds);
        if (normalized.length > 0)
        {
            mapper.insertReadAll(tenantId(), memberId(), userId(), normalized);
        }
    }

    private MerchantInternalNoticeCommand normalize(MerchantInternalNoticeCommand source, boolean preserveStatus)
    {
        if (source == null)
        {
            throw new ServiceException("内部通知数据不能为空");
        }
        MerchantInternalNoticeCommand command = new MerchantInternalNoticeCommand();
        command.setNoticeTitle(trim(source.getNoticeTitle()));
        command.setNoticeType(source.getNoticeType() == null
                ? MerchantInternalNoticeType.NOTIFICATION.getCode() : source.getNoticeType());
        command.setNoticeContent(source.getNoticeContent() == null ? null : source.getNoticeContent().trim());
        command.setTargetScope(source.getTargetScope() == null
                ? MerchantInternalNoticeScope.ALL_USERS.getCode() : source.getTargetScope());
        command.setStatus(preserveStatus && source.getStatus() != null
                ? source.getStatus() : MerchantInternalNoticeStatus.DRAFT.getCode());
        command.setEffectiveTime(source.getEffectiveTime());
        command.setOfflineTime(source.getOfflineTime());
        command.setRoleIds(normalizeIds(source.getRoleIds()));
        command.setRemark(trim(source.getRemark()));
        return command;
    }

    private void validate(MerchantInternalNoticeCommand command)
    {
        if (StringUtils.isBlank(command.getNoticeTitle()) || command.getNoticeTitle().length() > MAX_TITLE_LENGTH)
        {
            throw new ServiceException("通知标题不能为空且不能超过160个字符");
        }
        if (StringUtils.isBlank(command.getNoticeContent()) || command.getNoticeContent().length() > MAX_CONTENT_LENGTH)
        {
            throw new ServiceException("通知正文不能为空且不能超过100000个字符");
        }
        if (!MerchantInternalNoticeType.contains(command.getNoticeType()))
        {
            throw new ServiceException("通知类型不合法");
        }
        if (!MerchantInternalNoticeScope.contains(command.getTargetScope()))
        {
            throw new ServiceException("通知投放范围不合法");
        }
        if (!MerchantInternalNoticeStatus.contains(command.getStatus()))
        {
            throw new ServiceException("通知状态不合法");
        }
        if (command.getEffectiveTime() != null && command.getOfflineTime() != null
                && !command.getOfflineTime().after(command.getEffectiveTime()))
        {
            throw new ServiceException("下架时间必须晚于生效时间");
        }
        if (MerchantInternalNoticeScope.SPECIFIED_ROLES.getCode() == command.getTargetScope())
        {
            if (command.getRoleIds() == null || command.getRoleIds().length == 0
                    || mapper.countValidRoles(tenantId(), memberId(), command.getRoleIds()) != command.getRoleIds().length)
            {
                throw new ServiceException("指定员工组不存在、已停用或不属于当前商户");
            }
        }
        else
        {
            command.setRoleIds(new Long[0]);
        }
    }

    private void validateQuery(MerchantInternalNoticeQuery query)
    {
        if (query.getNoticeType() != null && !MerchantInternalNoticeType.contains(query.getNoticeType()))
        {
            throw new ServiceException("通知类型不合法");
        }
        if (query.getTargetScope() != null && !MerchantInternalNoticeScope.contains(query.getTargetScope()))
        {
            throw new ServiceException("通知投放范围不合法");
        }
        if (query.getStatus() != null && !MerchantInternalNoticeStatus.contains(query.getStatus()))
        {
            throw new ServiceException("通知状态不合法");
        }
    }

    private void syncTargetRoles(Long noticeId, Integer targetScope, Long[] roleIds)
    {
        mapper.deleteTargetRoles(tenantId(), memberId(), noticeId, userId());
        if (MerchantInternalNoticeScope.SPECIFIED_ROLES.getCode() == targetScope && roleIds.length > 0)
        {
            mapper.insertTargetRoles(tenantId(), memberId(), noticeId, roleIds, userId());
        }
    }

    private MerchantInternalNoticeVO requireManagementNotice(Long noticeId)
    {
        if (noticeId == null || noticeId <= 0)
        {
            throw new ServiceException("内部通知唯一编号不合法");
        }
        MerchantInternalNoticeVO notice = mapper.selectManagementById(tenantId(), memberId(), noticeId);
        if (notice == null)
        {
            throw new ServiceException("内部通知不存在或已删除");
        }
        return notice;
    }

    private MerchantInternalNoticeCommand commandFrom(MerchantInternalNoticeVO notice)
    {
        MerchantInternalNoticeCommand command = new MerchantInternalNoticeCommand();
        command.setInternalNoticeId(notice.getInternalNoticeId());
        command.setNoticeTitle(notice.getNoticeTitle());
        command.setNoticeType(notice.getNoticeType());
        command.setNoticeContent(notice.getNoticeContent());
        command.setTargetScope(notice.getTargetScope());
        command.setStatus(notice.getStatus());
        command.setEffectiveTime(notice.getEffectiveTime());
        command.setOfflineTime(notice.getOfflineTime());
        command.setRoleIds(toRoleArray(mapper.selectTargetRoleIds(tenantId(), memberId(), notice.getInternalNoticeId())));
        command.setRemark(notice.getRemark());
        return command;
    }

    private void audit(String module, String operationType, Long noticeId, String title, String description,
            Object before, Object after)
    {
        auditRecorder.record(module, operationType, "MERCHANT_INTERNAL_NOTICE", String.valueOf(noticeId), title,
                description, before, after);
    }

    private void requireApproved()
    {
        MerchantContextUtils.requireMerchantApproved();
    }

    private Long tenantId() { return MerchantContextUtils.getTenantId(); }

    private Long memberId() { return MerchantContextUtils.getMemberId(); }

    private Long userId() { return SecurityUtils.getUserId(); }

    private String trim(String value) { return value == null ? null : value.trim(); }

    private Long[] normalizeIds(Long[] values)
    {
        Set<Long> ids = new LinkedHashSet<Long>();
        if (values != null)
        {
            for (Long value : values)
            {
                if (value != null && value > 0)
                {
                    ids.add(value);
                }
            }
        }
        return ids.toArray(new Long[0]);
    }

    private Long[] toRoleArray(List<Long> roleIds)
    {
        return normalizeIds(roleIds == null ? null : roleIds.toArray(new Long[0]));
    }

    private <T> List<T> emptyIfNull(List<T> rows)
    {
        return rows == null ? Collections.<T>emptyList() : rows;
    }
}
