package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.rentalbusiness.domain.RegionWeatherRecord;

/** 市级天气扩展表数据访问接口。 */
public interface RegionWeatherMapper
{
    /**
     * 按市级行政区和预报日期新增或更新天气快照。
     *
     * @param record 待保存天气记录
     * @param operatorId 系统任务操作人，默认使用 0
     * @return 写入或更新行数
     */
    int upsert(@Param("record") RegionWeatherRecord record, @Param("operatorId") Long operatorId);

    /**
     * 批量新增或更新同一批市级天气快照，减少大规模天气任务的数据库往返次数。
     *
     * @param records 待保存天气记录集合
     * @param operatorId 系统任务操作人，默认使用 0
     * @return 写入或更新的记录行数
     */
    int upsertBatch(@Param("records") List<RegionWeatherRecord> records,
            @Param("operatorId") Long operatorId);

    /**
     * 查询指定行政区和预报日期的天气快照；区县会先解析所属市级行政区并复用市级记录。
     *
     * @param regionCode 行政区划 adcode
     * @param weatherDate 预报日期
     * @return 天气快照，没有记录时返回 null
     */
    RegionWeatherRecord selectByRegionAndDate(@Param("regionCode") String regionCode,
            @Param("weatherDate") java.sql.Date weatherDate);

    /**
     * 查询指定日期的市级天气快照，供批量统计或缓存预热使用。
     *
     * @param weatherDate 预报日期
     * @return 天气快照列表
     */
    List<RegionWeatherRecord> selectByDate(@Param("weatherDate") java.sql.Date weatherDate);
}
