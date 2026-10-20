package com.ruoyi.rentalbusiness.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.lang.reflect.Proxy;
import org.junit.After;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.rentalbusiness.enums.MerchantInventoryAlertThresholdType;
import com.ruoyi.rentalbusiness.enums.MerchantInventoryCalculationMode;
import com.ruoyi.rentalbusiness.mapper.MerchantInventoryMapper;
import com.ruoyi.rentalbusiness.mapper.MerchantInventoryGovernanceMapper;
import com.ruoyi.rentalbusiness.model.MerchantInventoryAlertRuleCommand;
import com.ruoyi.rentalbusiness.model.MerchantInventoryPolicyCommand;
import com.ruoyi.rentalbusiness.model.MerchantInventorySyncTokenCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantExternalInventoryReservationCommand;
import com.ruoyi.rentalbusiness.model.MerchantExternalInventoryReservationView;
import com.ruoyi.rentalbusiness.model.MerchantExternalOrderCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantOrderCreateCommand;
import com.ruoyi.rentalbusiness.enums.MerchantExternalCallbackEventType;
import com.ruoyi.system.api.model.LoginUser;

/** 商户库存告警和外部同步服务核心约束测试。 */
public class MerchantInventoryGovernanceServiceTest
{
    @After
    public void clearContext()
    {
        SecurityContextHolder.remove();
    }

    /** 百分比阈值必须落在有效业务范围内，非法请求不能写入数据库。 */
    @Test
    public void saveRule_percentAboveLimit_rejectsWithoutInsert()
    {
        setMerchantContext();
        AtomicReference<Boolean> inserted = new AtomicReference<>(false);
        MerchantInventoryGovernanceMapper mapper = mapper((method, args) -> {
            if ("insertAlertRule".equals(method)) inserted.set(true);
            return null;
        });
        MerchantInventoryAlertRuleCommand command = new MerchantInventoryAlertRuleCommand();
        command.thresholdType = MerchantInventoryAlertThresholdType.AVAILABLE_PERCENT;
        command.thresholdValue = java.math.BigDecimal.valueOf(101);

        expectServiceException(() -> new MerchantInventoryGovernanceService(mapper).saveRule(command), "不超过100");
        require(!inserted.get(), "invalid threshold must not insert a rule");
    }

    /** 同步令牌只返回原文，持久层参数必须保存摘要而不是原文。 */
    @Test
    public void createSyncToken_storesHashAndReturnsRawTokenOnce()
    {
        setMerchantContext();
        AtomicReference<String> persistedHash = new AtomicReference<>();
        MerchantInventoryGovernanceMapper mapper = mapper((method, args) -> {
            if ("insertSyncToken".equals(method)) {
                persistedHash.set((String) args[4]);
                return 1;
            }
            if ("selectLastInsertId".equals(method)) return 88L;
            return null;
        });
        MerchantInventorySyncTokenCreateCommand command = new MerchantInventorySyncTokenCreateCommand();
        command.tokenName = "仓库系统读取";

        Map<String, Object> result = new MerchantInventoryGovernanceService(mapper).createSyncToken(command);

        String rawToken = (String) result.get("token");
        require(rawToken != null && rawToken.startsWith("qr_inv_"), "response must contain the one-time raw token");
        require(persistedHash.get() != null && persistedHash.get().equals(sha256(rawToken)),
                "database must store token hash");
        require(!persistedHash.get().equals(rawToken), "database must never store raw token");
    }

    /** 正式订单授权令牌必须绑定 HTTPS 回调地址，避免创建后无法交付订单事件。 */
    @Test
    public void createSyncToken_orderCreateScopeWithoutCallback_rejects()
    {
        setMerchantContext();
        AtomicReference<Boolean> inserted = new AtomicReference<>(false);
        MerchantInventoryGovernanceMapper mapper = mapper((method, args) -> {
            if ("insertSyncToken".equals(method))
            {
                inserted.set(true);
                return 1;
            }
            return null;
        });
        MerchantInventorySyncTokenCreateCommand command = new MerchantInventorySyncTokenCreateCommand();
        command.tokenName = "订单接入";
        command.scopes = List.of("order:create");

        expectServiceException(() -> new MerchantInventoryGovernanceService(mapper).createSyncToken(command),
                "HTTPS回调地址");
        require(!inserted.get(), "invalid callback configuration must not insert a token");
    }

    /** 外部正式订单签名原文必须包含外部订单标识和预占单号，字段顺序固定。 */
    @Test
    public void externalOrderCommand_canonicalValue_containsIdempotencyFields()
    {
        MerchantExternalOrderCreateCommand command = new MerchantExternalOrderCreateCommand();
        command.externalOrderNo = "EXT-001";
        command.reservationNo = "RSV-001";
        command.channelId = 7L;

        String[] lines = command.canonicalValue().split("\\n", -1);
        require("EXT-001".equals(lines[0]), "external order number must be the first signed field");
        require("RSV-001".equals(lines[1]), "reservation number must be signed");
        require("7".equals(lines[2]), "channel id must be signed");
    }

