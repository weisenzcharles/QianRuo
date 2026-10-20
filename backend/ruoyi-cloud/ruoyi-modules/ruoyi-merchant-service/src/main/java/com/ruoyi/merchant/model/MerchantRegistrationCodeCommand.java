package com.ruoyi.merchant.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** 获取商户入驻短信验证码的请求。 */
public class MerchantRegistrationCodeCommand
{
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** @return 需要获取入驻验证码的手机号 */
    public String getPhone() { return phone; }

    /** @param phone 需要获取入驻验证码的手机号 */
    public void setPhone(String phone) { this.phone = phone; }
}
