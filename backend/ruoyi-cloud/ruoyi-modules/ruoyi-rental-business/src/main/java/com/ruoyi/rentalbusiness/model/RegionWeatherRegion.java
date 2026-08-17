package com.ruoyi.rentalbusiness.model;

/**
 * 市级天气任务使用的行政区划投影。
 *
 * <p>该对象只保存任务查询和落库需要的字段，不直接暴露行政区划实体，避免天气任务把
 * 省、市、区县字典的其他内部字段带入第三方调用或天气扩展表。</p>
 */
public class RegionWeatherRegion
{
    /** 市级行政区划 adcode，也是天气记录的 region_code。 */
    public String regionCode;

    /** 市级行政区划名称。 */
    public String regionName;

    /** 市级行政区划 adcode。 */
    public String cityCode;

    /** 市级行政区划名称。 */
    public String cityName;

    /** 市级行政区划所属省级行政区划 adcode。 */
    public String provinceCode;

    /** 市级行政区划所属省名称。 */
    public String provinceName;

    /** 高德天气接口使用的市级查询 adcode。 */
    public String weatherAdcode;

    /** 行政区划导入的高德 citycode 快照，供天气记录和物流等业务保留。 */
    public String amapCityCode;
}
