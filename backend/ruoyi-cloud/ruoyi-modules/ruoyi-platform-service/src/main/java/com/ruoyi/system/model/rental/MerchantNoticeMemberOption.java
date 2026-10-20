package com.ruoyi.system.model.rental;

/** 平台商户公告指定接收商户的下拉选项。 */
public class MerchantNoticeMemberOption
{
    private Long memberId;
    private String merchantName;
    private String memberCode;

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    public String getMerchantName()
    {
        return merchantName;
    }

    public void setMerchantName(String merchantName)
    {
        this.merchantName = merchantName;
    }

    public String getMemberCode()
    {
        return memberCode;
    }

    public void setMemberCode(String memberCode)
    {
        this.memberCode = memberCode;
    }
}
