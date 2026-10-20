package com.ruoyi.system.model.rental;

/** 平台客服工单可选处理人，避免向页面返回用户敏感字段。 */
public class PlatformTicketAssigneeVO
{
    private Long userId;
    private String userName;
    private String nickName;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }
}
