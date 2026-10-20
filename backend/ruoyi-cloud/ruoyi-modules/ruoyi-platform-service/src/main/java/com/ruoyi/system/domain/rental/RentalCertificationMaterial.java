package com.ruoyi.system.domain.rental;

import com.ruoyi.common.core.web.domain.BaseEntity;

/** 商户认证版本关联的文件材料。 */
public class RentalCertificationMaterial extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long materialId;
    private Long tenantId;
    private Long memberId;
    private Long certificationId;
    private Integer materialType;
    private String materialName;
    private Long fileId;
    private String fileUrl;
    private Integer materialStatus;
    private String reviewRemark;
    private Integer sortNo;

    /** @return 材料唯一编号。 */
    public Long getMaterialId() { return materialId; }
    /** @param materialId 设置材料唯一编号。 */
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    /** @return 材料所属租户。 */
    public Long getTenantId() { return tenantId; }
    /** @param tenantId 设置材料所属租户。 */
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    /** @return 材料所属商户。 */
    public Long getMemberId() { return memberId; }
    /** @param memberId 设置材料所属商户。 */
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    /** @return 认证版本唯一编号。 */
    public Long getCertificationId() { return certificationId; }
    /** @param certificationId 设置认证版本唯一编号。 */
    public void setCertificationId(Long certificationId) { this.certificationId = certificationId; }
    /** @return 材料类型编码。 */
    public Integer getMaterialType() { return materialType; }
    /** @param materialType 设置材料类型编码。 */
    public void setMaterialType(Integer materialType) { this.materialType = materialType; }
    /** @return 材料展示名称。 */
    public String getMaterialName() { return materialName; }
    /** @param materialName 设置材料展示名称。 */
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    /** @return 文件服务唯一编号。 */
    public Long getFileId() { return fileId; }
    /** @param fileId 设置文件服务唯一编号。 */
    public void setFileId(Long fileId) { this.fileId = fileId; }
    /** @return 文件访问地址。 */
    public String getFileUrl() { return fileUrl; }
    /** @param fileUrl 设置文件访问地址。 */
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    /** @return 材料审核状态。 */
    public Integer getMaterialStatus() { return materialStatus; }
    /** @param materialStatus 设置材料审核状态。 */
    public void setMaterialStatus(Integer materialStatus) { this.materialStatus = materialStatus; }
    /** @return 材料审核意见。 */
    public String getReviewRemark() { return reviewRemark; }
    /** @param reviewRemark 设置材料审核意见。 */
    public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }
    /** @return 材料排序值。 */
    public Integer getSortNo() { return sortNo; }
    /** @param sortNo 设置材料排序值。 */
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
}
