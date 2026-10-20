package com.ruoyi.system.model.rental;

/** 平台财务查询条件。商户筛选使用业务编码、名称或主体名称，不向页面暴露内部编号。 */
public class PlatformFinanceQuery
{
    private String merchantKeyword;
    private String ledgerNo;
    private Integer accountType;
    private String paymentNo;
    private String bizType;
    private Integer direction;
    private Integer ledgerStatus;
    private Integer invoiceStatus;
    private Integer paymentStatus;
    private String withdrawalNo;
    private Integer withdrawalStatus;
    private String beginTime;
    private String endTime;
    private String scene;

    public String getMerchantKeyword()
    {
        return merchantKeyword;
    }

    public void setMerchantKeyword(String merchantKeyword)
    {
        this.merchantKeyword = merchantKeyword;
    }

    public String getLedgerNo()
    {
        return ledgerNo;
    }

    public void setLedgerNo(String ledgerNo)
    {
        this.ledgerNo = ledgerNo;
    }

    public Integer getAccountType()
    {
        return accountType;
    }

    public void setAccountType(Integer accountType)
    {
        this.accountType = accountType;
    }

    public String getPaymentNo()
    {
        return paymentNo;
    }

    public void setPaymentNo(String paymentNo)
    {
        this.paymentNo = paymentNo;
    }

    public String getBizType()
    {
        return bizType;
    }

    public void setBizType(String bizType)
    {
        this.bizType = bizType;
    }

    public Integer getDirection()
    {
        return direction;
    }

    public void setDirection(Integer direction)
    {
        this.direction = direction;
    }

    public Integer getLedgerStatus()
    {
        return ledgerStatus;
    }

    public void setLedgerStatus(Integer ledgerStatus)
    {
        this.ledgerStatus = ledgerStatus;
    }

    public Integer getInvoiceStatus()
    {
        return invoiceStatus;
    }

    public void setInvoiceStatus(Integer invoiceStatus)
    {
        this.invoiceStatus = invoiceStatus;
    }

    public Integer getPaymentStatus()
    {
        return paymentStatus;
    }

    public void setPaymentStatus(Integer paymentStatus)
    {
        this.paymentStatus = paymentStatus;
    }

    public String getWithdrawalNo() { return withdrawalNo; }
    public void setWithdrawalNo(String withdrawalNo) { this.withdrawalNo = withdrawalNo; }
    public Integer getWithdrawalStatus() { return withdrawalStatus; }
    public void setWithdrawalStatus(Integer withdrawalStatus) { this.withdrawalStatus = withdrawalStatus; }

    public String getBeginTime()
    {
        return beginTime;
    }

    public void setBeginTime(String beginTime)
    {
        this.beginTime = beginTime;
    }

    public String getEndTime()
    {
        return endTime;
    }

    public void setEndTime(String endTime)
    {
        this.endTime = endTime;
    }

    public String getScene()
    {
        return scene;
    }

    public void setScene(String scene)
    {
        this.scene = scene;
    }
}
