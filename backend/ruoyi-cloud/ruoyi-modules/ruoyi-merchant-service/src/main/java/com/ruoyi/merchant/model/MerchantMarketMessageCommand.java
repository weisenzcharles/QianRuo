package com.ruoyi.merchant.model;

/** 发送周转市场私信命令。 */
public class MerchantMarketMessageCommand
{
    private String postNo;
    private String content;

    public String getPostNo() { return postNo; }
    public void setPostNo(String postNo) { this.postNo = postNo; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
