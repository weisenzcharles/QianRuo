package com.ruoyi.system.service.rental;

import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.system.domain.rental.RentalCompensationRule;
import com.ruoyi.system.mapper.rental.RentalCompensationRuleMapper;
import com.ruoyi.system.model.rental.PlatformCompensationRuleCommand;

/** 平台联盟赔付规则模板服务。 */
@Service
public class RentalCompensationRuleService
{
    private final RentalCompensationRuleMapper mapper;
    public RentalCompensationRuleService(RentalCompensationRuleMapper mapper) { this.mapper = mapper; }
    public List<RentalCompensationRule> list() { return mapper.selectList(); }
    public RentalCompensationRule get(Long ruleId) {
        RentalCompensationRule rule = mapper.selectById(ruleId);
        if (rule == null) throw new ServiceException("赔付规则不存在或已删除");
        return rule;
    }
    public RentalCompensationRule applicable(Integer disputeType) { return mapper.selectApplicable(disputeType); }
    public int add(PlatformCompensationRuleCommand command, Long operator) {
        RentalCompensationRule rule = toEntity(command);
        rule.setCreateBy(operator);
        return mapper.insert(rule);
    }
    public int update(PlatformCompensationRuleCommand command, Long operator) {
        get(command.getRuleId());
        RentalCompensationRule rule = toEntity(command);
        rule.setUpdateBy(operator);
        return mapper.update(rule);
    }
    public int delete(Long ruleId, Long operator) { get(ruleId); return mapper.delete(ruleId, operator); }
    private RentalCompensationRule toEntity(PlatformCompensationRuleCommand command) {
        if (command == null) throw new ServiceException("赔付规则不能为空");
        if (RentalCode.AllianceDisputeType.fromCode(command.getDisputeType()) == null)
            throw new ServiceException("纠纷类型不合法");
        RentalCompensationRule rule = new RentalCompensationRule();
        rule.setRuleId(command.getRuleId()); rule.setRuleName(StringUtils.trim(command.getRuleName()));
        rule.setDisputeType(command.getDisputeType()); rule.setMaxAmount(command.getMaxAmount());
        rule.setPriority(command.getPriority() == null ? 100 : command.getPriority());
        rule.setStatus(command.getStatus() == null ? RentalCode.EnableStatus.ENABLED.getCode() : command.getStatus());
        rule.setRemark(StringUtils.trim(command.getRemark()));
        return rule;
    }
}
