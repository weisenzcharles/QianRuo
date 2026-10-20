package com.ruoyi.rentalbusiness.service;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.rentalbusiness.enums.MerchantAfterSalesCaseStatus;
import com.ruoyi.rentalbusiness.mapper.MerchantAfterSalesMapper;
import com.ruoyi.rentalbusiness.model.MerchantAfterSalesCaseVO;
import com.ruoyi.system.api.RemotePlatformGovernanceService;
import com.ruoyi.system.api.model.LoginUser;
import com.ruoyi.system.api.model.PlatformAfterSalesTicketCommand;
import com.ruoyi.system.api.model.PlatformAfterSalesTicketView;

/** 售后申请平台介入的权限、状态和幂等关联测试。 */
public class MerchantAfterSalesEscalationServiceTest
{
    @After
    public void clearContext()
    {
        SecurityContextHolder.remove();
    }

    @Test
    public void escalate_processingCase_savesTicketReference()
    {
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        MerchantAfterSalesCaseVO row = caseRow(MerchantAfterSalesCaseStatus.PROCESSING.getCode());
        AtomicBoolean updated = new AtomicBoolean(false);
        AtomicReference<PlatformAfterSalesTicketCommand> command = new AtomicReference<>();
        MerchantAfterSalesMapper mapper = mapper(row, updated);
        RemotePlatformGovernanceService remote = (RemotePlatformGovernanceService) Proxy.newProxyInstance(
                RemotePlatformGovernanceService.class.getClassLoader(),
                new Class<?>[] { RemotePlatformGovernanceService.class },
                (proxy, method, args) -> {
                    if ("createAfterSalesTicket".equals(method.getName()))
                    {
                        command.set((PlatformAfterSalesTicketCommand) args[0]);
                        PlatformAfterSalesTicketView result = new PlatformAfterSalesTicketView();
                        result.setTicketNo("AS-TKT-88");
                        result.setTicketStatusName("待分配");
                        return R.ok(result);
                    }
                    return R.fail("测试替身未实现");
                });

        PlatformAfterSalesTicketView result = new MerchantAfterSalesEscalationService(mapper, remote)
                .escalate("AS0000000088");

        Assert.assertEquals("AS-TKT-88", result.getTicketNo());
        Assert.assertTrue(updated.get());
        Assert.assertEquals(Long.valueOf(88L), command.get().getCaseId());
        Assert.assertEquals("OD0000000088", command.get().getOrderNo());
    }

    @Test
    public void escalate_completedCaseWithoutTicket_isRejected()
    {
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, merchantLoginUser());
        MerchantAfterSalesMapper mapper = mapper(caseRow(MerchantAfterSalesCaseStatus.COMPLETED.getCode()),
                new AtomicBoolean(false));
        AtomicBoolean called = new AtomicBoolean(false);
        RemotePlatformGovernanceService remote = (RemotePlatformGovernanceService) Proxy.newProxyInstance(
                RemotePlatformGovernanceService.class.getClassLoader(),
                new Class<?>[] { RemotePlatformGovernanceService.class },
                (proxy, method, args) -> {
                    called.set(true);
                    return R.fail("不应调用平台工单");
                });

        try
        {
            new MerchantAfterSalesEscalationService(mapper, remote).escalate("AS0000000088");
            Assert.fail("已完成售后单不应首次申请平台介入");
        }
        catch (com.ruoyi.common.core.exception.ServiceException expected)
        {
            Assert.assertEquals("已完成或已关闭的售后单不能申请平台介入", expected.getMessage());
        }
        Assert.assertFalse(called.get());
    }

    private MerchantAfterSalesMapper mapper(MerchantAfterSalesCaseVO row, AtomicBoolean updated)
    {
        return (MerchantAfterSalesMapper) Proxy.newProxyInstance(MerchantAfterSalesMapper.class.getClassLoader(),
                new Class<?>[] { MerchantAfterSalesMapper.class },
                (proxy, method, args) -> {
                    if ("selectByCaseNoForUpdate".equals(method.getName()))
                        return row;
                    if ("updatePlatformTicketNo".equals(method.getName()))
                    {
                        updated.set(true);
                        return 1;
                    }
                    return null;
                });
    }

    private MerchantAfterSalesCaseVO caseRow(Integer status)
    {
        MerchantAfterSalesCaseVO row = new MerchantAfterSalesCaseVO();
        row.caseId = 88L;
        row.orderId = 7001L;
        row.caseNo = "AS0000000088";
        row.orderNo = "OD0000000088";
        row.caseStatus = status;
        row.caseTypeLabel = "损坏";
        row.problemDesc = "设备外观损坏";
        return row;
    }

    private LoginUser merchantLoginUser()
    {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserid(9001L);
        loginUser.setTenantId(10001L);
        loginUser.setMemberId(27L);
        loginUser.setClientType(Constants.CLIENT_TYPE_MERCHANT);
        loginUser.setMerchantAuthStatus(com.ruoyi.common.core.enums.RentalCode.AuthenticationStatus.VERIFIED.getCode());
        loginUser.setWarehouseScopeType(com.ruoyi.common.core.enums.RentalCode.WarehouseScope.ALL.getCode());
        loginUser.setWarehouseIds(Collections.emptySet());
        return loginUser;
    }
}
