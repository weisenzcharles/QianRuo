package com.ruoyi.system.api.model;

import java.io.Serializable;

/** 商户端公告详情的最小视图。 */
public class MerchantNoticeDetail implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 公告唯一编号，仅供详情查询内部传递。 */
    private Long noticeId;

    /** 公告标题。 */
    private String noticeTitle;

    /** 公告类型，1通知、2公告。 */
    private Integer noticeType;

    /** 公告正文。 */
    private String noticeContent;

    /** 发布人展示名称，不返回平台用户唯一编号。 */
    private String publisherName;

    /** 发布时间。 */
    private String createTime;

    public Long getNoticeId() { return noticeId; }
    public void setNoticeId(Long noticeId) { this.noticeId = noticeId; }
    public String getNoticeTitle() { return noticeTitle; }
    public void setNoticeTitle(String noticeTitle) { this.noticeTitle = noticeTitle; }
    public Integer getNoticeType() { return noticeType; }
    public void setNoticeType(Integer noticeType) { this.noticeType = noticeType; }
    public String getNoticeContent() { return noticeContent; }
    public void setNoticeContent(String noticeContent) { this.noticeContent = noticeContent; }
    public String getPublisherName() { return publisherName; }
    public void setPublisherName(String publisherName) { this.publisherName = publisherName; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
