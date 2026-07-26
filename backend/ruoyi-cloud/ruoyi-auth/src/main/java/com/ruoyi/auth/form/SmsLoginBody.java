package com.ruoyi.auth.form;

/**
 * 手机验证码登录对象
 *
 * @author qianruo
 */
public class SmsLoginBody
{
    /**
     * 手机号
     */
    private String phone;

    /**
     * 短信验证码
     */
    private String code;

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }
}
