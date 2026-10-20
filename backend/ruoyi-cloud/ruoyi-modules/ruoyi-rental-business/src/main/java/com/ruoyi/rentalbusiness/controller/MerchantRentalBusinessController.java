package com.ruoyi.rentalbusiness.controller;

import java.util.List;
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
import com.ruoyi.rentalbusiness.model.RentalBusinessCommand;
import com.ruoyi.rentalbusiness.model.RentalBusinessVO;
import com.ruoyi.rentalbusiness.service.RentalBusinessService;

/**
 * 商户端商品、SN、配件、套餐、仓库和库存接口。
 *
 * <p>本控制器只接收业务输入，不接收 tenantId、memberId、createBy 等归属字段。
 * 所有归属和仓库范围由 {@link RentalBusinessService} 从可信商户登录上下文中取得，
 * 避免通过修改请求参数越权访问其他商户数据。</p>
 */
@RestController
@RequestMapping("/merchant")
public class MerchantRentalBusinessController extends BaseController
{
    private final RentalBusinessService businessService;

    /**
     * 注入商户租赁主数据应用服务。
     *
     * @param businessService 租赁主数据应用服务
     */
    public MerchantRentalBusinessController(RentalBusinessService businessService)
    {
        this.businessService = businessService;
    }

    /** 查询当前商户仓库列表。 */
    @RequiresPermissions("merchant:warehouse:list")
    @GetMapping("/warehouse/list")
    public TableDataInfo warehouseList(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status)
    {
        startPage();
        return getDataTable(businessService.listWarehouses(keyword, status));
    }

    /** 查询当前商户仓库详情。 */
    @RequiresPermissions("merchant:warehouse:query")
    @GetMapping("/warehouse/{id}")
    public AjaxResult warehouse(@PathVariable Long id)
    {
        return success(businessService.getWarehouse(id));
    }

    /** 新增当前商户仓库。 */
    @RequiresPermissions("merchant:warehouse:add")
    @Log(title = "商户仓库", businessType = BusinessType.INSERT)
    @PostMapping("/warehouse")
    public AjaxResult addWarehouse(@RequestBody RentalBusinessCommand command)
    {
        return success(businessService.addWarehouse(command));
    }

    /** 修改当前商户仓库。 */
    @RequiresPermissions("merchant:warehouse:edit")
    @Log(title = "商户仓库", businessType = BusinessType.UPDATE)
    @PutMapping("/warehouse")
    public AjaxResult updateWarehouse(@RequestBody RentalBusinessCommand command)
    {
        return success(businessService.updateWarehouse(command));
    }

    /** 软删除当前商户仓库。 */
    @RequiresPermissions("merchant:warehouse:remove")
    @Log(title = "商户仓库", businessType = BusinessType.DELETE)
    @DeleteMapping("/warehouse/{id}")
    public AjaxResult deleteWarehouse(@PathVariable Long id)
    {
        return toAjax(businessService.deleteWarehouse(id));
    }

    /** 查询当前商户商品列表。 */
    @RequiresPermissions("merchant:product:list")
    @GetMapping("/product/list")
    public TableDataInfo productList(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer productType, @RequestParam(required = false) Integer stockMode,
            @RequestParam(required = false) Long classId, @RequestParam(required = false) Integer status)
    {
        startPage();
        return getDataTable(businessService.listProducts(keyword, productType, stockMode, classId, status));
    }

    /** 查询商品详情。 */
    @RequiresPermissions("merchant:product:query")
    @GetMapping("/product/{id}")
    public AjaxResult product(@PathVariable Long id)
    {
        return success(businessService.getProduct(id));
    }

    /** 新增商品。 */
    @RequiresPermissions("merchant:product:add")
    @Log(title = "商户商品", businessType = BusinessType.INSERT)
    @PostMapping("/product")
    public AjaxResult addProduct(@RequestBody RentalBusinessCommand command)
    {
        return success(businessService.addProduct(command));
    }

