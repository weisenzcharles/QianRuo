package com.ruoyi.rentalbusiness.controller;

import java.util.List;
import java.util.Map;
import java.util.Collections;
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
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.rentalbusiness.model.MerchantAllianceDisputeCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantAllianceDisputeEvidenceCommand;
import com.ruoyi.rentalbusiness.model.MerchantAllianceMediationConfirmCommand;
import com.ruoyi.rentalbusiness.service.MerchantAllianceDisputeService;

/** 商户端联盟订单纠纷接口。 */
@RestController
@RequestMapping("/merchant/alliance-disputes")
public class MerchantAllianceDisputeController extends BaseController {
    private final MerchantAllianceDisputeService service;

    public MerchantAllianceDisputeController(MerchantAllianceDisputeService service) {
        this.service = service;
    }

    /** 查询当前商户参与的纠纷。 */
    @RequiresPermissions("merchant:alliance:list")
    @GetMapping
    public TableDataInfo list(@RequestParam(required = false) Integer disputeStatus) {
        startPage();
        List<Map<String, Object>> rows = service.list(disputeStatus);
        return getDataTable(rows);
    }

    /** 查询纠纷详情及证据。 */
    @RequiresPermissions("merchant:alliance:list")
    @GetMapping("/{disputeId}")
    public AjaxResult get(@PathVariable Long disputeId) {
        return success(service.get(disputeId));
    }

    /** 发起联盟订单纠纷。 */
    @RequiresPermissions("merchant:alliance:list")
    @PostMapping
    public AjaxResult create(@RequestBody MerchantAllianceDisputeCreateCommand command) {
        return success(Collections.singletonMap("disputeId", service.create(command)));
    }

    /** 提交纠纷证据。 */
    @RequiresPermissions("merchant:alliance:list")
    @PostMapping("/{disputeId}/evidence")
    public AjaxResult addEvidence(@PathVariable Long disputeId, @RequestBody MerchantAllianceDisputeEvidenceCommand command) {
        service.addEvidence(disputeId, command);
        return success();
    }

    /** 确认或拒绝当前联盟纠纷调解方案。 */
    @RequiresPermissions("merchant:alliance:list")
    @PostMapping("/{disputeId}/mediation/confirm")
    public AjaxResult confirmMediation(@PathVariable Long disputeId,
            @RequestBody MerchantAllianceMediationConfirmCommand command) {
        service.confirmMediation(disputeId, command);
        return success();
    }
}
