package com.ruoyi.system.controller.rental;

import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.context.ClientContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.domain.SysMerchantNotice;
import com.ruoyi.system.model.rental.MerchantNoticeListVO;
import com.ruoyi.system.service.rental.MerchantNoticeService;

/** 平台商户公告管理入口。 */
@RestController
@RequestMapping("/rental/merchantNotice")
public class MerchantNoticeController extends BaseController
{
    private final MerchantNoticeService noticeService;

    public MerchantNoticeController(MerchantNoticeService noticeService)
    {
        this.noticeService = noticeService;
    }

    @RequiresPermissions("platform:merchantNotice:list")
    @GetMapping("/list")
    public TableDataInfo list(SysMerchantNotice query)
    {
        assertPlatformClient();
        startPage();
        List<MerchantNoticeListVO> notices = noticeService.list(query);
        return getDataTable(notices);
    }

    @RequiresPermissions("platform:merchantNotice:list")
    @GetMapping("/options")
    public AjaxResult options()
    {
        assertPlatformClient();
        return success(noticeService.memberOptions());
    }

    @RequiresPermissions("platform:merchantNotice:query")
    @GetMapping("/{merchantNoticeId}")
    public AjaxResult get(@PathVariable Long merchantNoticeId)
    {
        assertPlatformClient();
        return success(noticeService.get(merchantNoticeId));
    }

    @RequiresPermissions("platform:merchantNotice:add")
    @Log(title = "商户公告", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysMerchantNotice command)
    {
        assertPlatformClient();
        return toAjax(noticeService.add(command, SecurityUtils.getUserId()));
    }

    @RequiresPermissions("platform:merchantNotice:edit")
    @Log(title = "商户公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult update(@Validated @RequestBody SysMerchantNotice command)
    {
        assertPlatformClient();
        return toAjax(noticeService.update(command, SecurityUtils.getUserId()));
    }

    @RequiresPermissions("platform:merchantNotice:remove")
    @Log(title = "商户公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{merchantNoticeIds}")
    public AjaxResult delete(@PathVariable Long[] merchantNoticeIds)
    {
        assertPlatformClient();
        return toAjax(noticeService.delete(merchantNoticeIds, SecurityUtils.getUserId()));
    }

    private void assertPlatformClient()
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商户端不允许访问平台商户公告管理");
        }
    }
}
