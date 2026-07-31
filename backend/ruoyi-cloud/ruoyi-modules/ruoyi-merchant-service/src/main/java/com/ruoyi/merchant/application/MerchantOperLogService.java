package com.ruoyi.merchant.application;

import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.merchant.domain.MerchantOperLogRecord;
import com.ruoyi.merchant.mapper.MerchantOperLogMapper;
import com.ruoyi.merchant.model.MerchantOperLogQuery;
import com.ruoyi.merchant.model.MerchantOperLogVO;
import com.ruoyi.system.api.domain.SysOperLog;

/** 商户操作日志应用服务。 */
@Service
public class MerchantOperLogService
{
    private final MerchantOperLogMapper operLogMapper;

    public MerchantOperLogService(MerchantOperLogMapper operLogMapper)
    {
        this.operLogMapper = operLogMapper;
    }

    /**
     * 把通用审计对象转换为商户日志记录；匿名注册请求允许归属字段为空但仍必须落库追踪。
     *
     * @param source 通用操作审计对象
     * @return 受影响行数
     */
    public int record(SysOperLog source)
    {
        if (source == null)
        {
            return 0;
        }
        MerchantOperLogRecord record = new MerchantOperLogRecord();
        record.setTenantId(source.getTenantId());
        record.setMerchantId(source.getMerchantId());
        record.setOperatorUserId(source.getOperatorUserId());
        record.setTitle(source.getTitle());
        record.setBusinessType(source.getBusinessType());
        record.setMethod(source.getMethod());
        record.setRequestMethod(source.getRequestMethod());
        record.setOperatorType(source.getOperatorType());
        record.setOperName(source.getOperName());
        record.setOperUrl(source.getOperUrl());
        record.setOperIp(source.getOperIp());
        record.setOperParam(source.getOperParam());
        record.setJsonResult(source.getJsonResult());
        record.setStatus(source.getStatus());
        record.setErrorMsg(source.getErrorMsg());
        record.setCostTime(source.getCostTime());
        record.setTraceId(source.getTraceId());
        record.setHttpStatus(source.getHttpStatus());
        record.setIsdeleted(0);
        return operLogMapper.insert(record);
    }

    /**
     * 查询当前登录商户的未删除操作日志，租户和商户归属只从登录上下文读取。
     *
     * @param query 日志筛选条件
     * @return 当前商户操作日志集合
     */
    public List<MerchantOperLogVO> list(MerchantOperLogQuery query)
    {
        return operLogMapper.selectList(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), query);
    }
}
