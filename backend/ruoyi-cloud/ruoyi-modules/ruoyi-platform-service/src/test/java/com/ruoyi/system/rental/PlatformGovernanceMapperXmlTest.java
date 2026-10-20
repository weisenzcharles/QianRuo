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
import com.ruoyi.system.model.rental.PlatformKnowledgeQuery;

/** 验证平台治理 Mapper 的风险事件 SQL 可以被 MyBatis 正确解析。 */
public class PlatformGovernanceMapperXmlTest
{
    @Test
    public void riskEventStatementsParseAndBind() throws Exception
    {
        String resource = "mapper/rental/PlatformGovernanceMapper.xml";
        Configuration configuration = new Configuration();
        try (InputStream input = Resources.getResourceAsStream(resource))
        {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("riskEventKey", "1:测试商户");
        parameters.put("riskType", Integer.valueOf(1));
        parameters.put("riskLevel", Integer.valueOf(2));
        parameters.put("subject", "测试商户");
        parameters.put("riskCount", Long.valueOf(1L));
        parameters.put("detail", "存在逾期订单");
        parameters.put("operator", Long.valueOf(1L));
        parameters.put("riskEventId", Long.valueOf(9L));
        parameters.put("riskEventKeys", java.util.Collections.singletonList("1:测试商户"));

        configuration.getMappedStatement(
                "com.ruoyi.system.mapper.rental.PlatformGovernanceMapper.upsertRiskEvent")
                .getBoundSql(parameters);
        configuration.getMappedStatement(
                "com.ruoyi.system.mapper.rental.PlatformGovernanceMapper.resolveMissingRiskEvents")
                .getBoundSql(parameters);
        configuration.getMappedStatement(
                "com.ruoyi.system.mapper.rental.PlatformGovernanceMapper.acknowledgeRiskEvent")
                .getBoundSql(parameters);
        String ticketSql = configuration.getMappedStatement(
            "com.ruoyi.system.mapper.rental.PlatformGovernanceMapper.selectTicketAssignees")
            .getBoundSql(parameters).getSql();
        assertTrue(ticketSql.contains("is_delete = 0"));
        assertFalse(ticketSql.contains("del_flag"));
        String assigneeSql = configuration.getMappedStatement(
            "com.ruoyi.system.mapper.rental.PlatformGovernanceMapper.countActiveTicketAssignee")
            .getBoundSql(parameters).getSql();
        assertTrue(assigneeSql.contains("is_delete = 0"));
        assertFalse(assigneeSql.contains("del_flag"));
        parameters.put("articleId", Long.valueOf(9L));
        configuration.getMappedStatement(
            "com.ruoyi.system.mapper.rental.PlatformGovernanceMapper.insertKnowledgeRecycle")
            .getBoundSql(parameters);
        configuration.getMappedStatement(
            "com.ruoyi.system.mapper.rental.PlatformGovernanceMapper.deleteKnowledgeArticle")
            .getBoundSql(parameters);
        parameters.put("memberId", Long.valueOf(27L));
        parameters.put("actionType", Integer.valueOf(1));
        parameters.put("reason", "测试违规原因");
        parameters.put("query", new PlatformKnowledgeQuery());
        configuration.getMappedStatement(
            "com.ruoyi.system.mapper.rental.PlatformGovernanceMapper.selectKnowledgeComplianceArticles")
            .getBoundSql(parameters);
        configuration.getMappedStatement(
            "com.ruoyi.system.mapper.rental.PlatformGovernanceMapper.selectKnowledgeComplianceArticle")
            .getBoundSql(parameters);
        configuration.getMappedStatement(
            "com.ruoyi.system.mapper.rental.PlatformGovernanceMapper.selectKnowledgeComplianceVersions")
            .getBoundSql(parameters);
        configuration.getMappedStatement(
            "com.ruoyi.system.mapper.rental.PlatformGovernanceMapper.insertKnowledgeViolation")
            .getBoundSql(parameters);
    }
}
