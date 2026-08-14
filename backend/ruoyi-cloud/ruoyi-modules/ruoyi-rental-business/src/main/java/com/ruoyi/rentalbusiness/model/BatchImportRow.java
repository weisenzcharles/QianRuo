package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;
import com.ruoyi.common.core.annotation.Excel;

/**
 * 主数据批量导入行。
 *
 * <p>不同资源只读取自身字段，其余字段保留为空。使用统一 DTO 可以让导入模板、审计和错误报告保持一致。</p>
 */
public class BatchImportRow
{
    @Excel(name = "编码") public String code;
    @Excel(name = "名称") public String name;
    @Excel(name = "联系人") public String contactName;
    @Excel(name = "联系电话") public String contactPhone;
    @Excel(name = "城市") public String city;
    @Excel(name = "详细地址") public String address;
    @Excel(name = "商品名称") public String productName;
    @Excel(name = "资源池名称") public String poolName;
    @Excel(name = "资源类型") public String resourceType;
    @Excel(name = "兼容型号ID") public String compatibleModelIds;
    @Excel(name = "商品类型") public String productType;
    @Excel(name = "库存模式") public String stockMode;
    @Excel(name = "类目ID") public Long classId;
    @Excel(name = "型号ID") public Long modelId;
    @Excel(name = "资源池ID") public Long poolId;
    @Excel(name = "品牌") public String brand;
    @Excel(name = "型号") public String modelName;
    @Excel(name = "规格") public String memorySpec;
    @Excel(name = "销售价") public BigDecimal salePrice;
    @Excel(name = "商品ID") public Long productId;
    @Excel(name = "仓库ID") public Long warehouseId;
    @Excel(name = "SN编码") public String snCode;
    @Excel(name = "条码") public String barcode;
    @Excel(name = "资产状态") public String assetStatus;
    @Excel(name = "套餐名称") public String packageName;
    @Excel(name = "BOM组件JSON") public String components;
    @Excel(name = "启用状态") public Integer enabled;
    @Excel(name = "状态") public Integer status;
    @Excel(name = "备注") public String remark;
}
