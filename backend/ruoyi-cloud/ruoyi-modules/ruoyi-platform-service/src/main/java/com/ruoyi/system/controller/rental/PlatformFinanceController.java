package com.ruoyi.system.controller.rental;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.context.ClientContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.system.api.model.PlatformAllianceOrderPageView;
import com.ruoyi.system.api.model.PlatformAllianceOrderQuery;
import com.ruoyi.system.model.rental.PlatformFinanceQuery;
import com.ruoyi.system.model.rental.PlatformFinanceWithdrawalReviewCommand;
import com.ruoyi.system.model.rental.PlatformInvoiceApplicationQuery;
import com.ruoyi.system.model.rental.PlatformInvoiceRedFlushCommand;
import com.ruoyi.system.model.rental.PlatformInvoiceReviewCommand;
import com.ruoyi.system.service.rental.PlatformInvoiceApplicationService;
import com.ruoyi.system.service.rental.PlatformFinanceService;

/** 平台财务中心只读治理接口。 */
@RestController
@RequestMapping("/rental/finance")
public class PlatformFinanceController extends BaseController
{
    private final PlatformFinanceService financeService;
    private final PlatformInvoiceApplicationService invoiceApplicationService;

    public PlatformFinanceController(PlatformFinanceService financeService,
            PlatformInvoiceApplicationService invoiceApplicationService)
    {
        this.financeService = financeService;
        this.invoiceApplicationService = invoiceApplicationService;
    }

    /** 查询平台跨商户财务总览。 */
    @RequiresPermissions("platform:finance:overview")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        assertPlatformClient();
        return success(financeService.summary());
    }

    /** 分页查询商户账户汇总。 */
    @RequiresPermissions("platform:finance:account")
    @GetMapping("/accounts")
    public TableDataInfo accounts(PlatformFinanceQuery query)
    {
        assertPlatformClient();
        startPage();
        return getDataTable(financeService.accounts(query));
    }

    /** 分页查询平台支付订单。 */
    @RequiresPermissions("platform:finance:payment")
    @GetMapping("/payments")
    public TableDataInfo payments(PlatformFinanceQuery query)
    {
        assertPlatformClient();
        startPage();
        return getDataTable(financeService.payments(query));
    }

    /** 查询单笔平台支付订单。 */
    @RequiresPermissions("platform:finance:payment")
    @GetMapping("/payments/{paymentNo}")
    public AjaxResult payment(@PathVariable String paymentNo)
    {
        assertPlatformClient();
        return success(financeService.payment(paymentNo));
    }

    /** 分页查询商户提现申请。 */
    @RequiresPermissions("platform:finance:ledger")
    @GetMapping("/withdrawals")
    public TableDataInfo withdrawals(PlatformFinanceQuery query)
    {
        assertPlatformClient();
        startPage();
        return getDataTable(financeService.withdrawals(query));
    }

    /** 查询单笔提现申请。 */
    @RequiresPermissions("platform:finance:ledger")
    @GetMapping("/withdrawals/{withdrawalNo}")
    public AjaxResult withdrawal(@PathVariable String withdrawalNo)
    {
        assertPlatformClient();
        return success(financeService.withdrawal(withdrawalNo));
    }

    /** 审核提现申请并完成扣款或解冻。 */
    @RequiresPermissions("platform:finance:withdrawal:review")
    @PostMapping("/withdrawals/{withdrawalNo}/review")
    public AjaxResult reviewWithdrawal(@PathVariable String withdrawalNo,
            @RequestBody PlatformFinanceWithdrawalReviewCommand command)
    {
        assertPlatformClient();
        return success(financeService.reviewWithdrawal(withdrawalNo, command));
    }

    /** 分页查询平台资金流水。 */
    @RequiresPermissions("platform:finance:ledger")
    @GetMapping("/ledger")
    public TableDataInfo ledger(PlatformFinanceQuery query)
    {
        assertPlatformClient();
        startPage();
        return getDataTable(financeService.ledger(query));
    }

    /** 查询单笔平台资金流水。 */
    @RequiresPermissions("platform:finance:ledger")
    @GetMapping("/ledger/{ledgerNo}")
    public AjaxResult ledger(@PathVariable String ledgerNo)
    {
        assertPlatformClient();
        return success(financeService.ledger(ledgerNo));
    }

    /** 分页查询开票申请和发票记录。 */
    @RequiresPermissions("platform:finance:invoice")
    @GetMapping("/invoice-applications")
    public TableDataInfo invoiceApplications(PlatformInvoiceApplicationQuery query)
    {
        assertPlatformClient();
        startPage();
        return getDataTable(invoiceApplicationService.list(query));
    }

    /** 查询单笔开票申请详情。 */
    @RequiresPermissions("platform:finance:invoice")
    @GetMapping("/invoice-applications/{applicationNo}")
    public AjaxResult invoiceApplication(@PathVariable String applicationNo)
    {
        assertPlatformClient();
        return success(invoiceApplicationService.get(applicationNo));
    }

    /** 审核开票申请。 */
    @RequiresPermissions("platform:finance:invoice")
    @PostMapping("/invoice-applications/{applicationNo}/review")
    public AjaxResult reviewInvoiceApplication(@PathVariable String applicationNo,
            @RequestBody PlatformInvoiceReviewCommand command)
    {
        assertPlatformClient();
        return success(invoiceApplicationService.review(applicationNo, command));
    }

    /** 为待开具申请补录发票信息。 */
    @RequiresPermissions("platform:finance:invoice")
    @PostMapping("/invoice-applications/{applicationNo}/issue")
    public AjaxResult issueInvoiceApplication(@PathVariable String applicationNo,
            @RequestBody PlatformInvoiceReviewCommand command)
    {
        assertPlatformClient();
        return success(invoiceApplicationService.issue(applicationNo, command));
    }

    /** 为已开具发票创建红冲申请。 */
    @RequiresPermissions("platform:finance:invoice")
    @PostMapping("/invoice-applications/{applicationNo}/red-flush")
    public AjaxResult createRedFlush(@PathVariable String applicationNo,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody PlatformInvoiceRedFlushCommand command)
    {
        assertPlatformClient();
        if (command != null && (command.getIdempotencyKey() == null || command.getIdempotencyKey().isBlank()))
        {
            command.setIdempotencyKey(idempotencyKey);
        }
        return success(invoiceApplicationService.createRedFlush(applicationNo, command));
    }

    /** 分页查询联盟订单结算明细。 */
    @RequiresPermissions("platform:finance:settlement")
    @GetMapping("/settlements")
    public TableDataInfo settlements(@RequestParam(required = false) Integer status,
            @RequestParam(required = false) String beginTime, @RequestParam(required = false) String endTime,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize)
    {
        assertPlatformClient();
        startPage();
        PlatformAllianceOrderQuery query = new PlatformAllianceOrderQuery();
        query.setStatus(status);
        query.setBeginTime(beginTime);
        query.setEndTime(endTime);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        PlatformAllianceOrderPageView page = financeService.settlements(query);
        return new TableDataInfo(page.getRows(), page.getTotal());
    }

    private void assertPlatformClient()
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商户端不允许访问平台财务中心");
        }
    }
}
