package com.ruoyi.system.api.model;

import java.util.List;

/** 商户认证版本跨服务传输模型，日期统一使用毫秒时间戳。 */
public class MerchantCertificationVersionView
{
    private Long certificationId;
    private Integer versionNo;
    private Integer certificationType;
    private Integer certificationStatus;
    private String legalSubjectName;
    private String licenseNo;
    private String legalRepresentativeName;
    private Long submittedAt;
    private Long reviewedAt;
    private String submittedBy;
    private String reviewedBy;
    private String reviewRemark;
    private List<Integer> supplementMaterialTypes;
    private List<MerchantCertificationMaterialView> materials;

    /** @return 认证版本唯一编号。 */
    public Long getCertificationId() { return certificationId; }
    /** @param certificationId 设置认证版本唯一编号。 */
    public void setCertificationId(Long certificationId) { this.certificationId = certificationId; }
    /** @return 认证版本号。 */
    public Integer getVersionNo() { return versionNo; }
    /** @param versionNo 设置认证版本号。 */
    public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }
    /** @return 认证主体类型。 */
    public Integer getCertificationType() { return certificationType; }
    /** @param certificationType 设置认证主体类型。 */
    public void setCertificationType(Integer certificationType) { this.certificationType = certificationType; }
    /** @return 认证版本状态。 */
    public Integer getCertificationStatus() { return certificationStatus; }
    /** @param certificationStatus 设置认证版本状态。 */
    public void setCertificationStatus(Integer certificationStatus) { this.certificationStatus = certificationStatus; }
    /** @return 认证主体名称。 */
    public String getLegalSubjectName() { return legalSubjectName; }
    /** @param legalSubjectName 设置认证主体名称。 */
    public void setLegalSubjectName(String legalSubjectName) { this.legalSubjectName = legalSubjectName; }
    /** @return 营业执照号。 */
    public String getLicenseNo() { return licenseNo; }
    /** @param licenseNo 设置营业执照号。 */
    public void setLicenseNo(String licenseNo) { this.licenseNo = licenseNo; }
    /** @return 法人姓名。 */
    public String getLegalRepresentativeName() { return legalRepresentativeName; }
    /** @param legalRepresentativeName 设置法人姓名。 */
    public void setLegalRepresentativeName(String legalRepresentativeName) { this.legalRepresentativeName = legalRepresentativeName; }
    /** @return 资料提交时间毫秒值。 */
    public Long getSubmittedAt() { return submittedAt; }
    /** @param submittedAt 设置资料提交时间毫秒值。 */
    public void setSubmittedAt(Long submittedAt) { this.submittedAt = submittedAt; }
    /** @return 审核时间毫秒值。 */
    public Long getReviewedAt() { return reviewedAt; }
    /** @param reviewedAt 设置审核时间毫秒值。 */
    public void setReviewedAt(Long reviewedAt) { this.reviewedAt = reviewedAt; }
    /** @return 资料提交人。 */
    public String getSubmittedBy() { return submittedBy; }
    /** @param submittedBy 设置资料提交人。 */
    public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }
    /** @return 审核人。 */
    public String getReviewedBy() { return reviewedBy; }
    /** @param reviewedBy 设置审核人。 */
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    /** @return 审核意见。 */
    public String getReviewRemark() { return reviewRemark; }
    /** @param reviewRemark 设置审核意见。 */
    public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }
    /** @return 待补材料类型编码。 */
    public List<Integer> getSupplementMaterialTypes() { return supplementMaterialTypes; }
    /** @param supplementMaterialTypes 设置待补材料类型编码。 */
    public void setSupplementMaterialTypes(List<Integer> supplementMaterialTypes) { this.supplementMaterialTypes = supplementMaterialTypes; }
    /** @return 当前版本的认证材料。 */
    public List<MerchantCertificationMaterialView> getMaterials() { return materials; }
    /** @param materials 设置当前版本的认证材料。 */
    public void setMaterials(List<MerchantCertificationMaterialView> materials) { this.materials = materials; }
}
