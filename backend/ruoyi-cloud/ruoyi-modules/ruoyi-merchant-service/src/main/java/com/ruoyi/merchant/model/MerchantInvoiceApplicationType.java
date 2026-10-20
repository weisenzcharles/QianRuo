package com.ruoyi.merchant.model;

/** 发票申请类型。补票和红冲均通过追加申请关联原记录。 */
public enum MerchantInvoiceApplicationType
{
    ISSUE(1, "开票"), SUPPLEMENT(2, "补票"), RED_FLUSH(3, "红冲");

    private final int code;
    private final String label;

    MerchantInvoiceApplicationType(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static MerchantInvoiceApplicationType fromCode(Integer code)
    {
        if (code != null)
        {
            for (MerchantInvoiceApplicationType item : values())
            {
                if (item.code == code) return item;
            }
        }
        return null;
    }
}
