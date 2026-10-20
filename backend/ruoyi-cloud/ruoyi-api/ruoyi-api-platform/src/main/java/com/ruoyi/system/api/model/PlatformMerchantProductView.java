package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 平台查看商户商品目录的只读视图，不包含商品技术主键。 */
public class PlatformMerchantProductView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String productCode;
    private String productName;
    private String productType;
    private String stockMode;
    private String brand;
    private String modelName;
    private Integer customFlag;
    private Integer status;

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    public String getStockMode() { return stockMode; }
    public void setStockMode(String stockMode) { this.stockMode = stockMode; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public Integer getCustomFlag() { return customFlag; }
    public void setCustomFlag(Integer customFlag) { this.customFlag = customFlag; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
