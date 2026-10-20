package com.ruoyi.auth.service;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.constant.CacheConstants;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.UserConstants;
import com.ruoyi.common.core.context.ClientContextHolder;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.enums.SoftDeleteStatus;
import com.ruoyi.common.core.enums.SystemCode;
import com.ruoyi.common.core.enums.UserStatus;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.utils.ip.IpUtils;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.common.log.service.MerchantBusinessAuditRecorder;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.merchant.api.RemoteMerchantUserService;
import com.ruoyi.merchant.api.model.MerchantLoginIdentity;
import com.ruoyi.merchant.api.model.MerchantLoginRecordCommand;
import com.ruoyi.system.api.RemoteUserService;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.system.api.model.LoginUser;

/**
 * 登录校验方法
 * 
 * @author ruoyi
 */
@Component
public class SysLoginService
{
    @Autowired
    private RemoteUserService remoteUserService;

    @Autowired
    private RemoteMerchantUserService remoteMerchantUserService;

    @Autowired
    private SysPasswordService passwordService;

    @Autowired
    private SysRecordLogService recordLogService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MerchantSmsLoginCodeService smsLoginCodeService;

    @Autowired
    private MerchantBusinessAuditRecorder auditRecorder;

    /**
     * 登录
     */
    public LoginUser login(String username, String password)
    {
        // 用户名或密码为空 错误
        if (StringUtils.isAnyBlank(username, password))
        {
            recordLogService.recordLogininfor(username, SystemCode.LoginStatus.FAILED.getCode(), "用户/密码必须填写");
            throw new ServiceException("用户/密码必须填写");
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            recordLogService.recordLogininfor(username, SystemCode.LoginStatus.FAILED.getCode(), "用户密码不在指定范围");
            throw new ServiceException("用户密码不在指定范围");
        }
        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            recordLogService.recordLogininfor(username, SystemCode.LoginStatus.FAILED.getCode(), "用户名不在指定范围");
            throw new ServiceException("用户名不在指定范围");
        }
        // IP黑名单校验
        String blackStr = Convert.toStr(redisService.getCacheObject(CacheConstants.SYS_LOGIN_BLACKIPLIST));
        if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr()))
        {
            recordLogService.recordLogininfor(username, SystemCode.LoginStatus.FAILED.getCode(), "很遗憾，访问IP已被列入系统黑名单");
            throw new ServiceException("很遗憾，访问IP已被列入系统黑名单");
        }
        String clientType = ClientContextHolder.getClientType();
        if (!StringUtils.equalsAny(clientType, Constants.CLIENT_TYPE_PLATFORM, Constants.CLIENT_TYPE_MERCHANT))
        {
            throw new ServiceException("无效的终端类型");
        }
        // 查询用户信息
        R<LoginUser> userResult = Constants.CLIENT_TYPE_MERCHANT.equals(clientType)
                ? getMerchantLoginUser(username)
                : remoteUserService.getAdminUserInfo(username, SecurityConstants.INNER);

        if (R.FAIL == userResult.getCode())
        {
            throw new ServiceException(userResult.getMsg());
        }

        LoginUser userInfo = userResult.getData();
        SysUser user = userResult.getData().getSysUser();
        if (SoftDeleteStatus.DELETED.getCode().equals(user.getIsDelete()))
        {
            recordLogService.recordLogininfor(username, SystemCode.LoginStatus.FAILED.getCode(), "对不起，您的账号已被删除");
            throw new ServiceException("对不起，您的账号：" + username + " 已被删除");
        }
        if (UserStatus.DISABLE.getCode().equals(user.getStatus()))
        {
            recordLogService.recordLogininfor(username, SystemCode.LoginStatus.FAILED.getCode(), "用户已停用，请联系管理员");
            throw new ServiceException("对不起，您的账号：" + username + " 已停用");
        }
        passwordService.validate(user, password);
        userInfo.setClientType(clientType);
        recordLogService.recordLogininfor(username, SystemCode.LoginStatus.SUCCESS.getCode(), "登录成功");
        recordLoginInfo(user.getUserId());
        auditMerchantLogin(userInfo, username, "账号密码登录");
        return userInfo;
    }

    /** 手机验证码登录。 */
    public LoginUser smsLogin(String phone, String code)
    {
        if (!ClientContextHolder.isMerchantClient())
        {
            throw new ServiceException("手机号验证码登录仅支持商家端");
        }
        phone = phone == null ? null : phone.trim();
        String maskedPhone = MerchantSmsLoginCodeService.maskPhone(phone);
        if (StringUtils.isBlank(phone))
        {
            recordLogService.recordLogininfor(maskedPhone, SystemCode.LoginStatus.FAILED.getCode(), "手机号必须填写");
            throw new ServiceException("手机号必须填写");
        }
        if (StringUtils.isBlank(code) || !code.matches("\\d{4}"))
        {
            recordLogService.recordLogininfor(maskedPhone, SystemCode.LoginStatus.FAILED.getCode(), "请输入四位验证码");
            throw new ServiceException("请输入四位验证码");
        }
        String blackStr = Convert.toStr(redisService.getCacheObject(CacheConstants.SYS_LOGIN_BLACKIPLIST));
        if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr()))
        {
            recordLogService.recordLogininfor(maskedPhone, SystemCode.LoginStatus.FAILED.getCode(), "很遗憾，访问IP已被列入系统黑名单");
            throw new ServiceException("很遗憾，访问IP已被列入系统黑名单");
        }

        smsLoginCodeService.verifyAndConsume(phone, code);

        R<MerchantLoginIdentity> identityResult = remoteMerchantUserService.getByPhone(phone,
                SecurityConstants.INNER, Constants.CLIENT_TYPE_MERCHANT);
        R<LoginUser> userResult = adaptMerchantResult(identityResult);
        if (R.FAIL == userResult.getCode())
        {
            throw new ServiceException(userResult.getMsg());
        }

        LoginUser userInfo = userResult.getData();
        SysUser user = userInfo.getSysUser();
        if (SoftDeleteStatus.DELETED.getCode().equals(user.getIsDelete()))
        {
            recordLogService.recordLogininfor(maskedPhone, SystemCode.LoginStatus.FAILED.getCode(), "对不起，您的账号已被删除");
            throw new ServiceException("对不起，您的账号已被删除");
        }
        if (UserStatus.DISABLE.getCode().equals(user.getStatus()))
        {
            recordLogService.recordLogininfor(maskedPhone, SystemCode.LoginStatus.FAILED.getCode(), "用户已停用，请联系管理员");
            throw new ServiceException("对不起，您的账号已停用");
        }
        userInfo.setClientType(Constants.CLIENT_TYPE_MERCHANT);
        recordLogService.recordLogininfor(maskedPhone, SystemCode.LoginStatus.SUCCESS.getCode(), "手机号验证码登录成功");
        recordLoginInfo(user.getUserId());
        auditMerchantLogin(userInfo, maskedPhone, "手机号验证码登录");
        return userInfo;
    }

    /**
     * 请求发送商户登录验证码。
     *
     * @param phone 商户手机号
     */
    public void sendSmsLoginCode(String phone)
    {
        if (!ClientContextHolder.isMerchantClient())
        {
            throw new ServiceException("手机号验证码登录仅支持商家端");
        }
        smsLoginCodeService.sendLoginCode(phone);
    }

    /**
     * 为商户登录写入业务审计；平台用户登录只保留平台登录日志，不进入商户审计表。
     *
     * @param loginUser 成功登录的商户用户
     * @param loginName 登录名或手机号
     * @param description 登录方式描述
     */
    private void auditMerchantLogin(LoginUser loginUser, String loginName, String description)
    {
        if (loginUser == null || !Constants.CLIENT_TYPE_MERCHANT.equals(loginUser.getClientType())
                || loginUser.getSysUser() == null)
        {
            return;
        }
        com.ruoyi.merchant.api.model.MerchantBusinessAuditCommand command =
                new com.ruoyi.merchant.api.model.MerchantBusinessAuditCommand();
        command.setTenantId(loginUser.getTenantId());
        command.setMemberId(loginUser.getMemberId());
        command.setOperatorUserId(loginUser.getSysUser().getUserId());
        command.setOperatorName(loginName);
        command.setBusinessModule("用户认证");
        command.setOperationType("LOGIN");
        command.setTargetType("MERCHANT_USER");
        command.setTargetId(String.valueOf(loginUser.getSysUser().getUserId()));
        command.setTargetName(loginName);
        command.setDescription(description);
        command.setIpAddress(IpUtils.getIpAddr());
        command.setStatus(0);
        // 登录成功后会话已经建立，审计服务短暂不可用不能阻断登录；业务变更仍使用严格审计入口。
        auditRecorder.recordBestEffort(command);
    }

    /**
     * 记录登录信息
     *
     * @param userId 用户唯一编号
     */
    public void recordLoginInfo(Long userId)
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        // 更新用户登录IP
        sysUser.setLoginIp(IpUtils.getIpAddr());
        // 更新用户登录时间
        sysUser.setLoginDate(DateUtils.getNowDate());
        if (ClientContextHolder.isMerchantClient())
        {
            MerchantLoginRecordCommand command = new MerchantLoginRecordCommand();
            command.setUserId(userId);
            command.setLoginIp(sysUser.getLoginIp());
            command.setLoginAt(sysUser.getLoginDate().getTime());
            remoteMerchantUserService.recordLogin(command, SecurityConstants.INNER,
                    Constants.CLIENT_TYPE_MERCHANT);
        }
        else
        {
            remoteUserService.recordAdminLogin(sysUser, SecurityConstants.INNER);
        }
    }

    /**
     * 退出
     */
    public void logout(String loginName)
    {
        recordLogService.recordLogininfor(loginName, SystemCode.LoginStatus.SUCCESS.getCode(), "退出成功");
    }

    /**
     * 解锁
     */
    public void unlock(String password)
    {
        String username = SecurityUtils.getUsername();
        // 或密码为空 错误
        if (StringUtils.isEmpty(password))
        {
            throw new ServiceException("密码不能为空");
        }
        // 查询用户信息
        R<LoginUser> userResult = ClientContextHolder.isMerchantClient()
                ? getMerchantLoginUser(username)
                : remoteUserService.getAdminUserInfo(username, SecurityConstants.INNER);

        if (R.FAIL == userResult.getCode())
        {
            throw new ServiceException(userResult.getMsg());
        }

        SysUser user = userResult.getData().getSysUser();
        if (!SecurityUtils.matchesPassword(password, user.getPassword()))
        {
            throw new ServiceException("密码错误，请重新输入");
        }
    }

    /**
     * 注册
     */
    public void register(String username, String password)
    {
        // 用户名或密码为空 错误
        if (StringUtils.isAnyBlank(username, password))
        {
            throw new ServiceException("用户/密码必须填写");
        }
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            throw new ServiceException("账户长度必须在2到20个字符之间");
        }
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            throw new ServiceException("密码长度必须在5到20个字符之间");
        }

        // 注册用户信息
        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);
        sysUser.setNickName(username);
        sysUser.setPwdUpdateDate(DateUtils.getNowDate());
        sysUser.setPassword(SecurityUtils.encryptPassword(password));
        R<?> registerResult = remoteUserService.registerUserInfo(sysUser, SecurityConstants.INNER);

        if (R.FAIL == registerResult.getCode())
        {
            throw new ServiceException(registerResult.getMsg());
        }
        recordLogService.recordLogininfor(username, SystemCode.LoginStatus.SUCCESS.getCode(), "注册成功");
    }

    /**
     * 按商户用户名调用独立商户身份服务并转换为统一登录结果。
     *
     * @param username 商户登录用户名
     * @return 统一登录用户结果
     */
    private R<LoginUser> getMerchantLoginUser(String username)
    {
        return adaptMerchantResult(remoteMerchantUserService.getByUsername(username, SecurityConstants.INNER,
                Constants.CLIENT_TYPE_MERCHANT));
    }

    /**
     * 将商户身份内部契约转换为认证中心使用的登录用户对象。
     *
     * @param result 商户身份服务返回结果
     * @return 统一登录用户结果
     */
    private R<LoginUser> adaptMerchantResult(R<MerchantLoginIdentity> result)
    {
        if (result == null || R.FAIL == result.getCode() || result.getData() == null)
        {
            return R.fail(result == null ? "商户身份服务无响应" : result.getMsg());
        }
        MerchantLoginIdentity identity = result.getData();
        SysUser user = new SysUser();
        user.setUserId(identity.getUserId());
        user.setUserName(identity.getUsername());
        user.setNickName(identity.getNickname());
        user.setPhonenumber(identity.getPhone());
        user.setEmail(identity.getEmail());
        user.setAvatar(identity.getAvatar());
        user.setPassword(identity.getPassword());
        user.setStatus(identity.getStatus());
        user.setIsDelete(identity.getIsDelete());
        user.setPwdUpdateDate(identity.getPasswordUpdatedAt() == null
                ? null : new Date(identity.getPasswordUpdatedAt()));
        user.setMustChangePassword(identity.getMustChangePassword());

        LoginUser loginUser = new LoginUser();
        loginUser.setClientType(Constants.CLIENT_TYPE_MERCHANT);
        loginUser.setTenantId(identity.getTenantId());
        loginUser.setMemberId(identity.getMerchantId());
        loginUser.setMerchantAuthStatus(identity.getMerchantAuthStatus());
        loginUser.setMemberRole(identity.getMemberRole());
        loginUser.setWarehouseScopeType(identity.getWarehouseScopeType());
        loginUser.setWarehouseIds(identity.getWarehouseIds());
        loginUser.setRoles(identity.getRoles());
        loginUser.setPermissions(identity.getPermissions());
        loginUser.setSysUser(user);
        return R.ok(loginUser);
    }
}
