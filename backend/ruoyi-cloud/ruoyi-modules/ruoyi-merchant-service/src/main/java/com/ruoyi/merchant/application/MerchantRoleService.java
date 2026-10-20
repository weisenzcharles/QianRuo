package com.ruoyi.merchant.application;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.log.service.MerchantBusinessAuditRecorder;
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

    @Autowired
    private MerchantBusinessAuditRecorder auditRecorder;

    @Transactional(rollbackFor = Exception.class)
    public List<MerchantRoleVO> list(MerchantRoleQuery query)
    {
        syncRoleTemplates();
        return roleMapper.selectList(tenantId(), merchantId(), query);
    }

    /**
     * 将平台新增的全局角色模板补齐为当前商户的运行角色，并恢复默认菜单授权。
     *
     * <p>模板本身不携带商户归属；这里生成的运行角色才属于当前商户，后续商户管理员
     * 修改菜单权限时只会影响当前商户，不会改变平台模板或其他商户。</p>
     */
    private void syncRoleTemplates()
    {
        roleMapper.insertMissingTemplateRoles(tenantId(), merchantId(), SecurityUtils.getUserId());
        roleMapper.insertTemplateRoleMenus(tenantId(), merchantId());
    }

    public MerchantRoleVO get(Long roleId)
    {
        MerchantRoleVO role = requireRole(roleId);
        List<Long> menuIds = menuMapper.selectIdsByRole(tenantId(), merchantId(), roleId,
                RentalCode.YesNo.YES.getCode().equals(role.getMenuCheckStrictly()));
        role.setMenuIds(menuIds.toArray(new Long[0]));
        return role;
    }

    @Transactional(rollbackFor = Exception.class)
    public int add(MerchantRoleCommand command)
    {
        validateUnique(command);
        Long[] menuIds = validateMenus(command.getMenuIds());
        applyDefaults(command);
        int rows = roleMapper.insert(tenantId(), merchantId(), command, SecurityUtils.getUserId());
        insertMenus(command.getRoleId(), menuIds);
        MerchantRoleVO created = get(command.getRoleId());
        audit("角色权限", "CREATE", "MERCHANT_ROLE", command.getRoleId(), created.getRoleName(), "创建商户角色",
                null, created);
        return rows;
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(MerchantRoleCommand command)
    {
        MerchantRoleVO existing = requireMutableRole(command.getRoleId());
        existing.setMenuIds(menuMapper.selectIdsByRole(tenantId(), merchantId(), existing.getRoleId(),
                RentalCode.YesNo.YES.getCode().equals(existing.getMenuCheckStrictly())).toArray(new Long[0]));
        validateUnique(command);
        Long[] menuIds = validateMenus(command.getMenuIds());
        applyDefaults(command);
        int rows = roleMapper.update(tenantId(), merchantId(), command, SecurityUtils.getUserId());
        roleMapper.deleteRoleMenus(tenantId(), merchantId(), existing.getRoleId());
        insertMenus(existing.getRoleId(), menuIds);
        invalidateRoleUsers(existing.getRoleId());
        MerchantRoleVO updated = get(existing.getRoleId());
        audit("角色权限", "UPDATE", "MERCHANT_ROLE", existing.getRoleId(), updated.getRoleName(), "编辑角色菜单权限",
                existing, updated);
        return rows;
    }

    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(Long roleId, Integer status)
    {
        MerchantRoleVO before = requireMutableRole(roleId);
        if (!RentalCode.EnableStatus.ENABLED.getCode().equals(status)
                && !RentalCode.EnableStatus.DISABLED.getCode().equals(status))
        {
            throw new ServiceException("角色状态不合法");
        }
        int rows = roleMapper.updateStatus(tenantId(), merchantId(), roleId, status, SecurityUtils.getUserId());
        invalidateRoleUsers(roleId);
        MerchantRoleVO after = get(roleId);
        audit("角色权限", "UPDATE", "MERCHANT_ROLE", roleId, after.getRoleName(), "修改角色状态", before, after);
        return rows;
    }

    @Transactional(rollbackFor = Exception.class)
    public int delete(Long[] roleIds)
    {
        if (roleIds == null || roleIds.length == 0)
        {
            throw new ServiceException("请选择要删除的角色");
        }
        Map<Long, MerchantRoleVO> beforeByRoleId = new LinkedHashMap<Long, MerchantRoleVO>();
        for (Long roleId : roleIds)
        {
            MerchantRoleVO existing = requireMutableRole(roleId);
            if (roleMapper.countUsers(tenantId(), merchantId(), roleId) > 0)
            {
                throw new ServiceException("角色已分配用户，不能删除");
            }
            roleMapper.deleteRoleMenus(tenantId(), merchantId(), roleId);
            beforeByRoleId.put(roleId, existing);
        }
        int rows = roleMapper.softDelete(tenantId(), merchantId(), roleIds, SecurityUtils.getUserId());
        for (Map.Entry<Long, MerchantRoleVO> entry : beforeByRoleId.entrySet())
        {
            MerchantRoleVO existing = entry.getValue();
            audit("角色权限", "DELETE", "MERCHANT_ROLE", entry.getKey(), existing.getRoleName(), "删除商户角色",
                    existing, null);
        }
        return rows;
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

    @Transactional(rollbackFor = Exception.class)
    public int grantUsers(Long roleId, Long[] userIds)
    {
        requireMutableRole(roleId);
        validateUsers(userIds);
        List<Long> before = roleMapper.selectUserIdsByRole(tenantId(), merchantId(), roleId);
        int rows = roleMapper.insertUserRole(tenantId(), merchantId(), roleId, unique(userIds));
        invalidateUsers(userIds);
        audit("角色权限", "GRANT_PERMISSION", "MERCHANT_ROLE", roleId, requireRole(roleId).getRoleName(), "为角色分配用户",
                before, roleMapper.selectUserIdsByRole(tenantId(), merchantId(), roleId));
        return rows;
    }

    @Transactional(rollbackFor = Exception.class)
    public int revokeUsers(Long roleId, Long[] userIds)
    {
        requireMutableRole(roleId);
        validateUsers(userIds);
        List<Long> before = roleMapper.selectUserIdsByRole(tenantId(), merchantId(), roleId);
        int rows = roleMapper.deleteUserRole(tenantId(), merchantId(), roleId, unique(userIds));
        invalidateUsers(userIds);
        audit("角色权限", "REVOKE_PERMISSION", "MERCHANT_ROLE", roleId, requireRole(roleId).getRoleName(), "撤销角色用户",
                before, roleMapper.selectUserIdsByRole(tenantId(), merchantId(), roleId));
        return rows;
    }

    /**
     * 记录商户角色、菜单和用户授权变化，平台角色不会经过本服务。
     *
     * @param module 业务模块
     * @param operationType 操作类型
     * @param targetType 业务对象类型
     * @param targetId 角色唯一编号
     * @param targetName 角色名称
     * @param description 业务描述
     * @param before 变更前内容
     * @param after 变更后内容
     */
    private void audit(String module, String operationType, String targetType, Long targetId,
            String targetName, String description, Object before, Object after)
    {
        auditRecorder.record(module, operationType, targetType, String.valueOf(targetId), targetName,
                description, before, after);
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
            command.setMenuCheckStrictly(RentalCode.YesNo.YES.getCode());
        }
        if (command.getStatus() == null)
        {
            command.setStatus(RentalCode.EnableStatus.ENABLED.getCode());
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
