package com.ruoyi.rentalbusiness.model;

/** 订单渠道及套餐渠道绑定展示对象。 */
public class OrderChannelVO
{
    public Long channelId;
    public Long packageId;
    public String packageName;
    public String channelCode;
    public String channelName;
    public Integer channelType;
    public Integer scopeType;
    public Integer status;
    public Integer sortNo;
    /** 是否默认渠道，取值见 {@code RentalCode.YesNo}。 */
    public Integer isDefault;
}
