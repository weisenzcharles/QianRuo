package com.ruoyi.system.controller.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.system.api.RemoteMerchantNoticeService;
import com.ruoyi.system.api.model.MerchantNoticeDetail;
import com.ruoyi.system.api.model.MerchantNoticeTop;
import com.ruoyi.system.service.rental.MerchantNoticeService;

/** 商户服务读取平台商户公告的内部接口。 */
@RestController
@RequestMapping(RemoteMerchantNoticeService.INTERNAL_PREFIX)
public class InternalMerchantNoticeController
{
    private final MerchantNoticeService noticeService;

    public InternalMerchantNoticeController(MerchantNoticeService noticeService)
    {
        this.noticeService = noticeService;
    }

    @InnerAuth
    @GetMapping("/top")
    public R<MerchantNoticeTop> top(@RequestParam Long memberId, @RequestParam Long merchantUserId,
            @RequestParam Integer limit)
    {
        return R.ok(noticeService.top(memberId, merchantUserId, limit));
    }

    @InnerAuth
    @GetMapping("/{noticeId}")
    public R<MerchantNoticeDetail> get(@PathVariable Long noticeId, @RequestParam Long memberId,
            @RequestParam Long merchantUserId)
    {
        try
        {
            return R.ok(noticeService.detail(noticeId, memberId, merchantUserId));
        }
        catch (ServiceException exception)
        {
            return R.fail(exception.getMessage());
        }
    }

    @InnerAuth
    @PostMapping("/{noticeId}/read")
    public R<Void> markRead(@PathVariable Long noticeId, @RequestParam Long memberId,
            @RequestParam Long merchantUserId)
    {
        try
        {
            noticeService.markRead(noticeId, memberId, merchantUserId);
            return R.ok();
        }
        catch (ServiceException exception)
        {
            return R.fail(exception.getMessage());
        }
    }

    @InnerAuth
    @PostMapping("/read-all")
    public R<Void> markReadAll(@RequestParam Long memberId, @RequestParam Long merchantUserId,
            @RequestParam String noticeIds)
    {
        try
        {
            Long[] ids = Convert.toLongArray(noticeIds);
            List<Long> list = ids == null ? Collections.<Long>emptyList() : Arrays.asList(ids);
            noticeService.markReadAll(list, memberId, merchantUserId);
            return R.ok();
        }
        catch (ServiceException exception)
        {
            return R.fail(exception.getMessage());
        }
    }
}
