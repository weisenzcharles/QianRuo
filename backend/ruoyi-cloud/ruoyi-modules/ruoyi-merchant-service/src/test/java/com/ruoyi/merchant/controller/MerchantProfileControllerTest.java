package com.ruoyi.merchant.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class MerchantProfileControllerTest
{
    @Test
    public void acceptsPasswordWithinConfiguredLength()
    {
        assertTrue(MerchantProfileController.validPassword("12345"));
        assertTrue(MerchantProfileController.validPassword("13067766577"));
        assertTrue(MerchantProfileController.validPassword("12345678901234567890"));
    }

    @Test
    public void rejectsMissingOrOutOfRangePassword()
    {
        assertFalse(MerchantProfileController.validPassword(null));
        assertFalse(MerchantProfileController.validPassword(""));
        assertFalse(MerchantProfileController.validPassword("1234"));
        assertFalse(MerchantProfileController.validPassword("123456789012345678901"));
    }
}
