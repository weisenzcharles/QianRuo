package com.ruoyi.system.controller.rental;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import com.ruoyi.system.api.model.PlatformLogisticsDetailView;
import com.ruoyi.system.service.rental.PlatformLogisticsService;

/** 平台物流治理查询和刷新接口。 */
@RestController
@RequestMapping("/rental/logistics")
public class PlatformLogisticsController extends BaseController
{
    private final PlatformLogisticsService service;

    public PlatformLogisticsController(PlatformLogisticsService service)
    {
        this.service = service;
    }

    /** 分页查询全部商户物流台账。 */
    @RequiresPermissions("platform:logistics:list")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "merchantKeyword", required = false) String merchantKeyword,
            @RequestParam(value = "direction", required = false) Integer direction,
            @RequestParam(value = "routeStatus", required = false) Integer routeStatus,
            @RequestParam(value = "abnormalOnly", required = false) Boolean abnormalOnly,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize)
    {
        assertPlatformClient();
        return service.list(keyword, merchantKeyword, direction, routeStatus, abnormalOnly, pageNum, pageSize);
    }

    /** 查询物流详情和已落库路线。 */
    @RequiresPermissions("platform:logistics:list")
    @GetMapping("/{logisticsId}")
    public AjaxResult detail(@PathVariable Long logisticsId)
    {
        assertPlatformClient();
        PlatformLogisticsDetailView detail = service.detail(logisticsId);
        return success(detail);
    }

    /** 触发未签收物流路线批量刷新，不改变订单履约状态。 */
    @Log(title = "平台物流路线刷新", businessType = BusinessType.UPDATE)
    @RequiresPermissions("platform:logistics:refresh")
    @PostMapping("/refresh")
    public AjaxResult refresh()
    {
        assertPlatformClient();
        return success().put("refreshed", service.refresh());
    }

    /** 手工刷新平台选中的单条物流路线，不改变订单履约状态。 */
    @Log(title = "平台单条物流路线刷新", businessType = BusinessType.UPDATE)
    @RequiresPermissions("platform:logistics:refresh")
    @PostMapping("/{logisticsId}/refresh")
    public AjaxResult refreshOne(@PathVariable Long logisticsId)
    {
        assertPlatformClient();
        return success().put("refreshed", service.refresh(logisticsId));
    }

    private void assertPlatformClient()
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("当前接口仅支持平台端访问");
        }
    }
}
