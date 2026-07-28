package com.ruoyi.system.service.rental;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.utils.uuid.IdUtils;
import com.ruoyi.system.api.model.MerchantRegistrationApplicationCommand;
import com.ruoyi.system.api.model.MerchantRegistrationApplicationVO;
import com.ruoyi.system.domain.rental.RentalMember;
import com.ruoyi.system.mapper.rental.RentalMemberMapper;

/** 平台持有的商户入驻申请应用服务。 */
@Service
public class PlatformMerchantRegistrationService
{
    private static final Long DEFAULT_TENANT_ID = 1L;
    private static final String PENDING = "pending";
    private static final String REJECTED = "rejected";
    private static final String VERIFIED = "verified";
    private static final String REGISTRATION_OPERATOR = "merchant-registration";

    @Autowired
    private RentalMemberMapper memberMapper;

    @Autowired
    private RentalMemberLifecyclePolicy lifecyclePolicy;

    /**
     * 创建一条新的商户入驻申请。商户编码、租户、审核状态及默认配额均由平台服务生成，
     * 不接受公开客户端指定这些服务端归属字段。
     *
     * @param command 商户服务校验后提交的入驻资料
     * @return 不含商户主键和租户主键的公开申请结果
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

        RentalMember member = new RentalMember();
        copyApplication(command, member);
        member.setTenantId(DEFAULT_TENANT_ID);
        member.setMemberCode(generateMemberCode());
        member.setAuthStatus(PENDING);
        member.setVipLevel("free");
        member.setStorageQuotaMb(1024L);
        member.setExternalAcceptEnabled(0);
        member.setStatus("0");
        member.setCreateBy(REGISTRATION_OPERATOR);
        lifecyclePolicy.prepareForCreate(member, DateUtils.getNowDate());
        memberMapper.insertRentalMember(member);
        return toVO(member);
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
        copyApplication(command, member);
        member.setAuthStatus(PENDING);
        member.setAuditRemark(null);
        member.setUpdateBy(REGISTRATION_OPERATOR);
        lifecyclePolicy.prepareForUpdate(previous, member, DateUtils.getNowDate());
        if (memberMapper.markRegistrationPending(member) == 0)
        {
            throw new ServiceException("申请状态已变更，请重新查询后再提交");
        }
        memberMapper.updateRentalMember(member);
        return toVO(memberMapper.selectRentalMemberById(member.getMemberId()));
    }

    /**
     * 校验平台持久化前必须满足的申请规则，企业商户必须提交完整的主体和法人资料。
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
        if (!StringUtils.equalsAny(command.getMemberType(), "personal", "enterprise"))
        {
            throw new ServiceException("商户类型不正确");
        }
        if ("enterprise".equals(command.getMemberType()))
        {
            require(command.getLegalSubjectName(), "企业名称不能为空");
            require(command.getLicenseNo(), "营业执照号不能为空");
            require(command.getLicenseImageUrl(), "请上传营业执照");
            require(command.getLegalRepresentativeName(), "法人姓名不能为空");
            require(command.getLegalRepresentativeIdNo(), "法人身份证号不能为空");
            require(command.getLegalIdFrontUrl(), "请上传法人身份证人像面");
            require(command.getLegalIdBackUrl(), "请上传法人身份证国徽面");
        }
    }

    /**
     * 将公开申请字段复制到平台商户实体，并统一去除字符串首尾空白及解析日期。
     * 此方法不会复制租户、商户主键、审核状态和账号等服务端控制字段。
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
     * 将平台商户实体转换为公开查询结果，显式排除租户主键和商户主键。
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
        return result;
    }

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
     * 生成平台唯一的商户申请编码，组合当前日期和随机标识以降低并发碰撞概率。
     *
     * @return 以字母 M 开头的商户编码
     */
    private String generateMemberCode()
    {
        return "M" + DateUtils.dateTime() + IdUtils.simpleUUID().substring(0, 12).toUpperCase();
    }

    /**
     * 根据已有申请状态生成阻止重复注册时的可操作提示。
     *
     * @param authStatus 当前审核状态
     * @return 面向商户的状态提示
     */
    private String statusMessage(String authStatus)
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
}
