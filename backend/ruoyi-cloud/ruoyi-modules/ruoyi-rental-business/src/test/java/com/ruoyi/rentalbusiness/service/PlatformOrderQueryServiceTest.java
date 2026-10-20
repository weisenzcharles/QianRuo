package com.ruoyi.rentalbusiness.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import com.ruoyi.rentalbusiness.domain.MerchantOrderRow;
import com.ruoyi.rentalbusiness.mapper.PlatformOrderMapper;
import com.ruoyi.rentalbusiness.model.MerchantOrderEditLogVO;
import com.ruoyi.system.api.model.PlatformOrderDetailView;
import com.ruoyi.system.api.model.PlatformOrderEditLogView;
import com.ruoyi.system.api.model.PlatformOrderItemView;
import com.ruoyi.system.api.model.PlatformOrderPageView;
import com.ruoyi.system.api.model.PlatformOrderReturnInspectionView;
import com.ruoyi.system.api.model.PlatformOrderView;

/** 平台跨商户订单分页查询的边界测试。 */
public class PlatformOrderQueryServiceTest
{
    /** 非法页码回落到第一页，每页数量限制在一百条。 */
    @Test
    public void listNormalizesPageBoundaryAndPreservesTotal()
    {
        CapturingMapper mapper = new CapturingMapper();
        mapper.total = 17L;
        mapper.rows = Collections.singletonList(new PlatformOrderView());

        PlatformOrderPageView page = new PlatformOrderQueryService(mapper)
                .list(null, "  ORD ", null, "2026-09-01", "2026-09-30", 0, 200);

        require(page.getTotal() == 17L, "total must come from the unpaged count query");
        require(mapper.offset == 0, "invalid page number must use the first page");
        require(mapper.pageSize == 100, "page size must be capped at one hundred");
        require("ORD".equals(mapper.keyword), "keyword must be trimmed before querying");
        require("2026-09-01".equals(mapper.beginTime), "begin date must be passed to the mapper");
        require("2026-09-30".equals(mapper.endTime), "end date must be passed to the mapper");
    }

    /** 商铺搜索无匹配时直接返回空页，避免空集合退化成查询全部订单。 */
    @Test
    public void listReturnsEmptyPageForEmptyMerchantScope()
    {
        CapturingMapper mapper = new CapturingMapper();

        PlatformOrderPageView page = new PlatformOrderQueryService(mapper)
                .list(Collections.<Long>emptyList(), null, null, null, null, 1, 10);

        require(page.getTotal() == 0L, "empty merchant scope must return zero total");
        require(page.getRows().isEmpty(), "empty merchant scope must return no rows");
        require(!mapper.countCalled, "empty merchant scope must not query all orders");
    }

    /** 日期区间反向或状态编码非法时拒绝查询，避免把错误条件交给数据库。 */
    @Test
    public void listRejectsInvalidFilterValues()
    {
        CapturingMapper mapper = new CapturingMapper();
        assertServiceException(() -> new PlatformOrderQueryService(mapper)
                .list(null, null, 999, "2026-09-02", "2026-09-01", 1, 10));
        require(!mapper.countCalled, "invalid filters must not query the database");
    }

    /** 订单快照优先作为详情明细来源，并保留序列号和库存编码。 */
    @Test
    public void detailUsesPackageSnapshotItems()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderRow row = new MerchantOrderRow();
        row.orderId = 1L;
        row.tenantId = 1L;
        row.memberId = 1L;
        row.packageDetailsJson = "{\"items\":[{\"itemRole\":1,\"itemName\":\"EOS R50\","
                + "\"itemCode\":\"P-001\",\"quantity\":1,\"serialNumbers\":[\"SN-001\"],"
                + "\"unitCodes\":[\"INV-001\"]}]}";
        mapper.detailRow = row;
        PlatformOrderReturnInspectionView inspection = new PlatformOrderReturnInspectionView();
        inspection.setResult(1);
        mapper.returnInspection = inspection;

        PlatformOrderDetailView detail = new PlatformOrderQueryService(mapper).detail(1L);

