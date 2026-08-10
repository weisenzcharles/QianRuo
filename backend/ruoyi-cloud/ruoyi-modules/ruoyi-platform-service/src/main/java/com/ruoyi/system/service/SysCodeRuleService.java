package com.ruoyi.system.service;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.system.domain.SysCodeRule;
import com.ruoyi.system.mapper.SysCodeRuleMapper;

/**
 * 系统统一业务编码服务。
 *
 * <p>平台服务是唯一的编码分配中心。分配操作在数据库事务中锁定规则行、递增序号并
 * 格式化结果，业务服务只能通过内部契约获取结果，禁止在各业务服务中自行拼接时间、
 * 随机数或主键作为业务编码。</p>
 */
@Service
public class SysCodeRuleService
{
    /** 编码类型键格式，避免把路径或 SQL 特殊字符带入规则查询。 */
    private static final Pattern CODE_TYPE_PATTERN = Pattern.compile("^[a-z][a-z0-9_-]{1,63}$");

    /** 编码前缀格式，只允许稳定且可读的 ASCII 字符。 */
    private static final Pattern PREFIX_PATTERN = Pattern.compile("^[A-Z0-9_-]*$");

    /** 最小编码总长度，保证至少有一位数字序号。 */
    private static final int MIN_CODE_LENGTH = 2;

    /** 最大编码总长度，防止生成超长数据库编码。 */
    private static final int MAX_CODE_LENGTH = 64;

    /** Long 序号能够安全格式化的最大数字位数。 */
    private static final int MAX_SEQUENCE_WIDTH = 18;

    private final SysCodeRuleMapper codeRuleMapper;

    /**
     * 注入统一编码规则数据访问组件。
     *
     * @param codeRuleMapper 统一编码规则数据访问组件
     */
    public SysCodeRuleService(SysCodeRuleMapper codeRuleMapper)
    {
        this.codeRuleMapper = codeRuleMapper;
    }

    /**
     * 查询全部有效编码规则，供平台配置页面展示。
     *
     * @return 有效编码规则列表
     */
    public List<SysCodeRule> list()
    {
        return codeRuleMapper.selectList();
    }

    /**
     * 查询单个编码规则，不存在或已经停用时由调用方决定是否允许继续操作。
     *
     * @param codeType 编码类型键
     * @return 编码规则
     */
    public SysCodeRule get(String codeType)
    {
        String normalizedType = normalizeCodeType(codeType);
        SysCodeRule rule = codeRuleMapper.selectByCodeType(normalizedType);
        if (rule == null)
        {
            throw new ServiceException("编码规则不存在：" + normalizedType);
        }
        return rule;
    }

    /**
     * 在当前数据库事务中分配下一个编码。
     *
     * <p>行锁确保多个平台服务实例和多个业务服务实例并发调用时，每个编码类型的
     * 序号只会被一个事务取得。事务提交前发生异常会回滚序号递增，不会留下不可追踪的
     * 半成品业务记录；业务记录提交失败产生的序号空洞是允许的，因为序号不能复用。</p>
     *
     * @param codeType 编码类型键
     * @return 根据当前前缀和长度生成的下一个编码
     */
    @Transactional(rollbackFor = Exception.class)
    public String nextCode(String codeType)
    {
        SysCodeRule rule = codeRuleMapper.selectByCodeTypeForUpdate(normalizeCodeType(codeType));
        if (rule == null)
        {
            throw new ServiceException("编码规则不存在：" + codeType);
        }
        if (!Integer.valueOf(0).equals(rule.getStatus()))
        {
            throw new ServiceException("编码规则已停用：" + rule.getCodeName());
        }
        validateRule(rule.getPrefix(), rule.getCodeLength());

        long currentValue = rule.getCurrentValue() == null ? 0L : rule.getCurrentValue();
        if (currentValue == Long.MAX_VALUE)
        {
            throw new ServiceException("编码序列已达到系统上限：" + rule.getCodeName());
        }
        long nextValue = currentValue + 1L;
        int sequenceWidth = rule.getCodeLength() - normalizedPrefix(rule.getPrefix()).length();
        if (new BigInteger(String.valueOf(nextValue)).toString().length() > sequenceWidth)
        {
            throw new ServiceException("编码序列超出当前长度配置，请先扩大编码长度：" + rule.getCodeName());
        }
        if (codeRuleMapper.updateCurrentValue(rule.getRuleId(), nextValue) != 1)
        {
            throw new ServiceException("编码序列更新失败：" + rule.getCodeName());
        }
        return normalizedPrefix(rule.getPrefix()) + String.format(Locale.ROOT, "%0" + sequenceWidth + "d", nextValue);
    }

