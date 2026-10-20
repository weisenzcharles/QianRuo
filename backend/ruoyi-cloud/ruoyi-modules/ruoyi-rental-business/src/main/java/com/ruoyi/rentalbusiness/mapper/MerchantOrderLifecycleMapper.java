package com.ruoyi.rentalbusiness.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.rentalbusiness.domain.MerchantOrderLifecycleCandidate;
import com.ruoyi.rentalbusiness.model.MerchantOrderAllianceLinkModel;

/** 订单履约后台任务数据访问，所有写入均带当前状态条件。 */
public interface MerchantOrderLifecycleMapper
{
    /** 查询达到自动推进条件的订单最小投影。 */
    List<MerchantOrderLifecycleCandidate> selectCandidates(
            @Param("shippingStatuses") List<Integer> shippingStatuses,
            @Param("shipOverdueStatuses") List<Integer> shipOverdueStatuses,
            @Param("rentingStatuses") List<Integer> rentingStatuses,
            @Param("now") LocalDateTime now, @Param("businessDate") LocalDate businessDate);

    /** 在原状态仍匹配时推进订单，并同步订单时效状态。 */
    int updateOrderStatus(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("orderId") Long orderId, @Param("fromStatus") Integer fromStatus,
            @Param("toStatus") Integer toStatus, @Param("timeEffectStatus") Integer timeEffectStatus,
            @Param("operatorId") Long operatorId);

    /** 将订单下仍有效的明细状态同步为订单状态。 */
    int updateOrderItemStatus(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("orderId") Long orderId, @Param("itemStatus") Integer itemStatus,
            @Param("operatorId") Long operatorId);

    /** 查询订单是否为联盟子订单，并取得原订单归属。 */
    MerchantOrderAllianceLinkModel selectAllianceSourceOrderLink(@Param("tenantId") Long tenantId,
            @Param("targetMemberId") Long targetMemberId, @Param("childOrderId") Long childOrderId);

    /** 将联盟子订单状态和时效状态同步到原订单。 */
    int updateAllianceSourceOrderStatus(@Param("tenantId") Long tenantId,
            @Param("sourceMemberId") Long sourceMemberId, @Param("sourceOrderId") Long sourceOrderId,
            @Param("orderStatus") Integer orderStatus, @Param("timeEffectStatus") Integer timeEffectStatus,
            @Param("operatorId") Long operatorId);

    /** 写入系统自动状态推进审计记录。 */
    int insertOrderEditLog(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("orderId") Long orderId, @Param("fieldName") String fieldName,
            @Param("beforeValue") String beforeValue, @Param("afterValue") String afterValue,
            @Param("beforeDisplayValue") String beforeDisplayValue,
            @Param("afterDisplayValue") String afterDisplayValue,
            @Param("changeReason") String changeReason, @Param("operatorId") Long operatorId,
            @Param("operatorName") String operatorName);
}
