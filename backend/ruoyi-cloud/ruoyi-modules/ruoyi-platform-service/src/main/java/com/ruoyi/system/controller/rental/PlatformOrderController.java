package com.ruoyi.system.controller.rental;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import com.ruoyi.common.core.context.ClientContextHolder;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.system.api.model.PlatformOrderPageView;
import com.ruoyi.system.api.model.PlatformAllianceOrderPageView;
import com.ruoyi.system.api.model.PlatformAllianceOrderQuery;
import com.ruoyi.system.api.model.PlatformAllianceOrderDetailView;
import com.ruoyi.system.api.model.PlatformAllianceDisputePageView;
import com.ruoyi.system.api.model.PlatformAllianceDisputeView;
import com.ruoyi.system.api.model.PlatformAllianceMediationCommand;
import com.ruoyi.system.api.model.PlatformAllianceScopeView;
import com.ruoyi.system.api.model.PlatformAllianceSettlementSummaryView;
import com.ruoyi.system.model.rental.PlatformOrderCustomerVO;
import com.ruoyi.system.service.rental.PlatformOrderService;

/** 平台订单中心，只提供跨商户只读查询。 */
@RestController
@RequestMapping("/rental/order")
public class PlatformOrderController
{
    private final PlatformOrderService orderService;

    public PlatformOrderController(PlatformOrderService orderService)
    {
        this.orderService = orderService;
    }