    /**
     * 修改编码规则的前缀、总长度和启用状态。
     *
     * <p>当前序号由服务端保留，不能通过管理接口重置，避免历史编码被重新分配。前缀
     * 统一转换为大写，长度必须为最终编码的总长度而不是数字部分长度。</p>
     *
     * @param codeType 编码类型键
     * @param incoming 管理端提交的规则配置
     * @param operator 操作人员
     * @return 更新后的编码规则
     */
    @Transactional(rollbackFor = Exception.class)
    public SysCodeRule update(String codeType, SysCodeRule incoming, String operator)
    {
        SysCodeRule existing = get(codeType);
        if (incoming == null)
        {
            throw new ServiceException("编码规则配置不能为空");
        }
        String prefix = normalizedPrefix(incoming.getPrefix());
        Integer codeLength = incoming.getCodeLength();
        validateRule(prefix, codeLength);
        if (incoming.getStatus() == null || (incoming.getStatus() != 0 && incoming.getStatus() != 1))
        {
            throw new ServiceException("编码规则状态只能是0或1");
        }
        existing.setPrefix(prefix);
        existing.setCodeLength(codeLength);
        existing.setStatus(incoming.getStatus());
        existing.setRemark(StringUtils.trim(incoming.getRemark()));
        existing.setUpdateBy(operator);
        if (codeRuleMapper.updateRule(existing) != 1)
        {
            throw new ServiceException("编码规则更新失败：" + existing.getCodeName());
        }
        return get(codeType);
    }

    /**
     * 规范并校验编码类型键。
     *
     * @param codeType 原始编码类型键
     * @return 去除首尾空白后的编码类型键
     */
    private String normalizeCodeType(String codeType)
    {
        String normalized = StringUtils.trim(codeType);
        if (StringUtils.isBlank(normalized) || !CODE_TYPE_PATTERN.matcher(normalized).matches())
        {
            throw new ServiceException("编码类型不合法");
        }
        return normalized;
    }

    /**
     * 规范编码前缀并转换为大写。
     *
     * @param prefix 原始前缀
     * @return 标准化前缀
     */
    private String normalizedPrefix(String prefix)
    {
        String normalized = StringUtils.trimToEmpty(prefix).toUpperCase(Locale.ROOT);
        if (!PREFIX_PATTERN.matcher(normalized).matches())
        {
            throw new ServiceException("编码前缀只允许大写字母、数字、下划线和短横线");
        }
        return normalized;
    }

    /**
     * 校验前缀与总长度组合能够产生有效编码。
     *
     * @param prefix 编码前缀
     * @param codeLength 最终编码总长度
     */
    private void validateRule(String prefix, Integer codeLength)
    {
        String normalizedPrefix = normalizedPrefix(prefix);
        if (codeLength == null || codeLength < MIN_CODE_LENGTH || codeLength > MAX_CODE_LENGTH)
        {
            throw new ServiceException("编码总长度必须在" + MIN_CODE_LENGTH + "到" + MAX_CODE_LENGTH + "之间");
        }
        int sequenceWidth = codeLength - normalizedPrefix.length();
        if (sequenceWidth < 1 || sequenceWidth > MAX_SEQUENCE_WIDTH)
        {
            throw new ServiceException("编码总长度必须为前缀长度加一到十八位数字");
        }
    }
}
