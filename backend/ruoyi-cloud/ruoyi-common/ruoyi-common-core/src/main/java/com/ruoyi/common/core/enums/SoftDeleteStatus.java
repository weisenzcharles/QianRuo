package com.ruoyi.common.core.enums;

/**
 * 统一软删除状态。
 *
 * <p>该枚举只对应数据库 {@code is_delete} 字段，不与账号启停等业务状态混用。</p>
 */
public enum SoftDeleteStatus implements NumericCode
{
    /** 正常数据。 */
    NORMAL(0),

    /** 已软删除数据。 */
    DELETED(1);

    private final Integer code;

    /**
     * 构造软删除状态枚举。
     *
     * @param code 数据库存储编码
     */
    SoftDeleteStatus(Integer code)
    {
        this.code = code;
    }

    /**
     * 获取数据库软删除状态编码。
     *
     * @return 软删除状态编码
     */
    public Integer getCode()
    {
        return code;
    }

    @Override
    public String getLabel()
    {
        return this == NORMAL ? "正常" : "已删除";
    }
}
