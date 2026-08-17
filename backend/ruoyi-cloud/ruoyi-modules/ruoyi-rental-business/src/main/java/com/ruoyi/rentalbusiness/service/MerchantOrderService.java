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
import org.springframework.beans.factory.annotation.Autowired;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.log.service.MerchantBusinessAuditRecorder;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.rentalbusiness.domain.MerchantOrderRow;
import com.ruoyi.rentalbusiness.integration.amap.AmapWeatherClient;
import com.ruoyi.rentalbusiness.integration.amap.AmapWeatherSnapshot;
import com.ruoyi.rentalbusiness.mapper.MerchantOrderMapper;
import com.ruoyi.rentalbusiness.model.RegionAddressInfo;
import com.ruoyi.rentalbusiness.model.MerchantOrderListVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderQuery;
import com.ruoyi.rentalbusiness.model.MerchantOrderCreateCommand;

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
    private final CodeGeneratorClient codeGeneratorClient;
    private final MerchantBusinessAuditRecorder auditRecorder;
    private final OrderRecipientHistoryService recipientHistoryService;
    private final RegionAddressParser regionAddressParser;
    private final AmapWeatherClient amapWeatherClient;

    /**
     * 注入商户订单查询数据访问对象。
     *
     * @param orderMapper 商户订单 Mapper
     * @param codeGeneratorClient 统一业务编码客户端
     * @param auditRecorder 商户业务审计记录器
     * @param recipientHistoryService 收件人历史快照服务
     * @param regionAddressParser 行政区划解析公共服务
     * @param amapWeatherClient 高德天气客户端
     */
    @Autowired
    public MerchantOrderService(MerchantOrderMapper orderMapper, CodeGeneratorClient codeGeneratorClient,
            MerchantBusinessAuditRecorder auditRecorder, OrderRecipientHistoryService recipientHistoryService,
            RegionAddressParser regionAddressParser, AmapWeatherClient amapWeatherClient)
    {
        this.orderMapper = orderMapper;
        this.codeGeneratorClient = codeGeneratorClient;
        this.auditRecorder = auditRecorder;
        this.recipientHistoryService = recipientHistoryService;
        this.regionAddressParser = regionAddressParser;
        this.amapWeatherClient = amapWeatherClient;
    }

    /** 保留历史构造方式，供仅需要订单读写能力的测试使用。 */
    public MerchantOrderService(MerchantOrderMapper orderMapper, CodeGeneratorClient codeGeneratorClient)
    {
        this(orderMapper, codeGeneratorClient, null, null, null, null);
    }

    /** 保留查询服务的轻量构造方式，供现有列表单元测试和只读调用使用。 */
    public MerchantOrderService(MerchantOrderMapper orderMapper)
    {
        this(orderMapper, null);
    }

    /** 校验商户录单并创建订单主表记录。 */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void add(MerchantOrderCreateCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null || command.customerName == null || command.customerName.trim().isEmpty()
                || command.customerPhone == null || command.customerPhone.trim().isEmpty())
        {
            throw new com.ruoyi.common.core.exception.ServiceException("客户姓名和联系电话不能为空");
        }
        if (command.channelId == null
                || command.leaseStartDate == null || command.leaseEndDate == null
                || command.leaseStartDate.compareTo(command.leaseEndDate) > 0)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("订单渠道和租赁日期范围不能为空或不正确");
        }
        validateChannel(command.channelId);
        if (command.warehouseId != null) MerchantContextUtils.checkWarehouseAccess(command.warehouseId);
        command.orderNo = codeGeneratorClient.next("order");
        orderMapper.insertMerchantOrder(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), command,
                com.ruoyi.common.security.utils.SecurityUtils.getUserId());
        if (command.productId != null || command.packageId != null || command.snId != null)
        {
            orderMapper.insertMerchantOrderItem(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), command,
                    com.ruoyi.common.security.utils.SecurityUtils.getUserId());
        }
        MerchantOrderCreateCommand created = get(command.orderId);
        recordRecipientSnapshot(created, com.ruoyi.common.security.utils.SecurityUtils.getUserId());
        audit("订单管理", "CREATE", "ORDER", command.orderId, created.orderNo, "创建订单", null, created);
    }

    /** 校验并更新当前商户订单，避免跨商户修改订单数据。 */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void update(MerchantOrderCreateCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null || command.orderId == null || command.customerName == null
                || command.customerName.trim().isEmpty() || command.customerPhone == null
                || command.customerPhone.trim().isEmpty() || command.leaseStartDate == null
                || command.leaseEndDate == null || command.channelId == null
                || command.leaseStartDate.compareTo(command.leaseEndDate) > 0)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("订单编辑参数不完整或租赁日期范围不正确");
        }
        MerchantOrderCreateCommand before = get(command.orderId);
        // 编辑页面允许旧订单缺少新增字段，服务端统一补齐数据库非空布尔字段，禁止 null 进入更新 SQL。
        command.weatherRiskFlag = command.weatherRiskFlag == null ? Boolean.FALSE : command.weatherRiskFlag;
        command.priorityFlag = command.priorityFlag == null ? Boolean.FALSE : command.priorityFlag;
        command.orderStatus = command.orderStatus == null || command.orderStatus.trim().isEmpty()
                ? "pending_ship" : command.orderStatus;
        command.payStatus = command.payStatus == null || command.payStatus.trim().isEmpty()
                ? "unpaid" : command.payStatus;
        validateChannel(command.channelId);
        if (command.warehouseId != null) MerchantContextUtils.checkWarehouseAccess(command.warehouseId);
        int updated = orderMapper.updateMerchantOrder(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                command, com.ruoyi.common.security.utils.SecurityUtils.getUserId());
        if (updated == 0) throw new com.ruoyi.common.core.exception.ServiceException("订单不存在或无权编辑");
        MerchantOrderCreateCommand after = get(command.orderId);
        recordRecipientSnapshot(after, com.ruoyi.common.security.utils.SecurityUtils.getUserId());
        audit("订单管理", "UPDATE", "ORDER", command.orderId, after.orderNo, "编辑订单", before, after);
    }

    /**
     * 校验订单渠道主键是否属于当前商户可用范围。
     *
     * <p>平台启用的联盟渠道允许全部商户引用，商户自定义渠道只能由所属租户和商户引用；校验失败时
     * 在写入订单前终止请求，避免跨商户伪造 {@code channel_id} 关系。</p>
     *
     * @param channelId 待绑定的订单渠道主键
     */
    private void validateChannel(Long channelId)
    {
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        if (channelId == null || orderMapper.countUsableOrderChannel(tenantId, memberId, channelId) != 1)
        {
            throw new com.ruoyi.common.core.exception.ServiceException("订单渠道不存在、已停用或无权使用");
        }
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

    /** 查询当前商户订单详情，编辑页面只能读取当前登录上下文可见的订单。 */
    public MerchantOrderCreateCommand get(Long orderId)
    {
        MerchantOrderRow row = orderMapper.selectMerchantOrderById(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), orderId);
        if (row == null || row.orderId == null || !MerchantContextUtils.hasAllWarehouseAccess()
                && row.warehouseId != null && !MerchantContextUtils.hasWarehouseAccess(row.warehouseId))
        {
            throw new com.ruoyi.common.core.exception.ServiceException("订单不存在或无权查看");
        }
        MerchantOrderCreateCommand command = new MerchantOrderCreateCommand();
        command.orderId = row.orderId;
        command.orderNo = row.orderNo;
        command.externalOrderNo = row.externalOrderNo;
        command.channelId = row.channelId;
        command.shopName = row.shopName;
        command.customerName = row.customerName;
        command.customerPhone = row.customerPhone;
        command.customerAddress = row.customerAddress;
        command.warehouseId = row.warehouseId;
        command.returnWarehouseId = row.returnWarehouseId;
        command.leaseStartDate = formatDate(row.leaseStartDate);
        command.leaseEndDate = formatDate(row.leaseEndDate);
        command.shipDueTime = formatDateTime(row.shipDueTime);
        command.returnDueTime = formatDateTime(row.returnDueTime);
        command.rentAmount = row.rentAmount;
        command.depositAmount = row.depositAmount;
        command.orderStatus = row.orderStatus;
        command.payStatus = row.payStatus;
        command.timeEffectStatus = row.timeEffectStatus;
        command.weatherRiskFlag = row.weatherRiskFlag != null && row.weatherRiskFlag == 1;
        command.priorityFlag = row.priorityFlag != null && row.priorityFlag == 1;
        command.sourceTextSnapshot = row.sourceTextSnapshot;
        command.productId = row.productId;
        command.packageId = row.packageId;
        command.snId = row.snId;
        command.accessoryItemIds = row.accessoryItemIds;
        command.quantity = row.quantity;
        command.version = row.version;
        command.remark = row.remark;
        return command;
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
        view.orderId = row.orderId;
        view.leaseStartDate = formatDate(row.leaseStartDate);
        view.leaseEndDate = formatDate(row.leaseEndDate);
        view.externalOrderNo = row.externalOrderNo;
        view.orderNo = row.orderNo;
        view.model = defaultText(row.modelName, "商品待维护");
        view.packageName = defaultText(row.packageName, "未绑定套餐");
        view.rentalPeriod = formatDate(row.leaseStartDate) + " ~ " + formatDate(row.leaseEndDate);
        view.returnDate = formatDate(row.leaseEndDate);
        view.rent = row.rentAmount == null ? BigDecimal.ZERO : row.rentAmount;
        view.channelId = row.channelId;
        view.channel = defaultText(row.channelName, defaultText(row.channelCode, "未设置渠道"));
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
        view.operator = row.createBy == null ? "系统" : String.valueOf(row.createBy);
        view.coordinator = row.createBy == null ? "待分配" : String.valueOf(row.createBy);
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
     * 转换气象风险信息，先用统一地址解析器得到发货地和收货地的行政区划 adcode，再调用
     * 高德天气客户端获取实际天气；高德未配置或调用失败时保留稳定的“待查询”展示。
     *
     * @param row 数据库查询行
     * @return 气象风险展示对象
     */
    private MerchantOrderListVO.Weather toWeather(MerchantOrderRow row)
    {
        MerchantOrderListVO.Weather weather = new MerchantOrderListVO.Weather();
        RegionAddressInfo departureRegion = regionAddressParser == null ? null
                : regionAddressParser.parse(row.warehouseCity);
        RegionAddressInfo arrivalRegion = recipientRegion(row);
        String departureAdcode = weatherAdcode(departureRegion);
        String arrivalAdcode = weatherAdcode(arrivalRegion);
        AmapWeatherSnapshot departureWeather = amapWeatherClient == null
                ? AmapWeatherSnapshot.unavailable(departureAdcode, "天气服务未启用")
                : amapWeatherClient.query(departureAdcode);
        AmapWeatherSnapshot arrivalWeather = amapWeatherClient == null
                ? AmapWeatherSnapshot.unavailable(arrivalAdcode, "天气服务未启用")
                : amapWeatherClient.query(arrivalAdcode);

        weather.departCity = defaultText(departureRegion == null ? null : departureRegion.cityName,
                defaultText(row.warehouseCity, "发货地"));
        weather.arriveCity = defaultText(row.recipientCityName,
                defaultText(arrivalRegion == null ? null : arrivalRegion.cityName, "收货地"));
        weather.route = weather.departCity + " → " + weather.arriveCity;
        weather.depart = weatherText(departureWeather);
        weather.arrive = weatherText(arrivalWeather);
        weather.departIcon = defaultText(departureWeather.icon, "cloudy");
        weather.arriveIcon = defaultText(arrivalWeather.icon, "cloudy");
        weather.departReportTime = departureWeather.reportTime;
        weather.arriveReportTime = arrivalWeather.reportTime;
        weather.risk = row.weatherRiskFlag != null && row.weatherRiskFlag == 1
                || departureWeather.risk || arrivalWeather.risk;
        return weather;
    }

    /**
     * 取得订单收件地址的行政区划结果，历史快照缺失时兼容解析当前订单地址。
     *
     * @param row 订单查询行
     * @return 收件地址解析结果
     */
    private RegionAddressInfo recipientRegion(MerchantOrderRow row)
    {
        RegionAddressInfo result = new RegionAddressInfo();
        result.provinceCode = row.recipientProvinceCode;
        result.cityCode = row.recipientCityCode;
        result.districtCode = row.recipientDistrictCode;
        result.amapCityCode = row.recipientAmapCityCode;
        result.provinceName = row.recipientProvinceName;
        result.cityName = row.recipientCityName;
        result.districtName = row.recipientDistrictName;
        if (result.weatherAdcode() == null && regionAddressParser != null)
        {
            return regionAddressParser.parse(row.customerAddress);
        }
        return result;
    }

    /**
     * 取得天气服务应使用的行政区划 adcode，禁止把 citycode 误作为天气接口参数。
     *
     * @param region 地址解析结果
     * @return 区县、市或省级 adcode；解析结果为空时返回 null
     */
    private String weatherAdcode(RegionAddressInfo region)
    {
        return region == null ? null : region.weatherAdcode();
    }

    /**
     * 将高德天气快照转换为订单列表中的简短天气文本。
     *
     * @param snapshot 天气快照
     * @return 天气和温度展示文本
     */
    private String weatherText(AmapWeatherSnapshot snapshot)
    {
        if (snapshot == null || !snapshot.available || snapshot.weather == null || snapshot.weather.trim().isEmpty())
        {
            return "待查询";
        }
        return snapshot.temperature == null || snapshot.temperature.trim().isEmpty()
                ? snapshot.weather : snapshot.weather + " " + snapshot.temperature + "℃";
    }

    /**
     * 在订单主表保存成功后记录收件人历史快照。
     *
     * @param command 已保存订单命令
     * @param operatorId 操作人用户主键
     */
    private void recordRecipientSnapshot(MerchantOrderCreateCommand command, Long operatorId)
    {
        if (recipientHistoryService != null && command != null)
        {
            recipientHistoryService.record(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                    command.orderId, command.orderNo, command.customerName, command.customerPhone,
                    command.customerAddress, operatorId);
        }
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
     * 写入订单业务审计；单元测试未提供记录器时跳过审计，不改变订单主流程。
     *
     * @param module 业务模块
     * @param operationType 操作类型
     * @param targetType 业务对象类型
     * @param targetId 订单主键
     * @param targetName 订单编号
     * @param description 业务描述
     * @param before 变更前订单
     * @param after 变更后订单
     */
    private void audit(String module, String operationType, String targetType, Long targetId,
            String targetName, String description, Object before, Object after)
    {
        if (auditRecorder != null)
        {
            auditRecorder.record(module, operationType, targetType, String.valueOf(targetId), targetName,
                    description, before, after);
        }
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
        labels.put("shipped", "已发货");
        labels.put("shipping", "运输中");
        labels.put("renting", "租用中");
        labels.put("returning", "归还中");
        labels.put("overdue_return", "归还逾期");
        labels.put("overdue", "已逾期");
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
        types.put("shipped", "primary");
        types.put("shipping", "primary");
        types.put("renting", "success");
        types.put("returning", "warning");
        types.put("overdue_return", "danger");
        types.put("overdue", "danger");
        types.put("after_sales", "danger");
        types.put("completed", "success");
        types.put("cancelled", "info");
        return Collections.unmodifiableMap(types);
    }
}
