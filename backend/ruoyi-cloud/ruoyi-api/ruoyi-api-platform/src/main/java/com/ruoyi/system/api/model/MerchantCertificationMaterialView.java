package com.ruoyi.system.api.model;

/** 商户认证材料跨服务传输模型。 */
public class MerchantCertificationMaterialView
{
    private String materialType;
    private String materialName;
    private String fileUrl;
    private String materialStatus;
    private String reviewRemark;

    /** @return 材料类型编码。 */
    public String getMaterialType() { return materialType; }
    /** @param materialType 设置材料类型编码。 */
    public void setMaterialType(String materialType) { this.materialType = materialType; }
    /** @return 材料名称。 */
    public String getMaterialName() { return materialName; }
    /** @param materialName 设置材料名称。 */
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    /** @return 文件地址。 */
    public String getFileUrl() { return fileUrl; }
    /** @param fileUrl 设置文件地址。 */
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    /** @return 材料审核状态。 */
    public String getMaterialStatus() { return materialStatus; }
    /** @param materialStatus 设置材料审核状态。 */
    public void setMaterialStatus(String materialStatus) { this.materialStatus = materialStatus; }
    /** @return 材料审核意见。 */
    public String getReviewRemark() { return reviewRemark; }
    /** @param reviewRemark 设置材料审核意见。 */
    public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }
}
