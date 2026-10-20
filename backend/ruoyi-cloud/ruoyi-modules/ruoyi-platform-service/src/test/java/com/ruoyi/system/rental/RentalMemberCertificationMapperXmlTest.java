package com.ruoyi.system.rental;

import java.io.InputStream;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;

/** 验证认证补件 Mapper 的结果映射和更新语句可以被 MyBatis 解析。 */
public class RentalMemberCertificationMapperXmlTest
{
    @Test
    public void mapperParsesSupplementStatements() throws Exception
    {
        String resource = "mapper/rental/RentalMemberCertificationMapper.xml";
        Configuration configuration = new Configuration();
        try (InputStream input = Resources.getResourceAsStream(resource))
        {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }
        require(configuration.getMappedStatement(
                "com.ruoyi.system.mapper.rental.RentalMemberCertificationMapper.updateCertificationSupplement")
                != null, "supplement update statement must be mapped");
    }

    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }
}
