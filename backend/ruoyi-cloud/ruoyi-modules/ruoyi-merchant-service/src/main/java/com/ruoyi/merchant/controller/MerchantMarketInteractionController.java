package com.ruoyi.merchant.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.merchant.application.MerchantMarketInteractionService;
import com.ruoyi.merchant.model.MerchantMarketBoardType;
import com.ruoyi.merchant.model.MerchantMarketMessageCommand;
import com.ruoyi.merchant.model.MerchantMarketReadStatus;
import com.ruoyi.merchant.model.MerchantMarketReportCommand;

/** 商户周转市场浏览、私信和举报接口。 */
@RestController
@RequestMapping("/merchant/market")
public class MerchantMarketInteractionController extends BaseController
{
    private final MerchantMarketInteractionService service;

    public MerchantMarketInteractionController(MerchantMarketInteractionService service)
    {
        this.service = service;
    }

    /** 分页查询其他商户公开发布的市场帖子。 */
    @RequiresPermissions("merchant:market:list")
    @GetMapping("/public/posts")
    public TableDataInfo publicPosts(@RequestParam(required = false) MerchantMarketBoardType boardType,
            @RequestParam(required = false) String keyword, @RequestParam(required = false) String city)
    {
        startPage();
        return getDataTable(service.publicPosts(boardType, keyword, city));
    }

    /** 分页查询当前商户参与的站内私信。 */
    @RequiresPermissions("merchant:market:list")
    @GetMapping("/messages")
    public TableDataInfo messages(@RequestParam(required = false) MerchantMarketReadStatus readStatus,
            @RequestParam(required = false) String keyword)
    {
        startPage();
        return getDataTable(service.messages(readStatus, keyword));
    }

    /** 发送周转市场私信。 */
    @RequiresPermissions("merchant:market:message")
    @PostMapping("/messages")
    public AjaxResult sendMessage(@RequestBody MerchantMarketMessageCommand command)
    {
        service.sendMessage(command);
        return success();
    }

    /** 将收到的私信标记为已读。 */
    @RequiresPermissions("merchant:market:list")
    @PutMapping("/messages/{messageId}/read")
    public AjaxResult markMessageRead(@PathVariable Long messageId)
    {
        service.markMessageRead(messageId);
        return success();
    }

    /** 查询当前商户提交的市场举报。 */
    @RequiresPermissions("merchant:market:list")
    @GetMapping("/reports")
    public TableDataInfo reports()
    {
        startPage();
        return getDataTable(service.reports());
    }

    /** 提交周转市场举报。 */
    @RequiresPermissions("merchant:market:report")
    @PostMapping("/reports")
    public AjaxResult submitReport(@RequestBody MerchantMarketReportCommand command)
    {
        service.submitReport(command);
        return success();
    }
}
