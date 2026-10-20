package com.ruoyi.system.rental;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.system.mapper.rental.PlatformFinanceMapper;
import com.ruoyi.system.model.rental.PlatformFinanceAccountVO;
import com.ruoyi.system.model.rental.PlatformFinanceLedgerVO;
import com.ruoyi.system.model.rental.PlatformFinanceQuery;
import com.ruoyi.system.model.rental.PlatformFinanceWithdrawalReviewCommand;
import com.ruoyi.system.model.rental.PlatformFinanceWithdrawalVO;
import com.ruoyi.system.model.rental.PlatformPaymentOrderVO;
import com.ruoyi.system.service.rental.PlatformFinanceService;

/** 平台财务查询服务的汇总和中文枚举映射测试。 */
public class PlatformFinanceServiceTest
{
    @Test
    public void summaryMapsAggregates()
    {
        StubFinanceMapper mapper = new StubFinanceMapper();
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("totalIncome", new BigDecimal("100.00"));
        values.put("totalExpense", new BigDecimal("25.50"));
        values.put("netAmount", new BigDecimal("74.50"));
        values.put("merchantCount", Long.valueOf(3));
        mapper.summary = values;

        PlatformFinanceService service = new PlatformFinanceService(mapper, null);
        Assert.assertEquals(new BigDecimal("74.50"), service.summary().getNetAmount());
        Assert.assertEquals(Integer.valueOf(3), service.summary().getMerchantCount());
    }

    @Test
    public void ledgerAddsChineseLabels()
    {
        PlatformFinanceLedgerVO row = new PlatformFinanceLedgerVO();
        row.setDirection(Integer.valueOf(1));
        row.setLedgerStatus(Integer.valueOf(1));
        row.setInvoiceStatus(Integer.valueOf(0));
        row.setBizType("recharge");
        StubFinanceMapper mapper = new StubFinanceMapper();
        mapper.ledger = Collections.singletonList(row);

        PlatformFinanceLedgerVO result = new PlatformFinanceService(mapper, null)
                .ledger(new PlatformFinanceQuery()).get(0);

        Assert.assertEquals("收入", result.getDirectionName());
        Assert.assertEquals("成功", result.getLedgerStatusName());
        Assert.assertEquals("待开具", result.getInvoiceStatusName());
        Assert.assertEquals("账户充值", result.getBizTypeName());
    }

    @Test
    public void ledgerRejectsUnknownScene()
    {
        PlatformFinanceQuery query = new PlatformFinanceQuery();
        query.setScene("unsupported");

        try
        {
            new PlatformFinanceService(new StubFinanceMapper(), null).ledger(query);
            Assert.fail("unknown scene must be rejected");
        }
        catch (ServiceException expected)
        {
            Assert.assertEquals("不支持的财务页面场景", expected.getMessage());
        }
    }

    @Test
    public void reviewWithdrawal_approved_deductsFrozenAndWritesExpense()
    {
        StubFinanceMapper mapper = withdrawalMapper();
        PlatformFinanceWithdrawalReviewCommand command = new PlatformFinanceWithdrawalReviewCommand();
        command.setApproved(Boolean.TRUE);

        new PlatformFinanceService(mapper, null).reviewWithdrawal("WDL-1", command);

        Assert.assertEquals(new BigDecimal("80.00"), mapper.updatedAvailable);
        Assert.assertEquals(new BigDecimal("0.00"), mapper.updatedFrozen);
        Assert.assertEquals(Integer.valueOf(2), mapper.ledgerDirection);
        Assert.assertEquals(Integer.valueOf(1), mapper.updatedWithdrawalStatus);
    }

    @Test
    public void reviewWithdrawal_rejected_requiresRemarkAndUnfreezes()
    {
        StubFinanceMapper mapper = withdrawalMapper();
        PlatformFinanceWithdrawalReviewCommand command = new PlatformFinanceWithdrawalReviewCommand();
        command.setApproved(Boolean.FALSE);
        command.setReviewRemark("收款信息需要补充");

        new PlatformFinanceService(mapper, null).reviewWithdrawal("WDL-1", command);

        Assert.assertEquals(new BigDecimal("100.00"), mapper.updatedAvailable);
        Assert.assertEquals(new BigDecimal("0.00"), mapper.updatedFrozen);
        Assert.assertEquals(Integer.valueOf(4), mapper.ledgerDirection);
        Assert.assertEquals(Integer.valueOf(2), mapper.updatedWithdrawalStatus);
    }

