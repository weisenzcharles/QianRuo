package com.ruoyi.system.service.rental;

import java.util.Date;
import java.util.Set;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.system.domain.rental.RentalMember;

/** 统一处理商户资质日期、审核和冻结状态。 */
@Component
public class RentalMemberLifecyclePolicy
{
    private static final String STATUS_FROZEN = "1";
    private static final String AUTH_VERIFIED = "verified";
    private static final String AUTH_REJECTED = "rejected";
    private static final Set<String> MEMBER_TYPES = Set.of("personal", "enterprise");
    private static final Set<String> AUTH_STATUSES = Set.of("unverified", "pending", AUTH_VERIFIED, AUTH_REJECTED);
    private static final Set<String> MEMBER_STATUSES = Set.of("0", STATUS_FROZEN);
    private static final Set<String> VIP_LEVELS = Set.of("free", "standard", "pro", "enterprise");

    public void prepareForCreate(RentalMember member, Date now)
    {
        normalizeAndValidate(member);
        member.setAuditTime(isAudited(member.getAuthStatus()) ? now : null);
        member.setFreezeTime(STATUS_FROZEN.equals(member.getStatus()) ? now : null);
        if (!STATUS_FROZEN.equals(member.getStatus()))
        {
            member.setFreezeReason(null);
        }
    }

    public void prepareForUpdate(RentalMember previous, RentalMember member, Date now)
    {
        if (previous == null)
        {
            throw new ServiceException("商户不存在或已删除");
        }
        normalizeAndValidate(member);

        if (isAudited(member.getAuthStatus()))
        {
            member.setAuditTime(member.getAuthStatus().equals(previous.getAuthStatus())
                    ? previous.getAuditTime() : now);
        }
        else
        {
            member.setAuditTime(null);
        }

        if (STATUS_FROZEN.equals(member.getStatus()))
        {
            member.setFreezeTime(STATUS_FROZEN.equals(previous.getStatus())
                    ? previous.getFreezeTime() : now);
        }
        else
        {
            member.setFreezeTime(null);
            member.setFreezeReason(null);
        }
    }

    private void normalizeAndValidate(RentalMember member)
    {
        if (!MEMBER_TYPES.contains(member.getMemberType()))
        {
            throw new ServiceException("商户类型不正确");
        }
        if (!AUTH_STATUSES.contains(member.getAuthStatus()))
        {
            throw new ServiceException("认证状态不正确");
        }
        if (!MEMBER_STATUSES.contains(member.getStatus()))
        {
            throw new ServiceException("冻结状态不正确");
        }
        if (!VIP_LEVELS.contains(member.getVipLevel()))
        {
            throw new ServiceException("会员等级不正确");
        }
        if (!Integer.valueOf(0).equals(member.getExternalAcceptEnabled())
                && !Integer.valueOf(1).equals(member.getExternalAcceptEnabled()))
        {
            throw new ServiceException("对外接单状态不正确");
        }
        if (member.getLicenseLongTerm() == null)
        {
            member.setLicenseLongTerm(0);
        }
        if (!Integer.valueOf(0).equals(member.getLicenseLongTerm())
                && !Integer.valueOf(1).equals(member.getLicenseLongTerm()))
        {
            throw new ServiceException("营业执照长期状态不正确");
        }
        if (Integer.valueOf(1).equals(member.getLicenseLongTerm()))
        {
            member.setLicenseValidTo(null);
        }
        else if (member.getLicenseValidFrom() != null && member.getLicenseValidTo() != null
                && member.getLicenseValidFrom().after(member.getLicenseValidTo()))
        {
            throw new ServiceException("营业执照有效期结束日期不能早于开始日期");
        }

        if (member.getContractValidFrom() != null && member.getContractValidTo() != null
                && member.getContractValidFrom().after(member.getContractValidTo()))
        {
            throw new ServiceException("合同有效期结束日期不能早于开始日期");
        }

        if (AUTH_VERIFIED.equals(member.getAuthStatus()) && "enterprise".equals(member.getMemberType()))
        {
            require(member.getLegalSubjectName(), "企业认证通过前必须填写企业名称");
            require(member.getLicenseNo(), "企业认证通过前必须填写营业执照号");
            require(member.getLicenseImageUrl(), "企业认证通过前必须上传营业执照");
            require(member.getLegalRepresentativeName(), "企业认证通过前必须填写法人姓名");
            require(member.getLegalRepresentativeIdNo(), "企业认证通过前必须填写法人身份证号");
            require(member.getLegalIdFrontUrl(), "企业认证通过前必须上传法人身份证人像面");
            require(member.getLegalIdBackUrl(), "企业认证通过前必须上传法人身份证国徽面");
        }
    }

    private boolean isAudited(String authStatus)
    {
        return AUTH_VERIFIED.equals(authStatus) || AUTH_REJECTED.equals(authStatus);
    }

    private void require(String value, String message)
    {
        if (StringUtils.isEmpty(value) || value.trim().isEmpty())
        {
            throw new ServiceException(message);
        }
    }
}
