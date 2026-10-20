package com.ruoyi.merchant.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.merchant.application.MerchantFinanceService;
import com.ruoyi.merchant.application.MerchantFinanceWithdrawalService;
import com.ruoyi.merchant.application.MerchantInvoiceApplicationService;
import com.ruoyi.merchant.model.MerchantInvoiceApplicationCreateCommand;
import com.ruoyi.merchant.model.MerchantFinanceLedgerQuery;
import com.ruoyi.merchant.model.MerchantWithdrawalApplyCommand;

/** 商户财务中心页面接口。金额、流水和状态均由当前商户范围过滤。 */
@RestController
@RequestMapping("/merchant/finance")
public class MerchantFinanceController extends BaseController
{
    private final MerchantFinanceService service;
    private final MerchantFinanceWithdrawalService withdrawalService;
    private final MerchantInvoiceApplicationService invoiceApplicationService;

    public MerchantFinanceController(MerchantFinanceService service, MerchantFinanceWithdrawalService withdrawalService,
            MerchantInvoiceApplicationService invoiceApplicationService)
    {
        this.service = service;
        this.withdrawalService = withdrawalService;
        this.invoiceApplicationService = invoiceApplicationService;
    }

    /** 财务总览。 */
    @RequiresPermissions("merchant:finance:overview")
    @GetMapping("/overview")
    public AjaxResult overview()
    {
        return success(service.overview());
    }

    /** 分户余额。 */
    @RequiresPermissions("merchant:finance:account")
    @GetMapping("/accounts")
    public AjaxResult accounts()
    {
        return success(service.accounts());
    }

    /** 全部资金流水。 */
    @RequiresPermissions("merchant:finance:ledger")
    @GetMapping("/ledger")
    public TableDataInfo ledger(MerchantFinanceLedgerQuery query)
    {
        startPage();
        return getDataTable(service.ledger(query, null));
    }

    /** 查询流水详情。 */
    @RequiresPermissions("merchant:finance:ledger")
    @GetMapping("/ledger/{ledgerNo}")
    public AjaxResult ledger(@PathVariable String ledgerNo)
    {
        com.ruoyi.merchant.model.MerchantFinanceLedgerVO result = service.ledger(ledgerNo);
        return result == null ? error("流水不存在或无权查看") : success(result);
    }

    /** 租金与收入流水。 */
    @RequiresPermissions("merchant:finance:revenue")
    @GetMapping("/revenue")
    public TableDataInfo revenue(MerchantFinanceLedgerQuery query)
    {
        startPage();
        return getDataTable(service.ledger(query, "revenue"));
    }

    /** 快递费用流水。 */
    @RequiresPermissions("merchant:finance:shipping")
    @GetMapping("/shipping")
    public TableDataInfo shipping(MerchantFinanceLedgerQuery query)
    {
        startPage();
        return getDataTable(service.ledger(query, "shipping"));
    }

    /** 联盟结算页面的基础流水入口，结算事实由租赁业务服务提供。 */
    @RequiresPermissions("merchant:finance:settlement")
    @GetMapping("/settlements")
    public AjaxResult settlements(@RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "beginTime", required = false) String beginTime,
            @RequestParam(value = "endTime", required = false) String endTime)
    {
        return success(service.settlements(status, beginTime, endTime));
    }

    /** 查询当前商户提现申请。 */
    @RequiresPermissions("merchant:finance:withdrawal")
    @GetMapping("/withdrawals")
    public TableDataInfo withdrawals(@RequestParam(value = "status", required = false) Integer status)
    {
        startPage();
        return getDataTable(withdrawalService.list(status));
    }

    /** 查询当前商户单笔提现申请。 */
    @RequiresPermissions("merchant:finance:withdrawal")
    @GetMapping("/withdrawals/{withdrawalNo}")
    public AjaxResult withdrawal(@PathVariable String withdrawalNo)
    {
        com.ruoyi.merchant.model.MerchantWithdrawalVO result = withdrawalService.get(withdrawalNo);
        return result == null ? error("提现申请不存在或无权查看") : success(result);
    }

    /** 提交提现申请，幂等键优先使用请求头。 */
    @RequiresPermissions("merchant:finance:withdrawal:apply")
    @PostMapping("/withdrawals")
    public AjaxResult applyWithdrawal(@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody MerchantWithdrawalApplyCommand command)
    {
        if (command != null && (command.getIdempotencyKey() == null || command.getIdempotencyKey().isBlank()))
            command.setIdempotencyKey(idempotencyKey);
        return success(withdrawalService.apply(command));
    }

    /** 发票相关流水。 */
    @RequiresPermissions("merchant:finance:invoice")
    @GetMapping("/invoices")
    public TableDataInfo invoices(MerchantFinanceLedgerQuery query)
    {
        startPage();
        return getDataTable(service.ledger(query, "invoice"));
    }

    /** 查询当前商户的开票申请和已开具发票。 */
    @RequiresPermissions("merchant:finance:invoice")
    @GetMapping("/invoice-applications")
    public TableDataInfo invoiceApplications(@RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "applicationType", required = false) Integer applicationType)
    {
        startPage();
        return getDataTable(invoiceApplicationService.list(status, applicationType));
    }

    /** 查询当前商户单笔开票申请。 */
    @RequiresPermissions("merchant:finance:invoice")
    @GetMapping("/invoice-applications/{applicationNo}")
    public AjaxResult invoiceApplication(@PathVariable String applicationNo)
    {
        com.ruoyi.merchant.model.MerchantInvoiceApplicationVO result = invoiceApplicationService.get(applicationNo);
        return result == null ? error("开票申请不存在或无权查看") : success(result);
    }

    /** 提交开票或补票申请，支持请求头幂等键。 */
    @RequiresPermissions("merchant:finance:invoice")
    @PostMapping("/invoice-applications")
    public AjaxResult createInvoiceApplication(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody MerchantInvoiceApplicationCreateCommand command)
    {
        if (command != null && (command.getIdempotencyKey() == null || command.getIdempotencyKey().isBlank()))
            command.setIdempotencyKey(idempotencyKey);
        return success(invoiceApplicationService.create(command));
    }

    /** 记账簿流水。 */
    @RequiresPermissions("merchant:finance:book")
    @GetMapping("/book")
    public TableDataInfo book(MerchantFinanceLedgerQuery query)
    {
        startPage();
        return getDataTable(service.ledger(query, "book"));
    }
}
