package com.ruoyi.system.controller.rental;

import java.util.List;
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
import com.ruoyi.system.model.rental.PlatformKnowledgeArticleCommand;
import com.ruoyi.system.model.rental.PlatformKnowledgeQuery;
import com.ruoyi.system.model.rental.PlatformKnowledgeViolationCommand;
import com.ruoyi.system.model.rental.PlatformRecycleQuery;
import com.ruoyi.system.model.rental.PlatformTicketCreateCommand;
import com.ruoyi.system.model.rental.PlatformTicketEventCommand;
import com.ruoyi.system.model.rental.PlatformTicketQuery;
import com.ruoyi.system.service.rental.PlatformGovernanceService;

/** 平台工单、知识库和回收站治理接口。 */
@RestController
public class PlatformGovernanceController extends BaseController
{
    private final PlatformGovernanceService service;

    public PlatformGovernanceController(PlatformGovernanceService service)
    {
        this.service = service;
    }

    /** 分页查询平台统一客服工单。 */
    @RequiresPermissions("platform:ticket:list")
    @GetMapping("/rental/ticket/list")
    public TableDataInfo ticketList(PlatformTicketQuery query)
    {
        assertPlatformClient();
        startPage();
        return getDataTable(service.tickets(query));
    }

    /** 查询工单详情及追加时间线。 */
    @RequiresPermissions("platform:ticket:list")
    @GetMapping("/rental/ticket/{ticketId}")
    public AjaxResult ticket(@PathVariable Long ticketId)
    {
        assertPlatformClient();
        return success(service.ticket(ticketId));
    }

    /** 查询可分配平台客服工单的启用用户。 */
    @RequiresPermissions("platform:ticket:process")
    @GetMapping("/rental/ticket/assignees")
    public AjaxResult ticketAssignees()
    {
        assertPlatformClient();
        return success(service.ticketAssignees());
    }

    /** 创建平台客服工单。 */
    @RequiresPermissions("platform:ticket:add")
    @Log(title = "平台客服工单", businessType = BusinessType.INSERT)
    @PostMapping("/rental/ticket")
    public AjaxResult createTicket(@Validated @RequestBody PlatformTicketCreateCommand command)
    {
        assertPlatformClient();
        return success(service.createTicket(command));
    }

    /** 追加工单事件、分配处理人或变更状态。 */
    @RequiresPermissions("platform:ticket:process")
    @Log(title = "平台客服工单处理", businessType = BusinessType.UPDATE)
    @PostMapping("/rental/ticket/{ticketId}/events")
    public AjaxResult appendTicketEvent(@PathVariable Long ticketId,
            @Validated @RequestBody PlatformTicketEventCommand command)
    {
        assertPlatformClient();
        service.appendTicketEvent(ticketId, command);
        return success();
    }

    /** 查询知识库分类。 */
    @RequiresPermissions("platform:knowledge:list")
    @GetMapping("/rental/knowledge/categories")
    public AjaxResult knowledgeCategories()
    {
        assertPlatformClient();
        return success(service.knowledgeCategories());
    }

    /** 分页查询平台知识文章。 */
    @RequiresPermissions("platform:knowledge:list")
    @GetMapping("/rental/knowledge/articles")
    public TableDataInfo knowledgeArticles(PlatformKnowledgeQuery query)
    {
        assertPlatformClient();
        startPage();
        return getDataTable(service.knowledgeArticles(query));
    }

    /** 分页查询商户知识合规文章，仅返回合规元数据。 */
    @RequiresPermissions("platform:knowledge:compliance:list")
    @GetMapping("/rental/knowledge/compliance/articles")
    public TableDataInfo knowledgeComplianceArticles(PlatformKnowledgeQuery query)
    {
        assertPlatformClient();
        startPage();
        return getDataTable(service.knowledgeComplianceArticles(query));
    }

    /** 查询知识文章详情和历史版本。 */
    @RequiresPermissions("platform:knowledge:list")
    @GetMapping("/rental/knowledge/articles/{articleId}")
    public AjaxResult knowledgeArticle(@PathVariable Long articleId)
    {
        assertPlatformClient();
        return success(service.knowledgeArticle(articleId));
    }

