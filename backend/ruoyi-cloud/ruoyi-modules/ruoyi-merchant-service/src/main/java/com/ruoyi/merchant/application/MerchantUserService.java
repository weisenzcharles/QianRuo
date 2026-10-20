package com.ruoyi.merchant.application;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.log.service.MerchantBusinessAuditRecorder;
import com.ruoyi.common.security.service.TokenService;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.merchant.mapper.MerchantIdentityMapper;
import com.ruoyi.merchant.mapper.MerchantRoleMapper;
import com.ruoyi.merchant.domain.MerchantWarehouse;
import com.ruoyi.merchant.model.MerchantUserQuery;
import com.ruoyi.merchant.model.MerchantUserVO;

/** 当前商户用户管理应用服务。 */
@Service
public class MerchantUserService
{
    @Autowired private MerchantIdentityMapper identityMapper;
    @Autowired private MerchantRoleMapper roleMapper;
    @Autowired private RentalBusinessWarehouseGateway warehouseGateway;
    @Autowired private TokenService tokenService;
    @Autowired private MerchantBusinessAuditRecorder auditRecorder;

    /** 查询当前商户所有有效用户。 */
    public List<MerchantUserVO> list(MerchantUserQuery query)
    {
        return identityMapper.selectMerchantUsers(tenantId(), merchantId(), query);
    }

    /** 查询当前商户用户详情及角色。 */
    public MerchantUserVO get(Long userId)
    {
        MerchantUserVO user = requireUser(userId);
        Long[] roleIds = roleMapper.selectRoleIdsByUser(tenantId(), merchantId(), userId).toArray(new Long[0]);
        user.setRoleIds(roleIds);
        user.setWarehouseIds(identityMapper.selectWarehouseIds(tenantId(), merchantId(), userId)
                .toArray(new Long[0]));
        if (isAdministrator(user, roleIds))
        {
            user.setWarehouseScopeType(RentalCode.WarehouseScope.ALL.getCode());
            user.setWarehouseIds(new Long[0]);
        }
        return user;
    }

    /** 修改当前商户用户资料和启用状态。 */
    @Transactional(rollbackFor = Exception.class)
    public int update(MerchantUserVO command)
    {
        if (command == null || command.getUserId() == null)
        {
            throw new ServiceException("用户唯一编号不能为空");
        }
        MerchantUserVO existing = get(command.getUserId());
        Set<Long> desiredRoles = validateRoles(command.getRoleIds(), "角色列表包含不存在或不属于当前商户的角色");
        boolean administrator = isAdministrator(existing, desiredRoles.toArray(new Long[0]));
        List<MerchantWarehouse> warehouseDirectory = hasWarehouseAssignments(existing)
                || hasWarehouseAssignments(command) ? warehouseGateway.listAllWarehouses() : null;
        List<Long> warehouseIds = normalizeWarehouseScope(command, administrator, warehouseDirectory);
        if (isAdministrator(existing, existing.getRoleIds())
                && !RentalCode.EnableStatus.ENABLED.getCode().equals(command.getStatus()))
        {
            throw new ServiceException("商户管理员账号不能停用");
        }
        int rows = identityMapper.updateMerchantUser(tenantId(), merchantId(), command.getUserId(),
                command.getNickName(), command.getPhone(), command.getEmail(), command.getStatus(), SecurityUtils.getUserId());
        identityMapper.upsertMerchantUserBinding(tenantId(), merchantId(), command.getUserId(),
                "owner".equals(existing.getMemberRole()) ? "owner" : "staff",
                command.getWarehouseScopeType(), SecurityUtils.getUserId());
        replaceWarehouseScope(command.getUserId(), warehouseIds);
        replaceRoles(command.getUserId(), desiredRoles);
        tokenService.delLoginUsers(com.ruoyi.common.core.constant.Constants.CLIENT_TYPE_MERCHANT, command.getUserId());
        MerchantUserVO updated = get(command.getUserId());
        audit("用户管理", "UPDATE", "MERCHANT_USER", command.getUserId(), updated.getUserName(), "编辑商户用户",
                existing, updated, auditDisplaySnapshot(existing, warehouseDirectory),
                auditDisplaySnapshot(updated, warehouseDirectory));
        return rows;
    }

