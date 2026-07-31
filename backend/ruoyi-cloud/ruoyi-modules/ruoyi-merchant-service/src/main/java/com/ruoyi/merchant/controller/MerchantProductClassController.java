package com.ruoyi.merchant.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.merchant.application.MerchantProductClassService;
import com.ruoyi.merchant.model.MerchantProductClassSelectionCommand;

/** 商户经营产品类目管理入口。 */
@RestController
@RequestMapping("/merchant/class")
public class MerchantProductClassController extends BaseController
{
    private final MerchantProductClassService productClassService;

    /**
     * 注入商户经营产品类目应用服务。
     *
     * @param productClassService 商户经营产品类目应用服务
     */
    public MerchantProductClassController(MerchantProductClassService productClassService)
    {
        this.productClassService = productClassService;
    }

    /**
     * 查询平台当前可供商户选择的全部正常类目树。
     *
     * @return 平台正常类目树
     */
    @RequiresPermissions("merchant:class:list")
    @GetMapping("/options")
    public AjaxResult options()
    {
        return success(productClassService.listOptions());
    }

    /**
     * 查询当前登录商户已经保存的经营类目主键。
     *
     * @return 当前商户经营类目主键集合
     */
    @RequiresPermissions("merchant:class:list")
    @GetMapping("/selected")
    public AjaxResult selected()
    {
        return success(productClassService.listSelectedIds());
    }

    /**
     * 查询当前商户业务可见类目树，商品和套餐页面只能使用该接口的数据。
     *
     * @return 当前商户业务可见类目树
     */
    @RequiresPermissions("merchant:class:list")
    @GetMapping("/tree")
    public AjaxResult businessTree()
    {
        return success(productClassService.listBusinessTree());
    }

    /**
     * 保存当前登录商户的经营类目，不接收任何租户或商户归属字段。
     *
     * @param command 商户经营类目保存命令
     * @return 操作结果
     */
    @RequiresPermissions("merchant:class:edit")
    @Log(title = "经营类目", businessType = BusinessType.UPDATE)
    @PutMapping("/selected")
    public AjaxResult save(@RequestBody MerchantProductClassSelectionCommand command)
    {
        productClassService.saveSelections(command == null ? null : command.getClassIds());
        return success();
    }
}
