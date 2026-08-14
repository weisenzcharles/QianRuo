package com.ruoyi.rentalbusiness.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Test;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.rentalbusiness.domain.MerchantOrderRow;
import com.ruoyi.rentalbusiness.mapper.MerchantOrderMapper;
import com.ruoyi.rentalbusiness.model.MerchantOrderCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderQuery;
import com.ruoyi.system.api.model.LoginUser;

/** 商户订单应用服务的租户、商户和仓库范围测试。 */
public class MerchantOrderServiceTest
{
    /** 清理线程级登录上下文，避免测试之间复用商户身份。 */
    @After
    public void clearContext()
    {
        SecurityContextHolder.remove();
    }

    /** 验证客户端提交的仓库范围会被可信登录上下文覆盖。 */
    @Test
    public void listUsesAuthenticatedMerchantAndWarehouseScope()
    {
        CapturingMapper mapper = new CapturingMapper();
        MerchantOrderService service = new MerchantOrderService(mapper);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());

        MerchantOrderQuery query = new MerchantOrderQuery();
        query.setWarehouseIds(Collections.singleton(999L));
        query.setAllWarehouseAccess(true);

        service.list(query);

        require(Long.valueOf(10001L).equals(mapper.tenantId), "tenant must come from login context");
        require(Long.valueOf(27L).equals(mapper.merchantId), "merchant must come from login context");
        require(!mapper.query.isAllWarehouseAccess(), "client must not expand warehouse scope");
        require(mapper.query.getWarehouseIds().equals(Collections.singleton(101L)),
                "warehouse ids must come from login context");
    }

    /** 构造带有指定仓库权限的商户登录上下文。 */
    private LoginUser merchantLoginUser()
    {
        LoginUser loginUser = new LoginUser();
        loginUser.setClientType(Constants.CLIENT_TYPE_MERCHANT);
        loginUser.setTenantId(10001L);
        loginUser.setMemberId(27L);
        loginUser.setWarehouseScopeType(Constants.WAREHOUSE_SCOPE_SPECIFIED);
        loginUser.setWarehouseIds(Collections.singleton(101L));
        return loginUser;
    }

    /** 执行无第三方断言库依赖的测试断言。 */
    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }

    /** 捕获订单查询参数的 Mapper 测试替身。 */
    private static class CapturingMapper implements MerchantOrderMapper
    {
        private Long tenantId;
        private Long merchantId;
        private MerchantOrderQuery query;

        /** 捕获订单详情查询参数，测试替身不访问真实数据库。 */
        @Override
        public MerchantOrderRow selectMerchantOrderById(Long tenantId, Long merchantId, Long orderId)
        {
            return null;
        }

        /** 模拟当前商户可以使用指定订单渠道。 */
        @Override
        public int countUsableOrderChannel(Long tenantId, Long merchantId, Long channelId)
        {
            return 1;
        }

        /** 模拟订单主表新增，测试查询范围时不执行写入。 */
        @Override
        public int insertMerchantOrder(Long tenantId, Long merchantId, MerchantOrderCreateCommand command,
                Long operatorId)
        {
            return 1;
        }

        /** 模拟订单主表更新，测试查询范围时不执行写入。 */
        @Override
        public int updateMerchantOrder(Long tenantId, Long merchantId, MerchantOrderCreateCommand command,
                Long operatorId)
        {
            return 1;
        }

        /** 模拟订单明细新增，测试查询范围时不执行写入。 */
        @Override
        public int insertMerchantOrderItem(Long tenantId, Long merchantId, MerchantOrderCreateCommand command,
                Long operatorId)
        {
            return 1;
        }

        /** 捕获应用服务传递给数据库层的可信范围和筛选条件。 */
        @Override
        public List<MerchantOrderRow> selectMerchantOrderList(Long tenantId, Long merchantId,
                MerchantOrderQuery query)
        {
            this.tenantId = tenantId;
            this.merchantId = merchantId;
            this.query = query;
            return Collections.emptyList();
        }
    }
}
