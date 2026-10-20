package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 商户服务提交给平台服务的入驻申请资料。 */
public class MerchantRegistrationApplicationCommand implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Integer memberType;
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
    private BigDecimal registeredCapital;
    private String businessScope;
    private String licenseAddress;
    private String licenseValidFrom;
    private String licenseValidTo;
    private Integer licenseLongTerm;
    private String licenseImageUrl;
    private String legalRepresentativeName;
    private String legalRepresentativeIdNo;
    private String legalIdFrontUrl;
    private String legalIdBackUrl;
    private Integer registrationMode;
    private String inviteCode;

    /** @return 商户主体类型，个人或企业 */
    public Integer getMemberType() { return memberType; }

    /** @param memberType 商户主体类型，个人或企业 */
    public void setMemberType(Integer memberType) { this.memberType = memberType; }

    /** @return 店铺展示名称 */
    public String getNickname() { return nickname; }

    /** @param nickname 店铺展示名称 */
    public void setNickname(String nickname) { this.nickname = nickname; }

    /** @return 店铺联系人姓名 */
    public String getContactName() { return contactName; }

    /** @param contactName 店铺联系人姓名 */
    public void setContactName(String contactName) { this.contactName = contactName; }

    /** @return 申请和管理员登录使用的手机号 */
    public String getContactPhone() { return contactPhone; }

    /** @param contactPhone 申请和管理员登录使用的手机号 */
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    /** @return 对外客服电话 */
    public String getServicePhone() { return servicePhone; }

    /** @param servicePhone 对外客服电话 */
    public void setServicePhone(String servicePhone) { this.servicePhone = servicePhone; }

    /** @return 商户联系邮箱 */
    public String getContactEmail() { return contactEmail; }

    /** @param contactEmail 商户联系邮箱 */
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    /** @return 支付宝账户实名 */
    public String getAlipayRealName() { return alipayRealName; }

    /** @param alipayRealName 支付宝账户实名 */
    public void setAlipayRealName(String alipayRealName) { this.alipayRealName = alipayRealName; }

    /** @return 支付宝收款账号 */
    public String getAlipayAccount() { return alipayAccount; }

    /** @param alipayAccount 支付宝收款账号 */
    public void setAlipayAccount(String alipayAccount) { this.alipayAccount = alipayAccount; }

    /** @return 店铺标志文件地址 */
    public String getStoreLogoUrl() { return storeLogoUrl; }

    /** @param storeLogoUrl 店铺标志文件地址 */
    public void setStoreLogoUrl(String storeLogoUrl) { this.storeLogoUrl = storeLogoUrl; }

    /** @return 店铺经营描述 */
    public String getStoreDescription() { return storeDescription; }

    /** @param storeDescription 店铺经营描述 */
    public void setStoreDescription(String storeDescription) { this.storeDescription = storeDescription; }

    /** @return 企业法定主体名称 */
    public String getLegalSubjectName() { return legalSubjectName; }

    /** @param legalSubjectName 企业法定主体名称 */
    public void setLegalSubjectName(String legalSubjectName) { this.legalSubjectName = legalSubjectName; }

    /** @return 营业执照统一社会信用代码 */
    public String getLicenseNo() { return licenseNo; }

    /** @param licenseNo 营业执照统一社会信用代码 */
    public void setLicenseNo(String licenseNo) { this.licenseNo = licenseNo; }

    /** @return 企业注册资本 */
    public BigDecimal getRegisteredCapital() { return registeredCapital; }

    /** @param registeredCapital 企业注册资本 */
    public void setRegisteredCapital(BigDecimal registeredCapital) { this.registeredCapital = registeredCapital; }

    /** @return 营业执照登记的经营范围 */
    public String getBusinessScope() { return businessScope; }

    /** @param businessScope 营业执照登记的经营范围 */
    public void setBusinessScope(String businessScope) { this.businessScope = businessScope; }

    /** @return 营业执照登记地址 */
    public String getLicenseAddress() { return licenseAddress; }

    /** @param licenseAddress 营业执照登记地址 */
    public void setLicenseAddress(String licenseAddress) { this.licenseAddress = licenseAddress; }

    /** @return 营业执照有效期开始日期 */
    public String getLicenseValidFrom() { return licenseValidFrom; }

    /** @param licenseValidFrom 营业执照有效期开始日期 */
    public void setLicenseValidFrom(String licenseValidFrom) { this.licenseValidFrom = licenseValidFrom; }

    /** @return 营业执照有效期结束日期 */
    public String getLicenseValidTo() { return licenseValidTo; }

    /** @param licenseValidTo 营业执照有效期结束日期 */
    public void setLicenseValidTo(String licenseValidTo) { this.licenseValidTo = licenseValidTo; }

    /** @return 营业执照是否长期有效，1 表示长期 */
    public Integer getLicenseLongTerm() { return licenseLongTerm; }

    /** @param licenseLongTerm 营业执照是否长期有效，1 表示长期 */
    public void setLicenseLongTerm(Integer licenseLongTerm) { this.licenseLongTerm = licenseLongTerm; }

    /** @return 营业执照文件地址 */
    public String getLicenseImageUrl() { return licenseImageUrl; }

    /** @param licenseImageUrl 营业执照文件地址 */
    public void setLicenseImageUrl(String licenseImageUrl) { this.licenseImageUrl = licenseImageUrl; }

    /** @return 企业法定代表人姓名 */
    public String getLegalRepresentativeName() { return legalRepresentativeName; }

    /** @param legalRepresentativeName 企业法定代表人姓名 */
    public void setLegalRepresentativeName(String legalRepresentativeName) { this.legalRepresentativeName = legalRepresentativeName; }

    /** @return 企业法定代表人身份证号 */
    public String getLegalRepresentativeIdNo() { return legalRepresentativeIdNo; }

    /** @param legalRepresentativeIdNo 企业法定代表人身份证号 */
    public void setLegalRepresentativeIdNo(String legalRepresentativeIdNo) { this.legalRepresentativeIdNo = legalRepresentativeIdNo; }

    /** @return 法定代表人身份证人像面文件地址 */
    public String getLegalIdFrontUrl() { return legalIdFrontUrl; }

    /** @param legalIdFrontUrl 法定代表人身份证人像面文件地址 */
    public void setLegalIdFrontUrl(String legalIdFrontUrl) { this.legalIdFrontUrl = legalIdFrontUrl; }

    /** @return 法定代表人身份证国徽面文件地址 */
    public String getLegalIdBackUrl() { return legalIdBackUrl; }

    /** @param legalIdBackUrl 法定代表人身份证国徽面文件地址 */
    public void setLegalIdBackUrl(String legalIdBackUrl) { this.legalIdBackUrl = legalIdBackUrl; }

    /** @return 注册方式，paid 表示付费注册，invite 表示邀请码注册 */
    public Integer getRegistrationMode() { return registrationMode; }

    /** @param registrationMode 注册方式 */
    public void setRegistrationMode(Integer registrationMode) { this.registrationMode = registrationMode; }

    /** @return 邀请码注册时提交的邀请码 */
    public String getInviteCode() { return inviteCode; }

    /** @param inviteCode 邀请码 */
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
}
