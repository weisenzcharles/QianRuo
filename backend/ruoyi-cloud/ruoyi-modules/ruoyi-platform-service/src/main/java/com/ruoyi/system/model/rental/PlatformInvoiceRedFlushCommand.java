package com.ruoyi.system.model.rental;

/** 平台发起发票红冲申请参数。 */
public class PlatformInvoiceRedFlushCommand
{
    private String reason;
    private String idempotencyKey;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
