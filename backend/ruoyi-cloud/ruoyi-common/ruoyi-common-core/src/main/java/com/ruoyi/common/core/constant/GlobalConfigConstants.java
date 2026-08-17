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

    /** 高德天气查询接口地址。 */
    public static final String AMAP_WEATHER_URL = "global.amap.weather-url";

    /** 全局配置缓存有效期配置键，默认值仍由代码常量兜底。 */
    public static final String CACHE_TTL_DAYS = "global.config.cache-ttl-days";

    /** 禁止实例化常量类。 */
    private GlobalConfigConstants()
    {
    }
}
