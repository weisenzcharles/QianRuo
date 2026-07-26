package com.ruoyi.merchant.application;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.service.TokenService;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.merchant.mapper.MerchantMenuMapper;
import com.ruoyi.merchant.mapper.MerchantRoleMapper;
import com.ruoyi.merchant.model.MerchantRoleCommand;
import com.ruoyi.merchant.model.MerchantRoleQuery;
import com.ruoyi.merchant.model.MerchantRoleUserQuery;
import com.ruoyi.merchant.model.MerchantRoleUserVO;
import com.ruoyi.merchant.model.MerchantRoleVO;

/** 商户角色和用户授权应用服务。 */
@Service
public class MerchantRoleService
{
    private static final String OWNER_ROLE_KEY = "merchant_admin";

    @Autowired
    private MerchantRoleMapper roleMapper;

    @Autowired
    private MerchantMenuMapper menuMapper;

    @Autowired
    private TokenService tokenService;

    public List<MerchantRoleVO> list(MerchantRoleQuery query)
    {
        return roleMapper.selectList(tenantId(), merchantId(), query);
    }

    public MerchantRoleVO get(Long roleId)
    {
        MerchantRoleVO role = requireRole(roleId);
        List<Long> menuIds = menuMapper.selectIdsByRole(tenantId(), merchantId(), roleId,
                Boolean.TRUE.equals(role.getMenuCheckStrictly()));
        role.setMenuIds(menuIds.toArray(new Long[0]));
        return role;
    }

    @Transactional
    public int add(MerchantRoleCommand command)
    {
        validateUnique(command);
        Long[] menuIds = validateMenus(command.getMenuIds());
        applyDefaults(command);
        int rows = roleMapper.insert(tenantId(), merchantId(), command, SecurityUtils.getUsername());
        insertMenus(command.getRoleId(), menuIds);
        return rows;
    }

    @Transactional
    public int update(MerchantRoleCommand command)
    {
        MerchantRoleVO existing = requireMutableRole(command.getRoleId());
        validateUnique(command);
        Long[] menuIds = validateMenus(command.getMenuIds());
        applyDefaults(command);
        int rows = roleMapper.update(tenantId(), merchantId(), command, SecurityUtils.getUsername());
        roleMapper.deleteRoleMenus(tenantId(), merchantId(), existing.getRoleId());
        insertMenus(existing.getRoleId(), menuIds);
        invalidateRoleUsers(existing.getRoleId());
        return rows;
    }

    public int changeStatus(Long roleId, String status)
    {
        requireMutableRole(roleId);
        if (!StringUtils.equalsAny(status, "0", "1"))
        {
            throw new ServiceException("角色状态不合法");
        }
        int rows = roleMapper.updateStatus(tenantId(), merchantId(), roleId, status, SecurityUtils.getUsername());
        invalidateRoleUsers(roleId);
        return rows;
    }

    @Transactional
    public int delete(Long[] roleIds)
    {
        if (roleIds == null || roleIds.length == 0)
        {
            throw new ServiceException("请选择要删除的角色");
        }
        for (Long roleId : roleIds)
        {
            requireMutableRole(roleId);
            if (roleMapper.countUsers(tenantId(), merchantId(), roleId) > 0)
            {
                throw new ServiceException("角色已分配用户，不能删除");
            }
            roleMapper.deleteRoleMenus(tenantId(), merchantId(), roleId);
        }
        return roleMapper.softDelete(tenantId(), merchantId(), roleIds, SecurityUtils.getUsername());
    }

    public List<MerchantRoleUserVO> allocatedUsers(MerchantRoleUserQuery query)
    {
        requireRole(query.getRoleId());
        return roleMapper.selectAllocatedUsers(tenantId(), merchantId(), query);
    }

    public List<MerchantRoleUserVO> unallocatedUsers(MerchantRoleUserQuery query)
    {
        requireRole(query.getRoleId());
        return roleMapper.selectUnallocatedUsers(tenantId(), merchantId(), query);
    }

