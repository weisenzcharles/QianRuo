package com.ruoyi.merchant.application;

import java.util.List;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.log.service.MerchantBusinessAuditRecorder;
import com.ruoyi.common.security.service.TokenService;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.merchant.mapper.MerchantIdentityMapper;
import com.ruoyi.merchant.mapper.MerchantRoleMapper;
import com.ruoyi.merchant.model.MerchantUserQuery;
import com.ruoyi.merchant.model.MerchantUserVO;

/** 当前商户用户管理应用服务。 */
@Service
public class MerchantUserService
{
    @Autowired private MerchantIdentityMapper identityMapper;
    @Autowired private MerchantRoleMapper roleMapper;
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
        user.setRoleIds(roleMapper.selectRoleIdsByUser(tenantId(), merchantId(), userId).toArray(new Long[0]));
        return user;
    }

    /** 修改当前商户用户资料和启用状态。 */
    @Transactional(rollbackFor = Exception.class)
    public int update(MerchantUserVO command)
    {
        MerchantUserVO existing = get(command.getUserId());
        if ("merchant_admin".equals(existing.getRoleNames()) && !"0".equals(command.getStatus()))
        {
            throw new ServiceException("商户管理员账号不能停用");
        }
        int rows = identityMapper.updateMerchantUser(tenantId(), merchantId(), command.getUserId(),
                command.getNickName(), command.getPhone(), command.getEmail(), command.getStatus(), SecurityUtils.getUsername());
        replaceRoles(command.getUserId(), command.getRoleIds());
        tokenService.delLoginUsers(com.ruoyi.common.core.constant.Constants.CLIENT_TYPE_MERCHANT, command.getUserId());
        MerchantUserVO updated = get(command.getUserId());
        audit("用户管理", "UPDATE", "MERCHANT_USER", command.getUserId(), updated.getUserName(), "编辑商户用户",
                existing, updated);
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
        Long[] roleIds = command.getRoleIds();
        Set<Long> roles = new LinkedHashSet<Long>(roleIds == null ? java.util.Collections.emptyList() : Arrays.asList(roleIds));
        roles.remove(null);
        if (roles.isEmpty() || roleMapper.countRolesInMerchant(tenantId(), merchantId(), roles.toArray(new Long[0])) != roles.size())
        {
            throw new ServiceException("至少选择一个属于当前商户的角色");
        }
        command.setUserName(command.getUserName().trim());
        command.setPhone(command.getPhone().trim());
        if (identityMapper.selectByUsername(command.getUserName()) != null || identityMapper.selectByPhone(command.getPhone()) != null)
        {
            throw new ServiceException("用户名或手机号已存在");
        }
        String rawPassword = command.getPassword() == null || command.getPassword().trim().isEmpty()
                ? "123456" : command.getPassword();
        int rows = identityMapper.insertMerchantUser(tenantId(), merchantId(), command,
                SecurityUtils.encryptPassword(rawPassword), SecurityUtils.getUsername());
        roleMapper.insertUserRoles(tenantId(), merchantId(), command.getUserId(), roles.toArray(new Long[0]));
        MerchantUserVO created = get(command.getUserId());
        audit("用户管理", "CREATE", "MERCHANT_USER", command.getUserId(), created.getUserName(), "创建商户用户",
                null, created);
        return rows;
    }

    /** 校验角色归属并替换用户角色，角色菜单权限随角色授权统一生效。 */
    private void replaceRoles(Long userId, Long[] roleIds)
    {
        Set<Long> desired = new LinkedHashSet<Long>(roleIds == null ? java.util.Collections.emptyList() : Arrays.asList(roleIds));
        desired.remove(null);
        if (desired.isEmpty() || roleMapper.countRolesInMerchant(tenantId(), merchantId(), desired.toArray(new Long[0])) != desired.size())
        {
            throw new ServiceException("角色列表包含不存在或不属于当前商户的角色");
        }
        Long[] old = roleMapper.selectRoleIdsByUser(tenantId(), merchantId(), userId).toArray(new Long[0]);
        if (old.length > 0) roleMapper.deleteUserRoleBatch(tenantId(), merchantId(), old, new Long[] { userId });
        roleMapper.insertUserRoles(tenantId(), merchantId(), userId, desired.toArray(new Long[0]));
    }

    /** 重置用户密码并强制用户下次登录初始化。 */
    @Transactional(rollbackFor = Exception.class)
    public int resetPassword(Long userId)
    {
        MerchantUserVO before = get(userId);
        int rows = identityMapper.resetMerchantUserPassword(tenantId(), merchantId(), userId,
                SecurityUtils.encryptPassword("123456"), SecurityUtils.getUsername());
        tokenService.delLoginUsers(com.ruoyi.common.core.constant.Constants.CLIENT_TYPE_MERCHANT, userId);
        MerchantUserVO after = get(userId);
        audit("用户管理", "RESET_PASSWORD", "MERCHANT_USER", userId, after.getUserName(), "重置商户用户密码",
                before, after);
        return rows;
    }

    /**
     * 记录商户用户资料、角色和密码管理审计，快照中的密码字段由通用记录器统一过滤。
     *
     * @param module 业务模块
     * @param operationType 操作类型
     * @param targetType 业务对象类型
     * @param targetId 用户主键
     * @param targetName 用户名
     * @param description 业务描述
     * @param before 变更前对象
     * @param after 变更后对象
     */
    private void audit(String module, String operationType, String targetType, Long targetId,
            String targetName, String description, Object before, Object after)
    {
        auditRecorder.record(module, operationType, targetType, String.valueOf(targetId), targetName,
                description, before, after);
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
