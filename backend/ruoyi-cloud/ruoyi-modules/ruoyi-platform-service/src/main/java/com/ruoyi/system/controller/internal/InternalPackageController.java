package com.ruoyi.system.controller.internal;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.system.api.RemotePackageService;
import com.ruoyi.system.api.model.PlatformPackage;
import com.ruoyi.system.service.rental.RentalPackageService;

/** 商户服务读取平台已发布套餐的内部入口。 */
@RestController
@RequestMapping(RemotePackageService.INTERNAL_PREFIX)
public class InternalPackageController
{
    private final RentalPackageService packageService;
    public InternalPackageController(RentalPackageService packageService) { this.packageService = packageService; }

    @InnerAuth
    @GetMapping
    public R<List<PlatformPackage>> listActive()
    {
        return R.ok(packageService.listActiveForInternal());
    }
}
