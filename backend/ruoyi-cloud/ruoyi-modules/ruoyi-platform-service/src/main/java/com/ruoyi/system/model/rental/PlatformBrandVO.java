package com.ruoyi.system.model.rental;

import java.util.List;
import java.util.Date;

/** 平台品牌列表和详情接口使用的展示对象。 */
public class PlatformBrandVO
{
    private Long brandId;
    private String brandName;
    private List<Long> classIdList;
    private String className;
    private Integer sortNo;
    private Integer status;
    private Integer modelCount;
    private Long createBy;
    private Date createTime;
    private Long updateBy;
    private Date updateTime;
    private String remark;

    public Long getBrandId() { return brandId; }

    public void setBrandId(Long brandId) { this.brandId = brandId; }

    public String getBrandName() { return brandName; }

    public void setBrandName(String brandName) { this.brandName = brandName; }

    public List<Long> getClassIdList() { return classIdList; }

    public void setClassIdList(List<Long> classIdList) { this.classIdList = classIdList; }

    public String getClassName() { return className; }

    public void setClassName(String className) { this.className = className; }

    public Integer getSortNo() { return sortNo; }

    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }

    public Integer getStatus() { return status; }

    public void setStatus(Integer status) { this.status = status; }

    public Integer getModelCount() { return modelCount; }

    public void setModelCount(Integer modelCount) { this.modelCount = modelCount; }

    public Long getCreateBy() { return createBy; }

    public void setCreateBy(Long createBy) { this.createBy = createBy; }

    public Date getCreateTime() { return createTime; }

    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Long getUpdateBy() { return updateBy; }

    public void setUpdateBy(Long updateBy) { this.updateBy = updateBy; }

    public Date getUpdateTime() { return updateTime; }

    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    public String getRemark() { return remark; }

    public void setRemark(String remark) { this.remark = remark; }
}
