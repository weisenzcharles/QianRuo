package com.ruoyi.merchant.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.merchant.application.MerchantUserService;
import com.ruoyi.merchant.model.MerchantUserQuery;
import com.ruoyi.merchant.model.MerchantUserVO;

/** 商户端用户管理接口。所有数据按当前商户上下文隔离。 */
@RestController
@RequestMapping("/merchant/user/manage")
public class MerchantUserManageController extends BaseController
{
    @Autowired private MerchantUserService userService;

    /** 添加商户用户并绑定角色。 */
    @RequiresPermissions("merchant:user:account:add")
    @Log(title = "商户用户", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MerchantUserVO command) { return toAjax(userService.add(command)); }

    /** 查询当前商户用户。 */
    @RequiresPermissions("merchant:user:account:list")
    @GetMapping("/list")
    public TableDataInfo list(MerchantUserQuery query) { startPage(); return getDataTable(userService.list(query)); }

    /** 查询用户详情和已选角色。 */
    @RequiresPermissions("merchant:user:account:query")
    @GetMapping("/{userId}")
    public AjaxResult get(@PathVariable Long userId) { return success(userService.get(userId)); }

    /** 修改用户基础资料和状态。 */
    @RequiresPermissions("merchant:user:account:edit")
    @Log(title = "商户用户", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult update(@RequestBody MerchantUserVO command) { return toAjax(userService.update(command)); }

    /** 将密码重置为随机临时口令并要求用户下次登录初始化。 */
    @RequiresPermissions("merchant:user:account:resetPwd")
    @Log(title = "商户用户密码", businessType = BusinessType.UPDATE)
    @PutMapping("/{userId}/reset-password")
    public AjaxResult resetPassword(@PathVariable Long userId) { return toAjax(userService.resetPassword(userId)); }
}
