package com.ruoyi.merchant.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import com.ruoyi.merchant.application.MerchantInternalNoticeService;
import com.ruoyi.merchant.model.MerchantInternalNoticeCommand;
import com.ruoyi.merchant.model.MerchantInternalNoticeQuery;
import com.ruoyi.merchant.model.MerchantInternalNoticeRoleOptionVO;
import com.ruoyi.merchant.model.MerchantInternalNoticeTopVO;
import com.ruoyi.merchant.model.MerchantInternalNoticeVO;

/** 商户内部通知与员工组投放接口。 */
@RestController
@RequestMapping("/merchant/internal-notice")
public class MerchantInternalNoticeController extends BaseController
{
    @Autowired
    private MerchantInternalNoticeService service;

    /** 查询当前用户顶部可见的内部通知。 */
    @RequiresPermissions("merchant:internalNotice:read")
    @GetMapping("/top")
    public AjaxResult top()
    {
        return success(service.top());
    }

    /** 查询内部通知详情并校验当前用户受众范围。 */
    @RequiresPermissions("merchant:internalNotice:read")
    @GetMapping("/{noticeId}")
    public AjaxResult readableDetail(@PathVariable Long noticeId)
    {
        return success(service.readableDetail(noticeId));
    }

    /** 标记一条内部通知已读。 */
    @RequiresPermissions("merchant:internalNotice:read")
    @PostMapping("/{noticeId}/read")
    public AjaxResult markRead(@PathVariable Long noticeId)
    {
        service.markRead(noticeId);
        return success();
    }

    /** 批量标记顶部列表内的内部通知已读。 */
    @RequiresPermissions("merchant:internalNotice:read")
    @PostMapping("/read-all")
    public AjaxResult markReadAll(@RequestParam(required = false) Long[] ids)
    {
        service.markReadAll(ids);
        return success();
    }

    /** 查询内部通知管理列表。 */
    @RequiresPermissions("merchant:internalNotice:list")
    @GetMapping("/list")
    public TableDataInfo list(MerchantInternalNoticeQuery query)
    {
        startPage();
        List<MerchantInternalNoticeVO> rows = service.list(query);
        return getDataTable(rows);
    }

    /** 查询员工组下拉选项。 */
    @RequiresPermissions("merchant:internalNotice:list")
    @GetMapping("/roles")
    public AjaxResult roles()
    {
        List<MerchantInternalNoticeRoleOptionVO> rows = service.roleOptions();
        return success(rows);
    }

    /** 查询内部通知管理详情。 */
    @RequiresPermissions("merchant:internalNotice:list")
    @GetMapping("/manage/{noticeId}")
    public AjaxResult managementDetail(@PathVariable Long noticeId)
    {
        return success(service.managementDetail(noticeId));
    }

    /** 查询内部通知对实际可见员工的阅读统计。 */
    @RequiresPermissions("merchant:internalNotice:list")
    @GetMapping("/manage/{noticeId}/read-stats")
    public AjaxResult readStats(@PathVariable Long noticeId)
    {
        return success(service.readStats(noticeId));
    }

    /** 新增内部通知。 */
    @RequiresPermissions("merchant:internalNotice:add")
    @Log(title = "商户内部通知", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult create(@RequestBody MerchantInternalNoticeCommand command)
    {
        return success(service.create(command));
    }

    /** 修改草稿或已下架内部通知。 */
    @RequiresPermissions("merchant:internalNotice:edit")
    @Log(title = "商户内部通知", businessType = BusinessType.UPDATE)
    @PutMapping("/{noticeId}")
    public AjaxResult update(@PathVariable Long noticeId, @RequestBody MerchantInternalNoticeCommand command)
    {
        return success(service.update(noticeId, command));
    }

    /** 发布内部通知。 */
    @RequiresPermissions("merchant:internalNotice:edit")
    @Log(title = "商户内部通知", businessType = BusinessType.UPDATE)
    @PostMapping("/{noticeId}/publish")
    public AjaxResult publish(@PathVariable Long noticeId)
    {
        return success(service.publish(noticeId));
    }

    /** 下架内部通知。 */
    @RequiresPermissions("merchant:internalNotice:edit")
    @Log(title = "商户内部通知", businessType = BusinessType.UPDATE)
    @PostMapping("/{noticeId}/offline")
    public AjaxResult offline(@PathVariable Long noticeId)
    {
        return success(service.offline(noticeId));
    }

    /** 删除草稿或已下架内部通知。 */
    @RequiresPermissions("merchant:internalNotice:remove")
    @Log(title = "商户内部通知", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeId}")
    public AjaxResult delete(@PathVariable Long noticeId)
    {
        service.delete(noticeId);
        return success();
    }
}
