package com.ruoyi.rentalbusiness.model;

import java.util.Collections;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

/** 商户订单物流轨迹响应对象，只返回轨迹弹窗实际需要的展示字段。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantLogisticsTrackingVO
{
    public String orderNo;
    public String direction;
    public String carrier;
    public String trackingNo;
    public boolean available;
    public String status;
    public String message;
    public List<Event> events = Collections.emptyList();

    /** 单条物流轨迹节点。 */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Event
    {
        public String time;
        public String status;
        public String description;
        public String location;
    }
}
