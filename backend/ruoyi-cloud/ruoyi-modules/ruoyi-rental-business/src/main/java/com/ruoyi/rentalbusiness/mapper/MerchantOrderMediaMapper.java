package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.rentalbusiness.model.MerchantOrderMediaCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderMediaVO;

/** 商户订单履约媒体数据访问。查询和写入均限制当前租户、商户及软删除范围。 */
public interface MerchantOrderMediaMapper
{
    List<MerchantOrderMediaVO> selectByOrder(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("orderId") Long orderId);

    MerchantOrderMediaVO selectLatest(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("orderId") Long orderId,
            @Param("mediaType") String mediaType);

    MerchantOrderMediaVO selectById(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("mediaId") Long mediaId);

    List<MerchantOrderMediaVO> selectUnmatched(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("mediaType") Integer mediaType,
            @Param("matchStatuses") List<Integer> matchStatuses);

    int insert(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("orderId") Long orderId, @Param("command") MerchantOrderMediaCreateCommand command,
            @Param("operatorId") Long operatorId);

    int updateMatch(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("mediaId") Long mediaId, @Param("orderId") Long orderId, @Param("unitId") Long unitId,
            @Param("snId") Long snId, @Param("detectedSn") String detectedSn,
            @Param("matchStatus") Integer matchStatus, @Param("operatorId") Long operatorId);

    Map<String, Object> selectOpenOrderBySnCode(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("detectedSn") String detectedSn,
            @Param("openOrderStatuses") List<Integer> openOrderStatuses);

    Map<String, Object> selectOpenOrderTarget(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("orderId") Long orderId, @Param("snId") Long snId,
            @Param("openOrderStatuses") List<Integer> openOrderStatuses);
}
