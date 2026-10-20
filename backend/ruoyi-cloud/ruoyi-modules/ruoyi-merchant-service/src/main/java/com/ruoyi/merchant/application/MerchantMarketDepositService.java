package com.ruoyi.merchant.application;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.merchant.mapper.MerchantMarketDepositMapper;
import com.ruoyi.merchant.model.MerchantFinanceAccountType;

/** 周转市场发布保证金闭环，使用履约保证金分户并追加不可变流水。 */
@Service
public class MerchantMarketDepositService
{
    private static final BigDecimal DEPOSIT_AMOUNT = new BigDecimal("500.00");
    private final MerchantMarketDepositMapper mapper;

    public MerchantMarketDepositService(MerchantMarketDepositMapper mapper)
    {
        this.mapper = mapper;
    }

    /** 发布帖子时冻结 500 元保证金。调用方必须已经锁定帖子。 */
    @Transactional(rollbackFor = Exception.class)
    public void freeze(Long tenantId, Long memberId, Long postId, String postNo)
    {
        Map<String, Object> latest = mapper.selectLatestDeposit(tenantId, memberId, postId);
        if (isSuccess(latest) && integer(latest.get("direction")) == 1)
        {
            return;
        }
        Long operatorId = SecurityUtils.getUserId();
        Map<String, Object> account = account(tenantId, memberId, operatorId);
        BigDecimal available = decimal(account.get("availableAmount"));
        if (available.compareTo(DEPOSIT_AMOUNT) < 0)
        {
            throw new ServiceException("履约保证金可用余额不足500元，无法发布市场帖子");
        }
        BigDecimal nextAvailable = available.subtract(DEPOSIT_AMOUNT);
        BigDecimal nextFrozen = decimal(account.get("frozenAmount")).add(DEPOSIT_AMOUNT);
        updateAccount(account, nextAvailable, nextFrozen, operatorId);
        String token = UUID.randomUUID().toString().replace("-", "");
        if (mapper.insertFinanceLedger(tenantId, memberId, MerchantFinanceAccountType.PERFORMANCE_DEPOSIT.getCode(),
                "MKT-FREEZE-" + token, RentalCode.FinanceDirection.FREEZE.getCode(), "market_deposit",
                DEPOSIT_AMOUNT, nextAvailable, postId, "market_post", "internal", postNo, operatorId,
                "周转市场帖子发布冻结保证金") != 1)
        {
            throw new ServiceException("市场保证金冻结流水保存失败");
        }
        if (mapper.insertMarketDepositLedger(tenantId, memberId, DEPOSIT_AMOUNT, 1, "market_post", postId,
                "MKT-DEPOSIT-" + postId + "-" + token, operatorId, "周转市场帖子发布保证金") != 1)
        {
            throw new ServiceException("市场保证金流水保存失败");
        }
    }

    /** 帖子下架或关闭时解冻保证金。 */
    @Transactional(rollbackFor = Exception.class)
    public void release(Long tenantId, Long memberId, Long postId, String postNo)
    {
        Map<String, Object> latest = mapper.selectLatestDeposit(tenantId, memberId, postId);
        if (!isSuccess(latest) || integer(latest.get("direction")) != 1)
        {
            return;
        }
        Long operatorId = SecurityUtils.getUserId();
        Map<String, Object> account = account(tenantId, memberId, operatorId);
        BigDecimal frozen = decimal(account.get("frozenAmount"));
        if (frozen.compareTo(DEPOSIT_AMOUNT) < 0)
        {
            throw new ServiceException("履约保证金冻结余额异常，无法解冻市场保证金");
        }
        BigDecimal nextAvailable = decimal(account.get("availableAmount")).add(DEPOSIT_AMOUNT);
        BigDecimal nextFrozen = frozen.subtract(DEPOSIT_AMOUNT);
        updateAccount(account, nextAvailable, nextFrozen, operatorId);
        String token = UUID.randomUUID().toString().replace("-", "");
        if (mapper.insertFinanceLedger(tenantId, memberId, MerchantFinanceAccountType.PERFORMANCE_DEPOSIT.getCode(),
                "MKT-UNFREEZE-" + token, RentalCode.FinanceDirection.UNFREEZE.getCode(), "market_deposit",
                DEPOSIT_AMOUNT, nextAvailable, postId, "market_post", "internal", postNo, operatorId,
                "周转市场帖子下架解冻保证金") != 1)
        {
            throw new ServiceException("市场保证金解冻流水保存失败");
        }
        if (mapper.insertMarketDepositLedger(tenantId, memberId, DEPOSIT_AMOUNT, 3, "market_post", postId,
                "MKT-RELEASE-" + postId + "-" + token, operatorId, "周转市场帖子下架退回保证金") != 1)
        {
            throw new ServiceException("市场保证金退回流水保存失败");
        }
    }

    private Map<String, Object> account(Long tenantId, Long memberId, Long operatorId)
    {
        Integer accountType = MerchantFinanceAccountType.PERFORMANCE_DEPOSIT.getCode();
        Map<String, Object> account = mapper.selectAccountForUpdate(tenantId, memberId, accountType);
        if (account == null)
        {
            mapper.insertAccount(tenantId, memberId, accountType, operatorId);
            account = mapper.selectAccountForUpdate(tenantId, memberId, accountType);
        }
        if (account == null || integer(account.get("accountStatus")) != 1)
        {
            throw new ServiceException("履约保证金账户不可用");
        }
        return account;
    }

    private void updateAccount(Map<String, Object> account, BigDecimal available, BigDecimal frozen, Long operatorId)
    {
        if (mapper.updateAccount(number(account.get("accountId")), available, frozen,
                number(account.get("version")), operatorId) != 1)
        {
            throw new ServiceException("履约保证金账户已发生变化，请刷新后重试");
        }
    }

    private boolean isSuccess(Map<String, Object> value)
    {
        return value != null && integer(value.get("ledgerStatus")) == RentalCode.LedgerStatus.SUCCESS.getCode();
    }

    private BigDecimal decimal(Object value) { return value == null ? BigDecimal.ZERO : new BigDecimal(value.toString()); }
    private Integer integer(Object value) { return value == null ? null : ((Number) value).intValue(); }
    private Long number(Object value) { return value == null ? null : ((Number) value).longValue(); }
}
