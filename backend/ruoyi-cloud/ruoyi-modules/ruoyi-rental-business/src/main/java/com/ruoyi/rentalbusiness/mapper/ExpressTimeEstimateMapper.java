package com.ruoyi.rentalbusiness.mapper;

import org.apache.ibatis.annotations.Param;
import com.ruoyi.rentalbusiness.domain.ExpressTimeEstimateRecord;

/** 快递时效预估数据库访问接口。 */
public interface ExpressTimeEstimateMapper
{
    /** 查询指定线路、公司和供应商下仍未过期的最新时效。 */
    ExpressTimeEstimateRecord selectValid(@Param("fromAcode") String fromAcode,
            @Param("toAcode") String toAcode, @Param("expressCompanyCode") String expressCompanyCode,
            @Param("expressType") String expressType, @Param("providerCode") String providerCode);

    /** 按线路唯一键插入或更新时效预估结果。 */
    int upsert(@Param("record") ExpressTimeEstimateRecord record);
}
