package com.ruoyi.rentalbusiness.mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.ibatis.annotations.Param;

/** 商户库存告警和外部同步数据访问。所有商户查询都带租户、商户隔离条件。 */
public interface MerchantInventoryGovernanceMapper
{
    Long selectLastInsertId();

    /** 查询需要由后台任务刷新告警的租户和商户范围。 */
    List<Map<String, Object>> selectAlertScopes();

    List<Map<String, Object>> selectAlertRules(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("warehouseId") Long warehouseId, @Param("productId") Long productId,
            @Param("warehouseIds") Set<Long> warehouseIds, @Param("allWarehouseAccess") boolean allWarehouseAccess);

    Map<String, Object> selectAlertRule(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("ruleId") Long ruleId);

    int insertAlertRule(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("warehouseId") Long warehouseId, @Param("productId") Long productId,
            @Param("thresholdType") Integer thresholdType, @Param("thresholdValue") BigDecimal thresholdValue,
            @Param("notifyEnabled") Integer notifyEnabled, @Param("ruleStatus") Integer ruleStatus,
            @Param("operator") Long operator);

    int updateAlertRule(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("ruleId") Long ruleId, @Param("warehouseId") Long warehouseId, @Param("productId") Long productId,
            @Param("thresholdType") Integer thresholdType, @Param("thresholdValue") BigDecimal thresholdValue,
            @Param("notifyEnabled") Integer notifyEnabled, @Param("ruleStatus") Integer ruleStatus,
            @Param("operator") Long operator);

    int softDeleteAlertRule(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("ruleId") Long ruleId, @Param("operator") Long operator);

    int resolveAlertEvents(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("warehouseIds") Set<Long> warehouseIds, @Param("allWarehouseAccess") boolean allWarehouseAccess,
            @Param("operator") Long operator);

    int createAlertEvents(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("warehouseIds") Set<Long> warehouseIds, @Param("allWarehouseAccess") boolean allWarehouseAccess,
            @Param("operator") Long operator);

    List<Map<String, Object>> selectAlertEvents(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("alertStatus") Integer alertStatus, @Param("warehouseId") Long warehouseId,
            @Param("warehouseIds") Set<Long> warehouseIds, @Param("allWarehouseAccess") boolean allWarehouseAccess);

    Map<String, Object> selectAlertEvent(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("alertId") Long alertId);

    int acknowledgeAlertEvent(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("alertId") Long alertId, @Param("operator") Long operator);

    List<Map<String, Object>> selectSyncTokens(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId);

    /** 查询当前商户最近的外部库存同步调用记录。 */
    List<Map<String, Object>> selectSyncLogs(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("tokenId") Long tokenId);

    Map<String, Object> selectSyncToken(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("tokenId") Long tokenId);

    Map<String, Object> selectSyncTokenByHash(@Param("tokenHash") String tokenHash);

    int insertSyncToken(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("tokenName") String tokenName, @Param("tokenPrefix") String tokenPrefix,
            @Param("tokenHash") String tokenHash, @Param("scopes") String scopes,
            @Param("callbackUrl") String callbackUrl, @Param("expireTime") LocalDateTime expireTime,
            @Param("operator") Long operator);

    int revokeSyncToken(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("tokenId") Long tokenId, @Param("operator") Long operator);

    int touchSyncToken(@Param("tokenId") Long tokenId);

    Map<String, Object> selectSyncLog(@Param("tokenId") Long tokenId, @Param("requestId") String requestId);

    Map<String, Object> selectExternalOrderBindingForUpdate(@Param("tokenId") Long tokenId,
            @Param("externalOrderNo") String externalOrderNo);

    Map<String, Object> selectExternalOrderBindingByOrder(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("orderId") Long orderId);

    int insertExternalOrderBinding(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("tokenId") Long tokenId, @Param("reservationId") Long reservationId,
            @Param("orderId") Long orderId, @Param("externalOrderNo") String externalOrderNo,
            @Param("orderNo") String orderNo, @Param("payloadHash") String payloadHash,
            @Param("callbackUrl") String callbackUrl, @Param("callbackSecretHash") String callbackSecretHash);

    int insertExternalCallbackEvent(@Param("eventNo") String eventNo, @Param("bindingId") Long bindingId,
            @Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("orderId") Long orderId, @Param("callbackUrl") String callbackUrl,
            @Param("callbackSecretHash") String callbackSecretHash, @Param("eventType") Integer eventType,
            @Param("payloadJson") String payloadJson);

    List<Map<String, Object>> selectDueExternalCallbackEvents(@Param("limit") int limit);

    int claimExternalCallbackEvent(@Param("eventId") Long eventId, @Param("pendingStatus") Integer pendingStatus,
            @Param("retryingStatus") Integer retryingStatus, @Param("sendingStatus") Integer sendingStatus);

    int markExternalCallbackDelivered(@Param("eventId") Long eventId, @Param("sendingStatus") Integer sendingStatus,
            @Param("deliveredStatus") Integer deliveredStatus, @Param("responseStatus") Integer responseStatus);

    int markExternalCallbackFailed(@Param("eventId") Long eventId, @Param("sendingStatus") Integer sendingStatus,
            @Param("nextStatus") Integer nextStatus, @Param("attemptCount") Integer attemptCount,
            @Param("nextRetryTime") LocalDateTime nextRetryTime, @Param("responseStatus") Integer responseStatus,
            @Param("errorMessage") String errorMessage);

    int insertSyncLog(@Param("tokenId") Long tokenId, @Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("requestId") String requestId,
            @Param("requestPath") String requestPath, @Param("responseStatus") Integer responseStatus,
            @Param("itemCount") Integer itemCount, @Param("resultCode") String resultCode,
            @Param("resultMessage") String resultMessage, @Param("latencyMs") Integer latencyMs);

    List<Map<String, Object>> selectExternalSnapshot(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId);

    Long selectExternalSnapshotVersion(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId);

    Map<String, Object> selectWarehouse(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("warehouseId") Long warehouseId);

    Map<String, Object> selectProduct(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("productId") Long productId);

    Map<String, Object> selectInventoryPolicy(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("inventoryId") Long inventoryId);
}
