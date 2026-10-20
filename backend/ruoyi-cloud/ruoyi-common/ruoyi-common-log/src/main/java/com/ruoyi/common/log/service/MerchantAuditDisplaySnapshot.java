package com.ruoyi.common.log.service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.utils.StringUtils;

/**
 * 商户审计日志展示快照处理器。
 *
 * <p>原始快照用于内部追溯，展示快照只保留业务字段并移除主键、外键、ID 集合和内部版本号。
 * 这样即使某个业务写入入口尚未提供名称解析，也不会把技术编号返回给商户端。</p>
 */
public final class MerchantAuditDisplaySnapshot
{
    private static final String HIDDEN_TEXT = "已隐藏";

    private MerchantAuditDisplaySnapshot()
    {
    }

    /**
     * 将 JSON 快照转换为不包含技术编号的展示快照。
     *
     * @param json 原始 JSON 快照
     * @return 可供商户页面展示的 JSON；空值返回空字符串
     */
    public static String sanitizeJson(String json)
    {
        if (StringUtils.isEmpty(json))
        {
            return "";
        }
        try
        {
            Object parsed = JSON.parse(json);
            if (parsed == null)
            {
                return "";
            }
            if (!(parsed instanceof Map<?, ?>) && !(parsed instanceof Collection<?>))
            {
                return JSON.toJSONString(singleValue(parsed));
            }
            return JSON.toJSONString(sanitizeValue(parsed));
        }
        catch (RuntimeException exception)
        {
            return JSON.toJSONString(singleValue("变更内容不可解析"));
        }
    }

    /** 判断字段是否是技术主键、外键或 ID 集合。 */
    public static boolean isTechnicalIdentifier(String fieldName)
    {
        if (StringUtils.isEmpty(fieldName))
        {
            return false;
        }
        return "id".equalsIgnoreCase(fieldName)
                || fieldName.matches(".*(?:_id|Id|ID|_ids|Ids|IDS)$");
    }

    private static Object sanitizeValue(Object value)
    {
        if (value instanceof Map<?, ?> map)
        {
            Map<String, Object> sanitized = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet())
            {
                String fieldName = entry.getKey() == null ? "" : String.valueOf(entry.getKey());
                if (isHiddenDisplayField(fieldName))
                {
                    continue;
                }
                sanitized.put(fieldName, sanitizeValue(entry.getValue()));
            }
            return sanitized;
        }
        if (value instanceof Collection<?> collection)
        {
            java.util.List<Object> sanitized = new java.util.ArrayList<>(collection.size());
            for (Object item : collection)
            {
                sanitized.add(sanitizeValue(item));
            }
            return sanitized;
        }
        if (value instanceof String text)
        {
            String trimmed = text.trim();
            if ((trimmed.startsWith("{") && trimmed.endsWith("}"))
                    || (trimmed.startsWith("[") && trimmed.endsWith("]")))
            {
                try
                {
                    Object nested = JSON.parse(trimmed);
                    if (nested instanceof Map<?, ?> || nested instanceof Collection<?>)
                    {
                        return sanitizeValue(nested);
                    }
                }
                catch (RuntimeException exception)
                {
                    // 普通文本可能恰好以括号开头，解析失败时按原文保留。
                    return text;
                }
            }
            return text;
        }
        return value;
    }

    /** 版本号只用于乐观锁并发控制，不属于商户可读的业务变更内容。 */
    private static boolean isHiddenDisplayField(String fieldName)
    {
        return isTechnicalIdentifier(fieldName) || "version".equalsIgnoreCase(fieldName);
    }

    private static Map<String, String> singleValue(Object value)
    {
        Map<String, String> result = new LinkedHashMap<>();
        String text = value == null ? "" : String.valueOf(value);
        result.put("内容", isNumeric(text) ? HIDDEN_TEXT : text);
        return result;
    }

    private static boolean isNumeric(String value)
    {
        return value != null && value.matches("[-+]?\\d+(?:\\.\\d+)?");
    }
}
