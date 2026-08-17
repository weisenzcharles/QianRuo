package com.ruoyi.rentalbusiness.domain;

import java.sql.Date;

/**
 * 市级天气扩展表记录。
 *
 * <p>一条记录表示一个市级行政区在一个预报日期的天气快照。天气数据与行政区划字典、业务订单
 * 解耦，便于订单、库存和首页统计按日期读取，同时保留第三方返回的原始单日 JSON 供排障。</p>
 */
public class RegionWeatherRecord
{
    /** 天气记录主键。 */
    public Long weatherId;

    /** 市级行政区划 adcode。 */
    public String regionCode;

    /** 市级行政区划名称快照。 */
    public String regionName;

    /** 市级行政区划 adcode。 */
    public String cityCode;

    /** 市级行政区划名称快照。 */
    public String cityName;

    /** 所属省级行政区划 adcode。 */
    public String provinceCode;

    /** 所属省名称快照。 */
    public String provinceName;

    /** 行政区划导入的高德 citycode 快照，天气请求使用市级 regionCode。 */
    public String amapCityCode;

    /** 预报日期。 */
    public Date weatherDate;

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

    /** 天气图标类型。 */
    public String weatherIcon;

    /** 是否为需要关注的风险天气：0 否，1 是。 */
    public Integer riskFlag;

    /** 查询状态：success、failed。 */
    public String queryStatus;

    /** 查询结果说明。 */
    public String queryMessage;

    /** 高德报告时间。 */
    public String reportTime;

    /** 高德单日预报原始 JSON。 */
    public String rawDataJson;
}
