package com.ruoyi.merchant.controller;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.utils.poi.ExcelUtil;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.merchant.application.MerchantBusinessAuditService;
import com.ruoyi.merchant.application.MerchantAuditFieldLabelService;
import com.ruoyi.merchant.model.MerchantBusinessAuditQuery;
import com.ruoyi.merchant.model.MerchantBusinessAuditOptionsVO;
import com.ruoyi.merchant.model.MerchantBusinessAuditVO;

/** 当前商户业务审计日志。 */
@RestController
@RequestMapping("/merchant/operlog")
public class MerchantOperLogController extends BaseController
{
    private final MerchantBusinessAuditService auditService;
    private final MerchantAuditFieldLabelService fieldLabelService;

    public MerchantOperLogController(MerchantBusinessAuditService auditService,
            MerchantAuditFieldLabelService fieldLabelService)
    {
        this.auditService = auditService;
        this.fieldLabelService = fieldLabelService;
    }

    @RequiresPermissions("merchant:operlog:list")
    @GetMapping("/list")
    public TableDataInfo list(MerchantBusinessAuditQuery query)
    {
        startPage();
        return getDataTable(auditService.list(query));
    }

    /** 查询当前商户日志列表使用的模块和操作人下拉选项。 */
    @RequiresPermissions("merchant:operlog:list")
    @GetMapping("/options")
    public AjaxResult options()
    {
        MerchantBusinessAuditOptionsVO options = auditService.options();
        return success(options);
    }

    /** 查询商户操作日志变更字段的中文名称，名称由平台字典统一维护并使用共享缓存。 */
    @RequiresPermissions("merchant:operlog:list")
    @GetMapping("/field-labels")
    public AjaxResult fieldLabels()
    {
        Map<String, String> labels = fieldLabelService.getFieldLabels();
        return success(labels);
    }

    /** 查询单条商户业务审计详情，数据范围仍由当前登录商户上下文限制。 */
    @RequiresPermissions("merchant:operlog:list")
    @GetMapping("/{auditId}")
    public AjaxResult get(@PathVariable Long auditId)
    {
        MerchantBusinessAuditVO detail = auditService.get(auditId);
        return detail == null ? error("日志不存在或无权查看") : success(detail);
    }

    @RequiresPermissions("merchant:operlog:export")
    @Log(title = "商户业务日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MerchantBusinessAuditQuery query)
    {
        List<MerchantBusinessAuditVO> list = auditService.list(query);
        new ExcelUtil<MerchantBusinessAuditVO>(MerchantBusinessAuditVO.class)
                .exportExcel(response, list, "商户操作日志");
    }
}
