package com.ruoyi.system.rental;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.system.mapper.rental.PlatformMemberRiskMapper;
import com.ruoyi.system.model.rental.PlatformMemberBlacklistRecordVO;
import com.ruoyi.system.model.rental.PlatformMemberBlacklistReleaseCommand;
import com.ruoyi.system.model.rental.PlatformMemberCreditRecordCommand;
import com.ruoyi.system.model.rental.PlatformMemberCreditRecordVO;
import com.ruoyi.system.model.rental.PlatformMemberCreditReversalCommand;
import com.ruoyi.system.model.rental.PlatformMemberRiskScope;
import com.ruoyi.system.service.rental.PlatformMemberRiskService;

/** 平台商户信用与黑名单服务测试。 */
public class PlatformMemberRiskServiceTest
{
    @Test
    public void addCreditRecord_clampsScoreAndWritesAuditRecord()
    {
        StubRiskMapper mapper = new StubRiskMapper();
        mapper.account.put("creditScore", Integer.valueOf(95));
        PlatformMemberCreditRecordCommand command = new PlatformMemberCreditRecordCommand();
        command.setEventType(Integer.valueOf(1));
        command.setScoreChange(Integer.valueOf(-50));
        command.setReason("履约逾期，平台人工调整");
        command.setSourceNo("OD0000000007");

        SecurityContextHolder.setUserId("7");
        try
        {
            new PlatformMemberRiskService(mapper).addCreditRecord(Long.valueOf(2), command);
        }
        finally
        {
            SecurityContextHolder.remove();
        }

        Assert.assertEquals(Integer.valueOf(45), mapper.updatedScore);
        Assert.assertEquals(Integer.valueOf(95), mapper.recordBefore);
        Assert.assertEquals(Integer.valueOf(-50), mapper.recordChange);
        Assert.assertEquals(Integer.valueOf(45), mapper.recordAfter);
        Assert.assertEquals("OD0000000007", mapper.recordSourceNo);
    }

    @Test
    public void addBlacklist_whenActiveRecordExists_rejectsDuplicate()
    {
        StubRiskMapper mapper = new StubRiskMapper();
        mapper.activeBlacklistCount = 1;
        com.ruoyi.system.model.rental.PlatformMemberBlacklistCommand command =
                new com.ruoyi.system.model.rental.PlatformMemberBlacklistCommand();
        command.setReasonType(Integer.valueOf(2));
        command.setReason("存在重大履约风险");

        try
        {
            new PlatformMemberRiskService(mapper).addBlacklist(Long.valueOf(2), command);
            Assert.fail("重复加入黑名单必须被拒绝");
        }
        catch (ServiceException expected)
        {
            Assert.assertEquals("该商户已有生效中的黑名单记录", expected.getMessage());
        }
    }

    @Test
    public void reverseCreditRecord_writesOppositeImmutableRecord()
    {
        StubRiskMapper mapper = new StubRiskMapper();
        mapper.account.put("creditScore", Integer.valueOf(45));
        mapper.creditRecord.put("scoreBefore", Integer.valueOf(55));
        mapper.creditRecord.put("scoreAfter", Integer.valueOf(45));
        mapper.creditRecord.put("scoreChange", Integer.valueOf(-20));
        mapper.creditRecord.put("sourceNo", "OD0000000007");
        PlatformMemberCreditReversalCommand command = new PlatformMemberCreditReversalCommand();
        command.setReason("申诉审核通过，原履约处罚证据不足");

        SecurityContextHolder.setUserId("7");
        try
        {
            new PlatformMemberRiskService(mapper).reverseCreditRecord(Long.valueOf(2), "CR-ORIGINAL", command);
        }
        finally
        {
            SecurityContextHolder.remove();
        }

        Assert.assertEquals(Integer.valueOf(55), mapper.updatedScore);
        Assert.assertEquals(Integer.valueOf(6), mapper.recordEventType);
        Assert.assertEquals(Integer.valueOf(10), mapper.recordChange);
        Assert.assertEquals("CR-ORIGINAL", mapper.recordReversalOfRecordNo);
        Assert.assertEquals("OD0000000007", mapper.recordSourceNo);
    }

    @Test
    public void reverseCreditRecord_whenAlreadyReversed_rejectsDuplicate()
    {
        StubRiskMapper mapper = new StubRiskMapper();
        mapper.creditReversalCount = 1;
        PlatformMemberCreditReversalCommand command = new PlatformMemberCreditReversalCommand();
        command.setReason("重复申诉");

        try
        {
            new PlatformMemberRiskService(mapper).reverseCreditRecord(Long.valueOf(2), "CR-ORIGINAL", command);
            Assert.fail("同一信用流水不能重复冲正");
        }
        catch (ServiceException expected)
        {
            Assert.assertEquals("该信用流水已经冲正，不能重复操作", expected.getMessage());
        }
        Assert.assertNull(mapper.updatedScore);
    }

