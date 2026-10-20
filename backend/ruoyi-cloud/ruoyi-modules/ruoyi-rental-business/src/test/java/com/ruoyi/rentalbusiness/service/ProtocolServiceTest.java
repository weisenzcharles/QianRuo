package com.ruoyi.rentalbusiness.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.lang.reflect.Proxy;
import org.junit.After;
import org.junit.Test;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.rentalbusiness.mapper.RentalWorkflowMapper;
import com.ruoyi.rentalbusiness.model.ProtocolAcceptanceCommand;
import com.ruoyi.rentalbusiness.model.ProtocolVO;
import com.ruoyi.system.api.model.LoginUser;

/** 协议入口筛选和定时生效规则测试。 */
public class ProtocolServiceTest
{
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @After
    public void clearContext()
    {
        SecurityContextHolder.remove();
    }

    /** 商户协议查询必须把入口编码传给数据层，并返回中文入口名称和确认状态。 */
    @Test
    public void listMerchant_requestedScope_marksAcceptedProtocol()
    {
        setMerchantContext();
        AtomicReference<Integer> requestedScope = new AtomicReference<Integer>();
        RentalWorkflowMapper mapper = mapper((method, args) -> {
            if ("selectProtocols".equals(method))
            {
                requestedScope.set((Integer) args[2]);
                return List.of(Map.of("protocolId", 11L, "protocolKey", "visitor_terms", "versionNo", 2,
                        "protocolTitle", "访客服务协议", "protocolStatus", 1, "effectiveTime",
                        LocalDateTime.now().format(DATE_TIME_FORMATTER), "scopeTypes", "2"));
            }
            if ("selectAcceptance".equals(method))
            {
                return Map.of("acceptanceId", 99L);
            }
            return null;
        });

        List<ProtocolVO> result = new ProtocolService(mapper).listMerchant("visitor_terms", 2);

        require(Integer.valueOf(2).equals(requestedScope.get()), "merchant query must pass requested scope");
        require(result.size() == 1 && Boolean.TRUE.equals(result.get(0).accepted),
                "accepted state must be resolved for the current user");
        require(result.get(0).scopeLabels.size() == 1 && "访客网页端".equals(result.get(0).scopeLabels.get(0)),
                "scope code must be converted to a Chinese label");
    }

    /** 定时生效协议在生效前不能被绕过确认，避免直接调用确认接口提前生效。 */
    @Test
    public void accept_beforeEffectiveTime_rejectsWithoutInsert()
    {
        setMerchantContext();
        AtomicInteger insertCount = new AtomicInteger();
        RentalWorkflowMapper mapper = mapper((method, args) -> {
            if ("selectProtocol".equals(method))
            {
                return Map.of("protocolId", 12L, "protocolKey", "merchant_terms", "versionNo", 3,
                        "protocolTitle", "商户服务协议", "protocolStatus", 1, "contentHash", "hash",
                        "effectiveTime", LocalDateTime.now().plusHours(1).format(DATE_TIME_FORMATTER));
            }
            if ("insertAcceptance".equals(method))
            {
                insertCount.incrementAndGet();
                return 1;
            }
            return null;
        });
        ProtocolAcceptanceCommand command = new ProtocolAcceptanceCommand();
        command.protocolId = 12L;

        try
        {
            new ProtocolService(mapper).accept(command);
            throw new AssertionError("future protocol must not be accepted");
        }
        catch (ServiceException exception)
        {
            require(exception.getMessage().contains("尚未到生效时间"),
                    "future protocol rejection must explain the effective time rule");
        }
        require(insertCount.get() == 0, "future protocol rejection must not write an acceptance record");
    }

    private void setMerchantContext()
    {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserid(9001L);
        loginUser.setClientType(Constants.CLIENT_TYPE_MERCHANT);
        loginUser.setTenantId(10001L);
        loginUser.setMemberId(27L);
        loginUser.setWarehouseScopeType(1);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);
        SecurityContextHolder.setUserId("9001");
    }

    private RentalWorkflowMapper mapper(MapperCall mapperCall)
    {
        return (RentalWorkflowMapper) Proxy.newProxyInstance(RentalWorkflowMapper.class.getClassLoader(),
                new Class<?>[] {RentalWorkflowMapper.class},
                (proxy, method, args) -> mapperCall.invoke(method.getName(), args));
    }

    private void require(boolean condition, String message)
    {
        if (!condition)
        {
            throw new AssertionError(message);
        }
    }

    @FunctionalInterface
    private interface MapperCall
    {
        Object invoke(String method, Object[] args);
    }
}
