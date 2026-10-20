package com.ruoyi.system.rental;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.system.domain.rental.RentalCompensationRule;
import com.ruoyi.system.mapper.rental.RentalCompensationRuleMapper;
import com.ruoyi.system.model.rental.PlatformCompensationRuleCommand;
import com.ruoyi.system.service.rental.RentalCompensationRuleService;

/** 验证平台联盟赔付规则模板的边界和默认值。 */
public class RentalCompensationRuleServiceTest
{
    /** 验证新增规则会填充默认优先级、启用状态和操作人。 */
    @Test
    public void addRuleFillsDefaults()
    {
        CapturingMapper mapper = new CapturingMapper();
        RentalCompensationRuleService service = new RentalCompensationRuleService(mapper);
        PlatformCompensationRuleCommand command = command(1);
        command.setRuleName("  损坏赔付  ");

        require(service.add(command, 7L) == 1, "rule insert must affect one row");
        require(mapper.inserted != null, "rule must be inserted");
        require("损坏赔付".equals(mapper.inserted.getRuleName()), "rule name must be trimmed");
        require(Integer.valueOf(100).equals(mapper.inserted.getPriority()), "default priority must be 100");
        require(Integer.valueOf(RentalCode.EnableStatus.ENABLED.getCode()).equals(mapper.inserted.getStatus()),
                "new rule must be enabled by default");
        require(Long.valueOf(7L).equals(mapper.inserted.getCreateBy()), "operator must be recorded");
    }

    /** 验证未知纠纷类型不会写入数据库。 */
    @Test
    public void addRuleRejectsUnknownDisputeType()
    {
        CapturingMapper mapper = new CapturingMapper();
        RentalCompensationRuleService service = new RentalCompensationRuleService(mapper);
        PlatformCompensationRuleCommand command = command(8);

        expectServiceException(() -> service.add(command, 7L), "纠纷类型不合法");
        require(mapper.inserted == null, "invalid rule must not be inserted");
    }

    /** 验证调解校验使用 Mapper 的当前适用规则。 */
    @Test
    public void applicableDelegatesDisputeType()
    {
        CapturingMapper mapper = new CapturingMapper();
        RentalCompensationRule expected = new RentalCompensationRule();
        mapper.applicable = expected;
        RentalCompensationRuleService service = new RentalCompensationRuleService(mapper);

        require(service.applicable(2) == expected, "applicable rule must come from mapper");
        require(Integer.valueOf(2).equals(mapper.requestedDisputeType), "dispute type must be forwarded");
    }

    private PlatformCompensationRuleCommand command(Integer disputeType)
    {
        PlatformCompensationRuleCommand command = new PlatformCompensationRuleCommand();
        command.setRuleName("规则");
        command.setDisputeType(disputeType);
        command.setMaxAmount(new BigDecimal("100.00"));
        return command;
    }

    private void expectServiceException(Runnable action, String expectedMessage)
    {
        try
        {
            action.run();
            throw new AssertionError("expected ServiceException");
        }
        catch (ServiceException exception)
        {
            require(exception.getMessage().contains(expectedMessage),
                    "unexpected service exception: " + exception.getMessage());
        }
    }

    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }

    private static class CapturingMapper implements RentalCompensationRuleMapper
    {
        private RentalCompensationRule inserted;
        private RentalCompensationRule applicable;
        private Integer requestedDisputeType;

        @Override
        public List<RentalCompensationRule> selectList()
        {
            return Collections.emptyList();
        }

        @Override
        public RentalCompensationRule selectById(Long ruleId)
        {
            return inserted;
        }

        @Override
        public RentalCompensationRule selectApplicable(Integer disputeType)
        {
            requestedDisputeType = disputeType;
            return applicable;
        }

        @Override
        public int insert(RentalCompensationRule rule)
        {
            inserted = rule;
            return 1;
        }

        @Override
        public int update(RentalCompensationRule rule)
        {
            return 1;
        }

        @Override
        public int delete(Long ruleId, Long operator)
        {
            return 1;
        }
    }
}
