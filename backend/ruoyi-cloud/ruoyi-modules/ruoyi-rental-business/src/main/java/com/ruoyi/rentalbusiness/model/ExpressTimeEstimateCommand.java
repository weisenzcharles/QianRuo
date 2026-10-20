package com.ruoyi.rentalbusiness.model;

/** 商户端快递时效预估查询参数。地址只用于本次解析，不会原样写入缓存或数据库。 */
public class ExpressTimeEstimateCommand
{
    /** 寄件详细地址。 */
    private String fromAddress;

    /** 收件详细地址。 */
    private String toAddress;

    /** 平台统一快递公司编码。 */
    private String expressCompanyCode;

    /** 可选的快递产品类型。 */
    private String expressType;

    /** 可选的下单时间，格式为 yyyy-MM-dd HH:mm:ss。 */
    private String orderTime;

    public String getFromAddress()
    {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress)
    {
        this.fromAddress = fromAddress;
    }

    public String getToAddress()
    {
        return toAddress;
    }

    public void setToAddress(String toAddress)
    {
        this.toAddress = toAddress;
    }

    public String getExpressCompanyCode()
    {
        return expressCompanyCode;
    }

    public void setExpressCompanyCode(String expressCompanyCode)
    {
        this.expressCompanyCode = expressCompanyCode;
    }

    public String getExpressType()
    {
        return expressType;
    }

    public void setExpressType(String expressType)
    {
        this.expressType = expressType;
    }

    public String getOrderTime()
    {
        return orderTime;
    }

    public void setOrderTime(String orderTime)
    {
        this.orderTime = orderTime;
    }
}
