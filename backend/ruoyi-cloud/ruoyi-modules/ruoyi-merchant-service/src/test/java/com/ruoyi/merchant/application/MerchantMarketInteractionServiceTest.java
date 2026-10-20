package com.ruoyi.merchant.application;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.ObjectProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.log.service.MerchantBusinessAuditRecorder;
import com.ruoyi.merchant.mapper.MerchantMarketPostMapper;
import com.ruoyi.merchant.model.MerchantMarketBoardType;
import com.ruoyi.merchant.model.MerchantMarketMessageCommand;
import com.ruoyi.merchant.model.MerchantMarketPostRecord;
import com.ruoyi.merchant.model.MerchantMarketReportCommand;
import com.ruoyi.merchant.model.MerchantMarketReportRecord;
import com.ruoyi.merchant.model.MerchantMarketReportType;
import com.ruoyi.system.api.model.LoginUser;

/** 商户周转市场公开互动和越权边界测试。 */
public class MerchantMarketInteractionServiceTest
{
    @After
    public void clearContext()
    {
        SecurityContextHolder.remove();
    }

    /** 发送私信时必须由服务端根据公开帖子解析接收商户，并返回中文板块状态。 */
    @Test
    public void sendMessage_publicPost_usesPostOwnerAndMapsChineseLabels()
    {
        setMerchantContext();
        MerchantMarketPostRecord post = post(88L);
        AtomicReference<Object[]> insertArguments = new AtomicReference<>();
        MerchantMarketPostMapper mapper = mapper((method, args) -> {
            if ("selectPublicByPostNo".equals(method)) return post;
            if ("insertMessage".equals(method)) {
                insertArguments.set(args);
                return 1;
            }
            if ("selectPublicList".equals(method)) return List.of(post);
            throw new UnsupportedOperationException(method);
        });
        MerchantMarketInteractionService service = new MerchantMarketInteractionService(mapper, auditRecorder(),
                new ObjectMapper());

        MerchantMarketMessageCommand command = new MerchantMarketMessageCommand();
        command.setPostNo(post.getPostNo());
        command.setContent("请确认设备可自提");
        service.sendMessage(command);

        Assert.assertEquals(Long.valueOf(10001L), insertArguments.get()[0]);
        Assert.assertEquals(Long.valueOf(12L), insertArguments.get()[1]);
        Assert.assertEquals(Long.valueOf(27L), insertArguments.get()[2]);
        Assert.assertEquals(Long.valueOf(88L), insertArguments.get()[3]);
        Assert.assertEquals("请确认设备可自提", insertArguments.get()[4]);
        Assert.assertEquals("佳能", service.publicPosts(MerchantMarketBoardType.SALE, null, null).get(0).getBrand());
        Assert.assertEquals("出售", service.publicPosts(MerchantMarketBoardType.SALE, null, null).get(0).getBoardTypeName());
    }

    /** 公开帖子不能被发布者自己私信，避免绕过商户间沟通边界。 */
    @Test
    public void sendMessage_ownPost_rejectsOperation()
    {
        setMerchantContext();
        MerchantMarketPostRecord post = post(27L);
        MerchantMarketPostMapper mapper = mapper((method, args) -> {
            if ("selectPublicByPostNo".equals(method)) return post;
            throw new UnsupportedOperationException(method);
        });
        MerchantMarketMessageCommand command = new MerchantMarketMessageCommand();
        command.setPostNo(post.getPostNo());
        command.setContent("不应发送");
        try
        {
            new MerchantMarketInteractionService(mapper, auditRecorder(), new ObjectMapper()).sendMessage(command);
            Assert.fail("own post must reject message");
        }
        catch (ServiceException exception)
        {
            Assert.assertEquals("不能给自己的市场帖子发送私信", exception.getMessage());
        }
    }

    /** 同一帖子和举报类型存在待处理记录时，必须拒绝重复举报。 */
    @Test
    public void submitReport_pendingSameType_rejectsDuplicate()
    {
        setMerchantContext();
        MerchantMarketPostRecord post = post(88L);
        MerchantMarketPostMapper mapper = mapper((method, args) -> {
            if ("selectPublicByPostNo".equals(method)) return post;
            if ("selectPendingReport".equals(method)) return new MerchantMarketReportRecord();
            throw new UnsupportedOperationException(method);
        });
        MerchantMarketReportCommand command = new MerchantMarketReportCommand();
        command.setPostNo(post.getPostNo());
        command.setReportType(MerchantMarketReportType.FAKE_INVENTORY);
        command.setEvidence("库存数量与实际不符");
        try
        {
            new MerchantMarketInteractionService(mapper, auditRecorder(), new ObjectMapper()).submitReport(command);
            Assert.fail("duplicate pending report must reject");
        }
        catch (ServiceException exception)
        {
            Assert.assertEquals("相同类型的举报正在处理中，请勿重复提交", exception.getMessage());
        }
    }

    private MerchantMarketPostRecord post(Long memberId)
    {
        MerchantMarketPostRecord post = new MerchantMarketPostRecord();
        post.setPostId(12L);
        post.setMemberId(memberId);
        post.setPostNo("MKT-TEST-INTERACTION");
        post.setPublisherName("杭州商户");
        post.setBoardType(MerchantMarketBoardType.SALE.getCode());
        post.setBrand("佳能");
        post.setModelName("EOS R50");
        post.setQuantity(2);
        post.setCity("杭州");
        post.setPostStatus(1);
        return post;
    }

    private void setMerchantContext()
    {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserid(9001L);
        loginUser.setClientType(Constants.CLIENT_TYPE_MERCHANT);
        loginUser.setTenantId(10001L);
        loginUser.setMemberId(27L);
        loginUser.setMerchantAuthStatus(RentalCode.AuthenticationStatus.VERIFIED.getCode());
        loginUser.setWarehouseScopeType(RentalCode.WarehouseScope.ALL.getCode());
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);
        SecurityContextHolder.setUserId(String.valueOf(loginUser.getUserid()));
    }

    private MerchantBusinessAuditRecorder auditRecorder()
    {
        Object remote = Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] { com.ruoyi.merchant.api.RemoteMerchantBusinessAuditService.class },
                (proxy, method, args) -> R.ok(Boolean.TRUE));
        @SuppressWarnings("unchecked")
        ObjectProvider<com.ruoyi.merchant.api.RemoteMerchantBusinessAuditService> provider =
                (ObjectProvider<com.ruoyi.merchant.api.RemoteMerchantBusinessAuditService>) Proxy.newProxyInstance(
                        getClass().getClassLoader(), new Class<?>[] { ObjectProvider.class },
                        (proxy, method, args) -> "getIfAvailable".equals(method.getName()) ? remote : null);
        return new MerchantBusinessAuditRecorder(provider);
    }

    private MerchantMarketPostMapper mapper(MapperCall mapperCall)
    {
        return (MerchantMarketPostMapper) Proxy.newProxyInstance(MerchantMarketPostMapper.class.getClassLoader(),
                new Class<?>[] { MerchantMarketPostMapper.class },
                (proxy, method, args) -> mapperCall.invoke(method.getName(), args));
    }

    @FunctionalInterface
    private interface MapperCall
    {
        Object invoke(String method, Object[] args);
    }
}
