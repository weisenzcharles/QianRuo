package com.ruoyi.rentalbusiness.model;

/**
 * 商户订单物流信息更新命令。
 *
 * <p>该命令只维护物流记录，不参与订单状态流转，避免编辑寄出或归还单号时覆盖订单其他字段。</p>
 */
public class MerchantOrderLogisticsUpdateCommand
{
    /** 路径中的订单唯一编号，仅由 Controller 绑定。 */
    public Long orderId;

    /** 物流方向编码，只允许发货或归还。 */
    public Integer direction;

    /** 平台字典配置的快递公司编码。 */
    public String carrier;

    /** 快递单号。 */
    public String trackingNo;
}
