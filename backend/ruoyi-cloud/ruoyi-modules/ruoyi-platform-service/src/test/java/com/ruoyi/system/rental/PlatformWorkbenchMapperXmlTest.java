package com.ruoyi.system.rental;

import java.io.InputStream;
import java.util.Collections;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;

/** 验证平台工作台待办查询 SQL 可以被 MyBatis 正确解析。 */
public class PlatformWorkbenchMapperXmlTest
{
    @Test
    public void workbenchStatementsParseAndBind() throws Exception
    {
        String resource = "mapper/rental/PlatformWorkbenchMapper.xml";
        Configuration configuration = new Configuration();
        try (InputStream input = Resources.getResourceAsStream(resource))
        {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }
        for (String statement : new String[] { "countOpenRiskTasks", "countOpenTicketTasks",
                "countPendingWithdrawalTasks", "sumPendingWithdrawalAmount", "countOpenInvoiceTasks",
                "countOpenMarketReportTasks", "selectRiskTasks", "selectTicketTasks", "selectWithdrawalTasks",
                "selectInvoiceTasks", "selectMarketReportTasks" })
        {
            configuration.getMappedStatement("com.ruoyi.system.mapper.rental.PlatformWorkbenchMapper." + statement)
                    .getBoundSql(Collections.singletonMap("limit", Integer.valueOf(50)));
        }
    }
}
