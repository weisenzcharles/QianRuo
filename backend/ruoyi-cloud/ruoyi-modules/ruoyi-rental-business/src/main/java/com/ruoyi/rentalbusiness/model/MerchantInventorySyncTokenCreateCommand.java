package com.ruoyi.rentalbusiness.model;

import java.time.LocalDateTime;
import java.util.List;

/** 创建商户外部库存同步令牌命令。原始令牌只在创建响应中返回一次。 */
public class MerchantInventorySyncTokenCreateCommand
{
    public String tokenName;
    public List<String> scopes;
    /** 正式外部订单创建成功后的回调地址。启用订单创建授权时必填。 */
    public String callbackUrl;
    public LocalDateTime expireTime;
}
