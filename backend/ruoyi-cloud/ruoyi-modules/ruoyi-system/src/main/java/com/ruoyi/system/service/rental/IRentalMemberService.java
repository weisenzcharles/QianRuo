package com.ruoyi.system.service.rental;

import java.util.List;
import com.ruoyi.system.domain.rental.RentalMember;

/**
 * 租赁商户 服务层
 *
 * @author qianruo
 */
public interface IRentalMemberService
{
    /**
     * 查询租赁商户列表
     *
     * @param member 租赁商户
     * @return 租赁商户集合
     */
    public List<RentalMember> selectRentalMemberList(RentalMember member);

    /**
     * 查询租赁商户
     *
     * @param memberId 商户ID
     * @return 租赁商户
     */
    public RentalMember selectRentalMemberById(Long memberId);

    /**
     * 新增租赁商户
     *
     * @param member 租赁商户
     * @return 结果
     */
    public int insertRentalMember(RentalMember member);

    /**
     * 修改租赁商户
     *
     * @param member 租赁商户
     * @return 结果
     */
    public int updateRentalMember(RentalMember member);

    /**
     * 批量删除租赁商户
     *
     * @param memberIds 需要删除的商户ID
     * @return 结果
     */
    public int deleteRentalMemberByIds(Long[] memberIds);

    /**
     * 校验商户编码是否唯一
     *
     * @param member 租赁商户
     * @return 结果
     */
    public boolean checkMemberCodeUnique(RentalMember member);
}
