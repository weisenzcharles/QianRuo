package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.Set;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.rentalbusiness.domain.MerchantInventoryForecastRecord;
import com.ruoyi.rentalbusiness.model.MerchantInboundOutboundTrendRowVO;
import com.ruoyi.rentalbusiness.model.MerchantInventoryForecastRowVO;
import com.ruoyi.rentalbusiness.model.MerchantInboundOutboundOrderVO;

/** 新架构商户库存数据访问接口。 */
public interface MerchantInventoryMapper
{
    /** 获取当前事务连接生成的自增编号。 */
    Long selectLastInsertId();

    List<Map<String, Object>> selectInventory(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("keyword") String keyword, @Param("warehouseId") Long warehouseId,
            @Param("unitStatus") Integer unitStatus, @Param("classId") Long classId,
            @Param("productType") Integer productType, @Param("brand") String brand,
            @Param("excludeWarehouseId") Long excludeWarehouseId, @Param("warehouseIds") Set<Long> warehouseIds,
            @Param("allWarehouseAccess") boolean allWarehouseAccess);

    List<Map<String, Object>> selectSharedInventory(@Param("tenantId") Long tenantId,
            @Param("allianceId") Long allianceId, @Param("memberIds") List<Long> memberIds,
            @Param("keyword") String keyword,
            @Param("warehouseId") Long warehouseId, @Param("productType") Integer productType);

    /** 查询带实物照片且当前可用的市场候选逐件库存。 */
    List<Map<String, Object>> selectMarketEligibleUnits(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("brand") String brand,
            @Param("modelName") String modelName, @Param("memorySpec") String memorySpec,
            @Param("unitIds") List<Long> unitIds);

    List<Map<String, Object>> selectModelOverview(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("warehouseId") Long warehouseId,
            @Param("brand") String brand, @Param("modelId") Long modelId, @Param("days") Integer days,
            @Param("excludedOrderStatuses") List<String> excludedOrderStatuses);

    List<Map<String, Object>> selectPackageOverview(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("warehouseId") Long warehouseId, @Param("days") Integer days,
            @Param("excludedOrderStatuses") List<String> excludedOrderStatuses);

    /** 查询需要重算库存预测的商户范围。 */
    List<Map<String, Object>> selectForecastMemberScopes();

    /** 查询商户启用库存涉及的仓库范围。 */
    List<Long> selectForecastWarehouseIds(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId);

    /** 查询已生成的库存预测快照。 */
    List<MerchantInventoryForecastRowVO> selectForecastSnapshots(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("warehouseId") Long warehouseId,
            @Param("forecastType") Integer forecastType, @Param("brand") String brand,
            @Param("modelId") Long modelId, @Param("days") Integer days);

    /** 软删除指定商户指定日期范围的旧预测快照。 */
    int removeForecastSnapshots(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("fromDate") java.time.LocalDate fromDate, @Param("toDate") java.time.LocalDate toDate,
            @Param("operator") Long operator);

    /** 批量保存一轮库存预测快照。 */
    int insertForecastSnapshots(@Param("rows") List<MerchantInventoryForecastRecord> rows);

    List<MerchantInboundOutboundTrendRowVO> selectInboundOutboundTrend(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("mode") String mode,
            @Param("warehouseId") Long warehouseId, @Param("brand") String brand,
            @Param("modelId") Long modelId, @Param("days") Integer days,
            @Param("shipmentStatusValues") List<String> shipmentStatusValues,
            @Param("returnStatusValues") List<String> returnStatusValues,
            @Param("warehouseIds") Set<Long> warehouseIds,
            @Param("allWarehouseAccess") boolean allWarehouseAccess,
            @Param("showAll") boolean showAll);

    /** 查询收发趋势某个数字对应的订单明细。 */
    List<MerchantInboundOutboundOrderVO> selectInboundOutboundOrders(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("mode") String mode,
            @Param("warehouseId") Long warehouseId, @Param("itemId") Long itemId,
            @Param("trendDate") String trendDate, @Param("direction") String direction,
            @Param("source") String source, @Param("shipmentStatusValues") List<String> shipmentStatusValues,
            @Param("returnStatusValues") List<String> returnStatusValues,
            @Param("warehouseIds") Set<Long> warehouseIds, @Param("allWarehouseAccess") boolean allWarehouseAccess);

    Long countPendingShipOrders(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("mode") String mode, @Param("warehouseId") Long warehouseId,
            @Param("brand") String brand, @Param("modelId") Long modelId,
            @Param("shipmentStatusValues") List<String> shipmentStatusValues,
            @Param("warehouseIds") Set<Long> warehouseIds,
            @Param("allWarehouseAccess") boolean allWarehouseAccess);

    List<Map<String, Object>> selectProductOptions(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("classId") Long classId,
            @Param("productType") Integer productType, @Param("brand") String brand,
            @Param("keyword") String keyword);

    List<Map<String, Object>> selectUnits(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("inventoryId") Long inventoryId, @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId, @Param("keyword") String keyword,
            @Param("unitStatus") Integer unitStatus, @Param("transferableOnly") boolean transferableOnly);

