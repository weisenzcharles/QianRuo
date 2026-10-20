package com.ruoyi.system.mapper.rental;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.rental.RentalMember;
import com.ruoyi.system.model.rental.PlatformMerchantOverviewBase;
import com.ruoyi.system.model.rental.PlatformMerchantBusinessScope;

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
     * @param memberId 商户唯一编号
     * @return 租赁商户
     */
    public RentalMember selectRentalMemberById(Long memberId);

    /** 按商户唯一编号集合查询商铺基础信息。 */
    public List<RentalMember> selectRentalMembersByIds(@Param("memberIds") List<Long> memberIds);

    /** 按商铺名称、主体名称或商户编码查询商户唯一编号，供平台跨服务筛选订单。 */
    public List<Long> selectRentalMemberIdsByKeyword(@Param("keyword") String keyword);

    /** 按平台商户筛选条件分页查询经营概况所需的最小基础信息。 */
    public List<PlatformMerchantOverviewBase> selectRentalMemberOverviewPage(
            @Param("memberCode") String memberCode,
            @Param("nickname") String nickname,
            @Param("legalSubjectName") String legalSubjectName,
            @Param("memberType") Integer memberType,
            @Param("authStatus") Integer authStatus,
            @Param("status") Integer status);

    /** 查询全部未删除商户的最小经营分析基础信息。 */
    public List<PlatformMerchantOverviewBase> selectAllRentalMemberOverview();

    /** 按商户编码解析租赁业务调用所需的可信归属范围。 */
    public PlatformMerchantBusinessScope selectRentalMemberBusinessScopeByCode(@Param("memberCode") String memberCode);

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
     * @param memberIds 需要删除的商户唯一编号
     * @return 结果
     */
    public int deleteRentalMemberByIds(Long[] memberIds);

}
