package com.ruoyi.system.service.rental;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.mapper.rental.PlatformMemberRiskMapper;
import com.ruoyi.system.model.rental.PlatformMemberBlacklistCommand;
import com.ruoyi.system.model.rental.PlatformMemberBlacklistReasonType;
import com.ruoyi.system.model.rental.PlatformMemberBlacklistRecordVO;
import com.ruoyi.system.model.rental.PlatformMemberBlacklistReleaseCommand;
import com.ruoyi.system.model.rental.PlatformMemberBlacklistStatus;
import com.ruoyi.system.model.rental.PlatformMemberCreditEventType;
import com.ruoyi.system.model.rental.PlatformMemberCreditLevel;
import com.ruoyi.system.model.rental.PlatformMemberCreditRecordCommand;
import com.ruoyi.system.model.rental.PlatformMemberCreditRecordVO;
import com.ruoyi.system.model.rental.PlatformMemberCreditReversalCommand;
import com.ruoyi.system.model.rental.PlatformMemberRiskScope;
import com.ruoyi.system.model.rental.PlatformMemberRiskVO;

/**
 * 平台商户信用与黑名单服务。
 *
 * <p>信用分和黑名单只作为可追溯的治理事实落库；本阶段不直接改变商户现有接单资格，避免运营人员
 * 录入风险记录时绕过认证资格状态机。</p>
 */
@Service
public class PlatformMemberRiskService
{
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int DEFAULT_SCORE = 100;
    private static final int MAX_REASON_LENGTH = 500;
    private static final int MAX_SOURCE_LENGTH = 128;

    private final PlatformMemberRiskMapper mapper;

    public PlatformMemberRiskService(PlatformMemberRiskMapper mapper)
    {
        this.mapper = mapper;
    }

    /** 查询商户当前信用分、信用流水和黑名单历史。 */
    public PlatformMemberRiskVO get(Long memberId)
    {
        PlatformMemberRiskScope scope = requireScope(memberId);
        PlatformMemberRiskVO result = new PlatformMemberRiskVO();
        Map<String, Object> account = mapper.selectCreditAccountForUpdate(scope.getTenantId(), scope.getMemberId());
        int score = account == null ? DEFAULT_SCORE : intValue(account.get("creditScore"), DEFAULT_SCORE);
        PlatformMemberCreditLevel level = PlatformMemberCreditLevel.fromScore(score);
        result.setCreditScore(Integer.valueOf(score));
        result.setCreditLevel(Integer.valueOf(level.getCode()));
        result.setCreditLevelName(level.getLabel());
        result.setCreditRecords(decorateCreditRecords(mapper.selectCreditRecords(scope.getTenantId(), scope.getMemberId())));
        result.setBlacklistRecords(decorateBlacklistRecords(
                mapper.selectBlacklistRecords(scope.getTenantId(), scope.getMemberId())));
        return result;
    }

    /** 新增信用分变更记录，并在同一事务中更新当前信用账户。 */
    @Transactional(rollbackFor = Exception.class)
    public PlatformMemberRiskVO addCreditRecord(Long memberId, PlatformMemberCreditRecordCommand command)
    {
        PlatformMemberRiskScope scope = requireScope(memberId);
        PlatformMemberCreditEventType eventType = requiredCreditEventType(command == null ? null : command.getEventType());
        int change = requiredScoreChange(command == null ? null : command.getScoreChange());
        String reason = requiredText(command == null ? null : command.getReason(), "信用变更原因", MAX_REASON_LENGTH);
        String sourceNo = optionalText(command == null ? null : command.getSourceNo(), MAX_SOURCE_LENGTH);
        Long operatorId = SecurityUtils.getUserId();
        mapper.insertCreditAccountIfAbsent(scope.getTenantId(), scope.getMemberId(), operatorId);
        Map<String, Object> account = mapper.selectCreditAccountForUpdate(scope.getTenantId(), scope.getMemberId());
        if (account == null)
        {
            throw new ServiceException("商户信用账户初始化失败");
        }
        int before = intValue(account.get("creditScore"), DEFAULT_SCORE);
        int after = Math.max(0, Math.min(100, before + change));
        PlatformMemberCreditLevel level = PlatformMemberCreditLevel.fromScore(after);
        if (mapper.updateCreditAccount(scope.getTenantId(), scope.getMemberId(), Integer.valueOf(after),
                Integer.valueOf(level.getCode()), operatorId) != 1)
        {
            throw new ServiceException("商户信用分更新失败");
        }
        String recordNo = buildRecordNo("CR-");
        if (mapper.insertCreditRecord(scope.getTenantId(), scope.getMemberId(), recordNo, eventType.getCode(), before,
                change, after, reason, sourceNo, null, operatorId) != 1)
        {
            throw new ServiceException("商户信用流水保存失败");
        }
        return get(memberId);
    }

