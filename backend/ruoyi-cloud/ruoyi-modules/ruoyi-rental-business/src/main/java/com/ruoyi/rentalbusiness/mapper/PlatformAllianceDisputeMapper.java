package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.api.model.PlatformAllianceDisputeView;

/** 平台联盟纠纷治理只读查询。 */
public interface PlatformAllianceDisputeMapper
{
    long count(@Param("status") Integer status);

    List<PlatformAllianceDisputeView> select(@Param("status") Integer status,
            @Param("offset") int offset, @Param("pageSize") int pageSize);

    PlatformAllianceDisputeView selectById(@Param("disputeId") Long disputeId);

    List<Map<String, Object>> selectEvidence(@Param("disputeId") Long disputeId);

    Map<String, Object> selectMediation(@Param("disputeId") Long disputeId);
}
