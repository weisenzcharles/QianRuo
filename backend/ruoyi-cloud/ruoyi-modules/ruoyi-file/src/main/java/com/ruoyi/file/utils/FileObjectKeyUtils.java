package com.ruoyi.file.utils;

import com.ruoyi.common.core.enums.FileBusinessType;

/** 构造统一的 OSS 对象键，保证不同业务文件使用独立目录。 */
public final class FileObjectKeyUtils
{
    private FileObjectKeyUtils()
    {
    }

    /**
     * 为文件名添加业务目录。
     *
     * @param businessType 业务类型，为空时归入通用目录
     * @param fileName 已完成时间戳命名的文件名
     * @param objectPrefix OSS 配置的对象根目录
     * @return OSS 对象键
     */
    public static String build(String businessType, String fileName, String objectPrefix)
    {
        FileBusinessType type = isBlank(businessType)
                ? FileBusinessType.COMMON
                : FileBusinessType.fromCode(businessType);
        String prefix = normalizeSegment(objectPrefix);
        String directory = type.getCode();
        String normalizedFileName = normalizeFileName(fileName);
        return prefix.isEmpty()
                ? directory + "/" + normalizedFileName
                : prefix + "/" + directory + "/" + normalizedFileName;
    }

    private static String normalizeSegment(String value)
    {
        if (isBlank(value))
        {
            return "";
        }
        String normalized = value.trim().replaceAll("^/+|/+$", "");
        if (!normalized.matches("[a-zA-Z0-9][a-zA-Z0-9/_-]*"))
        {
            throw new IllegalArgumentException("OSS 对象根目录格式不正确");
        }
        return normalized;
    }

    private static String normalizeFileName(String value)
    {
        if (isBlank(value) || value.startsWith("/") || value.contains("\\") || value.contains(".."))
        {
            throw new IllegalArgumentException("OSS 文件名格式不正确");
        }
        return value;
    }

    private static boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }
}
