package com.ruoyi.merchant.api.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 商户用户展示摘要查询命令。
 *
 * <p>租户和商户唯一编号由内部业务服务根据已校验的订单上下文传入，商户服务再次在 SQL
 * 中校验，避免操作人名称解析成为跨商户数据泄露入口。</p>
 */
public class MerchantUserSummaryQuery implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long tenantId;
    private Long merchantId;
    private Set<Long> userIds = new LinkedHashSet<Long>();

    public Long getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(Long tenantId)
    {
        this.tenantId = tenantId;
    }

    public Long getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(Long merchantId)
    {
        this.merchantId = merchantId;
    }

    public Set<Long> getUserIds()
    {
        return userIds;
    }

    public void setUserIds(Set<Long> userIds)
    {
        this.userIds = userIds == null ? new LinkedHashSet<Long>() : new LinkedHashSet<Long>(userIds);
    }
}
