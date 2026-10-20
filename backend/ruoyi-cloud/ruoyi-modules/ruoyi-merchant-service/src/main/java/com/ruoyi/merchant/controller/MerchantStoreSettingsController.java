package com.ruoyi.merchant.controller;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresLogin;
import com.ruoyi.merchant.application.MerchantStoreSettingsService;
import com.ruoyi.merchant.model.MerchantStoreProfileUpdateCommand;
import com.ruoyi.merchant.model.MerchantStoreSettingsUpdateCommand;

/** 商户店铺设置页面接口。商户范围只从当前登录上下文解析。 */
@RestController
@RequestMapping("/merchant/store/settings")
public class MerchantStoreSettingsController
{
    @Autowired
    private MerchantStoreSettingsService service;

    /** 查询店铺基础资料、偏好设置和当前账号是否有编辑权限。 */
    @RequiresLogin
    @GetMapping
    public AjaxResult get()
    {
        return AjaxResult.success(service.get());
    }

    /** 修改商户店铺基础资料。认证主体字段仍由平台审核流程维护。 */
    @RequiresLogin
    @Log(title = "店铺设置", businessType = BusinessType.UPDATE)
    @PutMapping("/profile")
    public AjaxResult updateProfile(@RequestBody MerchantStoreProfileUpdateCommand command)
    {
        return AjaxResult.success(service.updateProfile(command));
    }

    /** 修改订单、库存、配送和展示偏好。 */
    @RequiresLogin
    @Log(title = "店铺设置", businessType = BusinessType.UPDATE)
    @PutMapping("/preferences")
    public AjaxResult updatePreferences(@RequestBody MerchantStoreSettingsUpdateCommand command)
    {
        return AjaxResult.success(service.updateSettings(command));
    }

    /** 上传店铺 Logo 并立即写入商户主体资料。 */
    @RequiresLogin
    @Log(title = "店铺设置", businessType = BusinessType.UPDATE)
    @PostMapping("/logo")
    public AjaxResult uploadLogo(@RequestPart("file") MultipartFile file)
    {
        return AjaxResult.success(Collections.singletonMap("url", service.uploadLogo(file)));
    }

    /** 删除店铺 Logo。 */
    @RequiresLogin
    @Log(title = "店铺设置", businessType = BusinessType.UPDATE)
    @Transactional
    @DeleteMapping("/logo")
    public AjaxResult removeLogo()
    {
        service.removeLogo();
        return AjaxResult.success();
    }
}
