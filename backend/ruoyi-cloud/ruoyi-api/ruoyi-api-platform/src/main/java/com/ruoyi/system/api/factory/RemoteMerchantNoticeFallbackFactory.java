package com.ruoyi.system.api.factory;

import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.RemoteMerchantNoticeService;
import com.ruoyi.system.api.model.MerchantNoticeTop;

/** 平台商户公告内部调用降级处理。 */
@Component
public class RemoteMerchantNoticeFallbackFactory implements FallbackFactory<RemoteMerchantNoticeService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteMerchantNoticeFallbackFactory.class);

    @Override
    public RemoteMerchantNoticeService create(Throwable cause)
    {
        log.error("平台商户公告服务调用失败: {}", cause == null ? "未知异常" : cause.getMessage(), cause);
        return new RemoteMerchantNoticeService()
        {
            @Override
            public R<MerchantNoticeTop> top(Long memberId, Long merchantUserId, Integer limit, String source)
            {
                MerchantNoticeTop empty = new MerchantNoticeTop();
                empty.setNotices(Collections.emptyList());
                empty.setUnreadCount(0);
                return R.ok(empty);
            }

            @Override
            public R<com.ruoyi.system.api.model.MerchantNoticeDetail> get(Long noticeId, Long memberId,
                    Long merchantUserId, String source)
            {
                return R.fail("平台商户公告服务暂不可用");
            }

            @Override
            public R<Void> markRead(Long noticeId, Long memberId, Long merchantUserId, String source)
            {
                return R.fail("平台商户公告服务暂不可用");
            }

            @Override
            public R<Void> markReadAll(Long memberId, Long merchantUserId, String noticeIds, String source)
            {
                return R.fail("平台商户公告服务暂不可用");
            }
        };
    }
}