    /** 查询数量库存当前仍在预占中的订单。 */
    List<Map<String, Object>> selectQuantityReservationOrders(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("inventoryId") Long inventoryId);

    Map<String, Object> selectProduct(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("productId") Long productId);

    Map<String, Object> selectWarehouse(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("warehouseId") Long warehouseId);

    int countSerialNumbers(@Param("memberId") Long memberId, @Param("serialNumbers") List<String> serialNumbers);

    Map<String, Object> selectBatchByIdempotency(@Param("memberId") Long memberId,
            @Param("idempotencyKey") String idempotencyKey);

    int insertBatch(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("warehouseId") Long warehouseId, @Param("productId") Long productId,
            @Param("modelId") Long modelId, @Param("batchNo") String batchNo,
            @Param("totalQuantity") int totalQuantity, @Param("idempotencyKey") String idempotencyKey,
            @Param("operator") Long operator, @Param("remark") String remark);

    int insertBatchItem(@Param("batchId") Long batchId, @Param("memberId") Long memberId,
            @Param("serialNumber") String serialNumber, @Param("operator") Long operator);

    int insertInventory(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("warehouseId") Long warehouseId, @Param("productId") Long productId,
            @Param("modelId") Long modelId, @Param("operator") Long operator);

    /** 新增数量库存汇总，并初始化总库存和可用库存。 */
    int insertQuantityInventory(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("warehouseId") Long warehouseId, @Param("productId") Long productId,
            @Param("modelId") Long modelId, @Param("quantity") Integer quantity,
            @Param("operator") Long operator);

    Map<String, Object> selectInventoryByScope(@Param("memberId") Long memberId,
            @Param("warehouseId") Long warehouseId, @Param("productId") Long productId);

    int insertUnit(@Param("inventoryId") Long inventoryId, @Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId, @Param("modelId") Long modelId,
            @Param("unitCode") String unitCode, @Param("serialNumber") String serialNumber,
            @Param("batchId") Long batchId,
            @Param("operator") Long operator);

    int refreshInventoryCounts(@Param("inventoryId") Long inventoryId, @Param("operator") Long operator);

    int updateInventoryPolicy(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("inventoryId") Long inventoryId, @Param("stockProtectionQty") Integer stockProtectionQty,
            @Param("calculationMode") Integer calculationMode, @Param("calculationPercent") BigDecimal calculationPercent,
            @Param("operator") Long operator);

    int updateBatch(@Param("batchId") Long batchId, @Param("successQuantity") int successQuantity,
            @Param("failedQuantity") int failedQuantity, @Param("status") int status,
            @Param("operator") Long operator);