    /** 订单创建回调必须在事务内写入固定事件类型和订单快照。 */
    @Test
    public void enqueueOrderCreated_persistsCallbackEvent()
    {
        AtomicReference<Object[]> callbackArgs = new AtomicReference<>();
        MerchantInventoryGovernanceMapper mapper = mapper((method, args) -> {
            if ("insertExternalCallbackEvent".equals(method))
            {
                callbackArgs.set(args);
                return 1;
            }
            return null;
        });
        MerchantOrderCreateCommand order = new MerchantOrderCreateCommand();
        order.externalOrderNo = "EXT-001";
        order.orderNo = "OD0000000001";
        order.orderStatus = RentalCode.OrderStatus.PENDING_SHIP.getCode();

        String eventNo = new MerchantExternalOrderCallbackService(mapper, new com.fasterxml.jackson.databind.ObjectMapper())
                .enqueueOrderCreated(11L, 10001L, 27L, 99L, "https://partner.example/callback",
                        sha256("qr_inv_secret"), order, "RSV-001");

        require(eventNo.startsWith("EOC-"), "callback event number must use the external callback prefix");
        require(callbackArgs.get() != null, "callback event must be persisted");
        require(Integer.valueOf(MerchantExternalCallbackEventType.ORDER_CREATED.getCode())
                .equals(callbackArgs.get()[7]), "callback event type must be order created");
        require(String.valueOf(callbackArgs.get()[8]).contains("EXT-001"),
                "callback payload must contain the external order number");
    }

    /** 外部订单状态变化必须沿用同一绑定和出站队列，未绑定的普通订单不产生回调。 */
    @Test
    public void enqueueOrderStatusChanged_persistsStatusPayloadForBoundOrder()
    {
        AtomicReference<Object[]> callbackArgs = new AtomicReference<>();
        MerchantInventoryGovernanceMapper mapper = mapper((method, args) -> {
            if ("selectExternalOrderBindingByOrder".equals(method))
            {
                return Map.of("bindingId", 11L, "externalOrderNo", "EXT-001", "orderNo", "OD0000000001",
                        "reservationNo", "RSV-001", "callbackUrl", "https://partner.example/callback",
                        "callbackSecretHash", sha256("qr_inv_secret"));
            }
            if ("insertExternalCallbackEvent".equals(method))
            {
                callbackArgs.set(args);
                return 1;
            }
            return null;
        });

        String eventNo = new MerchantExternalOrderCallbackService(mapper, new ObjectMapper())
                .enqueueOrderStatusChanged(10001L, 27L, 99L,
                        RentalCode.OrderStatus.PENDING_SHIP.getCode(), RentalCode.OrderStatus.SHIPPED.getCode(),
                        "订单发货");

        require(eventNo.startsWith("EOC-"), "status callback event number must use the external callback prefix");
        require(callbackArgs.get() != null, "bound order status event must be persisted");
        require(Integer.valueOf(MerchantExternalCallbackEventType.ORDER_STATUS_CHANGED.getCode())
                .equals(callbackArgs.get()[7]), "callback event type must be order status changed");
        require(String.valueOf(callbackArgs.get()[8]).contains("已发货"),
                "callback payload must contain the Chinese target status label");
    }

    /** 同步调用记录必须按当前商户和令牌范围返回，并补充中文结果。 */
    @Test
    public void listSyncLogs_decoratesResultWithoutExposingTokenSecret()
    {
        setMerchantContext();
        MerchantInventoryGovernanceMapper mapper = mapper((method, args) -> {
            if ("selectSyncToken".equals(method))
                return Map.of("tokenId", 88L, "tokenName", "仓库系统读取");
            if ("selectSyncLogs".equals(method))
                return List.of(Map.of("tokenName", "仓库系统读取", "resultCode", "SUCCESS",
                        "responseStatus", 200, "itemCount", 3));
            return null;
        });

        List<Map<String, Object>> result = new MerchantInventoryGovernanceService(mapper).listSyncLogs(88L);

        require(result.size() == 1, "sync logs must be returned");
        require("成功".equals(result.get(0).get("resultLabel")), "success result must have Chinese label");
        require(!result.get(0).containsKey("token"), "sync log must never expose raw token");
    }

