package com.ruoyi.rentalbusiness.enums;

/** 快递时效预估供应商编码。 */
public enum ExpressTimeProviderCode
{
    /** 快递100智能时效预估。 */
    KUAIDI100("kuaidi100");

    private final String code;

    ExpressTimeProviderCode(String code)
    {
        this.code = code;
    }

    public String getCode()
    {
        return code;
    }
}
