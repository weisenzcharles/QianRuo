package com.ruoyi.rentalbusiness.service;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.rentalbusiness.mapper.PlatformAllianceDisputeMapper;
import com.ruoyi.system.api.model.PlatformAllianceDisputePageView;
import com.ruoyi.system.api.model.PlatformAllianceDisputeView;

/** 平台联盟纠纷只读治理查询。 */
@Service
public class PlatformAllianceDisputeQueryService
{
    private final PlatformAllianceDisputeMapper mapper;

    public PlatformAllianceDisputeQueryService(PlatformAllianceDisputeMapper mapper)
    {
        this.mapper = mapper;
    }

    /** 按纠纷状态分页查询所有商户的联盟纠纷。 */
    public PlatformAllianceDisputePageView list(Integer status, Integer pageNum, Integer pageSize)
    {
        int page = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        long total = mapper.count(status);
        List<PlatformAllianceDisputeView> rows = total == 0 ? Collections.emptyList()
                : mapper.select(status, (page - 1) * size, size);
        PlatformAllianceDisputePageView result = new PlatformAllianceDisputePageView();
        result.setRows(rows);
        result.setTotal(total);
        return result;
    }

    /** 查询平台可见的纠纷详情。 */
    public PlatformAllianceDisputeView detail(Long disputeId)
    {
        PlatformAllianceDisputeView result = mapper.selectById(disputeId);
        if (result != null)
        {
            result.setEvidence(mapper.selectEvidence(disputeId));
            result.setMediation(mapper.selectMediation(disputeId));
        }
        return result;
    }
}
