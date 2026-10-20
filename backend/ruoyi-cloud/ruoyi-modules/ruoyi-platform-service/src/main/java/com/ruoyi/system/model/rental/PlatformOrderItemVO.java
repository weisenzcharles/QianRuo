package com.ruoyi.system.model.rental;

import java.util.Collections;
import java.util.List;

/** 平台订单商品明细浏览对象，不暴露商品或库存技术编号。 */
public class PlatformOrderItemVO
{
    private String itemTypeName;
    private String itemName;
    private String itemCode;
    private String modelName;
    private Integer quantity;
    private String itemStatusName;
    private List<String> serialNumbers = Collections.emptyList();
    private List<String> inventoryCodes = Collections.emptyList();

    public String getItemTypeName() { return itemTypeName; }
    public void setItemTypeName(String value) { itemTypeName = value; }
    public String getItemName() { return itemName; }
    public void setItemName(String value) { itemName = value; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String value) { itemCode = value; }
    public String getModelName() { return modelName; }
    public void setModelName(String value) { modelName = value; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer value) { quantity = value; }
    public String getItemStatusName() { return itemStatusName; }
    public void setItemStatusName(String value) { itemStatusName = value; }
    public List<String> getSerialNumbers() { return serialNumbers; }
    public void setSerialNumbers(List<String> value) { serialNumbers = value == null ? Collections.emptyList() : value; }
    public List<String> getInventoryCodes() { return inventoryCodes; }
    public void setInventoryCodes(List<String> value) { inventoryCodes = value == null ? Collections.emptyList() : value; }
}
