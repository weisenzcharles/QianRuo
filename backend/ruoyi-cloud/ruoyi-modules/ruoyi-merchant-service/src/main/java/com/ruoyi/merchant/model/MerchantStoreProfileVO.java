package com.ruoyi.merchant.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/** 商户店铺设置页面使用的商户主体资料视图。 */
public class MerchantStoreProfileVO
{
    private Integer memberType;
    private Integer membershipType;
    private Integer authStatus;
    private Integer status;
    private String nickname;
    private String contactName;
    private String contactPhone;
    private String servicePhone;
    private String contactEmail;
    private String alipayRealName;
    private String alipayAccount;
    private String storeLogoUrl;
    private String storeDescription;
    private String legalSubjectName;
    private String licenseNo;
    private String registeredCapital;
    private String businessScope;
    private String licenseAddress;
    private String licenseValidFrom;
    private String licenseValidTo;
    private Boolean licenseLongTerm;
    private String legalRepresentativeName;
    private Boolean wechatQrConfigured;
    @JsonIgnore
    private String vipLevel;
    private String vipLevelName;
    private Long storageQuotaMb;
    private Boolean externalAcceptEnabled;

    public Integer getMemberType() { return memberType; }
    public void setMemberType(Integer memberType) { this.memberType = memberType; }
    public Integer getMembershipType() { return membershipType; }
    public void setMembershipType(Integer membershipType) { this.membershipType = membershipType; }
    public Integer getAuthStatus() { return authStatus; }
    public void setAuthStatus(Integer authStatus) { this.authStatus = authStatus; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getServicePhone() { return servicePhone; }
    public void setServicePhone(String servicePhone) { this.servicePhone = servicePhone; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getAlipayRealName() { return alipayRealName; }
    public void setAlipayRealName(String alipayRealName) { this.alipayRealName = alipayRealName; }
    public String getAlipayAccount() { return alipayAccount; }
    public void setAlipayAccount(String alipayAccount) { this.alipayAccount = alipayAccount; }
    public String getStoreLogoUrl() { return storeLogoUrl; }
    public void setStoreLogoUrl(String storeLogoUrl) { this.storeLogoUrl = storeLogoUrl; }
    public String getStoreDescription() { return storeDescription; }
    public void setStoreDescription(String storeDescription) { this.storeDescription = storeDescription; }
    public String getLegalSubjectName() { return legalSubjectName; }
    public void setLegalSubjectName(String legalSubjectName) { this.legalSubjectName = legalSubjectName; }
    public String getLicenseNo() { return licenseNo; }
    public void setLicenseNo(String licenseNo) { this.licenseNo = licenseNo; }
    public String getRegisteredCapital() { return registeredCapital; }
    public void setRegisteredCapital(String registeredCapital) { this.registeredCapital = registeredCapital; }
    public String getBusinessScope() { return businessScope; }
    public void setBusinessScope(String businessScope) { this.businessScope = businessScope; }
    public String getLicenseAddress() { return licenseAddress; }
    public void setLicenseAddress(String licenseAddress) { this.licenseAddress = licenseAddress; }
    public String getLicenseValidFrom() { return licenseValidFrom; }
    public void setLicenseValidFrom(String licenseValidFrom) { this.licenseValidFrom = licenseValidFrom; }
    public String getLicenseValidTo() { return licenseValidTo; }
    public void setLicenseValidTo(String licenseValidTo) { this.licenseValidTo = licenseValidTo; }
    public Boolean getLicenseLongTerm() { return licenseLongTerm; }
    public void setLicenseLongTerm(Boolean licenseLongTerm) { this.licenseLongTerm = licenseLongTerm; }
    public String getLegalRepresentativeName() { return legalRepresentativeName; }
    public void setLegalRepresentativeName(String legalRepresentativeName) { this.legalRepresentativeName = legalRepresentativeName; }
    public Boolean getWechatQrConfigured() { return wechatQrConfigured; }
    public void setWechatQrConfigured(Boolean wechatQrConfigured) { this.wechatQrConfigured = wechatQrConfigured; }
    @JsonIgnore
    public String getVipLevel() { return vipLevel; }
    public void setVipLevel(String vipLevel) { this.vipLevel = vipLevel; }
    public String getVipLevelName() { return vipLevelName; }
    public void setVipLevelName(String vipLevelName) { this.vipLevelName = vipLevelName; }
    public Long getStorageQuotaMb() { return storageQuotaMb; }
    public void setStorageQuotaMb(Long storageQuotaMb) { this.storageQuotaMb = storageQuotaMb; }
    public Boolean getExternalAcceptEnabled() { return externalAcceptEnabled; }
    public void setExternalAcceptEnabled(Boolean externalAcceptEnabled) { this.externalAcceptEnabled = externalAcceptEnabled; }
}
