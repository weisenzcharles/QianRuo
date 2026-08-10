package com.ruoyi.system.mapper.rental;

import java.util.List;
import com.ruoyi.system.domain.rental.RentalMember;

/**
 * 租赁商户 数据层
 *
 * @author qianruo
 */
public interface RentalMemberMapper
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
     * 按注册手机号查询未删除的商户申请。
     *
     * @param contactPhone 商户申请时验证的手机号
     * @return 对应商户申请，不存在时返回 {@code null}
     */
    public RentalMember selectRentalMemberByContactPhone(String contactPhone);

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
     * 仅当申请仍为待审核状态时写入审核结果，作为并发审核的数据库保护条件。
     *
     * @param member 包含审核结果、审核时间和操作人员的商户记录
     * @return 成功更新的记录数，返回零表示状态已被其他请求修改
     */
    public int auditRentalMember(RentalMember member);

    /**
     * 仅当申请处于已驳回状态时将其恢复为待审核，并清理上次审核结果。
     *
     * @param member 待重新提交的商户记录
     * @return 成功更新的记录数，返回零表示申请状态已经变化
     */
    public int markRegistrationPending(RentalMember member);

    /**
     * 批量删除租赁商户
     *
     * @param memberIds 需要删除的商户ID
     * @return 结果
     */
    public int deleteRentalMemberByIds(Long[] memberIds);

}
