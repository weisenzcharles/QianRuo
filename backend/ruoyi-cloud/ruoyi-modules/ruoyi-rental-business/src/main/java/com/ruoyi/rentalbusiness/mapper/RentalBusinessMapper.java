package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.rentalbusiness.model.RentalBusinessComponentCommand;

/**
 * 租赁主数据统一数据访问接口。
 *
 * <p>所有方法都必须接收 tenantId 和 memberId；应用服务只传入可信登录上下文
 * 中的值，Mapper 不提供任何按客户端归属字段查询的重载。</p>
 */
public interface RentalBusinessMapper
{
    /** 获取当前事务连接刚生成的自增主键。 */
    Long selectLastInsertId();

    /** 查询当前商户仓库列表。 */
    List<Map<String, Object>> selectWarehouses(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("keyword") String keyword,
            @Param("status") Integer status, @Param("warehouseIds") java.util.Set<Long> warehouseIds,
            @Param("allWarehouseAccess") boolean allWarehouseAccess);

    /**
     * 查询供平台服务和商户服务使用的仓库内部视图。
     *
     * <p>该方法只允许租赁业务服务访问自己的业务库，调用方必须传入已经由上游
     * 身份服务确认的租户和商户主键。</p>
     *
     * @param tenantId 可信租户主键
     * @param memberId 可信商户主键
     * @param status 仓库状态，为空时不限制状态
     * @return 仓库内部视图集合
     */
    List<Map<String, Object>> selectWarehousesForInternal(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("status") Integer status);

    /**
     * 统计指定商户启用仓库中命中主键集合的数量，供平台端校验用户仓库范围。
     *
     * @param tenantId 可信租户主键
     * @param memberId 可信商户主键
     * @param warehouseIds 待校验仓库主键集合
     * @return 命中的启用仓库数量
     */
    int countEnabledWarehouses(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("warehouseIds") List<Long> warehouseIds);

    /**
     * 统计标准型号在租赁业务库中的有效引用数量。
     *
     * @param modelId 平台标准型号主键
     * @return 商品和资源池兼容关系中的有效引用数量
     */
    int countModelReferences(@Param("modelId") Long modelId);

    /** 查询当前商户单个仓库。 */
    Map<String, Object> selectWarehouse(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("warehouseId") Long warehouseId);

    /** 新增仓库。 */
    int insertWarehouse(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("code") String code, @Param("name") String name, @Param("contactName") String contactName,
            @Param("contactPhone") String contactPhone, @Param("province") String province,
            @Param("city") String city, @Param("district") String district, @Param("address") String address,
            @Param("longitude") java.math.BigDecimal longitude, @Param("latitude") java.math.BigDecimal latitude,
            @Param("pickupRuleJson") String pickupRuleJson, @Param("status") Integer status,
            @Param("returnEnabled") Integer returnEnabled, @Param("allianceShareEnabled") Integer allianceShareEnabled,
            @Param("remark") String remark, @Param("operator") String operator);

    /** 修改仓库。 */
    int updateWarehouse(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("warehouseId") Long warehouseId, @Param("code") String code, @Param("name") String name,
            @Param("contactName") String contactName, @Param("contactPhone") String contactPhone,
            @Param("province") String province, @Param("city") String city, @Param("district") String district,
            @Param("address") String address, @Param("longitude") java.math.BigDecimal longitude,
            @Param("latitude") java.math.BigDecimal latitude, @Param("pickupRuleJson") String pickupRuleJson,
            @Param("status") Integer status, @Param("returnEnabled") Integer returnEnabled,
            @Param("allianceShareEnabled") Integer allianceShareEnabled, @Param("remark") String remark,
            @Param("operator") String operator);

    /** 软删除仓库。 */
    int softDeleteWarehouse(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("warehouseId") Long warehouseId, @Param("operator") String operator);

    /** 查询仓库是否仍被商品或库存引用。 */
    int countWarehouseReferences(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("warehouseId") Long warehouseId);

    /** 查询资源池列表。 */
    List<Map<String, Object>> selectPools(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("keyword") String keyword, @Param("status") Integer status);

    /** 查询资源池详情。 */
    Map<String, Object> selectPool(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("poolId") Long poolId);

    /** 新增资源池。 */
    int insertPool(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("code") String code, @Param("name") String name, @Param("resourceType") String resourceType,
            @Param("substitutableFlag") Integer substitutableFlag, @Param("status") Integer status,
            @Param("remark") String remark, @Param("operator") String operator);

    /** 修改资源池。 */
    int updatePool(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("poolId") Long poolId, @Param("code") String code, @Param("name") String name,
            @Param("resourceType") String resourceType, @Param("substitutableFlag") Integer substitutableFlag,
            @Param("status") Integer status, @Param("remark") String remark, @Param("operator") String operator);

    /** 软删除资源池。 */
    int softDeletePool(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("poolId") Long poolId, @Param("operator") String operator);

