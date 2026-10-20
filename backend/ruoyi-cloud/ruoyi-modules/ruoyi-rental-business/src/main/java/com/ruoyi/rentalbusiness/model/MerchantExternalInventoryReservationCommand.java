package com.ruoyi.rentalbusiness.model;

import java.time.LocalDateTime;

/** 外部平台库存预占命令。令牌和商户归属由服务端上下文补齐。 */
public class MerchantExternalInventoryReservationCommand
{
    public String externalOrderNo;
    public Long packageId;
    public Long productId;
    public Long warehouseId;
    public Integer quantity;
    public LocalDateTime expireTime;
    public String remark;

    /** 生成跨客户端稳定的签名原文，避免依赖 JSON 字段顺序。 */
    public String canonicalValue()
    {
        return join(externalOrderNo, packageId, productId, warehouseId, quantity, expireTime, remark);
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
