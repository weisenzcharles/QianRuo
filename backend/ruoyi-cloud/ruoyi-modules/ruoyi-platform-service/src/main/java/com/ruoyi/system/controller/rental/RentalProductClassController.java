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
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.domain.rental.RentalProductClass;
import com.ruoyi.system.service.rental.RentalProductClassService;

/** 平台产品类目管理入口。 */
@RestController
@RequestMapping("/rental/class")
public class RentalProductClassController extends BaseController
{
    private final RentalProductClassService productClassService;

    /**
     * 注入平台产品类目业务服务。
     *
     * @param productClassService 平台产品类目业务服务
     */
    public RentalProductClassController(RentalProductClassService productClassService)
    {
        this.productClassService = productClassService;
    }

    /**
     * 查询平台产品类目树。
     *
     * @param query 类目名称和状态查询条件
     * @return 类目树
     */
    @RequiresPermissions("platform:class:list")
    @GetMapping("/tree")
    public AjaxResult tree(RentalProductClass query)
    {
        assertPlatformClient();
        return success(productClassService.listTree(query));
    }

    /**
     * 查询平台产品类目详情。
     *
     * @param classId 类目主键
     * @return 类目详情
     */
    @RequiresPermissions("platform:class:query")
    @GetMapping("/{classId}")
    public AjaxResult get(@PathVariable Long classId)
    {
        assertPlatformClient();
        return success(productClassService.get(classId));
    }

    /**
     * 新增平台产品类目。
     *
     * @param productClass 待新增类目
     * @return 操作结果
     */
    @RequiresPermissions("platform:class:add")
    @Log(title = "产品类目", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody RentalProductClass productClass)
    {
        assertPlatformClient();
        return toAjax(productClassService.add(productClass, SecurityUtils.getUsername()));
    }

    /**
     * 修改平台产品类目。
     *
     * @param productClass 待修改类目
     * @return 操作结果
     */
    @RequiresPermissions("platform:class:edit")
    @Log(title = "产品类目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult update(@Validated @RequestBody RentalProductClass productClass)
    {
        assertPlatformClient();
        return toAjax(productClassService.update(productClass, SecurityUtils.getUsername()));
    }

    /**
     * 软删除平台产品类目。
     *
     * @param classId 类目主键
     * @return 操作结果
     */
    @RequiresPermissions("platform:class:remove")
    @Log(title = "产品类目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{classId}")
    public AjaxResult delete(@PathVariable Long classId)
    {
        assertPlatformClient();
        return toAjax(productClassService.delete(classId, SecurityUtils.getUsername()));
    }

    /**
     * 校验当前请求来自平台客户端，阻止商户令牌越权管理平台类目主数据。
     */
    private void assertPlatformClient()
    {
        if (!ClientContextHolder.isPlatformClient())
        {
            throw new ServiceException("商户端不允许访问平台产品类目管理接口");
        }
    }
}