    /** 外部快照请求必须带有效令牌，并且响应包含稳定的幂等号和版本。 */
    @Test
    public void snapshot_validToken_returnsVersionedRows()
    {
        String rawToken = "qr_inv_test_token";
        MerchantInventoryGovernanceMapper mapper = mapper((method, args) -> {
            if ("selectSyncTokenByHash".equals(method))
                return Map.of("tokenId", 9L, "tenantId", 10001L, "memberId", 27L, "scopes", "inventory:read", "tokenStatus", 0);
            if ("selectSyncLog".equals(method)) return null;
            if ("selectExternalSnapshot".equals(method))
                return List.of(Map.of("productName", "EOS R50", "snapshotVersion", 6L));
            return 1;
        });

        Map<String, Object> result = new MerchantInventoryGovernanceService(mapper)
                .snapshot(rawToken, "request-1", null, null, "/open/inventory/snapshot");

        require("request-1".equals(result.get("requestId")), "request id must be echoed");
        require(Long.valueOf(6L).equals(result.get("snapshotVersion")), "snapshot version must be returned");
        require(((List<?>) result.get("rows")).size() == 1, "snapshot rows must be returned");
    }

    /** 缺少或错误令牌不能读取任何库存数据。 */
    @Test
    public void snapshot_invalidToken_rejects()
    {
        MerchantInventoryGovernanceMapper mapper = mapper((method, args) -> null);
        expectServiceException(() -> new MerchantInventoryGovernanceService(mapper)
                .snapshot("invalid", "request-1", null, null, "/open/inventory/snapshot"), "令牌无效");
    }

    /** 外部库存写接口不能复用只有读取权限的令牌。 */
    @Test
    public void reserveExternal_withoutReserveScope_rejectsBeforeReservation()
    {
        String rawToken = "qr_inv_read_only";
        MerchantInventoryGovernanceMapper mapper = mapper((method, args) -> {
            if ("selectSyncTokenByHash".equals(method))
            {
                return Map.of("tokenId", 9L, "tenantId", 10001L, "memberId", 27L,
                        "scopes", "inventory:read", "tokenStatus", 0);
            }
            return null;
        });
        MerchantExternalInventoryReservationCommand command = new MerchantExternalInventoryReservationCommand();
        command.externalOrderNo = "EXT-001";

        expectServiceException(() -> new MerchantInventoryGovernanceService(mapper, null, null,
                new MerchantInventoryReservationService(null, null, null))
                .reserveExternal(rawToken, "request-1", "0".repeat(64), command,
                        "/open/inventory/reservations"), "服务权限");
    }

    /** 外部库存写接口必须校验令牌 HMAC，不能只凭令牌本身完成预占。 */
    @Test
    public void reserveExternal_invalidSignature_rejectsBeforeReservation()
    {
        String rawToken = "qr_inv_reserve_token";
        MerchantInventoryGovernanceMapper mapper = mapper((method, args) -> {
            if ("selectSyncTokenByHash".equals(method))
            {
                return Map.of("tokenId", 9L, "tenantId", 10001L, "memberId", 27L,
                        "scopes", "inventory:read,inventory:reserve", "tokenStatus", 0);
            }
            return null;
        });
        MerchantExternalInventoryReservationCommand command = new MerchantExternalInventoryReservationCommand();
        command.externalOrderNo = "EXT-001";
        command.productId = 33L;
        command.warehouseId = 44L;
        command.expireTime = java.time.LocalDateTime.now().plusHours(2);

        expectServiceException(() -> new MerchantInventoryGovernanceService(mapper, null, null,
                new MerchantInventoryReservationService(null, null, null))
                .reserveExternal(rawToken, "request-1", "0".repeat(64), command,
                        "/open/inventory/reservations"), "签名无效");
    }

    /** 同一外部请求重复到达时只复用预占结果，不能再次写入同步调用日志。 */
    @Test
    public void reserveExternal_replayedRequest_doesNotInsertDuplicateSyncLog()
    {
        String rawToken = "qr_inv_reserve_token";
        AtomicInteger insertedLogs = new AtomicInteger();
        MerchantInventoryGovernanceMapper mapper = mapper((method, args) -> {
            if ("selectSyncTokenByHash".equals(method))
            {
                return Map.of("tokenId", 9L, "tenantId", 10001L, "memberId", 27L,
                        "scopes", "inventory:read,inventory:reserve", "tokenStatus", 0);
            }
            if ("selectSyncLog".equals(method))
            {
                return Map.of("requestPath", "/open/inventory/reservations");
            }
            if ("insertSyncLog".equals(method))
            {
                insertedLogs.incrementAndGet();
                return 1;
            }
            if ("touchSyncToken".equals(method)) return 1;
            return null;
        });
        MerchantExternalInventoryReservationCommand command = new MerchantExternalInventoryReservationCommand();
        command.externalOrderNo = "EXT-001";
        command.productId = 33L;
        command.warehouseId = 44L;
        command.expireTime = java.time.LocalDateTime.now().plusHours(2);
        MerchantInventoryReservationService reservationService = new MerchantInventoryReservationService(null, null, null)
        {
            @Override
            public MerchantExternalInventoryReservationView reserveExternal(Long tenantId, Long memberId,
                    Long tokenId, MerchantExternalInventoryReservationCommand request, String requestId)
            {
                MerchantExternalInventoryReservationView view = new MerchantExternalInventoryReservationView();
                view.externalOrderNo = request.externalOrderNo;
                view.reservationNo = "RSV0001";
                view.productId = request.productId;
                view.warehouseId = request.warehouseId;
                view.quantity = 1;
                view.reservationStatus = 1;
                view.reservationStatusLabel = "已预占";
                view.replayed = true;
                return view;
            }
        };
        String signature = hmacSha256(rawToken, "request-1\n" + command.canonicalValue());

        Map<String, Object> result = new MerchantInventoryGovernanceService(mapper, null, null, reservationService)
                .reserveExternal(rawToken, "request-1", signature, command, "/open/inventory/reservations");

        require(Boolean.TRUE.equals(result.get("replayed")), "replayed reservation must be marked as replayed");
        require(insertedLogs.get() == 0, "replayed request must not insert a duplicate sync log");
    }

