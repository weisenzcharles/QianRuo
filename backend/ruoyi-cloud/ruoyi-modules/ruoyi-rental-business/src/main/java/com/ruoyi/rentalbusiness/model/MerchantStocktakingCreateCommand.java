package com.ruoyi.rentalbusiness.model;

import java.time.LocalDate;
import com.ruoyi.rentalbusiness.enums.MerchantStocktakingMode;
import com.ruoyi.rentalbusiness.enums.MerchantStocktakingScope;

/** 创建商户库存盘点单命令。 */
public class MerchantStocktakingCreateCommand
{
    public Long warehouseId;
    public LocalDate stocktakingDate;
    public MerchantStocktakingMode mode;
    public MerchantStocktakingScope scope;
    public Long classId;
    public Long modelId;
    public String remark;
}
