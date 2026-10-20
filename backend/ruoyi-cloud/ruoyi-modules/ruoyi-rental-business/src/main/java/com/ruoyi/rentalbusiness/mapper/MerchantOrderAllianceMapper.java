package com.ruoyi.rentalbusiness.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.rentalbusiness.model.MerchantOrderAllianceTransferCommand;

/** 商户订单联盟转单记录数据访问。 */
public interface MerchantOrderAllianceMapper
{
    /** 写入一个指定接收商户的脱敏转单任务。 */
    int insertTransfer(@Param("tenantId") Long tenantId, @Param("sourceMemberId") Long sourceMemberId,
            @Param("targetMemberId") Long targetMemberId, @Param("command") MerchantOrderAllianceTransferCommand command,
            @Param("maskedSnapshotJson") String maskedSnapshotJson, @Param("allianceId") Long allianceId,
            @Param("operatorId") Long operatorId);

    /** 按接收方共享范围、套餐和可用库存筛选转单候选商户。 */
    List<Long> selectEligibleTargetMemberIds(@Param("tenantId") Long tenantId,
            @Param("allianceId") Long allianceId, @Param("sourceMemberId") Long sourceMemberId,
            @Param("sourceOrderId") Long sourceOrderId, @Param("memberIds") List<Long> memberIds);

    List<Map<String, Object>> selectTasks(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("memberIds") List<Long> memberIds, @Param("status") Integer status,
            @Param("viewCode") String viewCode, @Param("keyword") String keyword,
            @Param("publicPoolStatus") Integer publicPoolStatus, @Param("deliveryCity") String deliveryCity,
            @Param("leaseStartDate") LocalDate leaseStartDate);
    Map<String, Object> selectTaskSummary(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("memberIds") List<Long> memberIds, @Param("viewCode") String viewCode,
            @Param("pendingStatus") Integer pendingStatus, @Param("acceptedStatus") Integer acceptedStatus,
            @Param("fulfillmentStatus") Integer fulfillmentStatus, @Param("completedStatus") Integer completedStatus,
            @Param("publicPoolStatus") Integer publicPoolStatus);
    Map<String, Object> selectTask(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("allianceOrderId") Long allianceOrderId);
    int updateChildOrderWarehouse(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("orderId") Long orderId, @Param("warehouseId") Long warehouseId,
            @Param("operatorId") Long operatorId);
    int updateTargetWarehouse(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("allianceOrderId") Long allianceOrderId, @Param("warehouseId") Long warehouseId,
            @Param("operatorId") Long operatorId);
    Map<String, Object> selectChildOrderPackage(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("orderId") Long orderId);
    List<Map<String, Object>> selectChildOrderFulfillment(@Param("tenantId") Long tenantId,
            @Param("sourceMemberId") Long sourceMemberId, @Param("targetMemberId") Long targetMemberId,
            @Param("sourceOrderId") Long sourceOrderId, @Param("warehouseId") Long warehouseId,
            @Param("allianceId") Long allianceId);
    int updateTaskStatus(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("allianceOrderId") Long allianceOrderId, @Param("fromStatus") Integer fromStatus,
            @Param("toStatus") Integer toStatus, @Param("operatorId") Long operatorId);
    int reassignTask(@Param("tenantId") Long tenantId, @Param("currentMemberId") Long currentMemberId,
            @Param("targetMemberId") Long targetMemberId, @Param("allianceOrderId") Long allianceOrderId,
            @Param("operatorId") Long operatorId);
    int cancelTask(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("allianceOrderId") Long allianceOrderId, @Param("operatorId") Long operatorId);
    int claimPublicPoolTask(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("allianceOrderId") Long allianceOrderId, @Param("fromStatus") Integer fromStatus,
            @Param("toStatus") Integer toStatus, @Param("operatorId") Long operatorId);
    int publishPublicPool(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("allianceOrderId") Long allianceOrderId, @Param("fromStatus") Integer fromStatus,
            @Param("toStatus") Integer toStatus, @Param("operatorId") Long operatorId);
    int insertChildOrder(@Param("tenantId") Long tenantId, @Param("sourceMemberId") Long sourceMemberId,
            @Param("targetMemberId") Long targetMemberId, @Param("sourceOrderId") Long sourceOrderId,
            @Param("orderNo") String orderNo, @Param("operatorId") Long operatorId,
            @Param("warehouseId") Long warehouseId);
    int insertChildOrderItems(@Param("tenantId") Long tenantId, @Param("sourceMemberId") Long sourceMemberId,
            @Param("targetMemberId") Long targetMemberId, @Param("sourceOrderId") Long sourceOrderId,
            @Param("childOrderId") Long childOrderId, @Param("operatorId") Long operatorId);
    Long selectLastInsertId();
    int bindChildOrder(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("allianceOrderId") Long allianceOrderId, @Param("childOrderId") Long childOrderId,
            @Param("warehouseId") Long warehouseId, @Param("snapshot") String snapshot,
            @Param("operatorId") Long operatorId);

    int insertAllianceItemSnapshot(@Param("tenantId") Long tenantId, @Param("allianceOrderId") Long allianceOrderId,
            @Param("sourceMemberId") Long sourceMemberId, @Param("sourceOrderId") Long sourceOrderId,
            @Param("operatorId") Long operatorId);

    int insertEvent(@Param("tenantId") Long tenantId, @Param("allianceOrderId") Long allianceOrderId,
            @Param("eventType") Integer eventType, @Param("fromStatus") Integer fromStatus,
            @Param("toStatus") Integer toStatus, @Param("operatorMemberId") Long operatorMemberId,
            @Param("operatorUserId") Long operatorUserId, @Param("eventDataJson") String eventDataJson);

    int insertSettlement(@Param("tenantId") Long tenantId, @Param("allianceOrderId") Long allianceOrderId,
            @Param("operatorId") Long operatorId);

    List<Map<String, Object>> selectChildOrderStatusChanges(@Param("tenantId") Long tenantId);

    int updateStatusFromChildOrder(@Param("tenantId") Long tenantId, @Param("allianceOrderId") Long allianceOrderId,
            @Param("fromStatus") Integer fromStatus, @Param("toStatus") Integer toStatus,
            @Param("operatorId") Long operatorId);
}
