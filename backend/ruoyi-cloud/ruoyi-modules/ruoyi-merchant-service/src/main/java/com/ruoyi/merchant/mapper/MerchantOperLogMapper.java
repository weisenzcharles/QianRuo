package com.ruoyi.merchant.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.merchant.domain.MerchantOperLogRecord;
import com.ruoyi.merchant.model.MerchantOperLogQuery;
import com.ruoyi.merchant.model.MerchantOperLogVO;

/** 商户操作日志数据访问。 */
public interface MerchantOperLogMapper
{
    int insert(@Param("record") MerchantOperLogRecord record);

    List<MerchantOperLogVO> selectList(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("query") MerchantOperLogQuery query);
}
