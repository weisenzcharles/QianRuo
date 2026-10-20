package com.ruoyi.rentalbusiness.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.rentalbusiness.model.MerchantAfterSalesCaseVO;
import com.ruoyi.rentalbusiness.model.MerchantAfterSalesEvidenceCommand;
import com.ruoyi.rentalbusiness.model.MerchantAfterSalesEventCommand;
import com.ruoyi.rentalbusiness.model.MerchantAfterSalesQuery;
import com.ruoyi.rentalbusiness.model.MerchantOrderActionCommand;
import com.ruoyi.rentalbusiness.service.MerchantOrderService;
import com.ruoyi.rentalbusiness.service.MerchantAfterSalesEscalationService;
import com.ruoyi.system.api.model.PlatformAfterSalesTicketView;

/** 商户售后单管理接口。 */
@RestController
@RequestMapping("/merchant/after-sales")
public class MerchantAfterSalesController extends BaseController
{
    private final MerchantOrderService orderService;
    private final MerchantAfterSalesEscalationService escalationService;

    public MerchantAfterSalesController(MerchantOrderService orderService,
            MerchantAfterSalesEscalationService escalationService)
    {
        this.orderService = orderService;
        this.escalationService = escalationService;
    }

    /** 分页查询当前商户售后单。 */
    @RequiresPermissions("merchant:inspection:list")
    @GetMapping("/list")
    public TableDataInfo list(MerchantAfterSalesQuery query)
    {
        startPage();
        return getDataTable(orderService.listAfterSales(query));
    }

    /** 查询当前商户售后单详情。 */
    @RequiresPermissions("merchant:inspection:list")
    @GetMapping("/{caseNo}")
    public AjaxResult getInfo(@PathVariable String caseNo)
    {
        MerchantAfterSalesCaseVO result = orderService.getAfterSales(caseNo);
        return success(result);
    }

    /** 查询售后处理时间线。 */
    @RequiresPermissions("merchant:inspection:list")
    @GetMapping("/{caseNo}/events")
    public AjaxResult events(@PathVariable String caseNo)
    {
        return success(orderService.listAfterSalesEvents(caseNo));
    }

    /** 查询售后证据记录。 */
    @RequiresPermissions("merchant:inspection:list")
    @GetMapping("/{caseNo}/evidence")
    public AjaxResult evidence(@PathVariable String caseNo)
    {
        return success(orderService.listAfterSalesEvidence(caseNo));
    }

    /** 申请平台介入并返回关联的客服工单。 */
    @Log(title = "售后申请平台介入", businessType = BusinessType.UPDATE)
    @RequiresPermissions("merchant:inspection:list")
    @PostMapping("/{caseNo}/platform-ticket")
    public AjaxResult platformTicket(@PathVariable String caseNo)
    {
        PlatformAfterSalesTicketView result = escalationService.escalate(caseNo);
        return success(result);
    }

    /** 追加售后处理记录。 */
    @Log(title = "售后处理记录", businessType = BusinessType.INSERT)
    @RequiresPermissions("merchant:inspection:list")
    @PostMapping("/{caseNo}/events")
    public AjaxResult addEvent(@PathVariable String caseNo,
            @RequestBody MerchantAfterSalesEventCommand command)
    {
        orderService.addAfterSalesEvent(caseNo, command);
        return success();
    }

    /** 追加售后证据文件记录。 */
    @Log(title = "售后证据", businessType = BusinessType.INSERT)
    @RequiresPermissions("merchant:inspection:list")
    @PostMapping("/{caseNo}/evidence")
    public AjaxResult addEvidence(@PathVariable String caseNo,
            @RequestBody MerchantAfterSalesEvidenceCommand command)
    {
        orderService.addAfterSalesEvidence(caseNo, command);
        return success();
    }

    /** 完成售后单，并在没有其他处理中售后单时同步完成订单。 */
    @Log(title = "售后单完成", businessType = BusinessType.UPDATE)
    @RequiresPermissions("merchant:inspection:list")
    @PutMapping("/{caseNo}/complete")
    public AjaxResult complete(@PathVariable String caseNo,
            @RequestBody(required = false) MerchantOrderActionCommand command)
    {
        orderService.completeAfterSales(caseNo, command);
        return success();
    }
}
