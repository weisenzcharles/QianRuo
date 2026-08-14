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
    public Long poolId;
    public Long classId;
    public Long modelId;

    public String code;
    public String name;
    public String poolCode;
    public String poolName;
    public String resourceType;
    public String productType;
    public String productCode;
    public String productName;
    public String brand;
    public String modelName;
    public String memorySpec;
    public String stockMode;
    public String snCode;
    public String barcode;
    public String itemCode;
    public String unitStatus;
    public String assetStatus;
    public String itemStatus;
    public String conditionLevel;
    public String packageCode;
    public String packageName;
    public String platformCode;
    public String componentRole;
    public String allocationPolicy;
    public String stockStatus;
    public String changeType;
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
    public Integer substitutableFlag;
    public BigDecimal purchaseCost;
    public BigDecimal salePrice;

    public List<Long> compatibleModelIds = new ArrayList<Long>();
    public List<RentalBusinessComponentCommand> components = new ArrayList<RentalBusinessComponentCommand>();
}