    /** 可接单比例必须受上限约束，非法策略不能更新库存。 */
    @Test
    public void updateInventoryPolicy_percentAboveLimit_rejectsWithoutUpdate()
    {
        setMerchantContext();
        AtomicReference<Boolean> updated = new AtomicReference<>(false);
        MerchantInventoryGovernanceMapper governanceMapper = mapper((method, args) -> {
            if ("selectInventoryPolicy".equals(method))
                return Map.of("inventoryId", 12L, "warehouseId", 21L);
            return null;
        });
        MerchantInventoryMapper inventoryMapper = inventoryMapper((method, args) -> {
            if ("updateInventoryPolicy".equals(method)) updated.set(true);
            return 1;
        });
        MerchantInventoryPolicyCommand command = new MerchantInventoryPolicyCommand();
        command.inventoryId = 12L;
        command.calculationMode = MerchantInventoryCalculationMode.TOTAL_PERCENT;
        command.calculationPercent = java.math.BigDecimal.valueOf(100.01);

        expectServiceException(() -> new MerchantInventoryGovernanceService(governanceMapper, null, inventoryMapper)
                .updateInventoryPolicy(command), "不超过100");
        require(!updated.get(), "invalid inventory policy must not update inventory");
    }

    /** 后台告警刷新按规则和未完成事件作用域执行，不依赖商户登录上下文。 */
    @Test
    public void refreshAllAlertEvents_refreshesEveryScope()
    {
        AtomicInteger refreshCalls = new AtomicInteger();
        MerchantInventoryGovernanceMapper mapper = mapper((method, args) -> {
            if ("selectAlertScopes".equals(method))
            {
                return List.of(Map.of("tenantId", 10001L, "memberId", 27L),
                        Map.of("tenantId", 10002L, "memberId", 28L));
            }
            if ("resolveAlertEvents".equals(method) || "createAlertEvents".equals(method))
            {
                refreshCalls.incrementAndGet();
                return 1;
            }
            return null;
        });

        int changed = new MerchantInventoryGovernanceService(mapper).refreshAllAlertEvents();

        require(changed == 4, "alert refresh must return all changed event counts");
        require(refreshCalls.get() == 4, "alert refresh must process every tenant and merchant scope");
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

    private MerchantInventoryGovernanceMapper mapper(MapperCall mapperCall)
    {
        return (MerchantInventoryGovernanceMapper) Proxy.newProxyInstance(
                MerchantInventoryGovernanceMapper.class.getClassLoader(),
                new Class<?>[] {MerchantInventoryGovernanceMapper.class},
                (proxy, method, args) -> mapperCall.invoke(method.getName(), args));
    }

    private MerchantInventoryMapper inventoryMapper(MapperCall mapperCall)
    {
        return (MerchantInventoryMapper) Proxy.newProxyInstance(
                MerchantInventoryMapper.class.getClassLoader(),
                new Class<?>[] {MerchantInventoryMapper.class},
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

    private String sha256(String value)
    {
        try
        {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        }
        catch (java.security.NoSuchAlgorithmException exception)
        {
            throw new AssertionError(exception);
        }
    }

    private String hmacSha256(String secret, String content)
    {
        try
        {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return java.util.HexFormat.of().formatHex(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        }
        catch (java.security.GeneralSecurityException exception)
        {
            throw new AssertionError(exception);
        }
    }

    private void require(boolean condition, String message)
    {
        if (!condition) throw new AssertionError(message);
    }

    @FunctionalInterface
    private interface MapperCall
    {
        Object invoke(String method, Object[] args);
    }
}
