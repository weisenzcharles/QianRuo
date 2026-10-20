package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 商户订单列表响应对象。
 *
 * 响应按订单工作台的展示域组织，隐藏数据库表连接细节，同时只返回当前
 * 登录商户有权查看的订单内容。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantOrderListVO
{
    public Long orderId;
    public boolean isAllianceChild;
    public boolean isAllianceSourceLocked;
    public String leaseStartDate;
    public String leaseEndDate;
    public Integer leaseDays;
    public String arrivalDate;
    public String externalOrderNo;
    public String orderNo;
    public Long warehouseId;
    public Long returnWarehouseId;
    public String returnWarehouseName;
    public Long productId;
    public Long packageId;
    public String model;
    public String packageName;
    public String rentalPeriod;
    public String returnDate;
    public BigDecimal rent;
    public BigDecimal deliveryAmount;
    public BigDecimal insuranceAmount;
    public Long channelId;
    public String channel;
    public String merchant;
    public String recipientName;
    public String recipientPhone;
    public String recipientAddress;
    public Customer customer;
    public Shipping shipping;
    public Weather weather;
    public Delivery delivery;
    public Integer status;
    /** 当前订单寄出方向的快递公司编码，仅供物流编辑表单回填。 */
    public String expressCompany;
    public String trackingNo;
    /** 当前订单归还方向的快递公司编码，仅供物流编辑表单回填。 */
    public String returnExpressCompany;
    public String returnNo;
    public String createdAt;
    public String productCode;
    public List<String> packageItems;
    public PackageDetail packageDetail;
    public List<String> addOns;
    public String attention;
    public String operator;
    public String coordinator;

    /** 客户信息展示对象。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Customer
    {
        public String nickname;
        public String name;
        public String phone;
        public String address;
        public String note;
    }

    /** 发货安排展示对象。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Shipping
    {
        public String warehouse;
        public String shipDate;
        public String method;
        public String timeSlot;
        public String doorTime;
        public String note;
    }

    /** 气象风险展示对象。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Weather
    {
        public String route;
        public DayForecast today;
        public DayForecast tomorrow;
        public boolean risk;
    }

    /** 订单列表单日发货地和收货地天气概况。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DayForecast
    {
        public String depart;
        public String arrive;
        public String departIcon;
        public String arriveIcon;
    }

    /** 配送方案展示对象。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Delivery
    {
        public List<DeliveryPlan> plans;
        public String expectedDate;
    }

    /** 单条配送方案展示对象。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DeliveryPlan
    {
        public String carrier;
        public String name;
        public BigDecimal price;
    }

    /** 订单下单时固化的套餐展示详情，不把技术主键作为用户可读文本返回。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PackageDetail
    {
        public String packageName;
        public String packageCode;
        public MainProduct mainProduct;
        public List<Item> items;
        /** 订单下单时固化的额外配件名称和数量展示文本。 */
        public List<String> extraAccessories;
    }

    /** 套餐主商品展示信息。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MainProduct
    {
        public String name;
        public String code;
        public String model;
    }

    /** 套餐单条明细展示信息。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Item
    {
        /** 套餐快照 items 数组下标，仅供选择设备接口定位明细。 */
        public Integer itemIndex;
        public String itemType;
        public String name;
        public String code;
        public String model;
        public Integer quantity;
        public String requiredText;
        /** 是否为支持绑定库存设备的逐件商品。 */
        public Boolean selectableDevice;
        /** 已绑定的实物序列号，仅用于套餐详情展示。 */
        public List<String> serialNumbers;
        /** 库存单元唯一编号，仅供选择设备接口回传，不在页面展示。 */
        public List<Long> unitIds;
    }
}
