package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 平台追加售后证据请求，文件内容由统一文件服务上传后只提交对象地址。 */
public class PlatformAfterSalesEvidenceCommand implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Integer evidenceType;
    private String fileUrl;
    private String fileName;
    private String fileMime;
    private String description;

    public Integer getEvidenceType()
    {
        return evidenceType;
    }

    public void setEvidenceType(Integer value)
    {
        evidenceType = value;
    }

    public String getFileUrl()
    {
        return fileUrl;
    }

    public void setFileUrl(String value)
    {
        fileUrl = value;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String value)
    {
        fileName = value;
    }

    public String getFileMime()
    {
        return fileMime;
    }

    public void setFileMime(String value)
    {
        fileMime = value;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String value)
    {
        description = value;
    }
}
