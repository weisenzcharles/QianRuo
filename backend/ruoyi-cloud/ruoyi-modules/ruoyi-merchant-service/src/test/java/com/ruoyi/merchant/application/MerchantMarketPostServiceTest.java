package com.ruoyi.merchant.application;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.ObjectProvider;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.log.service.MerchantBusinessAuditRecorder;
import com.ruoyi.merchant.api.RemoteRentalBusinessService;
import com.ruoyi.merchant.api.model.MerchantMarketInventoryUnitView;
import com.ruoyi.merchant.mapper.MerchantMarketDepositMapper;
import com.ruoyi.merchant.mapper.MerchantMarketPostMapper;
import com.ruoyi.merchant.model.MerchantMarketBoardType;
import com.ruoyi.merchant.model.MerchantMarketConditionLevel;
import com.ruoyi.merchant.model.MerchantMarketPostCommand;
import com.ruoyi.merchant.model.MerchantMarketPostRecord;
import com.ruoyi.merchant.model.MerchantMarketPostStatus;
import com.ruoyi.merchant.model.MerchantMarketPostVO;
import com.ruoyi.merchant.model.MerchantMarketPriceRange;
import com.ruoyi.system.api.RemoteBrandModelService;
import com.ruoyi.system.api.model.BrandModel;
import com.ruoyi.system.api.model.LoginUser;

/** 商户周转市场帖子状态机测试。 */
public class MerchantMarketPostServiceTest
{
    @After
    public void clearContext()
    {
        SecurityContextHolder.remove();
    }

    /** 创建草稿和发布必须返回稳定的中文板块、状态标签。 */
    @Test
    public void createAndPublish_returnsChineseLabelsAndUpdatesStatus()
    {
        setMerchantContext();
        MerchantMarketPostRecord record = record(MerchantMarketPostStatus.DRAFT.getCode());
        AtomicReference<Integer> status = new AtomicReference<>(record.getPostStatus());
        MerchantMarketPostMapper mapper = mapper((method, args) -> {
            if ("insert".equals(method)) return 1;
            if ("selectByPostNo".equals(method) || "selectByPostNoForUpdate".equals(method)) {
                record.setPostStatus(status.get());
                return record;
            }
            if ("removeInventoryBindings".equals(method)) return 1;
            if ("insertInventoryBinding".equals(method)) return 1;
            if ("selectInventoryUnitIds".equals(method)) return List.of(1L);
            if ("updateStatus".equals(method)) {
                status.set((Integer) args[3]);
                return 1;
            }
            throw new UnsupportedOperationException(method);
        });

        MerchantMarketPostService service = new MerchantMarketPostService(mapper, auditRecorder(), optionsService(),
                inventoryService(), depositService());
        MerchantMarketPostVO created = service.create(command());
        Assert.assertEquals("出售", created.getBoardTypeName());
        Assert.assertEquals("草稿", created.getPostStatusName());

        MerchantMarketPostVO published = service.publish(record.getPostNo());
        Assert.assertEquals(Integer.valueOf(MerchantMarketPostStatus.PUBLISHED.getCode()), published.getPostStatus());
        Assert.assertEquals("已发布", published.getPostStatusName());
    }

    /** 已关闭帖子不允许再次发布，避免状态逆向流转。 */
    @Test
    public void publish_closedPost_rejectsTransition()
    {
        setMerchantContext();
        MerchantMarketPostRecord record = record(MerchantMarketPostStatus.CLOSED.getCode());
        MerchantMarketPostMapper mapper = mapper((method, args) -> {
            if ("selectByPostNoForUpdate".equals(method)) return record;
            throw new UnsupportedOperationException(method);
        });
        try
        {
            new MerchantMarketPostService(mapper, auditRecorder(), optionsService(), inventoryService(),
                    depositService()).publish(record.getPostNo());
            Assert.fail("closed post must reject publish");
        }
        catch (ServiceException exception)
        {
            Assert.assertEquals("当前帖子状态不允许执行该操作", exception.getMessage());
        }
    }

