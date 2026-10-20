package com.ruoyi.rentalbusiness.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.rentalbusiness.model.MerchantOrderCreateCommand;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.rentalbusiness.model.MerchantOrderListVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderNoteVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderNoteCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderEditLogVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderSummaryVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderQuery;
import com.ruoyi.rentalbusiness.model.MerchantOrderRecipientUpdateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderForwardCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderLogisticsUpdateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderAllianceTransferCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderAllianceTransferResult;
import com.ruoyi.rentalbusiness.model.MerchantAllianceOptionVO;
import com.ruoyi.rentalbusiness.model.MerchantAllianceAcceptCommand;
import com.ruoyi.rentalbusiness.model.MerchantAllianceReassignCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderPackageDeviceBindCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderShipCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderReturnInspectionCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderActionCommand;
import com.ruoyi.rentalbusiness.model.MerchantAfterSalesCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderMediaCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderMediaBatchCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderMediaRecognizeCommand;
import com.ruoyi.rentalbusiness.service.MerchantOrderPackageDeviceService;
import com.ruoyi.rentalbusiness.service.MerchantLogisticsTrackingService;
import com.ruoyi.rentalbusiness.service.MerchantOrderService;
import com.ruoyi.rentalbusiness.service.MerchantOrderAllianceService;
import com.ruoyi.rentalbusiness.service.MerchantOrderMediaService;

/** 商户订单工作台接口。订单数据由 rental-business 独立维护。 */
@RestController
@RequestMapping("/merchant/orders")
public class MerchantOrderController extends BaseController
{
    private final MerchantOrderService orderService;
    private final MerchantOrderPackageDeviceService packageDeviceService;
    private final MerchantOrderAllianceService allianceService;
    private final MerchantLogisticsTrackingService logisticsTrackingService;
    private final MerchantOrderMediaService mediaService;

    /**
     * 注入商户订单应用服务。
     *
     * @param orderService 商户订单应用服务
     */
    public MerchantOrderController(MerchantOrderService orderService,
            MerchantOrderPackageDeviceService packageDeviceService,
            MerchantOrderAllianceService allianceService, MerchantLogisticsTrackingService logisticsTrackingService,
            MerchantOrderMediaService mediaService)
    {
        this.orderService = orderService;
        this.packageDeviceService = packageDeviceService;
        this.allianceService = allianceService;
        this.logisticsTrackingService = logisticsTrackingService;
        this.mediaService = mediaService;
    }

    /** 查询当前商户可用的租商联盟转单目标。 */
    @RequiresPermissions("merchant:order:list")
    @GetMapping("/alliance-options")
    public AjaxResult allianceOptions()
    {
        return success(allianceService.listOptions());
    }

    /** 将当前订单提交给租商联盟或指定联盟商户。 */
    @Log(title = "订单转单", businessType = BusinessType.UPDATE)
    @RequiresPermissions("merchant:order:edit")
    @PostMapping("/alliance")
    public AjaxResult transferAlliance(@RequestBody MerchantOrderAllianceTransferCommand command)
    {
        MerchantOrderAllianceTransferResult result = allianceService.transfer(command);
        return success(result);
    }

    /** 查询当前商户的联盟协助订单。 */
    @RequiresPermissions("merchant:alliance:list")
    @GetMapping("/alliance-tasks")
    public TableDataInfo allianceTasks(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer status,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String view,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String keyword,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String deliveryCity,
            @org.springframework.web.bind.annotation.RequestParam(required = false) LocalDate leaseStartDate)
    {
        startPage();
        return getDataTable(allianceService.listTasks(status, view, keyword, deliveryCity, leaseStartDate));
    }

    /** 查询定向订单或公众池状态汇总。 */
    @RequiresPermissions("merchant:alliance:list")
    @GetMapping("/alliance-tasks/summary")
    public AjaxResult allianceTaskSummary(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String view)
    {
        return success(allianceService.taskSummary(view));
    }

    /** 接受协助订单并创建子订单。 */
    @RequiresPermissions("merchant:alliance:list")
    @PostMapping("/alliance-tasks/{allianceOrderId}/accept")
    public AjaxResult acceptAllianceTask(@PathVariable Long allianceOrderId,
            @RequestBody(required = false) MerchantAllianceAcceptCommand command)
    { return success(allianceService.accept(allianceOrderId, command)); }

