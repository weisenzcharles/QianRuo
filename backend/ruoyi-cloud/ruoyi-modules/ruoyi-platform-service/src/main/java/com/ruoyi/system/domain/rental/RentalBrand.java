package com.ruoyi.system.domain.rental;

import java.util.List;
import com.ruoyi.common.core.web.domain.BaseEntity;

/**
 * 平台品牌主数据实体。
 *
 * <p>品牌与标准型号分开维护，型号通过 {@code brandId} 关联品牌；型号查询时实时读取品牌主数据名称。</p>
 */
public class RentalBrand extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 品牌唯一编号。 */
    private Long brandId;

    /** 品牌名称。 */
    private String brandName;

    /** 显示顺序。 */
    private Integer sortNo;

    /** 状态：0正常，1停用。 */
    private Integer status;

    /** 关联标准型号数量。 */
    private Integer modelCount;

    /** 当前品牌绑定的二级产品分类唯一编号集合。 */
    private List<Long> classIdList;

    /** 当前品牌分类关系对应的二级分类名称。 */
    private String className;

    /** 软删除标识：0正常，1删除。 */
    private Integer isDelete;

    public Long getBrandId()
    {
        return brandId;
    }

    public void setBrandId(Long brandId)
    {
        this.brandId = brandId;
    }

    public String getBrandName()
    {
        return brandName;
    }

    public void setBrandName(String brandName)
    {
        this.brandName = brandName;
    }

    public Integer getSortNo()
    {
        return sortNo;
    }

    public void setSortNo(Integer sortNo)
    {
        this.sortNo = sortNo;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public Integer getModelCount()
    {
        return modelCount;
    }

    public void setModelCount(Integer modelCount)
    {
        this.modelCount = modelCount;
    }

    public List<Long> getClassIdList()
    {
        return classIdList;
    }

    public void setClassIdList(List<Long> classIdList)
    {
        this.classIdList = classIdList;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public Integer getIsDelete()
    {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete)
    {
        this.isDelete = isDelete;
    }
}
