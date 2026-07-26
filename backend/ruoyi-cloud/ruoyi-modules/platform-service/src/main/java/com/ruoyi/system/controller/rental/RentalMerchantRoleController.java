package com.ruoyi.system.controller.rental;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
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
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.domain.SysMenu;
import com.ruoyi.system.domain.SysRoleMenu;
import com.ruoyi.system.domain.rental.RentalMember;
import com.ruoyi.system.mapper.rental.RentalMerchantMenuMapper;
import com.ruoyi.system.mapper.rental.RentalMerchantRoleMapper;
import com.ruoyi.system.mapper.rental.RentalMerchantRoleMenuMapper;
import com.ruoyi.system.mapper.rental.RentalMerchantUserRoleMapper;
import com.ruoyi.system.service.ISysMenuService;
import com.ruoyi.system.service.rental.IRentalMemberService;

/**
 * 平台商户角色权限管理
 *
 * @author qianruo
 */
@RestController
@RequestMapping("/rental/merchantRole")
public class RentalMerchantRoleController extends BaseController
{
    @Autowired
    private RentalMerchantRoleMapper merchantRoleMapper;

    @Autowired
    private RentalMerchantMenuMapper merchantMenuMapper;

    @Autowired
    private RentalMerchantRoleMenuMapper merchantRoleMenuMapper;

    @Autowired
    private RentalMerchantUserRoleMapper merchantUserRoleMapper;

    @Autowired
    private IRentalMemberService memberService;

    @Autowired
    private ISysMenuService menuService;

    @RequiresPermissions("admin:merchantRole:list")
    @GetMapping("/list")
    public TableDataInfo list(SysRole role)
    {
        assertAdminClient();
        RentalMember member = requireMember(role.getParams().get("memberId"));
        role.getParams().put("tenantId", member.getTenantId());
        role.getParams().put("memberId", member.getMemberId());
        startPage();
        return getDataTable(merchantRoleMapper.selectRoleList(role));
    }

    @RequiresPermissions("admin:merchantRole:query")
    @GetMapping("/{memberId}/{roleId}")
    public AjaxResult getInfo(@PathVariable Long memberId, @PathVariable Long roleId)
    {
        assertAdminClient();
        RentalMember member = requireMember(memberId);
        return success(merchantRoleMapper.selectRoleById(member.getTenantId(), member.getMemberId(), roleId));
    }

