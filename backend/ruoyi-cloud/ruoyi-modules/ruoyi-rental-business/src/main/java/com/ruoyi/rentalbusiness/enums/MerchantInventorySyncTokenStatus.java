package com.ruoyi.rentalbusiness.enums;

/** 外部库存同步令牌状态。 */
public enum MerchantInventorySyncTokenStatus
{
    ENABLED(0, "启用"),
    REVOKED(1, "已吊销");

    private final int code;
    private final String label;

    MerchantInventorySyncTokenStatus(int code, String label)
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

    public static MerchantInventorySyncTokenStatus fromCode(Integer code)
    {
        if (code != null)
        {
            for (MerchantInventorySyncTokenStatus value : values())
            {
                if (value.code == code)
                {
                    return value;
                }
            }
        }
        return null;
    }
}
