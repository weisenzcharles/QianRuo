package com.ruoyi.system;

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import com.ruoyi.system.domain.SysCodeRule;
import com.ruoyi.system.mapper.SysCodeRuleMapper;
import com.ruoyi.system.service.SysCodeRuleService;

/** 验证统一编码规则的格式化和自增行为。 */
public class SysCodeRuleServiceTest
{
    /** 验证前缀和总长度会生成固定宽度的递增编码。 */
    @Test
    public void nextCodeUsesConfiguredPrefixAndLength()
    {
        InMemoryCodeRuleMapper mapper = new InMemoryCodeRuleMapper();
        SysCodeRule rule = new SysCodeRule();
        rule.setRuleId(1L);
        rule.setCodeType("warehouse");
        rule.setCodeName("仓库编码");
        rule.setPrefix("wh");
        rule.setCodeLength(8);
        rule.setCurrentValue(0L);
        rule.setStatus(0);
        mapper.rule = rule;

        SysCodeRuleService service = new SysCodeRuleService(mapper);

        require("WH000001".equals(service.nextCode("warehouse")), "first code must use configured width");
        require("WH000002".equals(service.nextCode("warehouse")), "second code must increment atomically");
    }

    /** 验证商户编码计数器落后时从数据库已用最大序号继续分配。 */
    @Test
    public void nextMerchantCodeSkipsPersistedCodesWhenCounterIsStale()
    {
        InMemoryCodeRuleMapper mapper = new InMemoryCodeRuleMapper();
        SysCodeRule rule = new SysCodeRule();
        rule.setRuleId(2L);
        rule.setCodeType("merchant");
        rule.setCodeName("商户编码");
        rule.setPrefix("M");
        rule.setCodeLength(12);
        rule.setCurrentValue(1L);
        rule.setStatus(0);
        mapper.rule = rule;
        mapper.maxMemberCodeSequence = 2L;

        String nextCode = new SysCodeRuleService(mapper).nextCode("merchant");

        require("M00000000003".equals(nextCode), "merchant code must skip an already persisted code");
        require(Long.valueOf(3L).equals(rule.getCurrentValue()), "counter must be advanced past persisted codes");
    }

    /** 验证编码序列宽度不足时拒绝继续分配，避免生成超出规则长度的编码。 */
    @Test
    public void nextCodeRejectsSequenceOverflow()
    {
        InMemoryCodeRuleMapper mapper = new InMemoryCodeRuleMapper();
        SysCodeRule rule = new SysCodeRule();
        rule.setRuleId(1L);
        rule.setCodeType("product");
        rule.setCodeName("商品编码");
        rule.setPrefix("P");
        rule.setCodeLength(3);
        rule.setCurrentValue(999L);
        rule.setStatus(0);
        mapper.rule = rule;

        try
        {
            new SysCodeRuleService(mapper).nextCode("product");
            throw new AssertionError("sequence overflow must be rejected");
        }
        catch (RuntimeException expected)
        {
            require(expected.getMessage().contains("扩大编码长度"), "overflow message must explain the remedy");
        }
    }

    /** 执行无第三方断言库依赖的测试断言。 */
    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }

    /** 保存单条规则并模拟数据库行锁读取的测试数据访问实现。 */
    private static class InMemoryCodeRuleMapper implements SysCodeRuleMapper
    {
        private SysCodeRule rule;
        private Long maxMemberCodeSequence = 0L;

        /** 返回内存中的唯一编码规则。 */
        @Override
        public List<SysCodeRule> selectList()
        {
            return Collections.singletonList(rule);
        }

        /** 按类型返回内存中的编码规则。 */
        @Override
        public SysCodeRule selectByCodeType(String codeType)
        {
            return rule != null && codeType.equals(rule.getCodeType()) ? rule : null;
        }

        /** 测试环境不需要真实数据库锁，直接返回同一规则对象。 */
        @Override
        public SysCodeRule selectByCodeTypeForUpdate(String codeType)
        {
            return selectByCodeType(codeType);
        }

        /** 返回测试中预置的已分配商户编码最大序号。 */
        @Override
        public Long selectMaxMemberCodeSequence(String prefix)
        {
            return maxMemberCodeSequence;
        }

        /** 模拟事务内保存递增后的序号。 */
        @Override
        public int updateCurrentValue(Long ruleId, Long currentValue)
        {
            rule.setCurrentValue(currentValue);
            return 1;
        }

        /** 模拟保存规则配置。 */
        @Override
        public int updateRule(SysCodeRule value)
        {
            rule.setPrefix(value.getPrefix());
            rule.setCodeLength(value.getCodeLength());
            rule.setStatus(value.getStatus());
            return 1;
        }
    }
}