    @RequiresPermissions("merchant:alliance:list")
    @PostMapping("/alliance-tasks/{allianceOrderId}/warehouse")
    public AjaxResult changeAllianceWarehouse(@PathVariable Long allianceOrderId,
            @RequestBody MerchantAllianceAcceptCommand command)
    { allianceService.changeFulfillmentWarehouse(allianceOrderId, command); return success(); }

    /** 拒绝协助订单。 */
    @RequiresPermissions("merchant:alliance:list")
    @PostMapping("/alliance-tasks/{allianceOrderId}/reject")
    public AjaxResult rejectAllianceTask(@PathVariable Long allianceOrderId)
    { allianceService.reject(allianceOrderId); return success(); }

    /** 将待接受协助订单转派给其他有效联盟成员。 */
    @RequiresPermissions("merchant:alliance:list")
    @PostMapping("/alliance-tasks/{allianceOrderId}/transfer")
    public AjaxResult transferAllianceTask(@PathVariable Long allianceOrderId,
            @RequestBody MerchantAllianceReassignCommand command)
    { allianceService.reassign(allianceOrderId, command); return success(); }

    /** 发起方取消尚未接受的协助订单。 */
    @RequiresPermissions("merchant:alliance:list")
    @PostMapping("/alliance-tasks/{allianceOrderId}/cancel")
    public AjaxResult cancelAllianceTask(@PathVariable Long allianceOrderId)
    { allianceService.cancel(allianceOrderId); return success(); }

    /** 发起方将已拒绝的协助订单重新发布到公众池。 */
    @RequiresPermissions("merchant:alliance:list")
    @PostMapping("/alliance-tasks/{allianceOrderId}/public-pool")
    public AjaxResult publishPublicPool(@PathVariable Long allianceOrderId)
    { allianceService.publishPublicPool(allianceOrderId); return success(); }

    /** 创建当前商户订单，写入前由服务层校验审核状态和仓库权限。 */
    @Log(title = "商户订单", businessType = BusinessType.INSERT)
    @RequiresPermissions("merchant:order:add")
    @PostMapping
    public AjaxResult add(@RequestBody MerchantOrderCreateCommand command)
    {
        orderService.add(command);
        logisticsTrackingService.initializeRouteSnapshots(command.orderId);
        return success();
    }

    /** 修改当前商户订单，写入前由服务层校验订单归属和业务状态。 */
    @Log(title = "商户订单", businessType = BusinessType.UPDATE)
    @RequiresPermissions("merchant:order:edit")
    @PutMapping
    public AjaxResult edit(@RequestBody MerchantOrderCreateCommand command)
    {
        orderService.update(command);
        logisticsTrackingService.initializeRouteSnapshots(command.orderId);
        return success();
    }

    /** 将当前订单从待发货置为已备货，只更新订单状态字段。 */
    @Log(title = "订单备货", businessType = BusinessType.UPDATE)
    @RequiresPermissions("merchant:order:edit")
    @PostMapping("/{orderId}/prepare")
    public AjaxResult prepare(@PathVariable Long orderId)
    {
        orderService.prepare(orderId);
        return success();
    }

    /** 保存发货快递信息并将订单推进到已发货。 */
    @Log(title = "订单发货", businessType = BusinessType.UPDATE)
    @RequiresPermissions("merchant:order:edit")
    @PostMapping("/{orderId}/ship")
    public AjaxResult ship(@PathVariable Long orderId, @RequestBody MerchantOrderShipCommand command)
    {
        orderService.ship(orderId, command);
        logisticsTrackingService.initializeRouteSnapshots(orderId);
        return success();
    }

    /** 归还入库成功后将订单推进到归还中。 */
    @Log(title = "订单归还入库", businessType = BusinessType.UPDATE)
    @RequiresPermissions("merchant:order:edit")
    @PostMapping("/{orderId}/returning")
    public AjaxResult returning(@PathVariable Long orderId)
    {
        orderService.markReturning(orderId);
        return success();
    }

    /** 提交归还验机结果，正常完成订单或创建售后单。 */
    @Log(title = "订单归还验机", businessType = BusinessType.UPDATE)
    @RequiresPermissions("merchant:order:edit")
    @PostMapping("/{orderId}/return-inspection")
    public AjaxResult returnInspection(@PathVariable Long orderId,
            @RequestBody MerchantOrderReturnInspectionCommand command)
    {
        return success(orderService.returnInspection(orderId, command));
    }

