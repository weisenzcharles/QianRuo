package com.ruoyi.rentalbusiness.model;

/**
 * 详细地址解析结果。
 *
 * <p>provinceCode、cityCode、districtCode 均为行政区划 adcode；amapCityCode 保留导入数据中的
 * 高德 citycode，天气接口查询则使用 weatherAdcode() 返回的市级行政区划 adcode。</p>
 */
public class RegionAddressInfo
{
    /** 省级行政区划 adcode。 */
    public String provinceCode;

    /** 市级行政区划 adcode。 */
    public String cityCode;

    /** 区县级行政区划 adcode。 */
    public String districtCode;

    /** 省名称快照。 */
    public String provinceName;

    /** 市名称快照。 */
    public String cityName;

    /** 区县名称快照。 */
    public String districtName;

    /** 高德行政区划导入数据中的 citycode 快照，供物流等需要城市编码的业务使用。 */
    public String amapCityCode;

    /** 解析状态：success、partial、failed。 */
    public String parseStatus;

    /** 解析说明，便于历史记录审计和问题排查。 */
    public String parseMessage;

    /**
     * 返回天气接口应使用的市级行政区划编码。
     *
     * <p>高德天气服务按市级行政区返回预报，区县天气统一复用所属市级行政区数据，
     * 因此不能再把 districtCode 直接传给高德。</p>
     *
     * @return 市级 adcode；市级编码为空时返回 null
     */
    public String weatherAdcode()
    {
        if (cityCode != null && !cityCode.trim().isEmpty())
        {
            return cityCode.trim();
        }
        return null;
    }

    /**
     * 构造解析失败结果。
     *
     * @param message 失败原因
     * @return 解析失败结果
     */
    public static RegionAddressInfo failed(String message)
    {
        RegionAddressInfo result = new RegionAddressInfo();
        result.parseStatus = "failed";
        result.parseMessage = message;
        return result;
    }
}
