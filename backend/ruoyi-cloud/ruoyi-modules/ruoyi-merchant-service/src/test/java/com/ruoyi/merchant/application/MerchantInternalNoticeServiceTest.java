package com.ruoyi.merchant.application;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.ObjectProvider;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.log.service.MerchantBusinessAuditRecorder;
import com.ruoyi.merchant.api.RemoteMerchantBusinessAuditService;
import com.ruoyi.merchant.mapper.MerchantInternalNoticeMapper;
import com.ruoyi.merchant.model.MerchantInternalNoticeCommand;
import com.ruoyi.merchant.model.MerchantInternalNoticeStatus;
import com.ruoyi.merchant.model.MerchantInternalNoticeVO;
import com.ruoyi.system.api.model.LoginUser;

/** 商户内部通知的商户隔离和状态流转测试。 */
public class MerchantInternalNoticeServiceTest
{
    @After
    public void clearContext()
    {
        SecurityContextHolder.remove();
    }

    /** 新增通知只能生成草稿，发布后返回稳定的中文状态。 */
    @Test
    public void createAndPublish_usesMerchantScopeAndChineseStatus()
    {
        setMerchantContext();
        AtomicReference<Integer> status = new AtomicReference<>(MerchantInternalNoticeStatus.DRAFT.getCode());
        MerchantInternalNoticeMapper mapper = mapper((method, args) -> {
            if ("insertNotice".equals(method)) return 1;
            if ("selectLastInsertId".equals(method)) return 12L;
            if ("deleteTargetRoles".equals(method) || "insertTargetRoles".equals(method)) return 1;
            if ("selectTargetRoleIds".equals(method)) return List.of(6L);
            if ("selectManagementById".equals(method)) return notice(status.get());
            if ("countValidRoles".equals(method)) return 1;
            if ("updateStatus".equals(method)) {
                status.set((Integer) args[3]);
                return 1;
            }
            throw new UnsupportedOperationException(method);
        });

        MerchantInternalNoticeService service = new MerchantInternalNoticeService(mapper, auditRecorder());
        MerchantInternalNoticeCommand command = new MerchantInternalNoticeCommand();
        command.setNoticeTitle("周末仓库盘点安排");
        command.setNoticeContent("请各员工组在周五前完成盘点准备。");
        command.setTargetScope(2);
        command.setRoleIds(new Long[] { 6L });

        MerchantInternalNoticeVO created = service.create(command);
        Assert.assertEquals("草稿", created.getStatusName());
        MerchantInternalNoticeVO published = service.publish(12L);
        Assert.assertEquals(Integer.valueOf(MerchantInternalNoticeStatus.PUBLISHED.getCode()), published.getStatus());
        Assert.assertEquals("已发布", published.getStatusName());
    }

    private MerchantInternalNoticeVO notice(Integer status)
    {
        MerchantInternalNoticeVO notice = new MerchantInternalNoticeVO();
        notice.setInternalNoticeId(12L);
        notice.setNoticeTitle("周末仓库盘点安排");
        notice.setNoticeType(1);
        notice.setNoticeContent("请各员工组在周五前完成盘点准备。");
        notice.setTargetScope(2);
        notice.setStatus(status);
        notice.setRoleIds(new Long[] { 6L });
        notice.setStatusName(MerchantInternalNoticeStatus.labelOf(status));
        return notice;
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
        RemoteMerchantBusinessAuditService remote = (RemoteMerchantBusinessAuditService) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[] { RemoteMerchantBusinessAuditService.class },
                (proxy, method, args) -> R.ok(Boolean.TRUE));
        @SuppressWarnings("unchecked")
        ObjectProvider<RemoteMerchantBusinessAuditService> provider =
                (ObjectProvider<RemoteMerchantBusinessAuditService>) Proxy.newProxyInstance(
                        getClass().getClassLoader(), new Class<?>[] { ObjectProvider.class },
                        (proxy, method, args) -> "getIfAvailable".equals(method.getName()) ? remote : null);
        return new MerchantBusinessAuditRecorder(provider);
    }

    private MerchantInternalNoticeMapper mapper(MapperCall mapperCall)
    {
        return (MerchantInternalNoticeMapper) Proxy.newProxyInstance(
                MerchantInternalNoticeMapper.class.getClassLoader(), new Class<?>[] { MerchantInternalNoticeMapper.class },
                (proxy, method, args) -> mapperCall.invoke(method.getName(), args));
    }

    @FunctionalInterface
    private interface MapperCall
    {
        Object invoke(String method, Object[] args);
    }
}
