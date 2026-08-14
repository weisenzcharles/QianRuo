package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/** 行政区划数据访问接口。 */
public interface RegionMapper
{
    /** 查询指定父级下的有效行政区划。 */
    List<Map<String, Object>> selectChildren(@Param("parentCode") String parentCode);
}
