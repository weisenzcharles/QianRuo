package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/** 平台联盟订单分页视图。 */
public class PlatformAllianceOrderPageView implements Serializable
{
    private static final long serialVersionUID = 1L;
    private List<PlatformAllianceOrderView> rows = Collections.emptyList();
    private long total;

    public List<PlatformAllianceOrderView> getRows() { return rows; }
    public void setRows(List<PlatformAllianceOrderView> value) { rows = value; }
    public long getTotal() { return total; }
    public void setTotal(long value) { total = value; }
}
