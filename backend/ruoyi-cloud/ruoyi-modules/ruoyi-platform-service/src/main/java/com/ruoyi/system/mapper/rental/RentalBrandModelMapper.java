package com.ruoyi.system.mapper.rental;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.rental.RentalBrandModel;
import com.ruoyi.system.model.rental.PlatformBrandModelQuery;

/** 平台标准品牌型号数据访问。 */
public interface RentalBrandModelMapper
{
    /**
     * 按条件查询平台标准品牌型号，结果只包含未删除记录。
     *
     * @param query 品牌、型号、分类和状态查询条件
     * @return 标准品牌型号集合
     */
    List<RentalBrandModel> selectList(PlatformBrandModelQuery query);

    /**
     * 查询全部正常标准品牌型号，供商户服务内部读取。
     *
     * @return 正常标准品牌型号集合
     */
    List<RentalBrandModel> selectActiveList();

    /**
     * 按唯一编号查询未删除的标准型号。
     *
     * @param modelId 标准型号唯一编号
     * @return 标准型号实体，不存在时返回 null
     */
    RentalBrandModel selectById(Long modelId);

    /**
     * 查询相同分类、品牌、型号和规格的有效重复记录。
     *
     * @param classId 产品分类唯一编号
     * @param brandId 品牌唯一编号
     * @param modelName 型号
     * @param memorySpec 内存或规格
     * @param excludeModelId 编辑时排除的唯一编号
     * @return 重复记录数
     */
    int countByIdentity(@Param("classId") Long classId, @Param("brandId") Long brandId,
            @Param("modelName") String modelName, @Param("memorySpec") String memorySpec,
            @Param("excludeModelId") Long excludeModelId);

    /**
     * 查询标准型号是否仍被商品或商户常用型号引用。
     *
     * @param modelId 标准型号唯一编号
     * @return 引用记录数
     */
    int countMerchantCommonModelReferences(Long modelId);

    /**
     * 新增平台标准品牌型号。
     *
     * @param model 待新增标准型号
     * @return 受影响行数
     */
    int insert(RentalBrandModel model);

    /**
     * 修改未删除的平台标准品牌型号。
     *
     * @param model 待修改标准型号
     * @return 受影响行数
     */
    int update(RentalBrandModel model);

    /**
     * 软删除平台标准品牌型号。
     *
     * @param modelId 标准型号唯一编号
     * @param operator 操作人员
     * @return 受影响行数
     */
    int softDelete(@Param("modelId") Long modelId, @Param("operator") Long operator);
}