    @Test
    public void releaseBlacklist_updatesActiveRecordAndKeepsHistory()
    {
        StubRiskMapper mapper = new StubRiskMapper();
        mapper.blacklistRecord.put("recordId", Long.valueOf(11));
        mapper.blacklistRecord.put("blacklistStatus", Integer.valueOf(1));
        PlatformMemberBlacklistReleaseCommand command = new PlatformMemberBlacklistReleaseCommand();
        command.setRemark("复核完成，风险已消除");

        SecurityContextHolder.setUserId("7");
        try
        {
            new PlatformMemberRiskService(mapper).releaseBlacklist(Long.valueOf(2), "BL-20260928-1", command);
        }
        finally
        {
            SecurityContextHolder.remove();
        }

        Assert.assertEquals(Long.valueOf(11), mapper.releasedRecordId);
        Assert.assertEquals("复核完成，风险已消除", mapper.releaseRemark);
    }

    private static class StubRiskMapper implements PlatformMemberRiskMapper
    {
        private final PlatformMemberRiskScope scope = scope();
        private final Map<String, Object> account = new HashMap<String, Object>();
        private final Map<String, Object> blacklistRecord = new HashMap<String, Object>();
        private final Map<String, Object> creditRecord = new HashMap<String, Object>();
        private final List<PlatformMemberCreditRecordVO> creditRecords = new ArrayList<PlatformMemberCreditRecordVO>();
        private final List<PlatformMemberBlacklistRecordVO> blacklistRecords = new ArrayList<PlatformMemberBlacklistRecordVO>();
        private int activeBlacklistCount;
        private int creditReversalCount;
        private Integer updatedScore;
        private Integer recordEventType;
        private Integer recordBefore;
        private Integer recordChange;
        private Integer recordAfter;
        private String recordSourceNo;
        private String recordReversalOfRecordNo;
        private Long releasedRecordId;
        private String releaseRemark;

        @Override
        public PlatformMemberRiskScope selectMemberScope(Long memberId)
        {
            return memberId == null ? null : scope;
        }

        @Override
        public int insertCreditAccountIfAbsent(Long tenantId, Long memberId, Long operatorId)
        {
            return 1;
        }

        @Override
        public Map<String, Object> selectCreditAccountForUpdate(Long tenantId, Long memberId)
        {
            return account;
        }

        @Override
        public int updateCreditAccount(Long tenantId, Long memberId, Integer score, Integer level, Long operatorId)
        {
            updatedScore = score;
            account.put("creditScore", score);
            account.put("creditLevel", level);
            return 1;
        }

        @Override
        public int insertCreditRecord(Long tenantId, Long memberId, String recordNo, Integer eventType,
                Integer scoreBefore, Integer scoreChange, Integer scoreAfter, String reason, String sourceNo,
                String reversalOfRecordNo, Long operatorId)
        {
            recordEventType = eventType;
            recordBefore = scoreBefore;
            recordChange = scoreChange;
            recordAfter = scoreAfter;
            recordSourceNo = sourceNo;
            recordReversalOfRecordNo = reversalOfRecordNo;
            return 1;
        }

        @Override
        public Map<String, Object> selectCreditRecordForUpdate(Long tenantId, Long memberId, String recordNo)
        {
            return creditRecord;
        }

        @Override
        public int countCreditReversal(Long tenantId, Long memberId, String recordNo)
        {
            return creditReversalCount;
        }

        @Override
        public List<PlatformMemberCreditRecordVO> selectCreditRecords(Long tenantId, Long memberId)
        {
            return creditRecords;
        }

        @Override
        public List<PlatformMemberBlacklistRecordVO> selectBlacklistRecords(Long tenantId, Long memberId)
        {
            return blacklistRecords;
        }

        @Override
        public int countActiveBlacklist(Long tenantId, Long memberId)
        {
            return activeBlacklistCount;
        }

        @Override
        public int insertBlacklistRecord(Long tenantId, Long memberId, String recordNo, Integer reasonType,
                String reason, String expiresTime, Long operatorId)
        {
            return 1;
        }

        @Override
        public Map<String, Object> selectBlacklistForUpdate(Long tenantId, Long memberId, String recordNo)
        {
            return blacklistRecord;
        }

        @Override
        public int releaseBlacklist(Long recordId, String remark, Long operatorId)
        {
            releasedRecordId = recordId;
            releaseRemark = remark;
            return 1;
        }

        private static PlatformMemberRiskScope scope()
        {
            PlatformMemberRiskScope scope = new PlatformMemberRiskScope();
            scope.setTenantId(Long.valueOf(1));
            scope.setMemberId(Long.valueOf(2));
            return scope;
        }
    }
}
