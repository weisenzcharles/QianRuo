package com.ruoyi.system.model.rental;

/** 平台发票申请查询条件。 */
public class PlatformInvoiceApplicationQuery
{
    private String applicationNo;
    private String merchantKeyword;
    private Integer applicationStatus;
    private Integer applicationType;
    private String beginTime;
    private String endTime;

    public String getApplicationNo() { return applicationNo; }
    public void setApplicationNo(String applicationNo) { this.applicationNo = applicationNo; }
    public String getMerchantKeyword() { return merchantKeyword; }
    public void setMerchantKeyword(String merchantKeyword) { this.merchantKeyword = merchantKeyword; }
    public Integer getApplicationStatus() { return applicationStatus; }
    public void setApplicationStatus(Integer applicationStatus) { this.applicationStatus = applicationStatus; }
    public Integer getApplicationType() { return applicationType; }
    public void setApplicationType(Integer applicationType) { this.applicationType = applicationType; }
    public String getBeginTime() { return beginTime; }
    public void setBeginTime(String beginTime) { this.beginTime = beginTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}
