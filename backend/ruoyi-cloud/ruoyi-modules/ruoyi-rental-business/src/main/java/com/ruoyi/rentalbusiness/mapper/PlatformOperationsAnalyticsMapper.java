package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.api.model.PlatformOperationsCityStat;
import com.ruoyi.system.api.model.PlatformOperationsMerchantStat;
import com.ruoyi.system.api.model.PlatformOperationsModelStat;
import com.ruoyi.system.api.model.PlatformOperationsTrendView;

/** 平台经营分析聚合数据访问。只读取租赁业务事实表，不向浏览器暴露内部编号。 */
public interface PlatformOperationsAnalyticsMapper
{
    /** 查询指定商户范围内的每日订单趋势。 */
    List<PlatformOperationsTrendView> selectOrderTrend(@Param("memberIds") List<Long> memberIds,
            @Param("beginTime") String beginTime, @Param("endTime") String endTime);

    /** 查询指定商户范围内的周期经营指标。 */
    List<PlatformOperationsMerchantStat> selectMerchantStats(@Param("memberIds") List<Long> memberIds,
            @Param("beginTime") String beginTime, @Param("endTime") String endTime);

    /** 查询指定商户范围内的收货城市聚合指标。 */
    List<PlatformOperationsCityStat> selectCityStats(@Param("memberIds") List<Long> memberIds,
            @Param("beginTime") String beginTime, @Param("endTime") String endTime);

    /** 查询指定商户范围内的商品型号热度指标。 */
    List<PlatformOperationsModelStat> selectModelStats(@Param("memberIds") List<Long> memberIds,
            @Param("beginTime") String beginTime, @Param("endTime") String endTime);
}
