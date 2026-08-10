package com.ruoyi.system.service.rental;

import java.util.List;
import com.ruoyi.system.domain.rental.RentalMember;
import com.ruoyi.system.model.rental.RentalMemberAuditCommand;

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
     * 审核待处理的商户申请，并在通过时开通商户管理员账号。
     *
     * @param memberId 服务端商户主键
     * @param command 审核结果和审核意见
     * @param operator 当前平台操作人员
     * @return 成功更新的记录数
     */
    public int auditRentalMember(Long memberId, RentalMemberAuditCommand command, String operator);

    /**
     * 批量删除租赁商户
     *
     * @param memberIds 需要删除的商户ID
     * @return 结果
     */
    public int deleteRentalMemberByIds(Long[] memberIds);

}
