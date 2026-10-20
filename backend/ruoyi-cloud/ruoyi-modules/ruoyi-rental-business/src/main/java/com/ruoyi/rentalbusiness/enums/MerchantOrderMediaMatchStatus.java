package com.ruoyi.rentalbusiness.enums;

/** 订单履约媒体匹配状态，与 ry_rental_media_asset.match_status 保持映射。 */
public enum MerchantOrderMediaMatchStatus
{
    PENDING(0, "待识别"),
    MATCHED(1, "已匹配"),
    UNMATCHED(2, "待人工处理");

    private final int code;
    private final String label;

    MerchantOrderMediaMatchStatus(int code, String label)
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

    public static MerchantOrderMediaMatchStatus fromCode(Integer code)
    {
        if (code == null)
        {
            return null;
        }
        for (MerchantOrderMediaMatchStatus value : values())
        {
            if (value.code == code)
            {
                return value;
            }
        }
        return null;
    }
}
