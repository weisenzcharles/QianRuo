package com.ruoyi.rentalbusiness.controller;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.rentalbusiness.enums.MerchantInventoryConditionLevel;
import com.ruoyi.rentalbusiness.model.MerchantDictionaryOption;
import com.ruoyi.system.api.RemoteExpressDictionaryService;
import com.ruoyi.system.api.domain.SysDictData;

/** 商户端设备成色选项接口。字典维护在平台端，商户端只接收最小选项数据。 */
@RestController
@RequestMapping("/merchant/inventory")
public class MerchantInventoryConditionController extends BaseController
{
    private static final String DICT_TYPE = "rental_condition_level";
    private static final Logger log = LoggerFactory.getLogger(MerchantInventoryConditionController.class);

    private final RemoteExpressDictionaryService dictionaryService;

    public MerchantInventoryConditionController(RemoteExpressDictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /** 查询启用的设备成色等级。 */
    @RequiresPermissions("merchant:inventory:edit")
    @GetMapping("/condition-levels")
    public AjaxResult list()
    {
        R<List<SysDictData>> response = dictionaryService.list(DICT_TYPE, SecurityConstants.INNER);
        if (response == null || !R.isSuccess(response) || response.getData() == null)
        {
            return error("设备成色配置暂不可用");
        }

        List<MerchantDictionaryOption> options = new ArrayList<MerchantDictionaryOption>();
        for (SysDictData item : response.getData())
        {
            if (item == null || (item.getStatus() != null && item.getStatus() != 0)
                    || item.getDictLabel() == null || item.getDictLabel().isBlank()
                    || item.getDictValue() == null || item.getDictValue().isBlank())
            {
                continue;
            }
            try
            {
                Integer value = Integer.valueOf(item.getDictValue().trim());
                if (MerchantInventoryConditionLevel.isSupported(value))
                {
                    options.add(new MerchantDictionaryOption(item.getDictLabel().trim(), value));
                }
            }
            catch (NumberFormatException ignored)
            {
                log.warn("设备成色字典值不是整数，label={}", item.getDictLabel());
            }
        }
        if (options.isEmpty())
        {
            return error("设备成色配置暂不可用");
        }
        return success(options);
    }
}
