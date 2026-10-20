package com.ruoyi.merchant.model;

/** 周转市场举报类型。数据库保存受控字符串，页面只展示中文名称。 */
public enum MerchantMarketReportType
{
    FAKE_INVENTORY("FAKE_INVENTORY", "虚假库存"),
    ILLEGAL_CONTENT("ILLEGAL_CONTENT", "违规内容"),
    CONTACT_OUTSIDE("CONTACT_OUTSIDE", "引导站外联系"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String label;

    MerchantMarketReportType(String code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }

    public static MerchantMarketReportType fromCode(String code)
    {
        if (code == null)
            return null;
        for (MerchantMarketReportType value : values())
        {
            if (value.code.equals(code))
                return value;
        }
        return null;
    }
}
