package com.ruoyi.system.domain.rental;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.common.core.web.domain.BaseEntity;

/** 平台产品分类实体。 */
public class RentalProductClass extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 分类唯一编号。 */
    private Long classId;

    /** 父分类唯一编号，根节点固定为 0。 */
    @NotNull(message = "上级分类不能为空")
    private Long parentId;

    /** 祖先唯一编号路径，由服务端维护。 */
    private String ancestors;

    /**
     * 分类编码由平台统一编码规则服务在新增时生成，客户端只负责展示已有编码。
     */
    @Size(max = 64, message = "分类编码长度不能超过64个字符")
    private String classCode;

    /** 分类名称。 */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称长度不能超过100个字符")
    private String className;

    /** 分类层级，由服务端根据父分类计算。 */
    private Integer classLevel;

    /** 同级显示顺序。 */
    @NotNull(message = "显示顺序不能为空")
    private Integer sortNo;

    /** 状态：0 正常，1 停用。 */
    @Min(value = 0, message = "分类状态不合法")
    @Max(value = 1, message = "分类状态不合法")
    private Integer status;

    /** 软删除标识：0 正常，1 已删除。 */
    private Integer isDelete;

    /** 子分类集合。 */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<RentalProductClass> children = new ArrayList<RentalProductClass>();

    /** @return 分类唯一编号 */
    public Long getClassId()
    {
        return classId;
    }

    /** @param classId 分类唯一编号 */
    public void setClassId(Long classId)
    {
        this.classId = classId;
    }

    /** @return 父分类唯一编号 */
    public Long getParentId()
    {
        return parentId;
    }

    /** @param parentId 父分类唯一编号 */
    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    /** @return 祖先路径 */
    public String getAncestors()
    {
        return ancestors;
    }

    /** @param ancestors 祖先路径 */
    public void setAncestors(String ancestors)
    {
        this.ancestors = ancestors;
    }

    /** @return 分类编码 */
    public String getClassCode()
    {
        return classCode;
    }

    /** @param classCode 分类编码 */
    public void setClassCode(String classCode)
    {
        this.classCode = classCode;
    }

    /** @return 分类名称 */
    public String getClassName()
    {
        return className;
    }

    /** @param className 分类名称 */
    public void setClassName(String className)
    {
        this.className = className;
    }

    /** @return 分类层级 */
    public Integer getClassLevel()
    {
        return classLevel;
    }

    /** @param classLevel 分类层级 */
    public void setClassLevel(Integer classLevel)
    {
        this.classLevel = classLevel;
    }

    /** @return 同级显示顺序 */
    public Integer getSortNo()
    {
        return sortNo;
    }

    /** @param sortNo 同级显示顺序 */
    public void setSortNo(Integer sortNo)
    {
        this.sortNo = sortNo;
    }

    /** @return 分类状态 */
    public Integer getStatus()
    {
        return status;
    }

    /** @param status 分类状态 */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /** @return 软删除标识 */
    public Integer getIsDelete()
    {
        return isDelete;
    }

    /** @param isDelete 软删除标识 */
    public void setIsDelete(Integer isDelete)
    {
        this.isDelete = isDelete;
    }

    /** @return 子分类集合 */
    public List<RentalProductClass> getChildren()
    {
        return children;
    }

    /** @param children 子分类集合 */
    public void setChildren(List<RentalProductClass> children)
    {
        this.children = children;
    }
}
