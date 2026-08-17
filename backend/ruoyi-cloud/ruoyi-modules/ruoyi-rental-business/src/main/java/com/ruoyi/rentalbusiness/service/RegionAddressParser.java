package com.ruoyi.rentalbusiness.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.ruoyi.rentalbusiness.mapper.RegionMapper;
import com.ruoyi.rentalbusiness.model.RegionAddressInfo;
import com.ruoyi.rentalbusiness.model.RegionNode;

/**
 * 详细地址行政区划解析公共服务。
 *
 * <p>解析器使用数据库维护的省、市、区县字典，支持带完整后缀的地址，也支持用户输入
 * “杭州”“哈尔滨”等省略“市”的常见写法。解析结果同时提供行政区划 adcode 和高德
 * citycode，订单、物流、天气等业务均通过该服务复用相同规则。</p>
 */
@Service
public class RegionAddressParser
{
    private final RegionMapper regionMapper;

    /** 使用 volatile 保证字典刷新后其他请求可立即看到新快照。 */
    private volatile List<RegionNode> regionSnapshot = Collections.emptyList();

    /**
     * 注入行政区划数据访问对象。
     *
     * @param regionMapper 行政区划 Mapper
     */
    public RegionAddressParser(RegionMapper regionMapper)
    {
        this.regionMapper = regionMapper;
    }

    /**
     * 解析详细地址中的省、市、区县和高德城市编码。
     *
     * @param address 客户详细地址
     * @return 解析结果；无法识别时返回 failed 状态，不抛出异常阻断订单保存
     */
    public RegionAddressInfo parse(String address)
    {
        if (address == null || address.trim().isEmpty())
        {
            return RegionAddressInfo.failed("详细地址为空，无法解析行政区划");
        }

        String normalizedAddress = normalizeText(address);
        List<RegionNode> nodes = getRegionSnapshot();
        Map<String, RegionNode> nodeByCode = indexByCode(nodes);
        RegionNode province = findBestNode(normalizedAddress, nodes, 1, null);
        RegionNode city = findBestNode(normalizedAddress, nodes, 2, province == null ? null : province.code);
        RegionNode district = findBestNode(normalizedAddress, nodes, 3, city != null ? city.code
                : province == null ? null : province.code);

        if (city == null && district != null)
        {
            city = nodeByCode.get(district.parentCode);
        }
        if (province == null && city != null)
        {
            province = nodeByCode.get(city.parentCode);
        }
        if (province == null && district != null)
        {
            RegionNode parent = nodeByCode.get(district.parentCode);
            if (parent != null)
            {
                province = nodeByCode.get(parent.parentCode);
            }
        }

        RegionAddressInfo result = new RegionAddressInfo();
        result.provinceCode = codeOf(province);
        result.cityCode = codeOf(city);
        result.districtCode = codeOf(district);
        result.provinceName = nameOf(province);
        result.cityName = nameOf(city);
        result.districtName = nameOf(district);
        result.amapCityCode = firstNonBlank(cityCodeOf(district), cityCodeOf(city), cityCodeOf(province));
        if (province == null && city == null && district == null)
        {
            result.parseStatus = "failed";
            result.parseMessage = "未识别出省、市或区县";
        }
        else if (result.amapCityCode == null || result.amapCityCode.trim().isEmpty())
        {
            result.parseStatus = "partial";
            result.parseMessage = "已识别行政区划，但缺少高德 citycode";
        }
        else
        {
            result.parseStatus = "success";
            result.parseMessage = "已根据详细地址识别行政区划";
        }
        return result;
    }

    /**
     * 主动刷新行政区划内存快照，供字典更新后调用。
     */
    public synchronized void refresh()
    {
        List<RegionNode> nodes = regionMapper.selectActiveRegionNodes();
        regionSnapshot = nodes == null ? Collections.emptyList() : new ArrayList<RegionNode>(nodes);
    }

