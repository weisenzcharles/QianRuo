package com.ruoyi.merchant.api.model;

import java.io.Serializable;
import java.util.List;

/**
 * 周转市场实物库存查询契约。
 *
 * <p>该对象只用于商户服务和租赁业务服务之间的内部调用，租户与商户范围由可信上下文填充，
 * 不允许由浏览器直接指定。</p>
 */
public class MerchantMarketInventoryQuery implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long tenantId;
    private Long memberId;
    private String brand;
    private String modelName;
    private String memorySpec;
    private List<Long> unitIds;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getMemorySpec() { return memorySpec; }
    public void setMemorySpec(String memorySpec) { this.memorySpec = memorySpec; }
    public List<Long> getUnitIds() { return unitIds; }
    public void setUnitIds(List<Long> unitIds) { this.unitIds = unitIds; }
}
