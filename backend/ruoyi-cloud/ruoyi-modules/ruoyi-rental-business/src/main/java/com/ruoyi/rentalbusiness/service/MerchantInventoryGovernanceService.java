package com.ruoyi.rentalbusiness.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.rentalbusiness.enums.MerchantInventoryAlertRuleStatus;
import com.ruoyi.rentalbusiness.enums.MerchantInventoryAlertStatus;
import com.ruoyi.rentalbusiness.enums.MerchantInventoryAlertThresholdType;
import com.ruoyi.rentalbusiness.enums.MerchantInventoryCalculationMode;
import com.ruoyi.rentalbusiness.enums.MerchantInventorySyncScope;
import com.ruoyi.rentalbusiness.enums.MerchantInventorySyncTokenStatus;
import com.ruoyi.rentalbusiness.mapper.MerchantInventoryMapper;
import com.ruoyi.rentalbusiness.mapper.MerchantInventoryGovernanceMapper;
import com.ruoyi.rentalbusiness.model.MerchantInventoryAlertRuleCommand;
import com.ruoyi.rentalbusiness.model.MerchantExternalInventoryReservationCommand;
import com.ruoyi.rentalbusiness.model.MerchantExternalInventoryReservationView;
import com.ruoyi.rentalbusiness.model.MerchantInventoryPolicyCommand;
import com.ruoyi.rentalbusiness.model.MerchantInventorySyncTokenCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantExternalOrderCreateCommand;
import com.ruoyi.rentalbusiness.model.MerchantExternalOrderView;
import com.ruoyi.rentalbusiness.model.MerchantOrderCreateCommand;
import com.ruoyi.rentalbusiness.enums.MerchantInventoryReservationStatus;

/**
 * 商户库存高级治理服务。
 *
 * 告警是库存事实的派生状态，查询时会先补齐触发和恢复事件；外部同步读取库存快照，
 * 并在独立的外部预占来源下复用库存锁，不直接伪造正式订单或覆盖库存事实。
 */
@Service
public class MerchantInventoryGovernanceService
{
    private static final String RATE_LIMIT_KEY_PREFIX = "rental:inventory-sync:rate:";
    private static final int RATE_LIMIT_PER_SECOND = 10;
    private static final int MAX_REQUEST_ID_LENGTH = 128;
    private final MerchantInventoryGovernanceMapper mapper;
    private final RedisService redisService;
    private final MerchantInventoryMapper inventoryMapper;
    private final MerchantInventoryReservationService reservationService;
    private final MerchantOrderService orderService;
    private final MerchantExternalOrderCallbackService callbackService;

    @org.springframework.beans.factory.annotation.Autowired
    public MerchantInventoryGovernanceService(MerchantInventoryGovernanceMapper mapper, RedisService redisService,
            MerchantInventoryMapper inventoryMapper, MerchantInventoryReservationService reservationService,
            MerchantOrderService orderService, MerchantExternalOrderCallbackService callbackService)
    {
        this.mapper = mapper;
        this.redisService = redisService;
        this.inventoryMapper = inventoryMapper;
        this.reservationService = reservationService;
        this.orderService = orderService;
        this.callbackService = callbackService;
    }

    public MerchantInventoryGovernanceService(MerchantInventoryGovernanceMapper mapper, RedisService redisService,
            MerchantInventoryMapper inventoryMapper, MerchantInventoryReservationService reservationService)
    {
        this(mapper, redisService, inventoryMapper, reservationService, null, null);
    }

    /** 供不涉及 Redis 限流的服务单元测试使用。 */
    public MerchantInventoryGovernanceService(MerchantInventoryGovernanceMapper mapper)
    {
        this(mapper, null, null, null, null, null);
    }

    /** 供服务单元测试复用不含库存策略依赖的构造方式。 */
    public MerchantInventoryGovernanceService(MerchantInventoryGovernanceMapper mapper, RedisService redisService)
    {
        this(mapper, redisService, null, null, null, null);
    }

    /** 供外部同步服务测试使用的显式构造方式。 */
    public MerchantInventoryGovernanceService(MerchantInventoryGovernanceMapper mapper, RedisService redisService,
            MerchantInventoryMapper inventoryMapper)
    {
        this(mapper, redisService, inventoryMapper, null, null, null);
    }

