package com.ruoyi.system.model.rental;

/** 平台发票申请类型。 */
public enum PlatformInvoiceApplicationType
{
    ISSUE(1, "开票"), SUPPLEMENT(2, "补票"), RED_FLUSH(3, "红冲");

    private final int code;
    private final String label;

    PlatformInvoiceApplicationType(int code, String label) { this.code = code; this.label = label; }
    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static PlatformInvoiceApplicationType fromCode(Integer code)
    {
        if (code != null) for (PlatformInvoiceApplicationType item : values()) if (item.code == code) return item;
        return null;
    }
}
