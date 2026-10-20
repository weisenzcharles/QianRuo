package com.ruoyi.merchant.model;

import java.util.ArrayList;
import java.util.List;

/** 商户顶部消息提醒聚合视图。 */
public class MerchantInternalNoticeTopVO
{
    private List<MerchantInternalNoticeVO> notices = new ArrayList<MerchantInternalNoticeVO>();
    private int unreadCount;

    public List<MerchantInternalNoticeVO> getNotices() { return notices; }
    public void setNotices(List<MerchantInternalNoticeVO> notices) { this.notices = notices == null
            ? new ArrayList<MerchantInternalNoticeVO>() : notices; }
    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}
