package com.ruoyi.rentalbusiness.model;

import java.util.List;
import com.ruoyi.rentalbusiness.enums.MerchantVerificationMode;
import com.ruoyi.rentalbusiness.enums.MerchantVerificationSource;

/** 极速核销台批量提交命令。 */
public class MerchantVerificationCommitCommand
{
    public MerchantVerificationMode mode;
    public MerchantVerificationSource source;
    public Long orderId;
    public Long warehouseId;
    public Long productId;
    public List<String> codes;
    public String idempotencyKey;
    public String remark;
}
