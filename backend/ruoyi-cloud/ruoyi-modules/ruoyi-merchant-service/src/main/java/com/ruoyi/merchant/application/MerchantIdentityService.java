package com.ruoyi.merchant.application;

import java.util.Date;
import java.util.Collections;
import java.util.LinkedHashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.merchant.api.model.MerchantOwnerProvisionCommand;
import com.ruoyi.merchant.api.model.MerchantLoginIdentity;
import com.ruoyi.merchant.domain.MerchantAccount;
import com.ruoyi.merchant.domain.MerchantAdminRole;
import com.ruoyi.merchant.domain.MerchantBinding;
import com.ruoyi.merchant.mapper.MerchantIdentityMapper;

/** 商户身份应用服务。 */
@Service
public class MerchantIdentityService
{
    @Autowired
    private MerchantIdentityMapper identityMapper;

    /**
     * 按用户名加载商户登录身份。待审核和已驳回商户也允许建立登录态，
     * 但只组装默认只读权限，只有审核通过后才恢复完整业务权限。
     *
     * @param username 商户用户名
     * @return 包含可信租户、商户和权限信息的登录身份
     */
    public MerchantLoginIdentity loadByUsername(String username)
    {
        MerchantAccount account = identityMapper.selectByUsername(username);
        if (account == null)
        {
            throw new ServiceException("商户账号或密码错误");
        }
        return assemble(account);
    }

    /**
     * 按手机号加载商户登录身份。注册完成后账号会立即创建，因此待审核和已驳回状态都允许登录。
     *
     * @param phone 已在入驻申请中验证的手机号
     * @return 包含可信租户、商户和权限信息的登录身份
     */
    public MerchantLoginIdentity loadByPhone(String phone)
    {
        MerchantAccount account = identityMapper.selectByPhone(phone);
        if (account == null)
        {
            throw new ServiceException(loginUnavailableMessage(identityMapper.selectMemberAuthStatusByPhone(phone)));
        }
        return assemble(account);
    }

    /**
     * 幂等开通审核通过商户的所有者账号、主体绑定、管理员角色及全部可用菜单。
     * 已存在账号必须属于同一租户和商户，否则拒绝覆盖原有绑定。
     *
     * @param command 平台审核服务传入的可信开户命令
     * @return 开通流程完整成功时返回 {@code true}
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean provisionOwner(MerchantOwnerProvisionCommand command)
    {
        validateProvision(command);
        String phone = command.getPhone().trim();
        MerchantAccount account = identityMapper.selectByPhone(phone);
        if (account == null)
        {
            MerchantAccount usernameAccount = identityMapper.selectByUsername(phone);
            if (usernameAccount != null)
            {
                throw new ServiceException("手机号已被其他商户账号占用");
            }
            account = new MerchantAccount();
            account.setTenantId(command.getTenantId());
            account.setMerchantId(command.getMerchantId());
            account.setUsername(phone);
            account.setPhone(phone);
            account.setNickname(StringUtils.isBlank(command.getNickname()) ? phone : command.getNickname().trim());
            account.setEmail(StringUtils.isBlank(command.getEmail()) ? null : command.getEmail().trim());
            identityMapper.insertAccount(account, command.getOperator());
        }
        else
        {
            if (!command.getTenantId().equals(account.getTenantId())
                    || !command.getMerchantId().equals(account.getMerchantId()))
            {
                throw new ServiceException("手机号已绑定其他商户");
            }
            account.setNickname(StringUtils.isBlank(command.getNickname()) ? phone : command.getNickname().trim());
            account.setEmail(StringUtils.isBlank(command.getEmail()) ? null : command.getEmail().trim());
            identityMapper.activateOwnerAccount(account, command.getOperator());
        }

        if (identityMapper.countOwnerBinding(command.getTenantId(), command.getMerchantId(), account.getUserId()) == 0)
        {
            identityMapper.insertOwnerBinding(command.getTenantId(), command.getMerchantId(), account.getUserId(),
                    command.getOperator());
        }

        identityMapper.insertMerchantRolesFromTemplates(command.getTenantId(), command.getMerchantId(),
                command.getOperator());
        identityMapper.insertMerchantRoleMenusFromTemplates(command.getTenantId(), command.getMerchantId());
        Long roleId = identityMapper.selectAdminRoleId(command.getTenantId(), command.getMerchantId());
        if (roleId == null)
        {
            MerchantAdminRole role = new MerchantAdminRole();
            role.setTenantId(command.getTenantId());
            role.setMerchantId(command.getMerchantId());
            role.setOperator(command.getOperator());
            identityMapper.insertAdminRole(role);
            roleId = role.getRoleId();
        }
        identityMapper.insertAdminRoleMenus(command.getTenantId(), command.getMerchantId(), roleId);
        identityMapper.insertAdminUserRole(command.getTenantId(), command.getMerchantId(), roleId, account.getUserId());
        return true;
    }

    /**
     * 按商户用户主键加载登录身份，用于令牌刷新和已登录用户信息查询。
     *
     * @param userId 商户用户主键
     * @return 当前商户用户的可信登录身份
     */
    public MerchantLoginIdentity loadById(Long userId)
    {
        MerchantAccount account = identityMapper.selectById(userId);
        if (account == null)
        {
            throw new ServiceException("商户用户不存在");
        }
        return assemble(account);
    }

