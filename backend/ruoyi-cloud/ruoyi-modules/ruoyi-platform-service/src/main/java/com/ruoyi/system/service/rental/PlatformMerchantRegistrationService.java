package com.ruoyi.system.service.rental;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.merchant.api.RemoteMerchantUserService;
import com.ruoyi.merchant.api.model.MerchantOwnerProvisionCommand;
import com.ruoyi.system.api.model.MerchantRegistrationApplicationCommand;
import com.ruoyi.system.api.model.MerchantRegistrationApplicationVO;
import com.ruoyi.system.domain.rental.RentalMember;
import com.ruoyi.system.mapper.rental.RentalMemberMapper;
import com.ruoyi.system.mapper.rental.MerchantPromotionMapper;
import com.ruoyi.system.domain.rental.MerchantLevel;
import com.ruoyi.system.service.SysCodeRuleService;

/** 平台持有的商户入驻申请应用服务。 */
@Service
public class PlatformMerchantRegistrationService
{
    private static final Long DEFAULT_TENANT_ID = 1L;
    private static final Integer PENDING = RentalCode.AuthenticationStatus.PENDING.getCode();
    private static final Integer REJECTED = RentalCode.AuthenticationStatus.REJECTED.getCode();
    private static final Integer VERIFIED = RentalCode.AuthenticationStatus.VERIFIED.getCode();
    private static final String REGISTRATION_OPERATOR = "merchant-registration";
    private static final Integer PAID = RentalCode.RegistrationMode.PAID.getCode();
    private static final Integer INVITE = RentalCode.RegistrationMode.INVITE.getCode();
    private static final Integer AWAITING_PAYMENT = RentalCode.RegistrationStatus.AWAITING_PAYMENT.getCode();
    private static final Integer COMPLETED = RentalCode.RegistrationStatus.COMPLETED.getCode();

    @Autowired
    private RentalMemberMapper memberMapper;

    @Autowired
    private RentalMemberLifecyclePolicy lifecyclePolicy;

    @Autowired
    private RentalMemberCertificationService certificationService;

    @Autowired
    private RemoteMerchantUserService remoteMerchantUserService;

    @Autowired
    private SysCodeRuleService codeRuleService;

    @Autowired
    private MerchantPromotionMapper promotionMapper;

