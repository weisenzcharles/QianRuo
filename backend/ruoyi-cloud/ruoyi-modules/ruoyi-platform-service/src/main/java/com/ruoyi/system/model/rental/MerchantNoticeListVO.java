package com.ruoyi.system.model.rental;

import java.util.Date;

/** 平台商户公告列表的最小视图。 */
public class MerchantNoticeListVO
{
    private Long merchantNoticeId;
    private String noticeTitle;
    private Integer noticeType;
    private Integer targetScope;
    private Integer status;
    private String targetMemberNames;
    private Date createTime;

    public Long getMerchantNoticeId()
    {
        return merchantNoticeId;
    }

    public void setMerchantNoticeId(Long merchantNoticeId)
    {
        this.merchantNoticeId = merchantNoticeId;
    }

    public String getNoticeTitle()
    {
        return noticeTitle;
    }

    public void setNoticeTitle(String noticeTitle)
    {
        this.noticeTitle = noticeTitle;
    }

    public Integer getNoticeType()
    {
        return noticeType;
    }

    public void setNoticeType(Integer noticeType)
    {
        this.noticeType = noticeType;
    }

    public Integer getTargetScope()
    {
        return targetScope;
    }

    public void setTargetScope(Integer targetScope)
    {
        this.targetScope = targetScope;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public String getTargetMemberNames()
    {
        return targetMemberNames;
    }

    public void setTargetMemberNames(String targetMemberNames)
    {
        this.targetMemberNames = targetMemberNames;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
