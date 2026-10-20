package com.ruoyi.rentalbusiness.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.rentalbusiness.service.BatchDataService;

/** 商户商品、SN、配件、套餐和仓库批量导入导出接口。 */
@RestController
@RequestMapping("/merchant/batch")
public class MerchantBatchDataController
{
    private final BatchDataService service;

    /** 注入批量数据服务。 */
    public MerchantBatchDataController(BatchDataService service)
    {
        this.service = service;
    }

    /** 导入指定资源的 Excel 文件。 */
    @RequiresPermissions("merchant:batch:import")
    @Log(title = "商户主数据批量导入", businessType = BusinessType.IMPORT)
    @PostMapping("/{resource}/import")
    public AjaxResult importData(@PathVariable String resource, @RequestParam("file") MultipartFile file)
    {
        return AjaxResult.success(service.importData(resource, file));
    }

    /** 导出当前商户指定资源的 Excel 文件。 */
    @RequiresPermissions("merchant:batch:export")
    @Log(title = "商户主数据批量导出", businessType = BusinessType.EXPORT)
    @GetMapping("/{resource}/export")
    public void exportData(@PathVariable String resource, HttpServletResponse response)
    {
        service.exportData(resource, response);
    }
}
