package com.ruoyi.system.controller.rental;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.context.ClientContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.system.model.rental.PlatformMarketQuery;
import com.ruoyi.system.model.rental.PlatformMarketReportReviewCommand;
import com.ruoyi.system.service.rental.PlatformMarketService;

/** 平台周转市场帖子和举报治理接口。 */
@RestController
@RequestMapping("/rental/market")
public class PlatformMarketController extends BaseController
{
    private final PlatformMarketService service;

    public PlatformMarketController(PlatformMarketService service)
    {
        this.service = service;
    }

    /** 分页查询周转市场帖子。 */
    @RequiresPermissions("platform:market:list")
    @GetMapping("/posts")
    public TableDataInfo posts(PlatformMarketQuery query)
    {
        assertPlatformClient();
        startPage();
        return getDataTable(service.posts(query));
    }

    /** 分页查询周转市场举报。 */
    @RequiresPermissions("platform:market:list")
    @GetMapping("/reports")
    public TableDataInfo reports(PlatformMarketQuery query)
    {
        assertPlatformClient();
        startPage();
        return getDataTable(service.reports(query));
    }

    /** 处理市场举报，可联动违规下架帖子。 */
    @RequiresPermissions("platform:market:review")
    @Log(title = "周转市场举报", businessType = BusinessType.UPDATE)
    @PostMapping("/reports/{reportId}/review")
    public AjaxResult reviewReport(@PathVariable Long reportId,
            @Validated @RequestBody PlatformMarketReportReviewCommand command)
    {
        assertPlatformClient();
        service.reviewReport(reportId, command);
        return success();
    }

    /** 平台强制下架帖子。 */
    @RequiresPermissions("platform:market:moderate")
    @Log(title = "周转市场帖子下架", businessType = BusinessType.UPDATE)
    @PostMapping("/posts/{postId}/offline")
    public AjaxResult offlinePost(@PathVariable Long postId)
    {
        assertPlatformClient();
        service.offlinePost(postId);
        return success();
    }

    /** 更新帖子降权标识。 */
    @RequiresPermissions("platform:market:moderate")
    @Log(title = "周转市场帖子降权", businessType = BusinessType.UPDATE)
    @PostMapping("/posts/{postId}/rank")
    public AjaxResult updateRank(@PathVariable Long postId, @RequestBody PlatformMarketRankCommand command)
    {
        assertPlatformClient();
        service.updateRank(postId, command == null ? null : command.getDownRankFlag());
        return success();
    }

    private void assertPlatformClient()
    {
        if (!ClientContextHolder.isPlatformClient())
            throw new ServiceException("商户端不允许访问平台市场治理接口");
    }

    /** 平台帖子降权参数。 */
    public static class PlatformMarketRankCommand
    {
        private Integer downRankFlag;

        public Integer getDownRankFlag() { return downRankFlag; }
        public void setDownRankFlag(Integer downRankFlag) { this.downRankFlag = downRankFlag; }
    }
}
