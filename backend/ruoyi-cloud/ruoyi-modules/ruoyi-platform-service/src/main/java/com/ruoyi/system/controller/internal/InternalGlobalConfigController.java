package com.ruoyi.system.controller.internal;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.system.api.RemoteGlobalConfigService;
import com.ruoyi.system.service.ISysConfigService;

/** 平台全局配置内部读取入口。 */
@RestController
@RequestMapping(RemoteGlobalConfigService.INTERNAL_PREFIX)
public class InternalGlobalConfigController
{
    private final ISysConfigService configService;

    /**
     * 注入系统配置服务。
     *
     * @param configService 系统配置服务
     */
    public InternalGlobalConfigController(ISysConfigService configService)
    {
        this.configService = configService;
    }

    /**
     * 为可信业务服务读取一个全局配置值。
     *
     * @param configKey 配置键
     * @return 配置值
     */
    @InnerAuth
    @GetMapping("/value")
    public R<String> getValue(@RequestParam String configKey)
    {
        if (!StringUtils.hasText(configKey))
        {
            return R.fail("全局配置键不能为空");
        }
        return R.ok(configService.selectConfigByKey(configKey.trim()));
    }
}
