package com.ruoyi.rentalbusiness.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.rentalbusiness.mapper.PlatformMerchantOverviewMapper;
import com.ruoyi.system.api.model.PlatformMerchantOverviewStat;

/** 验证平台商户经营概况查询只接收有效且去重后的商户编号。 */
public class PlatformMerchantOverviewQueryServiceTest
{
    /** 空范围不访问数据库，避免生成无效的派生表查询。 */
    @Test
    public void list_emptyMemberIds_returnsEmptyWithoutQuery()
    {
        CapturingMapper mapper = new CapturingMapper();
        PlatformMerchantOverviewQueryService service = new PlatformMerchantOverviewQueryService(mapper);

        Assert.assertTrue(service.list(Collections.<Long>emptyList()).isEmpty());
        Assert.assertNull(mapper.memberIds);
    }

    /** 无效编号被过滤，重复编号按首次出现顺序传递给聚合查询。 */
    @Test
    public void list_invalidAndDuplicateMemberIds_normalizesBeforeQuery()
    {
        CapturingMapper mapper = new CapturingMapper();
        PlatformMerchantOverviewQueryService service = new PlatformMerchantOverviewQueryService(mapper);

        List<PlatformMerchantOverviewStat> result = service.list(Arrays.asList(
                Long.valueOf(5L), null, Long.valueOf(0L), Long.valueOf(-2L), Long.valueOf(5L), Long.valueOf(3L)));

        Assert.assertEquals(Arrays.asList(Long.valueOf(5L), Long.valueOf(3L)), mapper.memberIds);
        Assert.assertNotNull(result);
    }

    private static final class CapturingMapper implements PlatformMerchantOverviewMapper
    {
        private List<Long> memberIds;

        @Override
        public List<PlatformMerchantOverviewStat> selectStats(List<Long> memberIds)
        {
            this.memberIds = memberIds;
            return Collections.emptyList();
        }
    }
}
