package com.ruoyi.system.controller.rental;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.context.ClientContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.system.api.model.PlatformMerchantBusinessView;
import com.ruoyi.system.api.model.PlatformMerchantGovernanceView;
import com.ruoyi.system.service.rental.PlatformMerchantOverviewService;

/** 平台商户经营概况，只提供跨商户只读管理视图。 */
@RestController
@RequestMapping("/rental/merchant-overview")
public class PlatformMerchantOverviewController
{
    private final PlatformMerchantOverviewService overviewService;

    public PlatformMerchantOverviewController(PlatformMerchantOverviewService overviewService)
    {
        this.overviewService = overviewService;
    }

    /** 分页查询商户经营概况。 */
    @RequiresPermissions("platform:merchantOverview:list")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(value = "memberCode", required = false) String memberCode,
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "legalSubjectName", required = false) String legalSubjectName,
            @RequestParam(value = "memberType", required = false) Integer memberType,
            @RequestParam(value = "authStatus", required = false) Integer authStatus,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize)
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商户端不允许访问平台商户经营概况");
        }
        return overviewService.list(memberCode, nickname, legalSubjectName, memberType, authStatus, status,
                pageNum, pageSize);
    }

    /** 查询指定商户的仓库、商品、套餐和库存只读明细。 */
    @RequiresPermissions("platform:merchantOverview:list")
    @GetMapping("/{memberCode}/business")
    public PlatformMerchantBusinessView business(@PathVariable("memberCode") String memberCode)
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商户端不允许访问平台商户经营明细");
        }
        return overviewService.getBusiness(memberCode);
    }

    /** 查询指定商户的履约、物流和风险治理汇总。 */
    @RequiresPermissions("platform:merchantOverview:governance:view")
    @GetMapping("/{memberCode}/governance")
    public PlatformMerchantGovernanceView governance(@PathVariable("memberCode") String memberCode)
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商户端不允许访问商户治理明细");
        }
        return overviewService.getGovernance(memberCode);
    }
}
