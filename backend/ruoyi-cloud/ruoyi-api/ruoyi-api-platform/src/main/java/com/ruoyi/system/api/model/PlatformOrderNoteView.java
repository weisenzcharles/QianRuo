package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 平台订单详情中的订单批注。 */
public class PlatformOrderNoteView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String noteContent;
    private Integer noteType;
    private String operatorName;
    private String createTime;

    public String getNoteContent() { return noteContent; }
    public void setNoteContent(String noteContent) { this.noteContent = noteContent; }
    public Integer getNoteType() { return noteType; }
    public void setNoteType(Integer noteType) { this.noteType = noteType; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
