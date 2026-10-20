package com.ruoyi.system.controller.rental;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.context.ClientContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.system.model.rental.PlatformOrderChannelCommand;
import com.ruoyi.system.service.rental.RentalOrderChannelService;

/** 平台联盟订单渠道管理入口。 */
@RestController
@RequestMapping("/rental/order-channel")
public class RentalOrderChannelController extends BaseController
{
    private final RentalOrderChannelService orderChannelService;

    /** 注入平台订单渠道应用服务。 */
    public RentalOrderChannelController(RentalOrderChannelService orderChannelService)
    {
        this.orderChannelService = orderChannelService;
    }

    /** 查询平台联盟订单渠道。 */
    @RequiresPermissions("platform:orderChannel:list")
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status)
    {
        assertPlatformClient();
        return success(orderChannelService.list(keyword, status));
    }

    /** 新增平台联盟订单渠道。 */
    @RequiresPermissions("platform:orderChannel:add")
    @Log(title = "平台联盟订单渠道", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody PlatformOrderChannelCommand command)
    {
        assertPlatformClient();
        return success(orderChannelService.add(command));
    }

    /** 修改平台联盟订单渠道。 */
    @RequiresPermissions("platform:orderChannel:edit")
    @Log(title = "平台联盟订单渠道", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult update(@Validated @RequestBody PlatformOrderChannelCommand command)
    {
        assertPlatformClient();
        return success(orderChannelService.update(command));
    }

    /** 软删除平台联盟订单渠道。 */
    @RequiresPermissions("platform:orderChannel:remove")
    @Log(title = "平台联盟订单渠道", businessType = BusinessType.DELETE)
    @DeleteMapping("/{channelId}")
    public AjaxResult delete(@PathVariable("channelId") Long channelId)
    {
        assertPlatformClient();
        return success(orderChannelService.delete(channelId));
    }

    /** 校验请求来自平台终端，防止商户终端直接调用平台主数据写接口。 */
    private void assertPlatformClient()
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商户端不允许访问平台订单渠道接口");
        }
    }
}