    /** 按平台筛选条件分页查询全部商户订单。 */
    @RequiresPermissions("platform:order:list")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(value = "shopName", required = false) String shopName,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "orderStatus", required = false) Integer orderStatus,
            @RequestParam(value = "beginTime", required = false) String beginTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize)
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商户端不允许访问平台订单中心");
        }
        return orderService.list(shopName, keyword, orderStatus, beginTime, endTime, pageNum, pageSize);
    }

    /** 查询平台订单详情、批注和字段变更记录。 */
    @RequiresPermissions("platform:order:list")
    @GetMapping("/{orderId}")
    public R<?> detail(@PathVariable Long orderId)
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商户端不允许访问平台订单中心");
        }
        return R.ok(orderService.detail(orderId));
    }

    /** 查看平台订单完整客户信息，并将敏感信息访问写入平台操作日志。 */
    @Log(title = "平台订单客户信息查看", businessType = BusinessType.OTHER)
    @RequiresPermissions("platform:order:customer:view")
    @PostMapping("/{orderId}/customer")
    public R<PlatformOrderCustomerVO> customer(@PathVariable Long orderId)
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商户端不允许查看平台订单客户信息");
        }
        return R.ok(orderService.customerDetail(orderId));
    }

    /** 按状态分页查询全部联盟协助订单。 */
    @RequiresPermissions("platform:allianceOrder:list")
    @GetMapping("/alliance")
    public TableDataInfo alliance(@RequestParam(value = "allianceId", required = false) Long allianceId,
            @RequestParam(value = "sourceMemberId", required = false) Long sourceMemberId,
            @RequestParam(value = "targetMemberId", required = false) Long targetMemberId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "viewMode", required = false) Integer viewMode,
            @RequestParam(value = "beginTime", required = false) String beginTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize)
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商户端不允许访问平台联盟订单");
        }
        PlatformAllianceOrderQuery query = new PlatformAllianceOrderQuery();
        query.setAllianceId(allianceId); query.setSourceMemberId(sourceMemberId); query.setTargetMemberId(targetMemberId);
        query.setStatus(status); query.setViewMode(viewMode); query.setBeginTime(beginTime); query.setEndTime(endTime);
        query.setPageNum(pageNum); query.setPageSize(pageSize);
        PlatformAllianceOrderPageView page = orderService.listAlliance(query);
        return new TableDataInfo(page.getRows(), page.getTotal());
    }

    /** 查询平台联盟订单详情、事件和结算记录。 */
    @RequiresPermissions("platform:allianceOrder:list")
    @GetMapping("/alliance/{allianceOrderId}")
    public R<PlatformAllianceOrderDetailView> allianceDetail(
            @PathVariable Long allianceOrderId)
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商户端不允许访问平台联盟订单");
        }
        return R.ok(orderService.detailAlliance(allianceOrderId));
    }

    /** 查询联盟结算汇总。 */
    @RequiresPermissions("platform:allianceOrder:list")
    @GetMapping("/alliance/settlements/summary")
    public R<PlatformAllianceSettlementSummaryView> allianceSettlementSummary()
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商户端不允许访问联盟结算汇总");
        }
        return R.ok(orderService.allianceSettlementSummary());
    }

    /** 按状态分页查询平台联盟纠纷。 */
    @RequiresPermissions("platform:allianceOrder:list")
    @GetMapping("/alliance/disputes")
    public TableDataInfo allianceDisputes(@RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize)
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商户端不允许访问平台联盟纠纷");
        }
        PlatformAllianceDisputePageView page = orderService.listAllianceDisputes(status, pageNum, pageSize);
        return new TableDataInfo(page.getRows(), page.getTotal());
    }

    /** 查询平台联盟纠纷详情。 */
    @RequiresPermissions("platform:allianceOrder:list")
    @GetMapping("/alliance/disputes/{disputeId}")
    public R<PlatformAllianceDisputeView> allianceDisputeDetail(@PathVariable Long disputeId)
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商户端不允许访问平台联盟纠纷");
        }
        return R.ok(orderService.detailAllianceDispute(disputeId));
    }

    /** 提交联盟纠纷调解方案。 */
    @RequiresPermissions("platform:allianceOrder:edit")
    @PostMapping("/alliance/disputes/mediation")
    public R<Void> proposeAllianceMediation(@RequestBody PlatformAllianceMediationCommand command)
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商户端不允许处理平台联盟纠纷");
        }
        orderService.proposeAllianceMediation(command);
        return R.ok();
    }

    /** 查询平台联盟管理列表。 */
    @RequiresPermissions("platform:alliance:list")
    @GetMapping("/alliance-management")
    public TableDataInfo allianceManagement(@RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize)
    {
        if (!ClientContextHolder.isPlatformClient()) throw new ServiceException("商户端不允许访问联盟管理");
        return orderService.listMerchantAlliances(keyword, status, pageNum, pageSize);
    }

    /** 查询平台联盟成员。 */
    @RequiresPermissions("platform:alliance:list")
    @GetMapping("/alliance-management/{allianceId}/members")
    public R<?> allianceManagementMembers(@PathVariable Long allianceId)
    {
        if (!ClientContextHolder.isPlatformClient()) throw new ServiceException("商户端不允许访问联盟管理");
        return R.ok(orderService.listMerchantAllianceMembers(allianceId));
    }

    /** 查询指定联盟成员共享范围。 */
    @RequiresPermissions("platform:alliance:list")
    @GetMapping("/alliance-management/{allianceId}/members/{memberId}/scopes")
    public R<List<PlatformAllianceScopeView>> allianceManagementScopes(@PathVariable Long allianceId,
            @PathVariable Long memberId)
    {
        if (!ClientContextHolder.isPlatformClient()) throw new ServiceException("商户端不允许访问联盟管理");
        return R.ok(orderService.listMerchantAllianceScopes(allianceId, memberId));
    }

    /** 更新联盟或成员状态。 */
    @RequiresPermissions("platform:alliance:edit")
    @PostMapping("/alliance-management/status")
    public R<Void> updateAllianceStatus(@RequestParam Long allianceId,
            @RequestParam(required = false) Long memberId, @RequestParam Integer status)
    {
        if (!ClientContextHolder.isPlatformClient()) throw new ServiceException("商户端不允许更新联盟状态");
        orderService.updateMerchantAllianceStatus(allianceId, memberId, status);
        return R.ok();
    }
}
