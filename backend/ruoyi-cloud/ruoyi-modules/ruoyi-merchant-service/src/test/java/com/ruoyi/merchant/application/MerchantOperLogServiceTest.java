package com.ruoyi.merchant.application;

import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.merchant.domain.MerchantOperLogRecord;
import com.ruoyi.merchant.mapper.MerchantOperLogMapper;
import com.ruoyi.merchant.model.MerchantOperLogQuery;
import com.ruoyi.merchant.model.MerchantOperLogVO;
import com.ruoyi.system.api.domain.SysOperLog;
import com.ruoyi.system.api.model.LoginUser;

public class MerchantOperLogServiceTest
{
    /** 清理线程级登录上下文，避免不同测试之间相互污染。 */
    @After
    public void clearContext()
    {
        SecurityContextHolder.remove();
    }

    /** 验证商户日志查询的租户和商户范围只能取自可信登录上下文。 */
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

    /** 验证已登录商户的审计日志会完整保留可信归属和操作人信息。 */
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

    /** 验证注册等匿名接口仍会落库审计，同时不伪造租户和商户归属。 */
    @Test
    public void recordsAnonymousOperationWithoutMerchantContext()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOperLogService service = new MerchantOperLogService(mapper);
        SysOperLog source = new SysOperLog();
        source.setClientType(Constants.CLIENT_TYPE_MERCHANT);

        service.record(source);

        require(mapper.record != null, "anonymous operation must be persisted");
        require(mapper.record.getTenantId() == null, "anonymous operation must not fabricate tenant scope");
        require(mapper.record.getMerchantId() == null, "anonymous operation must not fabricate merchant scope");
    }

    /**
     * 构造带有可信商户归属的登录用户对象。
     *
     * @param tenantId 租户主键
     * @param merchantId 商户主键
     * @return 商户登录用户对象
     */
    private LoginUser merchantLoginUser(Long tenantId, Long merchantId)
    {
        LoginUser loginUser = new LoginUser();
        loginUser.setClientType(Constants.CLIENT_TYPE_MERCHANT);
        loginUser.setTenantId(tenantId);
        loginUser.setMemberId(merchantId);
        loginUser.setWarehouseScopeType(Constants.WAREHOUSE_SCOPE_ALL);
        return loginUser;
    }

    /**
     * 执行无第三方断言库依赖的测试断言。
     *
     * @param condition 期望成立的条件
     * @param message 断言失败提示
     */
    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }

    /** 用于捕获日志读写参数的内存 Mapper 测试替身。 */
    private static class CapturingMapper implements MerchantOperLogMapper
    {
        private Long tenantId;
        private Long merchantId;
        private MerchantOperLogRecord record;

        /**
         * 捕获待写入的商户操作日志。
         *
         * @param record 待写入日志记录
         * @return 模拟受影响行数
         */
        @Override
        public int insert(MerchantOperLogRecord record)
        {
            this.record = record;
            return 1;
        }

        /**
         * 捕获日志查询使用的租户和商户归属。
         *
         * @param tenantId 租户主键
         * @param merchantId 商户主键
         * @param query 日志查询条件
         * @return 空日志集合
         */
        @Override
        public List<MerchantOperLogVO> selectList(Long tenantId, Long merchantId, MerchantOperLogQuery query)
        {
            this.tenantId = tenantId;
            this.merchantId = merchantId;
            return Collections.emptyList();
        }
    }
}
