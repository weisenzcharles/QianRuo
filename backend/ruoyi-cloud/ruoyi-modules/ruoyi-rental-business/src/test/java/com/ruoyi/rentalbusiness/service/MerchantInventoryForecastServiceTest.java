package com.ruoyi.rentalbusiness.service;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Test;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.rentalbusiness.domain.MerchantInventoryForecastRecord;
import com.ruoyi.rentalbusiness.mapper.MerchantInventoryMapper;
import com.ruoyi.rentalbusiness.model.MerchantInventoryForecastQuery;
import com.ruoyi.rentalbusiness.model.MerchantInventoryForecastRowVO;
import com.ruoyi.system.api.model.LoginUser;

/** 商户库存预测的权限范围、天数校验和快照生成测试。 */
public class MerchantInventoryForecastServiceTest
{
    @After
    public void clearContext()
    {
        SecurityContextHolder.remove();
    }

    /** 指定仓库权限的用户查询全部范围时，只能读取自己的仓库快照。 */
    @Test
    public void list_restrictedWarehouse_readsOnlyPermittedSnapshot()
    {
        setMerchantContext(RentalCode.WarehouseScope.SPECIFIED.getCode(), Set.of(101L));
        AtomicReference<Long> requestedWarehouse = new AtomicReference<>();
        MerchantInventoryMapper mapper = mapper((method, args) -> {
            if ("selectForecastSnapshots".equals(method))
            {
                requestedWarehouse.set((Long) args[2]);
                MerchantInventoryForecastRowVO row = new MerchantInventoryForecastRowVO();
                row.forecastType = 1;
                row.riskLevel = 1;
                return List.of(row);
            }
            return null;
        });

        MerchantInventoryForecastQuery query = new MerchantInventoryForecastQuery();
        query.days = 3;
        List<MerchantInventoryForecastRowVO> result = new MerchantInventoryForecastService(mapper).list(query);

        require(Long.valueOf(101L).equals(requestedWarehouse.get()), "restricted user must query only assigned warehouse");
        require(result.size() == 1 && "低库存".equals(result.get(0).riskLevelName),
                "forecast risk must be translated to Chinese");
    }

    /** 预测查询只接受产品约定的展示天数，避免任意范围造成大查询。 */
    @Test
    public void list_invalidDays_rejectsBeforeQuery()
    {
        setMerchantContext(RentalCode.WarehouseScope.ALL.getCode(), Collections.emptySet());
        MerchantInventoryMapper mapper = mapper((method, args) -> {
            throw new AssertionError("invalid days must not query snapshots");
        });
        MerchantInventoryForecastQuery query = new MerchantInventoryForecastQuery();
        query.days = 10;

        expectServiceException(() -> new MerchantInventoryForecastService(mapper).list(query), "预测天数必须为3天");
    }

