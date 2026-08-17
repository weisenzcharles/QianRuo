package com.ruoyi.rentalbusiness.domain;

/**
 * 订单收件人历史快照。
 *
 * <p>一条记录代表订单一次保存时的收件人信息。订单主表保留当前值，该表保留每次变更
 * 的历史值，并按 order_no 建立关联，便于物流、天气和审计按订单编号追溯。</p>
 */
public class OrderRecipientHistoryRecord
{
    /** 历史快照主键。 */
    public Long recipientHistoryId;
    /** 联系人。 */
    public String contactName;
    /** 联系电话。 */
    public String contactPhone;
    /** 收件详细地址。 */
    public String contactAddress;
    /** 省级行政区划 adcode。 */
    public String provinceCode;
    /** 市级行政区划 adcode。 */
    public String cityCode;
    /** 区县级行政区划 adcode。 */
    public String districtCode;
    /** 导入的高德 citycode 快照；天气查询使用行政区划 adcode。 */
    public String amapCityCode;
    /** 省名称快照。 */
    public String provinceName;
    /** 市名称快照。 */
    public String cityName;
    /** 区县名称快照。 */
    public String districtName;
    /** 地址解析状态。 */
    public String parseStatus;
    /** 地址解析说明。 */
    public String parseMessage;

    /** 是否为订单当前使用的收件地址：1 是，0 否。 */
    public Integer used;
}
