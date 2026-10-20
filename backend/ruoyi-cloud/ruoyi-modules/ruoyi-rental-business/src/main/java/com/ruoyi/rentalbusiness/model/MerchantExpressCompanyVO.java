package com.ruoyi.rentalbusiness.model;

/** 商户端可选快递公司及其单号校验规则。 */
public class MerchantExpressCompanyVO
{
    private String code;
    private String name;
    private String pattern;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }
}
