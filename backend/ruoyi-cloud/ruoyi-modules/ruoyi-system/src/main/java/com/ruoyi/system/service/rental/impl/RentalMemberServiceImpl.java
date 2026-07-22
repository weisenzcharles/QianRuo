package com.ruoyi.system.service.rental.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.UserConstants;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.system.domain.rental.RentalMember;
import com.ruoyi.system.mapper.rental.RentalMemberMapper;
import com.ruoyi.system.service.rental.IRentalMemberService;

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
    public int insertRentalMember(RentalMember member)
    {
        return memberMapper.insertRentalMember(member);
    }

    /**
     * 修改租赁商户
     *
     * @param member 租赁商户
     * @return 结果
     */
    @Override
    public int updateRentalMember(RentalMember member)
    {
        return memberMapper.updateRentalMember(member);
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
     * 校验商户编码是否唯一
     *
     * @param member 租赁商户
     * @return 结果
     */
    @Override
    public boolean checkMemberCodeUnique(RentalMember member)
    {
        Long memberId = StringUtils.isNull(member.getMemberId()) ? -1L : member.getMemberId();
        RentalMember info = memberMapper.checkMemberCodeUnique(member.getMemberCode());
        if (StringUtils.isNotNull(info) && info.getMemberId().longValue() != memberId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }
}
