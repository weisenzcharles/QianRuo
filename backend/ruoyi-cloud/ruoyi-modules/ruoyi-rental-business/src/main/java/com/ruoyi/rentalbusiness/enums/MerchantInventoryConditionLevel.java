package com.ruoyi.rentalbusiness.enums;

/** 商户库存设备成色等级。数据库保存稳定的整型等级，页面标签由系统字典维护。 */
public enum MerchantInventoryConditionLevel
{
    NEW(100, "全新"),
    NINETY_NINE(99, "99新"),
    NINETY_FIVE(95, "95新"),
    NINETY(90, "90新"),
    EIGHTY(80, "80新");

    private final Integer code;
    private final String label;

    MerchantInventoryConditionLevel(Integer code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public Integer getCode()
    {
        return code;
    }

    public String getLabel()
    {
        return label;
    }

    /** 判断请求值是否属于当前受控成色等级。 */
    public static boolean isSupported(Integer code)
    {
        if (code == null)
        {
            return false;
        }
        for (MerchantInventoryConditionLevel level : values())
        {
            if (level.code.equals(code))
            {
                return true;
            }
        }
        return false;
    }
}
