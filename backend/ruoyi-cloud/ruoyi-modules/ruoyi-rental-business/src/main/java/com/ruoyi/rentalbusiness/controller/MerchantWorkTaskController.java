package com.ruoyi.rentalbusiness.controller;

import java.time.LocalDate;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.rentalbusiness.model.MerchantWorkTaskCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantWorkTaskReviewCommand;
import com.ruoyi.rentalbusiness.model.MerchantWorkTaskSubmissionCommand;
import com.ruoyi.rentalbusiness.service.MerchantWorkTaskService;

/** 商户通用工作台任务接口。 */
@RestController
@RequestMapping("/merchant/work")
public class MerchantWorkTaskController extends BaseController
{
    private final MerchantWorkTaskService service;

    public MerchantWorkTaskController(MerchantWorkTaskService service)
    {
        this.service = service;
    }

    /** 分页查询当前用户可见任务。 */
    @RequiresPermissions("merchant:work:task")
    @GetMapping("/tasks")
    public TableDataInfo list(@RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer status, @RequestParam(required = false) String keyword)
    {
        startPage();
        return getDataTable(service.list(warehouseId, status, keyword));
    }

    /** 查询工作台任务汇总。 */
    @RequiresPermissions("merchant:work:task")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        return success(service.summary());
    }

    /** 查询指定日期的工作日报，数据范围遵循当前用户的仓库权限。 */
    @RequiresPermissions("merchant:work:task")
    @GetMapping("/report")
    public AjaxResult report(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate reportDate,
            @RequestParam(required = false) Long warehouseId)
    {
        return success(service.dailyReport(reportDate, warehouseId));
    }

    /** 查询商户内可指派用户。 */
    @RequiresPermissions("merchant:work:task")
    @GetMapping("/users")
    public AjaxResult userOptions()
    {
        return success(service.userOptions());
    }

    /** 查询任务详情及完整时间线。 */
    @RequiresPermissions("merchant:work:task")
    @GetMapping("/tasks/{taskId}")
    public AjaxResult detail(@PathVariable Long taskId)
    {
        return success(service.detail(taskId));
    }

    /** 创建任务。 */
    @RequiresPermissions("merchant:work:task")
    @Log(title = "工作台任务", businessType = BusinessType.INSERT)
    @PostMapping("/tasks")
    public AjaxResult create(@RequestBody MerchantWorkTaskCreateCommand command)
    {
        return success(service.create(command));
    }

    /** 提交任务完成说明。 */
    @RequiresPermissions("merchant:work:task")
    @Log(title = "工作台任务提交", businessType = BusinessType.UPDATE)
    @PostMapping("/tasks/{taskId}/submit")
    public AjaxResult submit(@PathVariable Long taskId, @RequestBody MerchantWorkTaskSubmissionCommand command)
    {
        service.submit(taskId, command);
        return success();
    }

    /** 提交任务质检结果。 */
    @RequiresPermissions("merchant:work:task")
    @Log(title = "工作台任务质检", businessType = BusinessType.UPDATE)
    @PostMapping("/tasks/{taskId}/review")
    public AjaxResult review(@PathVariable Long taskId, @RequestBody MerchantWorkTaskReviewCommand command)
    {
        service.review(taskId, command);
        return success();
    }

    /** 标记当前用户的任务已阅。 */
    @RequiresPermissions("merchant:work:task")
    @Log(title = "工作台任务已阅", businessType = BusinessType.UPDATE)
    @PostMapping("/tasks/{taskId}/read")
    public AjaxResult markRead(@PathVariable Long taskId)
    {
        service.markRead(taskId);
        return success();
    }
}
