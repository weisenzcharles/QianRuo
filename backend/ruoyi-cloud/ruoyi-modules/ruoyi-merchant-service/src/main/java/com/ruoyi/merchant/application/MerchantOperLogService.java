package com.ruoyi.merchant.application;

import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.exception.ServiceException;
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

    public int record(SysOperLog source)
    {
        if (source == null || !Constants.CLIENT_TYPE_MERCHANT.equals(source.getClientType())
                || source.getTenantId() == null || source.getMerchantId() == null
                || source.getOperatorUserId() == null)
        {
            throw new ServiceException("商户操作日志缺少可信登录上下文");
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
        return operLogMapper.insert(record);
    }

    public List<MerchantOperLogVO> list(MerchantOperLogQuery query)
    {
        return operLogMapper.selectList(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), query);
    }
}
