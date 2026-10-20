package com.ruoyi.system.model.rental;

/** 平台知识文章版本附件入参。 */
public class PlatformKnowledgeAttachmentCommand
{
    private String attachmentName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;

    public String getAttachmentName() { return attachmentName; }
    public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
}
