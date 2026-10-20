package com.ruoyi.rentalbusiness.model;

import java.util.List;

/** 商户库存批量入库命令，兼容逐件序列号和数量库存。 */
public class MerchantInventoryBatchCommand
{
    public Long productId;
    public Long warehouseId;
    /** 数量库存入库数量；逐件库存由 serialNumbers 数量确定。 */
    public Integer quantity;
    public String idempotencyKey;
    public String remark;
    public List<String> serialNumbers;
}