        require(detail.getItems().size() == 1, "snapshot item must be returned");
        PlatformOrderItemView item = detail.getItems().get(0);
        require("产品".equals(item.getItemTypeName()), "product role must use the Chinese label");
        require("EOS R50".equals(item.getItemName()), "snapshot item name must be preserved");
        require(item.getSerialNumbers().contains("SN-001"), "serial number must be preserved");
        require(item.getInventoryCodes().contains("INV-001"), "inventory code must be preserved");
        require(detail.getReturnInspection() != null, "return inspection must be returned for platform detail");
        require("验机正常".equals(detail.getReturnInspection().getResultLabel()),
                "return inspection result must use the Chinese label");
    }

    /** 平台订单详情把状态变更日志单独组装为履约时间线。 */
    @Test
    public void detailBuildsFulfillmentTimelineFromStatusLogs()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderRow row = new MerchantOrderRow();
        row.orderId = 2L;
        row.tenantId = 1L;
        row.memberId = 1L;
        mapper.detailRow = row;
        MerchantOrderEditLogVO editLog = new MerchantOrderEditLogVO();
        editLog.fieldName = "订单状态";
        editLog.beforeDisplayValue = "待发货";
        editLog.afterDisplayValue = "已备货";
        editLog.changeReason = "订单备货";
        editLog.operatorName = "豆豆";
        editLog.createTime = new Date(0L);
        mapper.editLogs = Collections.singletonList(editLog);

        PlatformOrderDetailView detail = new PlatformOrderQueryService(mapper).detail(2L);

        require(detail.getFulfillmentEvents().size() == 1, "status logs must become fulfillment events");
        require("待发货".equals(detail.getFulfillmentEvents().get(0).getFromStatusLabel()),
                "fulfillment event must keep the previous status label");
        require("已备货".equals(detail.getFulfillmentEvents().get(0).getToStatusLabel()),
                "fulfillment event must keep the next status label");
    }

    private static void assertServiceException(Runnable action)
    {
        try
        {
            action.run();
            throw new AssertionError("expected ServiceException");
        }
        catch (com.ruoyi.common.core.exception.ServiceException expected)
        {
            // expected
        }
    }

    private static void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }

    private static final class CapturingMapper implements PlatformOrderMapper
    {
        private boolean countCalled;
        private long total;
        private String keyword;
        private String beginTime;
        private String endTime;
        private int offset = -1;
        private int pageSize = -1;
        private List<PlatformOrderView> rows = Collections.emptyList();
        private MerchantOrderRow detailRow;
        private PlatformOrderReturnInspectionView returnInspection;
        private List<MerchantOrderEditLogVO> editLogs = Collections.emptyList();

        @Override
        public long countPlatformOrders(List<Long> memberIds, String keyword, Integer orderStatus, String beginTime,
                String endTime)
        {
            countCalled = true;
            this.keyword = keyword;
            this.beginTime = beginTime;
            this.endTime = endTime;
            return total;
        }

        @Override
        public List<PlatformOrderView> selectPlatformOrders(List<Long> memberIds, String keyword, Integer orderStatus,
                String beginTime, String endTime, int offset, int pageSize)
        {
            this.offset = offset;
            this.pageSize = pageSize;
            return rows;
        }

        @Override
        public MerchantOrderRow selectPlatformOrderDetail(Long orderId)
        {
            return detailRow;
        }

        @Override
        public List<PlatformOrderItemView> selectPlatformOrderItems(Long orderId)
        {
            return Collections.emptyList();
        }

        @Override
        public PlatformOrderReturnInspectionView selectPlatformOrderReturnInspection(Long orderId)
        {
            return returnInspection;
        }

        @Override
        public List<com.ruoyi.rentalbusiness.model.MerchantOrderNoteVO> selectPlatformOrderNotes(Long orderId)
        {
            return Collections.emptyList();
        }

        @Override
        public List<com.ruoyi.rentalbusiness.model.MerchantOrderEditLogVO> selectPlatformOrderEditLogs(Long orderId)
        {
            return editLogs;
        }
    }
}
