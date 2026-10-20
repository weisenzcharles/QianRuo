package com.ruoyi.rentalbusiness.model;

/** 订单转寄命令，只允许修改新的收件人信息，不改变订单履约状态。 */
public class MerchantOrderForwardCommand
{
    public Long orderId;
    public String recipientName;
    public String recipientPhone;
    public String recipientAddress;
    public String remark;
}
