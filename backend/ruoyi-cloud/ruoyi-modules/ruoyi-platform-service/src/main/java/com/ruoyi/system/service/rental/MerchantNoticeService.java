package com.ruoyi.system.service.rental;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.enums.SystemCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.system.api.model.MerchantNoticeDetail;
import com.ruoyi.system.api.model.MerchantNoticeSummary;
import com.ruoyi.system.api.model.MerchantNoticeTop;
import com.ruoyi.system.domain.SysMerchantNotice;
import com.ruoyi.system.domain.rental.RentalMember;
import com.ruoyi.system.mapper.SysMerchantNoticeMapper;
import com.ruoyi.system.mapper.SysMerchantNoticeReadMapper;
import com.ruoyi.system.mapper.rental.PlatformGovernanceMapper;
import com.ruoyi.system.mapper.rental.RentalMemberMapper;
import com.ruoyi.system.model.rental.MerchantNoticeEditVO;
import com.ruoyi.system.model.rental.MerchantNoticeListVO;
import com.ruoyi.system.model.rental.MerchantNoticeMemberOption;

/** 平台商户公告业务服务，负责发布范围和商户可见性校验。 */
@Service
public class MerchantNoticeService
{
    private final SysMerchantNoticeMapper noticeMapper;
    private final SysMerchantNoticeReadMapper readMapper;
    private final RentalMemberMapper memberMapper;
    private final PlatformGovernanceMapper governanceMapper;

    public MerchantNoticeService(SysMerchantNoticeMapper noticeMapper, SysMerchantNoticeReadMapper readMapper,
            RentalMemberMapper memberMapper, PlatformGovernanceMapper governanceMapper)
    {
        this.noticeMapper = noticeMapper;
        this.readMapper = readMapper;
        this.memberMapper = memberMapper;
        this.governanceMapper = governanceMapper;
    }

    /** 查询平台商户公告管理列表。 */
    public List<MerchantNoticeListVO> list(SysMerchantNotice query)
    {
        return noticeMapper.selectMerchantNoticeList(query == null ? new SysMerchantNotice() : query);
    }

    /** 查询公告编辑详情，并回填指定商户选项。 */
    public MerchantNoticeEditVO get(Long merchantNoticeId)
    {
        MerchantNoticeEditVO notice = requireNotice(merchantNoticeId);
        notice.setTargetMemberIds(noticeMapper.selectTargetMemberIds(merchantNoticeId));
        return notice;
    }

    /** 查询可发布公告的有效商户选项。 */
    public List<MerchantNoticeMemberOption> memberOptions()
    {
        return noticeMapper.selectMemberOptions();
    }

    /** 新增商户公告。 */
    @Transactional(rollbackFor = Exception.class)
    public int add(SysMerchantNotice command, Long operator)
    {
        SysMerchantNotice notice = normalize(command);
        validate(notice);
        notice.setCreateBy(operator);
        int affected = noticeMapper.insertMerchantNotice(notice);
        if (affected != 1 || notice.getMerchantNoticeId() == null)
        {
            throw new ServiceException("商户公告新增失败");
        }
        if (isSpecified(notice))
        {
            noticeMapper.insertTargetMembers(notice);
        }
        return affected;
    }

    /** 修改商户公告及其发布范围。 */
    @Transactional(rollbackFor = Exception.class)
    public int update(SysMerchantNotice command, Long operator)
    {
        SysMerchantNotice notice = normalize(command);
        requireNotice(notice.getMerchantNoticeId());
        validate(notice);
        notice.setUpdateBy(operator);
        int affected = noticeMapper.updateMerchantNotice(notice);
        if (affected != 1)
        {
            throw new ServiceException("商户公告不存在或已删除");
        }
        noticeMapper.deleteTargetMembers(notice.getMerchantNoticeId());
        if (isSpecified(notice))
        {
            noticeMapper.insertTargetMembers(notice);
        }
        return affected;
    }

