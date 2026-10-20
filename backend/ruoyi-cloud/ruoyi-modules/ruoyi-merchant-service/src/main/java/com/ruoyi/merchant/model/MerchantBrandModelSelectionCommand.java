package com.ruoyi.merchant.model;

import java.util.List;

/** 商户保存常用型号命令。 */
public class MerchantBrandModelSelectionCommand
{
    /** 客户端提交的标准型号唯一编号集合。 */
    private List<Long> modelIds;

    /** @return 标准型号唯一编号集合 */
    public List<Long> getModelIds()
    {
        return modelIds;
    }

    /** @param modelIds 标准型号唯一编号集合 */
    public void setModelIds(List<Long> modelIds)
    {
        this.modelIds = modelIds;
    }
}
