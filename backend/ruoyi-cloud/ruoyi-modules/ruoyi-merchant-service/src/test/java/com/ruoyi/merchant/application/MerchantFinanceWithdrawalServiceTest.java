package com.ruoyi.merchant.application;

import java.math.BigDecimal;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.merchant.mapper.MerchantFinanceWithdrawalMapper;
import com.ruoyi.merchant.model.MerchantWithdrawalApplyCommand;
import com.ruoyi.merchant.model.MerchantWithdrawalChannel;
import com.ruoyi.merchant.model.MerchantWithdrawalStatus;
import com.ruoyi.merchant.model.MerchantWithdrawalVO;
import com.ruoyi.system.api.model.LoginUser;

/** 商户提现申请的余额冻结和异常边界测试。 */
public class MerchantFinanceWithdrawalServiceTest
{
    @After
    public void clearContext()
    {
        SecurityContextHolder.remove();
    }

    /** 正常申请必须扣减可用余额、增加冻结余额并记录冻结流水。 */
    @Test
    public void apply_withSufficientBalance_freezesBalanceAndCreatesPendingWithdrawal()
    {
        setMerchantContext();
        AtomicReference<BigDecimal> updatedAvailable = new AtomicReference<>();
        AtomicReference<BigDecimal> updatedFrozen = new AtomicReference<>();
        AtomicReference<Integer> ledgerDirection = new AtomicReference<>();
        MerchantFinanceWithdrawalMapper mapper = mapper((method, args) -> {
            if ("selectByIdempotencyForUpdate".equals(method)) return null;
            if ("selectCashAccountForUpdate".equals(method))
                return Map.of("accountId", 501L, "availableAmount", new BigDecimal("100.00"),
                        "frozenAmount", new BigDecimal("10.00"), "version", 3L);
            if ("updateCashAccount".equals(method))
            {
                updatedAvailable.set((BigDecimal) args[1]);
                updatedFrozen.set((BigDecimal) args[2]);
                return 1;
            }
            if ("insertWithdrawal".equals(method)) return 1;
            if ("selectLastInsertId".equals(method)) return 900L;
            if ("insertFinanceLedger".equals(method))
            {
                ledgerDirection.set((Integer) args[3]);
                return 1;
            }
            if ("selectByNo".equals(method))
            {
                MerchantWithdrawalVO row = new MerchantWithdrawalVO();
                row.setWithdrawalNo((String) args[2]);
                row.setWithdrawalStatus(MerchantWithdrawalStatus.PENDING_REVIEW.getCode());
                row.setWithdrawalChannel(MerchantWithdrawalChannel.ALIPAY.getCode());
                return row;
            }
            throw new UnsupportedOperationException(method);
        });

        MerchantWithdrawalApplyCommand command = new MerchantWithdrawalApplyCommand();
        command.setAmount(new BigDecimal("30.00"));
        command.setWithdrawalChannel(MerchantWithdrawalChannel.ALIPAY);
        command.setPayeeName("张三");
        command.setPayeeAccount("alipay-account");
        command.setIdempotencyKey("withdrawal-test-1");

        MerchantWithdrawalVO result = new MerchantFinanceWithdrawalService(mapper).apply(command);

        Assert.assertEquals(new BigDecimal("70.00"), updatedAvailable.get());
        Assert.assertEquals(new BigDecimal("40.00"), updatedFrozen.get());
        Assert.assertEquals(Integer.valueOf(RentalCode.FinanceDirection.FREEZE.getCode()), ledgerDirection.get());
        Assert.assertEquals("待审核", result.getWithdrawalStatusName());
        Assert.assertEquals("支付宝", result.getWithdrawalChannelName());
    }

    /** 可用余额不足时必须拒绝申请，不能修改账户。 */
    @Test
    public void apply_withInsufficientBalance_rejectsWithoutUpdatingAccount()
    {
        setMerchantContext();
        AtomicReference<Boolean> updated = new AtomicReference<>(false);
        MerchantFinanceWithdrawalMapper mapper = mapper((method, args) -> {
            if ("selectByIdempotencyForUpdate".equals(method)) return null;
            if ("selectCashAccountForUpdate".equals(method))
                return Map.of("accountId", 501L, "availableAmount", new BigDecimal("10.00"),
                        "frozenAmount", BigDecimal.ZERO, "version", 3L);
            if ("updateCashAccount".equals(method))
            {
                updated.set(true);
                return 1;
            }
            throw new UnsupportedOperationException(method);
        });

        MerchantWithdrawalApplyCommand command = new MerchantWithdrawalApplyCommand();
        command.setAmount(new BigDecimal("20.00"));
        command.setWithdrawalChannel(MerchantWithdrawalChannel.BANK_TRANSFER);
        command.setPayeeName("张三");
        command.setPayeeAccount("bank-account");

        try
        {
            new MerchantFinanceWithdrawalService(mapper).apply(command);
            Assert.fail("insufficient balance must reject withdrawal");
        }
        catch (ServiceException exception)
        {
            Assert.assertEquals("可用余额不足，无法提交提现申请", exception.getMessage());
        }
        Assert.assertFalse(updated.get());
    }

    private void setMerchantContext()
    {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserid(9001L);
        loginUser.setClientType(Constants.CLIENT_TYPE_MERCHANT);
        loginUser.setTenantId(10001L);
        loginUser.setMemberId(27L);
        loginUser.setMerchantAuthStatus(RentalCode.AuthenticationStatus.VERIFIED.getCode());
        loginUser.setWarehouseScopeType(RentalCode.WarehouseScope.ALL.getCode());
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);
        SecurityContextHolder.setUserId(String.valueOf(loginUser.getUserid()));
    }

    private MerchantFinanceWithdrawalMapper mapper(MapperCall mapperCall)
    {
        return (MerchantFinanceWithdrawalMapper) Proxy.newProxyInstance(
                MerchantFinanceWithdrawalMapper.class.getClassLoader(),
                new Class<?>[] { MerchantFinanceWithdrawalMapper.class },
                (proxy, method, args) -> mapperCall.invoke(method.getName(), args));
    }

    @FunctionalInterface
    private interface MapperCall
    {
        Object invoke(String method, Object[] args);
    }
}
