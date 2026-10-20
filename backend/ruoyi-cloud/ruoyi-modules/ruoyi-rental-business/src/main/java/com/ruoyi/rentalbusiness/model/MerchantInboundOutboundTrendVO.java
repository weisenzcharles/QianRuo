package com.ruoyi.rentalbusiness.model;

import java.util.ArrayList;
import java.util.List;

/** 商户收发趋势汇总响应。 */
public class MerchantInboundOutboundTrendVO
{
    public Long pendingShipTotal = 0L;
    public List<MerchantInboundOutboundTrendRowVO> rows = new ArrayList<MerchantInboundOutboundTrendRowVO>();
}
