package com.ruoyi.merchant.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.merchant.api.model.MerchantAllianceOption;

/** 商户联盟关系数据访问。 */
public interface MerchantAllianceMapper
{
    /** 查询与当前商户处于同一有效联盟的其他商户。 */
    List<MerchantAllianceOption> selectOptions(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId);
    List<MerchantAllianceOption> selectMemberOptions(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId);

    Map<String, Object> selectCurrent(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId);
    List<MerchantAllianceOption> selectMembers(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId);
    int insertAlliance(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("name") String name, @Param("operator") Long operator);
    Long selectLatestAllianceId(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId);
    int insertMember(@Param("tenantId") Long tenantId, @Param("allianceId") Long allianceId,
            @Param("memberId") Long memberId, @Param("operator") Long operator);
    int updateMember(@Param("tenantId") Long tenantId, @Param("allianceId") Long allianceId,
            @Param("memberId") Long memberId, @Param("shareStockEnabled") Integer shareStockEnabled,
            @Param("acceptOrderLimit") Integer acceptOrderLimit, @Param("ownerMemberId") Long ownerMemberId);
    int removeMember(@Param("tenantId") Long tenantId, @Param("allianceId") Long allianceId,
            @Param("memberId") Long memberId, @Param("ownerMemberId") Long ownerMemberId);
    List<Long> selectSharedMemberIds(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId);
    List<MerchantAllianceOption> selectSharedMembers(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId);
    List<Map<String, Object>> selectAdminAlliances(@Param("keyword") String keyword, @Param("status") Integer status);
    List<MerchantAllianceOption> selectAdminMembers(@Param("allianceId") Long allianceId);
    int updateAdminAllianceStatus(@Param("allianceId") Long allianceId, @Param("status") Integer status);
    int updateAdminMemberStatus(@Param("allianceId") Long allianceId, @Param("memberId") Long memberId,
            @Param("status") Integer status);
    int insertOrderSlot(@Param("tenantId") Long tenantId, @Param("allianceId") Long allianceId,
            @Param("memberId") Long memberId, @Param("allianceOrderId") Long allianceOrderId,
            @Param("operatorId") Long operatorId);
    boolean existsActiveOrderSlot(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("allianceOrderId") Long allianceOrderId);
    int incrementOrderSlot(@Param("tenantId") Long tenantId, @Param("allianceId") Long allianceId,
            @Param("memberId") Long memberId);
    int releaseOrderSlot(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("allianceOrderId") Long allianceOrderId, @Param("operatorId") Long operatorId);
    int decrementOrderSlot(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId);
}
