package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** 平台订单商品明细内部视图。技术关联编号只在租赁业务服务内部使用。 */
public class PlatformOrderItemView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long orderItemId;
    private String itemTypeName;
    private String productType;
    private String itemName;
    private String itemCode;
    private String modelName;
    private Integer quantity;
    private Integer itemStatus;
    private String itemStatusName;
    private String serialNumber;
    private String inventoryCode;
    private List<String> serialNumbers = new ArrayList<>();
    private List<String> inventoryCodes = new ArrayList<>();

    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long value) { orderItemId = value; }
    public String getItemTypeName() { return itemTypeName; }
    public void setItemTypeName(String value) { itemTypeName = value; }
    public String getProductType() { return productType; }
    public void setProductType(String value) { productType = value; }
    public String getItemName() { return itemName; }
    public void setItemName(String value) { itemName = value; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String value) { itemCode = value; }
    public String getModelName() { return modelName; }
    public void setModelName(String value) { modelName = value; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer value) { quantity = value; }
    public Integer getItemStatus() { return itemStatus; }
    public void setItemStatus(Integer value) { itemStatus = value; }
    public String getItemStatusName() { return itemStatusName; }
    public void setItemStatusName(String value) { itemStatusName = value; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String value) { serialNumber = value; }
    public String getInventoryCode() { return inventoryCode; }
    public void setInventoryCode(String value) { inventoryCode = value; }
    public List<String> getSerialNumbers() { return serialNumbers; }
    public void setSerialNumbers(List<String> value) { serialNumbers = value == null ? new ArrayList<>() : value; }
    public List<String> getInventoryCodes() { return inventoryCodes; }
    public void setInventoryCodes(List<String> value) { inventoryCodes = value == null ? new ArrayList<>() : value; }
}
