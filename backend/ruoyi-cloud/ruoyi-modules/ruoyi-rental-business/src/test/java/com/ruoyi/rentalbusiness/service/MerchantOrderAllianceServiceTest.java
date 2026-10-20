package com.ruoyi.rentalbusiness.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.ruoyi.rentalbusiness.enums.MerchantAllianceOrderStatus;

/** 验证子订单履约状态向联盟协助订单状态的稳定映射。 */
public class MerchantOrderAllianceServiceTest {

    private final MerchantOrderAllianceService service = new MerchantOrderAllianceService(null, null, null, null, null,
            null);

    @Test
    public void allianceStatusForChildOrder_履约状态_映射为履约中() {
        assertEquals(Integer.valueOf(MerchantAllianceOrderStatus.IN_FULFILLMENT.getCode()),
                service.allianceStatusForChildOrder(20, MerchantAllianceOrderStatus.ACCEPTED.getCode()));
        assertEquals(Integer.valueOf(MerchantAllianceOrderStatus.IN_FULFILLMENT.getCode()),
                service.allianceStatusForChildOrder(45, MerchantAllianceOrderStatus.ACCEPTED.getCode()));
    }

    @Test
    public void allianceStatusForChildOrder_完成和异常状态_映射到对应联盟状态() {
        assertEquals(Integer.valueOf(MerchantAllianceOrderStatus.COMPLETED.getCode()),
                service.allianceStatusForChildOrder(50, MerchantAllianceOrderStatus.IN_FULFILLMENT.getCode()));
        assertEquals(Integer.valueOf(MerchantAllianceOrderStatus.EXCEPTION.getCode()),
                service.allianceStatusForChildOrder(60, MerchantAllianceOrderStatus.IN_FULFILLMENT.getCode()));
        assertEquals(Integer.valueOf(MerchantAllianceOrderStatus.CANCELLED.getCode()),
                service.allianceStatusForChildOrder(80, MerchantAllianceOrderStatus.ACCEPTED.getCode()));
    }

    @Test
    public void allianceStatusForChildOrder_未识别状态_保持当前状态() {
        assertEquals(Integer.valueOf(MerchantAllianceOrderStatus.ACCEPTED.getCode()),
                service.allianceStatusForChildOrder(999, MerchantAllianceOrderStatus.ACCEPTED.getCode()));
    }

    @Test
    public void isCancellableStatus_待接受和公众池中_允许取消() {
        assertEquals(true, service.isCancellableStatus(MerchantAllianceOrderStatus.PENDING.getCode()));
        assertEquals(true, service.isCancellableStatus(MerchantAllianceOrderStatus.PUBLIC_POOL.getCode()));
    }

    @Test
    public void isCancellableStatus_已接受或空值_禁止取消() {
        assertEquals(false, service.isCancellableStatus(MerchantAllianceOrderStatus.ACCEPTED.getCode()));
        assertEquals(false, service.isCancellableStatus(null));
    }
}
