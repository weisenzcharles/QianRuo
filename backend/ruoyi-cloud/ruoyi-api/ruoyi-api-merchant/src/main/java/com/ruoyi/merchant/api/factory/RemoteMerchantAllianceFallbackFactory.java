package com.ruoyi.merchant.api.factory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.cloud.openfeign.FallbackFactory;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.merchant.api.RemoteMerchantAllianceService;
import com.ruoyi.merchant.api.model.MerchantAllianceOption;
import com.ruoyi.merchant.api.model.MerchantAllianceOptionQuery;

/** 商户联盟内部接口降级实现。 */
@Component
public class RemoteMerchantAllianceFallbackFactory implements FallbackFactory<RemoteMerchantAllianceService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteMerchantAllianceFallbackFactory.class);

    @Override
    public RemoteMerchantAllianceService create(Throwable cause)
    {
        log.error("商户联盟服务调用失败", cause);
        return new RemoteMerchantAllianceService()
        {
            @Override
            public R<TableDataInfo> listForPlatform(String keyword, Integer status, Integer pageNum, Integer pageSize,
                    String source, String clientType)
            { return R.ok(new TableDataInfo()); }

            @Override
            public R<List<MerchantAllianceOption>> membersForPlatform(Long allianceId, String source,
                    String clientType)
            { return R.ok(Collections.<MerchantAllianceOption>emptyList()); }

            @Override
            public R<Void> updateStatusForPlatform(Long allianceId, Long memberId, Integer status, String source,
                    String clientType)
            { return R.fail("商户联盟服务暂不可用"); }

            @Override
            public R<List<MerchantAllianceOption>> listOptions(MerchantAllianceOptionQuery query, String source,
                    String clientType)
            { return R.ok(Collections.<MerchantAllianceOption>emptyList()); }

            @Override
            public R<Map<String, Object>> current(MerchantAllianceOptionQuery query, String source,
                    String clientType)
            { return R.ok(Collections.<String, Object>emptyMap()); }

            @Override
            public R<List<MerchantAllianceOption>> listSharedMembers(MerchantAllianceOptionQuery query, String source,
                    String clientType)
            { return R.ok(Collections.<MerchantAllianceOption>emptyList()); }

            @Override
            public R<Void> reserveOrderSlot(Long tenantId, Long allianceId, Long memberId, Long allianceOrderId,
                    String source, String clientType)
            { return R.fail("商户联盟服务暂不可用"); }

            @Override
            public R<Void> releaseOrderSlot(Long tenantId, Long memberId, Long allianceOrderId, String source,
                    String clientType)
            { return R.fail("商户联盟服务暂不可用"); }
        };
    }
}
