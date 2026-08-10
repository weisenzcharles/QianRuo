package com.ruoyi.system.service.rental;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.system.domain.rental.RentalCertificationMaterial;
import com.ruoyi.system.domain.rental.RentalMember;
import com.ruoyi.system.domain.rental.RentalMemberCertification;
import com.ruoyi.system.domain.rental.RentalMemberMembershipHistory;
import com.ruoyi.system.domain.rental.RentalMemberQualification;
import com.ruoyi.system.domain.rental.RentalMemberQualificationHistory;
import com.ruoyi.system.mapper.rental.RentalMemberCertificationMapper;
import com.ruoyi.system.model.rental.RentalMemberAuditCommand;
import com.ruoyi.system.api.model.MerchantCertificationMaterialView;
import com.ruoyi.system.api.model.MerchantCertificationVersionView;
import com.ruoyi.system.api.model.MerchantCertificationView;
import com.ruoyi.system.api.model.MerchantMembershipHistoryView;
import com.ruoyi.system.api.model.MerchantQualificationHistoryView;
import com.ruoyi.system.api.model.MerchantQualificationView;

/**
 * 商户认证、会员类型和接单资格应用服务。
 *
 * <p>该服务只在平台服务中持有认证主数据。商户端需要通过内部契约读取，不能直接访问平台 Mapper。</p>
 */
@Service
public class RentalMemberCertificationService
{
    private static final List<String> QUALIFICATION_CODES = Arrays.asList(
            "external_accept", "alliance_accept", "mini_program_accept");

    private static final Set<String> MEMBERSHIP_TYPES = new HashSet<String>(Arrays.asList(
            "personal_member", "ordinary_enterprise_member", "settled_member"));

    @Autowired
    private RentalMemberCertificationMapper certificationMapper;

    /**
     * 为新注册或驳回重提的商户创建一份新的待审核认证版本，并复制当前材料地址。
     *
     * @param member 当前商户主体快照
     * @param operator 资料提交人
     * @return 新创建的认证版本
     */
    @Transactional(rollbackFor = Exception.class)
    public RentalMemberCertification createPendingCertification(RentalMember member, String operator)
    {
        RentalMemberCertification latest = certificationMapper.selectLatestCertification(member.getMemberId());
        int versionNo = latest == null || latest.getVersionNo() == null ? 1 : latest.getVersionNo() + 1;
        Date now = DateUtils.getNowDate();
        RentalMemberCertification certification = snapshot(member, versionNo, operator, now);
        certificationMapper.insertCertification(certification);
        insertMaterials(member, certification, operator);
        return certification;
    }

    /**
     * 查询商户认证历史，并为每个版本加载材料明细。
     *
     * @param memberId 商户主键
     * @return 认证历史列表
     */
    public List<RentalMemberCertification> selectCertificationHistory(Long memberId)
    {
        List<RentalMemberCertification> history = certificationMapper.selectCertificationHistory(memberId);
        for (RentalMemberCertification item : history)
        {
            item.setMaterials(certificationMapper.selectMaterials(item.getCertificationId()));
        }
        return history;
    }

    /**
     * 查询商户当前接单资格快照。
     *
     * @param memberId 商户主键
     * @return 当前接单资格
     */
    public List<RentalMemberQualification> selectQualifications(Long memberId)
    {
        return certificationMapper.selectQualifications(memberId);
    }

    /**
     * 查询商户会员类型变更历史。
     *
     * @param memberId 商户主键
     * @return 会员类型历史
     */
    public List<RentalMemberMembershipHistory> selectMembershipHistory(Long memberId)
    {
        return certificationMapper.selectMembershipHistory(memberId);
    }

    /**
     * 查询商户接单资格变更历史。
     *
     * @param memberId 商户主键
     * @return 接单资格历史
     */
    public List<RentalMemberQualificationHistory> selectQualificationHistory(Long memberId)
    {
        return certificationMapper.selectQualificationHistory(memberId);
    }

