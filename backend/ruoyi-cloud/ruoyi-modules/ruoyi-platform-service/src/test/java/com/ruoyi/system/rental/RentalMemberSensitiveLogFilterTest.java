package com.ruoyi.system.rental;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import com.ruoyi.common.log.aspect.LogAspect;

public class RentalMemberSensitiveLogFilterTest
{
    @Test
    public void merchantSensitiveFieldsAreExcludedFromOperationLogs()
    {
        List<String> excluded = Arrays.asList(LogAspect.EXCLUDE_PROPERTIES);
        for (String field : Arrays.asList(
                "mainAccount", "contactPhone", "servicePhone", "contactEmail", "alipayRealName", "alipayAccount",
                "licenseImageUrl", "legalRepresentativeName", "legalRepresentativeIdNo",
                "legalIdFrontUrl", "legalIdBackUrl", "contractFileUrl"))
        {
            require(excluded.contains(field), field + " must be excluded from operation logs");
        }
    }

    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }
}
