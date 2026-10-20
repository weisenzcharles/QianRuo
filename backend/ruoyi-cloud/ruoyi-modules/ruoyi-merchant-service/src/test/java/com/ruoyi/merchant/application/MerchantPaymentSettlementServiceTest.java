package com.ruoyi.merchant.application;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.merchant.mapper.MerchantPaymentMapper;
import com.ruoyi.merchant.model.MerchantPaymentRecord;
import com.ruoyi.merchant.model.MerchantPaymentSettlementResult;

/** 支付宝回调本地入账的金额校验和幂等测试。 */
public class MerchantPaymentSettlementServiceTest
{
    @Test
    public void settle_未支付单金额一致_更新支付单并追加一笔流水()
    {
        MerchantPaymentRecord payment = payment(RentalCode.PaymentStatus.UNPAID.getCode());
        AtomicInteger ledgerInsertCount = new AtomicInteger();
        AtomicInteger accountUpdateCount = new AtomicInteger();
        MerchantPaymentMapper mapper = mapper(payment, ledgerInsertCount, accountUpdateCount);

        MerchantPaymentSettlementResult result = new MerchantPaymentSettlementService(mapper).settle(
                payment.getPaymentNo(), "alipay-trade-1", "notify-1", new BigDecimal("100.00"));

        Assert.assertFalse(result.isAlreadyPaid());
        Assert.assertEquals(RentalCode.PaymentStatus.PAID.getCode(), result.getPayment().getPaymentStatus());
        Assert.assertEquals(1, ledgerInsertCount.get());
        Assert.assertEquals(1, accountUpdateCount.get());
    }

    @Test
    public void settle_已支付单重复通知_不重复追加流水()
    {
        MerchantPaymentRecord payment = payment(RentalCode.PaymentStatus.PAID.getCode());
        payment.setAlipayTradeNo("alipay-trade-1");
        AtomicInteger ledgerInsertCount = new AtomicInteger();
        AtomicInteger accountUpdateCount = new AtomicInteger();
        MerchantPaymentMapper mapper = mapper(payment, ledgerInsertCount, accountUpdateCount);

        MerchantPaymentSettlementResult result = new MerchantPaymentSettlementService(mapper).settle(
                payment.getPaymentNo(), "alipay-trade-1", "notify-2", new BigDecimal("100.00"));

        Assert.assertTrue(result.isAlreadyPaid());
        Assert.assertEquals(0, ledgerInsertCount.get());
        Assert.assertEquals(0, accountUpdateCount.get());
    }

    private MerchantPaymentMapper mapper(MerchantPaymentRecord payment, AtomicInteger ledgerInsertCount,
            AtomicInteger accountUpdateCount)
    {
        return (MerchantPaymentMapper) Proxy.newProxyInstance(MerchantPaymentMapper.class.getClassLoader(),
                new Class<?>[] { MerchantPaymentMapper.class }, (proxy, method, args) -> {
                    switch (method.getName())
                    {
                        case "selectByPaymentNoForUpdate":
                            return payment;
                        case "markPaid":
                            payment.setPaymentStatus(RentalCode.PaymentStatus.PAID.getCode());
                            payment.setAlipayTradeNo("alipay-trade-1");
                            return 1;
                        case "selectAvailableBalanceForUpdate":
                            return BigDecimal.ZERO;
                        case "insertFinanceLedger":
                            ledgerInsertCount.incrementAndGet();
                            return 1;
                        case "upsertAccountBalance":
                            accountUpdateCount.incrementAndGet();
                            return 1;
                        default:
                            throw new UnsupportedOperationException(method.getName());
                    }
                });
    }

    private MerchantPaymentRecord payment(Integer status)
    {
        MerchantPaymentRecord payment = new MerchantPaymentRecord();
        payment.setPaymentId(1L);
        payment.setTenantId(1L);
        payment.setMemberId(2L);
        payment.setPaymentNo("RCH202609241100001234567890");
        payment.setBizType("recharge");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setPaymentStatus(status);
        return payment;
    }
}
