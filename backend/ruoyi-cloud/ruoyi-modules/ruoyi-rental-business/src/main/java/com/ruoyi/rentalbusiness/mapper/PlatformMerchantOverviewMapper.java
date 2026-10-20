package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.api.model.PlatformMerchantOverviewStat;

/** 平台商户经营统计聚合数据访问。 */
public interface PlatformMerchantOverviewMapper
{
    /** 按商户唯一编号集合聚合仓库、商品、库存和订单数量。 */
    List<PlatformMerchantOverviewStat> selectStats(@Param("memberIds") List<Long> memberIds);
}
