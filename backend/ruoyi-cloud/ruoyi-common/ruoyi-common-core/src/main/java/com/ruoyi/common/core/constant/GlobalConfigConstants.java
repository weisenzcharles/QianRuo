package com.ruoyi.common.core.constant;

/**
 * 全局配置键常量。
 *
 * <p>平台端的系统配置页面、平台内部配置接口以及商户业务服务均只能通过这里的
 * 常量读取配置键，避免不同服务各自拼接字符串导致配置失效或缓存不一致。</p>
 */
public final class GlobalConfigConstants
{
    /** 高德开放平台 Web 服务 API Key 列表，多个 Key 使用逗号、分号或换行分隔。 */
    public static final String AMAP_API_KEYS = "global.amap.api-keys";

    /** 高德地图 JS API Web 端 Key，供授权后的商户地图选点使用。 */
    public static final String AMAP_JS_API_KEY = "global.amap.js-api-key";

    /** 高德地图 JS API 安全密钥，需与对应 JS API Key 配套。 */
    public static final String AMAP_JS_SECURITY_CODE = "global.amap.js-security-code";

    /** 高德天气查询接口地址。 */
    public static final String AMAP_WEATHER_URL = "global.amap.weather-url";

    /** 阿里云短信功能开关。 */
    public static final String ALIYUN_SMS_ENABLED = "global.aliyun.sms.enabled";

    /** 阿里云短信服务接入点。 */
    public static final String ALIYUN_SMS_ENDPOINT = "global.aliyun.sms.endpoint";

    /** 阿里云短信签名名称。 */
    public static final String ALIYUN_SMS_SIGN_NAME = "global.aliyun.sms.sign-name";

    /** 商户登录验证码短信模板编码。 */
    public static final String ALIYUN_SMS_LOGIN_TEMPLATE_CODE = "global.aliyun.sms.login-template-code";

    /** 阿里云公共 AccessKey ID，短信与 OSS 共用。 */
    public static final String ALIYUN_ACCESS_KEY_ID = "global.aliyun.access-key-id";

    /** 阿里云公共 AccessKey Secret，短信与 OSS 共用。 */
    public static final String ALIYUN_ACCESS_KEY_SECRET = "global.aliyun.access-key-secret";

    /** 兼容历史调用方的短信 AccessKey ID 常量，实际配置键已统一为公共凭据。 */
    @Deprecated
    public static final String ALIYUN_SMS_ACCESS_KEY_ID = ALIYUN_ACCESS_KEY_ID;

    /** 兼容历史调用方的短信 AccessKey Secret 常量，实际配置键已统一为公共凭据。 */
    @Deprecated
    public static final String ALIYUN_SMS_ACCESS_KEY_SECRET = ALIYUN_ACCESS_KEY_SECRET;

    /** 阿里云 OSS 功能开关。 */
    public static final String ALIYUN_OSS_ENABLED = "global.aliyun.oss.enabled";

    /** 阿里云 OSS 服务 Endpoint。 */
    public static final String ALIYUN_OSS_ENDPOINT = "global.aliyun.oss.endpoint";

    /** 阿里云 OSS Bucket 名称。 */
    public static final String ALIYUN_OSS_BUCKET_NAME = "global.aliyun.oss.bucket-name";

    /** 阿里云 OSS 自定义域名或 CDN 根地址。 */
    public static final String ALIYUN_OSS_PUBLIC_BASE_URL = "global.aliyun.oss.public-base-url";

    /** 阿里云 OSS 对象根目录。 */
    public static final String ALIYUN_OSS_OBJECT_PREFIX = "global.aliyun.oss.object-prefix";

    /** 支付宝支付功能开关。 */
    public static final String ALIPAY_ENABLED = "global.alipay.enabled";

    /** 支付宝开放平台网关地址。 */
    public static final String ALIPAY_SERVER_URL = "global.alipay.server-url";

    /** 支付宝应用编号。 */
    public static final String ALIPAY_APP_ID = "global.alipay.app-id";

    /** 支付宝应用私钥。 */
    public static final String ALIPAY_PRIVATE_KEY = "global.alipay.private-key";

    /** 支付宝公钥。 */
    public static final String ALIPAY_PUBLIC_KEY = "global.alipay.public-key";

    /** 支付宝异步通知地址。 */
    public static final String ALIPAY_NOTIFY_URL = "global.alipay.notify-url";

    /** 支付宝同步跳转地址。 */
    public static final String ALIPAY_RETURN_URL = "global.alipay.return-url";

    /** 支付宝签名算法。 */
    public static final String ALIPAY_SIGN_TYPE = "global.alipay.sign-type";

    /** 支付宝字符集。 */
    public static final String ALIPAY_CHARSET = "global.alipay.charset";

    /** 支付宝报文格式。 */
    public static final String ALIPAY_FORMAT = "global.alipay.format";

    /** 支付宝支付订单有效期。 */
    public static final String ALIPAY_TIMEOUT_EXPRESS = "global.alipay.timeout-express";

    /** 判断配置键是否属于短信系统参数。 */
    public static boolean isAliyunSmsConfigKey(String configKey)
    {
        return ALIYUN_SMS_ENABLED.equals(configKey)
                || ALIYUN_SMS_ENDPOINT.equals(configKey)
                || ALIYUN_SMS_SIGN_NAME.equals(configKey)
                || ALIYUN_SMS_LOGIN_TEMPLATE_CODE.equals(configKey)
                || isAliyunCredentialKey(configKey);
    }

