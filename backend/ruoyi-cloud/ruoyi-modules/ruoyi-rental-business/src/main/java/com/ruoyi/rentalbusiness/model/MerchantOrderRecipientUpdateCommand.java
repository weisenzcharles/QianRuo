package com.ruoyi.rentalbusiness.model;

/** 当前订单客户和收件人信息更新命令，只允许修改订单快照字段。 */
public class MerchantOrderRecipientUpdateCommand
{
    public Long orderId;
    public String customerName;
    public String customerPhone;
    /** 买家昵称快照，可为空。 */
    public String customerNickname;
    public String recipientName;
    public String recipientPhone;
    public String recipientAddress;
    public String remark;
}
