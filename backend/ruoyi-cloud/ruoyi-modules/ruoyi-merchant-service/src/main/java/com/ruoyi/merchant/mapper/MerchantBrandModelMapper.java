package com.ruoyi.merchant.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

/** 商户常用型号关系数据访问。 */
public interface MerchantBrandModelMapper
{
    /**
     * 查询当前商户仍有效的常用标准型号主键。
     *
     * @param tenantId 当前租户主键
     * @param merchantId 当前商户主键
     * @return 常用型号主键集合
     */
    List<Long> selectSelectedModelIds(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId);

    /**
     * 判断标准型号是否已经被当前商户选中。
     *
     * @param tenantId 当前租户主键
     * @param merchantId 当前商户主键
     * @param modelId 标准型号主键
     * @return 匹配记录数
     */
    int countSelected(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("modelId") Long modelId);

    /**
     * 将当前商户原有常用型号全部软删除。
     *
     * @param tenantId 当前租户主键
     * @param merchantId 当前商户主键
     * @param operator 操作人员
     * @return 受影响行数
     */
    int softDeleteAll(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("operator") String operator);

    /**
     * 批量新增或恢复当前商户的常用型号关系。
     *
     * @param tenantId 当前租户主键
     * @param merchantId 当前商户主键
     * @param modelIds 标准型号主键集合
     * @param operator 操作人员
     * @return 受影响行数
     */
    int upsertSelections(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("modelIds") List<Long> modelIds, @Param("operator") String operator);
}
