package com.ruoyi.system.service.rental;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.api.model.PlatformAllianceOrderPageView;
import com.ruoyi.system.api.model.PlatformAllianceOrderQuery;
import com.ruoyi.system.mapper.rental.PlatformFinanceMapper;
import com.ruoyi.system.model.rental.PlatformFinanceAccountVO;
import com.ruoyi.system.model.rental.PlatformFinanceAccountType;
import com.ruoyi.system.model.rental.PlatformFinanceLedgerVO;
import com.ruoyi.system.model.rental.PlatformFinanceQuery;
import com.ruoyi.system.model.rental.PlatformFinanceScene;
import com.ruoyi.system.model.rental.PlatformFinanceSummaryVO;
import com.ruoyi.system.model.rental.PlatformFinanceWithdrawalReviewCommand;
import com.ruoyi.system.model.rental.PlatformFinanceWithdrawalStatus;
import com.ruoyi.system.model.rental.PlatformFinanceWithdrawalVO;
import com.ruoyi.system.model.rental.PlatformPaymentOrderVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 平台财务查询服务。
 *
 * <p>平台端负责跨商户财务治理；资金流水仍保持不可变，提现审核通过余额投影和追加流水
 * 在同一事务内完成。</p>
 */
@Service
public class PlatformFinanceService
{
    private final PlatformFinanceMapper financeMapper;
    private final PlatformOrderService orderService;

    public PlatformFinanceService(PlatformFinanceMapper financeMapper, PlatformOrderService orderService)
    {
        this.financeMapper = financeMapper;
        this.orderService = orderService;
    }

    /** 查询平台财务总览。 */
    public PlatformFinanceSummaryVO summary()
    {
        Map<String, Object> values = financeMapper.selectSummary(LocalDate.now().withDayOfMonth(1).toString());
        PlatformFinanceSummaryVO result = new PlatformFinanceSummaryVO();
        if (values == null)
        {
            return result;
        }
        result.setTotalIncome(decimal(values, "totalIncome"));
        result.setTotalExpense(decimal(values, "totalExpense"));
        result.setNetAmount(decimal(values, "netAmount"));
        result.setFrozenAmount(decimal(values, "frozenAmount"));
        result.setMonthIncome(decimal(values, "monthIncome"));
        result.setMonthExpense(decimal(values, "monthExpense"));
        result.setPendingInvoiceCount(integer(values, "pendingInvoiceCount"));
        result.setAbnormalLedgerCount(integer(values, "abnormalLedgerCount"));
        result.setMerchantCount(integer(values, "merchantCount"));
        result.setPaymentOrderCount(integer(values, "paymentOrderCount"));
        result.setUnpaidPaymentCount(integer(values, "unpaidPaymentCount"));
        result.setPaidPaymentAmount(decimal(values, "paidPaymentAmount"));
        result.setUnpaidPaymentAmount(decimal(values, "unpaidPaymentAmount"));
        return result;
    }

    /** 按商户查询平台账户汇总。 */
    public List<PlatformFinanceAccountVO> accounts(PlatformFinanceQuery query)
    {
        normalize(query);
        List<PlatformFinanceAccountVO> rows = financeMapper.selectAccounts(query);
        return rows == null ? Collections.emptyList() : rows;
    }

    /** 分页查询资金流水，并补齐所有页面需要的中文枚举名称。 */
    public List<PlatformFinanceLedgerVO> ledger(PlatformFinanceQuery query)
    {
        normalize(query);
        validateScene(query.getScene());
        List<PlatformFinanceLedgerVO> rows = financeMapper.selectLedger(query);
        enrichLedger(rows);
        return rows == null ? Collections.emptyList() : rows;
    }

    /** 查询单条资金流水详情。 */
    public PlatformFinanceLedgerVO ledger(String ledgerNo)
    {
        PlatformFinanceLedgerVO row = financeMapper.selectLedgerByNo(trim(ledgerNo));
        enrichLedger(row);
        return row;
    }

