package com.ruoyi.rentalbusiness.domain;

/**
 * 后台订单履约状态推进候选。
 *
 * <p>该对象只承载定时任务判断状态所需的最小字段，不直接作为浏览器接口响应。</p>
 */
public class MerchantOrderLifecycleCandidate
{
    public Long tenantId;
    public Long memberId;
    public Long orderId;
    public String orderNo;
    public Integer orderStatus;
}