    /** 查询资源池是否仍被商品或套餐引用。 */
    int countPoolReferences(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("poolId") Long poolId);

    /** 查询资源池兼容型号。 */
    List<Long> selectPoolModelIds(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("poolId") Long poolId);

    /** 删除资源池兼容型号关系。 */
    int softDeletePoolCompatibility(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("poolId") Long poolId, @Param("operator") String operator);

    /** 批量保存资源池兼容型号关系。 */
    int insertPoolCompatibility(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("poolId") Long poolId, @Param("modelIds") List<Long> modelIds, @Param("operator") String operator);

    /** 查询商品列表。 */
    List<Map<String, Object>> selectProducts(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("keyword") String keyword, @Param("productType") String productType,
            @Param("stockMode") String stockMode, @Param("classId") Long classId,
            @Param("status") Integer status, @Param("allowedClassIds") List<Long> allowedClassIds);

    /** 查询商品详情。 */
    Map<String, Object> selectProduct(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("productId") Long productId);

    /** 新增商品。 */
    int insertProduct(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("classId") Long classId, @Param("modelId") Long modelId, @Param("stockPoolId") Long stockPoolId,
            @Param("productCode") String productCode, @Param("productName") String productName,
            @Param("productType") String productType, @Param("stockMode") String stockMode,
            @Param("brand") String brand, @Param("modelName") String modelName,
            @Param("memorySpec") String memorySpec, @Param("purchaseCost") java.math.BigDecimal purchaseCost,
            @Param("salePrice") java.math.BigDecimal salePrice, @Param("status") Integer status,
            @Param("remark") String remark, @Param("operator") String operator);

    /** 修改商品。 */
    int updateProduct(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("productId") Long productId, @Param("classId") Long classId, @Param("modelId") Long modelId,
            @Param("stockPoolId") Long stockPoolId, @Param("productCode") String productCode,
            @Param("productName") String productName, @Param("productType") String productType,
            @Param("stockMode") String stockMode, @Param("brand") String brand,
            @Param("modelName") String modelName, @Param("memorySpec") String memorySpec,
            @Param("purchaseCost") java.math.BigDecimal purchaseCost, @Param("salePrice") java.math.BigDecimal salePrice,
            @Param("status") Integer status, @Param("remark") String remark, @Param("operator") String operator);

    /** 软删除商品。 */
    int softDeleteProduct(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("productId") Long productId, @Param("operator") String operator);

    /** 查询商品是否被 SN、配件、套餐或订单引用。 */
    int countProductReferences(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("productId") Long productId);

    /** 查询 SN 列表。 */
    List<Map<String, Object>> selectSns(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("keyword") String keyword, @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId, @Param("assetStatus") String assetStatus,
            @Param("warehouseIds") java.util.Set<Long> warehouseIds, @Param("allWarehouseAccess") boolean allWarehouseAccess,
            @Param("allowedClassIds") List<Long> allowedClassIds);

    /** 查询 SN 详情。 */
    Map<String, Object> selectSn(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("snId") Long snId);

    /** 检查 SN 编码是否重复。 */
    int countSnCode(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("snCode") String snCode, @Param("snId") Long snId);

    /** 新增 SN。 */
    int insertSn(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("productId") Long productId, @Param("warehouseId") Long warehouseId,
            @Param("snCode") String snCode, @Param("barcode") String barcode,
            @Param("assetStatus") String assetStatus, @Param("conditionLevel") String conditionLevel,
            @Param("operator") String operator, @Param("remark") String remark);

    /** 修改 SN。 */
    int updateSn(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId, @Param("snId") Long snId,
            @Param("warehouseId") Long warehouseId, @Param("snCode") String snCode, @Param("barcode") String barcode,
            @Param("assetStatus") String assetStatus, @Param("conditionLevel") String conditionLevel,
            @Param("operator") String operator, @Param("remark") String remark);

    /** 软删除 SN。 */
    int softDeleteSn(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("snId") Long snId, @Param("operator") String operator);

    /** 查询配件实例列表。 */
    List<Map<String, Object>> selectAccessories(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("keyword") String keyword, @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId, @Param("itemStatus") String itemStatus,
            @Param("warehouseIds") java.util.Set<Long> warehouseIds, @Param("allWarehouseAccess") boolean allWarehouseAccess,
            @Param("allowedClassIds") List<Long> allowedClassIds);

    /** 查询配件实例详情。 */
    Map<String, Object> selectAccessory(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("accessoryItemId") Long accessoryItemId);

    /** 新增配件实例。 */
    int insertAccessory(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("productId") Long productId, @Param("warehouseId") Long warehouseId,
            @Param("itemCode") String itemCode, @Param("stockMode") String stockMode,
            @Param("itemStatus") String itemStatus, @Param("operator") String operator,
            @Param("remark") String remark);

