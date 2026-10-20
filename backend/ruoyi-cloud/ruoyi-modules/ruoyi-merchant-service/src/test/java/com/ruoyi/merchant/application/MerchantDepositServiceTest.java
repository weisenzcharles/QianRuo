package com.ruoyi.merchant.application;

import java.math.BigDecimal;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.merchant.api.model.MerchantDepositBalance;
import com.ruoyi.merchant.api.model.MerchantDepositOperationCommand;
import com.ruoyi.merchant.mapper.MerchantDepositMapper;
import com.ruoyi.merchant.model.MerchantFinanceAccountType;

/** 统一履约保证金分户的冻结、幂等和余额更新测试。 */
public class MerchantDepositServiceTest
{
    @Test
    public void freeze_usesUnifiedPerformanceDepositAccountAndFinanceLedger()
    {
        AtomicReference<BigDecimal> available = new AtomicReference<>(new BigDecimal("1000.00"));
        AtomicReference<BigDecimal> frozen = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<Integer> ledgerDirection = new AtomicReference<>();
        AtomicReference<Integer> ledgerAccountType = new AtomicReference<>();
        MerchantDepositMapper mapper = mapper((method, args) -> {
            if ("selectLedgerByKey".equals(method)) return null;
            if ("selectAccountForUpdate".equals(method)) return account(available.get(), frozen.get(), 0L);
            if ("updateBalance".equals(method)) {
                available.set((BigDecimal) args[1]);
                frozen.set((BigDecimal) args[2]);
                return 1;
            }
            if ("insertFinanceLedger".equals(method)) {
                ledgerAccountType.set((Integer) args[2]);
                ledgerDirection.set((Integer) args[4]);
                return 1;
            }
            if ("insertLedger".equals(method)) return 1;
            throw new UnsupportedOperationException(method);
        });

        MerchantDepositBalanceAssertions command = new MerchantDepositBalanceAssertions();
        MerchantDepositOperationCommand request = command.request();
        new MerchantDepositService(mapper).operate(request, 2);

        Assert.assertEquals(new BigDecimal("900.00"), available.get());
        Assert.assertEquals(new BigDecimal("100.00"), frozen.get());
        Assert.assertEquals(MerchantFinanceAccountType.PERFORMANCE_DEPOSIT.getCode(), ledgerAccountType.get().intValue());
        Assert.assertEquals(RentalCode.FinanceDirection.FREEZE.getCode().intValue(), ledgerDirection.get().intValue());
    }

    @Test
    public void repeatedIdempotency_returnsCurrentUnifiedBalanceWithoutNewLedger()
    {
        AtomicReference<Integer> financeLedgerWrites = new AtomicReference<>(0);
        MerchantDepositMapper mapper = mapper((method, args) -> {
            if ("selectLedgerByKey".equals(method)) return Map.of("ledgerId", 12L);
            if ("selectAccountForUpdate".equals(method)) return account(new BigDecimal("700.00"),
                    new BigDecimal("300.00"), 4L);
            if ("insertFinanceLedger".equals(method)) {
                financeLedgerWrites.set(financeLedgerWrites.get() + 1);
                return 1;
            }
            throw new UnsupportedOperationException(method);
        });

        MerchantDepositBalanceAssertions command = new MerchantDepositBalanceAssertions();
        MerchantDepositBalance result = new MerchantDepositService(mapper).operate(command.request(), 2);

        Assert.assertEquals(new BigDecimal("700.00"), result.getAvailableAmount());
        Assert.assertEquals(new BigDecimal("300.00"), result.getFrozenAmount());
        Assert.assertEquals(Integer.valueOf(0), financeLedgerWrites.get());
    }

    private Map<String, Object> account(BigDecimal available, BigDecimal frozen, Long version)
    {
        Map<String, Object> value = new HashMap<>();
        value.put("accountId", 9L);
        value.put("availableAmount", available);
        value.put("frozenAmount", frozen);
        value.put("version", version);
        value.put("accountStatus", 1);
        return value;
    }

    private MerchantDepositMapper mapper(MapperCall call)
    {
        return (MerchantDepositMapper) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] { MerchantDepositMapper.class }, (proxy, method, args) -> call.invoke(method.getName(), args));
    }

    @FunctionalInterface
    private interface MapperCall
    {
        Object invoke(String method, Object[] args);
    }

    private static final class MerchantDepositBalanceAssertions
    {
        private MerchantDepositOperationCommand request()
        {
            MerchantDepositOperationCommand value = new MerchantDepositOperationCommand();
            value.setTenantId(1L);
            value.setAllianceId(2L);
            value.setMemberId(3L);
            value.setAmount(new BigDecimal("100.00"));
            value.setRefType("dispute");
            value.setRefId(4L);
            value.setIdempotencyKey("dispute:4:freeze");
            value.setOperatorId(5L);
            value.setRemark("测试保证金操作");
            return value;
        }
    }
}
