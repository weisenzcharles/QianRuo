package com.ruoyi.system.controller;

import java.util.Arrays;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.UserConstants;
import com.ruoyi.common.core.context.ClientContextHolder;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.enums.FileBusinessType;
import com.ruoyi.common.core.enums.SystemCode;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.utils.file.FileTypeUtils;
import com.ruoyi.common.core.utils.file.MimeTypeUtils;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.service.TokenService;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.api.RemoteFileService;
import com.ruoyi.system.api.domain.SysFile;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.system.api.model.LoginUser;
import com.ruoyi.system.service.ISysUserService;

/**
 * 个人信息 业务处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/user/profile")
public class SysProfileController extends BaseController
{
    @Autowired
    private ISysUserService userService;
    
    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private RemoteFileService remoteFileService;


    /**
     * 个人信息
     */
    @GetMapping
    public AjaxResult profile()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (!isExpectedClient(loginUser, Constants.CLIENT_TYPE_PLATFORM))
        {
            return error("当前接口仅支持平台用户");
        }
        String username = SecurityUtils.getUsername();
        SysUser user = userService.selectUserByUserName(username);
        AjaxResult ajax = AjaxResult.success(user);
        ajax.put("roleGroup", userService.selectUserRoleGroup(username));
        ajax.put("postGroup", userService.selectUserPostGroup(username));
        return ajax;
    }

    /**
     * 修改用户
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult updateProfile(@RequestBody SysUser user)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (!isExpectedClient(loginUser, Constants.CLIENT_TYPE_PLATFORM))
        {
            return error("当前接口仅支持平台用户");
        }
        SysUser currentUser = loginUser.getSysUser();
        currentUser.setNickName(user.getNickName());
        currentUser.setEmail(user.getEmail());
        currentUser.setPhonenumber(user.getPhonenumber());
        currentUser.setSex(user.getSex());
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(currentUser))
        {
            return error("修改用户'" + loginUser.getUsername() + "'失败，手机号码已存在");
        }
        if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(currentUser))
        {
            return error("修改用户'" + loginUser.getUsername() + "'失败，邮箱账号已存在");
        }
        if (userService.updateUserProfile(currentUser))
        {
            // 更新缓存用户信息
            tokenService.setLoginUser(loginUser);
            return success();
        }
        return error("修改个人信息异常，请联系管理员");
    }

    /**
     * 重置密码
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/updatePwd")
    public AjaxResult updatePwd(@RequestBody Map<String, String> params)
    {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (!isExpectedClient(loginUser, Constants.CLIENT_TYPE_PLATFORM))
        {
            return error("当前接口仅支持平台用户");
        }
        Long userId = loginUser.getUserid();
        String password = loginUser.getSysUser().getPassword();
        if (SystemCode.DefaultFlag.YES.getCode().equals(loginUser.getSysUser().getMustChangePassword()))
        {
            return error("请先完成初始密码设置");
        }
        if (!SecurityUtils.matchesPassword(oldPassword, password))
        {
            return error("修改密码失败，旧密码错误");
        }
        if (SecurityUtils.matchesPassword(newPassword, password))
        {
            return error("新密码不能与旧密码相同");
        }
        newPassword = SecurityUtils.encryptPassword(newPassword);
        int rows = userService.updateUserPwd(userId, newPassword);
        if (rows > 0)
        {
            // 更新缓存用户密码&密码最后更新时间
            loginUser.getSysUser().setPwdUpdateDate(DateUtils.getNowDate());
            loginUser.getSysUser().setPassword(newPassword);
            tokenService.setLoginUser(loginUser);
            return success();
        }
        return error("修改密码异常，请联系管理员");
    }

    /**
     * 首次登录设置密码
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/admin/initPassword")
    public AjaxResult initAdminPassword(@RequestBody Map<String, String> params)
    {
        return initPassword(params, Constants.CLIENT_TYPE_PLATFORM);
    }

    private AjaxResult initPassword(Map<String, String> params, String expectedClientType)
    {
        String newPassword = params.get("newPassword");
        if (StringUtils.isEmpty(newPassword))
        {
            return error("新密码不能为空");
        }
        if (newPassword.length() < UserConstants.PASSWORD_MIN_LENGTH
                || newPassword.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            return error("用户密码长度必须在5到20个字符之间");
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (!isExpectedClient(loginUser, expectedClientType))
        {
            return error("登录客户端不匹配，请重新登录");
        }
        Long userId = loginUser.getUserid();
        SysUser user = userService.selectUserById(userId);
        boolean mustChangePassword = StringUtils.isNotNull(user)
                && SystemCode.DefaultFlag.YES.getCode().equals(user.getMustChangePassword());
        if (!mustChangePassword)
        {
            return error("当前账号已设置密码，请使用修改密码功能");
        }
        newPassword = SecurityUtils.encryptPassword(newPassword);
        int rows = userService.initUserPwd(userId, newPassword);
        if (rows > 0)
        {
            loginUser.getSysUser().setPwdUpdateDate(DateUtils.getNowDate());
            loginUser.getSysUser().setPassword(newPassword);
            loginUser.getSysUser().setMustChangePassword(SystemCode.DefaultFlag.NO.getCode());
            tokenService.setLoginUser(loginUser);
            return success();
        }
        return error("设置密码异常，请联系管理员");
    }

    private boolean isCurrentClient(LoginUser loginUser)
    {
        return StringUtils.isNotNull(loginUser)
                && ClientContextHolder.getClientType().equals(loginUser.getClientType());
    }

    private boolean isExpectedClient(LoginUser loginUser, String expectedClientType)
    {
        return isCurrentClient(loginUser) && expectedClientType.equals(loginUser.getClientType());
    }
    
    /**
     * 头像上传
     */
    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
    @PostMapping("/avatar")
    public AjaxResult avatar(@RequestParam("avatarfile") MultipartFile file)
    {
        if (!file.isEmpty())
        {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (!isExpectedClient(loginUser, Constants.CLIENT_TYPE_PLATFORM))
            {
                return error("当前接口仅支持平台用户");
            }
            String extension = FileTypeUtils.getExtension(file);
            if (!StringUtils.equalsAnyIgnoreCase(extension, MimeTypeUtils.IMAGE_EXTENSION))
            {
                return error("文件格式不正确，请上传 " + Arrays.toString(MimeTypeUtils.IMAGE_EXTENSION) + " 格式");
            }
            R<SysFile> fileResult = remoteFileService.upload(file, FileBusinessType.PLATFORM_USER.getCode());
            if (StringUtils.isNull(fileResult) || StringUtils.isNull(fileResult.getData()))
            {
                return error("文件服务异常，请联系管理员");
            }
            String url = fileResult.getData().getUrl();
            if (userService.updateUserAvatar(loginUser.getUserid(), url))
            {
                String oldAvatarUrl = loginUser.getSysUser().getAvatar();
                if (StringUtils.isNotEmpty(oldAvatarUrl))
                {
                    remoteFileService.delete(oldAvatarUrl);
                }
                AjaxResult ajax = AjaxResult.success();
                ajax.put("imgUrl", url);
                // 更新缓存用户头像
                loginUser.getSysUser().setAvatar(url);
                tokenService.setLoginUser(loginUser);
                return ajax;
            }
        }
        return error("上传图片异常，请联系管理员");
    }
}