    /**
     * 创建一条新的商户入驻申请。商户编码、租户、审核状态及默认配额均由平台服务生成，
     * 不接受公开客户端指定这些服务端归属字段。
     *
     * @param command 商户服务校验后提交的入驻资料
     * @return 不含商户唯一编号和租户唯一编号的公开申请结果
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantRegistrationApplicationVO submit(MerchantRegistrationApplicationCommand command)
    {
        validate(command);
        RentalMember existing = memberMapper.selectRentalMemberByContactPhone(command.getContactPhone().trim());
        if (existing != null)
        {
            throw new ServiceException(statusMessage(existing.getAuthStatus()));
        }

        MerchantLevel defaultLevel = promotionMapper.selectDefaultLevel();
        if (defaultLevel == null)
        {
            throw new ServiceException("平台未配置可用的商户等级");
        }
        Map<String, Object> invite = null;
        if (INVITE.equals(command.getRegistrationMode()))
        {
            invite = promotionMapper.selectInviteCodeForUpdate(command.getInviteCode().trim().toUpperCase());
            if (invite == null)
            {
                throw new ServiceException("邀请码无效或已停用");
            }
            if (intValue(invite.get("usedCount")) >= intValue(invite.get("inviteLimit")))
            {
                throw new ServiceException("该邀请码的邀请名额已用完");
            }
        }

        RentalMember member = new RentalMember();
        copyApplication(command, member);
        member.setTenantId(DEFAULT_TENANT_ID);
        member.setMemberCode(codeRuleService.nextCode("merchant"));
        member.setMembershipType(defaultMembershipType(member.getMemberType()));
        member.setAuthStatus(PAID.equals(command.getRegistrationMode())
                ? RentalCode.AuthenticationStatus.PAYMENT_PENDING.getCode() : PENDING);
        member.setVipLevel(defaultLevel.getLevelCode());
        member.setLevelId(defaultLevel.getLevelId());
        member.setRegistrationMode(command.getRegistrationMode());
        member.setRegistrationStatus(PAID.equals(command.getRegistrationMode()) ? AWAITING_PAYMENT : COMPLETED);
        member.setInviterMemberId(invite == null ? null : longValue(invite.get("memberId")));
        member.setStorageQuotaMb(1024L);
        member.setExternalAcceptEnabled(0);
        member.setStatus(RentalCode.EnableStatus.ENABLED.getCode());
        member.setCreateBy(null);
        lifecyclePolicy.prepareForCreate(member, DateUtils.getNowDate());
        memberMapper.insertRentalMember(member);
        MerchantRegistrationApplicationVO result = toVO(member);
        if (PAID.equals(command.getRegistrationMode()))
        {
            String paymentNo = "REG" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
            promotionMapper.insertRegistrationPayment(member.getMemberId(), paymentNo, defaultLevel.getRegistrationFee());
            result.setPaymentNo(paymentNo);
            result.setPaymentAmount(defaultLevel.getRegistrationFee());
        }
        else
        {
            Long inviteCodeId = longValue(invite.get("inviteCodeId"));
            Integer inviteLimit = intValue(invite.get("inviteLimit"));
            if (promotionMapper.consumeInviteQuota(inviteCodeId, inviteLimit) == 0)
            {
                throw new ServiceException("邀请码名额已被使用完，请更换邀请码");
            }
            promotionMapper.insertInvitation(longValue(invite.get("tenantId")), inviteCodeId,
                    String.valueOf(invite.get("inviteCode")), longValue(invite.get("memberId")),
                    member.getMemberId(), member.getContactPhone());
            certificationService.createPendingCertification(member, REGISTRATION_OPERATOR, null);
            provisionMerchantOwner(member, null);
        }
        return result;
    }

    /**
     * 幂等确认注册支付单，并在首次支付成功时创建认证申请和商户所有者账号。
     *
     * @param paymentNo 注册支付单号
     * @return 注册完成后的申请资料
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantRegistrationApplicationVO confirmPayment(String paymentNo)
    {
        Map<String, Object> payment = promotionMapper.selectPaymentForUpdate(paymentNo);
        if (payment == null)
        {
            throw new ServiceException("注册支付单不存在");
        }
        Long memberId = longValue(payment.get("memberId"));
        RentalMember member = memberMapper.selectRentalMemberById(memberId);
        if (!RentalCode.PaymentStatus.PAID.getCode().equals(intValue(payment.get("paymentStatus"))))
        {
            promotionMapper.markPaymentPaid(paymentNo);
            if (promotionMapper.completePaidRegistration(memberId) == 0)
            {
                throw new ServiceException("注册状态已变化，请重新查询");
            }
            member = memberMapper.selectRentalMemberById(memberId);
            certificationService.createPendingCertification(member, REGISTRATION_OPERATOR, null);
            provisionMerchantOwner(member, null);
        }
        return toVO(memberMapper.selectRentalMemberById(memberId));
    }

    /**
     * 根据已经验证的申请手机号查询商户入驻申请。
     *
     * @param phone 商户提交申请时使用的手机号
     * @return 可供商户查看和重新编辑的申请资料
     */
    public MerchantRegistrationApplicationVO getByPhone(String phone)
    {
        requirePhone(phone);
        RentalMember member = memberMapper.selectRentalMemberByContactPhone(phone.trim());
        if (member == null)
        {
            throw new ServiceException("未查询到该手机号提交的商户申请");
        }
        return toVO(member);
    }

