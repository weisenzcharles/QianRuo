package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 平台查看商户经营明细的聚合视图。
 *
 * <p>该视图同时作为平台服务与 rental-business 的内部契约以及平台只读接口的安全返回模型，
 * 不包含租户、商户、仓库、商品或套餐的技术主键。</p>
 */
public class PlatformMerchantBusinessView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String merchantName;
    private String memberCode;
    private List<PlatformMerchantWarehouseView> warehouses = Collections.emptyList();
    private List<PlatformMerchantProductView> products = Collections.emptyList();
    private List<PlatformMerchantPackageView> packages = Collections.emptyList();
    private List<PlatformMerchantInventoryView> inventories = Collections.emptyList();

    public String getMerchantName()
    {
        return merchantName;
    }

    public void setMerchantName(String merchantName)
    {
        this.merchantName = merchantName;
    }

    public String getMemberCode()
    {
        return memberCode;
    }

    public void setMemberCode(String memberCode)
    {
        this.memberCode = memberCode;
    }

    public List<PlatformMerchantWarehouseView> getWarehouses()
    {
        return warehouses;
    }

    public void setWarehouses(List<PlatformMerchantWarehouseView> warehouses)
    {
        this.warehouses = warehouses == null ? Collections.emptyList() : warehouses;
    }

    public List<PlatformMerchantProductView> getProducts()
    {
        return products;
    }

    public void setProducts(List<PlatformMerchantProductView> products)
    {
        this.products = products == null ? Collections.emptyList() : products;
    }

    public List<PlatformMerchantPackageView> getPackages()
    {
        return packages;
    }

    public void setPackages(List<PlatformMerchantPackageView> packages)
    {
        this.packages = packages == null ? Collections.emptyList() : packages;
    }

    public List<PlatformMerchantInventoryView> getInventories()
    {
        return inventories;
    }

    public void setInventories(List<PlatformMerchantInventoryView> inventories)
    {
        this.inventories = inventories == null ? Collections.emptyList() : inventories;
    }
}
