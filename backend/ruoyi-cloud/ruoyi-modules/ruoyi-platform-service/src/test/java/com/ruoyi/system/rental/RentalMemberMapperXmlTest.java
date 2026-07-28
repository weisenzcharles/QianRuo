package com.ruoyi.system.rental;

import java.io.InputStream;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;

public class RentalMemberMapperXmlTest
{
    @Test
    public void mapperParsesAndContainsCompleteProfileColumns() throws Exception
    {
        String resource = "mapper/rental/RentalMemberMapper.xml";
        Configuration configuration = new Configuration();
        try (InputStream input = Resources.getResourceAsStream(resource))
        {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }

        String mapper;
        try (InputStream input = Resources.getResourceAsStream(resource))
        {
            mapper = new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
        require(mapper.contains("store_logo_url"), "store profile columns must be mapped");
        require(mapper.contains("license_image_url"), "enterprise license columns must be mapped");
        require(mapper.contains("contract_file_url"), "contract columns must be mapped");
        require(mapper.contains("as main_account"), "owner account must come from the merchant binding");
    }

    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }
}
