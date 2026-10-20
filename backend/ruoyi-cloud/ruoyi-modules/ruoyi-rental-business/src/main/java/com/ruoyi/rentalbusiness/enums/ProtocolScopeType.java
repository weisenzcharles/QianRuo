package com.ruoyi.rentalbusiness.enums;

/** 协议适用入口类型。数据库仅保存编码，页面统一使用中文标签。 */
public enum ProtocolScopeType
{
    MERCHANT_WEB(1, "商户网页端"),
    VISITOR_WEB(2, "访客网页端"),
    MINI_PROGRAM(3, "小程序");

    private final int code;
    private final String label;

    ProtocolScopeType(int code, String label)
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

    /** 将数据库或请求编码转换为受控枚举。 */
    public static ProtocolScopeType fromCode(Integer code)
    {
        if (code == null)
        {
            return null;
        }
        for (ProtocolScopeType item : values())
        {
            if (item.code == code.intValue())
            {
                return item;
            }
        }
        return null;
    }
}
