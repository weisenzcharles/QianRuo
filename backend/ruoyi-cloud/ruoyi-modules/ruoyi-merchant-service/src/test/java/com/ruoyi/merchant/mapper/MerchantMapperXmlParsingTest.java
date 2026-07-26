package com.ruoyi.merchant.mapper;

import java.io.InputStream;
import java.util.Arrays;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;

/** Verifies that every merchant mapper can be parsed with the production alias configuration. */
public class MerchantMapperXmlParsingTest
{
    @Test
    public void allMapperXmlFilesCanBeParsed() throws Exception
    {
        Configuration configuration = new Configuration();
        configuration.getTypeAliasRegistry().registerAliases("com.ruoyi.merchant.domain");

        for (String resource : Arrays.asList(
                "mapper/merchant/MerchantIdentityMapper.xml",
                "mapper/merchant/MerchantMenuMapper.xml",
                "mapper/merchant/MerchantRoleMapper.xml",
                "mapper/merchant/MerchantOperLogMapper.xml"))
        {
            try (InputStream input = Resources.getResourceAsStream(resource))
            {
                XMLMapperBuilder builder = new XMLMapperBuilder(
                        input, configuration, resource, configuration.getSqlFragments());
                builder.parse();
            }
        }
    }
}