    /** 修改配件实例。 */
    int updateAccessory(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("accessoryItemId") Long accessoryItemId, @Param("warehouseId") Long warehouseId,
            @Param("itemCode") String itemCode, @Param("itemStatus") String itemStatus,
            @Param("operator") String operator, @Param("remark") String remark);

    /** 软删除配件实例。 */
    int softDeleteAccessory(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("accessoryItemId") Long accessoryItemId, @Param("operator") String operator);

    /** 查询套餐列表。 */
    List<Map<String, Object>> selectPackages(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("keyword") String keyword, @Param("enabled") Integer enabled);

    /** 查询套餐详情。 */
    Map<String, Object> selectPackage(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageId") Long packageId);

    /** 查询套餐当前生效版本。 */
    Map<String, Object> selectActivePackageVersion(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageId") Long packageId);

    /** 查询套餐版本组件。 */
    List<Map<String, Object>> selectPackageComponents(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("packageVersionId") Long packageVersionId);

    /** 新增套餐模板。 */
    int insertPackage(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageCode") String packageCode, @Param("packageName") String packageName,
            @Param("platformCode") String platformCode, @Param("enabled") Integer enabled,
            @Param("sortNo") Integer sortNo, @Param("remark") String remark, @Param("operator") String operator);

    /** 修改套餐模板。 */
    int updatePackage(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageId") Long packageId, @Param("packageCode") String packageCode,
            @Param("packageName") String packageName, @Param("platformCode") String platformCode,
            @Param("enabled") Integer enabled, @Param("sortNo") Integer sortNo,
            @Param("remark") String remark, @Param("operator") String operator);

    /** 软删除套餐模板。 */
    int softDeletePackage(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageId") Long packageId, @Param("operator") String operator);

    /** 查询套餐是否已被订单使用。 */
    int countPackageReferences(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageId") Long packageId);

    /** 查询套餐最新版本号。 */
    int nextPackageVersionNo(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageId") Long packageId);

    /** 将套餐旧生效版本改为退役。 */
    int retireActivePackageVersion(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageId") Long packageId, @Param("operator") String operator);

    /** 新增套餐版本。 */
    int insertPackageVersion(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageId") Long packageId, @Param("versionNo") Integer versionNo,
            @Param("versionStatus") String versionStatus, @Param("bomHash") String bomHash,
            @Param("operator") String operator);

    /** 新增套餐版本组件。 */
    int insertPackageComponents(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageVersionId") Long packageVersionId,
            @Param("components") List<RentalBusinessComponentCommand> components, @Param("operator") String operator);

    /** 软删除套餐全部版本和组件。 */
    int softDeletePackageVersions(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageId") Long packageId, @Param("operator") String operator);

    /** 软删除套餐全部版本组件。 */
    int softDeletePackageComponentsByPackage(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageId") Long packageId, @Param("operator") String operator);

    /** 查询库存列表。 */
    List<Map<String, Object>> selectInventory(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("keyword") String keyword, @Param("warehouseId") Long warehouseId,
            @Param("productType") String productType, @Param("warehouseIds") java.util.Set<Long> warehouseIds,
            @Param("allWarehouseAccess") boolean allWarehouseAccess, @Param("allowedClassIds") List<Long> allowedClassIds);

    /** 软删除零库存余额；库存流水和历史订单保持不变。 */
    int softDeleteInventoryBalance(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("productId") Long productId, @Param("warehouseId") Long warehouseId,
            @Param("operator") String operator);

    /** 查询数量库存余额并锁定行。 */
    Map<String, Object> selectBalanceForUpdate(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("productId") Long productId, @Param("warehouseId") Long warehouseId,
            @Param("stockStatus") String stockStatus);

    /** 新增数量库存余额。 */
    int insertBalance(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("productId") Long productId, @Param("warehouseId") Long warehouseId,
            @Param("stockStatus") String stockStatus, @Param("quantity") Integer quantity,
            @Param("operator") String operator, @Param("remark") String remark);

    /** 修改数量库存余额。 */
    int updateBalance(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("balanceId") Long balanceId, @Param("quantity") Integer quantity,
            @Param("operator") String operator, @Param("remark") String remark);

    /** 新增库存流水。 */
    int insertInventoryLedger(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("warehouseId") Long warehouseId, @Param("productId") Long productId,
            @Param("changeType") String changeType, @Param("changeQty") Integer changeQty,
            @Param("reason") String reason, @Param("operator") String operator);

    /** 查询资源池在指定仓库的可用数量。 */
    Map<String, Object> selectPoolAvailability(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("poolId") Long poolId, @Param("warehouseId") Long warehouseId);

    /** 查询资源池当前已预占数量。 */
    int selectReservedPoolQuantity(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("poolId") Long poolId, @Param("warehouseId") Long warehouseId);
}
