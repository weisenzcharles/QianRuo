package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/** 平台物流赔付单分页视图。 */
public class PlatformLogisticsClaimPageView implements Serializable
{
    private static final long serialVersionUID = 1L;
    private List<PlatformLogisticsClaimView> rows = Collections.emptyList();
    private long total;

    public List<PlatformLogisticsClaimView> getRows() { return rows; }
    public void setRows(List<PlatformLogisticsClaimView> value) { rows = value; }
    public long getTotal() { return total; }
    public void setTotal(long value) { total = value; }
}
