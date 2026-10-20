package com.ruoyi.rentalbusiness.controller;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.constant.GlobalConfigConstants;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.rentalbusiness.config.GlobalConfigService;
import com.ruoyi.rentalbusiness.model.MerchantAmapJsConfigVO;

/** 商户端仓库地图选点配置接口。 */
@RestController
@RequestMapping("/merchant/amap")
public class MerchantAmapController extends BaseController
{
    private final GlobalConfigService globalConfigService;

    /**
     * 注入平台全局配置读取服务。
     *
     * @param globalConfigService 全局配置读取服务
     */
    public MerchantAmapController(GlobalConfigService globalConfigService)
    {
        this.globalConfigService = globalConfigService;
    }

    /** 查询仓库地图选点必需的 JS API 配置。 */
    @RequiresPermissions("merchant:warehouse:list")
    @GetMapping("/config")
    public AjaxResult getJsConfig()
    {
        String jsApiKey = globalConfigService.get(GlobalConfigConstants.AMAP_JS_API_KEY).trim();
        if (!StringUtils.hasText(jsApiKey))
        {
            return error("平台尚未配置高德地图 JS API Key");
        }
        String securityJsCode = globalConfigService.get(GlobalConfigConstants.AMAP_JS_SECURITY_CODE).trim();
        if (!StringUtils.hasText(securityJsCode))
        {
            return error("平台尚未配置高德地图 JS API 安全密钥");
        }
        return success(new MerchantAmapJsConfigVO(jsApiKey, securityJsCode));
    }
}