    /**
     * 将平台认证主数据转换为商户服务使用的跨服务聚合视图，所有日期字段转换为毫秒时间戳，
     * 避免不同服务的 Jackson 默认日期格式造成登录或查询响应解析失败。
     *
     * @param member 平台查询到的商户主体快照
     * @return 不包含租户内部字段的商户认证聚合视图
     */
    public MerchantCertificationView toMerchantView(RentalMember member)
    {
        MerchantCertificationView view = new MerchantCertificationView();
        view.setMemberId(member.getMemberId());
        view.setMemberType(member.getMemberType());
        view.setMembershipType(member.getMembershipType());
        view.setAuthStatus(member.getAuthStatus());
        view.setCertificationHistory(selectCertificationHistory(member.getMemberId()).stream()
                .map(this::toVersionView).collect(Collectors.toList()));
        view.setQualifications(selectQualifications(member.getMemberId()).stream()
                .map(this::toQualificationView).collect(Collectors.toList()));
        view.setMembershipHistory(selectMembershipHistory(member.getMemberId()).stream()
                .map(this::toMembershipHistoryView).collect(Collectors.toList()));
        view.setQualificationHistory(selectQualificationHistory(member.getMemberId()).stream()
                .map(this::toQualificationHistoryView).collect(Collectors.toList()));
        return view;
    }

    /**
     * 更新认证版本审核结果，并在审核通过时同步会员类型和接单资格快照。
     *
     * @param member 当前商户主体
     * @param command 平台审核命令
     * @param operator 平台操作人
     */
    @Transactional(rollbackFor = Exception.class)
    public void applyReview(RentalMember member, RentalMemberAuditCommand command, String operator)
    {
        RentalMemberCertification certification = certificationMapper.selectLatestCertification(member.getMemberId());
        if (certification == null)
        {
            certification = createPendingCertification(member, operator);
        }
        if (!"pending".equals(certification.getCertificationStatus()))
        {
            throw new ServiceException("当前认证版本已经审核，请刷新后重试");
        }

        Date now = DateUtils.getNowDate();
        certification.setCertificationStatus("verified".equals(command.getAuthStatus()) ? "approved" : "rejected");
        certification.setReviewedBy(operator);
        certification.setReviewedTime(now);
        certification.setReviewRemark(command.getAuditRemark());
        certification.setUpdateBy(operator);
        if (certificationMapper.updateCertificationReview(certification) == 0)
        {
            throw new ServiceException("认证版本状态已变更，请刷新后重新审核");
        }

        if ("verified".equals(command.getAuthStatus()))
        {
            applyMembershipType(member, command, operator, now);
            applyQualifications(member, certification, command, operator, now);
        }
    }

    /**
     * 创建认证版本快照，避免后续编辑商户主体资料时篡改历史认证内容。
     *
     * @param member 商户主体
     * @param versionNo 新版本号
     * @param operator 资料提交人
     * @param now 提交时间
     * @return 认证版本快照
     */
    private RentalMemberCertification snapshot(RentalMember member, int versionNo, String operator, Date now)
    {
        RentalMemberCertification certification = new RentalMemberCertification();
        certification.setTenantId(member.getTenantId());
        certification.setMemberId(member.getMemberId());
        certification.setVersionNo(versionNo);
        certification.setCertificationType(member.getMemberType());
        certification.setCertificationStatus("pending");
        certification.setLegalSubjectName(member.getLegalSubjectName());
        certification.setLicenseNo(member.getLicenseNo());
        certification.setRegisteredCapital(member.getRegisteredCapital());
        certification.setBusinessScope(member.getBusinessScope());
        certification.setLicenseAddress(member.getLicenseAddress());
        certification.setLicenseValidFrom(member.getLicenseValidFrom());
        certification.setLicenseValidTo(member.getLicenseValidTo());
        certification.setLicenseLongTerm(member.getLicenseLongTerm());
        certification.setLegalRepresentativeName(member.getLegalRepresentativeName());
        certification.setLegalRepresentativeIdNo(member.getLegalRepresentativeIdNo());
        certification.setSubmittedBy(operator);
        certification.setSubmittedTime(now);
        certification.setCreateBy(operator);
        certification.setRemark(member.getRemark());
        return certification;
    }

    /**
     * 将商户主体上的认证文件地址拆成材料行，材料表只保存文件引用，不保存文件二进制。
     *
     * @param member 商户主体
     * @param certification 认证版本
     * @param operator 资料提交人
     */
    private void insertMaterials(RentalMember member, RentalMemberCertification certification, String operator)
    {
        insertMaterial(certification, "license", "营业执照", member.getLicenseImageUrl(), operator, 1);
        insertMaterial(certification, "legal_id_front", "法人身份证人像面", member.getLegalIdFrontUrl(), operator, 2);
        insertMaterial(certification, "legal_id_back", "法人身份证国徽面", member.getLegalIdBackUrl(), operator, 3);
        insertMaterial(certification, "contract", "签约合同", member.getContractFileUrl(), operator, 4);
    }

