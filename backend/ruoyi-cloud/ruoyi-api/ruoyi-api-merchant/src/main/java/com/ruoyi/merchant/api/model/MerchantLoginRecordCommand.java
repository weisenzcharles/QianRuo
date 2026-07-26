package com.ruoyi.merchant.api.model;

import java.io.Serializable;

/** 商户登录记录命令。 */
public class MerchantLoginRecordCommand implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String loginIp;
    private Long loginAt;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getLoginIp() { return loginIp; }
    public void setLoginIp(String loginIp) { this.loginIp = loginIp; }
    public Long getLoginAt() { return loginAt; }
    public void setLoginAt(Long loginAt) { this.loginAt = loginAt; }
}
