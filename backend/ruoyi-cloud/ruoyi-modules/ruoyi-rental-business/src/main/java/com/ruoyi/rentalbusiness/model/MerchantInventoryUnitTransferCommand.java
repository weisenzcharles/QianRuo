package com.ruoyi.rentalbusiness.model;

/** 商户库存单元转仓命令。来源仓库由服务端根据库存单元解析，不接受前端传入。 */
public class MerchantInventoryUnitTransferCommand
{
    /** 库存单元唯一编号，仅用于服务端定位数据。 */
    public Long unitId;

    /** 目标仓库唯一编号。 */
    public Long targetWarehouseId;
}
