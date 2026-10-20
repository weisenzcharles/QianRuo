package com.ruoyi.system.domain.rental;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 商户认证资料版本。
 *
 * <p>每次注册、驳回重提或平台发起重新认证都会生成新的版本，审核结果只更新当前版本，
 * 历史版本保留用于追踪认证过程。</p>
 */
public class RentalMemberCertification extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long certificationId;
    private Long tenantId;
    private Long memberId;
    private Integer versionNo;
    private Integer certificationType;
    private Integer certificationStatus;
    private String legalSubjectName;
    private String licenseNo;
    private BigDecimal registeredCapital;
    private String businessScope;
    private String licenseAddress;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date licenseValidFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date licenseValidTo;

    private Integer licenseLongTerm;
    private String legalRepresentativeName;
    private String legalRepresentativeIdNo;
    private String submittedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submittedTime;

    private String reviewedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewedTime;

    private String reviewRemark;
    /** 待补材料类型编码，使用逗号分隔保存，具体编码由认证材料类型枚举约束。 */
    private String supplementMaterialTypes;
    private List<RentalCertificationMaterial> materials;

    /** @return 认证版本唯一编号，用于关联材料和审核操作。 */
    public Long getCertificationId() { return certificationId; }
    /** @param certificationId 设置认证版本唯一编号。 */
    public void setCertificationId(Long certificationId) { this.certificationId = certificationId; }
    /** @return 认证记录所属租户。 */
    public Long getTenantId() { return tenantId; }
    /** @param tenantId 设置认证记录所属租户。 */
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    /** @return 认证记录所属商户。 */
    public Long getMemberId() { return memberId; }
    /** @param memberId 设置认证记录所属商户。 */
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    /** @return 同一商户内递增的认证版本号。 */
    public Integer getVersionNo() { return versionNo; }
    /** @param versionNo 设置认证版本号。 */
    public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }
    /** @return 认证主体类型，取 personal 或 enterprise。 */
    public Integer getCertificationType() { return certificationType; }
    /** @param certificationType 设置认证主体类型。 */
    public void setCertificationType(Integer certificationType) { this.certificationType = certificationType; }
    /** @return 认证审核状态。 */
    public Integer getCertificationStatus() { return certificationStatus; }
    /** @param certificationStatus 设置认证审核状态。 */
    public void setCertificationStatus(Integer certificationStatus) { this.certificationStatus = certificationStatus; }
    /** @return 认证主体名称。 */
    public String getLegalSubjectName() { return legalSubjectName; }
    /** @param legalSubjectName 设置认证主体名称。 */
    public void setLegalSubjectName(String legalSubjectName) { this.legalSubjectName = legalSubjectName; }
    /** @return 营业执照或主体编号。 */
    public String getLicenseNo() { return licenseNo; }
    /** @param licenseNo 设置营业执照或主体编号。 */
    public void setLicenseNo(String licenseNo) { this.licenseNo = licenseNo; }
    /** @return 注册资本。 */
    public BigDecimal getRegisteredCapital() { return registeredCapital; }
    /** @param registeredCapital 设置注册资本。 */
    public void setRegisteredCapital(BigDecimal registeredCapital) { this.registeredCapital = registeredCapital; }
    /** @return 营业执照经营范围。 */
    public String getBusinessScope() { return businessScope; }
    /** @param businessScope 设置营业执照经营范围。 */
    public void setBusinessScope(String businessScope) { this.businessScope = businessScope; }
    /** @return 营业执照登记地址。 */
    public String getLicenseAddress() { return licenseAddress; }
    /** @param licenseAddress 设置营业执照登记地址。 */
    public void setLicenseAddress(String licenseAddress) { this.licenseAddress = licenseAddress; }
    /** @return 营业执照有效期开始日期。 */
    public Date getLicenseValidFrom() { return licenseValidFrom; }
    /** @param licenseValidFrom 设置营业执照有效期开始日期。 */
    public void setLicenseValidFrom(Date licenseValidFrom) { this.licenseValidFrom = licenseValidFrom; }
    /** @return 营业执照有效期结束日期。 */
    public Date getLicenseValidTo() { return licenseValidTo; }
    /** @param licenseValidTo 设置营业执照有效期结束日期。 */
    public void setLicenseValidTo(Date licenseValidTo) { this.licenseValidTo = licenseValidTo; }
    /** @return 执照是否长期有效。 */
    public Integer getLicenseLongTerm() { return licenseLongTerm; }
    /** @param licenseLongTerm 设置执照是否长期有效。 */
    public void setLicenseLongTerm(Integer licenseLongTerm) { this.licenseLongTerm = licenseLongTerm; }
    /** @return 法人姓名。 */
    public String getLegalRepresentativeName() { return legalRepresentativeName; }
    /** @param legalRepresentativeName 设置法人姓名。 */
    public void setLegalRepresentativeName(String legalRepresentativeName) { this.legalRepresentativeName = legalRepresentativeName; }
    /** @return 法人身份证号。 */
    public String getLegalRepresentativeIdNo() { return legalRepresentativeIdNo; }
    /** @param legalRepresentativeIdNo 设置法人身份证号。 */
    public void setLegalRepresentativeIdNo(String legalRepresentativeIdNo) { this.legalRepresentativeIdNo = legalRepresentativeIdNo; }
    /** @return 资料提交人。 */
    public String getSubmittedBy() { return submittedBy; }
    /** @param submittedBy 设置资料提交人。 */
    public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }
    /** @return 资料提交时间。 */
    public Date getSubmittedTime() { return submittedTime; }
    /** @param submittedTime 设置资料提交时间。 */
    public void setSubmittedTime(Date submittedTime) { this.submittedTime = submittedTime; }
    /** @return 审核人。 */
    public String getReviewedBy() { return reviewedBy; }
    /** @param reviewedBy 设置审核人。 */
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    /** @return 审核时间。 */
    public Date getReviewedTime() { return reviewedTime; }
    /** @param reviewedTime 设置审核时间。 */
    public void setReviewedTime(Date reviewedTime) { this.reviewedTime = reviewedTime; }
    /** @return 审核意见或驳回原因。 */
    public String getReviewRemark() { return reviewRemark; }
    /** @param reviewRemark 设置审核意见或驳回原因。 */
    public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }
    /** @return 待补材料类型编码，未要求补件时返回 null。 */
    public String getSupplementMaterialTypes() { return supplementMaterialTypes; }
    /** @param supplementMaterialTypes 设置待补材料类型编码。 */
    public void setSupplementMaterialTypes(String supplementMaterialTypes) { this.supplementMaterialTypes = supplementMaterialTypes; }
    /** @return 当前版本关联的认证材料。 */
    public List<RentalCertificationMaterial> getMaterials() { return materials; }
    /** @param materials 设置当前版本关联的认证材料。 */
    public void setMaterials(List<RentalCertificationMaterial> materials) { this.materials = materials; }
}
