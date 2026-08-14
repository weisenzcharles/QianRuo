package com.ruoyi.system.controller.rental;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.service.TokenService;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.domain.SysMenu;
import com.ruoyi.system.domain.SysRoleMenu;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.system.domain.rental.RentalMember;
import com.ruoyi.system.domain.rental.RentalMerchantUser;
import com.ruoyi.system.mapper.rental.RentalMerchantMenuMapper;
import com.ruoyi.system.mapper.rental.RentalMerchantRoleMapper;
import com.ruoyi.system.mapper.rental.RentalMerchantUserMapper;
import com.ruoyi.system.mapper.rental.RentalMerchantUserMenuMapper;
import com.ruoyi.system.mapper.rental.RentalMerchantUserRoleMapper;
import com.ruoyi.system.mapper.rental.RentalUserMemberMapper;
import com.ruoyi.system.mapper.rental.RentalUserWarehouseScopeMapper;
import com.ruoyi.system.service.ISysMenuService;
import com.ruoyi.system.service.rental.IRentalMemberService;
import com.ruoyi.system.service.rental.RentalBusinessGateway;

/**
 * 平台商户用户管理
 *
 * @author qianruo
 */
@RestController
@RequestMapping("/rental/merchantUser")
public class RentalMerchantUserController extends BaseController
{
    @Autowired
    private RentalMerchantUserMapper merchantUserMapper;

    @Autowired
    private RentalMerchantRoleMapper merchantRoleMapper;

    @Autowired
    private RentalMerchantMenuMapper merchantMenuMapper;

    @Autowired
    private RentalMerchantUserRoleMapper merchantUserRoleMapper;

    @Autowired
    private RentalMerchantUserMenuMapper merchantUserMenuMapper;

    @Autowired
    private RentalUserMemberMapper userMemberMapper;

    @Autowired
    private RentalUserWarehouseScopeMapper userWarehouseScopeMapper;

    @Autowired
    private RentalBusinessGateway rentalBusinessGateway;

    @Autowired
    private IRentalMemberService memberService;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private TokenService tokenService;

    @RequiresPermissions("admin:merchantUser:list")
    @GetMapping("/list")
    public TableDataInfo list(RentalMerchantUser merchantUser)
    {
        assertAdminClient();
        startPage();
        return getDataTable(merchantUserMapper.selectMerchantUserList(merchantUser));
    }

    @RequiresPermissions("admin:merchantUser:query")
    @GetMapping("/{merchantUserId}")
    public AjaxResult getInfo(@PathVariable Long merchantUserId)
    {
        assertAdminClient();
        RentalMerchantUser user = merchantUserMapper.selectById(merchantUserId);
        if (StringUtils.isNull(user))
        {
            return error("商户用户不存在");
        }
        AjaxResult ajax = AjaxResult.success(user);
        SysRole roleQuery = new SysRole();
        roleQuery.getParams().put("tenantId", user.getTenantId());
        roleQuery.getParams().put("memberId", user.getMemberId());
        ajax.put("roles", merchantRoleMapper.selectRoleList(roleQuery));
        ajax.put("roleIds", merchantRoleMapper.selectRoleListByUserId(user.getTenantId(), user.getMemberId(),
                user.getMerchantUserId()));
        ajax.put("menus", menuService.buildMenuTreeSelect(merchantMenuMapper.selectMenuList(new SysMenu())));
        ajax.put("checkedKeys", merchantUserMenuMapper.selectMenuIdsByUserId(user.getTenantId(), user.getMemberId(),
                user.getMerchantUserId(), false));
        List<Long> warehouseIds = userWarehouseScopeMapper.selectWarehouseIds(user.getTenantId(), user.getMemberId(),
                user.getMerchantUserId());
        user.setWarehouseIds(warehouseIds.toArray(new Long[warehouseIds.size()]));
        ajax.put("warehouses", rentalBusinessGateway.listEnabledWarehouses(user.getTenantId(), user.getMemberId()));
        return ajax;
    }

    @RequiresPermissions("admin:merchantUser:query")
    @GetMapping("/warehouseOptions/{memberId}")
    public AjaxResult warehouseOptions(@PathVariable Long memberId)
    {
        assertAdminClient();
        RentalMember member = requireMember(memberId);
        return success(rentalBusinessGateway.listEnabledWarehouses(member.getTenantId(), member.getMemberId()));
    }

