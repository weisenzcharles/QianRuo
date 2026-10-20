package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.SysCodeRule;

/** 平台统一业务编码规则数据访问接口。 */
public interface SysCodeRuleMapper
{
    /**
     * 查询全部未删除编码规则。
     *
     * @return 编码规则列表
     */
    List<SysCodeRule> selectList();

    /**
     * 按编码类型查询未删除规则。
     *
     * @param codeType 编码类型键
     * @return 编码规则，不存在时返回空
     */
    SysCodeRule selectByCodeType(@Param("codeType") String codeType);

    /**
     * 在当前事务内锁定编码规则行，保证多实例并发分配不会得到相同序号。
     *
     * @param codeType 编码类型键
     * @return 已锁定的编码规则
     */
    SysCodeRule selectByCodeTypeForUpdate(@Param("codeType") String codeType);

    /**
     * 查询已分配的最大商户编码序号，包含软删除商户，因为编码唯一键不会释放。
     *
     * @param prefix 商户编码前缀
     * @return 最大数字序号；没有匹配编码时返回0
     */
    Long selectMaxMemberCodeSequence(@Param("prefix") String prefix);

    /**
     * 更新已分配的最大序号。
     *
     * @param ruleId 编码规则唯一编号
     * @param currentValue 新的最大序号
     * @return 受影响行数
     */
    int updateCurrentValue(@Param("ruleId") Long ruleId, @Param("currentValue") Long currentValue);

    /**
     * 修改编码规则的前缀、长度、状态和备注。
     *
     * @param rule 待修改规则
     * @return 受影响行数
     */
    int updateRule(SysCodeRule rule);
}
