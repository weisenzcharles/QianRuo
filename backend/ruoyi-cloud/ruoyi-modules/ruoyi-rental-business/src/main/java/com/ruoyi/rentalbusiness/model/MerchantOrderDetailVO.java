package com.ruoyi.rentalbusiness.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 商户订单详情展示对象。
 *
 * <p>保留编辑页面需要的订单字段，同时补充详情弹窗使用的展示名称和套餐快照。
 * 套餐明细由订单下单时的快照生成，避免目录变化后详情内容失真。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantOrderDetailVO extends MerchantOrderCreateCommand
{
    /** 渠道展示名称。 */
    public String channel;

    /** 发货仓库展示名称。 */
    public String warehouseName;

    /** 归还仓库展示名称。 */
    public String returnWarehouseName;

    /** 商品展示名称或型号。 */
    public String model;

    /** 商品名称。 */
    public String productName;

    /** 商品业务编码。 */
    public String productCode;

    /** 套餐展示名称。 */
    public String packageName;

    /** 格式化后的租赁时间。 */
    public String rentalPeriod;

    /** 格式化后的归还日期。 */
    public String returnDate;

    /** 格式化后的创建时间。 */
    public String createdAt;

    /** 套餐明细名称列表，兼容订单列表和旧页面。 */
    public List<String> packageItems;

    /** 订单下单时固化的套餐明细。 */
    public MerchantOrderListVO.PackageDetail packageDetail;

    /** 订单下单时固化的额外配件。 */
    public List<String> addOns;
}