    @Transactional
    public int grantUsers(Long roleId, Long[] userIds)
    {
        requireMutableRole(roleId);
        validateUsers(userIds);
        int rows = roleMapper.insertUserRole(tenantId(), merchantId(), roleId, unique(userIds));
        invalidateUsers(userIds);
        return rows;
    }

    @Transactional
    public int revokeUsers(Long roleId, Long[] userIds)
    {
        requireMutableRole(roleId);
        validateUsers(userIds);
        int rows = roleMapper.deleteUserRole(tenantId(), merchantId(), roleId, unique(userIds));
        invalidateUsers(userIds);
        return rows;
    }

    private MerchantRoleVO requireRole(Long roleId)
    {
        MerchantRoleVO role = roleMapper.selectById(tenantId(), merchantId(), roleId);
        if (role == null)
        {
            throw new ServiceException("商户角色不存在或不属于当前商户");
        }
        return role;
    }

    private MerchantRoleVO requireMutableRole(Long roleId)
    {
        MerchantRoleVO role = requireRole(roleId);
        if (OWNER_ROLE_KEY.equals(role.getRoleKey()))
        {
            throw new ServiceException("商户管理员角色为系统保护角色，不能修改或删除");
        }
        return role;
    }

    private void validateUnique(MerchantRoleCommand command)
    {
        MerchantRoleVO sameName = roleMapper.selectByName(tenantId(), merchantId(), command.getRoleName());
        if (sameName != null && !sameName.getRoleId().equals(command.getRoleId()))
        {
            throw new ServiceException("角色名称已存在");
        }
        MerchantRoleVO sameKey = roleMapper.selectByKey(tenantId(), merchantId(), command.getRoleKey());
        if (sameKey != null && !sameKey.getRoleId().equals(command.getRoleId()))
        {
            throw new ServiceException("角色权限字符已存在");
        }
        if (OWNER_ROLE_KEY.equals(command.getRoleKey()))
        {
            throw new ServiceException("不能创建或修改为系统保护角色");
        }
    }

    private Long[] validateMenus(Long[] menuIds)
    {
        Long[] uniqueIds = unique(menuIds);
        if (uniqueIds.length > 0 && menuMapper.countValidMenuIds(uniqueIds) != uniqueIds.length)
        {
            throw new ServiceException("菜单权限包含不存在或非 merchant:* 权限项");
        }
        return uniqueIds;
    }

    private void validateUsers(Long[] userIds)
    {
        Long[] uniqueIds = unique(userIds);
        if (uniqueIds.length == 0 || roleMapper.countUsersInMerchant(tenantId(), merchantId(), uniqueIds) != uniqueIds.length)
        {
            throw new ServiceException("用户列表包含不属于当前商户的账号");
        }
    }

    private void applyDefaults(MerchantRoleCommand command)
    {
        if (command.getMenuCheckStrictly() == null)
        {
            command.setMenuCheckStrictly(true);
        }
        if (StringUtils.isEmpty(command.getStatus()))
        {
            command.setStatus("0");
        }
    }

    private void insertMenus(Long roleId, Long[] menuIds)
    {
        if (menuIds.length > 0)
        {
            roleMapper.insertRoleMenus(tenantId(), merchantId(), roleId, menuIds);
        }
    }

    private void invalidateRoleUsers(Long roleId)
    {
        List<Long> userIds = roleMapper.selectUserIdsByRole(tenantId(), merchantId(), roleId);
        invalidateUsers(userIds.toArray(new Long[0]));
    }

    private void invalidateUsers(Long[] userIds)
    {
        if (userIds == null)
        {
            return;
        }
        for (Long userId : unique(userIds))
        {
            tokenService.delLoginUsers(Constants.CLIENT_TYPE_MERCHANT, userId);
        }
    }

    private Long[] unique(Long[] ids)
    {
        if (ids == null)
        {
            return new Long[0];
        }
        Set<Long> values = new LinkedHashSet<Long>(Arrays.asList(ids));
        values.remove(null);
        return values.toArray(new Long[0]);
    }

    private Long tenantId() { return MerchantContextUtils.getTenantId(); }
    private Long merchantId() { return MerchantContextUtils.getMemberId(); }
}
