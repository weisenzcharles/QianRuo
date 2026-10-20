package com.ruoyi.rentalbusiness.config;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.system.api.model.PlatformLogisticsQuery;

/** 验证平台物流治理 Mapper 的查询 SQL 可以解析并绑定。 */
public class PlatformLogisticsMapperXmlTest
{
    @Test
    public void logisticsStatementsParseAndBind() throws Exception
    {
        String resource = "mapper/merchant/PlatformLogisticsMapper.xml";
        Configuration configuration = new Configuration();
        try (InputStream input = Resources.getResourceAsStream(resource))
        {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }
        PlatformLogisticsQuery query = new PlatformLogisticsQuery();
        query.setKeyword("SF");
        query.setMerchantKeyword("商户");
        query.setDirection(Integer.valueOf(1));
        query.setRouteStatus(Integer.valueOf(1));
        query.setAbnormalOnly(Boolean.TRUE);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("query", query);
        parameters.put("offset", Integer.valueOf(0));
        parameters.put("pageSize", Integer.valueOf(10));
        parameters.put("logisticsId", Long.valueOf(11L));
        parameters.put("tenantId", Long.valueOf(1L));
        parameters.put("memberId", Long.valueOf(2L));
        for (String statement : new String[] { "countLogistics", "selectLogistics", "selectLogisticsById",
                "selectLogisticsByOrder", "selectRoutes", "selectProviderCalls" })
        {
            String sql = configuration
                    .getMappedStatement("com.ruoyi.rentalbusiness.mapper.PlatformLogisticsMapper." + statement)
                    .getBoundSql(parameters).getSql();
            if ("selectLogisticsByOrder".equals(statement))
            {
                Assert.assertFalse(sql.toLowerCase().contains("ry_rental.ry_rental_member"));
            }
        }
    }
}
