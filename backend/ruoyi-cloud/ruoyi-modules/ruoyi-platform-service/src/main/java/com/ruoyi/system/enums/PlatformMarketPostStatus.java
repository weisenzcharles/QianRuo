package com.ruoyi.system.enums;

/** 平台周转市场帖子状态。数据库保存数值，页面统一展示中文名称。 */
public enum PlatformMarketPostStatus
{
    DRAFT(0, "草稿"), PUBLISHED(1, "已发布"), CLOSED(2, "已关闭"), OFF_SHELF(3, "已下架");

    private final int code;
    private final String label;

    PlatformMarketPostStatus(int code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static PlatformMarketPostStatus fromCode(Integer code)
    {
        if (code == null)
            return null;
        for (PlatformMarketPostStatus item : values())
        {
            if (item.code == code.intValue())
                return item;
        }
        return null;
    }
}
