package com.ruoyi.merchant.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.merchant.application.MerchantMenuService;
import com.ruoyi.merchant.domain.MerchantMenu;

/** 商户菜单和动态路由。菜单模板由平台维护，商户端只读。 */
@RestController
@RequestMapping("/merchant/menu")
public class MerchantMenuController
{
    @Autowired
    private MerchantMenuService menuService;

    @RequiresPermissions("merchant:role:list")
    @GetMapping("/list")
    public AjaxResult list()
    {
        return AjaxResult.success(menuService.catalog());
    }

    @RequiresPermissions("merchant:role:query")
    @GetMapping("/{menuId}")
    public AjaxResult get(@PathVariable Long menuId)
    {
        return AjaxResult.success(menuService.get(menuId));
    }

    @RequiresPermissions("merchant:role:query")
    @GetMapping("/treeselect")
    public AjaxResult treeSelect()
    {
        return AjaxResult.success(menuService.treeSelect(menuService.catalog()));
    }

    @RequiresPermissions("merchant:role:query")
    @GetMapping("/roleMenuTreeselect/{roleId}")
    public AjaxResult roleMenuTreeSelect(@PathVariable Long roleId)
    {
        List<MerchantMenu> menus = menuService.catalog();
        AjaxResult result = AjaxResult.success();
        result.put("checkedKeys", menuService.selectedMenuIds(roleId, true));
        result.put("menus", menuService.treeSelect(menus));
        return result;
    }

    @GetMapping("/getRouters")
    public AjaxResult routers()
    {
        return AjaxResult.success(menuService.currentRouters());
    }
}
