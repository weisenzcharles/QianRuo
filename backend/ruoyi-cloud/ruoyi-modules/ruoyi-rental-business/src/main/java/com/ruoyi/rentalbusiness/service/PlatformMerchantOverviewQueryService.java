package com.ruoyi.rentalbusiness.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import com.ruoyi.rentalbusiness.mapper.PlatformMerchantOverviewMapper;
import com.ruoyi.system.api.model.PlatformMerchantOverviewStat;

/** rental-business 商户经营概况聚合查询服务。 */
@Service
public class PlatformMerchantOverviewQueryService
{
    private final PlatformMerchantOverviewMapper overviewMapper;

    public PlatformMerchantOverviewQueryService(PlatformMerchantOverviewMapper overviewMapper)
    {
        this.overviewMapper = overviewMapper;
    }

    /** 查询指定商户的经营统计；空集合直接返回，避免生成无效 IN 条件。 */
    public List<PlatformMerchantOverviewStat> list(List<Long> memberIds)
    {
        if (memberIds == null || memberIds.isEmpty())
        {
            return Collections.emptyList();
        }
        Set<Long> normalizedIds = new LinkedHashSet<Long>();
        for (Long memberId : memberIds)
        {
            if (memberId != null && memberId.longValue() > 0L)
            {
                normalizedIds.add(memberId);
            }
        }
        if (normalizedIds.isEmpty())
        {
            return Collections.emptyList();
        }
        return overviewMapper.selectStats(new ArrayList<Long>(normalizedIds));
    }
}
