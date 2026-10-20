package com.ruoyi.rentalbusiness.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 快递时效预估持久化记录。只保存线路编码、时长和供应商缓存元数据。 */
public class ExpressTimeEstimateRecord
{
    /** 时效预估记录唯一编号。 */
    public Long estimateId;

    /** 寄件地三级行政区划 adcode。 */
    public String fromAcode;

    /** 收件地三级行政区划 adcode。 */
    public String toAcode;

    /** 平台统一快递公司编码。 */
    public String expressCompanyCode;

    /** 快递产品类型，空值表示默认类型。 */
    public String expressType;

    /** 预计配送耗时，单位为天。 */
    public BigDecimal deliveryExpendTime;

    /** 预计总耗时，单位为小时。 */
    public BigDecimal totalTime;

    /** 预计剩余耗时，单位为小时。 */
    public BigDecimal remainTime;

    /** 时效数据供应商编码。 */
    public String providerCode;

    /** 最近一次供应商查询时间。 */
    public LocalDateTime lastQueryTime;

    /** 时效结果失效时间。 */
    public LocalDateTime expireTime;

    /** 创建时间。 */
    public LocalDateTime createTime;

    /** 更新时间。 */
    public LocalDateTime updateTime;
}
