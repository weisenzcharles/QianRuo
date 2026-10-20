package com.ruoyi.system.controller.rental;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.model.rental.PlatformBrandCommand;
import com.ruoyi.system.model.rental.PlatformBrandQuery;
import com.ruoyi.system.service.rental.RentalBrandService;

/** 平台品牌管理入口。 */
@RestController
@RequestMapping("/rental/brand")
public class RentalBrandController extends BaseController
{
    private final RentalBrandService brandService;

    /** 注入平台品牌业务服务。 */
    public RentalBrandController(RentalBrandService brandService)
    {
        this.brandService = brandService;
    }

    /** 查询平台品牌列表。 */
    @RequiresPermissions("platform:brand:list")
    @GetMapping("/list")
    public TableDataInfo list(@Validated PlatformBrandQuery query)
    {
        assertPlatformClient();
        startPage();
        return getDataTable(brandService.list(query));
    }

    /** 查询平台品牌详情。 */
    @RequiresPermissions("platform:brand:query")
    @GetMapping("/{brandId}")
    public AjaxResult get(@PathVariable Long brandId)
    {
        assertPlatformClient();
        return success(brandService.get(brandId));
    }

    /** 新增平台品牌。 */
    @RequiresPermissions("platform:brand:add")
    @Log(title = "品牌管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody PlatformBrandCommand command)
    {
        assertPlatformClient();
        return toAjax(brandService.add(command, SecurityUtils.getUserId()));
    }

    /** 修改平台品牌。 */
    @RequiresPermissions("platform:brand:edit")
    @Log(title = "品牌管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult update(@Validated @RequestBody PlatformBrandCommand command)
    {
        assertPlatformClient();
        return toAjax(brandService.update(command, SecurityUtils.getUserId()));
    }

    /** 软删除未被产品型号引用的平台品牌。 */
    @RequiresPermissions("platform:brand:remove")
    @Log(title = "品牌管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{brandId}")
    public AjaxResult delete(@PathVariable Long brandId)
    {
        assertPlatformClient();
        return toAjax(brandService.delete(brandId, SecurityUtils.getUserId()));
    }

    /** 仅允许平台客户端调用平台品牌主数据接口。 */
    private void assertPlatformClient()
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商户端不允许访问平台品牌管理接口");
        }
    }
}