    /** 分页查询支付宝支付订单。 */
    public List<PlatformPaymentOrderVO> payments(PlatformFinanceQuery query)
    {
        normalize(query);
        List<PlatformPaymentOrderVO> rows = financeMapper.selectPaymentOrders(query);
        enrichPaymentOrders(rows);
        return rows == null ? Collections.emptyList() : rows;
    }

    /** 查询支付订单详情。 */
    public PlatformPaymentOrderVO payment(String paymentNo)
    {
        PlatformPaymentOrderVO row = financeMapper.selectPaymentOrderByNo(trim(paymentNo));
        enrichPaymentOrder(row);
        return row;
    }

    /** 查询提现申请列表。收款账户始终由 Mapper 脱敏后返回。 */
    public List<PlatformFinanceWithdrawalVO> withdrawals(PlatformFinanceQuery query)
    {
        normalize(query);
        if (query.getWithdrawalStatus() != null
                && PlatformFinanceWithdrawalStatus.fromCode(query.getWithdrawalStatus()) == null)
            throw new ServiceException("提现状态不合法");
        List<PlatformFinanceWithdrawalVO> rows = financeMapper.selectWithdrawals(query);
        if (rows == null)
            return Collections.emptyList();
        for (PlatformFinanceWithdrawalVO row : rows)
            decorateWithdrawal(row);
        return rows;
    }

    /** 查询提现申请详情。 */
    public PlatformFinanceWithdrawalVO withdrawal(String withdrawalNo)
    {
        PlatformFinanceWithdrawalVO row = financeMapper.selectWithdrawalByNo(trim(withdrawalNo));
        decorateWithdrawal(row);
        return row;
    }

    /** 审核提现申请：通过则扣除冻结金额，拒绝则解冻，任何结果都追加不可变流水。 */
    @Transactional(rollbackFor = Exception.class)
    public PlatformFinanceWithdrawalVO reviewWithdrawal(String withdrawalNo,
            PlatformFinanceWithdrawalReviewCommand command)
    {
        String normalizedNo = trim(withdrawalNo);
        if (normalizedNo == null || command == null || command.getApproved() == null)
            throw new ServiceException("提现审核参数不完整");
        String reviewRemark = trim(command.getReviewRemark());
        if (!command.getApproved().booleanValue() && (reviewRemark == null || reviewRemark.isEmpty()))
            throw new ServiceException("拒绝提现时必须填写审核备注");

        Map<String, Object> withdrawal = financeMapper.selectWithdrawalForUpdate(normalizedNo);
        if (withdrawal == null)
            throw new ServiceException("提现申请不存在或已删除");
        Integer currentStatus = integerValue(withdrawal.get("withdrawalStatus"));
        if (currentStatus == null || currentStatus.intValue() != PlatformFinanceWithdrawalStatus.PENDING_REVIEW.getCode())
            throw new ServiceException("该提现申请已审核，不能重复处理");

        Long tenantId = longValue(withdrawal.get("tenantId"));
        Long memberId = longValue(withdrawal.get("memberId"));
        Long accountId = longValue(withdrawal.get("accountId"));
        Integer accountType = integerValue(withdrawal.get("accountType"));
        BigDecimal amount = decimalValue(withdrawal.get("amount"));
        Map<String, Object> account = financeMapper.selectAccountForUpdate(tenantId, memberId, accountType);
        if (account == null || !accountId.equals(longValue(account.get("accountId"))))
            throw new ServiceException("提现账户不存在或已变更");
        BigDecimal frozen = decimalValue(account.get("frozenAmount"));
        if (frozen.compareTo(amount) < 0)
            throw new ServiceException("账户冻结余额不足，无法完成审核");
        BigDecimal available = decimalValue(account.get("availableAmount"));
        BigDecimal nextFrozen = frozen.subtract(amount);
        BigDecimal nextAvailable = available;
        Integer direction;
        String remark;
        PlatformFinanceWithdrawalStatus targetStatus;
        if (command.getApproved().booleanValue())
        {
            direction = RentalCode.FinanceDirection.EXPENSE.getCode();
            remark = "提现审核通过并完成出款";
            targetStatus = PlatformFinanceWithdrawalStatus.COMPLETED;
        }
        else
        {
            direction = RentalCode.FinanceDirection.UNFREEZE.getCode();
            nextAvailable = available.add(amount);
            remark = "提现审核拒绝，解冻余额";
            targetStatus = PlatformFinanceWithdrawalStatus.REJECTED;
        }
        Long operatorId = SecurityUtils.getUserId();
        Long version = longValue(account.get("version"));
        if (financeMapper.updateAccount(accountId, nextAvailable, nextFrozen, version, operatorId) != 1)
            throw new ServiceException("账户余额已发生变化，请刷新后重试");
        String ledgerNo = "WDL-" + (targetStatus == PlatformFinanceWithdrawalStatus.COMPLETED ? "EXPENSE-" : "UNFREEZE-")
                + normalizedNo;
        if (financeMapper.insertWithdrawalLedger(tenantId, memberId, ledgerNo, direction, amount, nextAvailable,
                longValue(withdrawal.get("withdrawalId")), stringValue(withdrawal.get("withdrawalChannel")), normalizedNo,
                remark, operatorId, accountType) != 1)
            throw new ServiceException("提现审核流水保存失败");
        if (financeMapper.updateWithdrawal(longValue(withdrawal.get("withdrawalId")), targetStatus.getCode(),
                reviewRemark, operatorId) != 1)
            throw new ServiceException("提现申请状态更新失败");
        return withdrawal(normalizedNo);
    }