    /** 同一轮刷新必须同时生成全部仓库汇总和各仓库的型号、套餐快照。 */
    @Test
    public void refreshMember_generatesAggregateAndWarehouseSnapshots()
    {
        AtomicReference<List<MerchantInventoryForecastRecord>> inserted = new AtomicReference<>();
        AtomicReference<List<String>> excludedOrderStatuses = new AtomicReference<>();
        MerchantInventoryMapper mapper = mapper((method, args) -> {
            if ("selectForecastWarehouseIds".equals(method)) return List.of(101L);
            if ("selectModelOverview".equals(method))
            {
                @SuppressWarnings("unchecked")
                List<String> statuses = (List<String>) args[6];
                excludedOrderStatuses.set(statuses);
                return List.of(Map.of("modelId", 1L, "itemName", "EOS R50", "forecastDate", LocalDate.of(2026, 9, 26),
                        "totalQuantity", 10, "availableQuantity", 1, "orderableQuantity", 1));
            }
            if ("selectPackageOverview".equals(method))
                return List.of(Map.of("packageId", 2L, "itemName", "演唱会套餐", "forecastDate", LocalDate.of(2026, 9, 26),
                        "totalQuantity", 2, "availableQuantity", 0, "orderableQuantity", 0));
            if ("insertForecastSnapshots".equals(method))
            {
                @SuppressWarnings("unchecked")
                List<MerchantInventoryForecastRecord> rows = (List<MerchantInventoryForecastRecord>) args[0];
                inserted.set(rows);
                return rows.size();
            }
            return 1;
        });

        int saved = new MerchantInventoryForecastService(mapper).refreshMember(10001L, 27L, LocalDate.of(2026, 9, 26));

        require(saved == 4, "a test snapshot source must generate both scopes and both forecast types");
        require(inserted.get() != null && inserted.get().size() == 4, "all generated snapshots must be inserted in one batch");
        require(inserted.get().stream().anyMatch(row -> row.warehouseId == 0L && row.riskLevel == 1),
                "low inventory risk must be recorded for aggregate scope");
        require(inserted.get().stream().anyMatch(row -> row.warehouseId == 101L && row.riskLevel == 2),
                "unavailable inventory risk must be recorded for warehouse scope");
        require(excludedOrderStatuses.get() != null
                && excludedOrderStatuses.get().containsAll(List.of("50", "completed", "80", "cancelled")),
                "forecast must exclude numeric and legacy completed/cancelled statuses");
    }

    /** 预测风险按保护规则后的可接单数量计算，不能只看原始可用库存。 */
    @Test
    public void refreshMember_usesOrderableQuantityForRisk()
    {
        AtomicReference<List<MerchantInventoryForecastRecord>> inserted = new AtomicReference<>();
        MerchantInventoryMapper mapper = mapper((method, args) -> {
            if ("selectForecastWarehouseIds".equals(method))
            {
                return Collections.emptyList();
            }
            if ("selectModelOverview".equals(method))
            {
                return List.of(Map.of("modelId", 1L, "itemName", "EOS R50", "forecastDate", LocalDate.of(2026, 9, 26),
                        "totalQuantity", 10, "availableQuantity", 5, "orderableQuantity", 0));
            }
            if ("selectPackageOverview".equals(method))
            {
                return Collections.emptyList();
            }
            if ("insertForecastSnapshots".equals(method))
            {
                @SuppressWarnings("unchecked")
                List<MerchantInventoryForecastRecord> rows = (List<MerchantInventoryForecastRecord>) args[0];
                inserted.set(rows);
                return rows.size();
            }
            return 1;
        });

        new MerchantInventoryForecastService(mapper).refreshMember(10001L, 27L, LocalDate.of(2026, 9, 26));

        require(inserted.get() != null && inserted.get().size() == 1, "only the model snapshot should be inserted");
        MerchantInventoryForecastRecord row = inserted.get().get(0);
        require(row.orderableQuantity == 0 && row.riskLevel == 2,
                "zero orderable quantity must be recorded as unavailable risk");
    }

    private void setMerchantContext(Integer scopeType, Set<Long> warehouseIds)
    {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserid(9001L);
        loginUser.setClientType(Constants.CLIENT_TYPE_MERCHANT);
        loginUser.setTenantId(10001L);
        loginUser.setMemberId(27L);
        loginUser.setMerchantAuthStatus(RentalCode.AuthenticationStatus.VERIFIED.getCode());
        loginUser.setWarehouseScopeType(scopeType);
        loginUser.setWarehouseIds(warehouseIds);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);
    }

    private MerchantInventoryMapper mapper(MapperCall mapperCall)
    {
        return (MerchantInventoryMapper) Proxy.newProxyInstance(MerchantInventoryMapper.class.getClassLoader(),
                new Class<?>[] { MerchantInventoryMapper.class },
                (proxy, method, args) -> mapperCall.invoke(method.getName(), args));
    }

    private void expectServiceException(Runnable action, String message)
    {
        try
        {
            action.run();
            throw new AssertionError("expected ServiceException");
        }
        catch (ServiceException exception)
        {
            require(exception.getMessage().contains(message), "unexpected error message: " + exception.getMessage());
        }
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
