package com.ruoyi.rentalbusiness.enums;

/** 商户库存流水类型，与 ry_rental_merchant_inventory_transaction 的受控编码对应。 */
public enum MerchantInventoryTransactionType
{
    TRANSFER(8, "调拨");

    private final int code;
    private final String label;

    MerchantInventoryTransactionType(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }
}
