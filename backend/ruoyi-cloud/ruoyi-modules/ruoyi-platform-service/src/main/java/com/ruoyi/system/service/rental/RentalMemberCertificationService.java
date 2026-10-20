package com.ruoyi.system.service.rental;

import java.util.Date;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.enums.RentalCode;
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
import com.ruoyi.system.model.rental.RentalMemberSupplementCommand;
import com.ruoyi.system.api.model.MerchantCertificationMaterialView;
import com.ruoyi.system.api.model.MerchantCertificationMaterialCommand;
import com.ruoyi.system.api.model.MerchantCertificationSupplementCommand;
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
    private static final List<String> QUALIFICATION_CODES = List.of(
            "external_accept", "alliance_accept", "mini_program_accept");

    private static final Set<Integer> MEMBERSHIP_TYPES = Set.of(
            RentalCode.MembershipType.PERSONAL_MEMBER.getCode(),
            RentalCode.MembershipType.ORDINARY_ENTERPRISE_MEMBER.getCode(),
            RentalCode.MembershipType.SETTLED_MEMBER.getCode());

    private static final Map<Integer, String> MATERIAL_NAMES = Map.of(
            RentalCode.CertificationMaterialType.LICENSE.getCode(), "营业执照",
            RentalCode.CertificationMaterialType.LEGAL_ID_FRONT.getCode(), "身份证人像面",
            RentalCode.CertificationMaterialType.LEGAL_ID_BACK.getCode(), "身份证国徽面",
            RentalCode.CertificationMaterialType.CONTRACT.getCode(), "签约合同",
            RentalCode.CertificationMaterialType.OTHER.getCode(), "其他认证材料");

    @Autowired
    private RentalMemberCertificationMapper certificationMapper;

    /**
     * 为新注册或驳回重提的商户创建一份新的待审核认证版本，并复制当前材料地址。
     *
     * @param member 当前商户主体快照
     * @param operator 资料提交人名称，用于历史展示
     * @param operatorId 资料提交人用户唯一编号，用于审计关联
     * @return 新创建的认证版本
     */
    @Transactional(rollbackFor = Exception.class)
    public RentalMemberCertification createPendingCertification(RentalMember member, String operator, Long operatorId)
    {
        RentalMemberCertification latest = certificationMapper.selectLatestCertification(member.getMemberId());
        int versionNo = latest == null || latest.getVersionNo() == null ? 1 : latest.getVersionNo() + 1;
        Date now = DateUtils.getNowDate();
        RentalMemberCertification certification = snapshot(member, versionNo, operator, operatorId, now);
        certificationMapper.insertCertification(certification);
        insertMaterials(member, certification, operator, operatorId);
        return certification;
    }

    /**
     * 查询商户认证历史，并为每个版本加载材料明细。
     *
     * @param memberId 商户唯一编号
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
     * @param memberId 商户唯一编号
     * @return 当前接单资格
     */
    public List<RentalMemberQualification> selectQualifications(Long memberId)
    {
        return certificationMapper.selectQualifications(memberId);
    }

    /**
     * 查询商户会员类型变更历史。
     *
     * @param memberId 商户唯一编号
     * @return 会员类型历史
     */
    public List<RentalMemberMembershipHistory> selectMembershipHistory(Long memberId)
    {
        return certificationMapper.selectMembershipHistory(memberId);
    }

    /**
     * 查询商户接单资格变更历史。
     *
     * @param memberId 商户唯一编号
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
     * @param operator 平台操作人名称，用于历史展示
     * @param operatorId 平台操作人员唯一编号，用于审计关联
     */
    @Transactional(rollbackFor = Exception.class)
    public void applyReview(RentalMember member, RentalMemberAuditCommand command, String operator, Long operatorId)
    {
        RentalMemberCertification certification = certificationMapper.selectLatestCertification(member.getMemberId());
        if (certification == null)
        {
            certification = createPendingCertification(member, operator, operatorId);
        }
        if (!RentalCode.CertificationStatus.PENDING.getCode().equals(certification.getCertificationStatus()))
        {
            throw new ServiceException("当前认证版本已经审核，请刷新后重试");
        }

        Date now = DateUtils.getNowDate();
        certification.setCertificationStatus(RentalCode.AuthenticationStatus.VERIFIED.getCode().equals(command.getAuthStatus())
                ? RentalCode.CertificationStatus.APPROVED.getCode()
                : RentalCode.CertificationStatus.REJECTED.getCode());
        certification.setReviewedBy(operator);
        certification.setReviewedTime(now);
        certification.setReviewRemark(command.getAuditRemark());
        certification.setUpdateBy(operatorId);
        if (certificationMapper.updateCertificationReview(certification) == 0)
        {
            throw new ServiceException("认证版本状态已变更，请刷新后重新审核");
        }

        if (RentalCode.AuthenticationStatus.VERIFIED.getCode().equals(command.getAuthStatus()))
        {
            applyMembershipType(member, command, operator, operatorId, now);
            applyQualifications(member, certification, command, operator, operatorId, now);
        }
    }

    /**
     * 将待审核认证版本标记为待补件，并保存平台要求补交的材料类型。
     *
     * @param member 当前商户主体
     * @param command 平台补件要求
     * @param operator 平台操作人名称
     * @param operatorId 平台操作人员唯一编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void requestSupplement(RentalMember member, RentalMemberSupplementCommand command,
            String operator, Long operatorId)
    {
        if (command == null || StringUtils.isBlank(command.getAuditRemark()))
        {
            throw new ServiceException("补件说明不能为空");
        }
        List<Integer> materialTypes = normalizeMaterialTypes(command.getMaterialTypes());
        if (materialTypes.isEmpty())
        {
            throw new ServiceException("请选择至少一项需要补交的材料");
        }
        RentalMemberCertification certification = certificationMapper.selectLatestCertification(member.getMemberId());
        if (certification == null)
        {
            certification = createPendingCertification(member, operator, operatorId);
        }
        if (!RentalCode.CertificationStatus.PENDING.getCode().equals(certification.getCertificationStatus()))
        {
            throw new ServiceException("当前认证版本不是待审核状态，不能要求补件");
        }
        certification.setCertificationStatus(RentalCode.CertificationStatus.SUPPLEMENT_REQUIRED.getCode());
        certification.setSupplementMaterialTypes(joinMaterialTypes(materialTypes));
        certification.setReviewRemark(command.getAuditRemark().trim());
        certification.setReviewedBy(operator);
        certification.setReviewedTime(DateUtils.getNowDate());
        certification.setUpdateBy(operatorId);
        if (certificationMapper.updateCertificationSupplement(certification) == 0)
        {
            throw new ServiceException("认证版本状态已变更，请刷新后重新操作");
        }
    }

    /**
     * 校验商户补件并生成新的待审核认证版本。原版本及原材料保持不变，未被替换的材料继续复制到新版本。
     *
     * @param member 当前商户主体
     * @param command 商户上传的补件材料
     * @return 更新后的商户认证聚合资料
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantCertificationView submitSupplement(RentalMember member,
            MerchantCertificationSupplementCommand command)
    {
        RentalMemberCertification latest = certificationMapper.selectLatestCertification(member.getMemberId());
        if (latest == null || !RentalCode.CertificationStatus.SUPPLEMENT_REQUIRED.getCode()
                .equals(latest.getCertificationStatus()))
        {
            throw new ServiceException("当前没有待补交的认证材料");
        }
        Map<Integer, MerchantCertificationMaterialCommand> submitted = normalizeSubmittedMaterials(command);
        List<Integer> requiredTypes = normalizeMaterialTypes(parseMaterialTypes(latest.getSupplementMaterialTypes()));
        if (!submitted.keySet().containsAll(requiredTypes))
        {
            List<Integer> missing = requiredTypes.stream()
                    .filter(type -> !submitted.containsKey(type)).collect(Collectors.toList());
            throw new ServiceException("请补齐材料：" + missing.stream()
                    .map(this::materialName).collect(Collectors.joining("、")));
        }
        if (!requiredTypes.containsAll(submitted.keySet()))
        {
            throw new ServiceException("只能提交平台要求补交的材料");
        }

        String operator = StringUtils.isBlank(command.getOperatorName()) ? "merchant" : command.getOperatorName();
        Date now = DateUtils.getNowDate();
        int versionNo = latest.getVersionNo() == null ? 1 : latest.getVersionNo() + 1;
        RentalMemberCertification certification = snapshot(latest, versionNo, operator, command.getOperatorId(), now);
        certificationMapper.insertCertification(certification);

        List<RentalCertificationMaterial> previousMaterials = certificationMapper
                .selectMaterials(latest.getCertificationId());
        if (previousMaterials == null)
        {
            previousMaterials = List.of();
        }
        Map<Integer, RentalCertificationMaterial> previousByType = new LinkedHashMap<>();
        for (RentalCertificationMaterial material : previousMaterials)
        {
            previousByType.putIfAbsent(material.getMaterialType(), material);
        }
        for (RentalCertificationMaterial previous : previousByType.values())
        {
            MerchantCertificationMaterialCommand replacement = submitted.get(previous.getMaterialType());
            insertVersionMaterial(certification, previous.getMaterialType(), previous.getMaterialName(),
                    replacement == null ? previous.getFileUrl() : replacement.getFileUrl(),
                    previous.getSortNo(), command.getOperatorId());
        }
        for (MerchantCertificationMaterialCommand replacement : submitted.values())
        {
            if (!previousByType.containsKey(replacement.getMaterialType()))
            {
                insertVersionMaterial(certification, replacement.getMaterialType(),
                        materialName(replacement.getMaterialType()), replacement.getFileUrl(),
                        replacement.getMaterialType(), command.getOperatorId());
            }
        }
        return toMerchantView(member);
    }

    /**
     * 创建认证版本快照，避免后续编辑商户主体资料时篡改历史认证内容。
     *
     * @param member 商户主体
     * @param versionNo 新版本号
     * @param operator 资料提交人名称，用于历史展示
     * @param operatorId 资料提交人用户唯一编号，用于审计关联
     * @param now 提交时间
     * @return 认证版本快照
     */
    private RentalMemberCertification snapshot(RentalMember member, int versionNo, String operator, Long operatorId, Date now)
    {
        RentalMemberCertification certification = new RentalMemberCertification();
        certification.setTenantId(member.getTenantId());
        certification.setMemberId(member.getMemberId());
        certification.setVersionNo(versionNo);
        certification.setCertificationType(member.getMemberType());
        certification.setCertificationStatus(RentalCode.CertificationStatus.PENDING.getCode());
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
        certification.setCreateBy(operatorId);
        certification.setRemark(member.getRemark());
        return certification;
    }

    /** 从上一认证版本复制主体快照，补件只替换文件材料，不篡改历史认证文本。 */
    private RentalMemberCertification snapshot(RentalMemberCertification source, int versionNo,
            String operator, Long operatorId, Date now)
    {
        RentalMemberCertification certification = new RentalMemberCertification();
        certification.setTenantId(source.getTenantId());
        certification.setMemberId(source.getMemberId());
        certification.setVersionNo(versionNo);
        certification.setCertificationType(source.getCertificationType());
        certification.setCertificationStatus(RentalCode.CertificationStatus.PENDING.getCode());
        certification.setLegalSubjectName(source.getLegalSubjectName());
        certification.setLicenseNo(source.getLicenseNo());
        certification.setRegisteredCapital(source.getRegisteredCapital());
        certification.setBusinessScope(source.getBusinessScope());
        certification.setLicenseAddress(source.getLicenseAddress());
        certification.setLicenseValidFrom(source.getLicenseValidFrom());
        certification.setLicenseValidTo(source.getLicenseValidTo());
        certification.setLicenseLongTerm(source.getLicenseLongTerm());
        certification.setLegalRepresentativeName(source.getLegalRepresentativeName());
        certification.setLegalRepresentativeIdNo(source.getLegalRepresentativeIdNo());
        certification.setSubmittedBy(operator);
        certification.setSubmittedTime(now);
        certification.setCreateBy(operatorId);
        certification.setRemark(source.getRemark());
        return certification;
    }

    /**
     * 将商户主体上的认证文件地址拆成材料行，材料表只保存文件引用，不保存文件二进制。
     *
     * @param member 商户主体
     * @param certification 认证版本
     * @param operator 资料提交人名称，用于历史展示
     * @param operatorId 资料提交人用户唯一编号，用于审计关联
     */
    private void insertMaterials(RentalMember member, RentalMemberCertification certification, String operator, Long operatorId)
    {
        String identityCardName = RentalCode.MemberType.ENTERPRISE.getCode().equals(member.getMemberType())
                ? "法人身份证" : "身份证";
        insertMaterial(certification, RentalCode.CertificationMaterialType.LICENSE.getCode(), "营业执照",
                member.getLicenseImageUrl(), operator, operatorId, 1);
        insertMaterial(certification, RentalCode.CertificationMaterialType.LEGAL_ID_FRONT.getCode(), identityCardName + "正面",
                member.getLegalIdFrontUrl(), operator, operatorId, 2);
        insertMaterial(certification, RentalCode.CertificationMaterialType.LEGAL_ID_BACK.getCode(), identityCardName + "反面",
                member.getLegalIdBackUrl(), operator, operatorId, 3);
        insertMaterial(certification, RentalCode.CertificationMaterialType.CONTRACT.getCode(), "签约合同",
                member.getContractFileUrl(), operator, operatorId, 4);
    }

    /**
     * 在文件地址非空时新增一条认证材料。
     *
     * @param certification 认证版本
     * @param type 材料类型
     * @param name 材料名称
     * @param fileUrl 文件地址
     * @param operator 操作人名称，用于展示
     * @param operatorId 操作人用户唯一编号，用于审计关联
     * @param sortNo 排序值
     */
    private void insertMaterial(RentalMemberCertification certification, Integer type, String name, String fileUrl,
            String operator, Long operatorId, int sortNo)
    {
        if (StringUtils.isBlank(fileUrl))
        {
            return;
        }
        insertVersionMaterial(certification, type, name, fileUrl, sortNo, operatorId);
    }

    /** 新增认证版本材料，所有文件地址都必须来自统一文件服务。 */
    private void insertVersionMaterial(RentalMemberCertification certification, Integer type, String name,
            String fileUrl, Integer sortNo, Long operatorId)
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
        material.setMaterialStatus(RentalCode.CertificationStatus.PENDING.getCode());
        material.setSortNo(sortNo);
        material.setCreateBy(operatorId);
        certificationMapper.insertMaterial(material);
    }

    /** 过滤并去重平台允许的材料类型，防止请求直接写入未登记编码。 */
    private List<Integer> normalizeMaterialTypes(List<Integer> materialTypes)
    {
        if (materialTypes == null)
        {
            return List.of();
        }
        return materialTypes.stream().filter(Objects::nonNull).distinct()
                .filter(MATERIAL_NAMES::containsKey).collect(Collectors.toList());
    }

    /** 解析数据库中逗号分隔的受控材料类型。 */
    private List<Integer> parseMaterialTypes(String materialTypes)
    {
        if (StringUtils.isBlank(materialTypes))
        {
            return List.of();
        }
        return Arrays.stream(materialTypes.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(value -> {
                    try
                    {
                        return Integer.valueOf(value);
                    }
                    catch (NumberFormatException ex)
                    {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String joinMaterialTypes(List<Integer> materialTypes)
    {
        return materialTypes.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private String materialName(Integer materialType)
    {
        return MATERIAL_NAMES.getOrDefault(materialType, "认证材料");
    }

    private Map<Integer, MerchantCertificationMaterialCommand> normalizeSubmittedMaterials(
            MerchantCertificationSupplementCommand command)
    {
        if (command == null || command.getMaterials() == null || command.getMaterials().isEmpty())
        {
            throw new ServiceException("请上传补交的认证材料");
        }
        Map<Integer, MerchantCertificationMaterialCommand> submitted = new LinkedHashMap<>();
        for (MerchantCertificationMaterialCommand material : command.getMaterials())
        {
            if (material == null || !MATERIAL_NAMES.containsKey(material.getMaterialType())
                    || StringUtils.isBlank(material.getFileUrl()))
            {
                throw new ServiceException("补件材料类型或文件地址不正确");
            }
            if (submitted.put(material.getMaterialType(), material) != null)
            {
                throw new ServiceException("同一材料不能重复提交");
            }
        }
        return submitted;
    }

    /**
     * 应用审核通过后的会员类型，并记录前后值，避免只看当前字段无法追溯历史。
     *
     * @param member 商户主体
     * @param command 审核命令
     * @param operator 操作人名称，用于历史展示
     * @param operatorId 操作人用户唯一编号，用于审计关联
     * @param now 当前时间
     */
    private void applyMembershipType(RentalMember member, RentalMemberAuditCommand command, String operator, Long operatorId, Date now)
    {
        Integer membershipType = command.getMembershipType() == null
                ? defaultMembershipType(member.getMemberType()) : command.getMembershipType();
        if (!MEMBERSHIP_TYPES.contains(membershipType))
        {
            throw new ServiceException("会员类型不正确");
        }
        Integer previous = member.getMembershipType();
        member.setMembershipType(membershipType);
        if (!Objects.equals(previous, membershipType))
        {
            RentalMemberMembershipHistory history = new RentalMemberMembershipHistory();
            history.setTenantId(member.getTenantId());
            history.setMemberId(member.getMemberId());
            history.setPreviousType(previous);
            history.setMembershipType(membershipType);
            history.setReason(command.getAuditRemark());
            history.setOperatorName(operator);
            history.setOperatedTime(now);
            history.setCreateBy(operatorId);
            certificationMapper.insertMembershipHistory(history);
        }
    }

    /**
     * 应用审核通过后的接单资格集合。未勾选的资格会明确置为 disabled，防止旧权限残留。
     *
     * @param member 商户主体
     * @param certification 已审核通过的认证版本
     * @param command 审核命令
     * @param operator 操作人名称，用于历史展示
     * @param operatorId 操作人用户唯一编号，用于审计关联
     * @param now 当前时间
     */
    private void applyQualifications(RentalMember member, RentalMemberCertification certification,
            RentalMemberAuditCommand command, String operator, Long operatorId, Date now)
    {
        if (command.getQualificationCodes() == null)
        {
            return;
        }
        Set<String> selected = command.getQualificationCodes().stream()
                .filter(QUALIFICATION_CODES::contains).collect(Collectors.toSet());
        member.setExternalAcceptEnabled(selected.contains("external_accept")
                ? RentalCode.YesNo.YES.getCode() : RentalCode.YesNo.NO.getCode());
        for (String code : QUALIFICATION_CODES)
        {
            Integer status = selected.contains(code)
                    ? RentalCode.QualificationStatus.ENABLED.getCode()
                    : RentalCode.QualificationStatus.DISABLED.getCode();
            RentalMemberQualification current = certificationMapper.selectQualification(member.getMemberId(), code);
            if (current == null)
            {
                current = new RentalMemberQualification();
                current.setTenantId(member.getTenantId());
                current.setMemberId(member.getMemberId());
                current.setQualificationCode(code);
                current.setQualificationStatus(status);
                current.setCertificationId(certification.getCertificationId());
                current.setEffectiveFrom(RentalCode.QualificationStatus.ENABLED.getCode().equals(status) ? now : null);
                current.setReviewedBy(operator);
                current.setReviewedTime(now);
                current.setReviewRemark(command.getAuditRemark());
                current.setCreateBy(operatorId);
                certificationMapper.insertQualification(current);
            }
            else
            {
                Integer previous = current.getQualificationStatus();
                current.setQualificationStatus(status);
                current.setCertificationId(certification.getCertificationId());
                current.setEffectiveFrom(RentalCode.QualificationStatus.ENABLED.getCode().equals(status) ? now : null);
                current.setEffectiveTo(RentalCode.QualificationStatus.ENABLED.getCode().equals(status) ? null : now);
                current.setReviewedBy(operator);
                current.setReviewedTime(now);
                current.setReviewRemark(command.getAuditRemark());
                current.setUpdateBy(operatorId);
                certificationMapper.updateQualification(current);
                if (!Objects.equals(previous, status))
                {
                    insertQualificationHistory(member, current, previous, operator, operatorId, now, command.getAuditRemark());
                }
                continue;
            }
            insertQualificationHistory(member, current, null, operator, operatorId, now, command.getAuditRemark());
        }
    }

    /**
     * 追加一条接单资格历史记录。
     *
     * @param member 商户主体
     * @param current 当前资格快照
     * @param previous 变更前状态
     * @param operator 操作人名称，用于历史展示
     * @param operatorId 操作人用户唯一编号，用于审计关联
     * @param now 操作时间
     * @param reason 变更原因
     */
    private void insertQualificationHistory(RentalMember member, RentalMemberQualification current, Integer previous,
            String operator, Long operatorId, Date now, String reason)
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
        history.setCreateBy(operatorId);
        certificationMapper.insertQualificationHistory(history);
    }

    /**
     * 根据主体类型给新申请确定默认会员类型。
     *
     * @param memberType 主体类型
     * @return 默认会员类型编码
     */
    private Integer defaultMembershipType(Integer memberType)
    {
        return RentalCode.MemberType.ENTERPRISE.getCode().equals(memberType)
                ? RentalCode.MembershipType.ORDINARY_ENTERPRISE_MEMBER.getCode()
                : RentalCode.MembershipType.PERSONAL_MEMBER.getCode();
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
        target.setSupplementMaterialTypes(parseMaterialTypes(source.getSupplementMaterialTypes()));
        List<RentalCertificationMaterial> materials = source.getMaterials() == null
                ? List.of() : source.getMaterials();
        target.setMaterials(materials.stream().map(this::toMaterialView).collect(Collectors.toList()));
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
