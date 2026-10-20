package com.ruoyi.system.mapper.rental;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.model.rental.PlatformMemberBlacklistRecordVO;
import com.ruoyi.system.model.rental.PlatformMemberCreditRecordVO;
import com.ruoyi.system.model.rental.PlatformMemberRiskScope;

/** 平台商户信用与黑名单数据访问。 */
public interface PlatformMemberRiskMapper
{
    PlatformMemberRiskScope selectMemberScope(@Param("memberId") Long memberId);

    int insertCreditAccountIfAbsent(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("operatorId") Long operatorId);

    Map<String, Object> selectCreditAccountForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId);

    int updateCreditAccount(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("score") Integer score, @Param("level") Integer level, @Param("operatorId") Long operatorId);

    int insertCreditRecord(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("recordNo") String recordNo, @Param("eventType") Integer eventType,
            @Param("scoreBefore") Integer scoreBefore, @Param("scoreChange") Integer scoreChange,
            @Param("scoreAfter") Integer scoreAfter, @Param("reason") String reason,
            @Param("sourceNo") String sourceNo, @Param("reversalOfRecordNo") String reversalOfRecordNo,
            @Param("operatorId") Long operatorId);

    Map<String, Object> selectCreditRecordForUpdate(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("recordNo") String recordNo);

    int countCreditReversal(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("recordNo") String recordNo);

    List<PlatformMemberCreditRecordVO> selectCreditRecords(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId);

    List<PlatformMemberBlacklistRecordVO> selectBlacklistRecords(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId);

    int countActiveBlacklist(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId);

    int insertBlacklistRecord(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("recordNo") String recordNo, @Param("reasonType") Integer reasonType,
            @Param("reason") String reason, @Param("expiresTime") String expiresTime,
            @Param("operatorId") Long operatorId);

    Map<String, Object> selectBlacklistForUpdate(@Param("tenantId") Long tenantId, @Param("memberId") Long memberId,
            @Param("recordNo") String recordNo);

    int releaseBlacklist(@Param("recordId") Long recordId, @Param("remark") String remark,
            @Param("operatorId") Long operatorId);
}
