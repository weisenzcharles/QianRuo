package com.ruoyi.merchant.model;

import java.util.List;

/** 商户周转市场帖子新增或修改命令。商户归属和状态由服务端上下文与状态流转控制。 */
public class MerchantMarketPostCommand
{
    private MerchantMarketBoardType boardType;
    private String category;
    private String brand;
    private String modelName;
    private String memorySpec;
    private String conditionLevel;
    private Integer quantity;
    private String priceRangeCode;
    private String city;
    /** 出售/出租帖子绑定的逐件库存唯一编号，求购帖子必须为空。 */
    private List<Long> inventoryUnitIds;
    private String serviceTags;
    private String remark;

    public MerchantMarketBoardType getBoardType() { return boardType; }
    public void setBoardType(MerchantMarketBoardType boardType) { this.boardType = boardType; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getMemorySpec() { return memorySpec; }
    public void setMemorySpec(String memorySpec) { this.memorySpec = memorySpec; }
    public String getConditionLevel() { return conditionLevel; }
    public void setConditionLevel(String conditionLevel) { this.conditionLevel = conditionLevel; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getPriceRangeCode() { return priceRangeCode; }
    public void setPriceRangeCode(String priceRangeCode) { this.priceRangeCode = priceRangeCode; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public List<Long> getInventoryUnitIds() { return inventoryUnitIds; }
    public void setInventoryUnitIds(List<Long> inventoryUnitIds) { this.inventoryUnitIds = inventoryUnitIds; }
    public String getServiceTags() { return serviceTags; }
    public void setServiceTags(String serviceTags) { this.serviceTags = serviceTags; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