    @RequiresPermissions("admin:merchantUser:add")
    @Log(title = "商户用户", businessType = BusinessType.INSERT)
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult add(@Validated @RequestBody RentalMerchantUser user)
    {
        assertAdminClient();
        RentalMember member = requireMember(user.getMemberId());
        if (StringUtils.isNotNull(merchantUserMapper.selectByUserName(user.getUserName())))
        {
            return error("新增商户用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        if (StringUtils.isNotNull(merchantUserMapper.selectByPhone(user.getPhone())))
        {
            return error("新增商户用户'" + user.getPhone() + "'失败，手机号已存在");
        }
        user.setTenantId(member.getTenantId());
        user.setMemberId(member.getMemberId());
        user.setUserName(StringUtils.isEmpty(user.getUserName()) ? user.getPhone() : user.getUserName());
        user.setStatus(StringUtils.isEmpty(user.getStatus()) ? "0" : user.getStatus());
        user.setCreateBy(SecurityUtils.getUsername());
        if (StringUtils.isNotEmpty(user.getPassword()))
        {
            user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        }
        merchantUserMapper.insertMerchantUser(user);
        List<Long> warehouseIds = validateWarehouseScope(member, user, false);
        userMemberMapper.insertBinding(member.getTenantId(), member.getMemberId(), user.getMerchantUserId(),
                user.getMemberRole(), user.getWarehouseScopeType(), SecurityUtils.getUsername());
        saveWarehouseScope(member, user.getMerchantUserId(), warehouseIds);
        insertUserRole(member, user);
        insertUserMenu(member, user);
        invalidateMerchantSessions(user.getMerchantUserId());
        return success();
    }

    @RequiresPermissions("admin:merchantUser:edit")
    @Log(title = "商户用户", businessType = BusinessType.UPDATE)
    @PutMapping
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult edit(@Validated @RequestBody RentalMerchantUser user)
    {
        assertAdminClient();
        RentalMerchantUser oldUser = merchantUserMapper.selectById(user.getMerchantUserId());
        if (StringUtils.isNull(oldUser))
        {
            return error("商户用户不存在");
        }
        user.setTenantId(oldUser.getTenantId());
        user.setMemberId(oldUser.getMemberId());
        user.setUpdateBy(SecurityUtils.getUsername());
        RentalMember member = requireMember(oldUser.getMemberId());
        boolean owner = "owner".equals(oldUser.getMemberRole());
        List<Long> warehouseIds = validateWarehouseScope(member, user, owner);
        merchantUserMapper.updateMerchantUser(user);
        Long[] merchantUserIds = new Long[] { user.getMerchantUserId() };
        merchantUserRoleMapper.deleteUserRoleByUserIds(oldUser.getTenantId(), oldUser.getMemberId(), merchantUserIds);
        merchantUserMenuMapper.deleteUserMenuByUserIds(oldUser.getTenantId(), oldUser.getMemberId(), merchantUserIds);
        userMemberMapper.updateWarehouseScope(oldUser.getTenantId(), oldUser.getMemberId(),
                user.getMerchantUserId(), user.getMemberRole(), user.getWarehouseScopeType(),
                SecurityUtils.getUsername());
        saveWarehouseScope(member, user.getMerchantUserId(), warehouseIds);
        insertUserRole(member, user);
        insertUserMenu(member, user);
        invalidateMerchantSessions(user.getMerchantUserId());
        return success();
    }

    @RequiresPermissions("admin:merchantUser:edit")
    @Log(title = "商户用户", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody RentalMerchantUser user)
    {
        assertAdminClient();
        RentalMerchantUser oldUser = merchantUserMapper.selectById(user.getMerchantUserId());
        if (StringUtils.isNull(oldUser))
        {
            return error("商户用户不存在");
        }
        oldUser.setStatus(user.getStatus());
        oldUser.setUpdateBy(SecurityUtils.getUsername());
        int rows = merchantUserMapper.updateMerchantUser(oldUser);
        invalidateMerchantSessions(oldUser.getMerchantUserId());
        return toAjax(rows);
    }

    @RequiresPermissions("admin:merchantUser:resetPwd")
    @Log(title = "商户用户", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody RentalMerchantUser user)
    {
        assertAdminClient();
        if (StringUtils.isEmpty(user.getPassword()))
        {
            return error("新密码不能为空");
        }
        int rows = merchantUserMapper.resetUserPwd(user.getMerchantUserId(),
                SecurityUtils.encryptPassword(user.getPassword()));
        invalidateMerchantSessions(user.getMerchantUserId());
        return toAjax(rows);
    }

    @RequiresPermissions("admin:merchantUser:remove")
    @Log(title = "商户用户", businessType = BusinessType.DELETE)
    @DeleteMapping("/{merchantUserIds}")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult remove(@PathVariable Long[] merchantUserIds)
    {
        assertAdminClient();
        for (Long merchantUserId : merchantUserIds)
        {
            RentalMerchantUser user = merchantUserMapper.selectById(merchantUserId);
            if (StringUtils.isNotNull(user))
            {
                Long[] ids = new Long[] { merchantUserId };
                merchantUserRoleMapper.deleteUserRoleByUserIds(user.getTenantId(), user.getMemberId(), ids);
                merchantUserMenuMapper.deleteUserMenuByUserIds(user.getTenantId(), user.getMemberId(), ids);
                userWarehouseScopeMapper.deleteByUser(user.getTenantId(), user.getMemberId(), merchantUserId);
                invalidateMerchantSessions(merchantUserId);
            }
        }
        return toAjax(merchantUserMapper.deleteMerchantUserByIds(merchantUserIds));
    }

    private void insertUserRole(RentalMember member, RentalMerchantUser user)
    {
        if (StringUtils.isEmpty(user.getRoleIds()))
        {
            return;
        }
        List<SysUserRole> userRoles = new ArrayList<SysUserRole>();
        for (Long roleId : user.getRoleIds())
        {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(user.getMerchantUserId());
            userRole.setRoleId(roleId);
            userRoles.add(userRole);
        }
        merchantUserRoleMapper.batchUserRole(member.getTenantId(), member.getMemberId(), userRoles);
    }

    private void insertUserMenu(RentalMember member, RentalMerchantUser user)
    {
        if (StringUtils.isEmpty(user.getMenuIds()))
        {
            return;
        }
        List<SysRoleMenu> userMenus = new ArrayList<SysRoleMenu>();
        for (Long menuId : resolveMenuIdsWithAncestors(user.getMenuIds()))
        {
            SysRoleMenu userMenu = new SysRoleMenu();
            userMenu.setMenuId(menuId);
            userMenus.add(userMenu);
        }
        merchantUserMenuMapper.batchUserMenu(member.getTenantId(), member.getMemberId(), user.getMerchantUserId(),
                userMenus);
    }

    private Set<Long> resolveMenuIdsWithAncestors(Long[] menuIds)
    {
        List<SysMenu> menus = merchantMenuMapper.selectMenuList(new SysMenu());
        Map<Long, SysMenu> menuMap = new HashMap<Long, SysMenu>();
        for (SysMenu menu : menus)
        {
            menuMap.put(menu.getMenuId(), menu);
        }

        Set<Long> result = new LinkedHashSet<Long>();
        for (Long menuId : menuIds)
        {
            Long currentMenuId = menuId;
            while (currentMenuId != null && currentMenuId.longValue() != 0L && menuMap.containsKey(currentMenuId))
            {
                result.add(currentMenuId);
                SysMenu currentMenu = menuMap.get(currentMenuId);
                currentMenuId = currentMenu.getParentId();
            }
        }
        return result;
    }

    private RentalMember requireMember(Long memberId)
    {
        if (memberId == null)
        {
            throw new ServiceException("请选择商户");
        }
        RentalMember member = memberService.selectRentalMemberById(memberId);
        if (StringUtils.isNull(member))
        {
            throw new ServiceException("商户不存在");
        }
        return member;
    }

    private List<Long> validateWarehouseScope(RentalMember member, RentalMerchantUser user, boolean owner)
    {
        user.setMemberRole(owner ? "owner" : "staff");
        if (owner)
        {
            user.setWarehouseScopeType(Constants.WAREHOUSE_SCOPE_ALL);
            return new ArrayList<Long>();
        }
        if (StringUtils.isEmpty(user.getWarehouseScopeType()))
        {
            user.setWarehouseScopeType(Constants.WAREHOUSE_SCOPE_ALL);
        }
        if (!Constants.WAREHOUSE_SCOPE_ALL.equals(user.getWarehouseScopeType())
                && !Constants.WAREHOUSE_SCOPE_SPECIFIED.equals(user.getWarehouseScopeType()))
        {
            throw new ServiceException("仓库权限范围不合法");
        }
        if (Constants.WAREHOUSE_SCOPE_ALL.equals(user.getWarehouseScopeType()))
        {
            return new ArrayList<Long>();
        }

        Set<Long> warehouseIdSet = new LinkedHashSet<Long>();
        if (user.getWarehouseIds() != null)
        {
            warehouseIdSet.addAll(Arrays.asList(user.getWarehouseIds()));
            warehouseIdSet.remove(null);
        }
        if (warehouseIdSet.isEmpty())
        {
            throw new ServiceException("指定仓库权限时至少选择一个仓库");
        }
        List<Long> warehouseIds = new ArrayList<Long>(warehouseIdSet);
        if (rentalBusinessGateway.countEnabledWarehouses(member.getTenantId(), member.getMemberId(), warehouseIds)
                != warehouseIds.size())
        {
            throw new ServiceException("仓库不存在、已停用或不属于当前商户");
        }
        return warehouseIds;
    }

    private void saveWarehouseScope(RentalMember member, Long merchantUserId, List<Long> warehouseIds)
    {
        userWarehouseScopeMapper.deleteByUser(member.getTenantId(), member.getMemberId(), merchantUserId);
        if (!warehouseIds.isEmpty())
        {
            userWarehouseScopeMapper.batchInsert(member.getTenantId(), member.getMemberId(), merchantUserId,
                    warehouseIds, SecurityUtils.getUsername());
        }
    }

    private void assertAdminClient()
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商家端不允许访问平台商户用户管理接口");
        }
    }

    private void invalidateMerchantSessions(Long merchantUserId)
    {
        tokenService.delLoginUsers(Constants.CLIENT_TYPE_MERCHANT, merchantUserId);
    }
}
