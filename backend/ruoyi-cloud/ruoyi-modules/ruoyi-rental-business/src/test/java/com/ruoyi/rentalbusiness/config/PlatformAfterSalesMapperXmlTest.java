package com.ruoyi.rentalbusiness.config;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.system.api.model.PlatformAfterSalesQuery;

/** 验证平台售后 Mapper 的查询和状态写入 SQL 可以解析并绑定。 */
public class PlatformAfterSalesMapperXmlTest
{
    @Test
    public void afterSalesStatementsParseAndBind() throws Exception
    {
        String resource = "mapper/merchant/PlatformAfterSalesMapper.xml";
        Configuration configuration = new Configuration();
        try (InputStream input = Resources.getResourceAsStream(resource))
        {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("query", new PlatformAfterSalesQuery());
        parameters.put("caseId", Long.valueOf(11L));
        parameters.put("tenantId", Long.valueOf(1L));
        parameters.put("memberId", Long.valueOf(2L));
        parameters.put("orderId", Long.valueOf(3L));
        parameters.put("offset", Integer.valueOf(0));
        parameters.put("pageSize", Integer.valueOf(10));
        parameters.put("fromStatus", Integer.valueOf(0));
        parameters.put("toStatus", Integer.valueOf(1));
        parameters.put("eventType", Integer.valueOf(2));
        parameters.put("content", "处理完成");
        parameters.put("evidenceType", Integer.valueOf(1));
        parameters.put("fileUrl", "https://oss.example.com/after-sales/photo.jpg");
        parameters.put("fileName", "photo.jpg");
        parameters.put("fileMime", "image/jpeg");
        parameters.put("description", "设备外观照片");
        parameters.put("operatorId", Long.valueOf(99L));
        parameters.put("operatorName", "平台管理员");
        parameters.put("fieldName", "订单状态");
        parameters.put("beforeValue", "70");
        parameters.put("afterValue", "50");
        parameters.put("beforeDisplayValue", "售后中");
        parameters.put("afterDisplayValue", "已完成");
        parameters.put("changeReason", "售后处理完成");
        for (String statement : new String[] {"countCases", "selectCases", "selectCase", "selectCaseForUpdate",
                "selectCasesByOrder",
                "selectEvents", "selectEvidence", "updateCaseStatus", "insertEvent", "insertEvidence",
                "countProcessingByOrder",
                "updateOrderStatus", "insertOrderEditLog"})
        {
            String sql = configuration
                    .getMappedStatement("com.ruoyi.rentalbusiness.mapper.PlatformAfterSalesMapper." + statement)
                    .getBoundSql(parameters).getSql();
            if ("selectCasesByOrder".equals(statement))
            {
                Assert.assertFalse(sql.toLowerCase().contains("ry_rental.ry_rental_member"));
            }
        }
    }
}
