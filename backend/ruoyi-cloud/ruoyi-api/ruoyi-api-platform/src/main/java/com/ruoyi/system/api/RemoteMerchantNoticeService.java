package com.ruoyi.system.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.factory.RemoteMerchantNoticeFallbackFactory;
import com.ruoyi.system.api.model.MerchantNoticeDetail;
import com.ruoyi.system.api.model.MerchantNoticeTop;

/** 商户服务访问平台商户公告的内部契约。 */
@FeignClient(contextId = "remoteMerchantNoticeService", value = ServiceNameConstants.PLATFORM_SERVICE,
        fallbackFactory = RemoteMerchantNoticeFallbackFactory.class)
public interface RemoteMerchantNoticeService
{
    /** 平台商户公告内部接口前缀。 */
    String INTERNAL_PREFIX = "/internal/platform/merchant-notices";

    /** 查询当前商户用户的最新公告和未读数量。 */
    @GetMapping(INTERNAL_PREFIX + "/top")
    R<MerchantNoticeTop> top(@RequestParam("memberId") Long memberId,
            @RequestParam("merchantUserId") Long merchantUserId, @RequestParam("limit") Integer limit,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 查询当前商户可见的公告详情。 */
    @GetMapping(INTERNAL_PREFIX + "/{noticeId}")
    R<MerchantNoticeDetail> get(@PathVariable("noticeId") Long noticeId,
            @RequestParam("memberId") Long memberId, @RequestParam("merchantUserId") Long merchantUserId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 标记当前商户用户已读一条公告。 */
    @PostMapping(INTERNAL_PREFIX + "/{noticeId}/read")
    R<Void> markRead(@PathVariable("noticeId") Long noticeId, @RequestParam("memberId") Long memberId,
            @RequestParam("merchantUserId") Long merchantUserId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /** 批量标记当前顶部列表中的公告已读。 */
    @PostMapping(INTERNAL_PREFIX + "/read-all")
    R<Void> markReadAll(@RequestParam("memberId") Long memberId,
            @RequestParam("merchantUserId") Long merchantUserId, @RequestParam("noticeIds") String noticeIds,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