    /** 查询当前商户批量上传后待识别或待人工处理的发货视频。 */
    @RequiresPermissions("merchant:order:list")
    @GetMapping("/media/unmatched")
    public AjaxResult unmatchedMedia()
    {
        return success(mediaService.listUnmatched());
    }

    /** 批量登记已上传的发货视频，并尝试按 SN 绑定未完成订单。 */
    @Log(title = "批量订单履约视频", businessType = BusinessType.INSERT)
    @RequiresPermissions("merchant:order:edit")
    @PostMapping("/media/batch")
    public AjaxResult addMediaBatch(@RequestBody MerchantOrderMediaBatchCreateCommand command)
    {
        return success(mediaService.addBatch(command));
    }

    /** 对未匹配发货视频进行二次识别或人工补录 SN。 */
    @Log(title = "订单履约视频识别", businessType = BusinessType.UPDATE)
    @RequiresPermissions("merchant:order:edit")
    @PutMapping("/media/{mediaId}/recognize")
    public AjaxResult recognizeMedia(@PathVariable Long mediaId,
            @RequestBody MerchantOrderMediaRecognizeCommand command)
    {
        return success(mediaService.recognize(mediaId, command == null ? null : command.detectedSn));
    }

    /** 查询订单履约媒体凭证。 */
    @RequiresPermissions("merchant:order:list")
    @GetMapping("/{orderId}/media")
    public AjaxResult media(@PathVariable Long orderId)
    {
        return success(mediaService.list(orderId));
    }

    /** 登记统一文件服务已上传的订单履约媒体凭证。 */
    @Log(title = "订单履约凭证", businessType = BusinessType.INSERT)
    @RequiresPermissions("merchant:order:edit")
    @PostMapping("/{orderId}/media")
    public AjaxResult addMedia(@PathVariable Long orderId, @RequestBody MerchantOrderMediaCreateCommand command)
    {
        return success(mediaService.add(orderId, command));
    }

    /** 将归还或售后订单推进到已完成。 */
    @Log(title = "订单完成", businessType = BusinessType.UPDATE)
    @RequiresPermissions("merchant:order:edit")
    @PostMapping("/{orderId}/complete")
    public AjaxResult complete(@PathVariable Long orderId,
            @RequestBody(required = false) MerchantOrderActionCommand command)
    {
        orderService.complete(orderId, command);
        return success();
    }

    /** 取消未发货订单并释放库存预占。 */
    @Log(title = "订单取消", businessType = BusinessType.UPDATE)
    @RequiresPermissions("merchant:order:edit")
    @PostMapping("/{orderId}/cancel")
    public AjaxResult cancel(@PathVariable Long orderId,
            @RequestBody(required = false) MerchantOrderActionCommand command)
    {
        orderService.cancel(orderId, command);
        return success();
    }

    /** 在租赁中或归还阶段创建售后单。 */
    @Log(title = "订单售后", businessType = BusinessType.UPDATE)
    @RequiresPermissions("merchant:order:edit")
    @PostMapping("/{orderId}/after-sales")
    public AjaxResult afterSales(@PathVariable Long orderId, @RequestBody MerchantAfterSalesCreateCommand command)
    {
        return success(orderService.createAfterSales(orderId, command));
    }

    /** 更新当前商户订单指定方向的物流信息，不修改订单状态。 */
    @Log(title = "订单物流", businessType = BusinessType.UPDATE)
    @RequiresPermissions("merchant:order:edit")
    @PutMapping("/{orderId}/logistics")
    public AjaxResult updateLogistics(@PathVariable Long orderId,
            @RequestBody MerchantOrderLogisticsUpdateCommand command)
    {
        if (command == null)
        {
            command = new MerchantOrderLogisticsUpdateCommand();
        }
        command.orderId = orderId;
        orderService.updateLogistics(command);
        logisticsTrackingService.initializeRouteSnapshots(orderId);
        return success();
    }

    /** 查询当前商户订单指定方向的物流轨迹。 */
    @RequiresPermissions("merchant:order:list")
    @GetMapping("/{orderId}/logistics/track")
    public AjaxResult trackLogistics(@PathVariable Long orderId,
            @org.springframework.web.bind.annotation.RequestParam Integer direction)
    {
        return success(logisticsTrackingService.track(orderId, direction));
    }