    /**
     * 在文件地址非空时新增一条认证材料。
     *
     * @param certification 认证版本
     * @param type 材料类型
     * @param name 材料名称
     * @param fileUrl 文件地址
     * @param operator 操作人
     * @param sortNo 排序值
     */
    private void insertMaterial(RentalMemberCertification certification, String type, String name, String fileUrl,
            String operator, int sortNo)
    {
        if (StringUtils.isBlank(fileUrl))
        {
            return;
        }
        RentalCertificationMaterial material = new RentalCertificationMaterial();
        material.setTenantId(certification.getTenantId());
        material.setMemberId(certification.getMemberId());
        material.setCertificationId(certification.getCertificationId());
        material.setMaterialType(type);
        material.setMaterialName(name);
        material.setFileUrl(fileUrl);
        material.setMaterialStatus("pending");
        material.setSortNo(sortNo);
        material.setCreateBy(operator);
        certificationMapper.insertMaterial(material);
    }

    /**
     * 应用审核通过后的会员类型，并记录前后值，避免只看当前字段无法追溯历史。
     *
     * @param member 商户主体
     * @param command 审核命令
     * @param operator 操作人
     * @param now 当前时间
     */
    private void applyMembershipType(RentalMember member, RentalMemberAuditCommand command, String operator, Date now)
    {
        String membershipType = StringUtils.isBlank(command.getMembershipType())
                ? defaultMembershipType(member.getMemberType()) : command.getMembershipType();
        if (!MEMBERSHIP_TYPES.contains(membershipType))
        {
            throw new ServiceException("会员类型不正确");
        }
        String previous = member.getMembershipType();
        member.setMembershipType(membershipType);
        if (!StringUtils.equals(previous, membershipType))
        {
            RentalMemberMembershipHistory history = new RentalMemberMembershipHistory();
            history.setTenantId(member.getTenantId());
            history.setMemberId(member.getMemberId());
            history.setPreviousType(previous);
            history.setMembershipType(membershipType);
            history.setReason(command.getAuditRemark());
            history.setOperatorName(operator);
            history.setOperatedTime(now);
            history.setCreateBy(operator);
            certificationMapper.insertMembershipHistory(history);
        }
    }

    /**
     * 应用审核通过后的接单资格集合。未勾选的资格会明确置为 disabled，防止旧权限残留。
     *
     * @param member 商户主体
     * @param certification 已审核通过的认证版本
     * @param command 审核命令
     * @param operator 操作人
     * @param now 当前时间
     */
    private void applyQualifications(RentalMember member, RentalMemberCertification certification,
            RentalMemberAuditCommand command, String operator, Date now)
    {
        if (command.getQualificationCodes() == null)
        {
            return;
        }
        Set<String> selected = command.getQualificationCodes().stream()
                .filter(QUALIFICATION_CODES::contains).collect(Collectors.toSet());
        member.setExternalAcceptEnabled(selected.contains("external_accept") ? 1 : 0);
        for (String code : QUALIFICATION_CODES)
        {
            String status = selected.contains(code) ? "enabled" : "disabled";
            RentalMemberQualification current = certificationMapper.selectQualification(member.getMemberId(), code);
            if (current == null)
            {
                current = new RentalMemberQualification();
                current.setTenantId(member.getTenantId());
                current.setMemberId(member.getMemberId());
                current.setQualificationCode(code);
                current.setQualificationStatus(status);
                current.setCertificationId(certification.getCertificationId());
                current.setEffectiveFrom("enabled".equals(status) ? now : null);
                current.setReviewedBy(operator);
                current.setReviewedTime(now);
                current.setReviewRemark(command.getAuditRemark());
                current.setCreateBy(operator);
                certificationMapper.insertQualification(current);
            }
            else
            {
                String previous = current.getQualificationStatus();
                current.setQualificationStatus(status);
                current.setCertificationId(certification.getCertificationId());
                current.setEffectiveFrom("enabled".equals(status) ? now : null);
                current.setEffectiveTo("enabled".equals(status) ? null : now);
                current.setReviewedBy(operator);
                current.setReviewedTime(now);
                current.setReviewRemark(command.getAuditRemark());
                current.setUpdateBy(operator);
                certificationMapper.updateQualification(current);
                if (!StringUtils.equals(previous, status))
                {
                    insertQualificationHistory(member, current, previous, operator, now, command.getAuditRemark());
                }
                continue;
            }
            insertQualificationHistory(member, current, null, operator, now, command.getAuditRemark());
        }
    }

