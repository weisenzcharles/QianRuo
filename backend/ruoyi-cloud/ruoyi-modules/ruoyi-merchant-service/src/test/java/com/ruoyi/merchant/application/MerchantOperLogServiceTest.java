package com.ruoyi.merchant.application;

import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.merchant.domain.MerchantOperLogRecord;
import com.ruoyi.merchant.mapper.MerchantOperLogMapper;
import com.ruoyi.merchant.model.MerchantOperLogQuery;
import com.ruoyi.merchant.model.MerchantOperLogVO;
import com.ruoyi.system.api.domain.SysOperLog;
import com.ruoyi.system.api.model.LoginUser;

public class MerchantOperLogServiceTest
{
    @After
    public void clearContext()
    {
        SecurityContextHolder.remove();
    }

    @Test
    public void listUsesAuthenticatedMerchantScope()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOperLogService service = new MerchantOperLogService(mapper);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser(10001L, 27L));

        service.list(new MerchantOperLogQuery());

        require(Long.valueOf(10001L).equals(mapper.tenantId), "tenant must come from login context");
        require(Long.valueOf(27L).equals(mapper.merchantId), "merchant must come from login context");
    }

    @Test
    public void recordCopiesTrustedMerchantScope()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOperLogService service = new MerchantOperLogService(mapper);
        SysOperLog source = new SysOperLog();
        source.setClientType(Constants.CLIENT_TYPE_MERCHANT);
        source.setTenantId(10001L);
        source.setMerchantId(27L);
        source.setOperatorUserId(88L);
        source.setOperName("merchant-user");

        service.record(source);

        require(Long.valueOf(10001L).equals(mapper.record.getTenantId()), "record tenant must be retained");
        require(Long.valueOf(27L).equals(mapper.record.getMerchantId()), "record merchant must be retained");
        require(Long.valueOf(88L).equals(mapper.record.getOperatorUserId()), "record user must be retained");
    }

    @Test(expected = ServiceException.class)
    public void rejectsOperationLogWithoutMerchantContext()
    {
        MerchantOperLogService service = new MerchantOperLogService(new CapturingMapper());
        SysOperLog source = new SysOperLog();
        source.setClientType(Constants.CLIENT_TYPE_PLATFORM);
        service.record(source);
    }

    private LoginUser merchantLoginUser(Long tenantId, Long merchantId)
    {
        LoginUser loginUser = new LoginUser();
        loginUser.setClientType(Constants.CLIENT_TYPE_MERCHANT);
        loginUser.setTenantId(tenantId);
        loginUser.setMemberId(merchantId);
        loginUser.setWarehouseScopeType(Constants.WAREHOUSE_SCOPE_ALL);
        return loginUser;
    }

    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }

    private static class CapturingMapper implements MerchantOperLogMapper
    {
        private Long tenantId;
        private Long merchantId;
        private MerchantOperLogRecord record;

        @Override
        public int insert(MerchantOperLogRecord record)
        {
            this.record = record;
            return 1;
        }

        @Override
        public List<MerchantOperLogVO> selectList(Long tenantId, Long merchantId, MerchantOperLogQuery query)
        {
            this.tenantId = tenantId;
            this.merchantId = merchantId;
            return Collections.emptyList();
        }
    }
}