    /** 创建当前商户用户，校验角色归属并使用初始密码强制首次初始化。 */
    @Transactional(rollbackFor = Exception.class)
    public int add(MerchantUserVO command)
    {
        if (command == null || command.getUserName() == null || command.getUserName().trim().isEmpty()
                || command.getPhone() == null || command.getPhone().trim().isEmpty())
        {
            throw new ServiceException("用户名和手机号不能为空");
        }
        Set<Long> roles = validateRoles(command.getRoleIds(), "至少选择一个属于当前商户的角色");
        List<Long> warehouseIds = normalizeWarehouseScope(command, hasAdminRole(roles.toArray(new Long[0])));
        command.setUserName(command.getUserName().trim());
        command.setPhone(command.getPhone().trim());
        if (identityMapper.selectByUsername(command.getUserName()) != null || identityMapper.selectByPhone(command.getPhone()) != null)
        {
            throw new ServiceException("用户名或手机号已存在");
        }
        String rawPassword = command.getPassword() == null || command.getPassword().trim().isEmpty()
                ? generateInitialPassword() : command.getPassword();
        int rows = identityMapper.insertMerchantUser(tenantId(), merchantId(), command,
                SecurityUtils.encryptPassword(rawPassword), SecurityUtils.getUserId());
        identityMapper.upsertMerchantUserBinding(tenantId(), merchantId(), command.getUserId(), "staff",
                command.getWarehouseScopeType(), SecurityUtils.getUserId());
        replaceWarehouseScope(command.getUserId(), warehouseIds);
        roleMapper.insertUserRoles(tenantId(), merchantId(), command.getUserId(), roles.toArray(new Long[0]));
        MerchantUserVO created = get(command.getUserId());
        audit("用户管理", "CREATE", "MERCHANT_USER", command.getUserId(), created.getUserName(), "创建商户用户",
                null, created);
        return rows;
    }

    /** 校验角色归属并替换用户角色，角色菜单权限随角色授权统一生效。 */
    private void replaceRoles(Long userId, Set<Long> desired)
    {
        Long[] old = roleMapper.selectRoleIdsByUser(tenantId(), merchantId(), userId).toArray(new Long[0]);
        if (old.length > 0) roleMapper.deleteUserRoleBatch(tenantId(), merchantId(), old, new Long[] { userId });
        roleMapper.insertUserRoles(tenantId(), merchantId(), userId, desired.toArray(new Long[0]));
    }

    /** 校验角色确实属于当前商户，并去除重复和空的角色唯一编号。 */
    private Set<Long> validateRoles(Long[] roleIds, String emptyMessage)
    {
        Set<Long> roles = new LinkedHashSet<Long>(roleIds == null
                ? Collections.<Long>emptyList() : Arrays.asList(roleIds));
        roles.remove(null);
        if (roles.isEmpty())
        {
            throw new ServiceException(emptyMessage);
        }
        if (roleMapper.countRolesInMerchant(tenantId(), merchantId(), roles.toArray(new Long[0])) != roles.size())
        {
            throw new ServiceException("角色列表包含不存在或不属于当前商户的角色");
        }
        return roles;
    }

    /** 管理员和所有者不受仓库范围限制；普通用户按提交的指定仓库限制。 */
    private List<Long> normalizeWarehouseScope(MerchantUserVO command, boolean administrator)
    {
        return normalizeWarehouseScope(command, administrator, null);
    }