    /**
     * 追加一条接单资格历史记录。
     *
     * @param member 商户主体
     * @param current 当前资格快照
     * @param previous 变更前状态
     * @param operator 操作人
     * @param now 操作时间
     * @param reason 变更原因
     */
    private void insertQualificationHistory(RentalMember member, RentalMemberQualification current, String previous,
            String operator, Date now, String reason)
    {
        RentalMemberQualificationHistory history = new RentalMemberQualificationHistory();
        history.setTenantId(member.getTenantId());
        history.setMemberId(member.getMemberId());
        history.setQualificationId(current.getQualificationId());
        history.setQualificationCode(current.getQualificationCode());
        history.setPreviousStatus(previous);
        history.setQualificationStatus(current.getQualificationStatus());
        history.setCertificationId(current.getCertificationId());
        history.setReason(reason);
        history.setOperatorName(operator);
        history.setOperatedTime(now);
        history.setCreateBy(operator);
        certificationMapper.insertQualificationHistory(history);
    }

    /**
     * 根据主体类型给新申请确定默认会员类型。
     *
     * @param memberType 主体类型
     * @return 默认会员类型编码
     */
    private String defaultMembershipType(String memberType)
    {
        return "enterprise".equals(memberType) ? "ordinary_enterprise_member" : "personal_member";
    }

    /** 将平台认证版本转换为跨服务模型。 */
    private MerchantCertificationVersionView toVersionView(RentalMemberCertification source)
    {
        MerchantCertificationVersionView target = new MerchantCertificationVersionView();
        target.setCertificationId(source.getCertificationId());
        target.setVersionNo(source.getVersionNo());
        target.setCertificationType(source.getCertificationType());
        target.setCertificationStatus(source.getCertificationStatus());
        target.setLegalSubjectName(source.getLegalSubjectName());
        target.setLicenseNo(source.getLicenseNo());
        target.setLegalRepresentativeName(source.getLegalRepresentativeName());
        target.setSubmittedAt(toMillis(source.getSubmittedTime()));
        target.setReviewedAt(toMillis(source.getReviewedTime()));
        target.setSubmittedBy(source.getSubmittedBy());
        target.setReviewedBy(source.getReviewedBy());
        target.setReviewRemark(source.getReviewRemark());
        target.setMaterials(source.getMaterials().stream().map(this::toMaterialView).collect(Collectors.toList()));
        return target;
    }

    /** 将平台认证材料转换为跨服务模型。 */
    private MerchantCertificationMaterialView toMaterialView(RentalCertificationMaterial source)
    {
        MerchantCertificationMaterialView target = new MerchantCertificationMaterialView();
        target.setMaterialType(source.getMaterialType());
        target.setMaterialName(source.getMaterialName());
        target.setFileUrl(source.getFileUrl());
        target.setMaterialStatus(source.getMaterialStatus());
        target.setReviewRemark(source.getReviewRemark());
        return target;
    }

    /** 将平台资格快照转换为跨服务模型。 */
    private MerchantQualificationView toQualificationView(RentalMemberQualification source)
    {
        MerchantQualificationView target = new MerchantQualificationView();
        target.setQualificationCode(source.getQualificationCode());
        target.setQualificationStatus(source.getQualificationStatus());
        target.setEffectiveFrom(toMillis(source.getEffectiveFrom()));
        target.setEffectiveTo(toMillis(source.getEffectiveTo()));
        target.setReviewedBy(source.getReviewedBy());
        target.setReviewedAt(toMillis(source.getReviewedTime()));
        target.setReviewRemark(source.getReviewRemark());
        return target;
    }

    /** 将平台会员类型历史转换为跨服务模型。 */
    private MerchantMembershipHistoryView toMembershipHistoryView(RentalMemberMembershipHistory source)
    {
        MerchantMembershipHistoryView target = new MerchantMembershipHistoryView();
        target.setPreviousType(source.getPreviousType());
        target.setMembershipType(source.getMembershipType());
        target.setReason(source.getReason());
        target.setOperatorName(source.getOperatorName());
        target.setOperatedAt(toMillis(source.getOperatedTime()));
        return target;
    }

    /** 将平台接单资格历史转换为跨服务模型。 */
    private MerchantQualificationHistoryView toQualificationHistoryView(RentalMemberQualificationHistory source)
    {
        MerchantQualificationHistoryView target = new MerchantQualificationHistoryView();
        target.setQualificationCode(source.getQualificationCode());
        target.setPreviousStatus(source.getPreviousStatus());
        target.setQualificationStatus(source.getQualificationStatus());
        target.setReason(source.getReason());
        target.setOperatorName(source.getOperatorName());
        target.setOperatedAt(toMillis(source.getOperatedTime()));
        return target;
    }

    /** 将日期转换为跨服务稳定传输的毫秒值。 */
    private Long toMillis(Date date)
    {
        return date == null ? null : date.getTime();
    }
}