    Map<String, Object> selectReservationByKeyForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("idempotencyKey") String idempotencyKey);

    Map<String, Object> selectReservationForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("reservationId") Long reservationId);

    Map<String, Object> selectReservationByNoForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("reservationNo") String reservationNo);

    /** 将外部库存预占绑定到已创建的正式订单，绑定过程保留预占状态供后续发货流程消费。 */
    int bindExternalReservationOrder(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("reservationId") Long reservationId, @Param("tokenId") Long tokenId,
            @Param("externalOrderNo") String externalOrderNo, @Param("orderId") Long orderId);

    List<Map<String, Object>> selectActiveReservationsByOrderForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("orderId") Long orderId);

    /** 查询已到期的外部库存预占，统一调度任务会锁定后释放其库存。 */
    List<Map<String, Object>> selectExpiredExternalReservationsForUpdate(@Param("limit") int limit);

    List<Map<String, Object>> selectReservationUnits(@Param("reservationId") Long reservationId);

    List<Map<String, Object>> selectAvailableUnitsForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId, @Param("quantity") int quantity);

    /** 查询订单套餐明细可绑定的逐件库存单元，候选范围包含当前订单已预占的设备。 */
    List<Map<String, Object>> selectOrderPackageDeviceOptions(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("orderId") Long orderId,
            @Param("productId") Long productId, @Param("warehouseId") Long warehouseId);

    /** 锁定并校验订单套餐待绑定的逐件库存单元。 */
    List<Map<String, Object>> selectOrderPackageDeviceOptionsForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("orderId") Long orderId,
            @Param("productId") Long productId, @Param("warehouseId") Long warehouseId,
            @Param("unitIds") List<Long> unitIds);

    Map<String, Object> selectInventoryForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId);

    int reserveQuantity(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("inventoryId") Long inventoryId, @Param("quantity") int quantity,
            @Param("operator") Long operator);

    int releaseQuantity(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("inventoryId") Long inventoryId, @Param("quantity") int quantity,
            @Param("operator") Long operator);

    int consumeQuantity(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("inventoryId") Long inventoryId, @Param("quantity") int quantity,
            @Param("operator") Long operator);

    Map<String, Object> selectUnitForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("unitId") Long unitId, @Param("unitCode") String unitCode);

    int updateUnitWarehouse(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("unitId") Long unitId, @Param("sourceWarehouseId") Long sourceWarehouseId,
            @Param("sourceInventoryId") Long sourceInventoryId, @Param("targetWarehouseId") Long targetWarehouseId,
            @Param("targetInventoryId") Long targetInventoryId, @Param("operator") Long operator);

    int insertTransferOrder(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("transferNo") String transferNo, @Param("fromWarehouseId") Long fromWarehouseId,
            @Param("toWarehouseId") Long toWarehouseId, @Param("unitId") Long unitId,
            @Param("transferStatus") Integer transferStatus, @Param("operator") Long operator,
            @Param("remark") String remark);

    /** 查询极速核销台扫码对应的库存单元和业务名称。 */
    Map<String, Object> selectVerificationUnit(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("warehouseId") Long warehouseId,
            @Param("unitCode") String unitCode, @Param("orderId") Long orderId);

    /** 锁定极速核销台扫码对应的库存单元，防止并发重复核销。 */
    Map<String, Object> selectVerificationUnitForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("warehouseId") Long warehouseId,
            @Param("unitCode") String unitCode, @Param("orderId") Long orderId);

    /** 查询库存流水幂等记录，避免重复提交改变状态。 */
    Map<String, Object> selectInventoryTransactionByIdempotency(@Param("memberId") Long memberId,
            @Param("idempotencyKey") String idempotencyKey);

    /** 写入库存核销流水。 */
    int insertInventoryTransaction(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("inventoryId") Long inventoryId, @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId, @Param("transactionType") Integer transactionType,
            @Param("changeQuantity") Integer changeQuantity, @Param("beforeQuantity") Integer beforeQuantity,
            @Param("afterQuantity") Integer afterQuantity, @Param("sourceOrderId") Long sourceOrderId,
            @Param("idempotencyKey") String idempotencyKey, @Param("reason") String reason,
            @Param("operator") Long operator);

    int countSerialNumber(@Param("memberId") Long memberId, @Param("serialNumber") String serialNumber,
            @Param("unitId") Long unitId);

    int updateUnitDetails(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("unitId") Long unitId, @Param("serialNumber") String serialNumber,
            @Param("unitStatus") Integer unitStatus, @Param("memorySpec") String memorySpec,
            @Param("purchaseCost") BigDecimal purchaseCost, @Param("conditionLevel") Integer conditionLevel,
            @Param("insuranceUntil") String insuranceUntil, @Param("insuranceAmount") BigDecimal insuranceAmount,
            @Param("serialPhotoUrl") String serialPhotoUrl,
            @Param("remark") String remark, @Param("operator") Long operator);

    int countUnitOrderReferences(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("unitId") Long unitId);

    int softDeleteUnit(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("unitId") Long unitId, @Param("operator") Long operator);

    int insertReservation(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("orderId") Long orderId, @Param("packageId") Long packageId,
            @Param("packageVersionId") Long packageVersionId, @Param("warehouseId") Long warehouseId,
            @Param("reservationNo") String reservationNo, @Param("idempotencyKey") String idempotencyKey,
            @Param("operator") Long operator, @Param("remark") String remark);

    /** 插入外部平台库存预占，外部请求不创建正式订单。 */
    int insertExternalReservation(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageId") Long packageId, @Param("packageVersionId") Long packageVersionId,
            @Param("warehouseId") Long warehouseId, @Param("reservationNo") String reservationNo,
            @Param("idempotencyKey") String idempotencyKey, @Param("externalTokenId") Long externalTokenId,
            @Param("externalOrderNo") String externalOrderNo, @Param("externalProductId") Long externalProductId,
            @Param("externalQuantity") Integer externalQuantity,
            @Param("externalPayloadHash") String externalPayloadHash,
            @Param("expireTime") java.time.LocalDateTime expireTime, @Param("remark") String remark);

    int insertReservationUnit(@Param("reservationId") Long reservationId, @Param("inventoryId") Long inventoryId,
            @Param("unitId") Long unitId, @Param("operator") Long operator);

    int insertReservationQuantity(@Param("reservationId") Long reservationId,
            @Param("inventoryId") Long inventoryId, @Param("productId") Long productId,
            @Param("quantity") int quantity, @Param("operator") Long operator);

    List<Map<String, Object>> selectReservationQuantities(@Param("reservationId") Long reservationId);

    int updateReservationQuantityStatus(@Param("reservationId") Long reservationId,
            @Param("fromStatus") Integer fromStatus, @Param("toStatus") Integer toStatus,
            @Param("operator") Long operator);

    int updateUnitStatus(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("unitId") Long unitId, @Param("fromStatus") Integer fromStatus,
            @Param("toStatus") Integer toStatus, @Param("operator") Long operator);

    int updateReservationUnitStatus(@Param("reservationId") Long reservationId,
            @Param("fromStatus") Integer fromStatus, @Param("toStatus") Integer toStatus,
            @Param("operator") Long operator);

    int updateReservationUnitStatusById(@Param("reservationUnitId") Long reservationUnitId,
            @Param("fromStatus") Integer fromStatus, @Param("toStatus") Integer toStatus,
            @Param("operator") Long operator);

    int updateReservationStatus(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("reservationId") Long reservationId, @Param("fromStatus") Integer fromStatus,
            @Param("toStatus") Integer toStatus, @Param("operator") Long operator, @Param("remark") String remark);
}
