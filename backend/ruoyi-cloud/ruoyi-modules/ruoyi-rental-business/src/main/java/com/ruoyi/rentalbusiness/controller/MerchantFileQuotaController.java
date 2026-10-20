package com.ruoyi.rentalbusiness.controller;

import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.rentalbusiness.service.FileQuotaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 商户文件配额和文件操作审计接口。 */
@RestController
@RequestMapping("/merchant/file-quota")
public class MerchantFileQuotaController
{
    private final FileQuotaService service;

    /** 注入文件配额服务。 */
    public MerchantFileQuotaController(FileQuotaService service)
    {
        this.service = service;
    }

    /** 查询当前商户文件配额账户。 */
    @RequiresPermissions("merchant:fileQuota:list")
    @GetMapping
    public AjaxResult quota()
    {
        return AjaxResult.success(service.getQuota());
    }

    /** 查询当前商户文件配额审计流水。 */
    @RequiresPermissions("merchant:fileQuota:list")
    @GetMapping("/ledger")
    public AjaxResult ledger()
    {
        return AjaxResult.success(service.listLedger());
    }
}
