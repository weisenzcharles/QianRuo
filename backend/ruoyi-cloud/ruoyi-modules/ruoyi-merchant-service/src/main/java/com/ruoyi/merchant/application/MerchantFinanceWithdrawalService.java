package com.ruoyi.merchant.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.merchant.mapper.MerchantFinanceWithdrawalMapper;
import com.ruoyi.merchant.model.MerchantFinanceAccountType;
import com.ruoyi.merchant.model.MerchantWithdrawalApplyCommand;
import com.ruoyi.merchant.model.MerchantWithdrawalChannel;
import com.ruoyi.merchant.model.MerchantWithdrawalStatus;
import com.ruoyi.merchant.model.MerchantWithdrawalVO;

/** 商户提现账户闭环。申请时冻结余额，审核结果由平台服务追加对应财务流水。 */
@Service
public class MerchantFinanceWithdrawalService
{
    private static final DateTimeFormatter WITHDRAWAL_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
    private final MerchantFinanceWithdrawalMapper mapper;

    public MerchantFinanceWithdrawalService(MerchantFinanceWithdrawalMapper mapper)
    {
        this.mapper = mapper;
    }

    /** 提交提现申请并冻结可用余额。 */
    @Transactional(rollbackFor = Exception.class)
    public MerchantWithdrawalVO apply(MerchantWithdrawalApplyCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        validate(command);
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        String idempotencyKey = normalizeKey(command.getIdempotencyKey());
        MerchantWithdrawalVO existing = mapper.selectByIdempotencyForUpdate(tenantId, memberId, idempotencyKey);
        if (existing != null) return existing;

        Long operatorId = SecurityUtils.getUserId();
        Integer accountType = MerchantFinanceAccountType.RENTAL_SETTLEMENT.getCode();
        Map<String, Object> account = mapper.selectCashAccountForUpdate(tenantId, memberId, accountType);
        if (account == null)
        {
            mapper.insertCashAccount(tenantId, memberId, accountType, operatorId);
            account = mapper.selectCashAccountForUpdate(tenantId, memberId, accountType);
        }
        if (account == null) throw new ServiceException("财务账户不存在");
        BigDecimal available = decimal(account.get("availableAmount"));
        if (available.compareTo(command.getAmount()) < 0)
            throw new ServiceException("可用余额不足，无法提交提现申请");
        BigDecimal frozen = decimal(account.get("frozenAmount")).add(command.getAmount());
        BigDecimal nextAvailable = available.subtract(command.getAmount());
        Long accountId = number(account.get("accountId"));
        Long version = number(account.get("version"));
        if (mapper.updateCashAccount(accountId, nextAvailable, frozen, version, operatorId) != 1)
            throw new ServiceException("账户余额已发生变化，请刷新后重试");

        String withdrawalNo = "WDL-" + LocalDateTime.now().format(WITHDRAWAL_TIME)
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        BigDecimal fee = ZERO;
        try
        {
            if (mapper.insertWithdrawal(tenantId, memberId, accountId, accountType, withdrawalNo, idempotencyKey,
                    command.getAmount(), fee, command.getAmount(), command.getWithdrawalChannel().getCode(),
                    command.getPayeeName().trim(), command.getPayeeAccount().trim(), operatorId, trim(command.getRemark())) != 1)
                throw new ServiceException("提现申请保存失败");
        }
        catch (DuplicateKeyException exception)
        {
            // 兜底唯一键冲突必须抛出异常，让当前事务回滚账户扣减；不能在已扣款后直接返回旧申请。
            throw new ServiceException("提现申请重复提交，请重试")
                    .setDetailMessage(exception.getClass().getSimpleName());
        }
        Long withdrawalId = mapper.selectLastInsertId();
        if (mapper.insertFinanceLedger(tenantId, memberId, "WDF-FREEZE-" + withdrawalNo,
                RentalCode.FinanceDirection.FREEZE.getCode(), "withdrawal", command.getAmount(), nextAvailable,
                withdrawalId, command.getWithdrawalChannel().getCode(), withdrawalNo, "提现申请冻结租金 / 分账资金",
                operatorId, accountType) != 1)
            throw new ServiceException("提现冻结流水保存失败");
        return decorate(mapper.selectByNo(tenantId, memberId, withdrawalNo));
    }

    /** 查询当前商户提现申请。 */
    public List<MerchantWithdrawalVO> list(Integer status)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (status != null && MerchantWithdrawalStatus.fromCode(status) == null)
            throw new ServiceException("提现状态不合法");
        List<MerchantWithdrawalVO> rows = mapper.selectList(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), status);
        if (rows == null || rows.isEmpty()) return Collections.emptyList();
        for (MerchantWithdrawalVO row : rows) decorate(row);
        return rows;
    }

    /** 查询当前商户单笔提现申请。 */
    public MerchantWithdrawalVO get(String withdrawalNo)
    {
        MerchantContextUtils.requireMerchantApproved();
        MerchantWithdrawalVO row = mapper.selectByNo(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), trim(withdrawalNo));
        return decorate(row);
    }

    private void validate(MerchantWithdrawalApplyCommand command)
    {
        if (command == null || command.getAmount() == null || command.getAmount().signum() <= 0
                || command.getAmount().scale() > 2 || command.getWithdrawalChannel() == null
                || blank(command.getPayeeName()) || command.getPayeeName().trim().length() > 128
                || blank(command.getPayeeAccount()) || command.getPayeeAccount().trim().length() > 256)
            throw new ServiceException("提现申请参数不完整");
        if (command.getAmount().compareTo(new BigDecimal("1000000.00")) > 0)
            throw new ServiceException("单笔提现金额不能超过1000000元");
    }

    private MerchantWithdrawalVO decorate(MerchantWithdrawalVO row)
    {
        if (row == null) return null;
        MerchantWithdrawalStatus status = MerchantWithdrawalStatus.fromCode(row.getWithdrawalStatus());
        row.setWithdrawalStatusName(status == null ? "未知状态" : status.getLabel());
        MerchantWithdrawalChannel channel = MerchantWithdrawalChannel.fromCode(row.getWithdrawalChannel());
        row.setWithdrawalChannelName(channel == null ? "其他渠道" : channel.getLabel());
        return row;
    }

    private String normalizeKey(String value)
    {
        String key = trim(value);
        if (key == null) key = UUID.randomUUID().toString();
        if (key.length() > 128) throw new ServiceException("请求幂等键不能超过128个字符");
        return key;
    }

    private boolean blank(String value) { return value == null || value.trim().isEmpty(); }
    private String trim(String value) { return value == null ? null : value.trim(); }
    private BigDecimal decimal(Object value) { return value == null ? ZERO : new BigDecimal(value.toString()); }
    private Long number(Object value) { return value == null ? null : ((Number) value).longValue(); }
}
