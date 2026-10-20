package com.ruoyi.merchant.api.model;

import java.io.Serializable;

/**
 * 商户用户的最小展示摘要。
 *
 * <p>内部业务服务只需要展示操作人，不应依赖包含密码等认证字段的登录身份对象。</p>
 */
public class MerchantUserSummary implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String nickname;

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getNickname()
    {
        return nickname;
    }

    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }
}
