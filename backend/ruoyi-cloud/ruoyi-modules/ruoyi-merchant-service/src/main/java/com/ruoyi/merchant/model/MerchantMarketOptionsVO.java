package com.ruoyi.merchant.model;

import java.util.List;

/** 周转市场发帖所需的受控选项集合。 */
public class MerchantMarketOptionsVO
{
    private List<MerchantMarketModelOption> models;
    private List<MerchantMarketSelectOption> conditionLevels;
    private List<MerchantMarketSelectOption> priceRanges;

    public List<MerchantMarketModelOption> getModels()
    {
        return models;
    }

    public void setModels(List<MerchantMarketModelOption> models)
    {
        this.models = models;
    }

    public List<MerchantMarketSelectOption> getConditionLevels()
    {
        return conditionLevels;
    }

    public void setConditionLevels(List<MerchantMarketSelectOption> conditionLevels)
    {
        this.conditionLevels = conditionLevels;
    }

    public List<MerchantMarketSelectOption> getPriceRanges()
    {
        return priceRanges;
    }

    public void setPriceRanges(List<MerchantMarketSelectOption> priceRanges)
    {
        this.priceRanges = priceRanges;
    }
}
