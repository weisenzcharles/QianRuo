package com.ruoyi.system.rental;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;
import com.ruoyi.system.domain.rental.RentalMerchantUser;

public class RentalMerchantUserMapperXmlTest
{
    @Test
    public void mapperParsesAndReturnsMerchantCode() throws Exception
    {
        String resource = "mapper/rental/RentalMerchantUserMapper.xml";
        Configuration configuration = new Configuration();
        configuration.getTypeAliasRegistry().registerAlias("RentalMerchantUser", RentalMerchantUser.class);
        try (InputStream input = Resources.getResourceAsStream(resource))
        {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }

        String mapper;
        try (InputStream input = Resources.getResourceAsStream(resource))
        {
            mapper = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        require(mapper.contains("<result property=\"memberCode\" column=\"member_code\" />"),
                "merchant user result must expose merchant code");
        require(mapper.contains("m.member_code"), "merchant user query must select merchant code");
    }

    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }
}
