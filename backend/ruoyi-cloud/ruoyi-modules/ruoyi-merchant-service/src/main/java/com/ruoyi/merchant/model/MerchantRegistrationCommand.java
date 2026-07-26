package com.ruoyi.merchant.model;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 商户公开入驻申请。归属、审核和账号字段均由服务端控制。 */
public class MerchantRegistrationCommand
{
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{4}$", message = "请输入四位数字验证码")
    private String code;

    @NotBlank(message = "商户类型不能为空")
    private String memberType;

    @NotBlank(message = "店铺名称不能为空")
    @Size(max = 64, message = "店铺名称长度不能超过64个字符")
    private String nickname;

    @NotBlank(message = "联系人不能为空")
    @Size(max = 64, message = "联系人长度不能超过64个字符")
    private String contactName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactPhone;

    @Size(max = 32, message = "客服电话长度不能超过32个字符")
    private String servicePhone;

    @Email(message = "联系邮箱格式不正确")
    @Size(max = 128, message = "联系邮箱长度不能超过128个字符")
    private String contactEmail;

    @Size(max = 64, message = "支付宝实名长度不能超过64个字符")
    private String alipayRealName;

    @Size(max = 128, message = "支付宝账号长度不能超过128个字符")
    private String alipayAccount;

    @Size(max = 500, message = "店铺Logo地址长度不能超过500个字符")
    private String storeLogoUrl;

    @Size(max = 1000, message = "店铺描述长度不能超过1000个字符")
    private String storeDescription;

    @Size(max = 128, message = "企业名称长度不能超过128个字符")
    private String legalSubjectName;

    @Size(max = 64, message = "营业执照号长度不能超过64个字符")
    private String licenseNo;

    @DecimalMin(value = "0.00", message = "注册资本不能小于0")
    private BigDecimal registeredCapital;

    @Size(max = 2000, message = "经营范围长度不能超过2000个字符")
    private String businessScope;

    @Size(max = 500, message = "营业执照地址长度不能超过500个字符")
    private String licenseAddress;

    private String licenseValidFrom;
    private String licenseValidTo;
    private Integer licenseLongTerm;

    @Size(max = 500, message = "营业执照图片地址长度不能超过500个字符")
    private String licenseImageUrl;

    @Size(max = 64, message = "法人姓名长度不能超过64个字符")
    private String legalRepresentativeName;

    @Size(max = 64, message = "法人身份证号长度不能超过64个字符")
    private String legalRepresentativeIdNo;

    @Size(max = 500, message = "法人身份证人像面地址长度不能超过500个字符")
    private String legalIdFrontUrl;

    @Size(max = 500, message = "法人身份证国徽面地址长度不能超过500个字符")
    private String legalIdBackUrl;

    /** @return 当前用于提交或重新提交申请的四位验证码 */
    public String getCode() { return code; }

    /** @param code 当前用于提交或重新提交申请的四位验证码 */
    public void setCode(String code) { this.code = code; }

    /** @return 商户主体类型，个人或企业 */
    public String getMemberType() { return memberType; }

    /** @param memberType 商户主体类型，个人或企业 */
    public void setMemberType(String memberType) { this.memberType = memberType; }

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
}
