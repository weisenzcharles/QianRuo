package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 平台产品分类内部传输对象。
 *
 * <p>该对象只承载平台服务发布给其他服务的有效分类数据，不包含平台数据库实体的审计字段。</p>
 */
public class ProductClass implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 分类唯一编号。 */
    private Long classId;

    /** 父分类唯一编号，根节点固定为 0。 */
    private Long parentId;

    /** 分类编码。 */
    private String classCode;

    /** 分类名称。 */
    private String className;

    /** 分类层级，根节点从 1 开始。 */
    private Integer classLevel;

    /** 同级显示顺序。 */
    private Integer sortNo;

    /** 状态：0 正常，1 停用。 */
    private Integer status;

    /** 子分类集合，仅树形接口返回。 */
    private List<ProductClass> children = new ArrayList<ProductClass>();

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

    /** @return 子分类集合 */
    public List<ProductClass> getChildren()
    {
        return children;
    }

    /** @param children 子分类集合 */
    public void setChildren(List<ProductClass> children)
    {
        this.children = children;
    }
}
