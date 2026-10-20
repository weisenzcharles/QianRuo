package com.ruoyi.rentalbusiness.enums;

/** 商户间转单任务状态，与协作关系表的数值约定保持一致。 */
public enum MerchantAllianceOrderStatus
{
    PENDING(0), ACCEPTED(1), REJECTED(2), CANCELLED(3), EXPIRED(4), PUBLIC_POOL(5),
    IN_FULFILLMENT(6), COMPLETED(7), EXCEPTION(8);

    private final int code;
    MerchantAllianceOrderStatus(int code) { this.code = code; }
    public int getCode() { return code; }
}
