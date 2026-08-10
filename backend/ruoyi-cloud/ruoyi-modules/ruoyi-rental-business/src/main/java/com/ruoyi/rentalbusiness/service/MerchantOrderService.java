package com.ruoyi.rentalbusiness.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.rentalbusiness.domain.MerchantOrderRow;
import com.ruoyi.rentalbusiness.mapper.MerchantOrderMapper;
import com.ruoyi.rentalbusiness.model.MerchantOrderListVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderQuery;

/**
 * 商户订单列表应用服务。
 *
 * 订单属于 rental-business，服务只从可信商户登录上下文获取租户、商户和仓库
 * 范围，禁止使用请求参数扩大数据范围。订单列表是只读能力，未审核商户可以
 * 按默认只读菜单查看，但发货等后续状态操作必须另行校验认证状态和业务权限。
 */
@Service
public class MerchantOrderService
{
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final Map<String, String> STATUS_LABELS = createStatusLabels();

    private static final Map<String, String> STATUS_TYPES = createStatusTypes();

    private final MerchantOrderMapper orderMapper;

    /**
     * 注入商户订单查询数据访问对象。
     *
     * @param orderMapper 商户订单 Mapper
     */
    public MerchantOrderService(MerchantOrderMapper orderMapper)
    {
        this.orderMapper = orderMapper;
    }