    /**
     * 对原信用流水追加一条反向流水，供平台审核通过申诉后恢复信用分。
     * 原流水和原分值快照保持不变，同一原流水只允许成功冲正一次。
     */
    @Transactional(rollbackFor = Exception.class)
    public PlatformMemberRiskVO reverseCreditRecord(Long memberId, String recordNo,
            PlatformMemberCreditReversalCommand command)
    {
        PlatformMemberRiskScope scope = requireScope(memberId);
        String normalizedRecordNo = requiredText(recordNo, "信用记录编码", 48);
        String reason = requiredText(command == null ? null : command.getReason(), "冲正原因", MAX_REASON_LENGTH);
        Map<String, Object> original = mapper.selectCreditRecordForUpdate(scope.getTenantId(), scope.getMemberId(),
                normalizedRecordNo);
        if (original == null)
        {
            throw new ServiceException("信用流水不存在或无权操作");
        }
        String originalReversalOf = stringValue(original.get("reversalOfRecordNo"));
        if (originalReversalOf != null)
        {
            throw new ServiceException("申诉冲正流水不能再次冲正");
        }
        if (mapper.countCreditReversal(scope.getTenantId(), scope.getMemberId(), normalizedRecordNo) > 0)
        {
            throw new ServiceException("该信用流水已经冲正，不能重复操作");
        }

        mapper.insertCreditAccountIfAbsent(scope.getTenantId(), scope.getMemberId(), SecurityUtils.getUserId());
        Map<String, Object> account = mapper.selectCreditAccountForUpdate(scope.getTenantId(), scope.getMemberId());
        if (account == null)
        {
            throw new ServiceException("商户信用账户初始化失败");
        }
        int originalBefore = intValue(original.get("scoreBefore"), 0);
        int originalAfter = intValue(original.get("scoreAfter"), 0);
        int appliedChange = originalAfter - originalBefore;
        if (appliedChange == 0)
        {
            throw new ServiceException("该信用流水没有可冲正的分值变化");
        }
        int before = intValue(account.get("creditScore"), DEFAULT_SCORE);
        int reverseChange = -appliedChange;
        int after = Math.max(0, Math.min(100, before + reverseChange));
        PlatformMemberCreditLevel level = PlatformMemberCreditLevel.fromScore(after);
        Long operatorId = SecurityUtils.getUserId();
        if (mapper.updateCreditAccount(scope.getTenantId(), scope.getMemberId(), Integer.valueOf(after),
                Integer.valueOf(level.getCode()), operatorId) != 1)
        {
            throw new ServiceException("商户信用分更新失败");
        }
        String reversalRecordNo = buildRecordNo("CR-");
        if (mapper.insertCreditRecord(scope.getTenantId(), scope.getMemberId(), reversalRecordNo,
                PlatformMemberCreditEventType.APPEAL_REVERSAL.getCode(), before, reverseChange, after, reason,
                stringValue(original.get("sourceNo")), normalizedRecordNo, operatorId) != 1)
        {
            throw new ServiceException("信用冲正流水保存失败");
        }
        return get(memberId);
    }

    /** 将商户加入黑名单；同一商户存在未过期生效记录时拒绝重复加入。 */
    @Transactional(rollbackFor = Exception.class)
    public PlatformMemberRiskVO addBlacklist(Long memberId, PlatformMemberBlacklistCommand command)
    {
        PlatformMemberRiskScope scope = requireScope(memberId);
        PlatformMemberBlacklistReasonType reasonType = requiredBlacklistReasonType(
                command == null ? null : command.getReasonType());
        String reason = requiredText(command == null ? null : command.getReason(), "列入黑名单原因", MAX_REASON_LENGTH);
        String expiresTime = normalizeExpiresTime(command == null ? null : command.getExpiresTime());
        if (mapper.countActiveBlacklist(scope.getTenantId(), scope.getMemberId()) > 0)
        {
            throw new ServiceException("该商户已有生效中的黑名单记录");
        }
        if (mapper.insertBlacklistRecord(scope.getTenantId(), scope.getMemberId(), buildRecordNo("BL-"),
                reasonType.getCode(), reason, expiresTime, SecurityUtils.getUserId()) != 1)
        {
            throw new ServiceException("商户黑名单记录保存失败");
        }
        return get(memberId);
    }

    /** 解除指定黑名单记录，保留原记录和解除说明。 */
    @Transactional(rollbackFor = Exception.class)
    public PlatformMemberRiskVO releaseBlacklist(Long memberId, String recordNo,
            PlatformMemberBlacklistReleaseCommand command)
    {
        PlatformMemberRiskScope scope = requireScope(memberId);
        String normalizedNo = requiredText(recordNo, "黑名单记录编码", 48);
        String remark = requiredText(command == null ? null : command.getRemark(), "解除说明", MAX_REASON_LENGTH);
        Map<String, Object> record = mapper.selectBlacklistForUpdate(scope.getTenantId(), scope.getMemberId(), normalizedNo);
        if (record == null)
        {
            throw new ServiceException("黑名单记录不存在或无权操作");
        }
        if (intValue(record.get("blacklistStatus"), 0) != PlatformMemberBlacklistStatus.ACTIVE.getCode())
        {
            throw new ServiceException("黑名单记录已经解除");
        }
        if (mapper.releaseBlacklist(longValue(record.get("recordId")), remark, SecurityUtils.getUserId()) != 1)
        {
            throw new ServiceException("解除黑名单失败");
        }
        return get(memberId);
    }

