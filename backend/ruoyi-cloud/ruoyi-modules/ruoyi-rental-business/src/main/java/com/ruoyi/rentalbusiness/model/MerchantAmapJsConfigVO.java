package com.ruoyi.rentalbusiness.model;

/** 商户仓库地图选点所需的最小高德 JS API 配置。 */
public class MerchantAmapJsConfigVO
{
    /** 高德 Web 端 JS API Key。 */
    public String jsApiKey;

    /** 高德 JS API 安全密钥。 */
    public String securityJsCode;

    /**
     * 创建地图配置视图。
     *
     * @param jsApiKey Web 端 JS API Key
     * @param securityJsCode JS API 安全密钥
     */
    public MerchantAmapJsConfigVO(String jsApiKey, String securityJsCode)
    {
        this.jsApiKey = jsApiKey;
        this.securityJsCode = securityJsCode;
    }
}
