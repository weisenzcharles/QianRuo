package com.ruoyi.auth.form;

/** 商户登录短信验证码发送请求。 */
public class SmsCodeBody
{
    private String phone;
    private String captchaCode;
    private String uuid;

    /** @return 商户手机号。 */
    public String getPhone()
    {
        return phone;
    }

    /** @param phone 商户手机号。 */
    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    /** @return 图形验证码文本。 */
    public String getCaptchaCode()
    {
        return captchaCode;
    }

    /** @param captchaCode 图形验证码文本。 */
    public void setCaptchaCode(String captchaCode)
    {
        this.captchaCode = captchaCode;
    }

    /** @return 图形验证码标识。 */
    public String getUuid()
    {
        return uuid;
    }

    /** @param uuid 图形验证码标识。 */
    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }
}
