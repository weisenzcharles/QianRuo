package com.ruoyi.system.rental;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;
import com.ruoyi.system.model.rental.PlatformAlertEventQuery;

/** 验证平台运维告警 Mapper SQL 可以被 MyBatis 正确解析。 */
public class PlatformAlertEventMapperXmlTest
{
    @Test
    public void alertStatementsParseAndBind() throws Exception
    {
        String resource = "mapper/rental/PlatformAlertEventMapper.xml";
        Configuration configuration = new Configuration();
        try (InputStream input = Resources.getResourceAsStream(resource))
        {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("query", new PlatformAlertEventQuery());
        params.put("alertEventKey", "JOB_FAILURE:测试任务:test()");
        params.put("title", "调度任务执行失败");
        params.put("detail", "测试异常");
        params.put("sourceKey", "test()");
        params.put("occurrenceCount", Long.valueOf(1L));
        params.put("operator", Long.valueOf(1L));
        params.put("alertEventId", Long.valueOf(1L));
        params.put("remark", "已处理");
        params.put("alertEventKeys", Collections.singletonList("JOB_FAILURE:测试任务:test()"));
        params.put("alertLevel", Integer.valueOf(3));
        for (String statement : new String[] { "selectList", "selectRecentFailedJobs", "upsertScheduledTaskAlert",
                "resolveMissingScheduledTaskAlerts", "countOpenEvents", "selectById", "acknowledge", "resolve" })
        {
            configuration.getMappedStatement("com.ruoyi.system.mapper.rental.PlatformAlertEventMapper." + statement)
                    .getBoundSql(params);
        }
        for (String statement : new String[] { "upsertStorageAlert", "resolveStorageAlert",
                "upsertExternalInterfaceAlert", "resolveExternalInterfaceAlert" })
        {
            configuration.getMappedStatement("com.ruoyi.system.mapper.rental.PlatformAlertEventMapper." + statement)
                    .getBoundSql(params);
        }
    }
}