    /**
     * 懒加载行政区划快照，避免服务启动时数据库短暂不可用导致整个应用无法启动。
     *
     * @return 当前有效行政区划节点
     */
    private List<RegionNode> getRegionSnapshot()
    {
        if (regionSnapshot.isEmpty())
        {
            refresh();
        }
        return regionSnapshot;
    }

    /**
     * 按行政区划编码构造父子关系索引。
     *
     * @param nodes 行政区划节点
     * @return 编码到节点的索引
     */
    private Map<String, RegionNode> indexByCode(List<RegionNode> nodes)
    {
        Map<String, RegionNode> result = new HashMap<String, RegionNode>();
        for (RegionNode node : nodes)
        {
            if (node != null && node.code != null)
            {
                result.put(node.code, node);
            }
        }
        return result;
    }

    /**
     * 在指定层级和父级范围内查找最匹配的行政区划。
     *
     * @param address 已规范化地址
     * @param nodes 行政区划节点
     * @param level 目标层级
     * @param parentCode 父级编码，可为空
     * @return 最长名称匹配节点，没有匹配时返回 null
     */
    private RegionNode findBestNode(String address, List<RegionNode> nodes, int level, String parentCode)
    {
        List<RegionNode> candidates = new ArrayList<RegionNode>();
        for (RegionNode node : nodes)
        {
            if (node != null && node.level != null && node.level == level
                    && (parentCode == null || parentCode.equals(node.parentCode))
                    && containsRegionName(address, node.name))
            {
                candidates.add(node);
            }
        }
        if (candidates.isEmpty())
        {
            return null;
        }
        Collections.sort(candidates, new Comparator<RegionNode>()
        {
            /** 长名称优先，避免“市辖区”等短名称抢占具体区县匹配。 */
            @Override
            public int compare(RegionNode left, RegionNode right)
            {
                return Integer.compare(normalizeText(right.name).length(), normalizeText(left.name).length());
            }
        });
        return candidates.get(0);
    }

    /**
     * 判断地址是否包含完整名称或去除行政后缀后的常见简称。
     *
     * @param address 已规范化地址
     * @param regionName 行政区划名称
     * @return 包含匹配返回 true
     */
    private boolean containsRegionName(String address, String regionName)
    {
        String name = normalizeText(regionName);
        if (name.isEmpty() || address.contains(name))
        {
            return !name.isEmpty();
        }
        String shortName = name.replace("特别行政区", "").replace("自治区", "")
                .replace("省", "").replace("市", "").replace("地区", "").replace("盟", "")
                .replace("区", "").replace("县", "");
        return shortName.length() >= 2 && address.contains(shortName);
    }

    /**
     * 规范化地址文本，统一全角空格和常见分隔符。
     *
     * @param value 原始文本
     * @return 规范化文本
     */
    private String normalizeText(String value)
    {
        return value == null ? "" : value.replace('　', ' ').replaceAll("\\s+", "").replace("，", ",").trim();
    }

    /**
     * 读取节点编码。
     *
     * @param node 行政区划节点
     * @return 编码或 null
     */
    private String codeOf(RegionNode node)
    {
        return node == null ? null : node.code;
    }

    /**
     * 读取节点名称。
     *
     * @param node 行政区划节点
     * @return 名称或 null
     */
    private String nameOf(RegionNode node)
    {
        return node == null ? null : node.name;
    }

    /**
     * 读取节点高德城市编码。
     *
     * @param node 行政区划节点
     * @return 高德 citycode 或 null
     */
    private String cityCodeOf(RegionNode node)
    {
        return node == null ? null : node.amapCityCode;
    }

    /**
     * 返回第一个非空字符串。
     *
     * @param values 候选字符串
     * @return 第一个非空值或 null
     */
    private String firstNonBlank(String... values)
    {
        for (String value : values)
        {
            if (value != null && !value.trim().isEmpty())
            {
                return value;
            }
        }
        return null;
    }
}