    /**
     * 查询当前商户订单列表，并将数据库投影转换为前端订单工作台结构。
     *
     * @param query 页面筛选条件；允许为空
     * @return 当前登录商户可访问的订单列表
     */
    public List<MerchantOrderListVO> list(MerchantOrderQuery query)
    {
        MerchantOrderQuery actualQuery = query == null ? new MerchantOrderQuery() : query;
        actualQuery.setStatus(normalizeStatus(actualQuery.getStatus()));
        actualQuery.setAllWarehouseAccess(MerchantContextUtils.hasAllWarehouseAccess());
        Set<Long> warehouseIds = MerchantContextUtils.getWarehouseIds();
        actualQuery.setWarehouseIds(warehouseIds);

        List<MerchantOrderRow> rows = orderMapper.selectMerchantOrderList(
                MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), actualQuery);
        List<MerchantOrderListVO> result = new ArrayList<MerchantOrderListVO>(rows.size());
        for (MerchantOrderRow row : rows)
        {
            result.add(toView(row));
        }
        return result;
    }

    /**
     * 将前端状态标签或状态编码统一转换为数据库状态编码。
     *
     * @param status 前端传入的状态标签或编码
     * @return 可用于数据库筛选的状态编码；为空时返回 null
     */
    private String normalizeStatus(String status)
    {
        if (status == null || status.trim().isEmpty())
        {
            return null;
        }
        String value = status.trim();
        return STATUS_LABELS.containsKey(value) ? value : findStatusCodeByLabel(value);
    }

    /**
     * 根据中文状态标签反查数据库状态编码。
     *
     * @param label 中文状态标签或原始值
     * @return 数据库状态编码
     */
    private String findStatusCodeByLabel(String label)
    {
        for (Map.Entry<String, String> entry : STATUS_LABELS.entrySet())
        {
            if (entry.getValue().equals(label))
            {
                return entry.getKey();
            }
        }
        return label;
    }

    /**
     * 将内部查询行转换为商户端订单列表响应。
     *
     * @param row 数据库查询行
     * @return 前端订单列表响应
     */
    private MerchantOrderListVO toView(MerchantOrderRow row)
    {
        MerchantOrderListVO view = new MerchantOrderListVO();
        view.orderNo = row.orderNo;
        view.model = defaultText(row.modelName, "商品待维护");
        view.packageName = defaultText(row.packageName, "未绑定套餐");
        view.rentalPeriod = formatDate(row.leaseStartDate) + " ~ " + formatDate(row.leaseEndDate);
        view.returnDate = formatDate(row.leaseEndDate);
        view.rent = row.rentAmount == null ? BigDecimal.ZERO : row.rentAmount;
        view.channel = defaultText(row.channelCode, "未设置渠道");
        view.merchant = defaultText(row.shopName, "未设置店铺");
        view.customer = toCustomer(row);
        view.shipping = toShipping(row);
        view.weather = toWeather(row);
        view.delivery = toDelivery(row);
        view.status = STATUS_LABELS.getOrDefault(row.orderStatus, defaultText(row.orderStatus, "未知状态"));
        view.statusType = STATUS_TYPES.getOrDefault(row.orderStatus, "info");
        view.trackingNo = defaultText(row.shippingTrackingNo, "待安排");
        view.returnNo = defaultText(row.returnTrackingNo, "待安排");
        view.createdAt = formatDateTime(row.createTime);
        view.productCode = defaultText(row.productCode, "待维护");
        view.addOns = splitAddOns(row.addonNames);
        view.attention = row.attention;
        view.operator = defaultText(row.createBy, "系统");
        view.coordinator = defaultText(row.createBy, "待分配");
        return view;
    }

    /**
     * 转换客户信息，并对缺失的快照字段提供稳定的展示值。
     *
     * @param row 数据库查询行
     * @return 客户展示对象
     */
    private MerchantOrderListVO.Customer toCustomer(MerchantOrderRow row)
    {
        MerchantOrderListVO.Customer customer = new MerchantOrderListVO.Customer();
        customer.nickname = defaultText(row.customerNickname, row.customerName);
        customer.name = defaultText(row.customerName, "未填写");
        customer.phone = defaultText(row.customerPhone, "未填写");
        customer.address = defaultText(row.customerAddress, "未填写收货地址");
        customer.note = defaultText(row.customerNote, "暂无客户备注");
        return customer;
    }

    /**
     * 转换发货安排信息，实际可选配送方式由后续订单状态接口补充。
     *
     * @param row 数据库查询行
     * @return 发货安排展示对象
     */
    private MerchantOrderListVO.Shipping toShipping(MerchantOrderRow row)
    {
        MerchantOrderListVO.Shipping shipping = new MerchantOrderListVO.Shipping();
        shipping.warehouse = defaultText(row.warehouseName, "未分配仓库");
        shipping.shipDate = formatDate(row.shipDueTime == null ? row.createTime : row.shipDueTime);
        shipping.method = "快递 / 自提 / 跑腿";
        shipping.doorTime = row.shipDueTime == null ? "待安排" : formatDateTime(row.shipDueTime);
        shipping.note = defaultText(row.customerNote, "暂无发货备注");
        return shipping;
    }

    /**
     * 转换气象风险信息，当前阶段使用订单风险标识和仓库城市生成稳定展示结构。
     *
     * @param row 数据库查询行
     * @return 气象风险展示对象
     */
    private MerchantOrderListVO.Weather toWeather(MerchantOrderRow row)
    {
        MerchantOrderListVO.Weather weather = new MerchantOrderListVO.Weather();
        weather.route = defaultText(row.warehouseCity, "发货地") + " → 收货地";
        weather.depart = row.weatherRiskFlag != null && row.weatherRiskFlag == 1 ? "存在风险" : "待查询";
        weather.arrive = row.weatherRiskFlag != null && row.weatherRiskFlag == 1 ? "请关注天气" : "待查询";
        weather.departIcon = "sunny";
        weather.arriveIcon = "rain";
        weather.risk = row.weatherRiskFlag != null && row.weatherRiskFlag == 1;
        return weather;
    }

    /**
     * 转换物流配送方案，未创建物流单时返回空方案而不是虚构物流信息。
     *
     * @param row 数据库查询行
     * @return 配送方案展示对象
     */
    private MerchantOrderListVO.Delivery toDelivery(MerchantOrderRow row)
    {
        MerchantOrderListVO.Delivery delivery = new MerchantOrderListVO.Delivery();
        if (row.shippingCarrier != null && !row.shippingCarrier.trim().isEmpty())
        {
            MerchantOrderListVO.DeliveryPlan plan = new MerchantOrderListVO.DeliveryPlan();
            plan.carrier = row.shippingCarrier;
            plan.name = "已创建物流单";
            plan.price = row.shippingFee == null ? BigDecimal.ZERO : row.shippingFee;
            delivery.plans = Collections.singletonList(plan);
        }
        else
        {
            delivery.plans = Collections.emptyList();
        }
        delivery.expectedDate = formatDate(row.expectedArriveTime);
        return delivery;
    }

    /**
     * 将数据库中的增购商品拼接值拆分为前端商品清单数组。
     *
     * @param addonNames 数据库 GROUP_CONCAT 结果
     * @return 去除空白项后的增购商品名称集合
     */
    private List<String> splitAddOns(String addonNames)
    {
        if (addonNames == null || addonNames.trim().isEmpty())
        {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<String>();
        for (String item : Arrays.asList(addonNames.split("、")))
        {
            if (!item.trim().isEmpty())
            {
                result.add(item.trim());
            }
        }
        return result;
    }

    /**
     * 按项目统一时区格式化日期字段。
     *
     * @param date 日期对象
     * @return yyyy-MM-dd 格式日期或空字符串
     */
    private String formatDate(Date date)
    {
        return date == null ? "" : DateUtils.parseDateToStr(DATE_PATTERN, date);
    }

    /**
     * 按项目统一时区格式化日期时间字段。
     *
     * @param date 日期对象
     * @return yyyy-MM-dd HH:mm:ss 格式日期时间或空字符串
     */
    private String formatDateTime(Date date)
    {
        return date == null ? "" : DateUtils.parseDateToStr(DATETIME_PATTERN, date);
    }

    /**
     * 为页面字段提供非空展示文本。
     *
     * @param value 原始文本
     * @param fallback 缺失时的默认文本
     * @return 原始文本或默认文本
     */
    private String defaultText(String value, String fallback)
    {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    /**
     * 创建数据库状态编码到中文标签的只读映射。
     *
     * @return 订单状态标签映射
     */
    private static Map<String, String> createStatusLabels()
    {
        Map<String, String> labels = new LinkedHashMap<String, String>();
        labels.put("pending_ship", "待发货");
        labels.put("prepared", "已备货");
        labels.put("ship_in_transit", "已发货");
        labels.put("renting", "租用中");
        labels.put("returning", "归还中");
        labels.put("after_sales", "售后中");
        labels.put("completed", "已完成");
        labels.put("cancelled", "已取消");
        return Collections.unmodifiableMap(labels);
    }

    /**
     * 创建数据库状态编码到 Element Plus 标签类型的只读映射。
     *
     * @return 订单状态样式映射
     */
    private static Map<String, String> createStatusTypes()
    {
        Map<String, String> types = new LinkedHashMap<String, String>();
        types.put("pending_ship", "warning");
        types.put("prepared", "primary");
        types.put("ship_in_transit", "primary");
        types.put("renting", "success");
        types.put("returning", "warning");
        types.put("after_sales", "danger");
        types.put("completed", "success");
        types.put("cancelled", "info");
        return Collections.unmodifiableMap(types);
    }
}
