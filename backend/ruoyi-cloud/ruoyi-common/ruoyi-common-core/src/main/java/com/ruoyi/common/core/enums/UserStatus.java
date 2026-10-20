package com.ruoyi.common.core.enums;

/**
 * 用户状态
 * 
 * @author ruoyi
 */
public enum UserStatus implements NumericCode
{
    OK(0, "正常"), DISABLE(1, "停用");

    private final Integer code;
    private final String info;

    /**
     * 构造账号启停状态枚举。
     *
     * @param code 状态编码
     * @param info 状态说明
     */
    UserStatus(Integer code, String info)
    {
        this.code = code;
        this.info = info;
    }

    /**
     * 获取账号状态编码。
     *
     * @return 状态编码
     */
    public Integer getCode()
    {
        return code;
    }

    /**
     * 获取账号状态说明。
     *
     * @return 状态说明
     */
    public String getInfo()
    {
        return info;
    }

    @Override
    public String getLabel()
    {
        return info;
    }
}
