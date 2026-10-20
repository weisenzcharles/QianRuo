package com.ruoyi.merchant.model;

import java.util.Date;

/** 商户内部通知按员工展示的阅读统计。 */
public class MerchantInternalNoticeReadStatVO
{
    private String displayName;
    private String roleNames;
    private Integer readStatus;
    private String readStatusName;
    private Date readTime;

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public String getRoleNames()
    {
        return roleNames;
    }

    public void setRoleNames(String roleNames)
    {
        this.roleNames = roleNames;
    }

    public Integer getReadStatus()
    {
        return readStatus;
    }

    public void setReadStatus(Integer readStatus)
    {
        this.readStatus = readStatus;
        this.readStatusName = MerchantInternalNoticeReadStatus.labelOf(readStatus);
    }

    public String getReadStatusName()
    {
        return readStatusName;
    }

    public void setReadStatusName(String readStatusName)
    {
        this.readStatusName = readStatusName;
    }

    public Date getReadTime()
    {
        return readTime;
    }

    public void setReadTime(Date readTime)
    {
        this.readTime = readTime;
    }
}
