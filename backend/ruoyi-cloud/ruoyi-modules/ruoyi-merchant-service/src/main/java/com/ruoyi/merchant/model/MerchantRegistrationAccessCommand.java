package com.ruoyi.merchant.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** 商户入驻申请的手机号验证码凭证。 */
public class MerchantRegistrationAccessCommand
{
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{4}$", message = "请输入四位数字验证码")
    private String code;

    /** @return 商户提交申请时使用的手机号 */
    public String getPhone() { return phone; }

    /** @param phone 商户提交申请时使用的手机号 */
    public void setPhone(String phone) { this.phone = phone; }

    /** @return 用于查询申请的四位验证码 */
    public String getCode() { return code; }

    /** @param code 用于查询申请的四位验证码 */
    public void setCode(String code) { this.code = code; }
}
