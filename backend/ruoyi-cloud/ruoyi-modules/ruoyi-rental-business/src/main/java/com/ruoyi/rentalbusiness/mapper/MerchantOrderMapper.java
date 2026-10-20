package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.rentalbusiness.domain.MerchantOrderRow;
import com.ruoyi.rentalbusiness.domain.WeatherRiskOrderCandidate;
import com.ruoyi.rentalbusiness.model.MerchantOrderQuery;
import com.ruoyi.rentalbusiness.model.MerchantOrderCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderNoteVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderEditLogVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderSummaryVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderRecipientUpdateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderReturnInspectionCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderReturnInspectionVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderReturnReadinessVO;
import com.ruoyi.rentalbusiness.model.MerchantAuditReferenceModel;
import com.ruoyi.rentalbusiness.model.MerchantOrderAllianceLinkModel;
import java.util.Map;

/** 商户订单查询数据访问。所有查询必须带租户、商户和软删除条件。 */
public interface MerchantOrderMapper
{
    /** 查询当前商户订单的最小支付目标。 */
    Map<String, Object> selectMerchantOrderPaymentTarget(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("orderId") Long orderId);

    /** 仅将订单付款状态从未支付推进为已支付，保证通知重复时幂等。 */
    int updateMerchantOrderPayStatus(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("orderId") Long orderId, @Param("fromStatus") Integer fromStatus,
            @Param("toStatus") Integer toStatus, @Param("cancelledStatus") Integer cancelledStatus);
    /** 查询当前商户指定订单详情，SQL 层同时限制租户、商户和软删除范围。 */
    MerchantOrderRow selectMerchantOrderById(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("orderId") Long orderId);

    /** 查询当前商户指定订单的批注历史，按创建时间倒序返回。 */
    List<MerchantOrderNoteVO> selectMerchantOrderNotes(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("orderId") Long orderId);

    /** 新增当前商户订单批注，订单归属由 SQL 条件和服务层共同校验。 */
    int insertMerchantOrderNote(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("orderId") Long orderId, @Param("noteContent") String noteContent,
            @Param("noteType") Integer noteType, @Param("operatorId") Long operatorId);

    /** 查询当前商户订单字段编辑历史，按创建时间倒序返回。 */
    List<MerchantOrderEditLogVO> selectMerchantOrderEditLogs(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("orderId") Long orderId);

    /** 写入当前商户订单字段变更记录，与订单更新使用同一事务。 */
    int insertMerchantOrderEditLog(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("orderId") Long orderId, @Param("fieldName") String fieldName,
            @Param("beforeValue") String beforeValue, @Param("afterValue") String afterValue,
            @Param("beforeDisplayValue") String beforeDisplayValue, @Param("afterDisplayValue") String afterDisplayValue,
            @Param("changeReason") String changeReason, @Param("operatorId") Long operatorId,
            @Param("operatorName") String operatorName, @Param("operatorIp") String operatorIp);

    /** 批量解析当前商户仓库名称，供订单审计展示快照使用。 */
    List<MerchantAuditReferenceModel> selectWarehouseNames(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("referenceIds") List<Long> referenceIds);

    /** 批量解析当前商户可用订单渠道名称，供订单审计展示快照使用。 */
    List<MerchantAuditReferenceModel> selectOrderChannelNames(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("referenceIds") List<Long> referenceIds);

    /** 校验订单渠道是否为启用的平台渠道或当前商户自定义渠道。 */
    int countUsableOrderChannel(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("channelId") Long channelId);

    /** 插入商户订单主表，订单归属由服务端参数补齐。 */
    int insertMerchantOrder(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("command") MerchantOrderCreateCommand command, @Param("operatorId") Long operatorId);

    /** 更新当前商户订单的客户和租期信息。 */
    int updateMerchantOrder(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("command") MerchantOrderCreateCommand command, @Param("operatorId") Long operatorId);

    /** 在当前状态校验下仅更新订单备货状态，不覆盖订单其他业务字段。 */
    int updateMerchantOrderStatus(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("orderId") Long orderId, @Param("fromStatus") Integer fromStatus,
            @Param("toStatus") Integer toStatus, @Param("operatorId") Long operatorId);

    /** 查询当前商户订单最近一次归还验机记录。 */
    MerchantOrderReturnInspectionVO selectLatestReturnInspection(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("orderId") Long orderId);

    /** 查询订单套餐中已绑定逐件设备的归还数量，避免部分归还时提前验机完成。 */
    MerchantOrderReturnReadinessVO selectOrderReturnReadiness(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("orderId") Long orderId);

    /** 保存当前商户订单归还验机结果，记录只追加不覆盖。 */
    int insertReturnInspection(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("orderId") Long orderId, @Param("command") MerchantOrderReturnInspectionCommand command,
            @Param("operatorId") Long operatorId);

    /** 查询当前商户订单对应的联盟原订单，关系表是子订单识别的可信来源。 */
    MerchantOrderAllianceLinkModel selectAllianceSourceOrderLink(@Param("tenantId") Long tenantId,
            @Param("targetMemberId") Long targetMemberId, @Param("childOrderId") Long childOrderId);

