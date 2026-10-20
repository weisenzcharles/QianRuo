package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.api.model.PlatformAllianceOrderView;
import com.ruoyi.system.api.model.PlatformAllianceOrderEventView;
import com.ruoyi.system.api.model.PlatformAllianceSettlementView;
import com.ruoyi.system.api.model.PlatformAllianceSettlementSummaryView;

/** 平台联盟订单治理只读查询。 */
public interface PlatformAllianceOrderMapper
{
    long count(@Param("query") com.ruoyi.system.api.model.PlatformAllianceOrderQuery query);

    List<PlatformAllianceOrderView> select(@Param("query") com.ruoyi.system.api.model.PlatformAllianceOrderQuery query,
            @Param("offset") int offset, @Param("pageSize") int pageSize);

    PlatformAllianceOrderView selectById(@Param("allianceOrderId") Long allianceOrderId);

    List<PlatformAllianceOrderEventView> selectEvents(@Param("allianceOrderId") Long allianceOrderId);

    List<PlatformAllianceSettlementView> selectSettlements(@Param("allianceOrderId") Long allianceOrderId);

    PlatformAllianceSettlementSummaryView selectSettlementSummary();
}