    /** 查询当前商户有权查看的告警规则。 */
    public List<Map<String, Object>> listRules(Long warehouseId, Long productId)
    {
        MerchantContextUtils.requireMerchantApproved();
        checkWarehouse(warehouseId);
        List<Map<String, Object>> rows = mapper.selectAlertRules(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), warehouseId, productId, MerchantContextUtils.getWarehouseIds(),
                MerchantContextUtils.hasAllWarehouseAccess());
        return decorateRules(rows);
    }

    /** 新增或修改告警规则。 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> saveRule(MerchantInventoryAlertRuleCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null)
        {
            throw new ServiceException("告警规则不能为空");
        }
        MerchantInventoryAlertThresholdType thresholdType = command.thresholdType;
        if (thresholdType == null)
        {
            throw new ServiceException("请选择告警阈值类型");
        }
        BigDecimal thresholdValue = normalizeThreshold(thresholdType, command.thresholdValue);
        checkWarehouse(command.warehouseId);
        if (command.productId != null && mapper.selectProduct(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), command.productId) == null)
        {
            throw new ServiceException("商品不存在或已停用");
        }
        if (command.warehouseId != null && mapper.selectWarehouse(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), command.warehouseId) == null)
        {
            throw new ServiceException("仓库不存在或已停用");
        }
        MerchantInventoryAlertRuleStatus ruleStatus = command.ruleStatus == null
                ? MerchantInventoryAlertRuleStatus.ENABLED : command.ruleStatus;
        int notifyEnabled = Boolean.FALSE.equals(command.notifyEnabled) ? 0 : 1;
        Long operator = SecurityUtils.getUserId();
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        Map<String, Object> result = new LinkedHashMap<>();
        if (command.ruleId == null)
        {
            if (mapper.insertAlertRule(tenantId, memberId, command.warehouseId, command.productId,
                    thresholdType.getCode(), thresholdValue, notifyEnabled, ruleStatus.getCode(), operator) != 1)
            {
                throw new ServiceException("新增告警规则失败");
            }
            result.put("ruleId", mapper.selectLastInsertId());
        }
        else
        {
            requireRule(command.ruleId);
            if (mapper.updateAlertRule(tenantId, memberId, command.ruleId, command.warehouseId, command.productId,
                    thresholdType.getCode(), thresholdValue, notifyEnabled, ruleStatus.getCode(), operator) != 1)
            {
                throw new ServiceException("修改告警规则失败");
            }
            result.put("ruleId", command.ruleId);
        }
        result.put("ruleStatus", ruleStatus.getCode());
        result.put("ruleStatusLabel", ruleStatus.getLabel());
        return result;
    }

    /** 删除告警规则并在下一次查询时恢复其活动事件。 */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRule(Long ruleId)
    {
        MerchantContextUtils.requireMerchantApproved();
        requireRule(ruleId);
        if (mapper.softDeleteAlertRule(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), ruleId,
                SecurityUtils.getUserId()) != 1)
        {
            throw new ServiceException("删除告警规则失败");
        }
    }

    /** 刷新并查询库存告警事件。 */
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> listEvents(Integer alertStatus, Long warehouseId)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (alertStatus != null && MerchantInventoryAlertStatus.fromCode(alertStatus) == null)
        {
            throw new ServiceException("告警状态不合法");
        }
        checkWarehouse(warehouseId);
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        Set<Long> warehouseIds = MerchantContextUtils.getWarehouseIds();
        boolean allWarehouseAccess = MerchantContextUtils.hasAllWarehouseAccess();
        Long operator = SecurityUtils.getUserId();
        mapper.resolveAlertEvents(tenantId, memberId, warehouseIds, allWarehouseAccess, operator);
        mapper.createAlertEvents(tenantId, memberId, warehouseIds, allWarehouseAccess, operator);
        return decorateEvents(mapper.selectAlertEvents(tenantId, memberId, alertStatus, warehouseId, warehouseIds,
                allWarehouseAccess));
    }

    /**
     * 刷新所有商户的库存告警派生事件。
     *
     * <p>后台任务没有商户登录上下文，因此先从告警规则和未完成事件中读取作用域，
     * 再以全仓库范围执行恢复和触发计算。页面查询仍会继续即时刷新，任务失败不会影响库存事实。</p>
     *
     * @return 本次恢复和新增的告警事件数量
     */
    public int refreshAllAlertEvents()
    {
        List<Map<String, Object>> scopes = mapper.selectAlertScopes();
        if (scopes == null || scopes.isEmpty())
        {
            return 0;
        }
        int changed = 0;
        for (Map<String, Object> scope : scopes)
        {
            Long tenantId = number(scope.get("tenantId"));
            Long memberId = number(scope.get("memberId"));
            if (tenantId == null || memberId == null)
            {
                continue;
            }
            Set<Long> allWarehouses = Collections.emptySet();
            changed += mapper.resolveAlertEvents(tenantId, memberId, allWarehouses, true, null);
            changed += mapper.createAlertEvents(tenantId, memberId, allWarehouses, true, null);
        }
        return changed;
    }

    /** 确认一条待处理告警。 */
    @Transactional(rollbackFor = Exception.class)
    public void acknowledge(Long alertId)
    {
        MerchantContextUtils.requireMerchantApproved();
        Map<String, Object> event = mapper.selectAlertEvent(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), alertId);
        if (event == null)
        {
            throw new ServiceException("告警不存在或已处理");
        }
        checkWarehouse(number(event.get("warehouseId")));
        if (!Integer.valueOf(MerchantInventoryAlertStatus.PENDING.getCode()).equals(integer(event.get("alertStatus"))))
        {
            throw new ServiceException("只有待处理告警可以确认");
        }
        if (mapper.acknowledgeAlertEvent(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), alertId,
                SecurityUtils.getUserId()) != 1)
        {
            throw new ServiceException("确认告警失败");
        }
    }

    /** 查询当前商户外部同步令牌，不返回摘要或原始令牌。 */
    public List<Map<String, Object>> listSyncTokens()
    {
        MerchantContextUtils.requireMerchantApproved();
        return decorateTokens(mapper.selectSyncTokens(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId()));
    }

    /** 查询当前商户最近的外部库存同步调用结果，不返回同步令牌原文或摘要。 */
    public List<Map<String, Object>> listSyncLogs(Long tokenId)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (tokenId != null && mapper.selectSyncToken(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), tokenId) == null)
        {
            throw new ServiceException("同步令牌不存在");
        }
        return decorateSyncLogs(mapper.selectSyncLogs(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), tokenId));
    }

    /** 创建外部库存同步令牌，原始令牌只在本次响应中返回。 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createSyncToken(MerchantInventorySyncTokenCreateCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (command == null || trim(command.tokenName) == null || trim(command.tokenName).length() > 80)
        {
            throw new ServiceException("令牌名称不能为空且不能超过80个字符");
        }
        String scopes = normalizeScopes(command.scopes);
        String callbackUrl = normalizeCallbackUrl(command.callbackUrl);
        if (containsScope(scopes, MerchantInventorySyncScope.ORDER_CREATE) && callbackUrl == null)
        {
            throw new ServiceException("启用正式订单授权时必须配置HTTPS回调地址");
        }
        String rawToken = "qr_inv_" + UUID.randomUUID().toString().replace("-", "");
        String tokenHash = sha256(rawToken);
        String tokenPrefix = rawToken.substring(0, Math.min(rawToken.length(), 16));
        if (mapper.insertSyncToken(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                trim(command.tokenName), tokenPrefix, tokenHash, scopes, callbackUrl, command.expireTime,
                SecurityUtils.getUserId()) != 1)
        {
            throw new ServiceException("创建同步令牌失败");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tokenId", mapper.selectLastInsertId());
        result.put("tokenName", trim(command.tokenName));
        result.put("token", rawToken);
        result.put("tokenPrefix", tokenPrefix);
        result.put("scopes", scopes);
        result.put("callbackUrl", callbackUrl);
        result.put("expireTime", command.expireTime);
        return result;
    }

    /** 吊销当前商户的外部同步令牌。 */
    @Transactional(rollbackFor = Exception.class)
    public void revokeSyncToken(Long tokenId)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (mapper.selectSyncToken(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), tokenId) == null)
        {
            throw new ServiceException("同步令牌不存在");
        }
        if (mapper.revokeSyncToken(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), tokenId,
                SecurityUtils.getUserId()) != 1)
        {
            throw new ServiceException("吊销同步令牌失败");
        }
    }

    /** 修改库存可接单口径，并递增外部同步版本。 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateInventoryPolicy(MerchantInventoryPolicyCommand command)
    {
        MerchantContextUtils.requireMerchantApproved();
        if (inventoryMapper == null || command == null || command.inventoryId == null)
        {
            throw new ServiceException("库存策略参数不完整");
        }
        MerchantInventoryCalculationMode mode = command.calculationMode == null
                ? MerchantInventoryCalculationMode.PROTECTED_QUANTITY : command.calculationMode;
        Map<String, Object> existing = mapper.selectInventoryPolicy(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), command.inventoryId);
        if (existing == null)
        {
            throw new ServiceException("库存不存在或已删除");
        }
        checkWarehouse(number(existing.get("warehouseId")));
        int protectionQty = command.stockProtectionQty == null ? 0 : command.stockProtectionQty;
        if (protectionQty < 0)
        {
            throw new ServiceException("保护数量不能小于零");
        }
        BigDecimal percent = command.calculationPercent == null ? BigDecimal.valueOf(90) : command.calculationPercent;
        if (percent.signum() <= 0 || percent.compareTo(BigDecimal.valueOf(100)) > 0)
        {
            throw new ServiceException("总库存比例必须大于0且不超过100");
        }
        if (inventoryMapper.updateInventoryPolicy(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(),
                command.inventoryId, protectionQty, mode.getCode(), percent, SecurityUtils.getUserId()) != 1)
        {
            throw new ServiceException("保存库存策略失败");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("inventoryId", command.inventoryId);
        result.put("calculationMode", mode.name());
        result.put("calculationModeLabel", mode.getLabel());
        result.put("stockProtectionQty", protectionQty);
        result.put("calculationPercent", percent);
        return result;
    }

    /**
     * 根据外部令牌返回库存快照。该接口不依赖商户登录态，令牌本身只授予读取权限。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> snapshot(String rawToken, String requestId, Long warehouseId, Long productId,
            String requestPath)
    {
        String normalizedToken = trim(rawToken);
        if (normalizedToken == null || normalizedToken.length() > 256)
        {
            throw new ServiceException("库存同步令牌无效");
        }
        String normalizedRequestId = trim(requestId);
        if (normalizedRequestId == null)
        {
            normalizedRequestId = UUID.randomUUID().toString();
        }
        if (normalizedRequestId.length() > MAX_REQUEST_ID_LENGTH)
        {
            throw new ServiceException("请求幂等号不能超过128个字符");
        }
        Map<String, Object> token = mapper.selectSyncTokenByHash(sha256(normalizedToken));
        if (token == null || !Integer.valueOf(MerchantInventorySyncTokenStatus.ENABLED.getCode()).equals(integer(token.get("tokenStatus")))
                || expired(token.get("expireTime")) || !containsScope(string(token.get("scopes")), MerchantInventorySyncScope.INVENTORY_READ))
        {
            throw new ServiceException("库存同步令牌无效或已过期");
        }
        Long tokenId = number(token.get("tokenId"));
        throttle(tokenId);
        Map<String, Object> existing = mapper.selectSyncLog(tokenId, normalizedRequestId);
        Long tenantId = number(token.get("tenantId"));
        Long memberId = number(token.get("memberId"));
        if (existing != null)
        {
            return buildSnapshotResponse(mapper.selectExternalSnapshot(tenantId, memberId, warehouseId, productId),
                    normalizedRequestId, true);
        }
        long started = System.currentTimeMillis();
        List<Map<String, Object>> rows = mapper.selectExternalSnapshot(tenantId, memberId, warehouseId, productId);
        Map<String, Object> result = buildSnapshotResponse(rows, normalizedRequestId, false);
        mapper.insertSyncLog(tokenId, tenantId, memberId, normalizedRequestId,
                requestPath == null ? "/open/inventory/snapshot" : requestPath, 200, rows.size(), "SUCCESS", "库存快照已返回",
                (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - started));
        mapper.touchSyncToken(tokenId);
        return result;
    }

    /**
     * 外部平台创建库存预占。写接口必须同时通过令牌范围和 HMAC 签名校验，
     * 并把请求幂等号写入同步日志和预占单。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> reserveExternal(String rawToken, String requestId, String signature,
            MerchantExternalInventoryReservationCommand command, String requestPath)
    {
        String normalizedRequestId = normalizeRequestId(requestId);
        Map<String, Object> token = requireOpenToken(rawToken, normalizedRequestId,
                MerchantInventorySyncScope.INVENTORY_RESERVE);
        verifySignature(rawToken, signature, normalizedRequestId,
                command == null ? null : command.canonicalValue());
        if (reservationService == null)
        {
            throw new ServiceException("外部库存预占服务未初始化");
        }
        Long tokenId = number(token.get("tokenId"));
        Map<String, Object> existingLog = mapper.selectSyncLog(tokenId, normalizedRequestId);
        String expectedPath = requestPath == null ? "/open/inventory/reservations" : requestPath;
        if (existingLog != null && !expectedPath.equals(string(existingLog.get("requestPath"))))
        {
            throw new ServiceException("同一请求幂等号不能用于不同的库存同步请求");
        }
        long started = System.currentTimeMillis();
        MerchantExternalInventoryReservationView view = reservationService.reserveExternal(
                number(token.get("tenantId")), number(token.get("memberId")), tokenId, command, normalizedRequestId);
        if (existingLog == null)
        {
            mapper.insertSyncLog(tokenId, number(token.get("tenantId")), number(token.get("memberId")), normalizedRequestId,
                    expectedPath, 200, 1, "SUCCESS",
                    view.replayed ? "外部库存预占请求已幂等返回" : "外部库存预占已创建",
                    (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - started));
        }
        mapper.touchSyncToken(tokenId);
        return externalReservationResponse(view, normalizedRequestId);
    }

    /** 释放外部平台创建的库存预占，释放动作同样需要签名和原令牌范围。 */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> releaseExternal(String rawToken, String requestId, String signature,
            String reservationNo, String requestPath)
    {
        String normalizedRequestId = normalizeRequestId(requestId);
        String normalizedReservationNo = trim(reservationNo);
        if (normalizedReservationNo == null || normalizedReservationNo.length() > 64)
        {
            throw new ServiceException("预占单号不能为空且不能超过64个字符");
        }
        Map<String, Object> token = requireOpenToken(rawToken, normalizedRequestId,
                MerchantInventorySyncScope.INVENTORY_RESERVE);
        verifySignature(rawToken, signature, normalizedRequestId, "release\n" + normalizedReservationNo);
        if (reservationService == null)
        {
            throw new ServiceException("外部库存预占服务未初始化");
        }
        Long tokenId = number(token.get("tokenId"));
        String expectedPath = "/open/inventory/reservations/" + normalizedReservationNo + "/release";
        Map<String, Object> existingLog = mapper.selectSyncLog(tokenId, normalizedRequestId);
        if (existingLog != null && !expectedPath.equals(string(existingLog.get("requestPath"))))
        {
            throw new ServiceException("同一请求幂等号不能用于不同的库存释放请求");
        }
        long started = System.currentTimeMillis();
        MerchantExternalInventoryReservationView view = reservationService.releaseExternal(
                number(token.get("tenantId")), number(token.get("memberId")), tokenId, normalizedReservationNo);
        if (existingLog == null)
        {
            mapper.insertSyncLog(tokenId, number(token.get("tenantId")), number(token.get("memberId")), normalizedRequestId,
                    requestPath == null ? expectedPath : requestPath, 200, 1, "SUCCESS",
                    view.replayed ? "外部库存释放请求已幂等返回" : "外部库存预占已释放",
                    (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - started));
        }
        mapper.touchSyncToken(tokenId);
        return externalReservationResponse(view, normalizedRequestId);
    }

    /**
     * 基于令牌所属的有效外部预占创建正式订单，并将创建成功事件写入回调出站表。
     * 同一令牌下的外部订单标识是幂等键，不允许以不同载荷覆盖已创建订单。
     */
    @Transactional(rollbackFor = Exception.class)
    public MerchantExternalOrderView createExternalOrder(String rawToken, String requestId, String signature,
            MerchantExternalOrderCreateCommand command, String requestPath)
    {
        String normalizedRequestId = normalizeRequestId(requestId);
        Map<String, Object> token = requireOpenToken(rawToken, normalizedRequestId,
                MerchantInventorySyncScope.ORDER_CREATE);
        if (command == null)
        {
            throw new ServiceException("外部订单参数不能为空");
        }
        verifySignature(rawToken, signature, normalizedRequestId, command.canonicalValue());
        if (orderService == null || reservationService == null || callbackService == null)
        {
            throw new ServiceException("外部订单服务未初始化");
        }
        String externalOrderNo = requiredExternalText(command.externalOrderNo, "外部订单标识", 128);
        String reservationNo = requiredExternalText(command.reservationNo, "库存预占单号", 64);
        String expectedPath = requestPath == null ? "/open/orders" : requestPath;
        Long tokenId = number(token.get("tokenId"));
        Long tenantId = number(token.get("tenantId"));
        Long memberId = number(token.get("memberId"));
        if (tokenId == null || tenantId == null || memberId == null)
        {
            throw new ServiceException("外部订单令牌归属信息不完整");
        }
        Map<String, Object> existingLog = mapper.selectSyncLog(tokenId, normalizedRequestId);
        if (existingLog != null && !expectedPath.equals(string(existingLog.get("requestPath"))))
        {
            throw new ServiceException("同一请求幂等号不能用于不同的外部订单请求");
        }
        String payloadHash = sha256(command.canonicalValue());
        Map<String, Object> binding = mapper.selectExternalOrderBindingForUpdate(tokenId, externalOrderNo);
        if (binding != null)
        {
            if (!payloadHash.equals(string(binding.get("payloadHash"))))
            {
                throw new ServiceException("同一外部订单标识不能提交不同的订单参数");
            }
            MerchantExternalOrderView replayed = toExternalOrderView(binding, normalizedRequestId, true);
            recordExternalOrderSyncLog(existingLog, tokenId, tenantId, memberId, normalizedRequestId, expectedPath,
                    replayed, true);
            mapper.touchSyncToken(tokenId);
            return replayed;
        }
        Map<String, Object> reservation = reservationService.requireExternalReservationForOrder(tenantId, memberId,
                tokenId, reservationNo, externalOrderNo);
        if (number(reservation, "orderId") != null)
        {
            Map<String, Object> bound = mapper.selectExternalOrderBindingForUpdate(tokenId, externalOrderNo);
            if (bound != null)
            {
                if (!payloadHash.equals(string(bound.get("payloadHash"))))
                {
                    throw new ServiceException("同一外部订单标识不能提交不同的订单参数");
                }
                MerchantExternalOrderView replayed = toExternalOrderView(bound, normalizedRequestId, true);
                recordExternalOrderSyncLog(existingLog, tokenId, tenantId, memberId, normalizedRequestId, expectedPath,
                        replayed, true);
                mapper.touchSyncToken(tokenId);
                return replayed;
            }
            throw new ServiceException("库存预占已绑定订单，但订单绑定记录不存在");
        }
        Long warehouseId = number(reservation, "warehouseId");
        if (warehouseId == null || inventoryMapper == null
                || inventoryMapper.selectWarehouse(tenantId, memberId, warehouseId) == null)
        {
            throw new ServiceException("外部库存预占仓库不存在或已停用");
        }
        if (command.returnWarehouseId == null
                || inventoryMapper.selectWarehouse(tenantId, memberId, command.returnWarehouseId) == null)
        {
            throw new ServiceException("归还仓库不存在或已停用");
        }
        MerchantOrderCreateCommand order = toExternalOrderCommand(command, reservation, externalOrderNo,
                warehouseId);
        MerchantOrderCreateCommand created = orderService.addExternal(tenantId, memberId, order);
        reservationService.bindExternalReservationOrder(tenantId, memberId, tokenId, reservationNo,
                externalOrderNo, created.orderId);
        String callbackUrl = string(token.get("callbackUrl"));
        String tokenHash = string(token.get("tokenHash"));
        if (blank(callbackUrl) || blank(tokenHash))
        {
            throw new ServiceException("外部订单令牌未配置回调信息");
        }
        if (mapper.insertExternalOrderBinding(tenantId, memberId, tokenId, number(reservation, "reservationId"),
                created.orderId, externalOrderNo, created.orderNo, payloadHash, callbackUrl, tokenHash) != 1)
        {
            throw new ServiceException("外部订单绑定记录保存失败");
        }
        Long bindingId = mapper.selectLastInsertId();
        callbackService.enqueueOrderCreated(bindingId, tenantId, memberId, created.orderId, callbackUrl, tokenHash,
                created, reservationNo);
        MerchantExternalOrderView result = toExternalOrderView(created, normalizedRequestId, externalOrderNo,
                reservationNo, true);
        recordExternalOrderSyncLog(existingLog, tokenId, tenantId, memberId, normalizedRequestId, expectedPath,
                result, false);
        mapper.touchSyncToken(tokenId);
        return result;
    }

    private Map<String, Object> requireOpenToken(String rawToken, String requestId,
            MerchantInventorySyncScope scope)
    {
        String normalizedToken = trim(rawToken);
        if (normalizedToken == null || normalizedToken.length() > 256)
        {
            throw new ServiceException("库存同步令牌无效");
        }
        Map<String, Object> token = mapper.selectSyncTokenByHash(sha256(normalizedToken));
        if (token == null
                || !Integer.valueOf(MerchantInventorySyncTokenStatus.ENABLED.getCode())
                        .equals(integer(token.get("tokenStatus")))
                || expired(token.get("expireTime"))
                || !containsScope(string(token.get("scopes")), scope))
        {
            throw new ServiceException("库存同步令牌无效、已过期或没有对应服务权限");
        }
        throttle(number(token.get("tokenId")));
        return token;
    }

    private String normalizeRequestId(String requestId)
    {
        String normalized = trim(requestId);
        if (normalized == null)
        {
            normalized = UUID.randomUUID().toString();
        }
        if (normalized.length() > MAX_REQUEST_ID_LENGTH)
        {
            throw new ServiceException("请求幂等号不能超过128个字符");
        }
        return normalized;
    }

    private void verifySignature(String rawToken, String signature, String requestId, String payload)
    {
        String normalizedSignature = trim(signature);
        if (normalizedSignature == null || normalizedSignature.length() != 64)
        {
            throw new ServiceException("外部库存请求签名无效");
        }
        String expected = hmacSha256(rawToken, requestId + "\n" + (payload == null ? "" : payload));
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                normalizedSignature.toLowerCase(java.util.Locale.ROOT).getBytes(StandardCharsets.UTF_8)))
        {
            throw new ServiceException("外部库存请求签名无效");
        }
    }

    private String hmacSha256(String secret, String content)
    {
        try
        {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return java.util.HexFormat.of().formatHex(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        }
        catch (java.security.GeneralSecurityException exception)
        {
            throw new IllegalStateException("HMAC-SHA256不可用", exception);
        }
    }

    private Map<String, Object> externalReservationResponse(MerchantExternalInventoryReservationView view,
            String requestId)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("requestId", requestId);
        result.put("replayed", view.replayed);
        result.put("externalOrderNo", view.externalOrderNo);
        result.put("reservationNo", view.reservationNo);
        result.put("packageId", view.packageId);
        result.put("productId", view.productId);
        result.put("warehouseId", view.warehouseId);
        result.put("quantity", view.quantity);
        result.put("reservationStatus", view.reservationStatus);
        result.put("reservationStatusLabel", view.reservationStatusLabel);
        result.put("expireTime", view.expireTime);
        return result;
    }

    private MerchantOrderCreateCommand toExternalOrderCommand(MerchantExternalOrderCreateCommand command,
            Map<String, Object> reservation, String externalOrderNo, Long warehouseId)
    {
        MerchantOrderCreateCommand order = new MerchantOrderCreateCommand();
        order.externalOrderNo = externalOrderNo;
        order.channelId = command.channelId;
        order.shopName = trim(command.shopName);
        order.customerName = trim(command.customerName);
        order.customerNickname = trim(command.customerNickname);
        order.customerPhone = trim(command.customerPhone);
        order.recipientName = trim(command.recipientName);
        order.recipientPhone = trim(command.recipientPhone);
        order.recipientAddress = trim(command.recipientAddress);
        order.warehouseId = warehouseId;
        order.returnWarehouseId = command.returnWarehouseId;
        order.leaseStartDate = trim(command.leaseStartDate);
        order.leaseEndDate = trim(command.leaseEndDate);
        order.shipDueTime = trim(command.shipDueTime);
        order.shipTimeSlot = trim(command.shipTimeSlot);
        order.returnDueTime = trim(command.returnDueTime);
        order.deliveryMethod = trim(command.deliveryMethod);
        order.returnDeliveryMethod = trim(command.returnDeliveryMethod);
        order.deliveryAmount = command.deliveryAmount;
        order.insuranceAmount = command.insuranceAmount;
        order.rentAmount = command.rentAmount;
        order.depositAmount = command.depositAmount;
        order.remark = trim(command.remark);
        order.packageId = number(reservation, "packageId");
        order.productId = number(reservation, "externalProductId");
        order.quantity = intValue(reservation.get("externalQuantity"));
        if (order.quantity == null || order.quantity <= 0)
        {
            order.quantity = 1;
        }
        return order;
    }

    private MerchantExternalOrderView toExternalOrderView(Map<String, Object> binding, String requestId,
            boolean replayed)
    {
        MerchantExternalOrderView view = new MerchantExternalOrderView();
        view.requestId = requestId;
        view.replayed = replayed;
        view.orderNo = string(binding.get("orderNo"));
        view.externalOrderNo = string(binding.get("externalOrderNo"));
        view.reservationNo = string(binding.get("reservationNo"));
        view.orderStatus = com.ruoyi.common.core.enums.RentalCode.OrderStatus.PENDING_SHIP.getCode();
        view.orderStatusLabel = com.ruoyi.common.core.enums.RentalCode.OrderStatus.PENDING_SHIP.getLabel();
        view.createTime = localDateTime(binding.get("createTime"));
        view.callbackQueued = true;
        return view;
    }

    private MerchantExternalOrderView toExternalOrderView(MerchantOrderCreateCommand order, String requestId,
            String externalOrderNo, String reservationNo, boolean callbackQueued)
    {
        MerchantExternalOrderView view = new MerchantExternalOrderView();
        view.requestId = requestId;
        view.replayed = false;
        view.orderNo = order.orderNo;
        view.externalOrderNo = externalOrderNo;
        view.reservationNo = reservationNo;
        view.orderStatus = order.orderStatus;
        view.orderStatusLabel = com.ruoyi.common.core.enums.RentalCode.OrderStatus.PENDING_SHIP.getLabel();
        view.createTime = LocalDateTime.now();
        view.callbackQueued = callbackQueued;
        return view;
    }

    private void recordExternalOrderSyncLog(Map<String, Object> existingLog, Long tokenId, Long tenantId,
            Long memberId, String requestId, String requestPath, MerchantExternalOrderView view, boolean replayed)
    {
        if (existingLog == null)
        {
            mapper.insertSyncLog(tokenId, tenantId, memberId, requestId, requestPath, 200, 1, "SUCCESS",
                    replayed ? "外部正式订单请求已幂等返回" : "外部正式订单已创建并进入回调队列", 0);
        }
    }

    private String requiredExternalText(String value, String fieldName, int maxLength)
    {
        String normalized = trim(value);
        if (normalized == null || normalized.length() > maxLength)
        {
            throw new ServiceException(fieldName + "不能为空且不能超过" + maxLength + "个字符");
        }
        return normalized;
    }

    private String normalizeCallbackUrl(String value)
    {
        String normalized = trim(value);
        if (normalized == null)
        {
            return null;
        }
        if (normalized.length() > 500)
        {
            throw new ServiceException("回调地址不能超过500个字符");
        }
        try
        {
            URI uri = URI.create(normalized);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null)
            {
                throw new ServiceException("回调地址必须是合法的HTTPS地址");
            }
        }
        catch (IllegalArgumentException exception)
        {
            throw new ServiceException("回调地址格式不正确");
        }
        return normalized;
    }

    private void checkWarehouse(Long warehouseId)
    {
        if (warehouseId != null)
        {
            MerchantContextUtils.checkWarehouseAccess(warehouseId);
        }
    }

    private void requireRule(Long ruleId)
    {
        if (ruleId == null || mapper.selectAlertRule(MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), ruleId) == null)
        {
            throw new ServiceException("告警规则不存在");
        }
    }

    private BigDecimal normalizeThreshold(MerchantInventoryAlertThresholdType type, BigDecimal value)
    {
        if (value == null)
        {
            throw new ServiceException("请输入告警阈值");
        }
        if (value.signum() < 0 || (type == MerchantInventoryAlertThresholdType.AVAILABLE_PERCENT
                && (value.signum() == 0 || value.compareTo(BigDecimal.valueOf(100)) > 0))
                || (type == MerchantInventoryAlertThresholdType.AVAILABLE_QUANTITY && value.stripTrailingZeros().scale() > 0))
        {
            throw new ServiceException(type == MerchantInventoryAlertThresholdType.AVAILABLE_PERCENT
                    ? "库存百分比必须大于0且不超过100" : "库存数量必须为不小于0的整数");
        }
        return value.stripTrailingZeros();
    }

    private String normalizeScopes(List<String> requested)
    {
        List<String> scopes = new ArrayList<>();
        if (requested != null)
        {
            for (String value : requested)
            {
                MerchantInventorySyncScope scope = MerchantInventorySyncScope.fromCode(value);
                if (scope == null)
                {
                    throw new ServiceException("存在不支持的同步授权范围");
                }
                if (!scopes.contains(scope.getCode()))
                {
                    scopes.add(scope.getCode());
                }
            }
        }
        if (scopes.isEmpty())
        {
            scopes.add(MerchantInventorySyncScope.INVENTORY_READ.getCode());
        }
        return String.join(",", scopes);
    }

    private void throttle(Long tokenId)
    {
        if (redisService == null || redisService.redisTemplate == null)
        {
            return;
        }
        String key = RATE_LIMIT_KEY_PREFIX + tokenId + ":" + (System.currentTimeMillis() / 1000);
        Long count = redisService.redisTemplate.opsForValue().increment(key, 1L);
        if (count != null && count == 1L)
        {
            redisService.expire(key, 2, TimeUnit.SECONDS);
        }
        if (count != null && count > RATE_LIMIT_PER_SECOND)
        {
            throw new ServiceException("库存同步请求过于频繁，请稍后重试");
        }
    }

    private Map<String, Object> buildSnapshotResponse(List<Map<String, Object>> rows, String requestId, boolean replayed)
    {
        long snapshotVersion = 0L;
        for (Map<String, Object> row : rows)
        {
            Long rowVersion = number(row.get("snapshotVersion"));
            if (rowVersion != null) snapshotVersion = Math.max(snapshotVersion, rowVersion);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("requestId", requestId);
        result.put("snapshotVersion", snapshotVersion);
        result.put("generatedAt", LocalDateTime.now());
        result.put("replayed", replayed);
        result.put("rows", rows);
        return result;
    }

    private List<Map<String, Object>> decorateRules(List<Map<String, Object>> rows)
    {
        for (Map<String, Object> row : rows)
        {
            MerchantInventoryAlertThresholdType type = MerchantInventoryAlertThresholdType.fromCode(integer(row.get("thresholdType")));
            MerchantInventoryAlertRuleStatus status = MerchantInventoryAlertRuleStatus.fromCode(integer(row.get("ruleStatus")));
            row.put("thresholdTypeLabel", type == null ? "未知" : type.getLabel());
            row.put("ruleStatusLabel", status == null ? "未知" : status.getLabel());
            row.put("notifyEnabledLabel", Boolean.valueOf(Integer.valueOf(1).equals(integer(row.get("notifyEnabled")))) ? "启用" : "不通知");
            if (row.get("warehouseName") == null) row.put("warehouseName", "全部仓库");
            if (row.get("productName") == null) row.put("productName", "全部商品");
        }
        return rows;
    }

    private List<Map<String, Object>> decorateEvents(List<Map<String, Object>> rows)
    {
        for (Map<String, Object> row : rows)
        {
            MerchantInventoryAlertStatus status = MerchantInventoryAlertStatus.fromCode(integer(row.get("alertStatus")));
            MerchantInventoryAlertThresholdType type = MerchantInventoryAlertThresholdType.fromCode(integer(row.get("thresholdType")));
            row.put("alertStatusLabel", status == null ? "未知" : status.getLabel());
            row.put("thresholdTypeLabel", type == null ? "未知" : type.getLabel());
        }
        return rows;
    }

    private List<Map<String, Object>> decorateTokens(List<Map<String, Object>> rows)
    {
        for (Map<String, Object> row : rows)
        {
            MerchantInventorySyncTokenStatus status = MerchantInventorySyncTokenStatus.fromCode(integer(row.get("tokenStatus")));
            row.put("tokenStatusLabel", status == null ? "未知" : status.getLabel());
            List<String> labels = new ArrayList<>();
            String scopes = string(row.get("scopes"));
            if (scopes != null)
            {
                for (String value : scopes.split(","))
                {
                    MerchantInventorySyncScope scope = MerchantInventorySyncScope.fromCode(value);
                    if (scope != null && !labels.contains(scope.getLabel()))
                    {
                        labels.add(scope.getLabel());
                    }
                }
            }
            row.put("scopesLabel", labels.isEmpty() ? "无" : String.join("、", labels));
        }
        return rows;
    }

    private List<Map<String, Object>> decorateSyncLogs(List<Map<String, Object>> rows)
    {
        if (rows == null)
        {
            return Collections.emptyList();
        }
        List<Map<String, Object>> decorated = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows)
        {
            Map<String, Object> item = row == null ? new LinkedHashMap<>() : new LinkedHashMap<>(row);
            boolean success = "SUCCESS".equalsIgnoreCase(string(item.get("resultCode")))
                    && Integer.valueOf(200).equals(integer(item.get("responseStatus")));
            item.put("resultLabel", success ? "成功" : "失败");
            decorated.add(item);
        }
        return decorated;
    }

    private boolean expired(Object value)
    {
        if (value == null) return false;
        if (value instanceof LocalDateTime time) return time.isBefore(LocalDateTime.now());
        return false;
    }

    private boolean containsScope(String scopes, MerchantInventorySyncScope expected)
    {
        if (scopes == null) return false;
        for (String value : scopes.split(","))
            if (expected == MerchantInventorySyncScope.fromCode(value)) return true;
        return false;
    }

    private String sha256(String value)
    {
        try
        {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException exception)
        {
            throw new IllegalStateException("SHA-256不可用", exception);
        }
    }

    private String trim(String value)
    {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private Integer integer(Object value)
    {
        if (value instanceof Number number) return number.intValue();
        if (value == null) return null;
        try { return Integer.valueOf(String.valueOf(value)); } catch (NumberFormatException ignored) { return null; }
    }

    private Long number(Object value)
    {
        if (value instanceof Number number) return number.longValue();
        if (value == null) return null;
        try { return Long.valueOf(String.valueOf(value)); } catch (NumberFormatException ignored) { return null; }
    }

    private Long number(Map<String, Object> value, String key)
    {
        return number(value == null ? null : value.get(key));
    }

    private Integer intValue(Object value)
    {
        return integer(value);
    }

    private LocalDateTime localDateTime(Object value)
    {
        return value instanceof LocalDateTime ? (LocalDateTime) value : null;
    }

    private boolean blank(String value)
    {
        return trim(value) == null;
    }

    private String string(Object value)
    {
        return value == null ? null : String.valueOf(value);
    }
}