    /**
     * 重新提交审核未通过的商户申请。状态先通过条件更新从已驳回切换为待审核，
     * 再保存修改后的资料，从而阻止重复提交或与平台审核并发覆盖。
     *
     * @param command 商户修改后的完整申请资料
     * @return 重新进入待审核状态的申请资料
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantRegistrationApplicationVO resubmit(MerchantRegistrationApplicationCommand command)
    {
        validate(command);
        RentalMember member = memberMapper.selectRentalMemberByContactPhone(command.getContactPhone().trim());
        if (member == null)
        {
            throw new ServiceException("未查询到该手机号提交的商户申请");
        }
        if (!REJECTED.equals(member.getAuthStatus()))
        {
            throw new ServiceException(PENDING.equals(member.getAuthStatus())
                    ? "申请正在审核中，不能重复提交" : "当前申请状态不允许重新提交");
        }

        RentalMember previous = memberMapper.selectRentalMemberById(member.getMemberId());
        if (!StringUtils.equals(previous.getContactPhone(), command.getContactPhone().trim()))
        {
            throw new ServiceException("注册手机号已经开通商户账号，不能在驳回重提时修改");
        }
        copyApplication(command, member);
        member.setAuthStatus(PENDING);
        member.setAuditRemark(null);
        member.setUpdateBy(null);
        lifecyclePolicy.prepareForUpdate(previous, member, DateUtils.getNowDate());
        if (memberMapper.markRegistrationPending(member) == 0)
        {
            throw new ServiceException("申请状态已变更，请重新查询后再提交");
        }
        memberMapper.updateRentalMember(member);
        certificationService.createPendingCertification(member, REGISTRATION_OPERATOR, null);
        provisionMerchantOwner(member, null);
        return toVO(memberMapper.selectRentalMemberById(member.getMemberId()));
    }

    /**
     * 校验平台持久化前必须满足的申请规则。所有商户必须提交身份证正反面，企业商户还必须提交完整主体资料。
     *
     * @param command 待校验的申请资料
     */
    private void validate(MerchantRegistrationApplicationCommand command)
    {
        if (command == null)
        {
            throw new ServiceException("商户申请资料不能为空");
        }
        require(command.getNickname(), "店铺名称不能为空");
        require(command.getContactName(), "联系人不能为空");
        requirePhone(command.getContactPhone());
        if (!PAID.equals(command.getRegistrationMode()) && !INVITE.equals(command.getRegistrationMode()))
        {
            throw new ServiceException("注册方式不正确");
        }
        if (INVITE.equals(command.getRegistrationMode()))
        {
            require(command.getInviteCode(), "邀请码不能为空");
        }
        if (!RentalCode.MemberType.PERSONAL.getCode().equals(command.getMemberType())
                && !RentalCode.MemberType.ENTERPRISE.getCode().equals(command.getMemberType()))
        {
            throw new ServiceException("商户类型不正确");
        }
        require(command.getLegalIdFrontUrl(), "请上传身份证正面");
        require(command.getLegalIdBackUrl(), "请上传身份证反面");
        if (RentalCode.MemberType.ENTERPRISE.getCode().equals(command.getMemberType()))
        {
            require(command.getLegalSubjectName(), "企业名称不能为空");
            require(command.getLicenseNo(), "营业执照号不能为空");
            require(command.getLicenseAddress(), "营业执照地址不能为空");
            require(command.getLicenseImageUrl(), "请上传营业执照");
            require(command.getLegalRepresentativeName(), "法人姓名不能为空");
            require(command.getLegalRepresentativeIdNo(), "法人身份证号不能为空");
        }
    }

    /**
     * 将公开申请字段复制到平台商户实体，并统一去除字符串首尾空白及解析日期。
     * 此方法不会复制租户、商户唯一编号、审核状态和账号等服务端控制字段。
     *
     * @param source 商户提交的申请资料
     * @param target 平台持有的商户实体
     */
    private void copyApplication(MerchantRegistrationApplicationCommand source, RentalMember target)
    {
        target.setMemberType(source.getMemberType());
        target.setNickname(trim(source.getNickname()));
        target.setContactName(trim(source.getContactName()));
        target.setContactPhone(trim(source.getContactPhone()));
        target.setServicePhone(trim(source.getServicePhone()));
        target.setContactEmail(trim(source.getContactEmail()));
        target.setAlipayRealName(trim(source.getAlipayRealName()));
        target.setAlipayAccount(trim(source.getAlipayAccount()));
        target.setStoreLogoUrl(trim(source.getStoreLogoUrl()));
        target.setStoreDescription(trim(source.getStoreDescription()));
        target.setLegalSubjectName(trim(source.getLegalSubjectName()));
        target.setLicenseNo(trim(source.getLicenseNo()));
        target.setRegisteredCapital(source.getRegisteredCapital());
        target.setBusinessScope(trim(source.getBusinessScope()));
        target.setLicenseAddress(trim(source.getLicenseAddress()));
        target.setLicenseValidFrom(parseDate(source.getLicenseValidFrom(), "营业执照有效期开始日期不正确"));
        target.setLicenseValidTo(parseDate(source.getLicenseValidTo(), "营业执照有效期结束日期不正确"));
        target.setLicenseLongTerm(source.getLicenseLongTerm() == null ? 0 : source.getLicenseLongTerm());
        target.setLicenseImageUrl(trim(source.getLicenseImageUrl()));
        target.setLegalRepresentativeName(trim(source.getLegalRepresentativeName()));
        target.setLegalRepresentativeIdNo(trim(source.getLegalRepresentativeIdNo()));
        target.setLegalIdFrontUrl(trim(source.getLegalIdFrontUrl()));
        target.setLegalIdBackUrl(trim(source.getLegalIdBackUrl()));
    }

