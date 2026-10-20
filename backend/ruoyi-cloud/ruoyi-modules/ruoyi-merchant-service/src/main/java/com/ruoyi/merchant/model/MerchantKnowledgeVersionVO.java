package com.ruoyi.merchant.model;

import java.util.List;

/** 商户知识文章版本视图。 */
public class MerchantKnowledgeVersionVO
{
    private Long versionId;
    private Integer versionNo;
    private String content;
    private Integer versionStatus;
    private String versionStatusName;
    private String retentionUntil;
    private String createTime;
    private List<MerchantKnowledgeAttachmentVO> attachments;

    public Long getVersionId() { return versionId; }
    public void setVersionId(Long versionId) { this.versionId = versionId; }
    public Integer getVersionNo() { return versionNo; }
    public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getVersionStatus() { return versionStatus; }
    public void setVersionStatus(Integer versionStatus) { this.versionStatus = versionStatus; }
    public String getVersionStatusName() { return versionStatusName; }
    public void setVersionStatusName(String versionStatusName) { this.versionStatusName = versionStatusName; }
    public String getRetentionUntil() { return retentionUntil; }
    public void setRetentionUntil(String retentionUntil) { this.retentionUntil = retentionUntil; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public List<MerchantKnowledgeAttachmentVO> getAttachments() { return attachments; }
    public void setAttachments(List<MerchantKnowledgeAttachmentVO> attachments) { this.attachments = attachments; }
}
