package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/** 平台售后案件分页视图。 */
public class PlatformAfterSalesPageView implements Serializable
{
    private static final long serialVersionUID = 1L;
    private List<PlatformAfterSalesView> rows = Collections.emptyList();
    private long total;

    public List<PlatformAfterSalesView> getRows() { return rows; }
    public void setRows(List<PlatformAfterSalesView> value) { rows = value; }
    public long getTotal() { return total; }
    public void setTotal(long value) { total = value; }
}