    private List<Long> normalizeWarehouseScope(MerchantUserVO command, boolean administrator,
            List<MerchantWarehouse> warehouseDirectory)
    {
        if (administrator)
        {
            command.setWarehouseScopeType(RentalCode.WarehouseScope.ALL.getCode());
            command.setWarehouseIds(new Long[0]);
            return Collections.emptyList();
        }
        Integer scopeType = command.getWarehouseScopeType();
        if (scopeType == null)
        {
            scopeType = RentalCode.WarehouseScope.ALL.getCode();
            command.setWarehouseScopeType(scopeType);
        }
        if (!RentalCode.WarehouseScope.ALL.getCode().equals(scopeType)
                && !RentalCode.WarehouseScope.SPECIFIED.getCode().equals(scopeType))
        {
            throw new ServiceException("仓库权限范围不合法");
        }
        if (RentalCode.WarehouseScope.ALL.getCode().equals(scopeType))
        {
            command.setWarehouseIds(new Long[0]);
            return Collections.emptyList();
        }

        Set<Long> selected = new LinkedHashSet<Long>(command.getWarehouseIds() == null
                ? Collections.<Long>emptyList() : Arrays.asList(command.getWarehouseIds()));
        selected.remove(null);
        if (selected.isEmpty())
        {
            throw new ServiceException("指定仓库权限时至少选择一个仓库");
        }
        Set<Long> enabledWarehouseIds = new LinkedHashSet<Long>();
        List<MerchantWarehouse> warehouses = warehouseDirectory == null
                ? warehouseGateway.listEnabledWarehouses() : warehouseDirectory;
        for (MerchantWarehouse warehouse : warehouses)
        {
            if (warehouse.getWarehouseId() != null
                    && (warehouseDirectory == null
                            || RentalCode.EnableStatus.ENABLED.getCode().equals(warehouse.getStatus())))
            {
                enabledWarehouseIds.add(warehouse.getWarehouseId());
            }
        }
        if (!enabledWarehouseIds.containsAll(selected))
        {
            throw new ServiceException("仓库不存在、已停用或不属于当前用户可管理范围");
        }
        List<Long> warehouseIds = new ArrayList<Long>(selected);
        command.setWarehouseIds(warehouseIds.toArray(new Long[0]));
        return warehouseIds;
    }

    /** 替换指定仓库范围，保留历史关系但只恢复当前选中的关系。 */
    private void replaceWarehouseScope(Long userId, List<Long> warehouseIds)
    {
        identityMapper.deleteMerchantUserWarehouseScope(tenantId(), merchantId(), userId);
        if (warehouseIds != null && !warehouseIds.isEmpty())
        {
            identityMapper.insertMerchantUserWarehouseScope(tenantId(), merchantId(), userId, warehouseIds,
                    SecurityUtils.getUserId());
        }
    }

    /** 管理员角色或所有者不受仓库范围限制。 */
    private boolean isAdministrator(MerchantUserVO user, Long[] roleIds)
    {
        return (user != null && "owner".equals(user.getMemberRole())) || hasAdminRole(roleIds);
    }

    private boolean hasAdminRole(Long[] roleIds)
    {
        return roleIds != null && roleIds.length > 0
                && roleMapper.countAdminRoles(tenantId(), merchantId(), roleIds) > 0;
    }

    /** 重置用户密码并强制用户下次登录初始化。 */
    @Transactional(rollbackFor = Exception.class)
    public int resetPassword(Long userId)
    {
        MerchantUserVO before = get(userId);
        int rows = identityMapper.resetMerchantUserPassword(tenantId(), merchantId(), userId,
                SecurityUtils.encryptPassword(generateInitialPassword()), SecurityUtils.getUserId());
        tokenService.delLoginUsers(com.ruoyi.common.core.constant.Constants.CLIENT_TYPE_MERCHANT, userId);
        MerchantUserVO after = get(userId);
        audit("用户管理", "RESET_PASSWORD", "MERCHANT_USER", userId, after.getUserName(), "重置商户用户密码",
                before, after);
        return rows;
    }

    static String generateInitialPassword()
    {
        return UUID.randomUUID().toString();
    }

    /**
     * 记录商户用户资料、角色和密码管理审计，快照中的密码字段由通用记录器统一过滤。
     *
     * @param module 业务模块
     * @param operationType 操作类型
     * @param targetType 业务对象类型
     * @param targetId 用户唯一编号
     * @param targetName 用户名
     * @param description 业务描述
     * @param before 变更前对象
     * @param after 变更后对象
     */
    private void audit(String module, String operationType, String targetType, Long targetId,
            String targetName, String description, Object before, Object after)
    {
        audit(module, operationType, targetType, targetId, targetName, description,
                before, after, before, after);
    }

    private void audit(String module, String operationType, String targetType, Long targetId,
            String targetName, String description, Object before, Object after,
            Object beforeDisplay, Object afterDisplay)
    {
        auditRecorder.record(module, operationType, targetType, String.valueOf(targetId), targetName,
                description, before, after, beforeDisplay, afterDisplay);
    }