    @RequiresPermissions("admin:merchantRole:add")
    @Log(title = "商户角色", businessType = BusinessType.INSERT)
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult add(@Validated @RequestBody SysRole role)
    {
        assertAdminClient();
        RentalMember member = requireMember(role.getParams().get("memberId"));
        role.getParams().put("tenantId", member.getTenantId());
        role.getParams().put("memberId", member.getMemberId());
        if (StringUtils.isNotNull(merchantRoleMapper.checkRoleNameUnique(member.getTenantId(), member.getMemberId(), role.getRoleName())))
        {
            return error("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        }
        if (StringUtils.isNotNull(merchantRoleMapper.checkRoleKeyUnique(member.getTenantId(), member.getMemberId(), role.getRoleKey())))
        {
            return error("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        role.setDataScope("1");
        role.setDeptCheckStrictly(true);
        role.setCreateBy(SecurityUtils.getUsername());
        merchantRoleMapper.insertRole(role);
        insertRoleMenu(member, role);
        return success();
    }

    @RequiresPermissions("admin:merchantRole:edit")
    @Log(title = "商户角色", businessType = BusinessType.UPDATE)
    @PutMapping
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult edit(@Validated @RequestBody SysRole role)
    {
        assertAdminClient();
        RentalMember member = requireMember(role.getParams().get("memberId"));
        SysRole oldRole = merchantRoleMapper.selectRoleById(member.getTenantId(), member.getMemberId(), role.getRoleId());
        if (StringUtils.isNull(oldRole))
        {
            return error("商户角色不存在");
        }
        SysRole sameName = merchantRoleMapper.checkRoleNameUnique(member.getTenantId(), member.getMemberId(), role.getRoleName());
        if (StringUtils.isNotNull(sameName) && !sameName.getRoleId().equals(role.getRoleId()))
        {
            return error("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
        }
        SysRole sameKey = merchantRoleMapper.checkRoleKeyUnique(member.getTenantId(), member.getMemberId(), role.getRoleKey());
        if (StringUtils.isNotNull(sameKey) && !sameKey.getRoleId().equals(role.getRoleId()))
        {
            return error("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        role.getParams().put("tenantId", member.getTenantId());
        role.getParams().put("memberId", member.getMemberId());
        role.setDataScope("1");
        role.setDeptCheckStrictly(true);
        role.setUpdateBy(SecurityUtils.getUsername());
        merchantRoleMapper.updateRole(role);
        merchantRoleMenuMapper.deleteRoleMenuByRoleId(member.getTenantId(), member.getMemberId(), role.getRoleId());
        insertRoleMenu(member, role);
        return success();
    }

    @RequiresPermissions("admin:merchantRole:remove")
    @Log(title = "商户角色", businessType = BusinessType.DELETE)
    @DeleteMapping("/{memberId}/{roleIds}")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult remove(@PathVariable Long memberId, @PathVariable Long[] roleIds)
    {
        assertAdminClient();
        RentalMember member = requireMember(memberId);
        for (Long roleId : roleIds)
        {
            if (merchantUserRoleMapper.countUserRoleByRoleId(member.getTenantId(), member.getMemberId(), roleId) > 0)
            {
                SysRole role = merchantRoleMapper.selectRoleById(member.getTenantId(), member.getMemberId(), roleId);
                return error(String.format("%1$s已分配,不能删除", role == null ? roleId : role.getRoleName()));
            }
        }
        merchantRoleMenuMapper.deleteRoleMenu(member.getTenantId(), member.getMemberId(), roleIds);
        return toAjax(merchantRoleMapper.deleteRoleByIds(member.getTenantId(), member.getMemberId(), roleIds));
    }

    @RequiresPermissions("admin:merchantRole:query")
    @GetMapping("/menuTreeselect/{memberId}")
    public AjaxResult menuTreeselect(@PathVariable Long memberId)
    {
        assertAdminClient();
        requireMember(memberId);
        List<SysMenu> menus = merchantMenuMapper.selectMenuList(new SysMenu());
        return success(menuService.buildMenuTreeSelect(menus));
    }

    @RequiresPermissions("admin:merchantRole:query")
    @GetMapping("/roleMenuTreeselect/{memberId}/{roleId}")
    public AjaxResult roleMenuTreeselect(@PathVariable Long memberId, @PathVariable Long roleId)
    {
        assertAdminClient();
        RentalMember member = requireMember(memberId);
        List<SysMenu> menus = merchantMenuMapper.selectMenuList(new SysMenu());
        AjaxResult ajax = AjaxResult.success();
        ajax.put("checkedKeys", merchantMenuMapper.selectMenuListByRoleId(member.getTenantId(), member.getMemberId(),
                roleId, true));
        ajax.put("menus", menuService.buildMenuTreeSelect(menus));
        return ajax;
    }

    private void insertRoleMenu(RentalMember member, SysRole role)
    {
        Long[] menuIds = role.getMenuIds();
        if (StringUtils.isEmpty(menuIds))
        {
            return;
        }
        List<SysRoleMenu> roleMenus = new ArrayList<SysRoleMenu>();
        for (Long menuId : menuIds)
        {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(role.getRoleId());
            roleMenu.setMenuId(menuId);
            roleMenus.add(roleMenu);
        }
        merchantRoleMenuMapper.batchRoleMenu(member.getTenantId(), member.getMemberId(), roleMenus);
    }

    private RentalMember requireMember(Object memberId)
    {
        if (memberId == null || StringUtils.isEmpty(String.valueOf(memberId)))
        {
            throw new ServiceException("请选择商户");
        }
        RentalMember member = memberService.selectRentalMemberById(Long.valueOf(String.valueOf(memberId)));
        if (StringUtils.isNull(member))
        {
            throw new ServiceException("商户不存在");
        }
        return member;
    }

    private void assertAdminClient()
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商家端不允许访问平台商户权限管理接口");
        }
    }
}
