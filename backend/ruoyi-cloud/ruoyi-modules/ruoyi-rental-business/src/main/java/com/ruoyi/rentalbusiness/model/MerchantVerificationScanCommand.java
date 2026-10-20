package com.ruoyi.rentalbusiness.model;

import com.ruoyi.rentalbusiness.enums.MerchantVerificationMode;
import com.ruoyi.rentalbusiness.enums.MerchantVerificationSource;

/** 极速核销台单次扫码校验命令。 */
public class MerchantVerificationScanCommand
{
    public MerchantVerificationMode mode;
    public MerchantVerificationSource source;
    public Long orderId;
    public Long warehouseId;
    public Long productId;
    public String code;
}
