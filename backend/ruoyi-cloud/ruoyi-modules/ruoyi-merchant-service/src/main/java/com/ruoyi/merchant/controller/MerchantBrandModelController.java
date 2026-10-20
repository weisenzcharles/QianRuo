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
import com.ruoyi.merchant.application.MerchantBrandModelService;
import com.ruoyi.merchant.model.MerchantBrandModelSelectionCommand;

/** 商户常用品牌型号管理入口。 */
@RestController
@RequestMapping("/merchant/brand-model")
public class MerchantBrandModelController extends BaseController
{
    private final MerchantBrandModelService brandModelService;

    /**
     * 注入商户常用品牌型号应用服务。
     *
     * @param brandModelService 商户常用品牌型号应用服务
     */
    public MerchantBrandModelController(MerchantBrandModelService brandModelService)
    {
        this.brandModelService = brandModelService;
    }

    /**
     * 查询当前商户经营分类内可用的标准型号，并回显已选择项。
     *
     * @return 当前商户常用型号配置选项
     */
    @RequiresPermissions("merchant:brandModel:list")
    @GetMapping("/options")
    public AjaxResult options()
    {
        return success(brandModelService.listOptions());
    }

    /**
     * 查询当前商户已选择的常用型号唯一编号。
     *
     * @return 常用型号唯一编号集合
     */
    @RequiresPermissions("merchant:brandModel:list")
    @GetMapping("/selected")
    public AjaxResult selected()
    {
        return success(brandModelService.listSelectedIds());
    }

    /**
     * 保存当前商户常用型号选择，租户和商户归属由服务端登录上下文确定。
     *
     * @param command 常用型号保存命令
     * @return 操作结果
     */
    @RequiresPermissions("merchant:brandModel:edit")
    @Log(title = "商户常用型号", businessType = BusinessType.UPDATE)
    @PutMapping("/selected")
    public AjaxResult save(@RequestBody MerchantBrandModelSelectionCommand command)
    {
        brandModelService.saveSelections(command == null ? null : command.getModelIds());
        return success();
    }
}
