package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;

/** 商户录入订单命令，仅承载业务字段，不接受租户、商户和操作人归属字段。 */
public class MerchantOrderCreateCommand
{
    public String externalOrderNo;
    public Long channelId;
    public String shopName;
    public String customerName;
    public String customerPhone;
    public String customerAddress;
    public Long warehouseId;
    public Long returnWarehouseId;
    public String leaseStartDate;
    public String leaseEndDate;
    public String shipDueTime;
    public String returnDueTime;
    public BigDecimal rentAmount;
    public BigDecimal depositAmount;
    public String orderStatus;
    public String payStatus;
    public String timeEffectStatus;
    public Boolean weatherRiskFlag;
    public Boolean priorityFlag;
    public String sourceTextSnapshot;
    public String shareSnapshotJson;
    public Long sourceOrderId;
    public Integer version;
    public Long productId;
    public Long packageId;
    public Long snId;
    public String accessoryItemIds;
    public Integer quantity;
    public String remark;
    public String orderNo;
    public Long orderId;
}
