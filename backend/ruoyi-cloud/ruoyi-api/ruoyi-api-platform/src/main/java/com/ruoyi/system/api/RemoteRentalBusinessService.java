package com.ruoyi.system.api;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.ServiceNameConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.system.api.factory.RemoteRentalBusinessFallbackFactory;
import com.ruoyi.system.api.model.PlatformWarehouseView;

/**
 * 平台服务访问租赁业务服务的内部契约。
 *
 * <p>平台服务不得直接访问 {@code ry_business} 数据库，仓库范围和业务引用校验
 * 必须通过该契约进入 rental-business。</p>
 */
@FeignClient(contextId = "remoteRentalBusinessService", value = ServiceNameConstants.RENTAL_BUSINESS_SERVICE,
        fallbackFactory = RemoteRentalBusinessFallbackFactory.class)
public interface RemoteRentalBusinessService
{
    /** 租赁业务服务内部接口前缀。 */
    String INTERNAL_PREFIX = "/internal/rental-business";

    /**
     * 查询指定商户的仓库列表。
     *
     * @param tenantId 可信租户主键
     * @param memberId 可信商户主键
     * @param status 仓库状态，为空时不限制状态
     * @param source 内部服务调用标识
     * @return 仓库传输对象集合
     */
    @GetMapping(INTERNAL_PREFIX + "/warehouses")
    R<List<PlatformWarehouseView>> listWarehouses(@RequestParam("tenantId") Long tenantId,
            @RequestParam("memberId") Long memberId, @RequestParam(value = "status", required = false) Integer status,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 统计指定商户的启用仓库中命中给定主键集合的数量。
     *
     * @param tenantId 可信租户主键
     * @param memberId 可信商户主键
     * @param warehouseIds 待校验仓库主键集合
     * @param source 内部服务调用标识
     * @return 命中的启用仓库数量
     */
    @GetMapping(INTERNAL_PREFIX + "/warehouses/count")
    R<Integer> countEnabledWarehouses(@RequestParam("tenantId") Long tenantId,
            @RequestParam("memberId") Long memberId, @RequestParam("warehouseIds") List<Long> warehouseIds,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 判断标准型号是否已经被租赁业务数据引用。
     *
     * @param modelId 平台标准型号主键
     * @param source 内部服务调用标识
     * @return 已引用返回 true，否则返回 false
     */
    @GetMapping(INTERNAL_PREFIX + "/models/{modelId}/references")
    R<Boolean> hasModelReferences(@PathVariable("modelId") Long modelId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
