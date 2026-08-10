package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 租赁主数据统一展示对象，隐藏数据库表连接细节。 */
public class RentalBusinessVO
{
    public Long id;
    public Long warehouseId;
    public Long productId;
    public Long poolId;
    public Long packageId;
    public Long packageVersionId;
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
    public String stockMode;
    public String snCode;
    public String barcode;
    public String itemCode;
    public String assetStatus;
    public String itemStatus;
    public String conditionLevel;
    public String warehouseName;
    public String warehouseCity;
    public String packageCode;
    public String packageName;
    public String platformCode;
    public String versionStatus;
    public String componentRole;
    public String allocationPolicy;
    public String stockStatus;
    public String statusLabel;
    public String remark;
    public Integer status;
    public Integer enabled;
    public Integer sortNo;
    public Integer versionNo;
    public Integer requiredQty;
    public Integer quantity;
    public Integer onHandQty;
    public Integer reservedQty;
    public Integer availableQty;
    public Integer substitutableFlag;
    public BigDecimal purchaseCost;
    public BigDecimal salePrice;
    public List<RentalBusinessComponentVO> components = new ArrayList<RentalBusinessComponentVO>();
    public List<Long> compatibleModelIds = new ArrayList<Long>();
}
