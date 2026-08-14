package com.ruoyi.system.model.rental;

import java.util.Date;

/** 平台标准型号列表和详情接口使用的展示对象。 */
public class PlatformBrandModelVO
{
    private Long modelId;
    private Long classId;
    private String className;
    private String brand;
    private String modelName;
    private String memorySpec;
    private String officialFlag;
    private String status;
    private String createBy;
    private Date createTime;
    private String updateBy;
    private Date updateTime;
    private String remark;

    /** @return 标准型号主键 */
    public Long getModelId() { return modelId; }

    /** @param modelId 标准型号主键 */
    public void setModelId(Long modelId) { this.modelId = modelId; }

    /** @return 所属产品类目主键 */
    public Long getClassId() { return classId; }

    /** @param classId 所属产品类目主键 */
    public void setClassId(Long classId) { this.classId = classId; }

    /** @return 所属产品类目名称 */
    public String getClassName() { return className; }

    /** @param className 所属产品类目名称 */
    public void setClassName(String className) { this.className = className; }

    /** @return 平台标准品牌名称 */
    public String getBrand() { return brand; }

    /** @param brand 平台标准品牌名称 */
    public void setBrand(String brand) { this.brand = brand; }

    /** @return 平台标准型号名称 */
    public String getModelName() { return modelName; }

    /** @param modelName 平台标准型号名称 */
    public void setModelName(String modelName) { this.modelName = modelName; }

    /** @return 内存或规格描述 */
    public String getMemorySpec() { return memorySpec; }

    /** @param memorySpec 内存或规格描述 */
    public void setMemorySpec(String memorySpec) { this.memorySpec = memorySpec; }

    /** @return 1表示官方标准，0表示非官方标准 */
    public String getOfficialFlag() { return officialFlag; }

    /** @param officialFlag 1表示官方标准，0表示非官方标准 */
    public void setOfficialFlag(String officialFlag) { this.officialFlag = officialFlag; }

    /** @return 0表示正常，1表示停用 */
    public String getStatus() { return status; }

    /** @param status 0表示正常，1表示停用 */
    public void setStatus(String status) { this.status = status; }

    /** @return 创建该型号的平台操作人 */
    public String getCreateBy() { return createBy; }

    /** @param createBy 创建该型号的平台操作人 */
    public void setCreateBy(String createBy) { this.createBy = createBy; }

    /** @return 标准型号创建时间 */
    public Date getCreateTime() { return createTime; }

    /** @param createTime 标准型号创建时间 */
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    /** @return 最近修改该型号的平台操作人 */
    public String getUpdateBy() { return updateBy; }

    /** @param updateBy 最近修改该型号的平台操作人 */
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }

    /** @return 标准型号最近修改时间 */
    public Date getUpdateTime() { return updateTime; }

    /** @param updateTime 标准型号最近修改时间 */
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    /** @return 平台维护备注 */
    public String getRemark() { return remark; }

    /** @param remark 平台维护备注 */
    public void setRemark(String remark) { this.remark = remark; }
}
