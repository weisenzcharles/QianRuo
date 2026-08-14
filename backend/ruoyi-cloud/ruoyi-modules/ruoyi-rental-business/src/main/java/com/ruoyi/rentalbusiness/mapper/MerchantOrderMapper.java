package com.ruoyi.rentalbusiness.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.rentalbusiness.domain.MerchantOrderRow;
import com.ruoyi.rentalbusiness.model.MerchantOrderQuery;
import com.ruoyi.rentalbusiness.model.MerchantOrderCreateCommand;

/** 商户订单查询数据访问。所有查询必须带租户、商户和软删除条件。 */
public interface MerchantOrderMapper
{
    /** 查询当前商户指定订单详情，SQL 层同时限制租户、商户和软删除范围。 */
    MerchantOrderRow selectMerchantOrderById(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("orderId") Long orderId);

    /** 校验订单渠道是否为启用的平台渠道或当前商户自定义渠道。 */
    int countUsableOrderChannel(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("channelId") Long channelId);

    /** 插入商户订单主表，订单归属由服务端参数补齐。 */
    int insertMerchantOrder(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("command") MerchantOrderCreateCommand command, @Param("operatorId") Long operatorId);

    /** 更新当前商户订单的客户和租期信息。 */
    int updateMerchantOrder(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("command") MerchantOrderCreateCommand command, @Param("operatorId") Long operatorId);

    /** 插入订单商品明细，明细归属与订单保持一致。 */
    int insertMerchantOrderItem(@Param("tenantId") Long tenantId, @Param("merchantId") Long merchantId,
            @Param("command") MerchantOrderCreateCommand command, @Param("operatorId") Long operatorId);
    /**
     * 按当前商户上下文分页查询订单工作台所需字段。
     *
     * @param tenantId 可信租户主键
     * @param merchantId 可信商户主键
     * @param query 已覆盖仓库范围的商户订单查询条件
     * @return 当前用户可见的订单查询行
     */
    List<MerchantOrderRow> selectMerchantOrderList(@Param("tenantId") Long tenantId,
            @Param("merchantId") Long merchantId, @Param("query") MerchantOrderQuery query);
}
