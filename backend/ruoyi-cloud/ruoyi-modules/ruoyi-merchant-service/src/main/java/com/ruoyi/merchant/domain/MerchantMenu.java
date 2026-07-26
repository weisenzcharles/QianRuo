package com.ruoyi.merchant.domain;

import java.util.ArrayList;
import java.util.List;

/** 商户菜单模板视图。菜单定义由平台维护，商户服务只读。 */
public class MerchantMenu
{
    private Long menuId;
    private String menuName;
    private Long parentId;
    private Integer orderNum;
    private String path;
    private String component;
    private String query;
    private String routeName;
    private String isFrame;
    private String isCache;
    private String menuType;
    private String visible;
    private String status;
    private String perms;
    private String icon;
    private List<MerchantMenu> children = new ArrayList<MerchantMenu>();

    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; }
    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    public String getIsFrame() { return isFrame; }
    public void setIsFrame(String isFrame) { this.isFrame = isFrame; }
    public String getIsCache() { return isCache; }
    public void setIsCache(String isCache) { this.isCache = isCache; }
    public String getMenuType() { return menuType; }
    public void setMenuType(String menuType) { this.menuType = menuType; }
    public String getVisible() { return visible; }
    public void setVisible(String visible) { this.visible = visible; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPerms() { return perms; }
    public void setPerms(String perms) { this.perms = perms; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public List<MerchantMenu> getChildren() { return children; }
    public void setChildren(List<MerchantMenu> children) { this.children = children; }
}