    /** 批量删除商户公告。 */
    @Transactional(rollbackFor = Exception.class)
    public int delete(Long[] merchantNoticeIds, Long operator)
    {
        if (merchantNoticeIds == null || merchantNoticeIds.length == 0)
        {
            throw new ServiceException("请选择要删除的商户公告");
        }
        for (Long merchantNoticeId : merchantNoticeIds)
        {
            if (governanceMapper.insertMerchantNoticeRecycle(merchantNoticeId, operator) != 1)
            {
                throw new ServiceException("商户公告不存在或已删除，无法写入回收站");
            }
        }
        readMapper.deleteByNoticeIds(merchantNoticeIds);
        for (Long merchantNoticeId : merchantNoticeIds)
        {
            noticeMapper.deleteTargetMembers(merchantNoticeId);
        }
        return noticeMapper.deleteMerchantNotices(merchantNoticeIds, operator);
    }

    /** 查询当前商户用户可见的顶部公告和未读数量。 */
    public MerchantNoticeTop top(Long memberId, Long merchantUserId, Integer limit)
    {
        requireMerchantContext(memberId, merchantUserId);
        int pageLimit = limit == null ? 5 : Math.min(Math.max(limit, 1), 20);
        List<MerchantNoticeSummary> notices = noticeMapper.selectTopForMerchant(memberId, merchantUserId, pageLimit);
        MerchantNoticeTop result = new MerchantNoticeTop();
        result.setNotices(notices == null ? Collections.<MerchantNoticeSummary>emptyList() : notices);
        result.setUnreadCount(noticeMapper.selectUnreadCountForMerchant(memberId, merchantUserId));
        return result;
    }

    /** 查询当前商户用户可见的公告正文。 */
    public MerchantNoticeDetail detail(Long noticeId, Long memberId, Long merchantUserId)
    {
        requireMerchantContext(memberId, merchantUserId);
        MerchantNoticeDetail detail = noticeMapper.selectDetailForMerchant(noticeId, memberId, merchantUserId);
        if (detail == null)
        {
            throw new ServiceException("公告不存在、已停用或当前商户不可见");
        }
        return detail;
    }

    /** 标记当前商户用户的一条可见公告已读。 */
    public void markRead(Long noticeId, Long memberId, Long merchantUserId)
    {
        requireMerchantContext(memberId, merchantUserId);
        // MySQL ON DUPLICATE KEY UPDATE may report 2 for an existing row, so only zero means invisible.
        if (readMapper.insertRead(noticeId, memberId, merchantUserId) <= 0)
        {
            throw new ServiceException("公告不存在、已停用或当前商户不可见");
        }
    }

    /** 仅标记当前顶部列表中且当前商户可见的公告已读。 */
    public void markReadAll(List<Long> noticeIds, Long memberId, Long merchantUserId)
    {
        requireMerchantContext(memberId, merchantUserId);
        if (noticeIds == null || noticeIds.isEmpty())
        {
            return;
        }
        List<Long> normalized = normalizeIds(noticeIds);
        if (!normalized.isEmpty())
        {
            readMapper.insertReadAll(memberId, merchantUserId, normalized);
        }
    }

    private MerchantNoticeEditVO requireNotice(Long merchantNoticeId)
    {
        if (merchantNoticeId == null || merchantNoticeId <= 0)
        {
            throw new ServiceException("商户公告唯一编号不合法");
        }
        MerchantNoticeEditVO notice = noticeMapper.selectMerchantNoticeById(merchantNoticeId);
        if (notice == null)
        {
            throw new ServiceException("商户公告不存在或已删除");
        }
        return notice;
    }

