package com.ruoyi.rentalbusiness.enums;

/** 订单履约媒体类型，与 ry_rental_media_asset.media_type 保持映射。 */
public enum MerchantOrderMediaType
{
    SHIP_VIDEO("ship_video", 1, "发货视频"),
    RECEIVE_VIDEO("receive_video", 2, "收货视频"),
    UNBOX_VIDEO("unbox_video", 3, "开箱视频"),
    CHECK_PHOTO("check_photo", 4, "验机照片");

    private final String code;
    private final int dbCode;
    private final String label;

    MerchantOrderMediaType(String code, int dbCode, String label)
    {
        this.code = code;
        this.dbCode = dbCode;
        this.label = label;
    }

    public String getCode()
    {
        return code;
    }

    public int getDbCode()
    {
        return dbCode;
    }

    public String getLabel()
    {
        return label;
    }

    public static MerchantOrderMediaType fromCode(String code)
    {
        if (code != null)
        {
            String normalized = code.trim();
            for (MerchantOrderMediaType value : values())
            {
                if (value.code.equalsIgnoreCase(normalized)
                        || String.valueOf(value.dbCode).equals(normalized))
                {
                    return value;
                }
            }
        }
        return null;
    }
}
