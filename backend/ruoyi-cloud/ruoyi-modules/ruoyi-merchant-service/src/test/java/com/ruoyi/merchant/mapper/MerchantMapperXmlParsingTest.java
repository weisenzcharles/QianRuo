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
                "mapper/merchant/MerchantBusinessAuditMapper.xml",
                "mapper/merchant/MerchantDepositMapper.xml",
                "mapper/merchant/MerchantKnowledgeMapper.xml",
                "mapper/merchant/MerchantInternalNoticeMapper.xml",
                "mapper/merchant/MerchantFinanceWithdrawalMapper.xml"))
        {
            try (InputStream input = Resources.getResourceAsStream(resource))
            {
                XMLMapperBuilder builder = new XMLMapperBuilder(
                        input, configuration, resource, configuration.getSqlFragments());
                builder.parse();
            }
        }
    }

    @Test
    public void depositMapperMustUseUnifiedFinanceAccountAndAppendLedger() throws Exception
    {
        String resource = "mapper/merchant/MerchantDepositMapper.xml";
        try (InputStream input = Resources.getResourceAsStream(resource))
        {
            String xml = new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            org.junit.Assert.assertTrue(xml.contains("ry_rental.ry_rental_finance_account"));
            org.junit.Assert.assertTrue(xml.contains("account_type = #{accountType}"));
            org.junit.Assert.assertTrue(xml.contains("insertFinanceLedger"));
            org.junit.Assert.assertFalse(xml.contains("update ry_rental.ry_rental_alliance_deposit_account"));
        }
    }

    @Test
    public void withdrawalMapperMustLockIdempotencyBeforeAccountMutation() throws Exception
    {
        String resource = "mapper/merchant/MerchantFinanceWithdrawalMapper.xml";
        try (InputStream input = Resources.getResourceAsStream(resource))
        {
            String xml = new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            org.junit.Assert.assertTrue(xml.contains("selectByIdempotencyForUpdate"));
            org.junit.Assert.assertTrue(xml.contains("limit 1 for update"));
            org.junit.Assert.assertTrue(xml.contains("uk_rental_finance_withdrawal_request") ||
                    xml.contains("ry_rental_finance_withdrawal"));
        }
    }

    @Test
    public void allianceOrderSlotMapperMustExposeIdempotentReservationStatements() throws Exception
    {
        String resource = "mapper/merchant/MerchantAllianceMapper.xml";
        try (InputStream input = Resources.getResourceAsStream(resource))
        {
            XMLMapperBuilder builder = new XMLMapperBuilder(input, new Configuration(), resource,
                    new Configuration().getSqlFragments());
            builder.parse();
        }
        String xml;
        try (InputStream input = Resources.getResourceAsStream(resource))
        {
            xml = new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
        org.junit.Assert.assertTrue(xml.contains("insertOrderSlot"));
        org.junit.Assert.assertTrue(xml.contains("insert ignore into ry_rental.ry_rental_alliance_order_slot"));
        org.junit.Assert.assertTrue(xml.contains("current_accept_order_count = current_accept_order_count + 1"));
        org.junit.Assert.assertTrue(xml.contains("slot_status = 2"));
        org.junit.Assert.assertTrue(xml.contains("am.join_time as join_time"));
        org.junit.Assert.assertTrue(xml.contains("am.exit_time as exit_time"));
        org.junit.Assert.assertTrue(xml.contains("a.max_member_count as maxMemberCount"));
        org.junit.Assert.assertTrue(xml.contains("a.join_mode as joinMode"));
        org.junit.Assert.assertTrue(xml.contains("am.member_status in (1, 2, 3)"));
        org.junit.Assert.assertTrue(xml.contains("not (member_status = 3 and #{status} = 1)"));
    }
}
