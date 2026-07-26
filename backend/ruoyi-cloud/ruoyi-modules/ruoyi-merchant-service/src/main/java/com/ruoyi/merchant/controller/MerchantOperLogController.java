package com.ruoyi.merchant.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.merchant.application.MerchantOperLogService;
import com.ruoyi.merchant.model.MerchantOperLogQuery;
import com.ruoyi.merchant.model.MerchantOperLogVO;

/** 当前商户操作日志。 */
@RestController
@RequestMapping("/merchant/operlog")
public class MerchantOperLogController extends BaseController
{
    private final MerchantOperLogService operLogService;

    public MerchantOperLogController(MerchantOperLogService operLogService)
    {
        this.operLogService = operLogService;
    }

    @RequiresPermissions("merchant:operlog:list")
    @GetMapping("/list")
    public TableDataInfo list(MerchantOperLogQuery query)
    {
        startPage();
        return getDataTable(operLogService.list(query));
    }

    @Log(title = "商户操作日志", businessType = BusinessType.EXPORT)
    @RequiresPermissions("merchant:operlog:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MerchantOperLogQuery query)
    {
        List<MerchantOperLogVO> list = operLogService.list(query);
        new ExcelUtil<MerchantOperLogVO>(MerchantOperLogVO.class)
                .exportExcel(response, list, "商户操作日志");
    }
}
