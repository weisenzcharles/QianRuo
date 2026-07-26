package com.ruoyi.merchant.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.merchant.domain.MerchantWarehouse;
import com.ruoyi.merchant.mapper.MerchantIdentityMapper;

/** 当前商户可信工作上下文。 */
@RestController
@RequestMapping("/merchant/context")
public class MerchantContextController
{
    @Autowired
    private MerchantIdentityMapper identityMapper;

    @GetMapping
    public AjaxResult context()
    {
        AjaxResult result = AjaxResult.success();
        result.put("memberId", MerchantContextUtils.getMemberId());
        result.put("memberRole", MerchantContextUtils.getMemberRole());
        result.put("warehouseScopeType", MerchantContextUtils.getWarehouseScopeType());
        result.put("warehouseIds", MerchantContextUtils.getWarehouseIds());
        return result;
    }

    @GetMapping("/warehouses")
    public AjaxResult warehouses()
    {
        List<MerchantWarehouse> warehouses = identityMapper.selectWarehouses(
                MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId());
        if (!MerchantContextUtils.hasAllWarehouseAccess())
        {
            Set<Long> allowed = MerchantContextUtils.getWarehouseIds();
            warehouses = warehouses.stream()
                    .filter(warehouse -> allowed.contains(warehouse.getWarehouseId()))
                    .collect(Collectors.toList());
        }
        return AjaxResult.success(warehouses);
    }
}
