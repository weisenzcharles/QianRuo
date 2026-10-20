package com.ruoyi.rentalbusiness.domain;

/** 发货日前一日天气批处理使用的最小订单投影。 */
public class WeatherRiskOrderCandidate
{
    /** 租户唯一编号。 */
    public Long tenantId;

    /** 商户唯一编号。 */
    public Long memberId;

    /** 订单唯一编号。 */
    public Long orderId;

    /** 订单编码，仅用于任务日志定位。 */
    public String orderNo;

    /** 发货仓市级地址文本。 */
    public String warehouseCity;

    /** 收货市级行政区划 adcode。 */
    public String recipientCityCode;

    /** 收货区县级行政区划 adcode，市级编码缺失时兜底。 */
    public String recipientDistrictCode;
}
