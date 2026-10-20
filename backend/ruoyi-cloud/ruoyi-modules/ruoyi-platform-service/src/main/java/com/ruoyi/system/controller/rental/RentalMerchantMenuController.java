package com.ruoyi.system.controller.rental;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.context.ClientContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.domain.SysMenu;
import com.ruoyi.system.mapper.rental.RentalMerchantMenuMapper;

/** 平台端商户菜单管理，维护商户端可显示的全局菜单模板。 */
@RestController
@RequestMapping("/rental/merchantMenu")
public class RentalMerchantMenuController extends BaseController
{
    @Autowired
    private RentalMerchantMenuMapper merchantMenuMapper;

    /** 查询平台配置的商户菜单模板。 */
    @RequiresPermissions("admin:merchantMenu:list")
    @GetMapping("/list")
    public AjaxResult list(SysMenu menu)
    {
        assertAdminClient();
        return success(merchantMenuMapper.selectMenuList(menu));
    }

    /** 查询指定商户菜单详情。 */
    @RequiresPermissions("admin:merchantMenu:query")
    @GetMapping("/{menuId}")
    public AjaxResult getInfo(@PathVariable Long menuId)
    {
        assertAdminClient();
        SysMenu menu = merchantMenuMapper.selectMenuById(0L, 0L, menuId);
        return menu == null ? error("商户菜单不存在") : success(menu);
    }

    /** 新增商户端可显示菜单。 */
    @RequiresPermissions("admin:merchantMenu:add")
    @Log(title = "商户菜单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysMenu menu)
    {
        assertAdminClient();
        validateMenu(menu, false);
        menu.setCreateBy(SecurityUtils.getUserId());
        return toAjax(merchantMenuMapper.insertMenu(menu));
    }

    /** 修改商户端菜单显示配置和菜单权限标识。 */
    @RequiresPermissions("admin:merchantMenu:edit")
    @Log(title = "商户菜单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysMenu menu)
    {
        assertAdminClient();
        validateMenu(menu, true);
        menu.setUpdateBy(SecurityUtils.getUserId());
        return toAjax(merchantMenuMapper.updateMenu(menu));
    }

    /** 软删除商户菜单，存在子菜单或角色授权时拒绝删除。 */
    @RequiresPermissions("admin:merchantMenu:remove")
    @Log(title = "商户菜单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{menuId}")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult remove(@PathVariable Long menuId)
    {
        assertAdminClient();
        if (merchantMenuMapper.selectMenuById(0L, 0L, menuId) == null)
        {
            return error("商户菜单不存在");
        }
        if (merchantMenuMapper.hasChildByMenuId(0L, 0L, menuId) > 0)
        {
            return error("请先删除或停用子菜单");
        }
        if (merchantMenuMapper.checkMenuExistRole(0L, 0L, menuId) > 0)
        {
            return error("菜单已被商户角色使用，不能删除");
        }
        return toAjax(merchantMenuMapper.deleteMenuById(0L, 0L, menuId));
    }

    /** 校验菜单归属、父级和权限命名空间，禁止写入平台菜单或非商户权限。 */
    private void validateMenu(SysMenu menu, boolean editing)
    {
        if (menu == null || StringUtils.isEmpty(menu.getMenuName()))
        {
            throw new ServiceException("菜单名称不能为空");
        }
        if (editing && (menu.getMenuId() == null || merchantMenuMapper.selectMenuById(0L, 0L, menu.getMenuId()) == null))
        {
            throw new ServiceException("商户菜单不存在");
        }
        if (menu.getParentId() == null)
        {
            menu.setParentId(0L);
        }
        if (menu.getParentId() != 0L && merchantMenuMapper.selectMenuById(0L, 0L, menu.getParentId()) == null)
        {
            throw new ServiceException("父级商户菜单不存在");
        }
        if (StringUtils.isNotEmpty(menu.getPerms()) && !menu.getPerms().startsWith("merchant:"))
        {
            throw new ServiceException("商户菜单权限必须使用 merchant: 命名空间");
        }
    }

    /** 平台端接口只能由 platform 客户端调用，彻底隔离商户端管理边界。 */
    private void assertAdminClient()
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商户端不允许访问平台商户菜单管理接口");
        }
    }
}
