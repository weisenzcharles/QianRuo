package com.ruoyi.rentalbusiness.config;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;

/** 验证商户工作台任务和日报 Mapper 的动态 SQL 可以解析并绑定。 */
public class MerchantWorkTaskMapperXmlTest
{
    @Test
    public void workTaskAndDailyReportStatementsParseAndBind() throws Exception
    {
        String resource = "mapper/merchant/MerchantWorkTaskMapper.xml";
        Configuration configuration = new Configuration();
        try (InputStream input = Resources.getResourceAsStream(resource))
        {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("tenantId", Long.valueOf(1L));
        parameters.put("memberId", Long.valueOf(2L));
        parameters.put("currentUserId", Long.valueOf(3L));
        parameters.put("taskId", Long.valueOf(11L));
        parameters.put("warehouseId", Long.valueOf(5L));
        parameters.put("warehouseIds", Set.of(Long.valueOf(5L)));
        parameters.put("allWarehouseAccess", Boolean.TRUE);
        parameters.put("reportDate", LocalDate.of(2026, 9, 28));
        for (String statement : new String[] { "selectTaskList", "selectSummary", "selectDailyReport", "selectTask" })
        {
            configuration.getMappedStatement("com.ruoyi.rentalbusiness.mapper.MerchantWorkTaskMapper." + statement)
                    .getBoundSql(parameters);
        }
    }
}
