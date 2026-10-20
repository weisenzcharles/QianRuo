package com.ruoyi.system.controller.rental;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.context.ClientContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.system.service.rental.PlatformWorkbenchService;

/** 平台通用工作台接口。 */
@RestController
@RequestMapping("/rental/workbench")
public class PlatformWorkbenchController extends BaseController
{
    private final PlatformWorkbenchService service;

    public PlatformWorkbenchController(PlatformWorkbenchService service)
    {
        this.service = service;
    }

    /** 查询平台待办汇总和待办列表。 */
    @RequiresPermissions("platform:workbench:view")
    @GetMapping("/overview")
    public AjaxResult overview()
    {
        if (!ClientContextHolder.isPlatformClient())
            throw new ServiceException("商户端不允许访问平台工作台");
        return success(service.overview());
    }
}
