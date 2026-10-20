package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 平台追加售后处理事件请求。 */
public class PlatformAfterSalesEventCommand implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Integer toStatus;
    private String content;

    public Integer getToStatus() { return toStatus; }
    public void setToStatus(Integer value) { toStatus = value; }
    public String getContent() { return content; }
    public void setContent(String value) { content = value; }
}
