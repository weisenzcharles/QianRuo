package com.ruoyi.merchant.model;

/** 提现渠道枚举。 */
public enum MerchantWithdrawalChannel
{
    ALIPAY("ALIPAY", "支付宝"), BANK_TRANSFER("BANK_TRANSFER", "银行转账");

    private final String code;
    private final String label;

    MerchantWithdrawalChannel(String code, String label)
    {
        this.code = code;
        this.label = label;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }

    public static MerchantWithdrawalChannel fromCode(String code)
    {
        if (code == null) return null;
        for (MerchantWithdrawalChannel item : values()) if (item.code.equalsIgnoreCase(code)) return item;
        return null;
    }
}
