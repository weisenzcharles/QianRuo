package com.ruoyi.system.model.rental;

import java.util.ArrayList;
import java.util.List;

/** 平台商户公告编辑详情的最小视图。 */
public class MerchantNoticeEditVO
{
    private Long merchantNoticeId;
    private String noticeTitle;
    private Integer noticeType;
    private String noticeContent;
    private Integer targetScope;
    private Integer status;
    private List<Long> targetMemberIds = new ArrayList<Long>();
    private String remark;

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

    public String getNoticeContent()
    {
        return noticeContent;
    }

    public void setNoticeContent(String noticeContent)
    {
        this.noticeContent = noticeContent;
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

    public List<Long> getTargetMemberIds()
    {
        return targetMemberIds;
    }

    public void setTargetMemberIds(List<Long> targetMemberIds)
    {
        this.targetMemberIds = targetMemberIds == null ? new ArrayList<Long>() : targetMemberIds;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}
