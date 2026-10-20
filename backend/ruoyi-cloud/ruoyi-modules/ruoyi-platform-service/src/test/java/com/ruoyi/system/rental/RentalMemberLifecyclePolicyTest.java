package com.ruoyi.system.rental;

import java.util.Date;
import org.junit.Test;
import com.ruoyi.common.core.enums.RentalCode;
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
        RentalMember member = member(RentalCode.AuthenticationStatus.REJECTED.getCode(),
                RentalCode.EnableStatus.DISABLED.getCode());

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
        RentalMember previous = member(RentalCode.AuthenticationStatus.VERIFIED.getCode(),
                RentalCode.EnableStatus.DISABLED.getCode());
        previous.setAuditTime(oldAuditTime);
        previous.setFreezeTime(oldFreezeTime);
        RentalMember current = member(RentalCode.AuthenticationStatus.VERIFIED.getCode(),
                RentalCode.EnableStatus.DISABLED.getCode());

        policy.prepareForUpdate(previous, current, now);

        require(oldAuditTime.equals(current.getAuditTime()), "unchanged audit state should preserve audit time");
        require(oldFreezeTime.equals(current.getFreezeTime()), "unchanged freeze state should preserve freeze time");
    }

    @Test
    public void unfreezeClearsFreezeMetadata()
    {
        RentalMember previous = member(RentalCode.AuthenticationStatus.PENDING.getCode(),
                RentalCode.EnableStatus.DISABLED.getCode());
        previous.setFreezeTime(new Date(1000L));
        RentalMember current = member(RentalCode.AuthenticationStatus.PENDING.getCode(),
                RentalCode.EnableStatus.ENABLED.getCode());
        current.setFreezeReason("旧冻结原因");

        policy.prepareForUpdate(previous, current, new Date(2000L));

        require(current.getFreezeTime() == null, "unfrozen member should not retain freeze time");
        require(current.getFreezeReason() == null, "unfrozen member should not retain freeze reason");
    }

    @Test
    public void longTermLicenseClearsEndDateAndInvalidContractIsRejected()
    {
        RentalMember longTerm = member(RentalCode.AuthenticationStatus.PENDING.getCode(),
                RentalCode.EnableStatus.ENABLED.getCode());
        longTerm.setLicenseLongTerm(1);
        longTerm.setLicenseValidTo(new Date(1000L));
        policy.prepareForCreate(longTerm, new Date(2000L));
        require(longTerm.getLicenseValidTo() == null, "long-term license should not persist an end date");

        RentalMember invalid = member(RentalCode.AuthenticationStatus.PENDING.getCode(),
                RentalCode.EnableStatus.ENABLED.getCode());
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
        RentalMember invalid = member(RentalCode.AuthenticationStatus.PENDING.getCode(),
                RentalCode.EnableStatus.ENABLED.getCode());
        invalid.setAuthStatus(99);
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

    @Test
    public void verifiedPersonalMerchantRequiresIdentityCardImages()
    {
        RentalMember member = member(RentalCode.AuthenticationStatus.VERIFIED.getCode(),
                RentalCode.EnableStatus.ENABLED.getCode());
        member.setLegalIdFrontUrl(null);
        member.setLegalIdBackUrl(null);
        try
        {
            policy.prepareForCreate(member, new Date());
            throw new AssertionError("verified personal merchant without identity card images should be rejected");
        }
        catch (ServiceException expected)
        {
            require(expected.getMessage().contains("身份证正面"), "error should identify the missing identity-card front image");
        }

        member.setLegalIdFrontUrl("/files/identity-front.jpg");
        try
        {
            policy.prepareForCreate(member, new Date());
            throw new AssertionError("verified personal merchant without identity-card back image should be rejected");
        }
        catch (ServiceException expected)
        {
            require(expected.getMessage().contains("身份证反面"), "error should identify the missing identity-card back image");
        }

        member.setLegalIdBackUrl("/files/identity-back.jpg");
        policy.prepareForCreate(member, new Date());
    }

    private RentalMember member(Integer authStatus, Integer status)
    {
        RentalMember member = new RentalMember();
        member.setMemberType(RentalCode.MemberType.PERSONAL.getCode());
        member.setAuthStatus(authStatus);
        member.setStatus(status);
        member.setLicenseLongTerm(0);
        member.setVipLevel("free");
        member.setExternalAcceptEnabled(0);
        member.setLegalIdFrontUrl("/files/identity-front.jpg");
        member.setLegalIdBackUrl("/files/identity-back.jpg");
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
