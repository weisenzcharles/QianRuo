package com.ruoyi.merchant.model;

/** 商户可见的周转市场私信视图。messageId 仅用于已读接口参数，不直接作为页面文案展示。 */
public class MerchantMarketMessageVO
{
    private Long messageId;
    private String postNo;
    private String fromMerchantName;
    private String toMerchantName;
    private String messageContent;
    private Integer readFlag;
    private String readFlagName;
    private Integer riskFlag;
    private String createTime;

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public String getPostNo() { return postNo; }
    public void setPostNo(String postNo) { this.postNo = postNo; }
    public String getFromMerchantName() { return fromMerchantName; }
    public void setFromMerchantName(String fromMerchantName) { this.fromMerchantName = fromMerchantName; }
    public String getToMerchantName() { return toMerchantName; }
    public void setToMerchantName(String toMerchantName) { this.toMerchantName = toMerchantName; }
    public String getMessageContent() { return messageContent; }
    public void setMessageContent(String messageContent) { this.messageContent = messageContent; }
    public Integer getReadFlag() { return readFlag; }
    public void setReadFlag(Integer readFlag) { this.readFlag = readFlag; }
    public String getReadFlagName() { return readFlagName; }
    public void setReadFlagName(String readFlagName) { this.readFlagName = readFlagName; }
    public Integer getRiskFlag() { return riskFlag; }
    public void setRiskFlag(Integer riskFlag) { this.riskFlag = riskFlag; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
