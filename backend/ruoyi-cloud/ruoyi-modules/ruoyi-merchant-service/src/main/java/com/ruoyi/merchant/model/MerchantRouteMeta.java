package com.ruoyi.merchant.model;

/** 商户前端路由元数据。 */
public class MerchantRouteMeta
{
    private String title;
    private String icon;
    private boolean noCache;
    private String link;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public boolean isNoCache() { return noCache; }
    public void setNoCache(boolean noCache) { this.noCache = noCache; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
}
