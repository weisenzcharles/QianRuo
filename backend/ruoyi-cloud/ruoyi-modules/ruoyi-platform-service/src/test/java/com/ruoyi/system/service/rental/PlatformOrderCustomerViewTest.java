package com.ruoyi.system.service.rental;

import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.system.api.model.PlatformOrderView;
import com.ruoyi.system.model.rental.PlatformOrderCustomerVO;

/** 平台订单完整客户信息最小视图和空值兜底测试。 */
public class PlatformOrderCustomerViewTest
{
    @Test
    public void toBrowserCustomer_完整客户字段_不脱敏且不暴露技术编号()
    {
        PlatformOrderView source = new PlatformOrderView();
        source.setOrderId(Long.valueOf(99L));
        source.setTenantId(Long.valueOf(7L));
        source.setMemberId(Long.valueOf(8L));
        source.setOrderNo("OD0000000099");
        source.setCustomerName("张三");
        source.setCustomerPhone("13800138000");
        source.setRecipientName("李四");
        source.setRecipientPhone("13900139000");
        source.setRecipientAddress("浙江省杭州市西湖区完整收货地址");

        PlatformOrderCustomerVO result = PlatformOrderService.toBrowserCustomer(source);

        Assert.assertEquals("OD0000000099", result.getOrderNo());
        Assert.assertEquals("13800138000", result.getCustomerPhone());
        Assert.assertEquals("13900139000", result.getRecipientPhone());
        Assert.assertEquals("浙江省杭州市西湖区完整收货地址", result.getRecipientAddress());
    }

    @Test
    public void toBrowserCustomer_字段为空_返回稳定占位符()
    {
        PlatformOrderCustomerVO result = PlatformOrderService.toBrowserCustomer(new PlatformOrderView());

        Assert.assertEquals("-", result.getOrderNo());
        Assert.assertEquals("-", result.getCustomerName());
        Assert.assertEquals("-", result.getCustomerPhone());
        Assert.assertEquals("-", result.getRecipientAddress());
    }
}
