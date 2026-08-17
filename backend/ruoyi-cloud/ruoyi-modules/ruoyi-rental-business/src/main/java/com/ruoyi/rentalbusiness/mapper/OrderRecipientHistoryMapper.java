package com.ruoyi.rentalbusiness.mapper;

import org.apache.ibatis.annotations.Param;
import com.ruoyi.rentalbusiness.domain.OrderRecipientHistoryRecord;

/** 订单收件人历史快照数据访问接口。 */
public interface OrderRecipientHistoryMapper
{
    /**
     * 查询指定订单编号的最新收件人快照。
     *
     * @param tenantId 租户主键
     * @param memberId 商户主键
     * @param orderNo 订单编号
     * @return 最新快照，没有记录时返回 null
     */
    OrderRecipientHistoryRecord selectLatest(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("orderNo") String orderNo);

    /**
     * 将指定订单当前使用的收件人快照标记为历史地址。
     *
     * @param tenantId 租户主键
     * @param memberId 商户主键
     * @param orderNo 订单编号
     * @param operatorId 操作人用户主键
     * @return 更新行数
     */
    int clearUsedSnapshots(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("orderNo") String orderNo, @Param("operatorId") Long operatorId);

    /**
     * 新增订单收件人历史快照。
     *
     * @param tenantId 租户主键
     * @param memberId 商户主键
     * @param orderId 订单主键
     * @param orderNo 订单编号
     * @param record 收件人快照
     * @param operatorId 操作人用户主键
     * @return 写入行数
     */
    int insertSnapshot(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("orderId") Long orderId, @Param("orderNo") String orderNo,
            @Param("record") OrderRecipientHistoryRecord record, @Param("operatorId") Long operatorId);
}
