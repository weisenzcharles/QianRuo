package com.ruoyi.rentalbusiness.model;

/**
 * 行政区划解析节点。
 *
 * <p>该对象同时保存系统行政区划 adcode 和导入的高德 citycode 快照，解析器只依赖该
 * 投影，不直接依赖数据库字段名称；天气查询使用行政区划 adcode，不使用 citycode。</p>
 */
public class RegionNode
{
    /** 行政区划 adcode。 */
    public String code;

    /** 父级行政区划 adcode。 */
    public String parentCode;

    /** 行政区划层级：1 省，2 市，3 区县。 */
    public Integer level;

    /** 行政区划名称。 */
    public String name;

    /** 行政区划导入的高德 citycode，供级联和物流等城市编码场景使用。 */
    public String amapCityCode;
}
