package com.ruoyi.rentalbusiness.enums;

/** 订单转单目标类型。数据库只保存数值，业务代码统一通过枚举表达。 */
public enum MerchantOrderAllianceTransferMode
{
    ALLIANCE(1),
    DESIGNATED_MERCHANT(2),
    PUBLIC_POOL(3);

    private final int code;

    MerchantOrderAllianceTransferMode(int code) { this.code = code; }

    public int getCode() { return code; }

    public static MerchantOrderAllianceTransferMode fromCode(Integer code)
    {
        if (code != null)
        {
            for (MerchantOrderAllianceTransferMode mode : values())
            {
                if (mode.code == code) return mode;
            }
        }
        return null;
    }
}
