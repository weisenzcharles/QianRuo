package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.Date;

/** 平台售后证据视图。 */
public class PlatformAfterSalesEvidenceView implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Integer evidenceType;
    private String evidenceTypeName;
    private String fileUrl;
    private String fileName;
    private String fileMime;
    private String description;
    private String operatorName;
    private Date createTime;

    public Integer getEvidenceType() { return evidenceType; }
    public void setEvidenceType(Integer value) { evidenceType = value; }
    public String getEvidenceTypeName() { return evidenceTypeName; }
    public void setEvidenceTypeName(String value) { evidenceTypeName = value; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String value) { fileUrl = value; }
    public String getFileName() { return fileName; }
    public void setFileName(String value) { fileName = value; }
    public String getFileMime() { return fileMime; }
    public void setFileMime(String value) { fileMime = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String value) { operatorName = value; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date value) { createTime = value; }
}
