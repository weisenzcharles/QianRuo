package com.ruoyi.merchant.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.merchant.api.RemoteRentalBusinessService;
import com.ruoyi.merchant.api.model.MerchantFinanceSettlementView;
import com.ruoyi.merchant.mapper.MerchantFinanceMapper;
import com.ruoyi.merchant.model.MerchantFinanceAccountType;
import com.ruoyi.merchant.model.MerchantFinanceAccountVO;
import com.ruoyi.merchant.model.MerchantFinanceDirection;
import com.ruoyi.merchant.model.MerchantFinanceInvoiceStatus;
import com.ruoyi.merchant.model.MerchantFinanceLedgerQuery;
import com.ruoyi.merchant.model.MerchantFinanceLedgerStatus;
import com.ruoyi.merchant.model.MerchantFinanceLedgerVO;
import com.ruoyi.merchant.model.MerchantFinanceOverviewVO;
import com.ruoyi.merchant.model.MerchantFinanceScene;

/** 商户财务中心查询服务。财务流水只读，冲正和退款由业务模块追加新流水完成。 */
@Service
public class MerchantFinanceService
{
    private static final DateTimeFormatter MONTH_START_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MerchantFinanceMapper mapper;
    private final RemoteRentalBusinessService rentalBusinessService;

    public MerchantFinanceService(MerchantFinanceMapper mapper, RemoteRentalBusinessService rentalBusinessService)
    {
        this.mapper = mapper;
        this.rentalBusinessService = rentalBusinessService;
    }

    /** 查询当前商户财务总览。 */
    public MerchantFinanceOverviewVO overview()
    {
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        Map<String, Object> source = mapper.selectOverview(tenantId, memberId,
                LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay().format(MONTH_START_FORMATTER));
        MerchantFinanceOverviewVO result = new MerchantFinanceOverviewVO();
        if (source != null)
        {
            result.setAvailableBalance(decimal(source.get("availableBalance")));
            result.setValueAddedBalance(decimal(source.get("valueAddedBalance")));
            result.setRentalSettlementBalance(decimal(source.get("rentalSettlementBalance")));
            result.setPerformanceDepositAvailable(decimal(source.get("performanceDepositAvailable")));
            result.setPerformanceDepositFrozen(decimal(source.get("performanceDepositFrozen")));
            result.setMonthIncome(decimal(source.get("monthIncome")));
            result.setMonthExpense(decimal(source.get("monthExpense")));
            result.setFrozenAmount(decimal(source.get("frozenAmount")));
            result.setPendingInvoiceCount(integer(source.get("pendingInvoiceCount")));
            result.setAbnormalLedgerCount(integer(source.get("abnormalLedgerCount")));
        }
        result.setPendingSettlementAmount(BigDecimal.ZERO);
        result.setPendingWithdrawalCount(integer(source == null ? null : source.get("pendingWithdrawalCount")));
        result.setRecentLedgers(enrich(mapper.selectRecentLedger(tenantId, memberId)));
        return result;
    }

    /** 查询分户余额。履约保证金与联盟保证金统一归入履约保证金分户。 */
    public List<MerchantFinanceAccountVO> accounts()
    {
        List<Map<String, Object>> rows = mapper.selectAccounts(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId());
        if (rows == null || rows.isEmpty()) return Collections.emptyList();
        List<MerchantFinanceAccountVO> result = new ArrayList<>();
        for (Map<String, Object> row : rows)
        {
            MerchantFinanceAccountVO item = new MerchantFinanceAccountVO();
            item.setAccountType(string(row.get("accountType")));
            item.setAccountName(string(row.get("accountName")));
            item.setAvailableAmount(decimal(row.get("availableAmount")));
            item.setFrozenAmount(decimal(row.get("frozenAmount")));
            item.setTotalAmount(item.getAvailableAmount().add(item.getFrozenAmount()));
            item.setStatusName("正常");
            item.setDescription(string(row.get("description")));
            result.add(item);
        }
        return result;
    }

