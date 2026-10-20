package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/** 平台物流治理分页结果。 */
public class PlatformLogisticsPageView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private List<PlatformLogisticsView> rows = Collections.emptyList();
    private long total;

    public List<PlatformLogisticsView> getRows() { return rows; }
    public void setRows(List<PlatformLogisticsView> value) { rows = value; }
    public long getTotal() { return total; }
    public void setTotal(long value) { total = value; }
}
