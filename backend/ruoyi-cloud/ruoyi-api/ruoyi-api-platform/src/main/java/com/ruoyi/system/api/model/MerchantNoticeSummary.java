package com.ruoyi.system.api.model;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;

/** 商户端顶部公告列表的最小条目。 */
public class MerchantNoticeSummary implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 公告唯一编号，仅供已读接口内部传递。 */
    private Long noticeId;

    /** 公告标题。 */
    private String noticeTitle;

    /** 公告类型，1通知、2公告。 */
    private Integer noticeType;

    /** 创建时间。 */
    private String createTime;

    /** 当前商户用户是否已读。 */
    @JsonProperty("isRead")
    private boolean isRead;

    public Long getNoticeId() { return noticeId; }
    public void setNoticeId(Long noticeId) { this.noticeId = noticeId; }
    public String getNoticeTitle() { return noticeTitle; }
    public void setNoticeTitle(String noticeTitle) { this.noticeTitle = noticeTitle; }
    public Integer getNoticeType() { return noticeType; }
    public void setNoticeType(Integer noticeType) { this.noticeType = noticeType; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public boolean getIsRead() { return isRead; }
    public void setIsRead(boolean isRead) { this.isRead = isRead; }
}
