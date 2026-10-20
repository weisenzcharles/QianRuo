package com.ruoyi.merchant.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

/** 商户动态路由视图。 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MerchantRouterVO
{
    private String name;
    private String path;
    private boolean hidden;
    private String redirect;
    private String component;
    private String query;
    private Boolean alwaysShow;
    private MerchantRouteMeta meta;
    private List<MerchantRouterVO> children;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public boolean getHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
    public String getRedirect() { return redirect; }
    public void setRedirect(String redirect) { this.redirect = redirect; }
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public Boolean getAlwaysShow() { return alwaysShow; }
    public void setAlwaysShow(Boolean alwaysShow) { this.alwaysShow = alwaysShow; }
    public MerchantRouteMeta getMeta() { return meta; }
    public void setMeta(MerchantRouteMeta meta) { this.meta = meta; }
    public List<MerchantRouterVO> getChildren() { return children; }
    public void setChildren(List<MerchantRouterVO> children) { this.children = children; }
}
