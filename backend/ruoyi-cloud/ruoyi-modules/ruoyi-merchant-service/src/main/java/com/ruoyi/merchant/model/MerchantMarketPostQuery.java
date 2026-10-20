package com.ruoyi.merchant.model;

import com.ruoyi.common.core.web.domain.BaseEntity;

/** 商户周转市场帖子列表查询条件。归属范围由当前商户登录态确定。 */
public class MerchantMarketPostQuery extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private String keyword;
    private MerchantMarketBoardType boardType;
    private MerchantMarketPostStatus postStatus;
    private String city;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public MerchantMarketBoardType getBoardType() { return boardType; }
    public void setBoardType(MerchantMarketBoardType boardType) { this.boardType = boardType; }
    public MerchantMarketPostStatus getPostStatus() { return postStatus; }
    public void setPostStatus(MerchantMarketPostStatus postStatus) { this.postStatus = postStatus; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}
