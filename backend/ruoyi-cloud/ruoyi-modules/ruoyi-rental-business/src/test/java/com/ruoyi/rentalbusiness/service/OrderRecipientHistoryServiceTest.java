package com.ruoyi.rentalbusiness.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.rentalbusiness.domain.OrderRecipientHistoryRecord;
import com.ruoyi.rentalbusiness.mapper.OrderRecipientHistoryMapper;
import com.ruoyi.rentalbusiness.mapper.RegionMapper;
import com.ruoyi.rentalbusiness.model.RegionNode;
import com.ruoyi.rentalbusiness.model.RegionWeatherRegion;

/** 订单收件人历史快照的去重和当前地址切换测试。 */
public class OrderRecipientHistoryServiceTest
{
    /**
     * 验证重复保存完全相同的收件人信息时不新增历史记录，也不修改当前地址标识。
     */
    @Test
    public void shouldSkipInsertWhenRecipientInfoDoesNotChange()
    {
        CapturingMapper mapper = new CapturingMapper(currentRecord());
        OrderRecipientHistoryService service = new OrderRecipientHistoryService(mapper, createParser());

        service.record(10001L, 27L, 99L, "ORDER-001", "张三", "13800000000",
                "浙江省杭州市西湖区文三路1号", 7L);

        Assert.assertEquals(0, mapper.clearUsedCount);
        Assert.assertEquals(0, mapper.insertCount);
    }

    /**
     * 验证收件人信息发生变化时，旧地址先失效并新增一条当前使用地址快照。
     */
    @Test
    public void shouldSwitchUsedSnapshotWhenRecipientInfoChanges()
    {
        CapturingMapper mapper = new CapturingMapper(currentRecord());
        OrderRecipientHistoryService service = new OrderRecipientHistoryService(mapper, createParser());

        service.record(10001L, 27L, 99L, "ORDER-001", "李四", "13900000000",
                "浙江省杭州市西湖区文三路2号", 8L);

        Assert.assertEquals(1, mapper.clearUsedCount);
        Assert.assertEquals(1, mapper.insertCount);
        Assert.assertEquals(Integer.valueOf(1), mapper.insertedRecord.used);
        Assert.assertEquals("李四", mapper.insertedRecord.contactName);
    }

    /** 构造包含完整省、市、区县层级的测试地址解析器。 */
    private RegionAddressParser createParser()
    {
        return new RegionAddressParser(new StubRegionMapper());
    }

    /** 构造订单当前正在使用的收件人快照。 */
    private OrderRecipientHistoryRecord currentRecord()
    {
        OrderRecipientHistoryRecord record = new OrderRecipientHistoryRecord();
        record.contactName = "张三";
        record.contactPhone = "13800000000";
        record.contactAddress = "浙江省杭州市西湖区文三路1号";
        record.provinceCode = "330000";
        record.cityCode = "330100";
        record.districtCode = "330106";
        record.amapCityCode = "0571";
        record.provinceName = "浙江省";
        record.cityName = "杭州市";
        record.districtName = "西湖区";
        record.parseStatus = "success";
        record.used = 1;
        return record;
    }

    /** 记录收件人历史 Mapper 调用次数和新增快照内容的测试替身。 */
    private static class CapturingMapper implements OrderRecipientHistoryMapper
    {
        private final OrderRecipientHistoryRecord current;
        private int clearUsedCount;
        private int insertCount;
        private OrderRecipientHistoryRecord insertedRecord;

        /** 使用指定当前快照初始化测试替身。 */
        private CapturingMapper(OrderRecipientHistoryRecord current)
        {
            this.current = current;
        }

        /** 返回当前订单正在使用的地址快照。 */
        @Override
        public OrderRecipientHistoryRecord selectLatest(Long tenantId, Long memberId, String orderNo)
        {
            return current;
        }

        /** 记录旧当前地址被标记为历史地址。 */
        @Override
        public int clearUsedSnapshots(Long tenantId, Long memberId, String orderNo, Long operatorId)
        {
            clearUsedCount++;
            return 1;
        }

        /** 记录新当前地址快照，模拟数据库写入。 */
        @Override
        public int insertSnapshot(Long tenantId, Long memberId, Long orderId, String orderNo,
                OrderRecipientHistoryRecord record, Long operatorId)
        {
            insertCount++;
            record.used = 1;
            insertedRecord = record;
            return 1;
        }
    }

    /** 为地址解析器提供最小行政区划字典。 */
    private static class StubRegionMapper implements RegionMapper
    {
        private final List<RegionNode> nodes = Arrays.asList(
                node("330000", "0", 1, "浙江省", null),
                node("330100", "330000", 2, "杭州市", "0571"),
                node("330106", "330100", 3, "西湖区", "0571"));

        /** 地址级联查询在本测试中不参与验证。 */
        @Override
        public List<Map<String, Object>> selectChildren(String parentCode)
        {
            return Collections.emptyList();
        }

        /** 返回测试用的省、市、区县三级字典。 */
        @Override
        public List<RegionNode> selectActiveRegionNodes()
        {
            return nodes;
        }

        /**
         * 实现天气任务的区县查询契约；收件人历史快照测试不依赖天气行政区划投影。
         *
         * @return 空的天气行政区划列表
         */
        @Override
        public List<RegionWeatherRegion> selectWeatherRegions()
        {
            return Collections.emptyList();
        }

        /** 构造一条测试行政区划节点。 */
        private static RegionNode node(String code, String parentCode, int level, String name,
                String amapCityCode)
        {
            RegionNode node = new RegionNode();
            node.code = code;
            node.parentCode = parentCode;
            node.level = level;
            node.name = name;
            node.amapCityCode = amapCityCode;
            return node;
        }
    }
}
