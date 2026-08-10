package com.ruoyi.rentalbusiness.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.rentalbusiness.model.MerchantOrderListVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderQuery;
import com.ruoyi.rentalbusiness.service.MerchantOrderService;

/** 商户订单工作台接口。订单数据由 rental-business 独立维护。 */
@RestController
@RequestMapping("/merchant/orders")
public class MerchantOrderController extends BaseController
{
    private final MerchantOrderService orderService;

    /**
     * 注入商户订单应用服务。
     *
     * @param orderService 商户订单应用服务
     */
    public MerchantOrderController(MerchantOrderService orderService)
    {
        this.orderService = orderService;
    }

    /**
     * 分页查询当前商户可见的订单列表。
     *
     * 租户、商户和仓库范围不接受前端传参，应用服务会在调用 Mapper 前使用
     * 当前登录态覆盖查询范围；未审核商户允许读取，写操作由后续接口单独保护。
     *
     * @param query 订单筛选条件
     * @return 当前商户订单分页结果
     */
    @RequiresPermissions("merchant:order:list")
    @GetMapping
    public TableDataInfo list(MerchantOrderQuery query)
    {
        startPage();
        List<MerchantOrderListVO> list = orderService.list(query);
        return getDataTable(list);
    }
}
