package com.ruoyi.system.rental;

import java.util.Date;
import org.junit.Test;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.system.domain.rental.RentalMember;
import com.ruoyi.system.service.rental.RentalMemberLifecyclePolicy;

public class RentalMemberLifecyclePolicyTest
{
    private final RentalMemberLifecyclePolicy policy = new RentalMemberLifecyclePolicy();

    @Test
    public void createStampsAuditedAndFrozenStates()
    {
        Date now = new Date(123456789L);
        RentalMember member = member("rejected", "1");

        policy.prepareForCreate(member, now);

        require(now.equals(member.getAuditTime()), "audited member should receive audit time");
        require(now.equals(member.getFreezeTime()), "frozen member should receive freeze time");
    }

    @Test
    public void updatePreservesExistingSystemTimesUntilStateChanges()
    {
        Date oldAuditTime = new Date(1000L);
        Date oldFreezeTime = new Date(2000L);
        Date now = new Date(3000L);
        RentalMember previous = member("verified", "1");
        previous.setAuditTime(oldAuditTime);
        previous.setFreezeTime(oldFreezeTime);
        RentalMember current = member("verified", "1");

        policy.prepareForUpdate(previous, current, now);

        require(oldAuditTime.equals(current.getAuditTime()), "unchanged audit state should preserve audit time");
        require(oldFreezeTime.equals(current.getFreezeTime()), "unchanged freeze state should preserve freeze time");
    }

    @Test
    public void unfreezeClearsFreezeMetadata()
    {
        RentalMember previous = member("pending", "1");
        previous.setFreezeTime(new Date(1000L));
        RentalMember current = member("pending", "0");
        current.setFreezeReason("旧冻结原因");

        policy.prepareForUpdate(previous, current, new Date(2000L));

        require(current.getFreezeTime() == null, "unfrozen member should not retain freeze time");
        require(current.getFreezeReason() == null, "unfrozen member should not retain freeze reason");
    }

    @Test
    public void longTermLicenseClearsEndDateAndInvalidContractIsRejected()
    {
        RentalMember longTerm = member("pending", "0");
        longTerm.setLicenseLongTerm(1);
        longTerm.setLicenseValidTo(new Date(1000L));
        policy.prepareForCreate(longTerm, new Date(2000L));
        require(longTerm.getLicenseValidTo() == null, "long-term license should not persist an end date");

        RentalMember invalid = member("pending", "0");
        invalid.setContractValidFrom(new Date(3000L));
        invalid.setContractValidTo(new Date(2000L));
        try
        {
            policy.prepareForCreate(invalid, new Date(4000L));
            throw new AssertionError("invalid contract period should be rejected");
        }
        catch (ServiceException expected)
        {
            require(expected.getMessage().contains("合同有效期"), "error should identify the contract period");
        }
    }

    @Test
    public void unsupportedStateValuesAreRejected()
    {
        RentalMember invalid = member("pending", "0");
        invalid.setAuthStatus("approved");
        try
        {
            policy.prepareForCreate(invalid, new Date());
            throw new AssertionError("unsupported authentication state should be rejected");
        }
        catch (ServiceException expected)
        {
            require(expected.getMessage().contains("认证状态"), "error should identify authentication state");
        }
    }

    private RentalMember member(String authStatus, String status)
    {
        RentalMember member = new RentalMember();
        member.setMemberType("personal");
        member.setAuthStatus(authStatus);
        member.setStatus(status);
        member.setLicenseLongTerm(0);
        member.setVipLevel("free");
        member.setExternalAcceptEnabled(0);
        return member;
    }

    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }
}
