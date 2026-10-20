package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 平台查看的联盟协助订单只读视图。 */
public class PlatformAllianceOrderView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long allianceOrderId;
    private Long tenantId;
    private Long sourceMemberId;
    private Long targetMemberId;
    private Long sourceOrderId;
    private Long childOrderId;
    private String sourceOrderNo;
    private String childOrderNo;
    private Integer transferMode;
    private Integer allianceOrderStatus;
    private BigDecimal settlementPrice;
    private String createTime;
    private String acceptedTime;
    private String sourceMerchantName;
    private String targetMerchantName;
    private String shippingCarrier;
    private String shippingTrackingNo;
    private String returnCarrier;
    private String returnTrackingNo;
    private Integer shippingRouteStatus;
    private Integer returnRouteStatus;

    public Long getAllianceOrderId() { return allianceOrderId; }
    public void setAllianceOrderId(Long value) { allianceOrderId = value; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long value) { tenantId = value; }
    public Long getSourceMemberId() { return sourceMemberId; }
    public void setSourceMemberId(Long value) { sourceMemberId = value; }
    public Long getTargetMemberId() { return targetMemberId; }
    public void setTargetMemberId(Long value) { targetMemberId = value; }
    public Long getSourceOrderId() { return sourceOrderId; }
    public void setSourceOrderId(Long value) { sourceOrderId = value; }
    public Long getChildOrderId() { return childOrderId; }
    public void setChildOrderId(Long value) { childOrderId = value; }
    public String getSourceOrderNo() { return sourceOrderNo; }
    public void setSourceOrderNo(String value) { sourceOrderNo = value; }
    public String getChildOrderNo() { return childOrderNo; }
    public void setChildOrderNo(String value) { childOrderNo = value; }
    public Integer getTransferMode() { return transferMode; }
    public void setTransferMode(Integer value) { transferMode = value; }
    public Integer getAllianceOrderStatus() { return allianceOrderStatus; }
    public void setAllianceOrderStatus(Integer value) { allianceOrderStatus = value; }
    public BigDecimal getSettlementPrice() { return settlementPrice; }
    public void setSettlementPrice(BigDecimal value) { settlementPrice = value; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String value) { createTime = value; }
    public String getAcceptedTime() { return acceptedTime; }
    public void setAcceptedTime(String value) { acceptedTime = value; }
    public String getSourceMerchantName() { return sourceMerchantName; }
    public void setSourceMerchantName(String value) { sourceMerchantName = value; }
    public String getTargetMerchantName() { return targetMerchantName; }
    public void setTargetMerchantName(String value) { targetMerchantName = value; }
    public String getShippingCarrier() { return shippingCarrier; }
    public void setShippingCarrier(String value) { shippingCarrier = value; }
    public String getShippingTrackingNo() { return shippingTrackingNo; }
    public void setShippingTrackingNo(String value) { shippingTrackingNo = value; }
    public String getReturnCarrier() { return returnCarrier; }
    public void setReturnCarrier(String value) { returnCarrier = value; }
    public String getReturnTrackingNo() { return returnTrackingNo; }
    public void setReturnTrackingNo(String value) { returnTrackingNo = value; }
    public Integer getShippingRouteStatus() { return shippingRouteStatus; }
    public void setShippingRouteStatus(Integer value) { shippingRouteStatus = value; }
    public Integer getReturnRouteStatus() { return returnRouteStatus; }
    public void setReturnRouteStatus(Integer value) { returnRouteStatus = value; }
}