    /** 查询商户知识合规详情，只有专用合规查看权限可以读取正文。 */
    @RequiresPermissions("platform:knowledge:compliance:view")
    @GetMapping("/rental/knowledge/compliance/articles/{articleId}")
    public AjaxResult knowledgeComplianceArticle(@PathVariable Long articleId)
    {
        assertPlatformClient();
        return success(service.knowledgeComplianceArticle(articleId));
    }

    /** 新增平台知识文章。 */
    @RequiresPermissions("platform:knowledge:add")
    @Log(title = "平台知识库", businessType = BusinessType.INSERT)
    @PostMapping("/rental/knowledge/articles")
    public AjaxResult createKnowledge(@Validated @RequestBody PlatformKnowledgeArticleCommand command)
    {
        assertPlatformClient();
        return success(service.createKnowledge(command));
    }

    /** 修改平台知识文章并生成新版本。 */
    @RequiresPermissions("platform:knowledge:edit")
    @Log(title = "平台知识库", businessType = BusinessType.UPDATE)
    @PutMapping("/rental/knowledge/articles")
    public AjaxResult updateKnowledge(@Validated @RequestBody PlatformKnowledgeArticleCommand command)
    {
        assertPlatformClient();
        return success(service.updateKnowledge(command));
    }

    /** 发布知识文章。 */
    @RequiresPermissions("platform:knowledge:publish")
    @Log(title = "发布平台知识", businessType = BusinessType.UPDATE)
    @PostMapping("/rental/knowledge/articles/{articleId}/publish")
    public AjaxResult publishKnowledge(@PathVariable Long articleId)
    {
        assertPlatformClient();
        service.publishKnowledge(articleId);
        return success();
    }

    /** 下架知识文章。 */
    @RequiresPermissions("platform:knowledge:publish")
    @Log(title = "下架平台知识", businessType = BusinessType.UPDATE)
    @PostMapping("/rental/knowledge/articles/{articleId}/offline")
    public AjaxResult offlineKnowledge(@PathVariable Long articleId)
    {
        assertPlatformClient();
        service.offlineKnowledge(articleId);
        return success();
    }

    /** 记录违规原因并下架商户知识文章，不允许修改或删除商户原文。 */
    @RequiresPermissions("platform:knowledge:compliance:offline")
    @Log(title = "商户知识违规下架", businessType = BusinessType.UPDATE)
    @PostMapping("/rental/knowledge/compliance/articles/{articleId}/offline")
    public AjaxResult offlineMerchantKnowledge(@PathVariable Long articleId,
            @Validated @RequestBody PlatformKnowledgeViolationCommand command)
    {
        assertPlatformClient();
        service.offlineMerchantKnowledge(articleId, command);
        return success();
    }

    /** 删除平台知识文章并进入 30 天回收站。 */
    @RequiresPermissions("platform:knowledge:remove")
    @Log(title = "删除平台知识", businessType = BusinessType.DELETE)
    @DeleteMapping("/rental/knowledge/articles/{articleId}")
    public AjaxResult deleteKnowledge(@PathVariable Long articleId)
    {
        assertPlatformClient();
        service.deleteKnowledge(articleId);
        return success();
    }

    /** 查询平台回收站记录。 */
    @RequiresPermissions("platform:recycle:list")
    @GetMapping("/rental/recycle/list")
    public TableDataInfo recycleList(PlatformRecycleQuery query)
    {
        assertPlatformClient();
        startPage();
        return getDataTable(service.recycleRecords(query));
    }

    /** 恢复已接入恢复处理器的普通业务数据。 */
    @RequiresPermissions("platform:recycle:restore")
    @Log(title = "平台回收站恢复", businessType = BusinessType.UPDATE)
    @PostMapping("/rental/recycle/{recycleId}/restore")
    public AjaxResult restoreRecycle(@PathVariable Long recycleId)
    {
        assertPlatformClient();
        service.restoreRecycle(recycleId);
        return success();
    }

    private void assertPlatformClient()
    {
        if (!ClientContextHolder.isPlatformClient())
            throw new ServiceException("商户端不允许访问平台治理接口");
    }
}
