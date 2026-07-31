package com.ruoyi.system.domain.rental;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.ruoyi.common.core.web.domain.BaseEntity;

/** 平台产品类目实体。 */
public class RentalProductClass extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 类目主键。 */
    private Long classId;

    /** 父类目主键，根节点固定为 0。 */
    @NotNull(message = "上级类目不能为空")
    private Long parentId;

    /** 祖先主键路径，由服务端维护。 */
    private String ancestors;

    /** 类目编码。 */
    @NotBlank(message = "类目编码不能为空")
    @Size(max = 64, message = "类目编码长度不能超过64个字符")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "类目编码只能包含字母、数字、下划线和短横线")
    private String classCode;

    /** 类目名称。 */
    @NotBlank(message = "类目名称不能为空")
    @Size(max = 100, message = "类目名称长度不能超过100个字符")
    private String className;

    /** 类目层级，由服务端根据父类目计算。 */
    private Integer classLevel;

    /** 同级显示顺序。 */
    @NotNull(message = "显示顺序不能为空")
    private Integer sortNo;

    /** 状态：0 正常，1 停用。 */
    @Pattern(regexp = "^[01]$", message = "类目状态不合法")
    private String status;

    /** 软删除标识：0 正常，1 已删除。 */
    private Integer isdeleted;

    /** 子类目集合。 */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<RentalProductClass> children = new ArrayList<RentalProductClass>();

    /** @return 类目主键 */
    public Long getClassId()
    {
        return classId;
    }

    /** @param classId 类目主键 */
    public void setClassId(Long classId)
    {
        this.classId = classId;
    }

    /** @return 父类目主键 */
    public Long getParentId()
    {
        return parentId;
    }

    /** @param parentId 父类目主键 */
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

    /** @return 类目编码 */
    public String getClassCode()
    {
        return classCode;
    }

    /** @param classCode 类目编码 */
    public void setClassCode(String classCode)
    {
        this.classCode = classCode;
    }

    /** @return 类目名称 */
    public String getClassName()
    {
        return className;
    }

    /** @param className 类目名称 */
    public void setClassName(String className)
    {
        this.className = className;
    }

    /** @return 类目层级 */
    public Integer getClassLevel()
    {
        return classLevel;
    }

    /** @param classLevel 类目层级 */
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

    /** @return 类目状态 */
    public String getStatus()
    {
        return status;
    }

    /** @param status 类目状态 */
    public void setStatus(String status)
    {
        this.status = status;
    }

    /** @return 软删除标识 */
    public Integer getIsdeleted()
    {
        return isdeleted;
    }

    /** @param isdeleted 软删除标识 */
    public void setIsdeleted(Integer isdeleted)
    {
        this.isdeleted = isdeleted;
    }

    /** @return 子类目集合 */
    public List<RentalProductClass> getChildren()
    {
        return children;
    }

    /** @param children 子类目集合 */
    public void setChildren(List<RentalProductClass> children)
    {
        this.children = children;
    }
}
