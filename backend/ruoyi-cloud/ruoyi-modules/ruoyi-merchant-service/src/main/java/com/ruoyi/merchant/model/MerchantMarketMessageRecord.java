package com.ruoyi.merchant.model;

/** 周转市场私信内部记录。 */
public class MerchantMarketMessageRecord
{
    private Long messageId;
    private String postNo;
    private String fromMerchantName;
    private String toMerchantName;
    private String messageContent;
    private Integer readFlag;
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
    public Integer getRiskFlag() { return riskFlag; }
    public void setRiskFlag(Integer riskFlag) { this.riskFlag = riskFlag; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
