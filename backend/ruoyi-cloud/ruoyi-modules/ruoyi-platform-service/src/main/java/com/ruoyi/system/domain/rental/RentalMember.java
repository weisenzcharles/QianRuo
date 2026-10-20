package com.ruoyi.system.domain.rental;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.annotation.Excel;
import com.ruoyi.common.core.annotation.Excel.ColumnType;
import com.ruoyi.common.core.web.domain.BaseEntity;

/** 租赁商户/会员主体 ry_rental_member。 */
public class RentalMember extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "商户唯一编号", cellType = ColumnType.NUMERIC)
    private Long memberId;

    @Excel(name = "租户唯一编号", cellType = ColumnType.NUMERIC)
    private Long tenantId;

    @Excel(name = "商户编码")
    private String memberCode;

    private String mainAccount;

    @Excel(name = "商户类型", readConverterExp = "1=个人,2=企业")
    private Integer memberType;

    @Excel(name = "会员类型", readConverterExp = "1=个人会员,2=普通企业会员,3=入驻会员")
    private Integer membershipType;

    @Excel(name = "认证状态", readConverterExp = "0=未认证,1=待支付,2=待审核,3=已认证,4=已驳回")
    private Integer authStatus;

    @Excel(name = "主体名称")
    private String legalSubjectName;

    @Excel(name = "营业执照号")
    private String licenseNo;

    @Excel(name = "店铺名称")
    private String nickname;

    @Excel(name = "联系人")
    private String contactName;

    @Excel(name = "联系电话")
    private String contactPhone;

    @Excel(name = "客服电话")
    private String servicePhone;

    @Excel(name = "联系邮箱")
    private String contactEmail;

    private String alipayRealName;

    private String alipayAccount;

    private String storeLogoUrl;
    private String storeDescription;

    @Excel(name = "注册资本")
    private BigDecimal registeredCapital;

    private String businessScope;
    private String licenseAddress;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "执照有效期开始", width = 20, dateFormat = "yyyy-MM-dd")
    private Date licenseValidFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "执照有效期结束", width = 20, dateFormat = "yyyy-MM-dd")
    private Date licenseValidTo;

    private Integer licenseLongTerm;
    private String licenseImageUrl;

    @Excel(name = "法人姓名")
    private String legalRepresentativeName;

    private String legalRepresentativeIdNo;

    private String legalIdFrontUrl;
    private String legalIdBackUrl;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "签约日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date signingDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "合同有效期开始", width = 20, dateFormat = "yyyy-MM-dd")
    private Date contractValidFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "合同有效期结束", width = 20, dateFormat = "yyyy-MM-dd")
    private Date contractValidTo;

    private String contractFileUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    private String auditRemark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "冻结时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date freezeTime;

    private String freezeReason;
    private Long wechatQrFileId;

    @Excel(name = "会员等级")
    private String vipLevel;
    private Long levelId;
    private Integer registrationMode;
    private Integer registrationStatus;
    private Long inviterMemberId;

    @Excel(name = "存储额度MB", cellType = ColumnType.NUMERIC)
    private Long storageQuotaMb;

    @Excel(name = "允许对外接单", readConverterExp = "0=否,1=是")
    private Integer externalAcceptEnabled;

    @Excel(name = "冻结状态", readConverterExp = "0=未冻结,1=已冻结")
    private Integer status;

    private Integer isDelete;

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    /**
     * 获取商户编码。
     *
     * <p>商户编码由平台统一编码规则服务在新增时生成，公开注册端和平台管理端都不再
     * 要求客户端提交该字段；保留长度校验是为了保护数据库边界和历史数据回显。</p>
     *
     * @return 服务端生成的商户编码
     */
    @Size(max = 64, message = "商户编码长度不能超过64个字符")
    public String getMemberCode() { return memberCode; }
    public void setMemberCode(String memberCode) { this.memberCode = memberCode; }
    public String getMainAccount() { return mainAccount; }
    public void setMainAccount(String mainAccount) { this.mainAccount = mainAccount; }

    @NotNull(message = "商户类型不能为空")
    public Integer getMemberType() { return memberType; }
    public void setMemberType(Integer memberType) { this.memberType = memberType; }

    /**
     * 获取商户当前会员类型。该字段是当前有效快照，历史变更保存在会员类型历史表。
     *
     * @return 当前会员类型编码
     */
    public Integer getMembershipType() { return membershipType; }

    /**
     * 设置商户当前会员类型。调用方必须同时由服务层写入会员类型变更历史。
     *
     * @param membershipType 当前会员类型编码
     */
    public void setMembershipType(Integer membershipType) { this.membershipType = membershipType; }

    @NotNull(message = "认证状态不能为空")
    public Integer getAuthStatus() { return authStatus; }
    public void setAuthStatus(Integer authStatus) { this.authStatus = authStatus; }

    @Size(max = 128, message = "主体名称长度不能超过128个字符")
    public String getLegalSubjectName() { return legalSubjectName; }
    public void setLegalSubjectName(String legalSubjectName) { this.legalSubjectName = legalSubjectName; }

    @Size(max = 64, message = "营业执照号长度不能超过64个字符")
    public String getLicenseNo() { return licenseNo; }
    public void setLicenseNo(String licenseNo) { this.licenseNo = licenseNo; }

    @NotBlank(message = "店铺名称不能为空")
    @Size(max = 64, message = "店铺名称长度不能超过64个字符")
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    @Size(max = 64, message = "联系人长度不能超过64个字符")
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    @NotBlank(message = "联系电话不能为空，用于开通商户管理员账号")
    @Size(max = 32, message = "联系电话长度不能超过32个字符")
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    @Size(max = 32, message = "客服电话长度不能超过32个字符")
    public String getServicePhone() { return servicePhone; }
    public void setServicePhone(String servicePhone) { this.servicePhone = servicePhone; }

    @Email(message = "联系邮箱格式不正确")
    @Size(max = 128, message = "联系邮箱长度不能超过128个字符")
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    @Size(max = 64, message = "支付宝实名长度不能超过64个字符")
    public String getAlipayRealName() { return alipayRealName; }
    public void setAlipayRealName(String alipayRealName) { this.alipayRealName = alipayRealName; }

    @Size(max = 128, message = "支付宝账号长度不能超过128个字符")
    public String getAlipayAccount() { return alipayAccount; }
    public void setAlipayAccount(String alipayAccount) { this.alipayAccount = alipayAccount; }

    @Size(max = 500, message = "店铺logo地址长度不能超过500个字符")
    public String getStoreLogoUrl() { return storeLogoUrl; }
    public void setStoreLogoUrl(String storeLogoUrl) { this.storeLogoUrl = storeLogoUrl; }

    @Size(max = 1000, message = "店铺描述长度不能超过1000个字符")
    public String getStoreDescription() { return storeDescription; }
    public void setStoreDescription(String storeDescription) { this.storeDescription = storeDescription; }

    @DecimalMin(value = "0.00", message = "注册资本不能小于0")
    public BigDecimal getRegisteredCapital() { return registeredCapital; }
    public void setRegisteredCapital(BigDecimal registeredCapital) { this.registeredCapital = registeredCapital; }

    @Size(max = 2000, message = "经营范围长度不能超过2000个字符")
    public String getBusinessScope() { return businessScope; }
    public void setBusinessScope(String businessScope) { this.businessScope = businessScope; }

    @Size(max = 500, message = "营业执照地址长度不能超过500个字符")
    public String getLicenseAddress() { return licenseAddress; }
    public void setLicenseAddress(String licenseAddress) { this.licenseAddress = licenseAddress; }
    public Date getLicenseValidFrom() { return licenseValidFrom; }
    public void setLicenseValidFrom(Date licenseValidFrom) { this.licenseValidFrom = licenseValidFrom; }
    public Date getLicenseValidTo() { return licenseValidTo; }
    public void setLicenseValidTo(Date licenseValidTo) { this.licenseValidTo = licenseValidTo; }
    public Integer getLicenseLongTerm() { return licenseLongTerm; }
    public void setLicenseLongTerm(Integer licenseLongTerm) { this.licenseLongTerm = licenseLongTerm; }

    @Size(max = 500, message = "营业执照图片地址长度不能超过500个字符")
    public String getLicenseImageUrl() { return licenseImageUrl; }
    public void setLicenseImageUrl(String licenseImageUrl) { this.licenseImageUrl = licenseImageUrl; }

    @Size(max = 64, message = "法人姓名长度不能超过64个字符")
    public String getLegalRepresentativeName() { return legalRepresentativeName; }
    public void setLegalRepresentativeName(String legalRepresentativeName) { this.legalRepresentativeName = legalRepresentativeName; }

    @Size(max = 64, message = "法人身份证号长度不能超过64个字符")
    public String getLegalRepresentativeIdNo() { return legalRepresentativeIdNo; }
    public void setLegalRepresentativeIdNo(String legalRepresentativeIdNo) { this.legalRepresentativeIdNo = legalRepresentativeIdNo; }

    @Size(max = 500, message = "法人身份证人像面地址长度不能超过500个字符")
    public String getLegalIdFrontUrl() { return legalIdFrontUrl; }
    public void setLegalIdFrontUrl(String legalIdFrontUrl) { this.legalIdFrontUrl = legalIdFrontUrl; }

    @Size(max = 500, message = "法人身份证国徽面地址长度不能超过500个字符")
    public String getLegalIdBackUrl() { return legalIdBackUrl; }
    public void setLegalIdBackUrl(String legalIdBackUrl) { this.legalIdBackUrl = legalIdBackUrl; }

    public Date getSigningDate() { return signingDate; }
    public void setSigningDate(Date signingDate) { this.signingDate = signingDate; }
    public Date getContractValidFrom() { return contractValidFrom; }
    public void setContractValidFrom(Date contractValidFrom) { this.contractValidFrom = contractValidFrom; }
    public Date getContractValidTo() { return contractValidTo; }
    public void setContractValidTo(Date contractValidTo) { this.contractValidTo = contractValidTo; }

    @Size(max = 500, message = "签约合同地址长度不能超过500个字符")
    public String getContractFileUrl() { return contractFileUrl; }
    public void setContractFileUrl(String contractFileUrl) { this.contractFileUrl = contractFileUrl; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }

    @Size(max = 500, message = "审核备注长度不能超过500个字符")
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public Date getFreezeTime() { return freezeTime; }
    public void setFreezeTime(Date freezeTime) { this.freezeTime = freezeTime; }

    @Size(max = 500, message = "冻结原因长度不能超过500个字符")
    public String getFreezeReason() { return freezeReason; }
    public void setFreezeReason(String freezeReason) { this.freezeReason = freezeReason; }
    public Long getWechatQrFileId() { return wechatQrFileId; }
    public void setWechatQrFileId(Long wechatQrFileId) { this.wechatQrFileId = wechatQrFileId; }

    @NotBlank(message = "会员等级不能为空")
    public String getVipLevel() { return vipLevel; }
    public void setVipLevel(String vipLevel) { this.vipLevel = vipLevel; }
    /** @return 商户等级唯一编号 */
    public Long getLevelId() { return levelId; }
    /** @param levelId 商户等级唯一编号 */
    public void setLevelId(Long levelId) { this.levelId = levelId; }
    /** @return 注册方式 */
    public Integer getRegistrationMode() { return registrationMode; }
    /** @param registrationMode 注册方式 */
    public void setRegistrationMode(Integer registrationMode) { this.registrationMode = registrationMode; }
    /** @return 注册完成状态 */
    public Integer getRegistrationStatus() { return registrationStatus; }
    /** @param registrationStatus 注册完成状态 */
    public void setRegistrationStatus(Integer registrationStatus) { this.registrationStatus = registrationStatus; }
    /** @return 邀请商户唯一编号 */
    public Long getInviterMemberId() { return inviterMemberId; }
    /** @param inviterMemberId 邀请商户唯一编号 */
    public void setInviterMemberId(Long inviterMemberId) { this.inviterMemberId = inviterMemberId; }

    @NotNull(message = "存储额度不能为空")
    @Min(value = 0, message = "存储额度不能小于0")
    public Long getStorageQuotaMb() { return storageQuotaMb; }
    public void setStorageQuotaMb(Long storageQuotaMb) { this.storageQuotaMb = storageQuotaMb; }

    @NotNull(message = "对外接单状态不能为空")
    public Integer getExternalAcceptEnabled() { return externalAcceptEnabled; }
    public void setExternalAcceptEnabled(Integer externalAcceptEnabled) { this.externalAcceptEnabled = externalAcceptEnabled; }

    @NotNull(message = "冻结状态不能为空")
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getIsDelete() { return isDelete; }
    public void setIsDelete(Integer isDelete) { this.isDelete = isDelete; }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("memberId", memberId).append("tenantId", tenantId)
            .append("memberCode", memberCode).append("mainAccount", mainAccount)
            .append("memberType", memberType).append("membershipType", membershipType)
            .append("authStatus", authStatus)
            .append("legalSubjectName", legalSubjectName).append("licenseNo", licenseNo)
            .append("nickname", nickname).append("contactName", contactName)
            .append("storeDescription", storeDescription).append("registeredCapital", registeredCapital)
            .append("businessScope", businessScope).append("licenseAddress", licenseAddress)
            .append("licenseValidFrom", licenseValidFrom).append("licenseValidTo", licenseValidTo)
            .append("licenseLongTerm", licenseLongTerm)
            .append("legalRepresentativeName", legalRepresentativeName)
            .append("signingDate", signingDate).append("contractValidFrom", contractValidFrom)
            .append("contractValidTo", contractValidTo)
            .append("auditTime", auditTime).append("auditRemark", auditRemark)
            .append("freezeTime", freezeTime).append("freezeReason", freezeReason)
            .append("wechatQrFileId", wechatQrFileId).append("vipLevel", vipLevel)
            .append("storageQuotaMb", storageQuotaMb)
            .append("externalAcceptEnabled", externalAcceptEnabled).append("status", status)
            .append("createBy", getCreateBy()).append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy()).append("updateTime", getUpdateTime())
            .append("remark", getRemark()).append("isDelete", isDelete)
            .toString();
    }
}
