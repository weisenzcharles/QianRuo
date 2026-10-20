package com.ruoyi.merchant.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.merchant.application.MerchantKnowledgeService;
import com.ruoyi.merchant.model.MerchantKnowledgeArticleCommand;
import com.ruoyi.merchant.model.MerchantKnowledgeArticleQuery;
import com.ruoyi.merchant.model.MerchantKnowledgeCategoryCommand;

/** 商户私有知识库接口。 */
@RestController
@RequestMapping("/merchant/knowledge")
public class MerchantKnowledgeController extends BaseController
{
    private final MerchantKnowledgeService service;

    public MerchantKnowledgeController(MerchantKnowledgeService service)
    {
        this.service = service;
    }

    /** 查询平台公共和当前商户知识分类。 */
    @RequiresPermissions("merchant:knowledge:list")
    @GetMapping("/categories")
    public AjaxResult categories()
    {
        return success(service.categories());
    }

    /** 查询可用于文章受众范围的员工组。 */
    @RequiresPermissions("merchant:knowledge:list")
    @GetMapping("/roles")
    public AjaxResult roles()
    {
        return success(service.roleOptions());
    }

    /** 新增商户知识分类。 */
    @RequiresPermissions("merchant:knowledge:add")
    @Log(title = "商户知识分类", businessType = BusinessType.INSERT)
    @PostMapping("/categories")
    public AjaxResult createCategory(@RequestBody MerchantKnowledgeCategoryCommand command)
    {
        return success(service.createCategory(command));
    }

    /** 修改商户知识分类。 */
    @RequiresPermissions("merchant:knowledge:edit")
    @Log(title = "商户知识分类", businessType = BusinessType.UPDATE)
    @PutMapping("/categories/{categoryId}")
    public AjaxResult updateCategory(@PathVariable Long categoryId, @RequestBody MerchantKnowledgeCategoryCommand command)
    {
        command.setCategoryId(categoryId);
        return success(service.updateCategory(command));
    }

    /** 删除商户知识分类。 */
    @RequiresPermissions("merchant:knowledge:remove")
    @Log(title = "商户知识分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/categories/{categoryId}")
    public AjaxResult deleteCategory(@PathVariable Long categoryId)
    {
        service.deleteCategory(categoryId);
        return success();
    }

    /** 分页查询商户自己的文章和已发布的平台文章。 */
    @RequiresPermissions("merchant:knowledge:list")
    @GetMapping("/articles")
    public TableDataInfo list(MerchantKnowledgeArticleQuery query)
    {
        startPage();
        return getDataTable(service.list(query));
    }

    /** 查询知识文章详情和版本历史。 */
    @RequiresPermissions("merchant:knowledge:list")
    @GetMapping("/articles/{articleId}")
    public AjaxResult detail(@PathVariable Long articleId)
    {
        return success(service.detail(articleId));
    }

    /** 新增商户知识文章。 */
    @RequiresPermissions("merchant:knowledge:add")
    @Log(title = "商户知识文章", businessType = BusinessType.INSERT)
    @PostMapping("/articles")
    public AjaxResult create(@RequestBody MerchantKnowledgeArticleCommand command)
    {
        return success(service.create(command));
    }

    /** 修改商户知识文章并追加版本。 */
    @RequiresPermissions("merchant:knowledge:edit")
    @Log(title = "商户知识文章", businessType = BusinessType.UPDATE)
    @PutMapping("/articles/{articleId}")
    public AjaxResult update(@PathVariable Long articleId, @RequestBody MerchantKnowledgeArticleCommand command)
    {
        command.setArticleId(articleId);
        return success(service.update(command));
    }

    /** 发布商户知识文章。 */
    @RequiresPermissions("merchant:knowledge:edit")
    @Log(title = "商户知识文章", businessType = BusinessType.UPDATE)
    @PostMapping("/articles/{articleId}/publish")
    public AjaxResult publish(@PathVariable Long articleId)
    {
        return success(service.publish(articleId));
    }

    /** 下架商户知识文章。 */
    @RequiresPermissions("merchant:knowledge:edit")
    @Log(title = "商户知识文章", businessType = BusinessType.UPDATE)
    @PostMapping("/articles/{articleId}/offline")
    public AjaxResult offline(@PathVariable Long articleId)
    {
        return success(service.offline(articleId));
    }

    /** 删除商户知识文章。 */
    @RequiresPermissions("merchant:knowledge:remove")
    @Log(title = "商户知识文章", businessType = BusinessType.DELETE)
    @DeleteMapping("/articles/{articleId}")
    public AjaxResult delete(@PathVariable Long articleId)
    {
        service.delete(articleId);
        return success();
    }

    /** 记录知识文章阅读。 */
    @RequiresPermissions("merchant:knowledge:read")
    @PostMapping("/articles/{articleId}/read")
    public AjaxResult markRead(@PathVariable Long articleId)
    {
        service.markRead(articleId);
        return success();
    }
}
