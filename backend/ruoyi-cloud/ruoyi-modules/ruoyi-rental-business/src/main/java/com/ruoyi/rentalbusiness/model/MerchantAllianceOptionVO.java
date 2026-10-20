package com.ruoyi.rentalbusiness.model;

/** 商户端转单目标选项，不包含接收商户敏感信息。 */
public class MerchantAllianceOptionVO
{
    public Long memberId;
    public String merchantName;

    public MerchantAllianceOptionVO() { }

    public MerchantAllianceOptionVO(Long memberId, String merchantName)
    {
        this.memberId = memberId;
        this.merchantName = merchantName;
    }
}
