package com.ruoyi.rentalbusiness.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单渠道和渠道转移写入命令。
 *
 * <p>命令不接收 tenantId/memberId 等归属字段，归属信息统一由服务端登录上下文补齐。</p>
 */
public class OrderChannelCommand
{
    public Long channelId;
    public Long packageId;
    public Long fromChannelId;
    public Long toChannelId;
    public String channelName;
    public Integer channelType;
    public String idempotencyKey;
    public String reason;
    public Integer status;
    public Integer sortNo;
    /** 是否默认渠道，取值见 {@code RentalCode.YesNo}。 */
    public Integer isDefault;
    public List<Long> orderIds = new ArrayList<Long>();
}
