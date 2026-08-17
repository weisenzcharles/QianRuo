package com.ruoyi.rentalbusiness.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商户订单列表响应对象。
 *
 * 响应按订单工作台的展示域组织，隐藏数据库表连接细节，同时只返回当前
 * 登录商户有权查看的订单内容。
 */
public class MerchantOrderListVO
{
    public Long orderId;
    public String leaseStartDate;
    public String leaseEndDate;
    public String externalOrderNo;
    public String orderNo;
    public String model;
    public String packageName;
    public String rentalPeriod;
    public String returnDate;
    public BigDecimal rent;
    public Long channelId;
    public String channel;
    public String merchant;
    public Customer customer;
    public Shipping shipping;
    public Weather weather;
    public Delivery delivery;
    public String status;
    public String statusType;
    public String trackingNo;
    public String returnNo;
    public String createdAt;
    public String productCode;
    public List<String> addOns;
    public String attention;
    public String operator;
    public String coordinator;

    /** 客户信息展示对象。 */
    public static class Customer
    {
        public String nickname;
        public String name;
        public String phone;
        public String address;
        public String note;
    }

    /** 发货安排展示对象。 */
    public static class Shipping
    {
        public String warehouse;
        public String shipDate;
        public String method;
        public String doorTime;
        public String note;
    }

    /** 气象风险展示对象。 */
    public static class Weather
    {
        public String route;
        public String depart;
        public String arrive;
        public String departIcon;
        public String arriveIcon;
        public String departCity;
        public String arriveCity;
        public String departReportTime;
        public String arriveReportTime;
        public boolean risk;
    }

    /** 配送方案展示对象。 */
    public static class Delivery
    {
        public List<DeliveryPlan> plans;
        public String expectedDate;
    }

    /** 单条配送方案展示对象。 */
    public static class DeliveryPlan
    {
        public String carrier;
        public String name;
        public BigDecimal price;
    }
}
