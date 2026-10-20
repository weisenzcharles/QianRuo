package com.ruoyi.rentalbusiness.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.system.api.RemoteExpressDictionaryService;
import com.ruoyi.system.api.domain.SysDictData;

/** 商户端读取平台快递公司配置的接口。 */
@RestController
@RequestMapping("/merchant/express-companies")
public class MerchantExpressDictionaryController extends BaseController
{
    private final RemoteExpressDictionaryService dictionaryService;

    public MerchantExpressDictionaryController(RemoteExpressDictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /** 返回启用的快递公司及字典值中配置的单号正则。 */
    @GetMapping
    public AjaxResult list()
    {
        R<List<SysDictData>> result = dictionaryService.list("rental_express_company", SecurityConstants.INNER);
        if (result == null || result.getCode() != 200)
        {
            return error("快递公司配置暂不可用");
        }
        return success(result.getData());
    }
}
