package com.ruoyi.system;

import java.io.InputStream;
import java.util.Collections;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;

/** 验证商户编码最大序号查询会统计所有历史编码。 */
public class SysCodeRuleMapperXmlTest
{
    @Test
    public void maxMemberCodeSequenceIncludesSoftDeletedRows() throws Exception
    {
        String resource = "mapper/system/SysCodeRuleMapper.xml";
        Configuration configuration = new Configuration();
        try (InputStream input = Resources.getResourceAsStream(resource))
        {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }

        String sql = configuration
                .getMappedStatement("com.ruoyi.system.mapper.SysCodeRuleMapper.selectMaxMemberCodeSequence")
                .getBoundSql(Collections.singletonMap("prefix", "M"))
                .getSql()
                .toLowerCase(java.util.Locale.ROOT);

        require(sql.contains("ry_rental.ry_rental_member"), "query must inspect persisted merchant codes");
        require(sql.contains("regexp"), "query must only parse numeric code suffixes");
        require(!sql.contains("is_delete"), "query must include soft-deleted codes reserved by the unique key");
    }

    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }
}
