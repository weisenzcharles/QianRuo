package com.ruoyi.rentalbusiness.service;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;

/** 快递时效预估 SQL 契约测试。 */
public class ExpressTimeEstimateMapperContractTest
{
    /** 时效表必须按线路唯一键 upsert，且不能依赖用户审计字段或软删除字段。 */
    @Test
    public void mapper_shouldUseGlobalRouteCacheSchema() throws Exception
    {
        URL resource = getClass().getClassLoader().getResource("mapper/merchant/ExpressTimeEstimateMapper.xml");
        Assert.assertNotNull("express time mapper must exist", resource);
        String xml = Files.readString(Paths.get(resource.toURI())).toLowerCase();
        Assert.assertTrue(xml.contains("ry_business.ry_rental_express_time_estimate"));
        Assert.assertTrue(xml.contains("on duplicate key update"));
        Assert.assertTrue(xml.contains("from_acode"));
        Assert.assertTrue(xml.contains("delivery_expend_time"));
        Assert.assertFalse(xml.contains("is_delete"));
        Assert.assertFalse(xml.contains("create_by"));
        Assert.assertFalse(xml.contains("update_by"));
        Assert.assertFalse(xml.contains("remove_by"));
    }
}
