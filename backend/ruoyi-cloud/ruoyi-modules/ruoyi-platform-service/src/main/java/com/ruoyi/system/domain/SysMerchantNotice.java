package com.ruoyi.system.domain;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.ruoyi.common.core.web.domain.BaseEntity;
import com.ruoyi.common.core.xss.Xss;

/** 平台发布给商户端的公告，独立于原有 sys_notice。 */
public class SysMerchantNotice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long merchantNoticeId;
    private String noticeTitle;
    private Integer noticeType;
    private String noticeContent;
    private Integer targetScope;
    private Integer status;
    private List<Long> targetMemberIds = new ArrayList<Long>();
    private String targetMemberNames;

    public Long getMerchantNoticeId()
    {
        return merchantNoticeId;
    }

    public void setMerchantNoticeId(Long merchantNoticeId)
    {
        this.merchantNoticeId = merchantNoticeId;
    }

    @Xss(message = "公告标题不能包含脚本字符")
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 50, message = "公告标题不能超过50个字符")
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

    public String getTargetMemberNames()
    {
        return targetMemberNames;
    }

    public void setTargetMemberNames(String targetMemberNames)
    {
        this.targetMemberNames = targetMemberNames;
    }
}
