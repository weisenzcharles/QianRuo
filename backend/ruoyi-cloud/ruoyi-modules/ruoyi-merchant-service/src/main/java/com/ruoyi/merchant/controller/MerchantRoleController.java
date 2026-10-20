package com.ruoyi.merchant.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.merchant.application.MerchantRoleService;
import com.ruoyi.merchant.model.MerchantRoleCommand;
import com.ruoyi.merchant.model.MerchantRoleQuery;
import com.ruoyi.merchant.model.MerchantRoleStatusCommand;
import com.ruoyi.merchant.model.MerchantRoleUserQuery;
import com.ruoyi.merchant.model.MerchantRoleUserVO;
import com.ruoyi.merchant.model.MerchantRoleVO;
import com.ruoyi.merchant.model.MerchantUserRoleCommand;

/** 商户角色与用户授权。 */
@RestController
@RequestMapping("/merchant/role")
public class MerchantRoleController extends BaseController
{
    @Autowired
    private MerchantRoleService roleService;

    @RequiresPermissions("merchant:role:list")
    @GetMapping("/list")
    public TableDataInfo list(MerchantRoleQuery query)
    {
        startPage();
        List<MerchantRoleVO> roles = roleService.list(query);
        return getDataTable(roles);
    }

    @RequiresPermissions("merchant:role:query")
    @GetMapping("/{roleId}")
    public AjaxResult get(@PathVariable Long roleId)
    {
        return AjaxResult.success(roleService.get(roleId));
    }

    @RequiresPermissions("merchant:role:add")
    @Log(title = "商户角色", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MerchantRoleCommand command)
    {
        return toAjax(roleService.add(command));
    }

    @RequiresPermissions("merchant:role:edit")
    @Log(title = "商户角色", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult update(@Validated @RequestBody MerchantRoleCommand command)
    {
        return toAjax(roleService.update(command));
    }

    @RequiresPermissions("merchant:role:edit")
    @Log(title = "商户角色状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody MerchantRoleStatusCommand command)
    {
        return toAjax(roleService.changeStatus(command.getRoleId(), command.getStatus()));
    }

    @RequiresPermissions("merchant:role:remove")
    @Log(title = "商户角色", businessType = BusinessType.DELETE)
    @DeleteMapping("/{roleIds}")
    public AjaxResult delete(@PathVariable Long[] roleIds)
    {
        return toAjax(roleService.delete(roleIds));
    }

    @RequiresPermissions("merchant:role:list")
    @GetMapping("/authUser/allocatedList")
    public TableDataInfo allocatedUsers(MerchantRoleUserQuery query)
    {
        startPage();
        List<MerchantRoleUserVO> users = roleService.allocatedUsers(query);
        return getDataTable(users);
    }

    @RequiresPermissions("merchant:role:list")
    @GetMapping("/authUser/unallocatedList")
    public TableDataInfo unallocatedUsers(MerchantRoleUserQuery query)
    {
        startPage();
        List<MerchantRoleUserVO> users = roleService.unallocatedUsers(query);
        return getDataTable(users);
    }

    @RequiresPermissions("merchant:role:edit")
    @Log(title = "商户角色授权", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/cancel")
    public AjaxResult cancel(@RequestBody MerchantUserRoleCommand command)
    {
        return toAjax(roleService.revokeUsers(command.getRoleId(), new Long[] { command.getUserId() }));
    }

    @RequiresPermissions("merchant:role:edit")
    @Log(title = "商户角色授权", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/cancelAll")
    public AjaxResult cancelAll(@RequestParam Long roleId, @RequestParam Long[] userIds)
    {
        return toAjax(roleService.revokeUsers(roleId, userIds));
    }

    @RequiresPermissions("merchant:role:edit")
    @Log(title = "商户角色授权", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/selectAll")
    public AjaxResult selectAll(@RequestParam Long roleId, @RequestParam Long[] userIds)
    {
        return toAjax(roleService.grantUsers(roleId, userIds));
    }
}
