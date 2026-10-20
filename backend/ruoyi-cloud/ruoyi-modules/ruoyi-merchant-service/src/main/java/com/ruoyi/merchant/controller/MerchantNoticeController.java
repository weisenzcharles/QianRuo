package com.ruoyi.merchant.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.merchant.application.MerchantNoticeService;

/** 商户端顶部公告和已读状态接口。 */
@RestController
@RequestMapping("/merchant/merchant-notice")
public class MerchantNoticeController extends BaseController
{
    private final MerchantNoticeService noticeService;

    public MerchantNoticeController(MerchantNoticeService noticeService)
    {
        this.noticeService = noticeService;
    }

    /** 查询当前商户用户可见的最新公告。 */
    @GetMapping("/top")
    public AjaxResult top()
    {
        return success(noticeService.top());
    }

    /** 查询公告详情。 */
    @GetMapping("/{noticeId}")
    public AjaxResult detail(@PathVariable Long noticeId)
    {
        return success(noticeService.detail(noticeId));
    }

    /** 标记单条公告已读。 */
    @PostMapping("/{noticeId}/read")
    public AjaxResult markRead(@PathVariable Long noticeId)
    {
        noticeService.markRead(noticeId);
        return success();
    }

    /** 标记顶部公告全部已读。 */
    @PostMapping("/read-all")
    public AjaxResult markReadAll(@RequestParam("ids") String ids)
    {
        noticeService.markReadAll(ids);
        return success();
    }
}
