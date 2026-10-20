package com.ruoyi.rentalbusiness.model;

/** 商户订单中心顶部概览统计。统计范围只受当前商户可访问仓库限制。 */
public class MerchantOrderSummaryVO
{
    private long total;
    private long pendingShip;
    private long shipping;
    private long attention;

    public long getTotal()
    {
        return total;
    }

    public void setTotal(long total)
    {
        this.total = total;
    }

    public long getPendingShip()
    {
        return pendingShip;
    }

    public void setPendingShip(long pendingShip)
    {
        this.pendingShip = pendingShip;
    }

    public long getShipping()
    {
        return shipping;
    }

    public void setShipping(long shipping)
    {
        this.shipping = shipping;
    }

    public long getAttention()
    {
        return attention;
    }

    public void setAttention(long attention)
    {
        this.attention = attention;
    }
}
