package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/** 平台联盟纠纷分页视图。 */
public class PlatformAllianceDisputePageView implements Serializable
{
    private static final long serialVersionUID = 1L;
    private List<PlatformAllianceDisputeView> rows = Collections.emptyList();
    private long total;

    public List<PlatformAllianceDisputeView> getRows() { return rows; }
    public void setRows(List<PlatformAllianceDisputeView> value) { rows = value; }
    public long getTotal() { return total; }
    public void setTotal(long value) { total = value; }
}
