package com.ruoyi.merchant.api.model;

import java.io.Serializable;

/** 文件服务返回的最小商户视图。 */
public class MerchantFileInfo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String name;
    private String url;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
