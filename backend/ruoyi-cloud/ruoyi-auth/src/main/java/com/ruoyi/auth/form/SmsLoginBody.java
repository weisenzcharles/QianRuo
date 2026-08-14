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

    /** 图形验证码文本，由网关统一校验。 */
    private String captchaCode;

    /** 图形验证码唯一标识，由网关统一校验。 */
    private String uuid;

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

    /** 获取图形验证码文本。 */
    public String getCaptchaCode()
    {
        return captchaCode;
    }

    /** 设置图形验证码文本。 */
    public void setCaptchaCode(String captchaCode)
    {
        this.captchaCode = captchaCode;
    }

    /** 获取图形验证码唯一标识。 */
    public String getUuid()
    {
        return uuid;
    }

    /** 设置图形验证码唯一标识。 */
    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }
}
