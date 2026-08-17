package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.rentalbusiness.model.RegionWeatherRegion;
import com.ruoyi.rentalbusiness.model.RegionNode;

/** 行政区划数据访问接口。 */
public interface RegionMapper
{
    /** 查询指定父级下的有效行政区划。 */
    List<Map<String, Object>> selectChildren(@Param("parentCode") String parentCode);

    /** 查询省、市、区县三级全部有效节点，供地址解析器构建内存索引。 */
    List<RegionNode> selectActiveRegionNodes();

    /**
     * 查询所有有效市级行政区划，供每日天气任务批量获取预报。
     *
     * @return 市级天气任务所需的行政区划投影
     */
    List<RegionWeatherRegion> selectWeatherRegions();
}
