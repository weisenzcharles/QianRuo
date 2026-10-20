package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/** 平台订单分页视图。 */
public class PlatformOrderPageView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private List<PlatformOrderView> rows = Collections.emptyList();
    private long total;

    public List<PlatformOrderView> getRows() { return rows; }
    public void setRows(List<PlatformOrderView> rows) { this.rows = rows; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
}
