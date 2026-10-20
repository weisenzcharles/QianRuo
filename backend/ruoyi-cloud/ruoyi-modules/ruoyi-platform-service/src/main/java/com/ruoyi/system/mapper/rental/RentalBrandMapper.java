package com.ruoyi.system.mapper.rental;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.rental.RentalBrand;
import com.ruoyi.system.model.rental.PlatformBrandQuery;

/** 平台品牌主数据访问组件。 */
public interface RentalBrandMapper
{
    /** 查询未删除品牌列表。 */
    List<RentalBrand> selectList(PlatformBrandQuery query);

    /** 查询未删除品牌详情，分类关系由服务层单独读取为编号集合。 */
    RentalBrand selectById(@Param("brandId") Long brandId);

    /** 查询品牌当前有效的二级产品分类关系。 */
    List<Long> selectClassIds(@Param("brandId") Long brandId);

    /** 按名称统计有效品牌，用于应用层重复校验。 */
    int countByName(@Param("brandName") String brandName, @Param("excludeBrandId") Long excludeBrandId);

    /** 统计品牌下关联的标准型号数量。 */
    int countModelReferences(Long brandId);

    /** 统计指定品牌和产品分类下的型号数量。 */
    int countModelReferencesByClass(@Param("brandId") Long brandId, @Param("classId") Long classId);

    /** 判断品牌是否已绑定指定产品分类。 */
    int countClassRelation(@Param("brandId") Long brandId, @Param("classId") Long classId);

    /** 新增品牌。 */
    int insert(RentalBrand brand);

    /** 新增或恢复一条品牌和产品分类关系。 */
    int insertClassRelation(@Param("brandId") Long brandId, @Param("classId") Long classId,
            @Param("operator") Long operator);

    /** 软删除指定的一条品牌分类关系。 */
    int softDeleteClassRelation(@Param("brandId") Long brandId, @Param("classId") Long classId,
            @Param("operator") Long operator);

    /** 软删除品牌的全部分类关系。 */
    int softDeleteRelations(@Param("brandId") Long brandId, @Param("operator") Long operator);

    /** 修改品牌。 */
    int update(RentalBrand brand);

    /** 软删除品牌。 */
    int softDelete(@Param("brandId") Long brandId, @Param("operator") Long operator);
}
