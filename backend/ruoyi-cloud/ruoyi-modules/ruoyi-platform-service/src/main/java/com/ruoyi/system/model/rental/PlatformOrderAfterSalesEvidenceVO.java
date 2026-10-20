package com.ruoyi.system.model.rental;

/** 平台订单详情中的售后证据摘要。 */
public class PlatformOrderAfterSalesEvidenceVO
{
    private String evidenceTypeName;
    private String fileUrl;
    private String fileName;
    private String fileMime;
    private String description;
    private String operatorName;
    private String createTime;

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
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String value) { createTime = value; }
}
