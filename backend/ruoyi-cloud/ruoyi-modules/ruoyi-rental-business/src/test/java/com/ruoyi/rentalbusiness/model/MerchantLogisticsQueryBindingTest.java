package com.ruoyi.rentalbusiness.model;

import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.DataBinder;

/** 验证物流治理列表的请求参数可以通过 Spring MVC 默认绑定器写入查询对象。 */
public class MerchantLogisticsQueryBindingTest
{
    @Test
    public void bindRequestParameters_物流筛选条件_正确写入查询对象()
    {
        MerchantLogisticsQuery query = new MerchantLogisticsQuery();
        DataBinder binder = new DataBinder(query);
        binder.bind(new MutablePropertyValues(Map.of(
                "keyword", "OD0000000001",
                "direction", "1",
                "routeStatus", "0",
                "abnormalOnly", "true")));

        Assert.assertEquals("OD0000000001", query.getKeyword());
        Assert.assertEquals(Integer.valueOf(1), query.getDirection());
        Assert.assertEquals(Integer.valueOf(0), query.getRouteStatus());
        Assert.assertTrue(query.isAbnormalOnly());
    }
}
