package com.ruoyi.system.controller.internal;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.system.api.RemoteAccessoryService;
import com.ruoyi.system.api.model.PlatformAccessoryOption;
import com.ruoyi.system.service.rental.RentalAccessoryService;

/** 商户服务读取平台官方配件的内部入口。 */
@RestController
@RequestMapping(RemoteAccessoryService.INTERNAL_PREFIX)
public class InternalAccessoryController
{
    private final RentalAccessoryService accessoryService;

    public InternalAccessoryController(RentalAccessoryService accessoryService) { this.accessoryService = accessoryService; }

    @InnerAuth
    @GetMapping
    public R<List<PlatformAccessoryOption>> listActive()
    {
        return R.ok(accessoryService.listActiveOptions());
    }
}
