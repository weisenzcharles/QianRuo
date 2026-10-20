package com.ruoyi.rentalbusiness.service;

import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.system.api.model.PlatformAfterSalesEventCommand;
import com.ruoyi.system.api.model.PlatformAfterSalesEventView;
import com.ruoyi.system.api.model.PlatformAfterSalesEvidenceCommand;
import com.ruoyi.system.api.model.PlatformAfterSalesEvidenceView;
import com.ruoyi.system.api.model.PlatformAfterSalesQuery;
import com.ruoyi.system.api.model.PlatformAfterSalesView;
import com.ruoyi.rentalbusiness.mapper.PlatformAfterSalesMapper;

/** 平台售后案件状态推进和订单完成联动测试。 */
public class PlatformAfterSalesServiceTest
{
    @Test
    public void listDecoratesChineseCaseLabels()
    {
        StubMapper mapper = new StubMapper();
        PlatformAfterSalesView row = mapper.caseRow();
        mapper.rows = Collections.singletonList(row);

        PlatformAfterSalesService service = new PlatformAfterSalesService(mapper);
        Assert.assertEquals("损坏", service.list(new PlatformAfterSalesQuery()).getRows().get(0).getCaseTypeName());
        Assert.assertEquals("处理中", service.list(new PlatformAfterSalesQuery()).getRows().get(0).getCaseStatusName());
    }

    @Test
    public void completeLastCaseUpdatesOriginalOrderAndWritesEvent()
    {
        StubMapper mapper = new StubMapper();
        PlatformAfterSalesService service = new PlatformAfterSalesService(mapper);
        PlatformAfterSalesEventCommand command = new PlatformAfterSalesEventCommand();
        command.setToStatus(Integer.valueOf(1));
        command.setContent("已完成赔付并确认设备归还");

        service.appendEvent(Long.valueOf(11L), command, Long.valueOf(99L), "平台管理员");

        Assert.assertEquals(Integer.valueOf(1), mapper.updatedCaseStatus);
        Assert.assertEquals(Integer.valueOf(2), mapper.eventType);
        Assert.assertEquals(Integer.valueOf(70), mapper.updatedOrderFrom);
        Assert.assertEquals(Integer.valueOf(50), mapper.updatedOrderTo);
        Assert.assertEquals(Long.valueOf(99L), mapper.operatorId);
        Assert.assertEquals("平台管理员", mapper.operatorName);
        Assert.assertEquals("订单状态", mapper.editFieldName);
    }

    @Test
    public void completedCaseCannotBeProcessedAgain()
    {
        StubMapper mapper = new StubMapper();
        mapper.caseStatus = Integer.valueOf(1);
        PlatformAfterSalesService service = new PlatformAfterSalesService(mapper);
        PlatformAfterSalesEventCommand command = new PlatformAfterSalesEventCommand();
        command.setContent("重复处理");

        try
        {
            service.appendEvent(Long.valueOf(11L), command, Long.valueOf(99L), "平台管理员");
            Assert.fail("completed case must not be processed again");
        }
        catch (ServiceException expected)
        {
            Assert.assertEquals("已完成或已关闭的售后案件不能继续处理", expected.getMessage());
        }
    }

    @Test
    public void addEvidenceWritesEvidenceAndTimeline()
    {
        StubMapper mapper = new StubMapper();
        PlatformAfterSalesService service = new PlatformAfterSalesService(mapper);
        PlatformAfterSalesEvidenceCommand command = new PlatformAfterSalesEvidenceCommand();
        command.setEvidenceType(Integer.valueOf(1));
        command.setFileUrl("https://oss.example.com/after-sales/photo.jpg");
        command.setDescription("平台复核照片");

        service.addEvidence(Long.valueOf(11L), command, Long.valueOf(99L), "平台管理员");

        Assert.assertEquals(1, mapper.evidenceInsertCount);
        Assert.assertEquals("photo.jpg", mapper.fileName);
        Assert.assertEquals(Integer.valueOf(4), mapper.eventType);
        Assert.assertEquals("平台复核照片", mapper.eventContent);
    }

    private static class StubMapper implements PlatformAfterSalesMapper
    {
        private List<PlatformAfterSalesView> rows = Collections.emptyList();
        private Integer caseStatus = Integer.valueOf(0);
        private Integer updatedCaseStatus;
        private Integer eventType;
        private Integer updatedOrderFrom;
        private Integer updatedOrderTo;
        private Long operatorId;
        private String operatorName;
        private String editFieldName;
        private int evidenceInsertCount;
        private String fileName;
        private String eventContent;

        @Override public long countCases(PlatformAfterSalesQuery query) { return rows.size(); }
        @Override public List<PlatformAfterSalesView> selectCases(PlatformAfterSalesQuery query, int offset, int pageSize) { return rows; }
        @Override public PlatformAfterSalesView selectCase(Long caseId) { return caseRow(); }
        @Override public PlatformAfterSalesView selectCaseForUpdate(Long caseId) { return caseRow(); }
        @Override public List<PlatformAfterSalesView> selectCasesByOrder(Long tenantId, Long memberId, Long orderId) { return rows; }
        @Override public List<PlatformAfterSalesEventView> selectEvents(Long tenantId, Long memberId, Long caseId) { return Collections.emptyList(); }
        @Override public List<PlatformAfterSalesEvidenceView> selectEvidence(Long tenantId, Long memberId, Long caseId) { return Collections.emptyList(); }
        @Override public int updateCaseStatus(Long tenantId, Long memberId, Long caseId, Integer fromStatus, Integer toStatus, Long operatorId, String remark)
        { updatedCaseStatus = toStatus; return 1; }
        @Override public int insertEvent(Long tenantId, Long memberId, Long caseId, Integer eventType, Integer fromStatus, Integer toStatus, String content, Long operatorId, String operatorName)
        { this.eventType = eventType; this.eventContent = content; this.operatorId = operatorId; this.operatorName = operatorName; return 1; }
        @Override public int insertEvidence(Long tenantId, Long memberId, Long caseId, Integer evidenceType, String fileUrl, String fileName, String fileMime, String description, Long operatorId, String operatorName)
        { evidenceInsertCount++; this.fileName = fileName; return 1; }
        @Override public int countProcessingByOrder(Long tenantId, Long memberId, Long orderId) { return 0; }
        @Override public int updateOrderStatus(Long tenantId, Long memberId, Long orderId, Integer fromStatus, Integer toStatus, Long operatorId)
        { updatedOrderFrom = fromStatus; updatedOrderTo = toStatus; return 1; }
        @Override public int insertOrderEditLog(Long tenantId, Long memberId, Long orderId, String fieldName, String beforeValue, String afterValue, String beforeDisplayValue, String afterDisplayValue, String changeReason, Long operatorId, String operatorName)
        { editFieldName = fieldName; return 1; }

        private PlatformAfterSalesView caseRow()
        {
            PlatformAfterSalesView row = new PlatformAfterSalesView();
            row.setCaseId(Long.valueOf(11L));
            row.setTenantId(Long.valueOf(1L));
            row.setMemberId(Long.valueOf(2L));
            row.setOrderId(Long.valueOf(3L));
            row.setCaseType(Integer.valueOf(1));
            row.setCaseStatus(caseStatus);
            return row;
        }
    }
}
