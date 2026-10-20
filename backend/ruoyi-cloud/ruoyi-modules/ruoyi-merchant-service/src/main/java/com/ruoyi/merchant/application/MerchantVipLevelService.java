package com.ruoyi.merchant.application;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.DictUtils;
import com.ruoyi.system.api.RemoteExpressDictionaryService;
import com.ruoyi.system.api.domain.SysDictData;

/**
 * 商户会员等级名称服务。
 *
 * <p>会员等级编码是商户数据中的稳定值，展示名称统一来自平台系统字典。读取时先查共享
 * Redis 字典缓存，缓存未命中才通过平台内部接口获取，避免商户服务直接访问平台库。</p>
 */
@Service
public class MerchantVipLevelService
{
    /** 平台系统字典中的租赁会员等级类型。 */
    public static final String DICT_TYPE = "rental_vip_level";

    private final RemoteExpressDictionaryService dictionaryService;

    public MerchantVipLevelService(RemoteExpressDictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * 将等级编码转换为中文名称。
     *
     * @param levelCode 商户保存的等级编码
     * @param fallbackName 商户等级配置表中的名称，仅在字典暂无映射时作为兼容兜底
     * @return 中文等级名称
     */
    public String resolve(String levelCode, String fallbackName)
    {
        String label = getLabelMap().get(levelCode);
        if (StringUtils.isNotBlank(label))
        {
            return label;
        }
        if (StringUtils.isNotBlank(fallbackName))
        {
            return fallbackName;
        }
        return "未设置";
    }

    /** 将字典数据转换为页面实际需要的编码到名称映射。 */
    static Map<String, String> toLabelMap(List<SysDictData> dictData)
    {
        if (dictData == null || dictData.isEmpty())
        {
            return Collections.emptyMap();
        }
        Map<String, String> labels = new LinkedHashMap<>();
        for (SysDictData item : dictData)
        {
            if (item != null && (item.getStatus() == null || item.getStatus() == 0)
                    && StringUtils.isNotBlank(item.getDictValue())
                    && StringUtils.isNotBlank(item.getDictLabel()))
            {
                labels.put(item.getDictValue(), item.getDictLabel());
            }
        }
        return labels;
    }

    private Map<String, String> getLabelMap()
    {
        List<SysDictData> dictData = DictUtils.getDictCache(DICT_TYPE);
        if (dictData == null || dictData.isEmpty())
        {
            R<List<SysDictData>> response = dictionaryService.list(DICT_TYPE, SecurityConstants.INNER);
            if (response != null && R.isSuccess(response) && response.getData() != null
                    && !response.getData().isEmpty())
            {
                dictData = response.getData();
                DictUtils.setDictCache(DICT_TYPE, dictData);
            }
        }
        return toLabelMap(dictData);
    }
}
