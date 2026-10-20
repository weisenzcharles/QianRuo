package com.ruoyi.system.controller.internal;

import java.util.List;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.system.api.RemoteExpressDictionaryService;
import com.ruoyi.system.api.domain.SysDictData;
import com.ruoyi.system.service.ISysDictTypeService;

/** 平台字典内部只读接口，供租赁业务读取受控配置。 */
@RestController
@RequestMapping(RemoteExpressDictionaryService.INTERNAL_PREFIX)
public class InternalDictionaryController
{
    private static final Set<String> SUPPORTED_DICT_TYPES = Set.of(
            "rental_express_company", "rental_audit_field_label", "rental_condition_level", "rental_vip_level");
    private final ISysDictTypeService dictTypeService;

    public InternalDictionaryController(ISysDictTypeService dictTypeService)
    {
        this.dictTypeService = dictTypeService;
    }

    /** 仅开放租赁业务需要的受控字典，避免内部接口泛化暴露平台字典。 */
    @InnerAuth
    @GetMapping("/{dictType}")
    public R<List<SysDictData>> list(@PathVariable String dictType)
    {
        if (!SUPPORTED_DICT_TYPES.contains(dictType))
        {
            return R.fail("不支持读取该字典");
        }
        return R.ok(dictTypeService.selectDictDataByType(dictType));
    }
}
