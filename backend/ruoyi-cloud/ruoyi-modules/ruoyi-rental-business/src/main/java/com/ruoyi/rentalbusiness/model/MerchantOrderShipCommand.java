package com.ruoyi.rentalbusiness.model;

/** 订单发货动作命令，同时保存发货方向的快递信息。 */
public class MerchantOrderShipCommand
{
    public String carrier;
    public String trackingNo;
    public String remark;
}
