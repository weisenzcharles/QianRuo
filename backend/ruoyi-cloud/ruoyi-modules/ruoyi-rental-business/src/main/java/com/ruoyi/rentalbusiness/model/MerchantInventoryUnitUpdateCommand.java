package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;

/** 商户库存单元编辑命令。 */
public class MerchantInventoryUnitUpdateCommand
{
    /** 库存单元唯一编号，仅用于服务端定位数据。 */
    public Long unitId;

    /** 新的实物序列号。 */
    public String serialNumber;

    /** 兼容字段改名前的商户端请求，服务端不会修改库存单元编码。 */
    @Deprecated
    public String unitCode;

    /** 设备状态，使用 RentalCode.InventoryStatus 的编码。 */
    public Integer unitStatus;

    /** 设备规格。 */
    public String memorySpec;

    /** 设备购入价格。 */
    public BigDecimal purchaseCost;

    /** 成色等级，使用 MerchantInventoryConditionLevel 的整型编码。 */
    public Integer conditionLevel;

    /** 保险到期日期，格式为 yyyy-MM-dd。 */
    public String insuranceUntil;

    /** 保险费用。 */
    public BigDecimal insuranceAmount;

    /** 实物序列号照片地址，保存后作为周转市场认证依据。 */
    public String serialPhotoUrl;

    /** 设备备注。 */
    public String remark;
}
