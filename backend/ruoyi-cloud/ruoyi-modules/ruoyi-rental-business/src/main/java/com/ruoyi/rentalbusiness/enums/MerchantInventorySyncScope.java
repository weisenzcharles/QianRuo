package com.ruoyi.rentalbusiness.enums;

/** 外部库存同步令牌授权范围。 */
public enum MerchantInventorySyncScope
{
    INVENTORY_READ("inventory:read", "读取库存"),
    INVENTORY_RESERVE("inventory:reserve", "预占和释放库存"),
    ORDER_CREATE("order:create", "创建正式订单");

    private final String code;
    private final String label;

    MerchantInventorySyncScope(String code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public String getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }

    public static MerchantInventorySyncScope fromCode(String code)
    {
        if (code != null)
        {
            for (MerchantInventorySyncScope value : values())
            {
                if (value.code.equals(code.trim()))
                {
                    return value;
                }
            }
        }
        return null;
    }
}
