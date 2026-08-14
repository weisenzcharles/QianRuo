package com.ruoyi.merchant.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.merchant.domain.MerchantBusinessAuditRecord;
import com.ruoyi.merchant.model.MerchantBusinessAuditQuery;
import com.ruoyi.merchant.model.MerchantBusinessAuditVO;

/** 商户业务审计日志数据访问接口。 */
public interface MerchantBusinessAuditMapper
{
    /** 写入一条业务审计记录。 */
    int insert(@Param("record") MerchantBusinessAuditRecord record);

    /** 查询当前商户的业务审计摘要列表。 */
    List<MerchantBusinessAuditVO> selectList(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("query") MerchantBusinessAuditQuery query);

    /** 查询当前商户的一条业务审计详情。 */
    MerchantBusinessAuditVO selectById(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId, @Param("auditId") Long auditId);

    /** 查询当前商户日志中已经出现的业务模块名称。 */
    List<String> selectBusinessModules(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId);

    /** 查询当前商户日志中已经出现的操作人名称。 */
    List<String> selectOperatorNames(@Param("tenantId") Long tenantId,
            @Param("memberId") Long memberId);
}
