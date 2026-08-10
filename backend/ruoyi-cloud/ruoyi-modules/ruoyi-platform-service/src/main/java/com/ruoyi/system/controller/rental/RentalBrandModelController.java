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
import com.ruoyi.system.domain.rental.RentalBrandModel;
import com.ruoyi.system.service.rental.RentalBrandModelService;

/** 平台标准品牌型号管理入口。 */
@RestController
@RequestMapping("/rental/brand-model")
public class RentalBrandModelController extends BaseController
{
    private final RentalBrandModelService brandModelService;

    /**
     * 注入平台标准品牌型号业务服务。
     *
     * @param brandModelService 标准品牌型号业务服务
     */
    public RentalBrandModelController(RentalBrandModelService brandModelService)
    {
        this.brandModelService = brandModelService;
    }

    /**
     * 查询平台标准品牌型号列表。
     *
     * @param query 品牌、型号、类目和状态查询条件
     * @return 标准品牌型号列表
     */
    @RequiresPermissions("platform:brandModel:list")
    @GetMapping("/list")
    public TableDataInfo list(RentalBrandModel query)
    {
        assertPlatformClient();
        startPage();
        return getDataTable(brandModelService.list(query));
    }

    /**
     * 查询平台标准品牌型号详情。
     *
     * @param modelId 标准型号主键
     * @return 标准型号详情
     */
    @RequiresPermissions("platform:brandModel:query")
    @GetMapping("/{modelId}")
    public AjaxResult get(@PathVariable Long modelId)
    {
        assertPlatformClient();
        return success(brandModelService.get(modelId));
    }

    /**
     * 新增平台标准品牌型号。
     *
     * @param model 待新增标准型号
     * @return 操作结果
     */
    @RequiresPermissions("platform:brandModel:add")
    @Log(title = "标准品牌型号", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody RentalBrandModel model)
    {
        assertPlatformClient();
        return toAjax(brandModelService.add(model, SecurityUtils.getUsername()));
    }

    /**
     * 修改平台标准品牌型号。
     *
     * @param model 待修改标准型号
     * @return 操作结果
     */
    @RequiresPermissions("platform:brandModel:edit")
    @Log(title = "标准品牌型号", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult update(@Validated @RequestBody RentalBrandModel model)
    {
        assertPlatformClient();
        return toAjax(brandModelService.update(model, SecurityUtils.getUsername()));
    }

    /**
     * 软删除未被业务引用的平台标准品牌型号。
     *
     * @param modelId 标准型号主键
     * @return 操作结果
     */
    @RequiresPermissions("platform:brandModel:remove")
    @Log(title = "标准品牌型号", businessType = BusinessType.DELETE)
    @DeleteMapping("/{modelId}")
    public AjaxResult delete(@PathVariable Long modelId)
    {
        assertPlatformClient();
        return toAjax(brandModelService.delete(modelId, SecurityUtils.getUsername()));
    }

    /**
     * 校验请求来自平台客户端，避免商户令牌调用平台标准主数据写接口。
     */
    private void assertPlatformClient()
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商户端不允许访问平台标准品牌型号接口");
        }
    }
}
