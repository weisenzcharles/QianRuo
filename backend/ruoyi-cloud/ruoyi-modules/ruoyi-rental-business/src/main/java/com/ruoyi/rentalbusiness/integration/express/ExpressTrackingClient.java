package com.ruoyi.rentalbusiness.integration.express;

/** 快递轨迹供应商客户端统一契约。 */
public interface ExpressTrackingClient
{
    /**
     * 查询指定快递单号的轨迹。
     *
     * @param query 快递供应商、单号及可选收寄件编码
     * @return 标准化查询结果；未配置或供应商暂不可用时返回不可用结果
     */
    ExpressTrackResult query(ExpressTrackQuery query);

    /** 返回该客户端支持的供应商。 */
    ExpressProvider provider();
}
