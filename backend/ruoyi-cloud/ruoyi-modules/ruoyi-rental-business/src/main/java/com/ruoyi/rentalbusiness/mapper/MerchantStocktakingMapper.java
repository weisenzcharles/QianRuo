package com.ruoyi.rentalbusiness.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.ibatis.annotations.Param;

/** 商户库存盘点数据访问。 */
public interface MerchantStocktakingMapper
{
    Long selectLastInsertId();

    List<Map<String, Object>> selectStocktakingList(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("warehouseId") Long warehouseId,
            @Param("status") Integer status, @Param("mode") Integer mode, @Param("keyword") String keyword,
            @Param("beginDate") LocalDate beginDate, @Param("endDate") LocalDate endDate,
            @Param("warehouseIds") Set<Long> warehouseIds, @Param("allWarehouseAccess") boolean allWarehouseAccess);

    Map<String, Object> selectSummary(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("warehouseId") Long warehouseId, @Param("warehouseIds") Set<Long> warehouseIds,
            @Param("allWarehouseAccess") boolean allWarehouseAccess);

    Map<String, Object> selectStocktaking(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("stocktakingId") Long stocktakingId);

    Map<String, Object> selectStocktakingForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("stocktakingId") Long stocktakingId);

    List<Map<String, Object>> selectScopeOptions(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("warehouseId") Long warehouseId,
            @Param("scope") Integer scope);

    List<Map<String, Object>> selectItems(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("stocktakingId") Long stocktakingId);

    int insertStocktaking(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("warehouseId") Long warehouseId, @Param("stocktakingNo") String stocktakingNo,
            @Param("stocktakingDate") LocalDate stocktakingDate, @Param("mode") Integer mode,
            @Param("scope") Integer scope, @Param("classId") Long classId, @Param("modelId") Long modelId,
            @Param("scopeLabel") String scopeLabel, @Param("expectedQuantity") Integer expectedQuantity,
            @Param("operatorId") Long operatorId, @Param("remark") String remark);

    String selectScopeLabel(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("scope") Integer scope, @Param("classId") Long classId, @Param("modelId") Long modelId);

    Integer selectExpectedQuantity(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("warehouseId") Long warehouseId, @Param("mode") Integer mode,
            @Param("scope") Integer scope, @Param("classId") Long classId, @Param("modelId") Long modelId);

    int insertManualSnapshot(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("stocktakingId") Long stocktakingId, @Param("warehouseId") Long warehouseId,
            @Param("scope") Integer scope, @Param("classId") Long classId, @Param("modelId") Long modelId);

    int countDuplicateScan(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("stocktakingId") Long stocktakingId, @Param("scanCode") String scanCode);

    Map<String, Object> selectUnitByCode(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("code") String code);

    int insertScanItem(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("stocktakingId") Long stocktakingId, @Param("unitId") Long unitId,
            @Param("scanCode") String scanCode, @Param("systemStatus") Integer systemStatus,
            @Param("scanResult") Integer scanResult, @Param("matchedRefType") String matchedRefType,
            @Param("matchedRefId") Long matchedRefId, @Param("productName") String productName,
            @Param("modelName") String modelName, @Param("remark") String remark);

    int incrementAbnormalCount(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("stocktakingId") Long stocktakingId, @Param("operatorId") Long operatorId);

    Map<String, Object> selectItemForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("stocktakingId") Long stocktakingId,
            @Param("itemId") Long itemId);

    int updateCountedQuantity(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("stocktakingId") Long stocktakingId, @Param("itemId") Long itemId,
            @Param("countedQuantity") Integer countedQuantity);

    int countUncountedItems(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("stocktakingId") Long stocktakingId);

    int countDifferentItems(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("stocktakingId") Long stocktakingId);

    int updateStatus(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("stocktakingId") Long stocktakingId, @Param("fromStatus") Integer fromStatus,
            @Param("toStatus") Integer toStatus, @Param("operatorId") Long operatorId);
}
