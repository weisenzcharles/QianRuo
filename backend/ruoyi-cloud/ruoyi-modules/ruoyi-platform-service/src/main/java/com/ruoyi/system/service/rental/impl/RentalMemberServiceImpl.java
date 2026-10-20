package com.ruoyi.system.service.rental.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.merchant.api.RemoteMerchantUserService;
import com.ruoyi.merchant.api.model.MerchantOwnerProvisionCommand;
import com.ruoyi.system.domain.rental.RentalMember;
import com.ruoyi.system.domain.rental.MerchantLevel;
import com.ruoyi.system.mapper.rental.RentalMemberMapper;
import com.ruoyi.system.mapper.rental.MerchantPromotionMapper;
import com.ruoyi.system.model.rental.RentalMemberAuditCommand;
import com.ruoyi.system.model.rental.RentalMemberSupplementCommand;
import com.ruoyi.system.service.rental.IRentalMemberService;
import com.ruoyi.system.service.rental.RentalMemberCertificationService;
import com.ruoyi.system.service.rental.RentalMemberLifecyclePolicy;
import com.ruoyi.system.service.SysCodeRuleService;

/**
 * 租赁商户 服务层处理
 *
 * @author qianruo
 */
@Service
public class RentalMemberServiceImpl implements IRentalMemberService
{
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
     * 查询租赁商户列表
     *
     * @param member 租赁商户
     * @return 租赁商户集合
     */
    @Override
    public List<RentalMember> selectRentalMemberList(RentalMember member)
    {
        return memberMapper.selectRentalMemberList(member);
    }

    /**
     * 查询租赁商户
     *
     * @param memberId 商户唯一编号
     * @return 租赁商户
     */
    @Override
    public RentalMember selectRentalMemberById(Long memberId)
    {
        return memberMapper.selectRentalMemberById(memberId);
    }

    /**
     * 新增租赁商户
     *
     * @param member 租赁商户
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRentalMember(RentalMember member)
    {
        if (StringUtils.isEmpty(member.getContactPhone()))
        {
            throw new ServiceException("新增商户时联系电话不能为空，用于开通商户管理员账号");
        }
        if (memberMapper.selectRentalMemberByContactPhone(member.getContactPhone().trim()) != null)
        {
            throw new ServiceException("联系电话已提交过商户申请");
        }
        if (member.getMembershipType() == null)
        {
            member.setMembershipType(defaultMembershipType(member.getMemberType()));
        }
        member.setMemberCode(codeRuleService.nextCode("merchant"));
        applyMerchantLevel(member, null);
        member.setRegistrationMode(RentalCode.RegistrationMode.PAID.getCode());
        member.setRegistrationStatus(RentalCode.RegistrationStatus.COMPLETED.getCode());
        member.setAuthStatus(RentalCode.AuthenticationStatus.PENDING.getCode());
        member.setAuditRemark(null);
        lifecyclePolicy.prepareForCreate(member, DateUtils.getNowDate());
        int rows = memberMapper.insertRentalMember(member);
        if (rows > 0 && certificationService != null)
        {
            certificationService.createPendingCertification(member,
                    StringUtils.defaultIfBlank(SecurityUtils.getUsername(), "系统"), member.getCreateBy());
        }
        return rows;
    }

    /**
     * 修改租赁商户
     *
     * @param member 租赁商户
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRentalMember(RentalMember member)
    {
        RentalMember previous = memberMapper.selectRentalMemberById(member.getMemberId());
        if (previous == null)
        {
            throw new ServiceException("商户不存在或已删除");
        }
        if (StringUtils.isBlank(member.getContactPhone()))
        {
            throw new ServiceException("联系电话不能为空");
        }
        String contactPhone = member.getContactPhone().trim();
        RentalMember phoneOwner = memberMapper.selectRentalMemberByContactPhone(contactPhone);
        if (phoneOwner != null && !phoneOwner.getMemberId().equals(member.getMemberId()))
        {
            throw new ServiceException("联系电话已被其他商户使用");
        }
        if (RentalCode.AuthenticationStatus.VERIFIED.getCode().equals(previous.getAuthStatus())
                && !StringUtils.equals(previous.getContactPhone(), contactPhone))
        {
            throw new ServiceException("审核通过后的管理员手机号不能在商户资料中修改");
        }
        member.setContactPhone(contactPhone);
        member.setAuthStatus(previous.getAuthStatus());
        member.setAuditTime(previous.getAuditTime());
        member.setAuditRemark(previous.getAuditRemark());
        if (member.getMembershipType() == null)
        {
            member.setMembershipType(previous.getMembershipType());
        }
        // 修改资料时始终沿用数据库中的系统编码，避免客户端通过空值或自定义值覆盖唯一标识。
        member.setMemberCode(previous.getMemberCode());
        applyMerchantLevel(member, previous);
        member.setRegistrationMode(previous.getRegistrationMode());
        member.setRegistrationStatus(previous.getRegistrationStatus());
        member.setInviterMemberId(previous.getInviterMemberId());
        lifecyclePolicy.prepareForUpdate(previous, member, DateUtils.getNowDate());
        return memberMapper.updateRentalMember(member);
    }

    /**
     * 校验并应用平台选择的商户等级，同时把稳定等级编码同步到历史等级快照字段。
     *
     * @param member 待保存商户
     * @param previous 修改前商户，新增时为空
     */
    private void applyMerchantLevel(RentalMember member, RentalMember previous)
    {
        Long levelId = member.getLevelId();
        if (levelId == null && previous != null)
        {
            levelId = previous.getLevelId();
        }
        MerchantLevel level = levelId == null ? promotionMapper.selectDefaultLevel()
                : promotionMapper.selectLevelById(levelId);
        if (level == null || !RentalCode.EnableStatus.ENABLED.getCode().equals(level.getStatus()))
        {
            throw new ServiceException("请选择有效的商户等级");
        }
        member.setLevelId(level.getLevelId());
        member.setVipLevel(level.getLevelCode());
    }