    /** 将联盟子订单的履约状态同步到原订单，不复用原订单的状态流转校验。 */
    int updateAllianceSourceOrderStatus(@Param("tenantId") Long tenantId,
            @Param("sourceMemberId") Long sourceMemberId, @Param("sourceOrderId") Long sourceOrderId,
            @Param("orderStatus") Integer orderStatus, @Param("operatorId") Long operatorId);

    /** 判断当前商户订单是否已有已接受联盟子订单，原订单据此进入只读状态。 */
    int countAcceptedAllianceChildren(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("orderId") Long orderId);

    /** 更新当前订单的套餐明细，保证编辑表单与订单列表使用同一套餐编号。 */
    int updateMerchantOrderItemPackage(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("command") MerchantOrderCreateCommand command, @Param("operatorId") Long operatorId);

    /** 查询当前订单最新的发货物流单，重复发货操作时更新该记录。 */
    Long selectLatestMerchantOrderLogisticsId(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("orderId") Long orderId,
            @Param("direction") Integer direction);

    /** 更新当前商户订单的物流公司和快递单号。 */
    int updateMerchantOrderLogistics(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("logisticsId") Long logisticsId,
            @Param("warehouseId") Long warehouseId, @Param("carrier") String carrier,
            @Param("trackingNo") String trackingNo, @Param("operatorId") Long operatorId);

    /** 仅更新当前订单物流记录的快递信息，不重置物流路线状态或订单状态。 */
    int updateMerchantOrderLogisticsInfo(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("logisticsId") Long logisticsId,
            @Param("carrier") String carrier, @Param("trackingNo") String trackingNo,
            @Param("operatorId") Long operatorId);

    /** 插入当前商户订单的物流记录。 */
    int insertMerchantOrderLogistics(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("orderId") Long orderId,
            @Param("warehouseId") Long warehouseId, @Param("direction") Integer direction,
            @Param("carrier") String carrier, @Param("trackingNo") String trackingNo,
            @Param("operatorId") Long operatorId);

    /** 插入订单商品明细，明细归属与订单保持一致。 */
    int insertMerchantOrderItem(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("command") MerchantOrderCreateCommand command, @Param("operatorId") Long operatorId,
            @Param("itemStatus") Integer itemStatus);

    /** 更新订单自身的套餐详情快照；套餐取消时传入 null 清空快照。 */
    int updateOrderPackageDetails(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("orderId") Long orderId, @Param("packageDetailsJson") String packageDetailsJson,
            @Param("operatorId") Long operatorId);
    /**
     * 按当前商户上下文分页查询订单工作台所需字段。
     *
     * @param tenantId 可信租户唯一编号
     * @param merchantId 可信商户唯一编号
     * @param query 已覆盖仓库范围的商户订单查询条件
     * @return 当前用户可见的订单查询行
     */
    List<MerchantOrderRow> selectMerchantOrderList(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("query") MerchantOrderQuery query);

    /**
     * 使用独立的轻量筛选 SQL 查询订单总数，避免分页插件解析套餐明细投影。
     *
     * @param tenantId 可信租户唯一编号
     * @param merchantId 可信商户唯一编号
     * @param query 已覆盖仓库范围的商户订单查询条件
     * @return 当前筛选条件下的订单总数
     */
    long selectMerchantOrderCount(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("query") MerchantOrderQuery query);

    /** 查询发货日为指定日期且仍待发货/已备货的订单，供天气风险批处理使用。 */
    List<WeatherRiskOrderCandidate> selectWeatherRiskCandidates(@Param("shipDate") java.sql.Date shipDate,
            @Param("orderStatuses") List<Integer> orderStatuses);

    /** 按天气批处理结果更新仍待履约订单的风险标识。 */
    int updateWeatherRiskFlag(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("orderId") Long orderId, @Param("shipDate") java.sql.Date shipDate,
            @Param("orderStatuses") List<Integer> orderStatuses, @Param("weatherRiskFlag") Integer weatherRiskFlag,
            @Param("operatorId") Long operatorId);

    /** 查询当前商户订单中心顶部概览，忽略页面筛选条件但保留仓库权限范围。 */
    MerchantOrderSummaryVO selectMerchantOrderSummary(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("query") MerchantOrderQuery query);

    /** 更新当前商户指定订单的客户和收件人快照。 */
    int updateMerchantOrderRecipient(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("command") MerchantOrderRecipientUpdateCommand command, @Param("operatorId") Long operatorId);

    /** 追加当前商户订单转寄前后的收件人快照。 */
    int insertOrderForwardRecord(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("orderId") Long orderId, @Param("oldRecipientName") String oldRecipientName,
            @Param("oldRecipientPhone") String oldRecipientPhone,
            @Param("oldRecipientAddress") String oldRecipientAddress,
            @Param("newRecipientName") String newRecipientName,
            @Param("newRecipientPhone") String newRecipientPhone,
            @Param("newRecipientAddress") String newRecipientAddress,
            @Param("remark") String remark, @Param("operatorId") Long operatorId);
}
