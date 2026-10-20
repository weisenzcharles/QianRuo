package com.ruoyi.merchant.application;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.merchant.api.model.MerchantDepositBalance;
import com.ruoyi.merchant.api.model.MerchantDepositOperationCommand;
import com.ruoyi.merchant.mapper.MerchantDepositMapper;
import com.ruoyi.merchant.model.MerchantFinanceAccountType;

/** 保证金账户服务。账户和流水同属 ry_rental，因此在本地事务内完成余额与流水变更。 */
@Service
public class MerchantDepositService
{
    private static final int DEPOSIT = 1;
    private static final int FREEZE = 2;
    private static final int DEDUCT = 3;
    private static final int RELEASE = 4;
    private static final int REFUND = 5;

    private final MerchantDepositMapper mapper;

    public MerchantDepositService(MerchantDepositMapper mapper) { this.mapper = mapper; }

    @Transactional(rollbackFor = Exception.class)
    public MerchantDepositBalance operate(MerchantDepositOperationCommand command, int direction)
    {
        validate(command);
        Map<String, Object> oldLedger = mapper.selectLedgerByKey(command.getTenantId(), command.getIdempotencyKey());
        if (oldLedger != null)
        {
            Map<String, Object> current = mapper.selectAccountForUpdate(command.getTenantId(), command.getMemberId(),
                    MerchantFinanceAccountType.PERFORMANCE_DEPOSIT.getCode());
            if (current == null) throw new ServiceException("保证金账户不存在");
            return balanceFromAccount(current);
        }

        Integer accountType = MerchantFinanceAccountType.PERFORMANCE_DEPOSIT.getCode();
        Map<String, Object> account = mapper.selectAccountForUpdate(command.getTenantId(), command.getMemberId(), accountType);
        if (account == null)
        {
            mapper.insertAccount(command.getTenantId(), command.getMemberId(), accountType, command.getOperatorId());
            account = mapper.selectAccountForUpdate(command.getTenantId(), command.getMemberId(), accountType);
        }
        if (account == null || !Integer.valueOf(1).equals(integer(account.get("accountStatus"))))
            throw new ServiceException("保证金账户不可用");

        BigDecimal available = decimal(account.get("availableAmount"));
        BigDecimal frozen = decimal(account.get("frozenAmount"));
        switch (direction)
        {
            case DEPOSIT, REFUND -> available = available.add(command.getAmount());
            case FREEZE -> {
                requireEnough(available, command.getAmount(), "可用保证金不足");
                available = available.subtract(command.getAmount());
                frozen = frozen.add(command.getAmount());
            }
            case RELEASE -> {
                requireEnough(frozen, command.getAmount(), "冻结保证金不足");
                frozen = frozen.subtract(command.getAmount());
                available = available.add(command.getAmount());
            }
            case DEDUCT -> {
                requireEnough(frozen, command.getAmount(), "冻结保证金不足");
                frozen = frozen.subtract(command.getAmount());
            }
            default -> throw new ServiceException("不支持的保证金操作");
        }
        int version = ((Number) account.get("version")).intValue();
        if (mapper.updateBalance(((Number) account.get("accountId")).longValue(), available, frozen, version,
                command.getOperatorId()) != 1)
            throw new ServiceException("保证金账户已发生变化，请重试");

        if (mapper.insertFinanceLedger(command.getTenantId(), command.getMemberId(), accountType,
                "DEP-" + UUID.randomUUID().toString().replace("-", ""), financeDirection(direction),
                "alliance_deposit", command.getAmount(), available, command.getRefType(), command.getRefId(),
                command.getIdempotencyKey(), command.getOperatorId(), command.getRemark()) != 1)
            throw new ServiceException("统一保证金流水保存失败");

        // 保留联盟维度流水，用于按联盟幂等和历史业务追溯；余额事实以统一履约保证金账户为准。
        mapper.insertLedger(command.getTenantId(), command.getAllianceId(), command.getMemberId(), direction,
                command.getAmount(), command.getRefType(), command.getRefId(), available,
                command.getIdempotencyKey(), command.getOperatorId(), command.getRemark());
        MerchantDepositBalance result = new MerchantDepositBalance();
        result.setAccountId(((Number) account.get("accountId")).longValue());
        result.setAvailableAmount(available);
        result.setFrozenAmount(frozen);
        return result;
    }

    private void validate(MerchantDepositOperationCommand command)
    {
        if (command == null || command.getTenantId() == null || command.getAllianceId() == null
                || command.getMemberId() == null || command.getAmount() == null
                || command.getAmount().signum() <= 0 || command.getIdempotencyKey() == null
                || command.getIdempotencyKey().isBlank())
            throw new ServiceException("保证金操作参数无效");
    }

    private void requireEnough(BigDecimal balance, BigDecimal amount, String message)
    {
        if (balance.compareTo(amount) < 0) throw new ServiceException(message);
    }

    private Integer financeDirection(int direction)
    {
        return switch (direction)
        {
            case DEPOSIT, REFUND -> RentalCode.FinanceDirection.INCOME.getCode();
            case FREEZE -> RentalCode.FinanceDirection.FREEZE.getCode();
            case DEDUCT -> RentalCode.FinanceDirection.EXPENSE.getCode();
            case RELEASE -> RentalCode.FinanceDirection.UNFREEZE.getCode();
            default -> throw new ServiceException("不支持的保证金操作");
        };
    }

    private BigDecimal decimal(Object value) { return value == null ? BigDecimal.ZERO : new BigDecimal(value.toString()); }

    private Integer integer(Object value) { return value == null ? null : ((Number) value).intValue(); }

    private MerchantDepositBalance balanceFromAccount(Map<String, Object> account)
    {
        MerchantDepositBalance result = new MerchantDepositBalance();
        result.setAccountId(((Number) account.get("accountId")).longValue());
        result.setAvailableAmount(decimal(account.get("availableAmount")));
        result.setFrozenAmount(decimal(account.get("frozenAmount")));
        return result;
    }
}
