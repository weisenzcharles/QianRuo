package com.ruoyi.rentalbusiness.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.rentalbusiness.mapper.RegionMapper;
import com.ruoyi.rentalbusiness.model.RegionAddressInfo;
import com.ruoyi.rentalbusiness.model.RegionNode;
import com.ruoyi.rentalbusiness.model.RegionWeatherRegion;

/**
 * 详细地址行政区划解析器单元测试。
 *
 * <p>测试使用最小化的内存字典替代真实数据库，验证解析器的层级约束、父级回溯和高德
 * citycode 快照保留和天气 adcode 解析，避免测试依赖本地 MySQL 数据内容。</p>
 */
public class RegionAddressParserTest
{
    /**
     * 验证完整省市区地址能够返回行政区划编码和高德城市编码。
     */
    @Test
    public void shouldParseCompleteAddress()
    {
        RegionAddressInfo result = createParser().parse("黑龙江省哈尔滨市香坊区学府路1号");

        Assert.assertEquals("success", result.parseStatus);
        Assert.assertEquals("230000", result.provinceCode);
        Assert.assertEquals("230100", result.cityCode);
        Assert.assertEquals("230110", result.districtCode);
        Assert.assertEquals("0451", result.amapCityCode);
        Assert.assertEquals("230100", result.weatherAdcode());
        Assert.assertEquals("香坊区", result.districtName);
    }

    /**
     * 验证直辖市地址能够通过虚拟市级节点正确补齐省、市、区三级编码。
     */
    @Test
    public void shouldParseMunicipalityAddress()
    {
        RegionAddressInfo result = createParser().parse("北京市东城区东华门街道");

        Assert.assertEquals("success", result.parseStatus);
        Assert.assertEquals("110000", result.provinceCode);
        Assert.assertEquals("110100", result.cityCode);
        Assert.assertEquals("110101", result.districtCode);
        Assert.assertEquals("010", result.amapCityCode);
        Assert.assertEquals("110100", result.weatherAdcode());
    }

    /**
     * 验证空地址不会触发数据库异常，而是返回可供业务记录的失败状态。
     */
    @Test
    public void shouldReturnFailedStatusForBlankAddress()
    {
        RegionAddressInfo result = createParser().parse("  ");

        Assert.assertEquals("failed", result.parseStatus);
        Assert.assertNull(result.provinceCode);
        Assert.assertTrue(result.parseMessage.contains("为空"));
    }

    /** 只有省级编码时不请求省级天气，避免绕过市级天气快照范围。 */
    @Test
    public void weatherAdcodeShouldReturnNullWhenCityCodeMissing()
    {
        RegionAddressInfo result = new RegionAddressInfo();
        result.provinceCode = "230000";

        Assert.assertNull(result.weatherAdcode());
    }

    /**
     * 创建使用测试字典的解析器实例。
     *
     * @return 地址解析器
     */
    private RegionAddressParser createParser()
    {
        return new RegionAddressParser(new StubRegionMapper());
    }

    /**
     * 为解析器提供固定的省、市、区县最小字典。
     */
    private static class StubRegionMapper implements RegionMapper
    {
        private final List<RegionNode> nodes = Arrays.asList(
                node("230000", "0", 1, "黑龙江省", null),
                node("230100", "230000", 2, "哈尔滨市", "0451"),
                node("230110", "230100", 3, "香坊区", "0451"),
                node("110000", "0", 1, "北京市", "010"),
                node("110100", "110000", 2, "北京市", "010"),
                node("110101", "110100", 3, "东城区", "010"));

        /**
         * 实现仓库级联查询契约；本测试只验证全量快照，因此返回空列表。
         *
         * @param parentCode 父级编码
         * @return 空级联结果
         */
        @Override
        public List<Map<String, Object>> selectChildren(String parentCode)
        {
            return new ArrayList<Map<String, Object>>();
        }

        /**
         * 返回测试用的省、市、区县节点。
         *
         * @return 测试行政区划节点
         */
        @Override
        public List<RegionNode> selectActiveRegionNodes()
        {
            return nodes;
        }

        /**
         * 实现天气任务的区县查询契约；地址解析测试只使用省、市、区县内存节点。
         *
         * @return 空的天气行政区划列表
         */
        @Override
        public List<RegionWeatherRegion> selectWeatherRegions()
        {
            return new ArrayList<RegionWeatherRegion>();
        }

        /**
         * 构造一条测试行政区划记录。
         *
         * @param code 行政区划编码
         * @param parentCode 父级编码
         * @param level 层级
         * @param name 行政区划名称
         * @param amapCityCode 高德城市编码
         * @return 行政区划节点
         */
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
