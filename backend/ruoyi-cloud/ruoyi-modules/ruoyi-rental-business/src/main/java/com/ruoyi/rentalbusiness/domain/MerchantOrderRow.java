package com.ruoyi.rentalbusiness.domain;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商户订单列表查询结果行。
 *
 * 该对象只用于 Mapper 到应用服务的内部投影，不直接作为 Controller 响应，
 * 以避免数据库字段和商户端展示结构发生耦合。
 */
public class MerchantOrderRow
{
    public Long orderId;
    public String orderNo;
    public String externalOrderNo;
    public Long channelId;
    public String channelCode;
    public String channelName;
    public String shopName;
    public String customerName;
    public String customerNickname;
    public String customerPhone;
    public String customerAddress;
    public String customerNote;
    public String warehouseName;
    public String warehouseCity;
    public String recipientProvinceCode;
    public String recipientCityCode;
    public String recipientDistrictCode;
    public String recipientAmapCityCode;
    public String recipientProvinceName;
    public String recipientCityName;
    public String recipientDistrictName;
    public Long warehouseId;
    public Long returnWarehouseId;
    public Long productId;
    public Long packageId;
    public Long snId;
    public String accessoryItemIds;
    public Integer quantity;
    public Date leaseStartDate;
    public Date leaseEndDate;
    public Date shipDueTime;
    public Date returnDueTime;
    public Date expectedArriveTime;
    public Date createTime;
    public BigDecimal rentAmount;
    public BigDecimal depositAmount;
    public String orderStatus;
    public String payStatus;
    public String timeEffectStatus;
    public Integer priorityFlag;
    public Integer version;
    public String remark;
    public String sourceTextSnapshot;
    public Integer weatherRiskFlag;
    public String modelName;
    public String packageName;
    public String productCode;
    public String shippingCarrier;
    public String shippingTrackingNo;
    public String returnCarrier;
    public String returnTrackingNo;
    public BigDecimal shippingFee;
    public String attention;
    public String addonNames;
    public Long createBy;
}