    /** 查询联盟订单结算明细，复用平台订单服务的商户名称补全和数据权限边界。 */
    public PlatformAllianceOrderPageView settlements(PlatformAllianceOrderQuery query)
    {
        if (query == null)
        {
            query = new PlatformAllianceOrderQuery();
        }
        if (query.getPageNum() == null || query.getPageNum().intValue() < 1)
        {
            query.setPageNum(Integer.valueOf(1));
        }
        if (query.getPageSize() == null || query.getPageSize().intValue() < 1)
        {
            query.setPageSize(Integer.valueOf(10));
        }
        return orderService.listAlliance(query);
    }

    private void normalize(PlatformFinanceQuery query)
    {
        if (query == null)
        {
            throw new ServiceException("财务查询条件不能为空");
        }
        query.setMerchantKeyword(trim(query.getMerchantKeyword()));
        query.setLedgerNo(trim(query.getLedgerNo()));
        query.setPaymentNo(trim(query.getPaymentNo()));
        query.setBizType(trim(query.getBizType()));
        query.setWithdrawalNo(trim(query.getWithdrawalNo()));
        query.setBeginTime(trim(query.getBeginTime()));
        query.setEndTime(trim(query.getEndTime()));
        query.setScene(trim(query.getScene()));
        if (query.getAccountType() != null && PlatformFinanceAccountType.fromCode(query.getAccountType()) == null)
            throw new ServiceException("不支持的财务账户类型");
    }

    private void decorateWithdrawal(PlatformFinanceWithdrawalVO row)
    {
        if (row == null)
            return;
        PlatformFinanceWithdrawalStatus status = PlatformFinanceWithdrawalStatus.fromCode(row.getWithdrawalStatus());
        row.setWithdrawalStatusName(status == null ? "未知状态" : status.getLabel());
        row.setWithdrawalChannelName(withdrawalChannelName(row.getWithdrawalChannel()));
    }

    private String withdrawalChannelName(String value)
    {
        if ("ALIPAY".equalsIgnoreCase(value))
            return "支付宝";
        if ("BANK_TRANSFER".equalsIgnoreCase(value))
            return "银行转账";
        return "其他渠道";
    }

    private BigDecimal decimalValue(Object value)
    {
        return value == null ? BigDecimal.ZERO.setScale(2) : new BigDecimal(value.toString());
    }

    private Integer integerValue(Object value)
    {
        return value == null ? null : Integer.valueOf(value.toString());
    }

    private Long longValue(Object value)
    {
        return value == null ? null : Long.valueOf(value.toString());
    }

    private String stringValue(Object value)
    {
        return value == null ? null : value.toString();
    }

