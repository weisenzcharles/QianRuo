package com.ruoyi.merchant.application;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.utils.DictUtils;
import com.ruoyi.system.api.RemoteExpressDictionaryService;
import com.ruoyi.system.api.domain.SysDictData;

/**
 * 商户操作日志字段名称服务。
 *
 * <p>字段名称是平台统一维护的字典数据。读取时先使用平台和商户服务共享的
 * {@code sys_dict:rental_audit_field_label} Redis 缓存，缓存未命中才通过内部
 * Feign 接口读取平台字典，避免商户服务直接访问平台数据库。</p>
 */
@Service
public class MerchantAuditFieldLabelService
{
    /** 平台字典中保存审计字段中文名称的字典类型。 */
    public static final String DICT_TYPE = "rental_audit_field_label";
    private static final Set<String> REQUIRED_LABEL_KEYS = Set.of(
            "phoneDisplay", "emailDisplay", "warehouseNames");

    private final RemoteExpressDictionaryService dictionaryService;

    /** 注入平台字典内部契约。 */
    public MerchantAuditFieldLabelService(RemoteExpressDictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * 获取当前启用的审计字段中文名称映射。
     *
     * @return 字段键值到中文名称的有序映射
     */
    public Map<String, String> getFieldLabels()
    {
        List<SysDictData> dictData = DictUtils.getDictCache(DICT_TYPE);
        if (dictData == null || dictData.isEmpty() || !containsRequiredLabels(dictData))
        {
            R<List<SysDictData>> response = dictionaryService.list(DICT_TYPE, SecurityConstants.INNER);
            if (response != null && R.isSuccess(response) && response.getData() != null
                    && !response.getData().isEmpty())
            {
                dictData = response.getData();
                DictUtils.setDictCache(DICT_TYPE, dictData);
            }
        }
        if (dictData == null || dictData.isEmpty())
        {
            return Collections.emptyMap();
        }
        return toLabelMap(dictData);
    }

    static boolean containsRequiredLabels(List<SysDictData> dictData)
    {
        Set<String> keys = new HashSet<String>();
        for (SysDictData item : dictData)
        {
            if (item != null && item.getDictValue() != null)
            {
                keys.add(item.getDictValue());
            }
        }
        return keys.containsAll(REQUIRED_LABEL_KEYS);
    }

    /** 将字典实体压缩成商户页面实际需要的键值映射，避免透传系统字段。 */
    private Map<String, String> toLabelMap(List<SysDictData> dictData)
    {
        Map<String, String> labels = new LinkedHashMap<>();
        for (SysDictData item : dictData)
        {
            if (item != null && item.getDictValue() != null && !item.getDictValue().isBlank()
                    && item.getDictLabel() != null && !item.getDictLabel().isBlank())
            {
                labels.put(item.getDictValue(), normalizeLabel(item.getDictValue(), item.getDictLabel()));
            }
        }
        // 旧缓存可能没有 returnWarehouseName，补齐两个订单仓库字段的稳定语义。
        labels.put("warehouseName", "发货仓库");
        labels.put("returnWarehouseName", "归还仓库");
        return labels;
    }

    /**
     * 仓库字段在订单审计中有明确业务语义，不能沿用通用的“仓库名称”标签。
     * 这里保留字典作为统一来源，同时兼容字典迁移完成前仍存在的旧 Redis 缓存。
     */
    private String normalizeLabel(String fieldKey, String label)
    {
        if ("warehouseName".equals(fieldKey))
        {
            return "发货仓库";
        }
        if ("returnWarehouseName".equals(fieldKey))
        {
            return "归还仓库";
        }
        return label;
    }
}
