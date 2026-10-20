package com.ruoyi.system.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.constant.GlobalConfigConstants;
import com.ruoyi.common.core.constant.UserConstants;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 参数配置 信息操作处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/config")
public class SysConfigController extends BaseController
{
    private static final String MASKED_CREDENTIAL = "********";

    @Autowired
    private ISysConfigService configService;

    /**
     * 获取参数配置列表
     */
    @RequiresPermissions("admin:config:list")
    @GetMapping("/list")
    public TableDataInfo list(SysConfig config)
    {
        startPage();
        List<SysConfig> list = configService.selectConfigList(config);
        maskConfigValues(list);
        return getDataTable(list);
    }

    @Log(title = "参数管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("admin:config:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysConfig config)
    {
        List<SysConfig> list = configService.selectConfigList(config);
        maskConfigValues(list);
        ExcelUtil<SysConfig> util = new ExcelUtil<SysConfig>(SysConfig.class);
        util.exportExcel(response, list, "参数数据");
    }

    /**
     * 根据参数编号获取详细信息
     */
    @RequiresPermissions("admin:config:query")
    @GetMapping(value = "/{configId}")
    public AjaxResult getInfo(@PathVariable Long configId)
    {
        SysConfig config = configService.selectConfigById(configId);
        if (config != null)
        {
            config.setConfigValue(maskCredentialForEdit(config.getConfigKey(), config.getConfigValue()));
        }
        return success(config);
    }

    /**
     * 根据参数键名查询参数值
     */
    @GetMapping(value = "/configKey/{configKey}")
    public AjaxResult getConfigKey(@PathVariable String configKey)
    {
        String configValue = configService.selectConfigByKey(configKey);
        return success(maskCredentialForDisplay(configKey, configValue));
    }

    /**
     * 新增参数配置
     */
    @RequiresPermissions("admin:config:add")
    @Log(title = "参数管理", businessType = BusinessType.INSERT, excludeParamNames = { "configValue" })
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysConfig config)
    {
        if (!isConfigValueValid(config))
        {
            return error("参数键值不能为空");
        }
        if (GlobalConfigConstants.isCredentialConfigKey(config.getConfigKey())
                && MASKED_CREDENTIAL.equals(config.getConfigValue()))
        {
            return error("请填写实际的系统凭据");
        }
        markManagedGlobalConfigAsBuiltIn(config);
        if (!configService.checkConfigKeyUnique(config))
        {
            return error("新增参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        config.setCreateBy(SecurityUtils.getUserId());
        return toAjax(configService.insertConfig(config));
    }

    /**
     * 修改参数配置
     */
    @RequiresPermissions("admin:config:edit")
    @Log(title = "参数管理", businessType = BusinessType.UPDATE, excludeParamNames = { "configValue" })
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysConfig config)
    {
        if (config.getConfigId() == null)
        {
            return error("配置唯一编号不能为空");
        }
        SysConfig storedConfig = configService.selectConfigById(config.getConfigId());
        if (storedConfig == null)
        {
            return error("参数配置不存在");
        }
        if (GlobalConfigConstants.isManagedGlobalConfigKey(storedConfig.getConfigKey())
                && !storedConfig.getConfigKey().equals(config.getConfigKey()))
        {
            return error("系统参数键名不可修改");
        }
        restoreMaskedCredential(config, storedConfig);
        if (!isConfigValueValid(config))
        {
            return error("参数键值不能为空");
        }
        markManagedGlobalConfigAsBuiltIn(config);
        if (!configService.checkConfigKeyUnique(config))
        {
            return error("修改参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        config.setUpdateBy(SecurityUtils.getUserId());
        return toAjax(configService.updateConfig(config));
    }

    private void maskConfigValues(List<SysConfig> configs)
    {
        for (SysConfig config : configs)
        {
            config.setConfigValue(maskCredentialForDisplay(config.getConfigKey(), config.getConfigValue()));
        }
    }

    private boolean isConfigValueValid(SysConfig config)
    {
        return GlobalConfigConstants.isCredentialConfigKey(config.getConfigKey())
                || GlobalConfigConstants.ALIYUN_OSS_PUBLIC_BASE_URL.equals(config.getConfigKey())
                || StringUtils.hasText(config.getConfigValue());
    }

    private void markManagedGlobalConfigAsBuiltIn(SysConfig config)
    {
        if (GlobalConfigConstants.isManagedGlobalConfigKey(config.getConfigKey()))
        {
            config.setConfigType(UserConstants.YES);
        }
    }

    static String maskCredentialForDisplay(String configKey, String configValue)
    {
        if (!GlobalConfigConstants.isCredentialConfigKey(configKey))
        {
            return configValue;
        }
        return StringUtils.hasText(configValue) ? "已配置" : "未配置";
    }

    static String maskCredentialForEdit(String configKey, String configValue)
    {
        if (!GlobalConfigConstants.isCredentialConfigKey(configKey) || !StringUtils.hasText(configValue))
        {
            return configValue;
        }
        return MASKED_CREDENTIAL;
    }

    static void restoreMaskedCredential(SysConfig submittedConfig, SysConfig storedConfig)
    {
        String configKey = submittedConfig.getConfigKey();
        if (GlobalConfigConstants.isCredentialConfigKey(configKey)
                && configKey.equals(storedConfig.getConfigKey())
                && MASKED_CREDENTIAL.equals(submittedConfig.getConfigValue()))
        {
            submittedConfig.setConfigValue(storedConfig.getConfigValue());
        }
    }

    /** @deprecated 使用统一的阿里云凭据脱敏方法。 */
    @Deprecated
    static String maskSmsCredentialForDisplay(String configKey, String configValue)
    {
        return maskCredentialForDisplay(configKey, configValue);
    }

    /** @deprecated 使用统一的阿里云凭据脱敏方法。 */
    @Deprecated
    static String maskSmsCredentialForEdit(String configKey, String configValue)
    {
        return maskCredentialForEdit(configKey, configValue);
    }

    /** @deprecated 使用统一的阿里云凭据恢复方法。 */
    @Deprecated
    static void restoreMaskedSmsCredential(SysConfig submittedConfig, SysConfig storedConfig)
    {
        restoreMaskedCredential(submittedConfig, storedConfig);
    }

    /**
     * 删除参数配置
     */
    @RequiresPermissions("admin:config:remove")
    @Log(title = "参数管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public AjaxResult remove(@PathVariable Long[] configIds)
    {
        configService.deleteConfigByIds(configIds);
        return success();
    }

    /**
     * 刷新参数缓存
     */
    @RequiresPermissions("admin:config:remove")
    @Log(title = "参数管理", businessType = BusinessType.CLEAN)
    @DeleteMapping("/refreshCache")
    public AjaxResult refreshCache()
    {
        configService.resetConfigCache();
        return success();
    }
}
