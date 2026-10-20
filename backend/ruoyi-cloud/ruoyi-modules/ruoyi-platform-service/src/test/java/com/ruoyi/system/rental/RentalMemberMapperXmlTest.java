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
        require(mapper.contains("#{memberCode}"), "overview must support merchant code filtering");
        require(mapper.contains("#{nickname}"), "overview must support merchant name filtering");
        require(mapper.contains("#{legalSubjectName}"), "overview must support subject name filtering");
        require(mapper.contains("#{memberType}"), "overview must support merchant type filtering");
        require(mapper.contains("#{authStatus}"), "overview must support auth status filtering");
        require(mapper.contains("#{status}"), "overview must support frozen status filtering");
        require(mapper.contains("id=\"PlatformMerchantOverviewBaseResult\""),
                "overview must explicitly map snake case merchant fields");
        require(mapper.contains("id=\"PlatformMerchantBusinessScopeResult\""),
                "business scope must explicitly map merchant identity fields");
    }

    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }
}
