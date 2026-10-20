package com.ruoyi.system.api.model;

import java.io.Serializable;

/**
 * 外部接口异常告警命令。
 *
 * <p>该模型只在业务服务和平台治理服务之间传递，不暴露给浏览器端；告警业务键由调用方保证幂等。</p>
 */
public class PlatformExternalInterfaceAlertCommand implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String alertEventKey;
    private Integer alertLevel;
    private String title;
    private String detail;
    private String sourceKey;

    public String getAlertEventKey() { return alertEventKey; }
    public void setAlertEventKey(String value) { alertEventKey = value; }
    public Integer getAlertLevel() { return alertLevel; }
    public void setAlertLevel(Integer value) { alertLevel = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    public String getDetail() { return detail; }
    public void setDetail(String value) { detail = value; }
    public String getSourceKey() { return sourceKey; }
    public void setSourceKey(String value) { sourceKey = value; }
}