    /**
     * 将平台商户实体转换为公开查询结果，显式排除租户唯一编号和商户唯一编号。
     *
     * @param member 平台持久化的商户实体
     * @return 商户端可读取的申请资料
     */
    private MerchantRegistrationApplicationVO toVO(RentalMember member)
    {
        MerchantRegistrationApplicationVO result = new MerchantRegistrationApplicationVO();
        result.setMemberCode(member.getMemberCode());
        result.setAuthStatus(member.getAuthStatus());
        result.setAuditRemark(member.getAuditRemark());
        result.setSubmittedAt(toMillis(member.getCreateTime()));
        result.setUpdatedAt(toMillis(member.getUpdateTime()));
        result.setMemberType(member.getMemberType());
        result.setNickname(member.getNickname());
        result.setContactName(member.getContactName());
        result.setContactPhone(member.getContactPhone());
        result.setServicePhone(member.getServicePhone());
        result.setContactEmail(member.getContactEmail());
        result.setAlipayRealName(member.getAlipayRealName());
        result.setAlipayAccount(member.getAlipayAccount());
        result.setStoreLogoUrl(member.getStoreLogoUrl());
        result.setStoreDescription(member.getStoreDescription());
        result.setLegalSubjectName(member.getLegalSubjectName());
        result.setLicenseNo(member.getLicenseNo());
        result.setRegisteredCapital(member.getRegisteredCapital());
        result.setBusinessScope(member.getBusinessScope());
        result.setLicenseAddress(member.getLicenseAddress());
        result.setLicenseValidFrom(formatDate(member.getLicenseValidFrom()));
        result.setLicenseValidTo(formatDate(member.getLicenseValidTo()));
        result.setLicenseLongTerm(member.getLicenseLongTerm());
        result.setLicenseImageUrl(member.getLicenseImageUrl());
        result.setLegalRepresentativeName(member.getLegalRepresentativeName());
        result.setLegalRepresentativeIdNo(member.getLegalRepresentativeIdNo());
        result.setLegalIdFrontUrl(member.getLegalIdFrontUrl());
        result.setLegalIdBackUrl(member.getLegalIdBackUrl());
        result.setRegistrationMode(member.getRegistrationMode());
        result.setRegistrationStatus(member.getRegistrationStatus());
        Map<String, Object> payment = promotionMapper.selectPaymentByMember(member.getMemberId());
        if (payment != null)
        {
            result.setPaymentNo(String.valueOf(payment.get("paymentNo")));
            result.setPaymentAmount((java.math.BigDecimal) payment.get("paymentAmount"));
        }
        return result;
    }

    /** @param value 数据库数值 @return 长整数 */
    private Long longValue(Object value) { return value == null ? null : ((Number) value).longValue(); }

    /** @param value 数据库数值 @return 整数 */
    private int intValue(Object value) { return value == null ? 0 : ((Number) value).intValue(); }

    /**
     * 解析申请中的日期文本，空值保留为空，格式错误时返回明确业务提示。
     *
     * @param value 日期文本
     * @param message 日期格式错误时使用的提示
     * @return 解析后的日期，输入为空时返回 {@code null}
     */
    private Date parseDate(String value, String message)
    {
        if (StringUtils.isBlank(value))
        {
            return null;
        }
        Date date = DateUtils.parseDate(value);
        if (date == null)
        {
            throw new ServiceException(message);
        }
        return date;
    }

