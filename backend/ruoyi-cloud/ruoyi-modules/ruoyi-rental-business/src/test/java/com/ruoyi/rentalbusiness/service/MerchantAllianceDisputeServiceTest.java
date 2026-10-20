package com.ruoyi.rentalbusiness.service;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Test;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.merchant.api.RemoteMerchantDepositService;
import com.ruoyi.merchant.api.model.MerchantDepositBalance;
import com.ruoyi.merchant.api.model.MerchantDepositOperationCommand;
import com.ruoyi.rentalbusiness.mapper.MerchantAllianceDisputeMapper;
import com.ruoyi.system.api.model.LoginUser;

/** 验证到期纠纷只释放一次保证金并使用稳定幂等键关单。 */
public class MerchantAllianceDisputeServiceTest
{
    @After
    public void clearContext()
    {
        SecurityContextHolder.remove();
    }

    @Test
    public void releaseExpired_冻结保证金_释放后关闭纠纷()
    {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserid(9001L);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);
        SecurityContextHolder.setUserId("9001");

        AtomicReference<MerchantDepositOperationCommand> releaseCommand = new AtomicReference<>();
        AtomicInteger closed = new AtomicInteger();
        MerchantAllianceDisputeMapper mapper = mapper((method, args) -> {
            if ("selectExpiredForRelease".equals(method))
            {
                return List.of(Map.of("tenantId", 10001L, "disputeId", 88L, "allianceId", 9L,
                        "respondentMemberId", 27L, "frozenAmount", new BigDecimal("100.00")));
            }
            if ("closeExpired".equals(method))
            {
                closed.incrementAndGet();
                return 1;
            }
            return null;
        });
        RemoteMerchantDepositService depositService = (RemoteMerchantDepositService) Proxy.newProxyInstance(
                RemoteMerchantDepositService.class.getClassLoader(), new Class<?>[] { RemoteMerchantDepositService.class },
                (proxy, method, args) -> {
                    if ("release".equals(method.getName()))
                    {
                        releaseCommand.set((MerchantDepositOperationCommand) args[0]);
                        return R.ok(new MerchantDepositBalance());
                    }
                    return R.fail("测试替身未实现");
                });

        int result = new MerchantAllianceDisputeService(mapper, new MerchantAllianceDisputeLocalService(mapper),
                depositService).releaseExpired();

        assertEquals(1, result);
        assertEquals(1, closed.get());
        assertEquals("dispute:88:expiry-release", releaseCommand.get().getIdempotencyKey());
        assertEquals(Long.valueOf(9001L), releaseCommand.get().getOperatorId());
    }

    private MerchantAllianceDisputeMapper mapper(MapperCall mapperCall)
    {
        return (MerchantAllianceDisputeMapper) Proxy.newProxyInstance(MerchantAllianceDisputeMapper.class.getClassLoader(),
                new Class<?>[] { MerchantAllianceDisputeMapper.class },
                (proxy, method, args) -> mapperCall.invoke(method.getName(), args));
    }

    @FunctionalInterface
    private interface MapperCall
    {
        Object invoke(String method, Object[] args);
    }
}
