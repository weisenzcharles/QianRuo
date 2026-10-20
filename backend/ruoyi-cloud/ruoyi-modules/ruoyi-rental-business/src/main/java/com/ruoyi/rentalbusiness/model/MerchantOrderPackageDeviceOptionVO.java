package com.ruoyi.rentalbusiness.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/** 订单套餐逐件设备候选项。unitId 仅用于提交绑定，不作为商户可读文本展示。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantOrderPackageDeviceOptionVO
{
    /** 库存单元唯一编号，仅供绑定接口回传。 */
    public Long unitId;

    /** 库存单元编码，用于辅助识别设备。 */
    public String unitCode;

    /** 实物序列号。 */
    public String serialNumber;

    /** 商品名称。 */
    public String productName;

    /** 商品型号。 */
    public String modelName;

    /** 仓库名称。 */
    public String warehouseName;
}
