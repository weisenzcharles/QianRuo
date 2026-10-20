package com.ruoyi.common.core.enums;

import java.util.Arrays;

/**
 * 文件对象的业务目录。目录由服务端白名单控制，避免客户端把任意路径写入 OSS。
 */
public enum FileBusinessType
{
    COMMON("common"),
    MERCHANT("merchant"),
    MERCHANT_REGISTRATION("merchant-registration"),
    MERCHANT_USER("merchant-user"),
    PLATFORM_USER("platform-user"),
    ORDER("order"),
    PRODUCT("product"),
    PACKAGE("package"),
    INVENTORY("inventory"),
    LOGISTICS("logistics"),
    AFTER_SALES("after-sales");

    private final String code;

    FileBusinessType(String code)
    {
        this.code = code;
    }

    public String getCode()
    {
        return code;
    }

    /**
     * 将请求中的业务目录解析为白名单枚举。
     *
     * @param code 业务目录编码
     * @return 对应业务类型
     * @throws IllegalArgumentException 业务目录未登记
     */
    public static FileBusinessType fromCode(String code)
    {
        String normalized = code == null ? "" : code.trim();
        return Arrays.stream(values())
                .filter(item -> item.code.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("不支持的文件业务类型: " + code));
    }
}