    /**
     * 将数据库日期格式化为前端日期选择器使用的年月日文本。
     *
     * @param date 数据库日期
     * @return 年月日文本，日期为空时返回 {@code null}
     */
    private String formatDate(Date date)
    {
        return date == null ? null : DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, date);
    }

    /**
     * 将日期转换为毫秒时间戳，避免跨服务传输时区不明确的日期字符串。
     *
     * @param date 待转换日期
     * @return 毫秒时间戳，日期为空时返回 {@code null}
     */
    private Long toMillis(Date date)
    {
        return date == null ? null : date.getTime();
    }

    /**
     * 根据已有申请状态生成阻止重复注册时的可操作提示。
     *
     * @param authStatus 当前审核状态
     * @return 面向商户的状态提示
     */
    private String statusMessage(Integer authStatus)
    {
        if (PENDING.equals(authStatus))
        {
            return "该手机号的商户申请正在审核中";
        }
        if (REJECTED.equals(authStatus))
        {
            return "该手机号的申请审核未通过，请查询申请后修改并重新提交";
        }
        if (VERIFIED.equals(authStatus))
        {
            return "该手机号已通过商户审核，请直接登录";
        }
        return "该手机号已提交商户申请";
    }

    /**
     * 校验中国大陆手机号不能为空且符合十一位号码格式。
     *
     * @param phone 待校验手机号
     */
    private void requirePhone(String phone)
    {
        require(phone, "手机号不能为空");
        if (!phone.trim().matches("^1[3-9]\\d{9}$"))
        {
            throw new ServiceException("手机号格式不正确");
        }
    }

    /**
     * 校验必填字符串不是空值或纯空白。
     *
     * @param value 待校验字符串
     * @param message 校验失败时返回的业务提示
     */
    private void require(String value, String message)
    {
        if (StringUtils.isBlank(value))
        {
            throw new ServiceException(message);
        }
    }

    /**
     * 统一清理可选字符串：空白值归一为 {@code null}，非空值移除首尾空白。
     *
     * @param value 原始字符串
     * @return 规范化后的字符串
     */
    private String trim(String value)
    {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    /**
     * 将主体类型转换为首次申请使用的会员类型，审核通过前只作为默认值保存，
     * 最终会员类型仍由平台审核命令确认。
     *
     * @param memberType 商户主体类型
     * @return 会员类型编码
     */
    private Integer defaultMembershipType(Integer memberType)
    {
        return RentalCode.MemberType.ENTERPRISE.getCode().equals(memberType)
                ? RentalCode.MembershipType.ORDINARY_ENTERPRISE_MEMBER.getCode()
                : RentalCode.MembershipType.PERSONAL_MEMBER.getCode();
    }

    /**
     * 注册资料落库后立即创建商户所有者账号，使申请人在审核期间也可以通过短信验证码登录。
     * 账号权限由商户身份服务依据认证状态清空，平台审核通过后再次幂等激活完整权限。
     *
     * @param member 已生成唯一编号的商户申请
     */
    private void provisionMerchantOwner(RentalMember member, Long operatorId)
    {
        MerchantOwnerProvisionCommand provision = new MerchantOwnerProvisionCommand();
        provision.setTenantId(member.getTenantId());
        provision.setMerchantId(member.getMemberId());
        provision.setPhone(member.getContactPhone());
        provision.setNickname(StringUtils.isBlank(member.getContactName()) ? member.getNickname() : member.getContactName());
        provision.setEmail(member.getContactEmail());
        provision.setOperator(operatorId);
        R<Boolean> result = remoteMerchantUserService.provisionOwner(provision, SecurityConstants.INNER,
                Constants.CLIENT_TYPE_MERCHANT);
        if (result == null || R.FAIL == result.getCode() || !Boolean.TRUE.equals(result.getData()))
        {
            throw new ServiceException(result == null ? "商户管理员账号开通服务无响应" : result.getMsg());
        }
    }
}
