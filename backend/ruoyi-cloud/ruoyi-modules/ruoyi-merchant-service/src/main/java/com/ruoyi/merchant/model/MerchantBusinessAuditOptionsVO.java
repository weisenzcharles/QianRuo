package com.ruoyi.merchant.model;

import java.util.List;

/**
 * 商户业务审计筛选项。
 *
 * <p>筛选项直接来自当前商户的业务审计数据，确保下拉内容与日志列表使用同一数据范围。</p>
 */
public class MerchantBusinessAuditOptionsVO
{
    private List<String> businessModules;
    private List<String> operatorNames;

    /**
     * 获取当前商户已经产生业务日志的模块名称。
     *
     * @return 业务模块名称列表
     */
    public List<String> getBusinessModules()
    {
        return businessModules;
    }

    /**
     * 设置当前商户已经产生业务日志的模块名称。
     *
     * @param businessModules 业务模块名称列表
     */
    public void setBusinessModules(List<String> businessModules)
    {
        this.businessModules = businessModules;
    }

    /**
     * 获取当前商户日志中的操作人昵称。
     *
     * @return 操作人昵称列表
     */
    public List<String> getOperatorNames()
    {
        return operatorNames;
    }

    /**
     * 设置当前商户日志中的操作人昵称。
     *
     * @param operatorNames 操作人昵称列表
     */
    public void setOperatorNames(List<String> operatorNames)
    {
        this.operatorNames = operatorNames;
    }
}