    private SysMerchantNotice normalize(SysMerchantNotice command)
    {
        if (command == null)
        {
            throw new ServiceException("商户公告数据不能为空");
        }
        SysMerchantNotice notice = new SysMerchantNotice();
        notice.setMerchantNoticeId(command.getMerchantNoticeId());
        notice.setNoticeTitle(StringUtils.trim(command.getNoticeTitle()));
        notice.setNoticeType(command.getNoticeType() == null
                ? SystemCode.NoticeType.ANNOUNCEMENT.getCode() : command.getNoticeType());
        notice.setNoticeContent(command.getNoticeContent());
        notice.setTargetScope(command.getTargetScope() == null
                ? RentalCode.MerchantNoticeScope.ALL_MERCHANTS.getCode() : command.getTargetScope());
        notice.setStatus(command.getStatus() == null
                ? RentalCode.EnableStatus.ENABLED.getCode() : command.getStatus());
        notice.setRemark(StringUtils.trim(command.getRemark()));
        notice.setTargetMemberIds(normalizeIds(command.getTargetMemberIds()));
        return notice;
    }

    private void validate(SysMerchantNotice notice)
    {
        if (StringUtils.isBlank(notice.getNoticeTitle()) || notice.getNoticeTitle().length() > 50)
        {
            throw new ServiceException("公告标题不能为空且不能超过50个字符");
        }
        if (!isNoticeType(notice.getNoticeType()))
        {
            throw new ServiceException("公告类型不合法");
        }
        if (!isStatus(notice.getStatus()))
        {
            throw new ServiceException("公告状态不合法");
        }
        if (!isScope(notice.getTargetScope()))
        {
            throw new ServiceException("公告发布范围不合法");
        }
        if (isSpecified(notice))
        {
            if (notice.getTargetMemberIds().isEmpty())
            {
                throw new ServiceException("请选择指定商户");
            }
            List<RentalMember> members = memberMapper.selectRentalMembersByIds(notice.getTargetMemberIds());
            if (members == null || members.size() != notice.getTargetMemberIds().size())
            {
                throw new ServiceException("指定商户不存在或已删除");
            }
            for (RentalMember member : members)
            {
                if (!RentalCode.EnableStatus.ENABLED.getCode().equals(member.getStatus())
                        || !RentalCode.AuthenticationStatus.VERIFIED.getCode().equals(member.getAuthStatus()))
                {
                    throw new ServiceException("指定商户必须为已认证且启用状态");
                }
            }
        }
        else
        {
            notice.setTargetMemberIds(Collections.<Long>emptyList());
        }
    }

    private boolean isSpecified(SysMerchantNotice notice)
    {
        return RentalCode.MerchantNoticeScope.SPECIFIED_MERCHANTS.getCode().equals(notice.getTargetScope());
    }

    private boolean isScope(Integer value)
    {
        return RentalCode.MerchantNoticeScope.ALL_MERCHANTS.getCode().equals(value)
                || RentalCode.MerchantNoticeScope.SPECIFIED_MERCHANTS.getCode().equals(value);
    }

    private boolean isStatus(Integer value)
    {
        return RentalCode.EnableStatus.ENABLED.getCode().equals(value)
                || RentalCode.EnableStatus.DISABLED.getCode().equals(value);
    }

    private boolean isNoticeType(Integer value)
    {
        return SystemCode.NoticeType.NOTIFICATION.getCode().equals(value)
                || SystemCode.NoticeType.ANNOUNCEMENT.getCode().equals(value);
    }

    private List<Long> normalizeIds(List<Long> ids)
    {
        if (ids == null || ids.isEmpty())
        {
            return new ArrayList<Long>();
        }
        Set<Long> unique = new LinkedHashSet<Long>();
        for (Long id : ids)
        {
            if (id == null || id <= 0)
            {
                throw new ServiceException("商户唯一编号不合法");
            }
            unique.add(id);
        }
        return new ArrayList<Long>(unique);
    }

    private void requireMerchantContext(Long memberId, Long merchantUserId)
    {
        if (memberId == null || memberId <= 0 || merchantUserId == null || merchantUserId <= 0)
        {
            throw new ServiceException("商户公告访问上下文不完整");
        }
    }
}