    private void validateScene(String scene)
    {
        if (scene != null && !scene.isEmpty() && !PlatformFinanceScene.contains(scene))
        {
            throw new ServiceException("不支持的财务页面场景");
        }
    }

    private void enrichLedger(List<PlatformFinanceLedgerVO> rows)
    {
        if (rows == null)
        {
            return;
        }
        for (PlatformFinanceLedgerVO row : rows)
        {
            enrichLedger(row);
        }
    }

    private void enrichLedger(PlatformFinanceLedgerVO row)
    {
        if (row == null)
        {
            return;
        }
        row.setDirectionName(label(RentalCode.FinanceDirection.values(), row.getDirection(), "未知方向"));
        row.setAccountTypeName(PlatformFinanceAccountType.labelOf(row.getAccountType()));
        row.setInvoiceStatusName(row.getInvoiceStatus() == null ? "未申请"
                : label(RentalCode.InvoiceStatus.values(), row.getInvoiceStatus(), "未知状态"));
        row.setLedgerStatusName(label(RentalCode.LedgerStatus.values(), row.getLedgerStatus(), "未知状态"));
        row.setBizTypeName(bizTypeName(row.getBizType()));
    }

    private void enrichPaymentOrders(List<PlatformPaymentOrderVO> rows)
    {
        if (rows == null)
        {
            return;
        }
        for (PlatformPaymentOrderVO row : rows)
        {
            enrichPaymentOrder(row);
        }
    }

    private void enrichPaymentOrder(PlatformPaymentOrderVO row)
    {
        if (row == null)
        {
            return;
        }
        row.setPaymentStatusName(label(RentalCode.PaymentStatus.values(), row.getPaymentStatus(), "未知状态"));
        row.setBizTypeName(bizTypeName(row.getBizType()));
    }

    private String bizTypeName(String value)
    {
        String type = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (type.contains("recharge") || type.contains("充值")) return "账户充值";
        if (type.contains("order") || type.contains("订单")) return "订单支付";
        if (type.contains("service") || type.contains("storage") || type.contains("增值") || type.contains("服务")) return "增值服务";
        if (type.contains("express") || type.contains("shipping") || type.contains("delivery") || type.contains("快递") || type.contains("物流")) return "快递费用";
        if (type.contains("rent") || type.contains("settle") || type.contains("租金") || type.contains("结算")) return "租金 / 分账";
        if (type.contains("withdraw") || type.contains("提现")) return "提现";
        if (type.contains("compensation") || type.contains("赔付")) return "赔付";
        if (type.contains("sale") || type.contains("出售")) return "设备出售";
        return type.isEmpty() ? "其他财务业务" : value;
    }

    private <T extends Enum<T> & com.ruoyi.common.core.enums.NumericCode> String label(T[] values, Integer code,
            String fallback)
    {
        if (code != null)
        {
            for (T value : values)
            {
                if (value.getCode().equals(code))
                {
                    return value.getLabel();
                }
            }
        }
        return fallback;
    }

    private BigDecimal decimal(Map<String, Object> values, String key)
    {
        Object value = values.get(key);
        if (value == null) value = values.get(key.toUpperCase(Locale.ROOT));
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        if (value == null) return BigDecimal.ZERO;
        try
        {
            return new BigDecimal(value.toString());
        }
        catch (NumberFormatException ex)
        {
            throw new ServiceException("财务汇总金额格式错误");
        }
    }

    private Integer integer(Map<String, Object> values, String key)
    {
        Object value = values.get(key);
        if (value == null) value = values.get(key.toUpperCase(Locale.ROOT));
        if (value instanceof Number) return Integer.valueOf(((Number) value).intValue());
        if (value == null) return Integer.valueOf(0);
        try
        {
            return Integer.valueOf(value.toString());
        }
        catch (NumberFormatException ex)
        {
            throw new ServiceException("财务汇总数量格式错误");
        }
    }

    private String trim(String value)
    {
        if (value == null) return null;
        String result = value.trim();
        return result.isEmpty() ? null : result;
    }
}
