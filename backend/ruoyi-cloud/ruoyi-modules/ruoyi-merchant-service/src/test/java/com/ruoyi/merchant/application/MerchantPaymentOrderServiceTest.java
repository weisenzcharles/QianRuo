package com.ruoyi.merchant.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.reflect.Proxy;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.merchant.mapper.MerchantPaymentMapper;
import com.ruoyi.merchant.model.MerchantPaymentRecord;

/** 订单支付单创建的并发复用规则测试。 */
public class MerchantPaymentOrderServiceTest
{
    @Test
    public void createOrReuse_存在有效待支付单_复用原支付单()
    {
        MerchantPaymentRecord existing = payment();
        AtomicInteger insertCount = new AtomicInteger();
        MerchantPaymentMapper mapper = (MerchantPaymentMapper) Proxy.newProxyInstance(
                MerchantPaymentMapper.class.getClassLoader(), new Class<?>[] { MerchantPaymentMapper.class },
                (proxy, method, args) -> {
                    if ("selectLatestByBusinessForUpdate".equals(method.getName())) return existing;
                    if ("insertPaymentOrder".equals(method.getName()))
                    {
                        insertCount.incrementAndGet();
                        return 1;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });

        MerchantPaymentRecord result = new MerchantPaymentOrderService(mapper).createOrReuse(1L, 2L, 3L,
                "OD0000000003", "订单支付", new BigDecimal("100.00"), LocalDateTime.now().plusMinutes(10), 2L);

        Assert.assertSame(existing, result);
        Assert.assertEquals(0, insertCount.get());
        Assert.assertEquals(RentalCode.PaymentStatus.UNPAID.getCode(), result.getPaymentStatus());
    }

    private MerchantPaymentRecord payment()
    {
        MerchantPaymentRecord payment = new MerchantPaymentRecord();
        payment.setPaymentId(1L);
        payment.setPaymentNo("ORD202609241100001234567890");
        payment.setBizType("order");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setPaymentStatus(RentalCode.PaymentStatus.UNPAID.getCode());
        payment.setExpireTime(LocalDateTime.now().plusMinutes(10));
        return payment;
    }
}