    /**
     * 记录商户用户最近一次登录地址和时间。
     *
     * @param userId 商户用户主键
     * @param loginIp 登录来源地址
     * @param loginAt 登录时间毫秒值
     * @return 数据库记录是否更新成功
     */
    public boolean recordLogin(Long userId, String loginIp, Long loginAt)
    {
        if (userId == null || loginAt == null)
        {
            throw new ServiceException("商户登录记录参数不完整");
        }
        return identityMapper.updateLogin(userId, loginIp, new Date(loginAt)) > 0;
    }

    /**
     * 根据商户账号和唯一有效主体绑定组装登录身份，并从服务端查询角色、权限和仓库范围。
     *
     * @param account 已查询到的商户账号
     * @return 认证服务可使用的商户登录身份
     */
    private MerchantLoginIdentity assemble(MerchantAccount account)
    {
        if (identityMapper.countActiveBindings(account.getUserId()) != 1)
        {
            throw new ServiceException("商户账号绑定异常，请联系平台管理员");
        }
        MerchantBinding binding = identityMapper.selectActiveBinding(account.getUserId());
        if (binding == null || !account.getTenantId().equals(binding.getTenantId())
                || !account.getMerchantId().equals(binding.getMerchantId()))
        {
            throw new ServiceException("商户账号与商户主体绑定不一致，请联系平台管理员");
        }

        MerchantLoginIdentity identity = new MerchantLoginIdentity();
        identity.setUserId(account.getUserId());
        identity.setUsername(account.getUsername());
        identity.setNickname(account.getNickname());
        identity.setPhone(account.getPhone());
        identity.setEmail(account.getEmail());
        identity.setAvatar(account.getAvatar());
        identity.setPassword(account.getPassword());
        identity.setStatus(account.getStatus());
        identity.setIsdeleted(account.getIsdeleted());
        identity.setPasswordUpdatedAt(account.getPasswordUpdateDate() == null
                ? null : account.getPasswordUpdateDate().getTime());
        identity.setMustChangePassword(account.getMustChangePassword());
        identity.setCreatedAt(account.getCreateTime() == null ? null : account.getCreateTime().getTime());
        identity.setTenantId(binding.getTenantId());
        identity.setMerchantId(binding.getMerchantId());
        String authStatus = identityMapper.selectMemberAuthStatusByPhone(account.getPhone());
        identity.setMerchantAuthStatus(authStatus);
        identity.setMemberRole(binding.getMemberRole());
        identity.setWarehouseScopeType(binding.getWarehouseScopeType());
        identity.setWarehouseIds(new LinkedHashSet<Long>(identityMapper.selectWarehouseIds(
                binding.getTenantId(), binding.getMerchantId(), account.getUserId())));
        if ("verified".equals(authStatus))
        {
            identity.setRoles(new LinkedHashSet<String>(identityMapper.selectRoleKeys(
                    binding.getTenantId(), binding.getMerchantId(), account.getUserId())));
            identity.setPermissions(new LinkedHashSet<String>(identityMapper.selectPermissions(
                    binding.getTenantId(), binding.getMerchantId(), account.getUserId())));
        }
        else
        {
            identity.setRoles(new LinkedHashSet<String>(Collections.singleton("merchant_default")));
            identity.setPermissions(new LinkedHashSet<String>(identityMapper.selectDefaultReadPermissions()));
        }
        return identity;
    }

    /**
     * 校验平台开户命令中的服务端归属主键、手机号和操作人员信息。
     *
     * @param command 待校验的商户所有者开户命令
     */
    private void validateProvision(MerchantOwnerProvisionCommand command)
    {
        if (command == null || command.getTenantId() == null || command.getMerchantId() == null)
        {
            throw new ServiceException("商户管理员开通参数不完整");
        }
        if (StringUtils.isBlank(command.getPhone()) || !command.getPhone().trim().matches("^1[3-9]\\d{9}$"))
        {
            throw new ServiceException("商户管理员手机号格式不正确");
        }
        if (StringUtils.isBlank(command.getOperator()))
        {
            command.setOperator("platform-audit");
        }
    }

    /**
     * 将没有自动开通账号的历史申请转换为可指导用户下一步操作的登录失败提示。
     *
     * @param authStatus 平台商户审核状态
     * @return 商户登录不可用原因
     */
    private String loginUnavailableMessage(String authStatus)
    {
        if ("pending".equals(authStatus))
        {
            return "商户资料正在审核中，商户管理员账号尚未完成开通";
        }
        if ("rejected".equals(authStatus))
        {
            return "商户资料审核未通过，商户管理员账号尚未完成开通";
        }
        if ("verified".equals(authStatus))
        {
            return "商户管理员账号尚未开通，请联系平台管理员";
        }
        return "手机号未提交商户申请";
    }

}
