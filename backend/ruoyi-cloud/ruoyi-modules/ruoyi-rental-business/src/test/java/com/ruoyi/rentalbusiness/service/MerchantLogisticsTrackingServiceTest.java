package com.ruoyi.rentalbusiness.service;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.rentalbusiness.integration.express.ExpressProvider;
import com.ruoyi.rentalbusiness.integration.express.ExpressTrackNode;
import com.ruoyi.rentalbusiness.integration.express.ExpressTrackResult;

/** 商户物流供应商路由测试。 */
public class MerchantLogisticsTrackingServiceTest
{
    /** 京东快递应使用快递100的 jd 公司编码，而不是京东开放平台。 */
    @Test
    public void resolveProvider_京东快递_使用快递100()
    {
        Assert.assertEquals(ExpressProvider.KUAIDI100,
                MerchantLogisticsTrackingService.resolveProvider("jd"));
        Assert.assertEquals(ExpressProvider.KUAIDI100,
                MerchantLogisticsTrackingService.resolveProvider("京东快递"));
        Assert.assertEquals("jd",
                MerchantLogisticsTrackingService.resolveKuaidi100ShipperCode("京东物流"));
    }

    /** 顺丰仍保留专用接口，其他公司继续通过快递100查询。 */
    @Test
    public void resolveProvider_顺丰_保留顺丰客户端()
    {
        Assert.assertEquals(ExpressProvider.SF,
                MerchantLogisticsTrackingService.resolveProvider("sf"));
        Assert.assertEquals(ExpressProvider.KUAIDI100,
                MerchantLogisticsTrackingService.resolveProvider("zto"));
        Assert.assertEquals("zto",
                MerchantLogisticsTrackingService.resolveKuaidi100ShipperCode("zto"));
    }

    /** 顺丰主路由不可用时必须切换到快递100，其他已走快递100的公司不重复兜底。 */
    @Test
    public void fallbackProvider_顺丰主路由_使用快递100次路由()
    {
        Assert.assertEquals(ExpressProvider.KUAIDI100,
                MerchantLogisticsTrackingService.fallbackProvider(ExpressProvider.SF));
        Assert.assertNull(MerchantLogisticsTrackingService.fallbackProvider(ExpressProvider.KUAIDI100));
    }

    /** 同一物流记录的外部接口告警必须复用稳定业务键，便于平台幂等更新和自动解除。 */
    @Test
    public void logisticsAlertKey_按物流记录唯一编号稳定生成()
    {
        Assert.assertEquals("LOGISTICS_EXTERNAL_INTERFACE:11",
                MerchantLogisticsTrackingService.logisticsAlertKey(Long.valueOf(11L)));
        Assert.assertNull(MerchantLogisticsTrackingService.logisticsAlertKey(null));
    }

    /** 快递100状态码 3 表示签收，2 表示疑难异常，必须归一化为统一枚举。 */
    @Test
    public void resolveRouteStatus_快递100状态码_映射统一状态()
    {
        ExpressTrackResult delivered = new ExpressTrackResult(true, ExpressProvider.KUAIDI100,
                "JD0260028347946", "3", List.of(), null);
        ExpressTrackResult abnormal = new ExpressTrackResult(true, ExpressProvider.KUAIDI100,
                "JD0260028347946", "2", List.of(), null);

        Assert.assertEquals(RentalCode.LogisticsRouteStatus.DELIVERED,
                MerchantLogisticsTrackingService.resolveRouteStatus(ExpressProvider.KUAIDI100, delivered));
        Assert.assertEquals(RentalCode.LogisticsRouteStatus.ABNORMAL,
                MerchantLogisticsTrackingService.resolveRouteStatus(ExpressProvider.KUAIDI100, abnormal));
    }

    /** 顺丰测试轨迹中的 50 是收件/运输过程，不应被误判为已签收。 */
    @Test
    public void resolveRouteStatus_顺丰运输节点_映射运输中()
    {
        ExpressTrackResult result = new ExpressTrackResult(true, ExpressProvider.SF,
                "SF0214867476461", "success",
                List.of(new ExpressTrackNode(OffsetDateTime.parse("2026-09-15T10:00:00+08:00"),
                        "50", "已收件", "杭州")), null);

        Assert.assertEquals(RentalCode.LogisticsRouteStatus.IN_TRANSIT,
                MerchantLogisticsTrackingService.resolveRouteStatus(ExpressProvider.SF, result));
    }
}