    /** 查询当前商户财务流水。 */
    public List<MerchantFinanceLedgerVO> ledger(MerchantFinanceLedgerQuery query, String scene)
    {
        MerchantFinanceLedgerQuery actual = query == null ? new MerchantFinanceLedgerQuery() : query;
        if (scene != null && !MerchantFinanceScene.contains(scene))
        {
            throw new ServiceException("不支持的财务查询场景");
        }
        if (actual.getAccountType() != null && MerchantFinanceAccountType.fromCode(actual.getAccountType()) == null)
        {
            throw new ServiceException("不支持的财务账户类型");
        }
        actual.setScene(scene);
        return enrich(mapper.selectLedger(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), actual));
    }

    /** 查询单条流水详情，流水号属于当前商户时才返回。 */
    public MerchantFinanceLedgerVO ledger(String ledgerNo)
    {
        if (ledgerNo == null || ledgerNo.isBlank()) return null;
        MerchantFinanceLedgerVO row = mapper.selectLedgerByNo(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), ledgerNo.trim());
        List<MerchantFinanceLedgerVO> rows = row == null ? Collections.emptyList()
                : enrich(new ArrayList<>(Collections.singletonList(row)));
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** 查询联盟结算记录，归属校验在租赁业务服务内按当前商户范围执行。 */
    public List<MerchantFinanceSettlementView> settlements(Integer status, String beginTime, String endTime)
    {
        R<List<MerchantFinanceSettlementView>> response = rentalBusinessService.listFinanceSettlements(
                MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), status, beginTime, endTime,
                SecurityConstants.INNER);
        if (response == null || !R.isSuccess(response))
        {
            throw new ServiceException(response == null ? "租赁业务服务暂不可用" : response.getMsg());
        }
        return response.getData() == null ? Collections.emptyList() : response.getData();
    }

    private List<MerchantFinanceLedgerVO> enrich(List<MerchantFinanceLedgerVO> rows)
    {
        if (rows == null || rows.isEmpty()) return Collections.emptyList();
        rows.removeIf(item -> item == null);
        for (MerchantFinanceLedgerVO item : rows)
        {
            item.setDirectionName(MerchantFinanceDirection.labelOf(item.getDirection()));
            item.setAccountTypeName(MerchantFinanceAccountType.labelOf(item.getAccountType()));
            item.setBizTypeName(bizTypeName(item.getBizType()));
            item.setInvoiceStatusName(MerchantFinanceInvoiceStatus.labelOf(item.getInvoiceStatus()));
            item.setLedgerStatusName(MerchantFinanceLedgerStatus.labelOf(item.getLedgerStatus()));
            item.setRefType(referenceTypeName(item.getRefType()));
            item.setPayChannel(payChannelName(item.getPayChannel()));
        }
        return rows;
    }

    private String bizTypeName(String value)
    {
        if (value == null || value.isBlank()) return "其他财务业务";
        String normalized = value.toLowerCase();
        if (normalized.contains("rent") || normalized.contains("settle") || value.contains("租金") || value.contains("结算")) return "租金 / 分账";
        if (normalized.contains("express") || normalized.contains("shipping") || normalized.contains("delivery") || value.contains("快递") || value.contains("物流")) return "快递费用";
        if (normalized.contains("service") || normalized.contains("storage") || value.contains("增值") || value.contains("服务")) return "增值服务";
        if (normalized.contains("withdraw") || value.contains("提现")) return "提现";
        if (normalized.contains("compensation") || normalized.contains("repair") || value.contains("赔付")) return "赔付";
        if (normalized.contains("sale") || value.contains("出售")) return "设备出售";
        if (normalized.contains("recharge") || value.contains("充值")) return "账户充值";
        if (normalized.contains("order")) return "订单支付";
        return value.matches("\\d+") ? "其他财务业务" : value;
    }

    private String referenceTypeName(String value)
    {
        if (value == null || value.isBlank()) return null;
        String normalized = value.toLowerCase();
        if (normalized.contains("order") || value.contains("订单")) return "订单";
        if (normalized.contains("logistics") || normalized.contains("express") || value.contains("物流")) return "物流";
        if (normalized.contains("settle") || value.contains("结算")) return "联盟结算";
        return "业务关联";
    }

    private String payChannelName(String value)
    {
        if (value == null || value.isBlank()) return null;
        String normalized = value.toLowerCase();
        if (normalized.contains("alipay") || value.contains("支付宝")) return "支付宝";
        if (normalized.contains("wechat") || normalized.contains("wx") || value.contains("微信")) return "微信支付";
        if (normalized.contains("bank") || value.contains("银行")) return "银行转账";
        return value.matches("\\d+") ? "其他支付渠道" : value;
    }

    private BigDecimal decimal(Object value)
    {
        return value == null ? BigDecimal.ZERO : new BigDecimal(value.toString());
    }

    private Integer integer(Object value)
    {
        return value == null ? 0 : Integer.valueOf(value.toString());
    }

    private String string(Object value)
    {
        return value == null ? null : value.toString();
    }
}
