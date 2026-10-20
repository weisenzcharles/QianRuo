package com.ruoyi.merchant.mapper;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;

/** 商户资料查询必须返回等级编码，名称由共享系统字典统一解析。 */
public class MerchantStoreSettingsMapperContractTest
{
    @Test
    public void selectStoreProfile_shouldReturnRawLevelCodeForDictionaryResolution() throws Exception
    {
        URL resource = getClass().getClassLoader().getResource("mapper/merchant/MerchantStoreSettingsMapper.xml");
        Assert.assertNotNull("store settings mapper must exist", resource);
        String xml = Files.readString(Paths.get(resource.toURI()));
        int queryStart = xml.indexOf("<select id=\"selectStoreProfile\"");
        int queryEnd = xml.indexOf("</select>", queryStart);
        Assert.assertTrue("store profile query must exist", queryStart >= 0 && queryEnd > queryStart);
        String query = xml.substring(queryStart, queryEnd);

        Assert.assertTrue(query.contains("ry_rental_merchant_level"));
        Assert.assertTrue(query.contains("ml.level_id = m.level_id"));
        Assert.assertTrue(query.contains("ml.level_name"));
        Assert.assertTrue(query.contains("m.vip_level"));
        Assert.assertTrue(query.contains("as vip_level_name"));
        Assert.assertFalse(query.contains("when 'junior' then '初级会员'"));
    }
}
