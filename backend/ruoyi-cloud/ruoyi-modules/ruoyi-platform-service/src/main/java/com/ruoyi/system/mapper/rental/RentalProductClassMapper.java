package com.ruoyi.system.mapper.rental;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.rental.RentalProductClass;

/** 平台产品分类数据访问。 */
public interface RentalProductClassMapper
{
    /**
     * 按条件查询未删除分类。
     *
     * @param query 查询条件
     * @return 分类集合
     */
    List<RentalProductClass> selectList(RentalProductClass query);

    /**
     * 查询全部正常分类，供内部服务发布。
     *
     * @return 正常分类集合
     */
    List<RentalProductClass> selectActiveList();

    /**
     * 按唯一编号查询未删除分类。
     *
     * @param classId 分类唯一编号
     * @return 分类实体
     */
    RentalProductClass selectById(Long classId);

    /**
     * 查询重复的有效分类编码。
     *
     * @param classCode 分类编码
     * @param excludeClassId 修改时排除的分类唯一编号
     * @return 重复记录数
     */
    int countByCode(@Param("classCode") String classCode, @Param("excludeClassId") Long excludeClassId);

    /**
     * 查询同一父分类下重复的有效名称。
     *
     * @param parentId 父分类唯一编号
     * @param className 分类名称
     * @param excludeClassId 修改时排除的分类唯一编号
     * @return 重复记录数
     */
    int countByName(@Param("parentId") Long parentId, @Param("className") String className,
            @Param("excludeClassId") Long excludeClassId);

    /**
     * 查询指定分类的未删除直接子节点数量。
     *
     * @param classId 分类唯一编号
     * @return 子节点数量
     */
    int countChildren(Long classId);

    /**
     * 新增平台产品分类。
     *
     * @param productClass 待新增分类
     * @return 受影响行数
     */
    int insert(RentalProductClass productClass);

    /**
     * 修改未删除的平台产品分类。
     *
     * @param productClass 待修改分类
     * @return 受影响行数
     */
    int update(RentalProductClass productClass);

    /**
     * 分类移动后批量更新所有未删除后代节点的祖先路径和层级。
     *
     * @param oldAncestorPrefix 移动前包含当前节点的祖先路径前缀
     * @param newAncestorPrefix 移动后包含当前节点的祖先路径前缀
     * @param levelOffset 新旧层级差值
     * @param operator 操作人员
     * @return 受影响的后代节点数量
     */
    int updateDescendantHierarchy(@Param("oldAncestorPrefix") String oldAncestorPrefix,
            @Param("newAncestorPrefix") String newAncestorPrefix,
            @Param("levelOffset") int levelOffset,
            @Param("operator") Long operator);

    /**
     * 通过 isDelete 标记软删除分类。
     *
     * @param classId 分类唯一编号
     * @param operator 操作人员
     * @return 受影响行数
     */
    int softDelete(@Param("classId") Long classId, @Param("operator") Long operator);
}
