package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 订单渠道、库存工作流、文件配额和协议版本数据访问接口。
 *
 * <p>所有商户业务方法必须接收可信 tenantId/memberId；平台协议方法使用固定的全局协议表，
 * 但商户确认记录仍必须带商户范围，避免不同商户互相读取确认历史。</p>
 */
public interface RentalWorkflowMapper
{
    /** 查询平台联盟渠道与当前商户自定义渠道。 */
    List<Map<String, Object>> selectVisibleChannels(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("keyword") String keyword, @Param("status") Integer status);

    /** 查询当前商户可用渠道或当前商户自有渠道。 */
    Map<String, Object> selectChannel(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("channelId") Long channelId, @Param("ownOnly") boolean ownOnly);

    /** 新增平台联盟渠道或商户自定义渠道。 */
    int insertChannel(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("code") String code, @Param("name") String name, @Param("type") String type,
            @Param("scope") String scope, @Param("status") Integer status, @Param("sortNo") Integer sortNo,
            @Param("operatorId") Long operatorId, @Param("remark") String remark);

    /** 修改自有渠道。 */
    int updateChannel(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("channelId") Long channelId, @Param("name") String name, @Param("status") Integer status,
            @Param("sortNo") Integer sortNo, @Param("operatorId") Long operatorId, @Param("remark") String remark);

    /** 修改平台联盟渠道。 */
    int updatePlatformChannel(@Param("channelId") Long channelId, @Param("name") String name,
            @Param("status") Integer status, @Param("sortNo") Integer sortNo,
            @Param("operatorId") Long operatorId, @Param("remark") String remark);

    /** 软删除商户自有渠道。 */
    int softDeleteChannel(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("channelId") Long channelId, @Param("operatorId") Long operatorId);

    /** 软删除平台联盟渠道。 */
    int softDeletePlatformChannel(@Param("channelId") Long channelId, @Param("operatorId") Long operatorId);

    /** 查询平台渠道被全部商户套餐绑定或订单使用的次数。 */
    int countPlatformChannelReferences(@Param("channelId") Long channelId);

    /** 查询渠道是否仍被套餐绑定或订单使用。 */
    int countChannelReferences(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("channelId") Long channelId);

    /** 查询套餐渠道绑定。 */
    List<Map<String, Object>> selectPackageChannels(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("packageId") Long packageId);

    /** 查询套餐是否属于当前商户。 */
    Map<String, Object> selectPackage(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageId") Long packageId);

    /** 查询当前默认套餐渠道。 */
    Map<String, Object> selectDefaultPackageChannel(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("packageId") Long packageId);

    /** 清除套餐默认渠道标识。 */
    int clearPackageDefault(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageId") Long packageId, @Param("operatorId") Long operatorId);

    /** 新增或恢复套餐渠道绑定。 */
    int insertPackageChannel(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageId") Long packageId, @Param("channelId") Long channelId,
            @Param("isDefault") Integer isDefault, @Param("operatorId") Long operatorId);

    /** 按幂等键查询套餐渠道转移。 */
    Map<String, Object> selectPackageTransferByKey(@Param("memberId") Long memberId,
            @Param("idempotencyKey") String idempotencyKey);

    /** 写入套餐渠道转移历史。 */
    int insertPackageTransfer(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("packageId") Long packageId, @Param("fromChannelId") Long fromChannelId,
            @Param("toChannelId") Long toChannelId, @Param("idempotencyKey") String idempotencyKey,
            @Param("reason") String reason, @Param("operatorId") Long operatorId,
            @Param("operatorName") String operatorName);

    /** 查询订单及其当前渠道。 */
    Map<String, Object> selectOrder(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("orderId") Long orderId);

    /** 按幂等键查询订单渠道转移。 */
    Map<String, Object> selectOrderTransferByKey(@Param("memberId") Long memberId, @Param("orderId") Long orderId,
            @Param("idempotencyKey") String idempotencyKey);

    /** 在订单版本校验下更新订单渠道，防止并发转移覆盖。 */
    int updateOrderChannel(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("orderId") Long orderId, @Param("channelId") Long channelId,
            @Param("version") Integer version, @Param("operatorId") Long operatorId);

    /** 写入订单渠道转移历史。 */
    int insertOrderTransfer(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("orderId") Long orderId, @Param("fromChannelId") Long fromChannelId,
            @Param("toChannelId") Long toChannelId, @Param("idempotencyKey") String idempotencyKey,
            @Param("reason") String reason, @Param("operatorId") Long operatorId,
            @Param("operatorName") String operatorName);

    /** 确保资源池并发锁行存在。 */
    int ensurePoolLock(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("poolId") Long poolId, @Param("warehouseId") Long warehouseId);

    /** 锁定资源池和仓库组合，所有预占计算必须先取得该锁。 */
    Map<String, Object> selectPoolLockForUpdate(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("poolId") Long poolId, @Param("warehouseId") Long warehouseId);

    /** 查询套餐当前生效版本的资源池组件。 */
    List<Map<String, Object>> selectActivePackageComponents(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("packageId") Long packageId);

    /** 查询资源池在仓库内的总可用库存。 */
    int selectPoolAvailable(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("poolId") Long poolId, @Param("warehouseId") Long warehouseId);

    /** 查询资源池在仓库内尚未释放的预占数量。 */
    int selectPoolReserved(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("poolId") Long poolId, @Param("warehouseId") Long warehouseId);

    /** 按幂等键锁定预占主记录。 */
    Map<String, Object> selectReservationByKeyForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("idempotencyKey") String idempotencyKey);

    /** 新增库存预占主记录。 */
    int insertReservation(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("reservationNo") String reservationNo, @Param("orderId") Long orderId,
            @Param("packageId") Long packageId, @Param("warehouseId") Long warehouseId,
            @Param("quantity") Integer quantity, @Param("idempotencyKey") String idempotencyKey,
            @Param("operator") String operator);

    /** 锁定指定预占主记录。 */
    Map<String, Object> selectReservationForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("reservationId") Long reservationId);

