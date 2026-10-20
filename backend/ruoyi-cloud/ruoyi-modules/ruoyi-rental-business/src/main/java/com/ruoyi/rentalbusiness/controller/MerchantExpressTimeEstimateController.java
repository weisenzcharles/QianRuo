package com.ruoyi.rentalbusiness.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.rentalbusiness.model.ExpressTimeEstimateCommand;
import com.ruoyi.rentalbusiness.service.ExpressTimeEstimateService;

/** 商户端快递时效预估接口。 */
@RestController
@RequestMapping("/merchant/express-time-estimates")
public class MerchantExpressTimeEstimateController extends BaseController
{
    private final ExpressTimeEstimateService estimateService;

    /** 注入快递时效预估应用服务。 */
    public MerchantExpressTimeEstimateController(ExpressTimeEstimateService estimateService)
    {
        this.estimateService = estimateService;
    }

    /** 查询寄件地到收件地的快递时效，使用现有订单查询权限保护接口。 */
    @RequiresPermissions("merchant:order:list")
    @GetMapping
    public AjaxResult query(@ModelAttribute ExpressTimeEstimateCommand command)
    {
        return success(estimateService.query(command));
    }
}
