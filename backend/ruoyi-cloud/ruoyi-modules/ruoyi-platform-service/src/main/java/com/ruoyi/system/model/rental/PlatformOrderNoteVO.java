package com.ruoyi.system.model.rental;

/** 平台订单详情批注。 */
public class PlatformOrderNoteVO
{
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
