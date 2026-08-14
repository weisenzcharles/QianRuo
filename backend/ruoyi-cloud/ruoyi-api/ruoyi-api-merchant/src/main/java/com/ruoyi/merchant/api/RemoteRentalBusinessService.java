package com.ruoyi.merchant.api;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.merchant.api.factory.RemoteRentalBusinessFallbackFactory;
import com.ruoyi.merchant.api.model.MerchantWarehouseView;

/**
 * 商户服务访问租赁业务服务的内部仓库契约。
 *
 * <p>商户身份和权限仍由商户服务负责，仓库主数据只通过该内部契约读取，禁止商户服务
 * 直接访问 {@code ry_business} 数据库。</p>
 */
@FeignClient(contextId = "remoteMerchantRentalBusinessService",
        value = ServiceNameConstants.RENTAL_BUSINESS_SERVICE,
        fallbackFactory = RemoteRentalBusinessFallbackFactory.class)
public interface RemoteRentalBusinessService
{
    /** 租赁业务服务内部接口前缀。 */
    String INTERNAL_PREFIX = "/internal/rental-business";

    /**
     * 查询指定商户当前启用仓库。
     *
     * @param tenantId 可信租户主键
     * @param memberId 可信商户主键
     * @param status 仓库状态，为空时不限制状态
     * @param source 内部服务调用标识
     * @return 仓库传输对象集合
     */
    @GetMapping(INTERNAL_PREFIX + "/warehouses")
    R<List<MerchantWarehouseView>> listWarehouses(@RequestParam("tenantId") Long tenantId,
            @RequestParam("memberId") Long memberId, @RequestParam(value = "status", required = false) Integer status,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
