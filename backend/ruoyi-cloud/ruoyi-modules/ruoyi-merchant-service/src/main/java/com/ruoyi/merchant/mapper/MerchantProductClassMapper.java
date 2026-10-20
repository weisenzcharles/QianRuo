package com.ruoyi.merchant.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

/** 商户经营产品分类数据访问。 */
public interface MerchantProductClassMapper
{
    /**
     * 查询当前商户已选择且未删除的平台分类唯一编号。
     *
     * @param tenantId 租户唯一编号
     * @param merchantId 商户唯一编号
     * @return 分类唯一编号集合
     */
    List<Long> selectClassIds(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId);

    /**
     * 判断当前商户是否已选择指定有效分类。
     *
     * @param tenantId 租户唯一编号
     * @param merchantId 商户唯一编号
     * @param classId 平台分类唯一编号
     * @return 匹配记录数
     */
    int countSelected(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("classId") Long classId);

    /**
     * 将当前商户原有经营分类全部标记为已删除。
     *
     * @param tenantId 租户唯一编号
     * @param merchantId 商户唯一编号
     * @param operator 操作人员
     * @return 受影响行数
     */
    int softDeleteAll(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("operator") Long operator);

    /**
     * 批量新增或恢复当前商户选择的经营分类。
     *
     * @param tenantId 租户唯一编号
     * @param merchantId 商户唯一编号
     * @param classIds 平台分类唯一编号集合
     * @param operator 操作人员
     * @return 受影响行数
     */
    int upsertSelections(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("classIds") List<Long> classIds, @Param("operator") Long operator);
}
