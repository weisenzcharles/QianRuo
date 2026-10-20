package com.ruoyi.system.model.rental;

import java.math.BigDecimal;

/**
 * 平台订单中心浏览器展示对象。
 *
 * <p>平台只读订单查询的内部视图包含跨服务关联编号，但这些编号不属于平台用户可读信息，
 * 因此在平台 Controller 边界前转换为本对象。</p>
 */
public class PlatformOrderVO
{
    /** 仅供详情接口使用，页面不直接展示该技术编号。 */
    private Long orderId;
    private String orderNo;
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

    public String getOrderNo() { return orderNo; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
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
}
