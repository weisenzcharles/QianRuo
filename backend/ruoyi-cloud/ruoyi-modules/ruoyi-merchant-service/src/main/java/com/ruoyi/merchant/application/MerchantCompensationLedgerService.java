package com.ruoyi.merchant.application;

import java.math.BigDecimal;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.merchant.api.model.MerchantCompensationLedgerCommand;
import com.ruoyi.merchant.api.model.MerchantCompensationLedgerResult;
import com.ruoyi.merchant.mapper.MerchantPaymentMapper;
import com.ruoyi.merchant.model.MerchantFinanceAccountType;

/** 物流赔付入账服务。以确定性流水号实现跨服务重试幂等。 */
@Service
public class MerchantCompensationLedgerService
{
    private static final String LEDGER_PREFIX = "COMP-";
    private final MerchantPaymentMapper mapper;

    public MerchantCompensationLedgerService(MerchantPaymentMapper mapper)
    {
        this.mapper = mapper;
    }

    /** 将审核通过的物流赔付计入租金/分账资金账户。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantCompensationLedgerResult post(MerchantCompensationLedgerCommand command)
    {
        validate(command);
        String ledgerNo = ledgerNo(command);
        Integer existing = mapper.countFinanceLedgerByNoForUpdate(command.getTenantId(), command.getMemberId(), ledgerNo);
        if (existing != null && existing.intValue() > 0)
        {
            MerchantCompensationLedgerResult result = new MerchantCompensationLedgerResult();
            result.setLedgerNo(ledgerNo);
            result.setAlreadySettled(true);
            result.setBalanceAfter(mapper.selectAvailableBalanceForUpdate(command.getTenantId(), command.getMemberId(),
                    RentalCode.FinanceDirection.INCOME.getCode(), RentalCode.FinanceDirection.EXPENSE.getCode(),
                    RentalCode.FinanceDirection.UNFREEZE.getCode(), RentalCode.FinanceDirection.REVERSE.getCode(),
                    RentalCode.LedgerStatus.SUCCESS.getCode(), MerchantFinanceAccountType.RENTAL_SETTLEMENT.getCode()));
            return result;
        }

        Integer accountType = MerchantFinanceAccountType.RENTAL_SETTLEMENT.getCode();
        BigDecimal currentBalance = mapper.selectAvailableBalanceForUpdate(command.getTenantId(), command.getMemberId(),
                RentalCode.FinanceDirection.INCOME.getCode(), RentalCode.FinanceDirection.EXPENSE.getCode(),
                RentalCode.FinanceDirection.UNFREEZE.getCode(), RentalCode.FinanceDirection.REVERSE.getCode(),
                RentalCode.LedgerStatus.SUCCESS.getCode(), accountType);
        BigDecimal balanceAfter = (currentBalance == null ? BigDecimal.ZERO : currentBalance).add(command.getAmount());
        if (mapper.insertFinanceLedger(command.getTenantId(), command.getMemberId(), ledgerNo,
                "logistics_compensation", command.getAmount(), balanceAfter, command.getClaimId(),
                "logistics_claim", "platform", ledgerNo, RentalCode.FinanceDirection.INCOME.getCode(),
                RentalCode.LedgerStatus.SUCCESS.getCode(), command.getOperatorId(),
                command.getRemark() == null ? "物流赔付入账" : command.getRemark(), accountType) != 1)
        {
            throw new ServiceException("物流赔付财务流水保存失败");
        }
        if (mapper.upsertAccountBalance(command.getTenantId(), command.getMemberId(), accountType,
                command.getAmount(), command.getOperatorId()) <= 0)
        {
            throw new ServiceException("物流赔付账户余额更新失败");
        }
        MerchantCompensationLedgerResult result = new MerchantCompensationLedgerResult();
        result.setLedgerNo(ledgerNo);
        result.setBalanceAfter(balanceAfter);
        result.setAlreadySettled(false);
        return result;
    }

    private void validate(MerchantCompensationLedgerCommand command)
    {
        if (command == null || command.getTenantId() == null || command.getMemberId() == null
                || command.getClaimId() == null || blank(command.getClaimNo())
                || command.getAmount() == null || command.getAmount().signum() <= 0)
        {
            throw new ServiceException("物流赔付入账参数无效");
        }
    }

    private String ledgerNo(MerchantCompensationLedgerCommand command)
    {
        String claimNo = command.getClaimNo().trim().toUpperCase(Locale.ROOT);
        return LEDGER_PREFIX + claimNo;
    }

    private boolean blank(String value)
    {
        return value == null || value.isBlank();
    }
}
