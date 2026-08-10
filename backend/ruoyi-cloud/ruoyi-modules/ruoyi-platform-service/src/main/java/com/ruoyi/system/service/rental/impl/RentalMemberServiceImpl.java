package com.ruoyi.system.service.rental.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.merchant.api.RemoteMerchantUserService;
import com.ruoyi.merchant.api.model.MerchantOwnerProvisionCommand;
import com.ruoyi.system.domain.rental.RentalMember;
import com.ruoyi.system.mapper.rental.RentalMemberMapper;
import com.ruoyi.system.model.rental.RentalMemberAuditCommand;
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
     * @param memberId 商户ID
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
        if (StringUtils.isBlank(member.getMembershipType()))
        {
            member.setMembershipType(defaultMembershipType(member.getMemberType()));
        }
        member.setMemberCode(codeRuleService.nextCode("merchant"));
        member.setAuthStatus("pending");
        member.setAuditRemark(null);
        lifecyclePolicy.prepareForCreate(member, DateUtils.getNowDate());
        int rows = memberMapper.insertRentalMember(member);
        if (rows > 0 && certificationService != null)
        {
            certificationService.createPendingCertification(member, member.getCreateBy());
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
        if ("verified".equals(previous.getAuthStatus())
                && !StringUtils.equals(previous.getContactPhone(), contactPhone))
        {
            throw new ServiceException("审核通过后的管理员手机号不能在商户资料中修改");
        }
        member.setContactPhone(contactPhone);
        member.setAuthStatus(previous.getAuthStatus());
        member.setAuditTime(previous.getAuditTime());
        member.setAuditRemark(previous.getAuditRemark());
        if (StringUtils.isBlank(member.getMembershipType()))
        {
            member.setMembershipType(previous.getMembershipType());
        }
        // 修改资料时始终沿用数据库中的系统编码，避免客户端通过空值或自定义值覆盖唯一标识。
        member.setMemberCode(previous.getMemberCode());
        lifecyclePolicy.prepareForUpdate(previous, member, DateUtils.getNowDate());
        return memberMapper.updateRentalMember(member);
    }

    /**
     * 审核待处理的商户入驻申请，并在审核通过时跨服务开通商户管理员。
     * 审核更新使用待审核状态作为数据库条件，避免并发审核覆盖已经落库的结果。
     *
     * @param memberId 服务端确定的商户主键
     * @param command 平台人员提交的审核结果和审核意见
     * @param operator 当前平台操作人员账号
     * @return 成功更新的商户记录数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditRentalMember(Long memberId, RentalMemberAuditCommand command, String operator)
    {
        RentalMember member = memberMapper.selectRentalMemberById(memberId);
        if (member == null)
        {
            throw new ServiceException("商户申请不存在或已删除");
        }
        if (!"pending".equals(member.getAuthStatus()))
        {
            throw new ServiceException("只有待审核的商户申请可以审核");
        }
        if (!StringUtils.equalsAny(command.getAuthStatus(), "verified", "rejected"))
        {
            throw new ServiceException("审核结果只能是通过或驳回");
        }
        if ("rejected".equals(command.getAuthStatus()) && StringUtils.isBlank(command.getAuditRemark()))
        {
            throw new ServiceException("审核驳回时必须填写原因");
        }

        RentalMember previous = memberMapper.selectRentalMemberById(memberId);
        member.setAuthStatus(command.getAuthStatus());
        member.setAuditRemark(StringUtils.isBlank(command.getAuditRemark()) ? null : command.getAuditRemark().trim());
        member.setUpdateBy(operator);
        lifecyclePolicy.prepareForUpdate(previous, member, DateUtils.getNowDate());
        if (certificationService != null)
        {
            certificationService.applyReview(member, command, operator);
        }
        int rows = memberMapper.auditRentalMember(member);
        if (rows == 0)
        {
            throw new ServiceException("商户申请状态已变更，请刷新后重新审核");
        }
        if ("verified".equals(command.getAuthStatus()))
        {
            provisionMerchantOwner(member, operator);
        }
        return rows;
    }

    /**
     * 批量删除租赁商户
     *
     * @param memberIds 需要删除的商户ID
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
     * @param operator 执行审核的平台操作人员账号
     */
    private void provisionMerchantOwner(RentalMember member, String operator)
    {
        MerchantOwnerProvisionCommand provision = new MerchantOwnerProvisionCommand();
        provision.setTenantId(member.getTenantId());
        provision.setMerchantId(member.getMemberId());
        provision.setPhone(member.getContactPhone());
        provision.setNickname(StringUtils.isBlank(member.getContactName()) ? member.getNickname() : member.getContactName());
        provision.setEmail(member.getContactEmail());
        provision.setOperator(operator);
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
    private String defaultMembershipType(String memberType)
    {
        return "enterprise".equals(memberType) ? "ordinary_enterprise_member" : "personal_member";
    }
}
