package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;

/**
 * 外部平台正式订单创建命令。
 *
 * <p>库存相关字段不由调用方提交，服务端会从已预占的库存记录中恢复，避免外部请求绕过库存锁。</p>
 */
public class MerchantExternalOrderCreateCommand
{
    /** 外部平台订单标识，同一令牌下用于幂等创建。 */
    public String externalOrderNo;
    public String reservationNo;
    public Long channelId;
    public String shopName;
    public String customerName;
    public String customerNickname;
    public String customerPhone;
    public String recipientName;
    public String recipientPhone;
    public String recipientAddress;
    public Long returnWarehouseId;
    public String leaseStartDate;
    public String leaseEndDate;
    public String shipDueTime;
    public String shipTimeSlot;
    public String returnDueTime;
    public String deliveryMethod;
    public String returnDeliveryMethod;
    public BigDecimal deliveryAmount;
    public BigDecimal insuranceAmount;
    public BigDecimal rentAmount;
    public BigDecimal depositAmount;
    public String remark;

    /** 生成与 JSON 字段顺序无关的固定签名原文。 */
    public String canonicalValue()
    {
        return join(externalOrderNo, reservationNo, channelId, shopName, customerName, customerNickname, customerPhone,
                recipientName, recipientPhone, recipientAddress, returnWarehouseId, leaseStartDate,
                leaseEndDate, shipDueTime, shipTimeSlot, returnDueTime, deliveryMethod, returnDeliveryMethod,
                deliveryAmount, insuranceAmount, rentAmount, depositAmount, remark);
    }

    private String join(Object... values)
    {
        StringBuilder builder = new StringBuilder();
        for (Object value : values)
        {
            if (builder.length() > 0)
            {
                builder.append('\n');
            }
            builder.append(value == null ? "" : String.valueOf(value).trim());
        }
        return builder.toString();
    }
}
