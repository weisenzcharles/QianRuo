package com.ruoyi.merchant.model;

/** 商户店铺基础资料修改参数。认证主体字段由平台审核流程维护，不在此接口修改。 */
public class MerchantStoreProfileUpdateCommand
{
    private String nickname;
    private String contactName;
    private String servicePhone;
    private String contactEmail;
    private String alipayRealName;
    private String alipayAccount;
    private String storeDescription;
    private Boolean externalAcceptEnabled;

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getServicePhone() { return servicePhone; }
    public void setServicePhone(String servicePhone) { this.servicePhone = servicePhone; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getAlipayRealName() { return alipayRealName; }
    public void setAlipayRealName(String alipayRealName) { this.alipayRealName = alipayRealName; }
    public String getAlipayAccount() { return alipayAccount; }
    public void setAlipayAccount(String alipayAccount) { this.alipayAccount = alipayAccount; }
    public String getStoreDescription() { return storeDescription; }
    public void setStoreDescription(String storeDescription) { this.storeDescription = storeDescription; }
    public Boolean getExternalAcceptEnabled() { return externalAcceptEnabled; }
    public void setExternalAcceptEnabled(Boolean externalAcceptEnabled) { this.externalAcceptEnabled = externalAcceptEnabled; }
}