    private PlatformMemberRiskScope requireScope(Long memberId)
    {
        if (memberId == null)
        {
            throw new ServiceException("商户唯一编号不能为空");
        }
        PlatformMemberRiskScope scope = mapper.selectMemberScope(memberId);
        if (scope == null || scope.getTenantId() == null || scope.getMemberId() == null)
        {
            throw new ServiceException("商户不存在或已删除");
        }
        return scope;
    }

    private PlatformMemberCreditEventType requiredCreditEventType(Integer code)
    {
        PlatformMemberCreditEventType value = PlatformMemberCreditEventType.fromCode(code);
        if (value == null)
        {
            throw new ServiceException("信用事件类型不合法");
        }
        return value;
    }

    private PlatformMemberBlacklistReasonType requiredBlacklistReasonType(Integer code)
    {
        PlatformMemberBlacklistReasonType value = PlatformMemberBlacklistReasonType.fromCode(code);
        if (value == null)
        {
            throw new ServiceException("黑名单原因类型不合法");
        }
        return value;
    }

    private int requiredScoreChange(Integer value)
    {
        if (value == null || value.intValue() == 0 || value.intValue() < -100 || value.intValue() > 100)
        {
            throw new ServiceException("信用分变更值必须在-100到100之间且不能为0");
        }
        return value.intValue();
    }

    private String normalizeExpiresTime(String value)
    {
        if (value == null || value.trim().isEmpty())
        {
            return null;
        }
        String normalized = value.trim().replace('T', ' ');
        try
        {
            LocalDateTime expiresTime = LocalDateTime.parse(normalized, DATE_TIME);
            if (!expiresTime.isAfter(LocalDateTime.now()))
            {
                throw new ServiceException("黑名单到期时间必须晚于当前时间");
            }
            return expiresTime.format(DATE_TIME);
        }
        catch (DateTimeParseException exception)
        {
            throw new ServiceException("黑名单到期时间格式不正确，应为yyyy-MM-dd HH:mm:ss");
        }
    }

    private List<PlatformMemberCreditRecordVO> decorateCreditRecords(List<PlatformMemberCreditRecordVO> rows)
    {
        if (rows == null || rows.isEmpty())
        {
            return Collections.emptyList();
        }
        for (PlatformMemberCreditRecordVO row : rows)
        {
            PlatformMemberCreditEventType type = PlatformMemberCreditEventType.fromCode(row.getEventType());
            row.setEventTypeName(type == null ? "未知类型" : type.getLabel());
        }
        return rows;
    }

    private List<PlatformMemberBlacklistRecordVO> decorateBlacklistRecords(List<PlatformMemberBlacklistRecordVO> rows)
    {
        if (rows == null || rows.isEmpty())
        {
            return Collections.emptyList();
        }
        for (PlatformMemberBlacklistRecordVO row : rows)
        {
            PlatformMemberBlacklistReasonType reasonType = PlatformMemberBlacklistReasonType.fromCode(row.getReasonType());
            PlatformMemberBlacklistStatus status = PlatformMemberBlacklistStatus.fromCode(row.getBlacklistStatus());
            row.setReasonTypeName(reasonType == null ? "未知原因" : reasonType.getLabel());
            row.setBlacklistStatusName(status == null ? "未知状态" : status.getLabel());
        }
        return rows;
    }

    private String buildRecordNo(String prefix)
    {
        return prefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String requiredText(String value, String label, int maxLength)
    {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty())
        {
            throw new ServiceException(label + "不能为空");
        }
        if (normalized.length() > maxLength)
        {
            throw new ServiceException(label + "不能超过" + maxLength + "个字符");
        }
        return normalized;
    }

    private String optionalText(String value, int maxLength)
    {
        if (value == null || value.trim().isEmpty())
        {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength)
        {
            throw new ServiceException("关联业务编码不能超过" + maxLength + "个字符");
        }
        return normalized;
    }

    private int intValue(Object value, int fallback)
    {
        return value instanceof Number ? ((Number) value).intValue() : fallback;
    }

    private String stringValue(Object value)
    {
        if (value == null)
        {
            return null;
        }
        String result = String.valueOf(value).trim();
        return result.isEmpty() ? null : result;
    }

    private Long longValue(Object value)
    {
        return value instanceof Number ? Long.valueOf(((Number) value).longValue()) : null;
    }
}
