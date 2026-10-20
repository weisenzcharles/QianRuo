package com.ruoyi.merchant.model;

/** 商户内部通知管理查询条件。 */
public class MerchantInternalNoticeQuery
{
    private String noticeTitle;
    private Integer noticeType;
    private Integer targetScope;
    private Integer status;

    public String getNoticeTitle() { return noticeTitle; }
    public void setNoticeTitle(String noticeTitle) { this.noticeTitle = noticeTitle; }
    public Integer getNoticeType() { return noticeType; }
    public void setNoticeType(Integer noticeType) { this.noticeType = noticeType; }
    public Integer getTargetScope() { return targetScope; }
    public void setTargetScope(Integer targetScope) { this.targetScope = targetScope; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