    /**
     * 审核待处理的商户入驻申请，并在审核通过时跨服务开通商户管理员。
     * 审核更新使用待审核状态作为数据库条件，避免并发审核覆盖已经落库的结果。
     *
     * @param memberId 服务端确定的商户唯一编号
     * @param command 平台人员提交的审核结果和审核意见
     * @param operator 当前平台操作人员账号，用于历史展示
     * @param operatorId 当前平台操作人员唯一编号，用于审计关联
     * @return 成功更新的商户记录数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditRentalMember(Long memberId, RentalMemberAuditCommand command, String operator, Long operatorId)
    {
        RentalMember member = memberMapper.selectRentalMemberById(memberId);
        if (member == null)
        {
            throw new ServiceException("商户申请不存在或已删除");
        }
        if (!RentalCode.AuthenticationStatus.PENDING.getCode().equals(member.getAuthStatus()))
        {
            throw new ServiceException("只有待审核的商户申请可以审核");
        }
        if (!RentalCode.AuthenticationStatus.VERIFIED.getCode().equals(command.getAuthStatus())
                && !RentalCode.AuthenticationStatus.REJECTED.getCode().equals(command.getAuthStatus()))
        {
            throw new ServiceException("审核结果只能是通过或驳回");
        }
        if (RentalCode.AuthenticationStatus.REJECTED.getCode().equals(command.getAuthStatus())
                && StringUtils.isBlank(command.getAuditRemark()))
        {
            throw new ServiceException("审核驳回时必须填写原因");
        }

        RentalMember previous = memberMapper.selectRentalMemberById(memberId);
        member.setAuthStatus(command.getAuthStatus());
        member.setAuditRemark(StringUtils.isBlank(command.getAuditRemark()) ? null : command.getAuditRemark().trim());
        member.setUpdateBy(operatorId);
        lifecyclePolicy.prepareForUpdate(previous, member, DateUtils.getNowDate());
        if (certificationService != null)
        {
            certificationService.applyReview(member, command, operator, operatorId);
        }
        int rows = memberMapper.auditRentalMember(member);
        if (rows == 0)
        {
            throw new ServiceException("商户申请状态已变更，请刷新后重新审核");
        }
        if (RentalCode.AuthenticationStatus.VERIFIED.getCode().equals(command.getAuthStatus()))
        {
            provisionMerchantOwner(member, operatorId);
        }
        return rows;
    }

    /**
     * 将待审核认证版本标记为待补件，商户补交后由认证服务创建新版本重新审核。
     *
     * @param memberId 商户唯一编号
     * @param command 补件材料和说明
     * @param operator 当前平台操作人员账号
     * @param operatorId 当前平台操作人员唯一编号
     * @return 成功更新的认证记录数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int requestCertificationSupplement(Long memberId, RentalMemberSupplementCommand command,
            String operator, Long operatorId)
    {
        RentalMember member = memberMapper.selectRentalMemberById(memberId);
        if (member == null)
        {
            throw new ServiceException("商户申请不存在或已删除");
        }
        if (!RentalCode.AuthenticationStatus.PENDING.getCode().equals(member.getAuthStatus()))
        {
            throw new ServiceException("只有待审核的商户申请可以要求补件");
        }
        certificationService.requestSupplement(member, command, operator, operatorId);
        return 1;
    }

    /**
     * 批量删除租赁商户
     *
     * @param memberIds 需要删除的商户唯一编号
     * @return 结果
     */
    @Override
    public int deleteRentalMemberByIds(Long[] memberIds)
    {
        return memberMapper.deleteRentalMemberByIds(memberIds);
    }

    /**
     * 调用商户身份服务幂等开通商户所有者账号、管理员角色及对应菜单权限。
     * 远程调用失败时抛出业务异常，使当前审核事务回滚，避免出现已审核但未开户的中间状态。
     *
     * @param member 已通过审核的商户资料
     * @param operatorId 执行审核的平台操作人员唯一编号
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
            throw new ServiceException(result == null ? "商户管理员开通服务无响应" : result.getMsg());
        }
    }

    /**
     * 根据商户主体类型生成平台默认会员类型，避免历史表单未提交新字段时写入空值。
     *
     * @param memberType 商户主体类型
     * @return 平台会员类型编码
     */
    private Integer defaultMembershipType(Integer memberType)
    {
        return RentalCode.MemberType.ENTERPRISE.getCode().equals(memberType)
                ? RentalCode.MembershipType.ORDINARY_ENTERPRISE_MEMBER.getCode()
                : RentalCode.MembershipType.PERSONAL_MEMBER.getCode();
    }
}