    private MerchantMarketPostCommand command()
    {
        MerchantMarketPostCommand command = new MerchantMarketPostCommand();
        command.setBoardType(MerchantMarketBoardType.SALE);
        command.setCategory("摄影航拍");
        command.setBrand("佳能");
        command.setModelName("EOS R50");
        command.setConditionLevel(MerchantMarketConditionLevel.NEW.getCode());
        command.setPriceRangeCode(MerchantMarketPriceRange.UNDER_1000.getCode());
        command.setQuantity(1);
        command.setCity("杭州");
        command.setInventoryUnitIds(List.of(1L));
        return command;
    }

    private MerchantMarketInventoryService inventoryService()
    {
        RemoteRentalBusinessService remote = (RemoteRentalBusinessService) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[] { RemoteRentalBusinessService.class },
                (proxy, method, args) -> {
                    if (!"listMarketEligibleUnits".equals(method.getName())) return null;
                    MerchantMarketInventoryUnitView unit = new MerchantMarketInventoryUnitView();
                    unit.setUnitId(1L);
                    unit.setUnitCode("UNIT-1");
                    unit.setSerialNumber("SN-1");
                    unit.setBrand("佳能");
                    unit.setModelName("EOS R50");
                    return R.ok(List.of(unit));
                });
        return new MerchantMarketInventoryService(remote);
    }

    private MerchantMarketDepositService depositService()
    {
        MerchantMarketDepositMapper mapper = (MerchantMarketDepositMapper) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[] { MerchantMarketDepositMapper.class },
                (proxy, method, args) -> {
                    if ("selectLatestDeposit".equals(method.getName())) return null;
                    if ("selectAccountForUpdate".equals(method.getName())) {
                        return Map.of("accountId", 1L, "availableAmount", new BigDecimal("1000.00"),
                                "frozenAmount", BigDecimal.ZERO, "version", 0L, "accountStatus", 1);
                    }
                    if ("updateAccount".equals(method.getName())
                            || "insertFinanceLedger".equals(method.getName())
                            || "insertMarketDepositLedger".equals(method.getName())) return 1;
                    if ("insertAccount".equals(method.getName())) return 1;
                    throw new UnsupportedOperationException(method.getName());
                });
        return new MerchantMarketDepositService(mapper);
    }

    private MerchantMarketOptionsService optionsService()
    {
        RemoteBrandModelService remote = (RemoteBrandModelService) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[] { RemoteBrandModelService.class },
                (proxy, method, args) -> "listActive".equals(method.getName()) ? R.ok(List.of(brandModel())) : null);
        return new MerchantMarketOptionsService(remote);
    }

    private BrandModel brandModel()
    {
        BrandModel model = new BrandModel();
        model.setClassName("摄影航拍");
        model.setBrand("佳能");
        model.setModelName("EOS R50");
        return model;
    }

    private MerchantMarketPostRecord record(Integer status)
    {
        MerchantMarketPostRecord record = new MerchantMarketPostRecord();
        record.setPostNo("MKT-TEST-1");
        record.setBoardType(MerchantMarketBoardType.SALE.getCode());
        record.setBrand("佳能");
        record.setModelName("EOS R50");
        record.setQuantity(1);
        record.setCity("杭州");
        record.setPostStatus(status);
        record.setRecent30dTurnover(0);
        record.setIdleQty(0);
        record.setRentedQty(0);
        record.setLongIdleQty(0);
        return record;
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
        return (MerchantMarketPostMapper) Proxy.newProxyInstance(
                MerchantMarketPostMapper.class.getClassLoader(), new Class<?>[] { MerchantMarketPostMapper.class },
                (proxy, method, args) -> mapperCall.invoke(method.getName(), args));
    }

    @FunctionalInterface
    private interface MapperCall
    {
        Object invoke(String method, Object[] args);
    }
}