    private StubFinanceMapper withdrawalMapper()
    {
        StubFinanceMapper mapper = new StubFinanceMapper();
        mapper.withdrawal.put("withdrawalId", Long.valueOf(11));
        mapper.withdrawal.put("tenantId", Long.valueOf(1));
        mapper.withdrawal.put("memberId", Long.valueOf(2));
        mapper.withdrawal.put("accountId", Long.valueOf(3));
        mapper.withdrawal.put("accountType", Integer.valueOf(1));
        mapper.withdrawal.put("amount", new BigDecimal("20.00"));
        mapper.withdrawal.put("withdrawalChannel", "ALIPAY");
        mapper.withdrawal.put("withdrawalStatus", Integer.valueOf(0));
        mapper.account.put("accountId", Long.valueOf(3));
        mapper.account.put("availableAmount", new BigDecimal("80.00"));
        mapper.account.put("frozenAmount", new BigDecimal("20.00"));
        mapper.account.put("version", Long.valueOf(1));
        return mapper;
    }

    private static class StubFinanceMapper implements PlatformFinanceMapper
    {
        private Map<String, Object> summary = Collections.emptyMap();
        private List<PlatformFinanceLedgerVO> ledger = Collections.emptyList();
        private Map<String, Object> withdrawal = new HashMap<String, Object>();
        private Map<String, Object> account = new HashMap<String, Object>();
        private BigDecimal updatedAvailable;
        private BigDecimal updatedFrozen;
        private Integer ledgerDirection;
        private Integer updatedWithdrawalStatus;

        @Override
        public int countPendingWithdrawals() { return 0; }

        @Override
        public Map<String, Object> selectSummary(String monthStart) { return summary; }

        @Override
        public List<PlatformFinanceAccountVO> selectAccounts(PlatformFinanceQuery query) { return Collections.emptyList(); }

        @Override
        public List<PlatformFinanceLedgerVO> selectLedger(PlatformFinanceQuery query) { return ledger; }

        @Override
        public PlatformFinanceLedgerVO selectLedgerByNo(String ledgerNo) { return null; }

        @Override
        public List<PlatformPaymentOrderVO> selectPaymentOrders(PlatformFinanceQuery query) { return Collections.emptyList(); }

        @Override
        public PlatformPaymentOrderVO selectPaymentOrderByNo(String paymentNo) { return null; }

        @Override
        public List<PlatformFinanceWithdrawalVO> selectWithdrawals(PlatformFinanceQuery query) { return Collections.emptyList(); }

        @Override
        public PlatformFinanceWithdrawalVO selectWithdrawalByNo(String withdrawalNo) { return new PlatformFinanceWithdrawalVO(); }

        @Override
        public Map<String, Object> selectWithdrawalForUpdate(String withdrawalNo) { return withdrawal; }

        @Override
        public Map<String, Object> selectAccountForUpdate(Long tenantId, Long memberId, Integer accountType) { return account; }

        @Override
        public int updateAccount(Long accountId, BigDecimal availableAmount, BigDecimal frozenAmount, Long version,
                Long operatorId)
        {
            updatedAvailable = availableAmount;
            updatedFrozen = frozenAmount;
            return 1;
        }

        @Override
        public int updateWithdrawal(Long withdrawalId, Integer withdrawalStatus, String reviewRemark, Long reviewerId)
        {
            updatedWithdrawalStatus = withdrawalStatus;
            return 1;
        }

        @Override
        public int insertWithdrawalLedger(Long tenantId, Long memberId, String ledgerNo, Integer direction,
                BigDecimal amount, BigDecimal balanceAfter, Long refId, String payChannel, String payOrderNo,
                String remark, Long operatorId, Integer accountType)
        {
            ledgerDirection = direction;
            return 1;
        }
    }
}
