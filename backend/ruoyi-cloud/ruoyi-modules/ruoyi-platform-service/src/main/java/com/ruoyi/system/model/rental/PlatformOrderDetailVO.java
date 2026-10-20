package com.ruoyi.system.model.rental;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/** 平台订单详情浏览对象，默认对客户联系方式和地址做脱敏。 */
public class PlatformOrderDetailVO
{
    private String orderNo;
    private String merchantName;
    private String sourceShopName;
    private String externalOrderNo;
    private String customerName;
    private String customerPhone;
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    private String leaseStartDate;
    private String leaseEndDate;
    private String shipDueTime;
    private String returnDueTime;
    private String arrivalDate;
    private Integer leaseDays;
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
    private BigDecimal rentAmount;
    private BigDecimal depositAmount;
    private BigDecimal deliveryAmount;
    private BigDecimal insuranceAmount;
    private Integer orderStatus;
    private Integer payStatus;
    private Integer timeEffectStatus;
    private Integer priorityFlag;
    private Integer weatherRiskFlag;
    private String customerNote;
    private String remark;
    private String createTime;
    private List<PlatformOrderItemVO> items = Collections.emptyList();
    private List<PlatformOrderLogisticsVO> logistics = Collections.emptyList();
    private PlatformOrderReturnInspectionVO returnInspection;
    private List<PlatformOrderAfterSalesVO> afterSales = Collections.emptyList();
    private List<PlatformOrderNoteVO> notes = Collections.emptyList();
    private List<PlatformOrderEditLogVO> editLogs = Collections.emptyList();

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public String getSourceShopName() { return sourceShopName; }
    public void setSourceShopName(String sourceShopName) { this.sourceShopName = sourceShopName; }
    public String getExternalOrderNo() { return externalOrderNo; }
    public void setExternalOrderNo(String externalOrderNo) { this.externalOrderNo = externalOrderNo; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
    public String getRecipientAddress() { return recipientAddress; }
    public void setRecipientAddress(String recipientAddress) { this.recipientAddress = recipientAddress; }
    public String getLeaseStartDate() { return leaseStartDate; }
    public void setLeaseStartDate(String leaseStartDate) { this.leaseStartDate = leaseStartDate; }
    public String getLeaseEndDate() { return leaseEndDate; }
    public void setLeaseEndDate(String leaseEndDate) { this.leaseEndDate = leaseEndDate; }
    public String getShipDueTime() { return shipDueTime; }
    public void setShipDueTime(String shipDueTime) { this.shipDueTime = shipDueTime; }
    public String getReturnDueTime() { return returnDueTime; }
    public void setReturnDueTime(String returnDueTime) { this.returnDueTime = returnDueTime; }
    public String getArrivalDate() { return arrivalDate; }
    public void setArrivalDate(String arrivalDate) { this.arrivalDate = arrivalDate; }
    public Integer getLeaseDays() { return leaseDays; }
    public void setLeaseDays(Integer leaseDays) { this.leaseDays = leaseDays; }
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
    public BigDecimal getRentAmount() { return rentAmount; }
    public void setRentAmount(BigDecimal rentAmount) { this.rentAmount = rentAmount; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
    public BigDecimal getDeliveryAmount() { return deliveryAmount; }
    public void setDeliveryAmount(BigDecimal deliveryAmount) { this.deliveryAmount = deliveryAmount; }
    public BigDecimal getInsuranceAmount() { return insuranceAmount; }
    public void setInsuranceAmount(BigDecimal insuranceAmount) { this.insuranceAmount = insuranceAmount; }
    public Integer getOrderStatus() { return orderStatus; }
    public void setOrderStatus(Integer orderStatus) { this.orderStatus = orderStatus; }
    public Integer getPayStatus() { return payStatus; }
    public void setPayStatus(Integer payStatus) { this.payStatus = payStatus; }
    public Integer getTimeEffectStatus() { return timeEffectStatus; }
    public void setTimeEffectStatus(Integer timeEffectStatus) { this.timeEffectStatus = timeEffectStatus; }
    public Integer getPriorityFlag() { return priorityFlag; }
    public void setPriorityFlag(Integer priorityFlag) { this.priorityFlag = priorityFlag; }
    public Integer getWeatherRiskFlag() { return weatherRiskFlag; }
    public void setWeatherRiskFlag(Integer weatherRiskFlag) { this.weatherRiskFlag = weatherRiskFlag; }
    public String getCustomerNote() { return customerNote; }
    public void setCustomerNote(String customerNote) { this.customerNote = customerNote; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public List<PlatformOrderItemVO> getItems() { return items; }
    public void setItems(List<PlatformOrderItemVO> items) { this.items = items; }
    public List<PlatformOrderLogisticsVO> getLogistics() { return logistics; }
    public void setLogistics(List<PlatformOrderLogisticsVO> logistics) { this.logistics = logistics; }
    public PlatformOrderReturnInspectionVO getReturnInspection() { return returnInspection; }
    public void setReturnInspection(PlatformOrderReturnInspectionVO value) { returnInspection = value; }
    public List<PlatformOrderAfterSalesVO> getAfterSales() { return afterSales; }
    public void setAfterSales(List<PlatformOrderAfterSalesVO> afterSales) { this.afterSales = afterSales; }
    public List<PlatformOrderNoteVO> getNotes() { return notes; }
    public void setNotes(List<PlatformOrderNoteVO> notes) { this.notes = notes; }
    public List<PlatformOrderEditLogVO> getEditLogs() { return editLogs; }
    public void setEditLogs(List<PlatformOrderEditLogVO> editLogs) { this.editLogs = editLogs; }
}