    /** 生成只包含商户可读字段的用户审计快照，不把关联 ID 或原始联系方式写入展示快照。 */
    static Map<String, Object> auditDisplaySnapshot(MerchantUserVO user, List<MerchantWarehouse> warehouses)
    {
        if (user == null)
        {
            return Collections.emptyMap();
        }
        Map<Long, String> warehouseNames = new LinkedHashMap<Long, String>();
        if (warehouses != null)
        {
            for (MerchantWarehouse warehouse : warehouses)
            {
                if (warehouse.getWarehouseId() != null)
                {
                    String name = warehouse.getWarehouseName();
                    if (name == null || name.trim().isEmpty())
                    {
                        name = warehouse.getWarehouseCode();
                    }
                    warehouseNames.put(warehouse.getWarehouseId(), name);
                }
            }
        }

        Map<String, Object> snapshot = new LinkedHashMap<String, Object>();
        snapshot.put("userName", user.getUserName());
        snapshot.put("nickName", user.getNickName());
        snapshot.put("phoneDisplay", maskPhone(user.getPhone()));
        snapshot.put("emailDisplay", maskEmail(user.getEmail()));
        snapshot.put("status", statusLabel(user.getStatus()));
        snapshot.put("roleNames", user.getRoleNames());
        snapshot.put("memberRole", "owner".equals(user.getMemberRole()) ? "商户所有者" : "普通员工");
        Integer scopeType = user.getWarehouseScopeType();
        snapshot.put("warehouseScopeType", RentalCode.WarehouseScope.ALL.getCode().equals(scopeType)
                ? "全部仓库" : RentalCode.WarehouseScope.SPECIFIED.getCode().equals(scopeType) ? "指定仓库" : "未配置");
        snapshot.put("warehouseNames", assignedWarehouseNames(user, warehouseNames));
        return snapshot;
    }

    private static String assignedWarehouseNames(MerchantUserVO user, Map<Long, String> warehouseNames)
    {
        if (RentalCode.WarehouseScope.ALL.getCode().equals(user.getWarehouseScopeType()))
        {
            return "全部仓库";
        }
        Long[] warehouseIds = user.getWarehouseIds();
        if (warehouseIds == null || warehouseIds.length == 0)
        {
            return "未设置";
        }
        StringBuilder names = new StringBuilder();
        for (Long warehouseId : warehouseIds)
        {
            if (warehouseId == null)
            {
                continue;
            }
            if (names.length() > 0)
            {
                names.append("、");
            }
            String name = warehouseNames.get(warehouseId);
            names.append(name == null || name.trim().isEmpty() ? "仓库不可用" : name);
        }
        return names.length() == 0 ? "未设置" : names.toString();
    }

    private static String statusLabel(Integer status)
    {
        if (RentalCode.EnableStatus.ENABLED.getCode().equals(status))
        {
            return RentalCode.EnableStatus.ENABLED.getLabel();
        }
        if (RentalCode.EnableStatus.DISABLED.getCode().equals(status))
        {
            return RentalCode.EnableStatus.DISABLED.getLabel();
        }
        return "未配置";
    }

    private static String maskPhone(String phone)
    {
        if (phone == null || phone.trim().isEmpty())
        {
            return "";
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() >= 8)
        {
            return digits.substring(0, 3) + "****" + digits.substring(digits.length() - 4);
        }
        if (digits.length() >= 5)
        {
            return digits.substring(0, 2) + "****" + digits.substring(digits.length() - 2);
        }
        return digits.length() >= 3
                ? digits.substring(0, 1) + "****" + digits.substring(digits.length() - 1) : "已设置";
    }

    private static String maskEmail(String email)
    {
        if (email == null || email.trim().isEmpty())
        {
            return "";
        }
        int separator = email.lastIndexOf('@');
        if (separator <= 0 || separator == email.length() - 1)
        {
            return "已设置";
        }
        return email.substring(0, 1) + "***" + email.substring(separator);
    }

    private static boolean hasWarehouseAssignments(MerchantUserVO user)
    {
        return user != null && user.getWarehouseIds() != null && user.getWarehouseIds().length > 0;
    }

    /** 校验用户属于当前商户。 */
    private MerchantUserVO requireUser(Long userId)
    {
        MerchantUserVO user = identityMapper.selectMerchantUser(tenantId(), merchantId(), userId);
        if (user == null) throw new ServiceException("用户不存在或不属于当前商户");
        return user;
    }
    private Long tenantId() { return MerchantContextUtils.getTenantId(); }
    private Long merchantId() { return MerchantContextUtils.getMemberId(); }
}
