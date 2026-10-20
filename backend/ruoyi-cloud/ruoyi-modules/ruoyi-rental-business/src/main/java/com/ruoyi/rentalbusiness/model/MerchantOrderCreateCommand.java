package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/** 商户录入订单命令，仅承载业务字段，不接受租户、商户和操作人归属字段。 */
public class MerchantOrderCreateCommand
{
    public String externalOrderNo;
    public Long channelId;
    public String shopName;
    public String customerName;
    /** 客户昵称快照。 */
    public String customerNickname;
    public String customerPhone;
    /** 收件人姓名快照。 */
    public String recipientName;
    /** 收件人电话快照。 */
    public String recipientPhone;
    /** 收件人地址快照；兼容旧客户端提交的 customerAddress。 */
    @JsonAlias("customerAddress")
    public String recipientAddress;
    public Long warehouseId;
    public Long returnWarehouseId;
    public String leaseStartDate;
    public String leaseEndDate;
    public String shipDueTime;
    /** 发货时段，例如 09:00-12:00。 */
    public String shipTimeSlot;
    public String returnDueTime;
    /** 配送方式：express、flash、door_to_door。 */
    public String deliveryMethod;
    /** 归还配送方式：express、flash、door_to_door。 */
    public String returnDeliveryMethod;
    public BigDecimal deliveryAmount;
    public BigDecimal insuranceAmount;
    /** 服务端按租期起止日期计算，客户端提交值不生效。 */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer leaseDays;
    /** 服务端按预计到库规则计算，客户端提交值不生效。 */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String arrivalDate;
    public BigDecimal rentAmount;
    public BigDecimal depositAmount;
    public Integer orderStatus;
    public Integer payStatus;
    public Integer timeEffectStatus;
    public Integer weatherRiskFlag;
    public Integer priorityFlag;
    public String sourceTextSnapshot;
    public String shareSnapshotJson;
    public Long sourceOrderId;
    public Integer version;
    public Long productId;
    /** 订单主商品型号唯一编号，仅用于新增和编辑时校验套餐归属。 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public Long modelId;
    public Long packageId;
    /** 仅由订单详情回填，客户端不能提交或覆盖该快照。 */
    @JsonIgnore
    public String packageDetailsJson;
    public Long snId;
    public String accessoryItemIds;
    public Integer quantity;
    public String remark;
    public String orderNo;
    public Long orderId;
    public Long orderItemId;
    /** 发货方向快递信息。 */
    public String expressCompany;
    public String expressTrackingNo;
    /** 归还方向快递信息。 */
    public String returnExpressCompany;
    public String returnExpressTrackingNo;
    /** 最近一次归还验机结果，仅供订单详情展示，不能通过订单编辑提交。 */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public MerchantOrderReturnInspectionVO returnInspection;
}
