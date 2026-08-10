package com.ruoyi.merchant.controller;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.constant.UserConstants;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.utils.file.FileTypeUtils;
import com.ruoyi.common.core.utils.file.MimeTypeUtils;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.merchant.api.model.MerchantLoginIdentity;
import com.ruoyi.merchant.api.model.MerchantFileInfo;
import com.ruoyi.merchant.api.RemoteMerchantFileService;
import com.ruoyi.merchant.application.MerchantIdentityService;
import com.ruoyi.merchant.application.MerchantSessionService;
import com.ruoyi.merchant.domain.MerchantAccount;
import com.ruoyi.merchant.mapper.MerchantIdentityMapper;
import com.ruoyi.merchant.model.MerchantPasswordCommand;
import com.ruoyi.merchant.model.MerchantProfileUpdateCommand;
import com.ruoyi.common.core.domain.R;

/** 商户个人中心。 */
@RestController
@RequestMapping("/merchant/user/profile")
public class MerchantProfileController
{
    @Autowired
    private MerchantIdentityMapper identityMapper;

    @Autowired
    private MerchantIdentityService identityService;

    @Autowired
    private MerchantSessionService sessionService;

    @Autowired
    private RemoteMerchantFileService remoteFileService;

    @GetMapping
    public AjaxResult profile()
    {
        MerchantLoginIdentity identity = refreshSession();
        AjaxResult result = AjaxResult.success(sessionService.toProfile(identity));
        result.put("roleGroup", "商户用户");
        result.put("postGroup", "");
        return result;
    }

    @Log(title = "商户个人信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult update(@RequestBody MerchantProfileUpdateCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        MerchantAccount current = requireCurrentAccount();
        MerchantAccount phoneAccount = StringUtils.isEmpty(command.getPhonenumber())
                ? null : identityMapper.selectByPhone(command.getPhonenumber());
        if (phoneAccount != null && !phoneAccount.getUserId().equals(current.getUserId()))
        {
            return AjaxResult.error("手机号码已被其他商户用户使用");
        }
        MerchantAccount emailAccount = StringUtils.isEmpty(command.getEmail())
                ? null : identityMapper.selectByEmail(command.getEmail());
        if (emailAccount != null && !emailAccount.getUserId().equals(current.getUserId()))
        {
            return AjaxResult.error("邮箱已被其他商户用户使用");
        }
        int rows = identityMapper.updateProfile(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), current.getUserId(), command.getNickName(),
                command.getPhonenumber(), command.getEmail(), SecurityUtils.getUsername());
        if (rows <= 0)
        {
            return AjaxResult.error("修改个人信息异常，请联系管理员");
        }
        refreshSession();
        return AjaxResult.success();
    }

    @Log(title = "商户个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/updatePwd")
    public AjaxResult updatePassword(@RequestBody MerchantPasswordCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        MerchantAccount account = requireCurrentAccount();
        if (!validPassword(command.getNewPassword()))
        {
            return AjaxResult.error("用户密码长度必须在5到20个字符之间");
        }
        if (Boolean.TRUE.equals(account.getMustChangePassword()))
        {
            return AjaxResult.error("请先完成初始密码设置");
        }
        if (!SecurityUtils.matchesPassword(command.getOldPassword(), account.getPassword()))
        {
            return AjaxResult.error("修改密码失败，旧密码错误");
        }
        if (SecurityUtils.matchesPassword(command.getNewPassword(), account.getPassword()))
        {
            return AjaxResult.error("新密码不能与旧密码相同");
        }
        return savePassword(command.getNewPassword(), false);
    }

    @Log(title = "商户个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/initPassword")
    public AjaxResult initializePassword(@RequestBody MerchantPasswordCommand command)
    {
        MerchantAccount account = requireCurrentAccount();
        if (!Boolean.TRUE.equals(account.getMustChangePassword()))
        {
            return AjaxResult.error("当前账号已设置密码，请使用修改密码功能");
        }
        return savePassword(command.getNewPassword(), true);
    }

    @Log(title = "商户用户头像", businessType = BusinessType.UPDATE)
    @PostMapping("/avatar")
    public AjaxResult avatar(@RequestParam("avatarfile") MultipartFile file)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (file.isEmpty())
        {
            return AjaxResult.error("请选择头像文件");
        }
        String extension = FileTypeUtils.getExtension(file);
        if (!StringUtils.equalsAnyIgnoreCase(extension, MimeTypeUtils.IMAGE_EXTENSION))
        {
            return AjaxResult.error("文件格式不正确，请上传" + Arrays.toString(MimeTypeUtils.IMAGE_EXTENSION) + "格式");
        }
        R<MerchantFileInfo> fileResult = remoteFileService.upload(file);
        if (fileResult == null || fileResult.getData() == null)
        {
            return AjaxResult.error("文件服务异常，请联系管理员");
        }
        MerchantAccount account = requireCurrentAccount();
        String oldAvatar = account.getAvatar();
        String avatar = fileResult.getData().getUrl();
        int rows = identityMapper.updateAvatar(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                account.getUserId(), avatar, SecurityUtils.getUsername());
        if (rows <= 0)
        {
            return AjaxResult.error("上传图片异常，请联系管理员");
        }
        if (StringUtils.isNotEmpty(oldAvatar))
        {
            remoteFileService.delete(oldAvatar);
        }
        refreshSession();
        AjaxResult result = AjaxResult.success();
        result.put("imgUrl", avatar);
        return result;
    }

    private AjaxResult savePassword(String password, boolean initialOnly)
    {
        if (!validPassword(password))
        {
            return AjaxResult.error("用户密码长度必须在5到20个字符之间");
        }
        int rows = identityMapper.updatePassword(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), SecurityUtils.getUserId(),
                SecurityUtils.encryptPassword(password), initialOnly);
        if (rows <= 0)
        {
            return AjaxResult.error(initialOnly ? "设置密码异常，请联系管理员" : "修改密码异常，请联系管理员");
        }
        refreshSession();
        return AjaxResult.success();
    }

    static boolean validPassword(String password)
    {
        return StringUtils.isNotEmpty(password) && password.length() >= UserConstants.PASSWORD_MIN_LENGTH
                && password.length() <= UserConstants.PASSWORD_MAX_LENGTH;
    }

    private MerchantAccount requireCurrentAccount()
    {
        MerchantAccount account = identityMapper.selectById(SecurityUtils.getUserId());
        if (account == null || !MerchantContextUtils.getTenantId().equals(account.getTenantId())
                || !MerchantContextUtils.getMemberId().equals(account.getMerchantId()))
        {
            throw new ServiceException("商户登录上下文与账号归属不一致");
        }
        return account;
    }

    private MerchantLoginIdentity refreshSession()
    {
        MerchantLoginIdentity identity = identityService.loadById(SecurityUtils.getUserId());
        sessionService.refresh(identity);
        return identity;
    }
}
