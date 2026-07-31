package com.ruoyi.merchant.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

/** 商户经营产品类目数据访问。 */
public interface MerchantProductClassMapper
{
    /**
     * 查询当前商户已选择且未删除的平台类目主键。
     *
     * @param tenantId 租户主键
     * @param merchantId 商户主键
     * @return 类目主键集合
     */
    List<Long> selectClassIds(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId);

    /**
     * 判断当前商户是否已选择指定有效类目。
     *
     * @param tenantId 租户主键
     * @param merchantId 商户主键
     * @param classId 平台类目主键
     * @return 匹配记录数
     */
    int countSelected(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("classId") Long classId);

    /**
     * 将当前商户原有经营类目全部标记为已删除。
     *
     * @param tenantId 租户主键
     * @param merchantId 商户主键
     * @param operator 操作人员
     * @return 受影响行数
     */
    int softDeleteAll(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("operator") String operator);

    /**
     * 批量新增或恢复当前商户选择的经营类目。
     *
     * @param tenantId 租户主键
     * @param merchantId 商户主键
     * @param classIds 平台类目主键集合
     * @param operator 操作人员
     * @return 受影响行数
     */
    int upsertSelections(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("classIds") List<Long> classIds, @Param("operator") String operator);
}
