package com.ruoyi.merchant.application;

import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.api.RemoteMerchantNoticeService;
import com.ruoyi.system.api.model.MerchantNoticeDetail;
import com.ruoyi.system.api.model.MerchantNoticeTop;

/** 商户端公告应用服务，使用认证上下文访问平台可见公告。 */
@Service
public class MerchantNoticeService
{
    private static final Logger log = LoggerFactory.getLogger(MerchantNoticeService.class);

    private final RemoteMerchantNoticeService remoteService;

    public MerchantNoticeService(RemoteMerchantNoticeService remoteService)
    {
        this.remoteService = remoteService;
    }

    /** 查询当前商户用户的顶部公告。平台公告服务不可用时返回空结果，不能阻断首页。 */
    public MerchantNoticeTop top()
    {
        R<MerchantNoticeTop> response = remoteService.top(MerchantContextUtils.getMemberId(),
                SecurityUtils.getUserId(), 5, SecurityConstants.INNER);
        if (response == null || R.FAIL == response.getCode() || response.getData() == null)
        {
            log.warn("平台商户公告查询失败，商户顶部公告降级为空列表");
            MerchantNoticeTop empty = new MerchantNoticeTop();
            empty.setNotices(Collections.emptyList());
            empty.setUnreadCount(0);
            return empty;
        }
        return response.getData();
    }

    /** 查询当前商户用户可见的公告详情。 */
    public MerchantNoticeDetail detail(Long noticeId)
    {
        R<MerchantNoticeDetail> response = remoteService.get(noticeId, MerchantContextUtils.getMemberId(),
                SecurityUtils.getUserId(), SecurityConstants.INNER);
        if (response == null || R.FAIL == response.getCode() || response.getData() == null)
        {
            throw new ServiceException(response == null ? "平台公告服务无响应" : response.getMsg());
        }
        return response.getData();
    }

    /** 标记当前商户用户已读一条公告。 */
    public void markRead(Long noticeId)
    {
        R<Void> response = remoteService.markRead(noticeId, MerchantContextUtils.getMemberId(),
                SecurityUtils.getUserId(), SecurityConstants.INNER);
        if (response == null || R.FAIL == response.getCode())
        {
            throw new ServiceException(response == null ? "平台公告服务无响应" : response.getMsg());
        }
    }

    /** 批量标记当前顶部公告已读。 */
    public void markReadAll(String noticeIds)
    {
        if (noticeIds == null || noticeIds.trim().isEmpty())
        {
            return;
        }
        R<Void> response = remoteService.markReadAll(MerchantContextUtils.getMemberId(), SecurityUtils.getUserId(),
                noticeIds, SecurityConstants.INNER);
        if (response == null || R.FAIL == response.getCode())
        {
            throw new ServiceException(response == null ? "平台公告服务无响应" : response.getMsg());
        }
    }
}
