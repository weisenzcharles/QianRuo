package com.ruoyi.merchant.model;

/** 商户个人资料修改命令。归属字段只从登录态取得。 */
public class MerchantProfileUpdateCommand
{
    private String nickName;
    private String phonenumber;
    private String email;

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }
    public String getPhonenumber() { return phonenumber; }
    public void setPhonenumber(String phonenumber) { this.phonenumber = phonenumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
