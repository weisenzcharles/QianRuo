package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 平台查看的商户订单视图，数据来源为租赁执行业务服务。 */
public class PlatformOrderView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String orderNo;
    private Long tenantId;
    private Long memberId;
    private String merchantName;
    private String sourceShopName;
    private String customerName;
    private String customerPhone;
    private String recipientName;
    private String recipientPhone;
    private String leaseStartDate;
    private String leaseEndDate;
    private BigDecimal rentAmount;
    private BigDecimal depositAmount;
    private Integer orderStatus;
    private String createTime;
    private String externalOrderNo;
    private String recipientAddress;
    private String customerNote;
    private String warehouseName;
    private String returnWarehouseName;
    private String warehouseCity;
    private String productName;
    private String modelName;
    private String productCode;
    private String packageName;
    private String packageDetailsJson;
    private String shippingCarrier;
    private String shippingTrackingNo;
    private String returnCarrier;
    private String returnTrackingNo;
    private String shipDueTime;
    private String returnDueTime;
    private String arrivalDate;
    private java.math.BigDecimal deliveryAmount;
    private java.math.BigDecimal insuranceAmount;
    private Integer leaseDays;
    private Integer payStatus;
    private Integer timeEffectStatus;
    private Integer priorityFlag;
    private Integer weatherRiskFlag;
    private String remark;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public String getSourceShopName() { return sourceShopName; }
    public void setSourceShopName(String sourceShopName) { this.sourceShopName = sourceShopName; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
    public String getLeaseStartDate() { return leaseStartDate; }
    public void setLeaseStartDate(String leaseStartDate) { this.leaseStartDate = leaseStartDate; }
    public String getLeaseEndDate() { return leaseEndDate; }
    public void setLeaseEndDate(String leaseEndDate) { this.leaseEndDate = leaseEndDate; }
    public BigDecimal getRentAmount() { return rentAmount; }
    public void setRentAmount(BigDecimal rentAmount) { this.rentAmount = rentAmount; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
    public Integer getOrderStatus() { return orderStatus; }
    public void setOrderStatus(Integer orderStatus) { this.orderStatus = orderStatus; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getExternalOrderNo() { return externalOrderNo; }
    public void setExternalOrderNo(String externalOrderNo) { this.externalOrderNo = externalOrderNo; }
    public String getRecipientAddress() { return recipientAddress; }
    public void setRecipientAddress(String recipientAddress) { this.recipientAddress = recipientAddress; }
    public String getCustomerNote() { return customerNote; }
    public void setCustomerNote(String customerNote) { this.customerNote = customerNote; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getReturnWarehouseName() { return returnWarehouseName; }
    public void setReturnWarehouseName(String returnWarehouseName) { this.returnWarehouseName = returnWarehouseName; }
    public String getWarehouseCity() { return warehouseCity; }
    public void setWarehouseCity(String warehouseCity) { this.warehouseCity = warehouseCity; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public String getPackageDetailsJson() { return packageDetailsJson; }
    public void setPackageDetailsJson(String packageDetailsJson) { this.packageDetailsJson = packageDetailsJson; }
    public String getShippingCarrier() { return shippingCarrier; }
    public void setShippingCarrier(String shippingCarrier) { this.shippingCarrier = shippingCarrier; }
    public String getShippingTrackingNo() { return shippingTrackingNo; }
    public void setShippingTrackingNo(String shippingTrackingNo) { this.shippingTrackingNo = shippingTrackingNo; }
    public String getReturnCarrier() { return returnCarrier; }
    public void setReturnCarrier(String returnCarrier) { this.returnCarrier = returnCarrier; }
    public String getReturnTrackingNo() { return returnTrackingNo; }
    public void setReturnTrackingNo(String returnTrackingNo) { this.returnTrackingNo = returnTrackingNo; }
    public String getShipDueTime() { return shipDueTime; }
    public void setShipDueTime(String shipDueTime) { this.shipDueTime = shipDueTime; }
    public String getReturnDueTime() { return returnDueTime; }
    public void setReturnDueTime(String returnDueTime) { this.returnDueTime = returnDueTime; }
    public String getArrivalDate() { return arrivalDate; }
    public void setArrivalDate(String arrivalDate) { this.arrivalDate = arrivalDate; }
    public java.math.BigDecimal getDeliveryAmount() { return deliveryAmount; }
    public void setDeliveryAmount(java.math.BigDecimal deliveryAmount) { this.deliveryAmount = deliveryAmount; }
    public java.math.BigDecimal getInsuranceAmount() { return insuranceAmount; }
    public void setInsuranceAmount(java.math.BigDecimal insuranceAmount) { this.insuranceAmount = insuranceAmount; }
    public Integer getLeaseDays() { return leaseDays; }
    public void setLeaseDays(Integer leaseDays) { this.leaseDays = leaseDays; }
    public Integer getPayStatus() { return payStatus; }
    public void setPayStatus(Integer payStatus) { this.payStatus = payStatus; }
    public Integer getTimeEffectStatus() { return timeEffectStatus; }
    public void setTimeEffectStatus(Integer timeEffectStatus) { this.timeEffectStatus = timeEffectStatus; }
    public Integer getPriorityFlag() { return priorityFlag; }
    public void setPriorityFlag(Integer priorityFlag) { this.priorityFlag = priorityFlag; }
    public Integer getWeatherRiskFlag() { return weatherRiskFlag; }
    public void setWeatherRiskFlag(Integer weatherRiskFlag) { this.weatherRiskFlag = weatherRiskFlag; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
