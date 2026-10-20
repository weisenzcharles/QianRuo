package com.ruoyi.system.rental;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;

/** 验证平台周转市场 Mapper 的查询和治理 SQL 可以解析。 */
public class PlatformMarketMapperXmlTest
{
    @Test
    public void marketStatementsParseAndBind() throws Exception
    {
        String resource = "mapper/rental/PlatformMarketMapper.xml";
        Configuration configuration = new Configuration();
        try (InputStream input = Resources.getResourceAsStream(resource))
        {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("query", new com.ruoyi.system.model.rental.PlatformMarketQuery());
        parameters.put("postId", Long.valueOf(1L));
        parameters.put("reportId", Long.valueOf(2L));
        parameters.put("postStatus", Integer.valueOf(3));
        parameters.put("downRankFlag", Integer.valueOf(1));
        parameters.put("reportStatus", Integer.valueOf(1));
        parameters.put("reviewResult", "已处理");
        parameters.put("reviewerId", Long.valueOf(1L));
        parameters.put("operatorId", Long.valueOf(1L));
        for (String statement : new String[] { "selectPosts", "selectPost", "reviewReport", "updatePostStatus",
                "updatePostRank" })
        {
            configuration.getMappedStatement("com.ruoyi.system.mapper.rental.PlatformMarketMapper." + statement)
                    .getBoundSql(parameters);
        }
        String reportsSql = configuration.getMappedStatement(
                "com.ruoyi.system.mapper.rental.PlatformMarketMapper.selectReports")
                .getBoundSql(parameters).getSql();
        assertTrue(reportsSql.contains("is_delete = 0"));
        assertFalse(reportsSql.contains("del_flag"));
        String reportSql = configuration.getMappedStatement(
                "com.ruoyi.system.mapper.rental.PlatformMarketMapper.selectReport")
                .getBoundSql(parameters).getSql();
        assertTrue(reportSql.contains("is_delete = 0"));
        assertFalse(reportSql.contains("del_flag"));
    }
}
