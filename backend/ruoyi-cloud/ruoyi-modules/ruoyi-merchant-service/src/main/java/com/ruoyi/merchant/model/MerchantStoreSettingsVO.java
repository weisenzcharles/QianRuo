package com.ruoyi.merchant.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 商户店铺偏好设置视图及持久化 JSON 结构。 */
public class MerchantStoreSettingsVO
{
    private Integer inboundOutboundTrendDays = 30;
    private Integer inventoryOverviewDays = 30;
    private MerchantInventoryAlertType inventoryAlertType = MerchantInventoryAlertType.COUNT;
    private Integer inventoryAlertValue = 2;
    private Integer protectedQuantity = 0;
    private List<String> orderCopyFields = new ArrayList<String>(Arrays.asList(
            "customer", "recipient", "package", "lease", "warehouse", "amount", "remark"));
    private MerchantOrderViewMode orderViewMode = MerchantOrderViewMode.CARD;
    private MerchantDeliveryMethod defaultDeliveryMethod = MerchantDeliveryMethod.EXPRESS;
    private MerchantShipTimeSlot defaultShipTimeSlot = MerchantShipTimeSlot.MORNING;
    private Boolean contactVisible = Boolean.TRUE;
    private Boolean depositFreeEnabled = Boolean.FALSE;
    private Boolean allianceStoreVisible = Boolean.TRUE;
    private Boolean videoAutoDeleteEnabled = Boolean.FALSE;
    private Integer videoRetentionDays = 15;

    public static MerchantStoreSettingsVO defaults()
    {
        return new MerchantStoreSettingsVO();
    }

    public Integer getInboundOutboundTrendDays() { return inboundOutboundTrendDays; }
    public void setInboundOutboundTrendDays(Integer inboundOutboundTrendDays) { this.inboundOutboundTrendDays = inboundOutboundTrendDays; }
    public Integer getInventoryOverviewDays() { return inventoryOverviewDays; }
    public void setInventoryOverviewDays(Integer inventoryOverviewDays) { this.inventoryOverviewDays = inventoryOverviewDays; }
    public MerchantInventoryAlertType getInventoryAlertType() { return inventoryAlertType; }
    public void setInventoryAlertType(MerchantInventoryAlertType inventoryAlertType) { this.inventoryAlertType = inventoryAlertType; }
    public Integer getInventoryAlertValue() { return inventoryAlertValue; }
    public void setInventoryAlertValue(Integer inventoryAlertValue) { this.inventoryAlertValue = inventoryAlertValue; }
    public Integer getProtectedQuantity() { return protectedQuantity; }
    public void setProtectedQuantity(Integer protectedQuantity) { this.protectedQuantity = protectedQuantity; }
    public List<String> getOrderCopyFields() { return orderCopyFields; }
    public void setOrderCopyFields(List<String> orderCopyFields) { this.orderCopyFields = orderCopyFields; }
    public MerchantOrderViewMode getOrderViewMode() { return orderViewMode; }
    public void setOrderViewMode(MerchantOrderViewMode orderViewMode) { this.orderViewMode = orderViewMode; }
    public MerchantDeliveryMethod getDefaultDeliveryMethod() { return defaultDeliveryMethod; }
    public void setDefaultDeliveryMethod(MerchantDeliveryMethod defaultDeliveryMethod) { this.defaultDeliveryMethod = defaultDeliveryMethod; }
    public MerchantShipTimeSlot getDefaultShipTimeSlot() { return defaultShipTimeSlot; }
    public void setDefaultShipTimeSlot(MerchantShipTimeSlot defaultShipTimeSlot) { this.defaultShipTimeSlot = defaultShipTimeSlot; }
    public Boolean getContactVisible() { return contactVisible; }
    public void setContactVisible(Boolean contactVisible) { this.contactVisible = contactVisible; }
    public Boolean getDepositFreeEnabled() { return depositFreeEnabled; }
    public void setDepositFreeEnabled(Boolean depositFreeEnabled) { this.depositFreeEnabled = depositFreeEnabled; }
    public Boolean getAllianceStoreVisible() { return allianceStoreVisible; }
    public void setAllianceStoreVisible(Boolean allianceStoreVisible) { this.allianceStoreVisible = allianceStoreVisible; }
    public Boolean getVideoAutoDeleteEnabled() { return videoAutoDeleteEnabled; }
    public void setVideoAutoDeleteEnabled(Boolean videoAutoDeleteEnabled) { this.videoAutoDeleteEnabled = videoAutoDeleteEnabled; }
    public Integer getVideoRetentionDays() { return videoRetentionDays; }
    public void setVideoRetentionDays(Integer videoRetentionDays) { this.videoRetentionDays = videoRetentionDays; }
}
