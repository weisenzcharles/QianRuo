package com.ruoyi.system.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** 商户端顶部公告聚合结果。 */
public class MerchantNoticeTop implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 当前商户可见的最新公告。 */
    private List<MerchantNoticeSummary> notices = new ArrayList<MerchantNoticeSummary>();

    /** 当前商户用户未读公告数量。 */
    private int unreadCount;

    public List<MerchantNoticeSummary> getNotices() { return notices; }
    public void setNotices(List<MerchantNoticeSummary> notices)
    {
        this.notices = notices == null ? new ArrayList<MerchantNoticeSummary>() : notices;
    }
    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}
