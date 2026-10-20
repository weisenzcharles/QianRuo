package com.ruoyi.system.mapper.rental;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.rental.RentalCompensationRule;

/** 平台赔付规则模板数据访问组件。 */
public interface RentalCompensationRuleMapper
{
    List<RentalCompensationRule> selectList();
    RentalCompensationRule selectById(@Param("ruleId") Long ruleId);
    RentalCompensationRule selectApplicable(@Param("disputeType") Integer disputeType);
    int insert(RentalCompensationRule rule);
    int update(RentalCompensationRule rule);
    int delete(@Param("ruleId") Long ruleId, @Param("operator") Long operator);
}
