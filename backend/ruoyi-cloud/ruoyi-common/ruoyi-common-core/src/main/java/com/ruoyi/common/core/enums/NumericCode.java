package com.ruoyi.common.core.enums;

/**
 * 数字编码枚举的统一契约。
 *
 * <p>数据库只保存稳定的数字编码，业务代码通过枚举表达语义，避免将状态、类型和标志位
 * 作为裸字符串分散在 Controller、Service、Mapper 与前端之间。</p>
 */
public interface NumericCode
{
    /**
     * 获取持久化数字编码。
     *
     * @return 数据库存储编码
     */
    Integer getCode();

    /**
     * 获取面向用户的中文名称。
     *
     * @return 显示名称
     */
    String getLabel();
}
