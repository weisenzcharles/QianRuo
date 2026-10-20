package com.ruoyi.rentalbusiness.model;

/** 新架构商户套餐明细命令。 */
public class MerchantPackageItemCommand
{
    public Integer itemRole;
    public Long productId;
    public Long accessoryId;
    public Long accessoryItemProductId;
    public Long merchantAccessoryId;
    public Integer quantity = 1;
    public Integer requiredFlag = 1;
    public Integer sortNo = 0;
}