    /** 查询预占资源池明细。 */
    List<Map<String, Object>> selectReservationItems(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("reservationId") Long reservationId);

    /** 新增预占资源池明细。 */
    int insertReservationItem(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("reservationId") Long reservationId, @Param("poolId") Long poolId,
            @Param("warehouseId") Long warehouseId, @Param("requiredQty") Integer requiredQty,
            @Param("operator") String operator);

    /** 以版本号更新预占主状态。 */
    int updateReservationStatus(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("reservationId") Long reservationId, @Param("fromStatus") String fromStatus,
            @Param("toStatus") String toStatus, @Param("reason") String reason, @Param("operator") String operator);

    /** 以版本号更新预占明细状态。 */
    int updateReservationItemStatus(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("reservationId") Long reservationId, @Param("fromStatus") String fromStatus,
            @Param("toStatus") String toStatus, @Param("operator") String operator);

    /** 查询数量库存状态余额并加行锁。 */
    Map<String, Object> selectBalanceForUpdate(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("productId") Long productId, @Param("warehouseId") Long warehouseId,
            @Param("stockStatus") String stockStatus);

    /** 新增数量库存状态余额。 */
    int insertBalance(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("productId") Long productId, @Param("warehouseId") Long warehouseId,
            @Param("stockStatus") String stockStatus, @Param("quantity") Integer quantity,
            @Param("operator") String operator, @Param("remark") String remark);

    /** 更新数量库存状态余额。 */
    int updateBalance(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("balanceId") Long balanceId, @Param("quantity") Integer quantity,
            @Param("operator") String operator, @Param("remark") String remark);

