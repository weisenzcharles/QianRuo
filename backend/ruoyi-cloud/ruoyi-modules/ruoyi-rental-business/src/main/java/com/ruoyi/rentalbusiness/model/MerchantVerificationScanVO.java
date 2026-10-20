package com.ruoyi.rentalbusiness.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/** 极速核销台单次扫码校验结果，只返回页面实际需要的业务信息。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantVerificationScanVO
{
    public boolean valid;
    public String code;
    public String unitCode;
    public String serialNumber;
    public String productName;
    public String modelName;
    public String productTypeLabel;
    public String warehouseName;
    public String statusLabel;
    public String validationLabel;
    public String orderNo;
    public String packageName;
    public String rentalPeriod;
    public String returnDate;
    public String sourceLabel;
}
