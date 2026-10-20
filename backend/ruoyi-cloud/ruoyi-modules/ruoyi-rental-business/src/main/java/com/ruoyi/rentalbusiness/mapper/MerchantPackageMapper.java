package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/** 新架构商户套餐数据访问组件。 */
public interface MerchantPackageMapper
{
    List<Map<String, Object>> selectList(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("keyword") String keyword, @Param("status") Integer status);
    Map<String, Object> selectById(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageId") Long packageId);
    Map<String, Object> selectVersionById(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageId") Long packageId);
    List<Map<String, Object>> selectItems(@Param("packageVersionId") Long packageVersionId);
    Map<String, Object> selectProductByModel(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("modelId") Long modelId);
    Map<String, Object> selectProduct(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("productId") Long productId);
    Map<String, Object> selectPackageProduct(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("productId") Long productId);
    Map<String, Object> selectPackageProductByModel(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("modelId") Long modelId);
    Map<String, Object> selectAccessoryProduct(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("productId") Long productId);
    Map<String, Object> selectMerchantAccessory(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("accessoryId") Long accessoryId);
    List<Long> selectMerchantAccessoryModelIds(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("accessoryId") Long accessoryId, @Param("modelId") Long modelId);
    List<Map<String, Object>> selectMerchantAccessoryOptions(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("modelId") Long modelId);
    Map<String, Object> selectMerchantAccessoryByModel(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("modelId") Long modelId);
    Map<String, Object> selectPlatformPackage(@Param("packageId") Long packageId);
    int insertPackage(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("platformPackageId") Long platformPackageId, @Param("classId") Long classId,
            @Param("modelId") Long modelId, @Param("packageCode") String packageCode,
            @Param("packageName") String packageName, @Param("sortNo") Integer sortNo,
            @Param("operator") Long operator, @Param("remark") String remark);
    int insertCustomPackage(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("classId") Long classId, @Param("modelId") Long modelId, @Param("packageCode") String packageCode,
            @Param("packageName") String packageName, @Param("status") Integer status, @Param("sortNo") Integer sortNo,
            @Param("operator") Long operator, @Param("remark") String remark);
    int updatePackage(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageId") Long packageId, @Param("classId") Long classId,
            @Param("modelId") Long modelId, @Param("packageName") String packageName,
            @Param("status") Integer status, @Param("sortNo") Integer sortNo,
            @Param("operator") Long operator, @Param("remark") String remark);
    int softDeletePackage(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageId") Long packageId, @Param("operator") Long operator);
    int softDeletePackageVersions(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageId") Long packageId, @Param("operator") Long operator);
    int softDeletePackageItems(@Param("packageVersionId") Long packageVersionId, @Param("operator") Long operator);
    int selectNextVersionNo(@Param("packageId") Long packageId);
    Long selectLastInsertId();
    int insertVersion(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageId") Long packageId, @Param("versionNo") Integer versionNo,
            @Param("bomHash") String bomHash, @Param("operator") Long operator);
    Long selectLastVersionId();
    int insertItem(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageVersionId") Long packageVersionId, @Param("itemRole") Integer itemRole,
            @Param("productId") Long productId, @Param("accessoryId") Long accessoryId,
            @Param("accessoryItemProductId") Long accessoryItemProductId, @Param("merchantAccessoryId") Long merchantAccessoryId,
            @Param("quantity") Integer quantity,
            @Param("requiredFlag") Integer requiredFlag, @Param("sortNo") Integer sortNo,
            @Param("operator") Long operator);
}
