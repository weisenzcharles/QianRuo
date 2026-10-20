package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 平台联盟订单筛选条件。编号字段仅用于内部查询，不直接展示给用户。 */
public class PlatformAllianceOrderQuery implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long allianceId;
    private Long sourceMemberId;
    private Long targetMemberId;
    private Integer status;
    /** 订单视图：1 协助订单（排除公众池），2 公众池订单。 */
    private Integer viewMode;
    private String beginTime;
    private String endTime;
    private Integer pageNum;
    private Integer pageSize;

    public Long getAllianceId() { return allianceId; }
    public void setAllianceId(Long value) { allianceId = value; }
    public Long getSourceMemberId() { return sourceMemberId; }
    public void setSourceMemberId(Long value) { sourceMemberId = value; }
    public Long getTargetMemberId() { return targetMemberId; }
    public void setTargetMemberId(Long value) { targetMemberId = value; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer value) { status = value; }
    public Integer getViewMode() { return viewMode; }
    public void setViewMode(Integer value) { viewMode = value; }
    public String getBeginTime() { return beginTime; }
    public void setBeginTime(String value) { beginTime = value; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String value) { endTime = value; }
    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer value) { pageNum = value; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer value) { pageSize = value; }
}