    /** 判断配置键是否属于 OSS 系统参数。 */
    public static boolean isAliyunOssConfigKey(String configKey)
    {
        return ALIYUN_OSS_ENABLED.equals(configKey)
                || ALIYUN_OSS_ENDPOINT.equals(configKey)
                || ALIYUN_OSS_BUCKET_NAME.equals(configKey)
                || ALIYUN_OSS_PUBLIC_BASE_URL.equals(configKey)
                || ALIYUN_OSS_OBJECT_PREFIX.equals(configKey)
                || isAliyunCredentialKey(configKey);
    }

    /** 判断配置键是否属于阿里云系统参数。 */
    public static boolean isAliyunConfigKey(String configKey)
    {
        return isAliyunSmsConfigKey(configKey) || isAliyunOssConfigKey(configKey);
    }

    /** 判断配置键是否保存阿里云公共凭据。 */
    public static boolean isAliyunCredentialKey(String configKey)
    {
        return ALIYUN_ACCESS_KEY_ID.equals(configKey) || ALIYUN_ACCESS_KEY_SECRET.equals(configKey);
    }

    /** 判断配置键是否属于支付宝系统参数。 */
    public static boolean isAlipayConfigKey(String configKey)
    {
        return ALIPAY_ENABLED.equals(configKey)
                || ALIPAY_SERVER_URL.equals(configKey)
                || ALIPAY_APP_ID.equals(configKey)
                || ALIPAY_PRIVATE_KEY.equals(configKey)
                || ALIPAY_PUBLIC_KEY.equals(configKey)
                || ALIPAY_NOTIFY_URL.equals(configKey)
                || ALIPAY_RETURN_URL.equals(configKey)
                || ALIPAY_SIGN_TYPE.equals(configKey)
                || ALIPAY_CHARSET.equals(configKey)
                || ALIPAY_FORMAT.equals(configKey)
                || ALIPAY_TIMEOUT_EXPRESS.equals(configKey);
    }

    /** 判断配置键是否保存支付宝凭据。 */
    public static boolean isAlipayCredentialKey(String configKey)
    {
        return ALIPAY_PRIVATE_KEY.equals(configKey) || ALIPAY_PUBLIC_KEY.equals(configKey);
    }

    /** 判断配置键是否由平台系统统一托管。 */
    public static boolean isManagedGlobalConfigKey(String configKey)
    {
        return isAliyunConfigKey(configKey) || isAlipayConfigKey(configKey);
    }

    /** 判断配置键是否必须脱敏展示。 */
    public static boolean isCredentialConfigKey(String configKey)
    {
        return isAliyunCredentialKey(configKey) || isAlipayCredentialKey(configKey);
    }

    /** 判断配置键是否保存阿里云凭据，保留历史方法名供既有代码使用。 */
    @Deprecated
    public static boolean isAliyunSmsCredentialKey(String configKey)
    {
        return isAliyunCredentialKey(configKey);
    }

    /** 顺丰开放平台合作伙伴编码。 */
    public static final String EXPRESS_SF_PARTNER_ID = "global.express.sf.partner-id";

    /** 顺丰开放平台合作伙伴密钥。 */
    public static final String EXPRESS_SF_PARTNER_KEY = "global.express.sf.partner-key";

    /** 顺丰轨迹查询接口地址。 */
    public static final String EXPRESS_SF_TRACKING_URL = "global.express.sf.tracking-url";

    /** 京东开放平台应用编码。 */
    public static final String EXPRESS_JD_APP_KEY = "global.express.jd.app-key";

    /** 京东开放平台应用密钥。 */
    public static final String EXPRESS_JD_APP_SECRET = "global.express.jd.app-secret";

    /** 京东物流轨迹查询接口地址。 */
    public static final String EXPRESS_JD_TRACKING_URL = "global.express.jd.tracking-url";

    /** 快递100客户编号。 */
    public static final String EXPRESS_KUAIDI100_CUSTOMER = "global.express.kuaidi100.customer";

    /** 快递100授权密钥。 */
    public static final String EXPRESS_KUAIDI100_KEY = "global.express.kuaidi100.key";

    /** 快递100轨迹查询接口地址。 */
    public static final String EXPRESS_KUAIDI100_TRACKING_URL = "global.express.kuaidi100.tracking-url";

    /** 快递100智能时效预估接口地址。 */
    public static final String EXPRESS_KUAIDI100_TIME_URL = "global.express.kuaidi100.time-url";

    /** 快递100智能时效预估接口密钥。 */
    public static final String EXPRESS_KUAIDI100_TIME_KEY = "global.express.kuaidi100.time-key";

    /** 快递100智能时效预估接口私密密钥。 */
    public static final String EXPRESS_KUAIDI100_TIME_SECRET = "global.express.kuaidi100.time-secret";

    /** 快递100智能时效预估结果缓存时长，单位为小时。 */
    public static final String EXPRESS_KUAIDI100_TIME_CACHE_TTL_HOURS =
            "global.express.kuaidi100.time-cache-ttl-hours";

    /** 全局配置缓存有效期配置键，默认值仍由代码常量兜底。 */
    public static final String CACHE_TTL_DAYS = "global.config.cache-ttl-days";

    /** 禁止实例化常量类。 */
    private GlobalConfigConstants()
    {
    }
}
