package com.ruoyi.rentalbusiness.integration.amap;

/** 高德天气查询结果的内部标准快照。 */
public class AmapWeatherSnapshot
{
    /** 是否成功获得可展示天气。 */
    public boolean available;
    /** 请求高德天气接口使用的行政区划 adcode。 */
    public String adcode;
    /** 高德返回的城市名称。 */
    public String city;
    /** 天气预报日期，格式为 yyyy-MM-dd。 */
    public String forecastDate;
    /** 当前天气或预报天气。 */
    public String weather;
    /** 温度文本。 */
    public String temperature;
    /** 白天天气描述。 */
    public String dayWeather;
    /** 夜间天气描述。 */
    public String nightWeather;
    /** 白天温度。 */
    public String dayTemperature;
    /** 夜间温度。 */
    public String nightTemperature;
    /** 白天风向。 */
    public String dayWindDirection;
    /** 夜间风向。 */
    public String nightWindDirection;
    /** 白天风力。 */
    public String dayWindPower;
    /** 夜间风力。 */
    public String nightWindPower;
    /** 高德报告时间。 */
    public String reportTime;
    /** 前端天气图标类型。 */
    public String icon;
    /** 是否属于需要关注的天气。 */
    public boolean risk;
    /** 查询失败说明，不把远程响应原文暴露给前端。 */
    public String message;
    /** 当前预报日对应的高德原始 cast JSON，仅供服务端持久化和排障使用。 */
    public String rawDataJson;

    /**
     * 创建不可用天气结果。
     *
     * @param adcode 行政区划 adcode
     * @param message 不可用原因
     * @return 不可用结果
     */
    public static AmapWeatherSnapshot unavailable(String adcode, String message)
    {
        AmapWeatherSnapshot result = new AmapWeatherSnapshot();
        result.available = false;
        result.adcode = adcode;
        result.icon = "cloudy";
        result.message = message;
        return result;
    }
}
