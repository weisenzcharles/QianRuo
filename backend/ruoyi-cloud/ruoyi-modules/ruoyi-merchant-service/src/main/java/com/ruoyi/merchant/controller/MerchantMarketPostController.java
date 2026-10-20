package com.ruoyi.merchant.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.merchant.application.MerchantMarketInventoryService;
import com.ruoyi.merchant.application.MerchantMarketOptionsService;
import com.ruoyi.merchant.application.MerchantMarketPostService;
import com.ruoyi.merchant.model.MerchantMarketPostCommand;
import com.ruoyi.merchant.model.MerchantMarketPostQuery;

/** 商户周转市场帖子管理接口。 */
@RestController
@RequestMapping("/merchant/market/posts")
public class MerchantMarketPostController extends BaseController
{
    private final MerchantMarketPostService service;
    private final MerchantMarketOptionsService optionsService;
    private final MerchantMarketInventoryService inventoryService;

    public MerchantMarketPostController(MerchantMarketPostService service, MerchantMarketOptionsService optionsService,
            MerchantMarketInventoryService inventoryService)
    {
        this.service = service;
        this.optionsService = optionsService;
        this.inventoryService = inventoryService;
    }

    /** 查询平台固定的周转市场发帖选项。 */
    @RequiresPermissions("merchant:market:list")
    @GetMapping("/options")
    public AjaxResult options()
    {
        return success(optionsService.options());
    }

    /** 查询当前商户可绑定的已认证逐件库存。 */
    @RequiresPermissions("merchant:market:list")
    @GetMapping("/inventory-options")
    public AjaxResult inventoryOptions(@org.springframework.web.bind.annotation.RequestParam String brand,
            @org.springframework.web.bind.annotation.RequestParam String modelName,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String memorySpec)
    {
        return success(inventoryService.listEligible(brand, modelName, memorySpec, null));
    }

    /** 分页查询当前商户的市场帖子。 */
    @RequiresPermissions("merchant:market:list")
    @GetMapping
    public TableDataInfo list(MerchantMarketPostQuery query)
    {
        startPage();
        return getDataTable(service.list(query));
    }

    /** 查询当前商户的一条市场帖子。 */
    @RequiresPermissions("merchant:market:list")
    @GetMapping("/{postNo}")
    public AjaxResult get(@PathVariable String postNo)
    {
        var result = service.get(postNo);
        return result == null ? error("市场帖子不存在或无权查看") : success(result);
    }

    /** 创建市场帖子草稿。 */
    @RequiresPermissions("merchant:market:add")
    @PostMapping
    public AjaxResult create(@RequestBody MerchantMarketPostCommand command)
    {
        return success(service.create(command));
    }

    /** 修改草稿或已下架帖子。 */
    @RequiresPermissions("merchant:market:edit")
    @PutMapping("/{postNo}")
    public AjaxResult update(@PathVariable String postNo, @RequestBody MerchantMarketPostCommand command)
    {
        return success(service.update(postNo, command));
    }

    /** 发布市场帖子。 */
    @RequiresPermissions("merchant:market:edit")
    @PostMapping("/{postNo}/publish")
    public AjaxResult publish(@PathVariable String postNo)
    {
        return success(service.publish(postNo));
    }

    /** 下架市场帖子。 */
    @RequiresPermissions("merchant:market:edit")
    @PostMapping("/{postNo}/off-shelf")
    public AjaxResult offShelf(@PathVariable String postNo)
    {
        return success(service.offShelf(postNo));
    }

    /** 关闭市场帖子。 */
    @RequiresPermissions("merchant:market:edit")
    @PostMapping("/{postNo}/close")
    public AjaxResult close(@PathVariable String postNo)
    {
        return success(service.close(postNo));
    }
}
