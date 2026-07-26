package com.ruoyi.system.mapper.rental;

import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.rental.RentalUserMember;

/**
 * 商户员工绑定 数据层
 *
 * @author qianruo
 */
public interface RentalUserMemberMapper
{
    /**
     * 查询商户用户当前有效绑定。
     *
     * @param merchantUserId 商户用户ID
     * @return 商户绑定
     */
    public RentalUserMember selectActiveByUserId(@Param("merchantUserId") Long merchantUserId);

    /**
     * 查询用户有效商户绑定数量
     *
     * @param merchantUserId 商户用户ID
     * @return 绑定数量
     */
    public int countActiveByUserId(@Param("merchantUserId") Long merchantUserId);

    /**
     * 查询用户和商户绑定数量
     *
     * @param memberId 商户ID
     * @param merchantUserId 商户用户ID
     * @return 绑定数量
     */
    public int countByMemberIdAndUserId(@Param("memberId") Long memberId,
            @Param("merchantUserId") Long merchantUserId);

    /**
     * 新增商户管理员绑定
     *
     * @param tenantId 租户ID
     * @param memberId 商户ID
     * @param merchantUserId 商户用户ID
     * @param createBy 创建人
     * @return 结果
     */
    public int insertOwnerBinding(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("merchantUserId") Long merchantUserId, @Param("createBy") String createBy);

    public int insertBinding(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("merchantUserId") Long merchantUserId, @Param("memberRole") String memberRole,
            @Param("warehouseScopeType") String warehouseScopeType, @Param("createBy") String createBy);

    public int updateWarehouseScope(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("merchantUserId") Long merchantUserId, @Param("memberRole") String memberRole,
            @Param("warehouseScopeType") String warehouseScopeType, @Param("updateBy") String updateBy);
}
