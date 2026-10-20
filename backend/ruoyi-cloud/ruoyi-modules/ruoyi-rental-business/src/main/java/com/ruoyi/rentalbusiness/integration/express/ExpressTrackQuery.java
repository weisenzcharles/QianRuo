package com.ruoyi.rentalbusiness.integration.express;

/**
 * 快递轨迹查询参数。
 *
 * <p>顺丰接口使用收件方编码，快递100使用快递公司编码和目的地地址；不需要时保持为空即可。</p>
 */
public record ExpressTrackQuery(ExpressProvider provider, String trackingNo,
        String shipperCode, String receiverCode, String destinationAddress)
{
    /** 兼容不需要目的地地址的历史供应商调用。 */
    public ExpressTrackQuery(ExpressProvider provider, String trackingNo,
            String shipperCode, String receiverCode)
    {
        this(provider, trackingNo, shipperCode, receiverCode, null);
    }

    /** 校验并标准化查询参数。 */
    public ExpressTrackQuery normalized()
    {
        if (provider == null)
        {
            throw new IllegalArgumentException("缺少快递轨迹服务供应商");
        }
        if (trackingNo == null || trackingNo.trim().isEmpty())
        {
            throw new IllegalArgumentException("缺少快递单号");
        }
        return new ExpressTrackQuery(provider, trackingNo.trim(), trim(shipperCode), trim(receiverCode),
                trim(destinationAddress));
    }

    private static String trim(String value)
    {
        return value == null ? null : value.trim();
    }
}