    /** 更新当前商户订单的客户、收件人和备注快照。 */
    @Log(title = "订单客户信息", businessType = BusinessType.UPDATE)
    @RequiresPermissions("merchant:order:edit")
    @PutMapping("/{orderId}/recipient")
    public AjaxResult updateRecipient(@PathVariable Long orderId,
            @RequestBody MerchantOrderRecipientUpdateCommand command)
    {
        if (command == null)
        {
            command = new MerchantOrderRecipientUpdateCommand();
        }
        command.orderId = orderId;
        orderService.updateRecipient(command);
        return success();
    }

    /** 将运输中的订单转寄到新的收件地址，不改变订单履约状态。 */
    @Log(title = "订单转寄", businessType = BusinessType.UPDATE)
    @RequiresPermissions("merchant:order:edit")
    @PostMapping("/{orderId}/forward")
    public AjaxResult forward(@PathVariable Long orderId, @RequestBody MerchantOrderForwardCommand command)
    {
        if (command == null)
        {
            command = new MerchantOrderForwardCommand();
        }
        command.orderId = orderId;
        orderService.forward(command);
        return success();
    }

    /** 查询订单编辑所需的完整业务字段。 */
    @RequiresPermissions("merchant:order:list")
    @GetMapping("/{orderId}")
    public AjaxResult getInfo(@PathVariable Long orderId)
    {
        return success(orderService.get(orderId));
    }

    /** 查询订单套餐明细可绑定的逐件设备。 */
    @RequiresPermissions("merchant:order:list")
    @GetMapping("/{orderId}/package-devices")
    public AjaxResult packageDevices(@PathVariable Long orderId,
            @org.springframework.web.bind.annotation.RequestParam Integer itemIndex)
    {
        return success(packageDeviceService.list(orderId, itemIndex));
    }

    /** 绑定订单套餐明细的逐件设备，不改变订单工作流状态。 */
    @Log(title = "订单套餐设备", businessType = BusinessType.UPDATE)
    @RequiresPermissions("merchant:order:edit")
    @PutMapping("/{orderId}/package-devices")
    public AjaxResult bindPackageDevices(@PathVariable Long orderId,
            @RequestBody MerchantOrderPackageDeviceBindCommand command)
    {
        packageDeviceService.bind(orderId, command);
        return success();
    }

    /** 查询当前商户订单的历史批注，按创建时间倒序返回。 */
    @RequiresPermissions("merchant:order:list")
    @GetMapping("/{orderId}/notes")
    public AjaxResult notes(@PathVariable Long orderId)
    {
        List<MerchantOrderNoteVO> notes = orderService.listNotes(orderId);
        return success(notes);
    }

    /** 新增当前商户订单批注，服务层负责校验订单归属和内容。 */
    @Log(title = "订单批注", businessType = BusinessType.INSERT)
    @RequiresPermissions("merchant:order:edit")
    @PostMapping("/{orderId}/notes")
    public AjaxResult addNote(@PathVariable Long orderId, @RequestBody MerchantOrderNoteCreateCommand command)
    {
        orderService.addNote(orderId, command);
        return success();
    }

    /** 查询当前商户指定订单的字段编辑历史，按创建时间倒序返回。 */
    @RequiresPermissions("merchant:order:list")
    @GetMapping("/{orderId}/edit-logs")
    public AjaxResult editLogs(@PathVariable Long orderId)
    {
        List<MerchantOrderEditLogVO> logs = orderService.listEditLogs(orderId);
        return success(logs);
    }

    /**
     * 分页查询当前商户可见的订单列表。
     *
     * 租户、商户和仓库范围不接受前端传参，应用服务会在调用 Mapper 前使用
     * 当前登录态覆盖查询范围；未审核商户允许读取，写操作由后续接口单独保护。
     *
     * @param query 订单筛选条件
     * @return 当前商户订单分页结果
     */
    @RequiresPermissions("merchant:order:list")
    @GetMapping
    public TableDataInfo list(MerchantOrderQuery query)
    {
        // 套餐明细投影包含 JSON_TABLE 和相关子查询，列表总数使用独立轻量 SQL。
        startPage(false);
        List<MerchantOrderListVO> list = orderService.list(query);
        return getDataTable(list, orderService.count(query));
    }

    /** 查询当前商户订单中心顶部概览，统计不受列表筛选条件影响。 */
    @RequiresPermissions("merchant:order:list")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        MerchantOrderSummaryVO summary = orderService.summary();
        return success(summary);
    }
}
