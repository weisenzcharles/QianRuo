package com.ruoyi.merchant.application;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.merchant.mapper.MerchantPaymentMapper;
import com.ruoyi.merchant.model.MerchantPaymentBizType;
import com.ruoyi.merchant.model.MerchantPaymentRecord;
import com.ruoyi.merchant.model.MerchantPaymentSettlementResult;
import com.ruoyi.merchant.model.MerchantFinanceAccountType;

/**
 * 支付宝通知的本地账务事务。
 *
 * <p>远程订单状态同步不放在此事务中，避免数据库事务持有期间进行 Feign 网络调用。</p>
 */
@Service
public class MerchantPaymentSettlementService
{
    private final MerchantPaymentMapper mapper;

    public MerchantPaymentSettlementService(MerchantPaymentMapper mapper)
    {
        this.mapper = mapper;
    }

    /** 验证金额后幂等更新支付单，并追加一笔收入流水。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantPaymentSettlementResult settle(String paymentNo, String alipayTradeNo, String notifyId,
            BigDecimal totalAmount)
    {
        MerchantPaymentRecord payment = mapper.selectByPaymentNoForUpdate(paymentNo);
        if (payment == null)
        {
            throw new ServiceException("支付单不存在");
        }
        if (payment.getAmount() == null || totalAmount == null || payment.getAmount().compareTo(totalAmount) != 0)
        {
            throw new ServiceException("支付宝支付金额校验失败");
        }
        if (RentalCode.PaymentStatus.PAID.getCode().equals(payment.getPaymentStatus()))
        {
            if (payment.getAlipayTradeNo() != null && !payment.getAlipayTradeNo().equals(alipayTradeNo))
            {
                throw new ServiceException("支付单已绑定其他支付宝交易");
            }
            MerchantPaymentSettlementResult result = new MerchantPaymentSettlementResult();
            result.setPayment(payment);
            result.setAlreadyPaid(true);
            return result;
        }
        if (!RentalCode.PaymentStatus.UNPAID.getCode().equals(payment.getPaymentStatus()))
        {
            throw new ServiceException("支付单当前状态不可入账");
        }
        if (mapper.markPaid(payment.getPaymentId(), alipayTradeNo, notifyId,
                RentalCode.PaymentStatus.UNPAID.getCode(), RentalCode.PaymentStatus.PAID.getCode()) != 1)
        {
            throw new ServiceException("支付单状态更新失败，请重试");
        }

        MerchantPaymentBizType bizType = MerchantPaymentBizType.fromCode(payment.getBizType());
        if (bizType == null)
        {
            throw new ServiceException("支付业务类型不受支持");
        }
        Integer accountType = paymentAccountType(payment.getBizType());
        BigDecimal currentBalance = mapper.selectAvailableBalanceForUpdate(payment.getTenantId(), payment.getMemberId(),
                RentalCode.FinanceDirection.INCOME.getCode(), RentalCode.FinanceDirection.EXPENSE.getCode(),
                RentalCode.FinanceDirection.UNFREEZE.getCode(), RentalCode.FinanceDirection.REVERSE.getCode(),
                RentalCode.LedgerStatus.SUCCESS.getCode(), accountType);
        BigDecimal balanceAfter = (currentBalance == null ? BigDecimal.ZERO : currentBalance).add(payment.getAmount());
        String ledgerNo = "PAY-" + payment.getPaymentNo();
        if (mapper.insertFinanceLedger(payment.getTenantId(), payment.getMemberId(), ledgerNo, payment.getBizType(),
                payment.getAmount(), balanceAfter, payment.getPaymentId(), "payment", "alipay", alipayTradeNo,
                RentalCode.FinanceDirection.INCOME.getCode(), RentalCode.LedgerStatus.SUCCESS.getCode(), null,
                bizType.getLabel() + "支付宝入账", accountType) != 1)
        {
            throw new ServiceException("财务流水入账失败，请重试");
        }
        if (mapper.upsertAccountBalance(payment.getTenantId(), payment.getMemberId(),
                accountType, payment.getAmount(), null) <= 0)
        {
            throw new ServiceException("财务账户余额更新失败，请重试");
        }

        payment.setPaymentStatus(RentalCode.PaymentStatus.PAID.getCode());
        payment.setAlipayTradeNo(alipayTradeNo);
        payment.setNotifyId(notifyId);
        MerchantPaymentSettlementResult result = new MerchantPaymentSettlementResult();
        result.setPayment(payment);
        result.setAlreadyPaid(false);
        return result;
    }

    private Integer paymentAccountType(String bizType)
    {
        return MerchantPaymentBizType.ORDER.getCode().equalsIgnoreCase(bizType)
                ? MerchantFinanceAccountType.RENTAL_SETTLEMENT.getCode()
                : MerchantFinanceAccountType.VALUE_ADDED.getCode();
    }
}