    /** 修改商品。 */
    @RequiresPermissions("merchant:product:edit")
    @Log(title = "商户商品", businessType = BusinessType.UPDATE)
    @PutMapping("/product")
    public AjaxResult updateProduct(@RequestBody RentalBusinessCommand command)
    {
        return success(businessService.updateProduct(command));
    }

    /** 软删除商品。 */
    @RequiresPermissions("merchant:product:remove")
    @Log(title = "商户商品", businessType = BusinessType.DELETE)
    @DeleteMapping("/product/{id}")
    public AjaxResult deleteProduct(@PathVariable Long id)
    {
        return toAjax(businessService.deleteProduct(id));
    }

    /** 查询 SN 列表。 */
    @RequiresPermissions("merchant:sn:list")
    @GetMapping("/sn/list")
    public TableDataInfo snList(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long productId, @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer assetStatus)
    {
        startPage();
        return getDataTable(businessService.listSns(keyword, productId, warehouseId, assetStatus));
    }

    /** 查询 SN 详情。 */
    @RequiresPermissions("merchant:sn:query")
    @GetMapping("/sn/{id}")
    public AjaxResult sn(@PathVariable Long id)
    {
        return success(businessService.getSn(id));
    }

    /** 新增 SN。 */
    @RequiresPermissions("merchant:sn:add")
    @Log(title = "商户商品SN", businessType = BusinessType.INSERT)
    @PostMapping("/sn")
    public AjaxResult addSn(@RequestBody RentalBusinessCommand command)
    {
        return success(businessService.addSn(command));
    }

    /** 修改 SN。 */
    @RequiresPermissions("merchant:sn:edit")
    @Log(title = "商户商品SN", businessType = BusinessType.UPDATE)
    @PutMapping("/sn")
    public AjaxResult updateSn(@RequestBody RentalBusinessCommand command)
    {
        return success(businessService.updateSn(command));
    }

    /** 软删除 SN。 */
    @RequiresPermissions("merchant:sn:remove")
    @Log(title = "商户商品SN", businessType = BusinessType.DELETE)
    @DeleteMapping("/sn/{id}")
    public AjaxResult deleteSn(@PathVariable Long id)
    {
        return toAjax(businessService.deleteSn(id));
    }

    /** 查询逐件配件实例列表。 */
    @RequiresPermissions("merchant:accessory:list")
    @GetMapping("/accessory/list")
    public TableDataInfo accessoryList(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long productId, @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer itemStatus)
    {
        startPage();
        return getDataTable(businessService.listAccessories(keyword, productId, warehouseId, itemStatus));
    }

    /** 查询配件实例详情。 */
    @RequiresPermissions("merchant:accessory:query")
    @GetMapping("/accessory/{id}")
    public AjaxResult accessory(@PathVariable Long id)
    {
        return success(businessService.getAccessory(id));
    }

    /** 新增逐件配件实例。 */
    @RequiresPermissions("merchant:accessory:add")
    @Log(title = "商户配件实例", businessType = BusinessType.INSERT)
    @PostMapping("/accessory")
    public AjaxResult addAccessory(@RequestBody RentalBusinessCommand command)
    {
        return success(businessService.addAccessory(command));
    }

    /** 修改逐件配件实例。 */
    @RequiresPermissions("merchant:accessory:edit")
    @Log(title = "商户配件实例", businessType = BusinessType.UPDATE)
    @PutMapping("/accessory")
    public AjaxResult updateAccessory(@RequestBody RentalBusinessCommand command)
    {
        return success(businessService.updateAccessory(command));
    }

    /** 软删除逐件配件实例。 */
    @RequiresPermissions("merchant:accessory:remove")
    @Log(title = "商户配件实例", businessType = BusinessType.DELETE)
    @DeleteMapping("/accessory/{id}")
    public AjaxResult deleteAccessory(@PathVariable Long id)
    {
        return toAjax(businessService.deleteAccessory(id));
    }

}
