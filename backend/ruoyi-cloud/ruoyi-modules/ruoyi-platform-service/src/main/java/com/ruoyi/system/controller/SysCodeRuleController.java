package com.ruoyi.system.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.domain.SysCodeRule;
import com.ruoyi.system.service.SysCodeRuleService;

/** 平台统一业务编码规则管理入口。 */
@RestController
@RequestMapping("/code/rule")
public class SysCodeRuleController extends BaseController
{
    private final SysCodeRuleService codeRuleService;

    /**
     * 注入统一编码规则应用服务。
     *
     * @param codeRuleService 统一编码规则应用服务
     */
    public SysCodeRuleController(SysCodeRuleService codeRuleService)
    {
        this.codeRuleService = codeRuleService;
    }

    /**
     * 查询平台全部编码规则。
     *
     * @return 编码规则列表
     */
    @RequiresPermissions("platform:code:list")
    @GetMapping("/list")
    public AjaxResult list()
    {
        List<SysCodeRule> rules = codeRuleService.list();
        return success(rules);
    }

    /**
     * 查询一个编码规则。
     *
     * @param codeType 编码类型键
     * @return 编码规则
     */
    @RequiresPermissions("platform:code:query")
    @GetMapping("/{codeType}")
    public AjaxResult get(@PathVariable String codeType)
    {
        return success(codeRuleService.get(codeType));
    }

    /**
     * 修改编码前缀、总长度和启用状态；当前序号不会被客户端覆盖。
     *
     * @param codeType 编码类型键
     * @param rule 管理端提交的配置
     * @return 更新后的规则
     */
    @RequiresPermissions("platform:code:edit")
    @Log(title = "统一编码规则", businessType = BusinessType.UPDATE)
    @PutMapping("/{codeType}")
    public AjaxResult update(@PathVariable String codeType, @RequestBody SysCodeRule rule)
    {
        return success(codeRuleService.update(codeType, rule, SecurityUtils.getUsername()));
    }
}
