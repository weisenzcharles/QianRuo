package com.ruoyi.system.controller.rental;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.context.ClientContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.system.api.model.PlatformAfterSalesDetailView;
import com.ruoyi.system.api.model.PlatformAfterSalesEventCommand;
import com.ruoyi.system.api.model.PlatformAfterSalesEvidenceCommand;
import com.ruoyi.system.service.rental.PlatformAfterSalesService;

/** 平台售后案件查询和处理接口。 */
@RestController
@RequestMapping("/rental/after-sales")
public class PlatformAfterSalesController extends BaseController
{
    private final PlatformAfterSalesService service;

    public PlatformAfterSalesController(PlatformAfterSalesService service)
    {
        this.service = service;
    }

    /** 分页查询全部商户售后案件。 */
    @RequiresPermissions("platform:afterSales:list")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(value = "caseStatus", required = false) Integer caseStatus,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "merchantKeyword", required = false) String merchantKeyword,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize)
    {
        assertPlatformClient();
        return service.list(caseStatus, keyword, merchantKeyword, pageNum, pageSize);
    }

    /** 查询售后案件详情、处理时间线和证据。 */
    @RequiresPermissions("platform:afterSales:list")
    @GetMapping("/{caseId}")
    public AjaxResult detail(@PathVariable Long caseId)
    {
        assertPlatformClient();
        PlatformAfterSalesDetailView detail = service.detail(caseId);
        return success(detail);
    }

    /** 追加平台售后处理记录并变更案件状态。 */
    @RequiresPermissions("platform:afterSales:process")
    @Log(title = "平台售后处理", businessType = BusinessType.UPDATE)
    @PostMapping("/{caseId}/events")
    public AjaxResult appendEvent(@PathVariable Long caseId, @RequestBody PlatformAfterSalesEventCommand command)
    {
        assertPlatformClient();
        service.appendEvent(caseId, command);
        return success();
    }

    /** 追加平台售后证据。 */
    @RequiresPermissions("platform:afterSales:process")
    @Log(title = "平台售后证据", businessType = BusinessType.INSERT)
    @PostMapping("/{caseId}/evidence")
    public AjaxResult addEvidence(@PathVariable Long caseId, @RequestBody PlatformAfterSalesEvidenceCommand command)
    {
        assertPlatformClient();
        service.addEvidence(caseId, command);
        return success();
    }

    private void assertPlatformClient()
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商户端不允许访问平台售后治理接口");
        }
    }
}