    /** 锁定逐件库存单元。 */
    Map<String, Object> selectInventoryUnitForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("unitId") Long unitId, @Param("unitCode") String unitCode);

    /** 锁定历史 SN 实例，确保旧库存数据也遵守统一状态机。 */
    Map<String, Object> selectAssetSnForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("snId") Long snId, @Param("snCode") String snCode);

    /** 更新逐件库存单元状态。 */
    int updateInventoryUnitStatus(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("unitId") Long unitId, @Param("fromStatus") String fromStatus, @Param("toStatus") String toStatus,
            @Param("operator") String operator);

    /** 查询资源池中可消费的数量库存和逐件库存，并按行加锁。 */
    List<Map<String, Object>> selectPoolStocksForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("poolId") Long poolId, @Param("warehouseId") Long warehouseId);

    /** 更新资产 SN 状态。 */
    int updateAssetSnStatus(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("snId") Long snId, @Param("fromStatus") String fromStatus, @Param("toStatus") String toStatus,
            @Param("operator") String operator);

    /** 写入库存状态机记录。 */
    int insertInventoryTransition(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("productId") Long productId, @Param("unitId") Long unitId, @Param("warehouseId") Long warehouseId,
            @Param("snId") Long snId,
            @Param("fromStatus") String fromStatus, @Param("toStatus") String toStatus,
            @Param("changeQty") Integer changeQty, @Param("idempotencyKey") String idempotencyKey,
            @Param("reason") String reason, @Param("operatorId") Long operatorId,
            @Param("operatorName") String operatorName);

    /** 写入库存状态流水。 */
    int insertInventoryLedger(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("warehouseId") Long warehouseId, @Param("productId") Long productId,
            @Param("changeType") String changeType, @Param("fromStatus") String fromStatus,
            @Param("toStatus") String toStatus, @Param("changeQty") Integer changeQty,
            @Param("refType") String refType, @Param("refId") String refId, @Param("reason") String reason,
            @Param("operator") String operator);

    /** 查询幂等请求并加行锁。 */
    Map<String, Object> selectIdempotentForUpdate(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("scope") String scope, @Param("idempotencyKey") String idempotencyKey);

    /** 创建幂等请求占位记录，重复请求不产生第二条记录。 */
    int insertIdempotentIgnore(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("scope") String scope, @Param("idempotencyKey") String idempotencyKey,
            @Param("requestHash") String requestHash, @Param("expireTime") java.util.Date expireTime);

    /** 更新幂等请求结果。 */
    int completeIdempotent(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("scope") String scope, @Param("idempotencyKey") String idempotencyKey,
            @Param("businessId") Long businessId);

    /** 锁定商户文件配额账户。 */
    Map<String, Object> selectQuotaForUpdate(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId);

    /** 创建默认文件配额账户。 */
    int insertQuota(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("quotaBytes") Long quotaBytes);

    /** 原子增加已使用文件配额。 */
    int increaseQuota(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("deltaBytes") Long deltaBytes);

    /** 写入文件配额审计流水。 */
    int insertQuotaLedger(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("fileName") String fileName, @Param("fileUrl") String fileUrl, @Param("fileSize") Long fileSize,
            @Param("changeType") String changeType, @Param("refType") String refType,
            @Param("refId") String refId, @Param("operator") String operator);

    /** 查询文件配额审计流水。 */
    List<Map<String, Object>> selectQuotaLedger(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId);

    /** 查询协议版本列表。 */
    List<Map<String, Object>> selectProtocols(@Param("protocolKey") String protocolKey,
            @Param("publishedOnly") boolean publishedOnly);

    /** 查询协议版本详情。 */
    Map<String, Object> selectProtocol(@Param("protocolId") Long protocolId);

    /** 查询协议键当前最大版本号。 */
    int selectMaxProtocolVersion(@Param("protocolKey") String protocolKey);

    /** 新增协议版本。 */
    int insertProtocol(@Param("protocolKey") String protocolKey, @Param("versionNo") Integer versionNo,
            @Param("title") String title, @Param("content") String content, @Param("contentHash") String contentHash,
            @Param("status") String status, @Param("operator") String operator, @Param("remark") String remark);

    /** 修改协议草稿。 */
    int updateProtocol(@Param("protocolId") Long protocolId, @Param("title") String title,
            @Param("content") String content, @Param("contentHash") String contentHash,
            @Param("status") String status, @Param("operator") String operator, @Param("remark") String remark);

    /** 按协议键下线其他已发布版本。 */
    int retireProtocols(@Param("protocolKey") String protocolKey, @Param("operator") String operator);

    /** 查询当前商户用户是否确认协议版本。 */
    Map<String, Object> selectAcceptance(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("userId") Long userId, @Param("protocolId") Long protocolId, @Param("versionNo") Integer versionNo);

    /** 写入协议确认记录。 */
    int insertAcceptance(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("userId") Long userId, @Param("protocolId") Long protocolId, @Param("protocolKey") String protocolKey,
            @Param("versionNo") Integer versionNo, @Param("contentHash") String contentHash,
            @Param("operator") String operator);
}
