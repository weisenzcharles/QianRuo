package com.ruoyi.merchant.model;

import com.ruoyi.common.core.web.domain.BaseEntity;

/** 当前商户用户查询条件。 */
public class MerchantUserQuery extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    private String userName;
    private String phone;
    private String status;
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
