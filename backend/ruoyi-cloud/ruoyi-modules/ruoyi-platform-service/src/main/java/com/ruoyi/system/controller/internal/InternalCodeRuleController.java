package com.ruoyi.system.controller.internal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.system.api.RemoteCodeRuleService;
import com.ruoyi.system.service.SysCodeRuleService;

/** 统一编码分配器的内部服务入口。 */
@RestController
@RequestMapping(RemoteCodeRuleService.INTERNAL_PREFIX)
public class InternalCodeRuleController
{
    private final SysCodeRuleService codeRuleService;

    /**
     * 注入统一编码应用服务。
     *
     * @param codeRuleService 统一编码应用服务
     */
    public InternalCodeRuleController(SysCodeRuleService codeRuleService)
    {
        this.codeRuleService = codeRuleService;
    }

    /**
     * 为可信业务服务分配一个全局唯一的下一个编码。
     *
     * @param codeType 编码类型键
     * @return 系统生成的业务编码
     */
    @InnerAuth
    @GetMapping("/{codeType}/next")
    public R<String> nextCode(@PathVariable String codeType)
    {
        return R.ok(codeRuleService.nextCode(codeType));
    }
}
