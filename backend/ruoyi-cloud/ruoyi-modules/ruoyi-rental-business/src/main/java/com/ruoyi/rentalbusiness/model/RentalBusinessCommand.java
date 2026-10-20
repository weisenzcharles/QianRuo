package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 租赁主数据写入命令。
 *
 * <p>六类主数据共用该命令只用于减少重复 DTO；具体字段由对应接口和应用服务
 * 校验，客户端提交的 tenantId、memberId、createBy 等归属字段不会被读取。</p>
 */
public class RentalBusinessCommand
{
    public Long id;
    public Long warehouseId;
    public Long productId;
    public Long snId;
    public Long accessoryItemId;
    public Long packageId;
    public Long classId;
    public Long modelId;
    /** 商户自定义配件兼容的多个平台型号唯一编号。 */
    public List<Long> compatibleModelIds = new ArrayList<Long>();

    public String code;
    public String name;
    public Integer productType;
    /** 是否商户自定义配件；服务端按商品类型重新计算，不信任客户端传值。 */
    public Integer customFlag;
    public String productCode;
    public String productName;
    public String brand;
    public String modelName;
    public String memorySpec;
    public String description;
    public Integer stockMode;
    public String snCode;
    public String barcode;
    public String itemCode;
    public Integer unitStatus;
    public Integer assetStatus;
    public Integer itemStatus;
    public String conditionLevel;
    public String packageCode;
    public String packageName;
    public String platformCode;
    public Integer componentRole;
    public Integer allocationPolicy;
    public Integer stockStatus;
    public Integer changeType;
    public String reason;
    public String contactName;
    public String contactPhone;
    public String province;
    public String city;
    public String district;
    public String address;
    /** 仓库经度。 */
    public BigDecimal longitude;
    /** 仓库纬度。 */
    public BigDecimal latitude;
    /** 上门取件规则 JSON。 */
    public String pickupRuleJson;
    public String remark;

    public Integer status;
    /** 是否允许作为归还仓：1 是，0 否。 */
    public Integer returnEnabled;
    /** 是否允许参与租商联盟共享：1 是，0 否。 */
    public Integer allianceShareEnabled;
    public Integer enabled;
    public Integer sortNo;
    public Integer requiredQty;
    public Integer quantity;
    public Integer changeQty;
    public BigDecimal purchaseCost;
    public BigDecimal salePrice;

    public List<RentalBusinessComponentCommand> components = new ArrayList<RentalBusinessComponentCommand>();
}
