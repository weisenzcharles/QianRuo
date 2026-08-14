package com.ruoyi.rentalbusiness.controller.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.security.annotation.InnerAuth;
import com.ruoyi.rentalbusiness.service.RentalBusinessService;
import com.ruoyi.system.api.model.PlatformWarehouseView;

/**
 * 租赁业务服务内部探活接口。
 *
 * 该接口仅用于网关、部署脚本或其他服务验证服务注册与内部鉴权是否正常，不承载
 * 商品、订单等业务能力，也不允许浏览器端直接调用。
 */
@RestController
@RequestMapping("/internal/rental-business")
public class InternalRentalBusinessController
{
    private final RentalBusinessService rentalBusinessService;

    /** 注入租赁业务应用服务，内部控制器不直接操作 Mapper。 */
    public InternalRentalBusinessController(RentalBusinessService rentalBusinessService)
    {
        this.rentalBusinessService = rentalBusinessService;
    }

    /**
     * 返回固定服务标识，供内部调用方确认请求已经到达 rental-business。
     *
     * @return 租赁业务服务标识
     */
    @InnerAuth
    @GetMapping("/ping")
    public R<String> ping()
    {
        return R.ok("rental-business");
    }

    /**
     * 查询指定商户的仓库内部视图，平台服务和商户服务共用同一份业务库事实。
     *
     * @param tenantId 可信租户主键
     * @param memberId 可信商户主键
     * @param status 仓库状态，为空时不限制状态
     * @return 仓库内部传输对象集合
     */
    @InnerAuth
    @GetMapping("/warehouses")
    public R<List<PlatformWarehouseView>> warehouses(@RequestParam("tenantId") Long tenantId,
            @RequestParam("memberId") Long memberId, @RequestParam(value = "status", required = false) Integer status)
    {
        return R.ok(toWarehouseViews(rentalBusinessService.listWarehousesForInternal(tenantId, memberId, status)));
    }

    /**
     * 统计指定商户启用仓库中命中的仓库主键数量，供平台端校验仓库权限提交内容。
     *
     * @param tenantId 可信租户主键
     * @param memberId 可信商户主键
     * @param warehouseIds 待校验仓库主键集合
     * @return 命中的启用仓库数量
     */
    @InnerAuth
    @GetMapping("/warehouses/count")
    public R<Integer> countWarehouses(@RequestParam("tenantId") Long tenantId,
            @RequestParam("memberId") Long memberId, @RequestParam("warehouseIds") List<Long> warehouseIds)
    {
        return R.ok(rentalBusinessService.countEnabledWarehousesForInternal(tenantId, memberId, warehouseIds));
    }

    /**
     * 判断标准型号是否被租赁业务库中的商品或资源池兼容关系引用。
     *
     * @param modelId 平台标准型号主键
     * @return 已引用返回 true，否则返回 false
     */
    @InnerAuth
    @GetMapping("/models/{modelId}/references")
    public R<Boolean> modelReferences(@PathVariable("modelId") Long modelId)
    {
        return R.ok(rentalBusinessService.hasModelReferencesForInternal(modelId));
    }

    /**
     * 将租赁业务 Mapper 的动态结果转换为平台内部契约对象，防止数据库 Map 越过服务边界。
     *
     * @param rows 租赁业务仓库查询结果
     * @return 平台内部仓库传输对象集合
     */
    private List<PlatformWarehouseView> toWarehouseViews(List<Map<String, Object>> rows)
    {
        if (rows == null || rows.isEmpty())
        {
            return Collections.emptyList();
        }
        List<PlatformWarehouseView> result = new ArrayList<PlatformWarehouseView>(rows.size());
        for (Map<String, Object> row : rows)
        {
            PlatformWarehouseView view = new PlatformWarehouseView();
            view.setWarehouseId(longValue(row.get("warehouseId")));
            view.setTenantId(longValue(row.get("tenantId")));
            view.setMemberId(longValue(row.get("memberId")));
            view.setWarehouseCode(stringValue(row.get("warehouseCode")));
            view.setWarehouseName(stringValue(row.get("warehouseName")));
            view.setStatus(stringValue(row.get("status")));
            result.add(view);
        }
        return result;
    }

    /** 将 JDBC 数值字段统一转换为长整数。 */
    private Long longValue(Object value)
    {
        return value == null ? null : value instanceof Number ? ((Number) value).longValue()
                : Long.valueOf(String.valueOf(value));
    }

    /** 将 JDBC 字段统一转换为文本。 */
    private String stringValue(Object value)
    {
        return value == null ? null : String.valueOf(value);
    }
}
