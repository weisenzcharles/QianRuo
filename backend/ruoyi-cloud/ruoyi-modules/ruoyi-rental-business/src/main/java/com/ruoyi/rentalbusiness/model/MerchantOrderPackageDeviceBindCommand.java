package com.ruoyi.rentalbusiness.model;

import java.util.List;

/** 订单套餐逐件设备绑定命令。设备唯一编号只作为接口内部参数使用。 */
public class MerchantOrderPackageDeviceBindCommand
{
    /** 套餐快照 items 数组下标。 */
    public Integer itemIndex;

    /** 要绑定的库存单元唯一编号，数量必须与套餐明细一致。 */
    public List<Long> unitIds;
}
