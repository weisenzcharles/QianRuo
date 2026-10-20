package com.ruoyi.rentalbusiness.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.enums.RentalCode;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.common.security.utils.MerchantContextUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.rentalbusiness.domain.LogisticsOrderTrackingRecord;
import com.ruoyi.rentalbusiness.domain.LogisticsProviderCallRecord;
import com.ruoyi.rentalbusiness.domain.LogisticsRouteRecord;
import com.ruoyi.rentalbusiness.domain.MerchantLogisticsListRecord;
import com.ruoyi.rentalbusiness.integration.express.ExpressProvider;
import com.ruoyi.rentalbusiness.integration.express.ExpressTrackNode;
import com.ruoyi.rentalbusiness.integration.express.ExpressTrackQuery;
import com.ruoyi.rentalbusiness.integration.express.ExpressTrackResult;
import com.ruoyi.rentalbusiness.integration.express.ExpressTrackingService;
import com.ruoyi.rentalbusiness.mapper.MerchantLogisticsRouteMapper;
import com.ruoyi.rentalbusiness.model.MerchantLogisticsListVO;
import com.ruoyi.rentalbusiness.model.MerchantLogisticsQuery;
import com.ruoyi.rentalbusiness.model.MerchantLogisticsTrackingVO;
import com.ruoyi.rentalbusiness.model.MerchantOrderCreateCommand;
import com.ruoyi.system.api.RemotePlatformGovernanceService;
import com.ruoyi.system.api.model.PlatformExternalInterfaceAlertCommand;

/** 商户订单物流轨迹应用服务，负责权限校验、缓存读取和供应商结果标准化。 */
@Service
public class MerchantLogisticsTrackingService
{
    private static final Logger log = LoggerFactory.getLogger(MerchantLogisticsTrackingService.class);

    private static final ZoneId BUSINESS_ZONE = DateUtils.DEFAULT_ZONE_ID;

    private static final DateTimeFormatter EVENT_TIME_FORMAT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(BUSINESS_ZONE);

    private static final Set<String> SHUNFENG_CODES = Set.of("sf", "sfexpress", "shunfeng", "顺丰", "顺丰快递",
            "顺丰物流");

    private static final Set<String> JD_KUAIDI100_CODES = Set.of(
            "jd", "jdexpress", "jingdong", "jingdongexpress", "京东", "京东快递", "京东物流");

    private static final int MAX_REFRESH_COUNT = 1000;
    private static final Long SYSTEM_OPERATOR_ID = 0L;
    private static final String LOCK_KEY = "rental-business:task:logistics-route:refresh";
    private static final long LOCK_MINUTES = 30L;
    private static final String PLATFORM_ALERT_KEY_PREFIX = "LOGISTICS_EXTERNAL_INTERFACE:";

    private final MerchantOrderService orderService;
    private final ExpressTrackingService trackingService;
    private final MerchantLogisticsRouteMapper routeMapper;
    private final LogisticsRouteSnapshotService snapshotService;
    private final RedisService redisService;
    private final LogisticsProviderRateLimiter providerRateLimiter;
    private final RemotePlatformGovernanceService platformGovernanceService;

    /**
     * 注入订单权限校验、供应商门面、路线缓存 Mapper、快照事务服务和 Redis 任务锁。
     *
     * @param orderService 商户订单服务
     * @param trackingService 统一快递轨迹查询门面
     * @param routeMapper 物流路线缓存数据访问
     * @param snapshotService 物流路线快照事务写入服务
     * @param redisService 分布式任务锁服务
     * @param providerRateLimiter 物流供应商全站限流器
     */
    @Autowired
    public MerchantLogisticsTrackingService(MerchantOrderService orderService,
            ExpressTrackingService trackingService, MerchantLogisticsRouteMapper routeMapper,
            LogisticsRouteSnapshotService snapshotService, RedisService redisService,
            LogisticsProviderRateLimiter providerRateLimiter,
            RemotePlatformGovernanceService platformGovernanceService)
    {
        this.orderService = orderService;
        this.trackingService = trackingService;
        this.routeMapper = routeMapper;
        this.snapshotService = snapshotService;
        this.redisService = redisService;
        this.providerRateLimiter = providerRateLimiter;
        this.platformGovernanceService = platformGovernanceService;
    }

    /** 保留供应商路由单元测试使用的历史构造器。 */
    public MerchantLogisticsTrackingService(MerchantOrderService orderService,
            ExpressTrackingService trackingService)
    {
        this(orderService, trackingService, null, null, null, null, null);
    }

    /**
     * 查询当前商户订单指定方向的物流轨迹。
     *
     * <p>首次保存物流信息后会先初始化查询；如果历史数据尚未初始化，首次查看时也会调用对应
     * 供应商并把标准化结果写入物流主记录和路线表。只要存在查询快照，后续请求直接读取数据库。
     * 快递信息被修改时由订单服务清理快照，因此不会把旧单号的路线返回给商户。</p>
     *
     * @param orderId 订单唯一编号
     * @param direction 物流方向，只允许发货或归还
     * @return 标准化轨迹响应
     */
    public MerchantLogisticsTrackingVO track(Long orderId, Integer direction)
    {
        return track(orderId, direction, false);
    }

    /** 查询当前商户订单编码对应的物流轨迹，避免页面暴露订单唯一编号。 */
    public MerchantLogisticsTrackingVO trackByOrderNo(String orderNo, Integer direction, boolean forceRefresh)
    {
        if (isBlank(orderNo))
        {
            throw new ServiceException("订单编码不能为空");
        }
        if (!isTrackableDirection(direction))
        {
            throw new ServiceException("物流方向不合法");
        }
        Long orderId = routeMapper.selectOrderIdByOrderNo(MerchantContextUtils.getTenantId(),
                MerchantContextUtils.getMemberId(), orderNo.trim());
        if (orderId == null)
        {
            throw new ServiceException("订单不存在或无权查看物流");
        }
        return track(orderId, direction, forceRefresh);
    }

    /** 分页查询当前商户可见的物流治理台账。 */
    public List<MerchantLogisticsListVO> list(MerchantLogisticsQuery query)
    {
        MerchantLogisticsQuery actualQuery = query == null ? new MerchantLogisticsQuery() : query;
        actualQuery.setKeyword(normalizeKeyword(actualQuery.getKeyword()));
        actualQuery.setDirection(normalizeDirectionFilter(actualQuery.getDirection()));
        actualQuery.setRouteStatus(normalizeRouteStatusFilter(actualQuery.getRouteStatus()));
        actualQuery.setAllWarehouseAccess(MerchantContextUtils.hasAllWarehouseAccess());
        actualQuery.setWarehouseIds(MerchantContextUtils.getWarehouseIds());
        List<MerchantLogisticsListRecord> rows = routeMapper.selectMerchantLogisticsList(
                MerchantContextUtils.getTenantId(), MerchantContextUtils.getMemberId(), actualQuery);
        if (rows == null || rows.isEmpty())
        {
            return Collections.emptyList();
        }
        List<MerchantLogisticsListVO> result = new ArrayList<MerchantLogisticsListVO>(rows.size());
        for (MerchantLogisticsListRecord row : rows)
        {
            result.add(toListView(row));
        }
        return result;
    }

    private MerchantLogisticsTrackingVO track(Long orderId, Integer direction, boolean forceRefresh)
    {
        if (!isTrackableDirection(direction))
        {
            throw new ServiceException("物流方向不合法");
        }
        MerchantOrderCreateCommand order = orderService.get(orderId);
        Long tenantId = MerchantContextUtils.getTenantId();
        Long memberId = MerchantContextUtils.getMemberId();
        LogisticsOrderTrackingRecord logistics = routeMapper.selectCurrentLogistics(tenantId, memberId, orderId,
                direction);
        if (logistics == null)
        {
            throw new ServiceException(RentalCode.DeliveryDirection.RETURN.getCode().equals(direction)
                    ? "当前订单没有已保存的归还物流信息，请先保存快递公司和快递单号"
                    : "当前订单没有已保存的寄出物流信息，请先保存快递公司和快递单号");
        }
        String carrier = logistics.carrier;
        String trackingNo = logistics.trackingNo;
        if (isBlank(trackingNo))
        {
            throw new ServiceException(RentalCode.DeliveryDirection.RETURN.getCode().equals(direction)
                    ? "当前订单未填写归还快递单号" : "当前订单未填写寄出快递单号");
        }
        if (isBlank(carrier))
        {
            throw new ServiceException(RentalCode.DeliveryDirection.RETURN.getCode().equals(direction)
                    ? "当前订单未填写归还快递公司" : "当前订单未填写寄出快递公司");
        }

        String directionLabel = RentalCode.DeliveryDirection.RETURN.getCode().equals(direction) ? "归还" : "寄出";
        if (!forceRefresh && logistics.routeQueryTime != null)
        {
            // 联盟原订单可能读取接收商户的子订单物流，缓存读取必须使用物流记录所属商户。
            List<LogisticsRouteRecord> routes = routeMapper.selectCachedRoutes(logistics.tenantId, logistics.memberId,
                    logistics.logisticsId);
            return toCachedView(logistics, directionLabel, routes);
        }

        ExpressProvider provider = resolveProvider(carrier);
        ExpressTrackResult result = querySafely(provider, trackingNo, carrier, order.recipientPhone,
                logistics.destinationAddress,
                logistics.tenantId, logistics.memberId, logistics.orderId, logistics.logisticsId, logistics.direction,
                SecurityUtils.getUserId());
        synchronizePlatformAlert(logistics.logisticsId, carrier, trackingNo, result);
        provider = result.provider() == null ? provider : result.provider();
        RentalCode.LogisticsRouteStatus routeStatus = resolveRouteStatus(provider, result);
        List<LogisticsRouteRecord> routeNodes = toRouteRecords(logistics.logisticsId, trackingNo, provider,
                result.nodes());
        String routeMessage = truncate(result.message(), 500);
        snapshotService.saveSnapshot(logistics, routeStatus.getCode(),
                routeStatus == RentalCode.LogisticsRouteStatus.ABNORMAL ? 1 : 0,
                result.available() ? 1 : 0, provider.name().toLowerCase(Locale.ROOT), routeMessage, routeNodes,
                result.estimatedArriveTime(), SecurityUtils.getUserId());
        return toView(logistics.orderNo, directionLabel, carrier, trackingNo, provider, routeStatus, result);
    }

    private MerchantLogisticsListVO toListView(MerchantLogisticsListRecord row)
    {
        MerchantLogisticsListVO view = new MerchantLogisticsListVO();
        view.logisticsId = row.logisticsId;
        view.orderId = row.orderId;
        view.orderNo = row.orderNo;
        view.direction = row.direction;
        view.directionName = directionLabel(row.direction);
        view.carrier = row.carrier;
        view.trackingNo = row.trackingNo;
        view.sourceType = row.sourceType;
        view.sourceTypeName = sourceTypeLabel(row.sourceType);
        view.pickupTime = row.pickupTime;
        view.estimatedArriveTime = row.estimatedArriveTime;
        view.routeStatus = row.routeStatus;
        view.routeStatusName = routeStatusLabel(row.routeStatus);
        view.abnormalFlag = row.abnormalFlag;
        view.routeAvailable = row.routeAvailable;
        view.routeProvider = row.routeProvider;
        view.routeMessage = row.routeMessage;
        view.routeQueryTime = row.routeQueryTime;
        view.lastRouteTime = row.lastRouteTime;
        view.warehouseName = row.warehouseName;
        view.feeAmount = row.feeAmount;
        view.updateTime = row.updateTime;
        return view;
    }

    private Integer normalizeDirectionFilter(Integer direction)
    {
        if (direction == null)
        {
            return null;
        }
        for (RentalCode.DeliveryDirection item : RentalCode.DeliveryDirection.values())
        {
            if (item.getCode().equals(direction))
            {
                return direction;
            }
        }
        throw new ServiceException("物流方向筛选条件不合法");
    }

    private Integer normalizeRouteStatusFilter(Integer routeStatus)
    {
        if (routeStatus == null)
        {
            return null;
        }
        for (RentalCode.LogisticsRouteStatus item : RentalCode.LogisticsRouteStatus.values())
        {
            if (item.getCode().equals(routeStatus))
            {
                return routeStatus;
            }
        }
        throw new ServiceException("物流状态筛选条件不合法");
    }

    private String normalizeKeyword(String keyword)
    {
        if (isBlank(keyword))
        {
            return null;
        }
        String normalized = keyword.trim();
        return normalized.length() > 128 ? normalized.substring(0, 128) : normalized;
    }

    private String directionLabel(Integer code)
    {
        if (code != null)
        {
            for (RentalCode.DeliveryDirection item : RentalCode.DeliveryDirection.values())
            {
                if (item.getCode().equals(code))
                {
                    return item.getLabel();
                }
            }
        }
        return "未知方向";
    }

    private String sourceTypeLabel(Integer code)
    {
        if (code != null)
        {
            for (RentalCode.LogisticsSourceType item : RentalCode.LogisticsSourceType.values())
            {
                if (item.getCode().equals(code))
                {
                    return item.getLabel();
                }
            }
        }
        return "未知来源";
    }

    /**
     * 在订单新增或编辑事务提交后，初始化当前订单已有的寄出和归还物流路线。
     *
     * <p>调用方必须在订单写事务返回后调用本方法，避免在数据库事务内执行第三方网络请求。
     * 已经存在快照的方向只读数据库，不会重复请求供应商；供应商不可用时仍会写入不可用快照，
     * 后续由六小时刷新任务重试。</p>
     *
     * @param orderId 订单唯一编号
     */
    public void initializeRouteSnapshots(Long orderId)
    {
        if (orderId == null)
        {
            return;
        }
        MerchantOrderCreateCommand order;
        try
        {
            order = orderService.get(orderId);
        }
        catch (RuntimeException exception)
        {
            log.warn("初始化订单物流路线失败，orderId={}, message={}", orderId, exception.getMessage());
            return;
        }
        primeRouteSnapshot(orderId, RentalCode.DeliveryDirection.SHIP.getCode(), order.expressCompany,
                order.expressTrackingNo);
        primeRouteSnapshot(orderId, RentalCode.DeliveryDirection.RETURN.getCode(), order.returnExpressCompany,
                order.returnExpressTrackingNo);
    }

    private void primeRouteSnapshot(Long orderId, Integer direction, String carrier, String trackingNo)
    {
        if (isBlank(carrier) || isBlank(trackingNo))
        {
            return;
        }
        try
        {
            track(orderId, direction);
        }
        catch (RuntimeException exception)
        {
            // 订单已经完成保存，路线预取失败交给后续六小时任务重试，不能回滚订单写入。
            log.warn("初始化订单物流路线失败，orderId={}, direction={}, message={}", orderId, direction,
                    exception.getMessage());
        }
    }

    /**
     * 刷新全部达到间隔且尚未签收的物流路线。
     *
     * <p>该方法只由 ruoyi-job 通过内部接口调用，不依赖商户登录上下文；单条供应商失败会记录
     * 不可用快照并继续处理其他物流，避免一个异常单号阻塞全量刷新。</p>
     *
     * @return 本次成功写入快照的物流记录数量
     */
    public int refreshUnreceivedRoutes()
    {
        String lockToken = UUID.randomUUID().toString();
        if (!tryAcquireLock(lockToken))
        {
            log.info("物流路线刷新任务已由其他实例执行");
            return 0;
        }
        int refreshed = 0;
        try
        {
            List<LogisticsOrderTrackingRecord> targets = routeMapper.selectRefreshTargets(MAX_REFRESH_COUNT);
            if (targets == null)
            {
                return 0;
            }
            for (LogisticsOrderTrackingRecord target : targets)
            {
                if (target == null || isBlank(target.carrier) || isBlank(target.trackingNo))
                {
                    continue;
                }
                try
                {
                    refreshTarget(target, SYSTEM_OPERATOR_ID);
                    refreshed++;
                }
                catch (RuntimeException exception)
                {
                    log.error("刷新物流路线失败，logisticsId={}, trackingNo={}, message={}", target.logisticsId,
                            maskTrackingNo(target.trackingNo), exception.getMessage(), exception);
                }
            }
            return refreshed;
        }
        finally
        {
            releaseLock(lockToken);
        }
    }

    /** 手工刷新单条物流路线，复用批量任务的供应商路由、限流、审计和快照写入逻辑。 */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public int refreshRoute(Long logisticsId)
    {
        if (logisticsId == null || logisticsId.longValue() <= 0L)
        {
            throw new ServiceException("物流唯一编号不合法");
        }
        if (routeMapper == null || snapshotService == null)
        {
            throw new ServiceException("物流刷新服务未初始化");
        }
        LogisticsOrderTrackingRecord target = routeMapper.selectLogisticsForRefresh(logisticsId);
        if (target == null)
        {
            throw new ServiceException("物流记录不存在或已删除");
        }
        if (isBlank(target.carrier) || isBlank(target.trackingNo))
        {
            throw new ServiceException("当前物流没有完整的快递公司和单号");
        }
        Long operatorId = SecurityUtils.getUserId();
        refreshTarget(target, operatorId == null ? SYSTEM_OPERATOR_ID : operatorId);
        return 1;
    }

    private void refreshTarget(LogisticsOrderTrackingRecord target, Long operatorId)
    {
        ExpressProvider provider = resolveProvider(target.carrier);
        ExpressTrackResult result = querySafely(provider, target.trackingNo, target.carrier,
                target.recipientPhone, target.destinationAddress, target.tenantId, target.memberId, target.orderId,
                target.logisticsId, target.direction, operatorId);
        synchronizePlatformAlert(target.logisticsId, target.carrier, target.trackingNo, result);
        provider = result.provider() == null ? provider : result.provider();
        RentalCode.LogisticsRouteStatus routeStatus = resolveRouteStatus(provider, result);
        String routeMessage = truncate(result.message(), 500);
        snapshotService.saveSnapshot(target, routeStatus.getCode(),
                routeStatus == RentalCode.LogisticsRouteStatus.ABNORMAL ? 1 : 0,
                result.available() ? 1 : 0, provider.name().toLowerCase(Locale.ROOT), routeMessage,
                toRouteRecords(target.logisticsId, target.trackingNo, provider, result.nodes()),
                result.estimatedArriveTime(), operatorId);
    }

    private ExpressTrackResult querySafely(ExpressProvider provider, String trackingNo, String carrier,
            String recipientPhone, String destinationAddress, Long tenantId, Long memberId, Long orderId,
            Long logisticsId, Integer direction, Long operatorId)
    {
        ExpressTrackResult primaryResult = queryProvider(provider, trackingNo, carrier, recipientPhone,
                destinationAddress, tenantId, memberId, orderId, logisticsId, direction, operatorId);
        if (primaryResult.available())
        {
            return primaryResult;
        }

        ExpressProvider fallback = fallbackProvider(provider);
        if (fallback == null)
        {
            return primaryResult;
        }
        ExpressTrackResult fallbackResult = queryProvider(fallback, trackingNo, carrier, recipientPhone,
                destinationAddress, tenantId, memberId, orderId, logisticsId, direction, operatorId);
        return fallbackResult.available() ? fallbackResult : mergeUnavailableResult(fallbackResult, primaryResult);
    }

    private ExpressTrackResult queryProvider(ExpressProvider provider, String trackingNo, String carrier,
            String recipientPhone, String destinationAddress, Long tenantId, Long memberId, Long orderId,
            Long logisticsId, Integer direction, Long operatorId)
    {
        ExpressTrackQuery query = new ExpressTrackQuery(provider, trackingNo,
                provider == ExpressProvider.KUAIDI100 ? resolveKuaidi100ShipperCode(carrier) : null,
                provider == ExpressProvider.SF ? recipientPhone : null, destinationAddress);
        long startNanos = System.nanoTime();
        ExpressTrackResult result;
        String errorType = null;
        if (providerRateLimiter != null && !providerRateLimiter.tryAcquire(provider))
        {
            result = ExpressTrackResult.unavailable(provider, trackingNo, "物流查询已进入队列，请稍候刷新");
            errorType = "rate_limited";
        }
        else
        {
            try
            {
                result = trackingService.query(query);
                result = result == null
                        ? ExpressTrackResult.unavailable(provider, query.trackingNo(), "物流轨迹查询失败") : result;
                if (!result.available())
                {
                    errorType = "provider_unavailable";
                }
            }
            catch (RuntimeException exception)
            {
                errorType = exception.getClass().getSimpleName();
                log.warn("物流供应商查询异常，provider={}, trackingNo={}, message={}", provider,
                        maskTrackingNo(trackingNo), exception.getMessage());
                result = ExpressTrackResult.unavailable(provider, trackingNo, "物流轨迹查询失败");
            }
        }
        recordProviderCall(tenantId, memberId, orderId, logisticsId, direction, provider, carrier, trackingNo,
                result, errorType, operatorId, (System.nanoTime() - startNanos) / 1_000_000L);
        return result;
    }

    /**
     * 顺丰作为主路由不可用时使用快递100兜底；京东等其他快递本身已经通过快递100查询，避免重复调用。
     */
    static ExpressProvider fallbackProvider(ExpressProvider primary)
    {
        return primary == ExpressProvider.SF ? ExpressProvider.KUAIDI100 : null;
    }

    /** 物流供应商异常告警按物流记录幂等，避免同一单号每次刷新产生新事件。 */
    static String logisticsAlertKey(Long logisticsId)
    {
        return logisticsId == null ? null : PLATFORM_ALERT_KEY_PREFIX + logisticsId;
    }

    /**
     * 将物流供应商不可用状态同步到平台告警中心；平台治理不可用时只记录日志，不阻断物流查询。
     */
    private void synchronizePlatformAlert(Long logisticsId, String carrier, String trackingNo,
            ExpressTrackResult result)
    {
        if (platformGovernanceService == null || logisticsId == null || result == null)
        {
            return;
        }
        String alertKey = logisticsAlertKey(logisticsId);
        try
        {
            if (result.available())
            {
                R<Void> response = platformGovernanceService.resolveExternalInterfaceAlert(alertKey,
                        SecurityConstants.INNER);
                if (response != null && !R.isSuccess(response))
                {
                    log.warn("物流外部接口告警解除失败，logisticsId={}, message={}", logisticsId, response.getMsg());
                }
                return;
            }

            PlatformExternalInterfaceAlertCommand command = new PlatformExternalInterfaceAlertCommand();
            command.setAlertEventKey(alertKey);
            command.setAlertLevel(Integer.valueOf(3));
            command.setTitle("物流供应商查询异常：" + truncate(carrier, 120));
            command.setDetail(truncate("物流单号 " + maskTrackingNo(trackingNo) + " 查询失败："
                    + (isBlank(result.message()) ? "供应商未返回有效轨迹" : result.message()), 1000));
            command.setSourceKey("logistics:" + logisticsId);
            R<Void> response = platformGovernanceService.upsertExternalInterfaceAlert(command,
                    SecurityConstants.INNER);
            if (response != null && !R.isSuccess(response))
            {
                log.warn("物流外部接口告警上报失败，logisticsId={}, message={}", logisticsId, response.getMsg());
            }
        }
        catch (RuntimeException exception)
        {
            log.warn("同步物流外部接口告警异常，logisticsId={}, message={}", logisticsId, exception.getMessage());
        }
    }

    private ExpressTrackResult mergeUnavailableResult(ExpressTrackResult fallbackResult,
            ExpressTrackResult primaryResult)
    {
        String primaryMessage = primaryResult == null ? null : primaryResult.message();
        String fallbackMessage = fallbackResult == null ? null : fallbackResult.message();
        String unavailableMessage = firstNonBlank(fallbackMessage, primaryMessage);
        String message = "主路由未返回有效轨迹；次路由："
                + (isBlank(unavailableMessage) ? "物流轨迹查询失败" : unavailableMessage);
        ExpressProvider provider = fallbackResult == null || fallbackResult.provider() == null
                ? primaryResult == null ? null : primaryResult.provider() : fallbackResult.provider();
        String tracking = fallbackResult == null || fallbackResult.trackingNo() == null
                ? primaryResult == null ? null : primaryResult.trackingNo() : fallbackResult.trackingNo();
        return new ExpressTrackResult(false, provider, tracking,
                fallbackResult == null ? null : fallbackResult.status(),
                fallbackResult == null ? List.of() : fallbackResult.nodes(), message);
    }

    /** 记录供应商调用结果，审计写入失败不能阻断物流查询主流程。 */
    private void recordProviderCall(Long tenantId, Long memberId, Long orderId, Long logisticsId,
            Integer direction, ExpressProvider provider, String carrier, String trackingNo,
            ExpressTrackResult result, String errorType, Long operatorId, long durationMs)
    {
        if (routeMapper == null)
        {
            return;
        }
        LogisticsProviderCallRecord record = new LogisticsProviderCallRecord();
        record.tenantId = tenantId;
        record.memberId = memberId;
        record.orderId = orderId;
        record.logisticsId = logisticsId;
        record.direction = direction;
        record.provider = provider == null ? null : provider.name().toLowerCase(Locale.ROOT);
        record.carrier = truncate(carrier, 128);
        record.trackingNoMasked = maskTrackingNo(trackingNo);
        record.trackingNoHash = hashTrackingNo(trackingNo);
        record.available = result != null && result.available() ? 1 : 0;
        record.providerStatus = result == null ? null : truncate(result.status(), 64);
        record.routeStatus = result == null ? null : resolveRouteStatus(provider, result).getCode();
        record.responseMessage = result == null ? "物流轨迹查询失败" : truncate(result.message(), 500);
        record.durationMs = durationMs;
        record.errorType = truncate(errorType, 64);
        record.operatorId = operatorId;
        try
        {
            routeMapper.insertProviderCallLog(record);
        }
        catch (RuntimeException exception)
        {
            log.warn("物流供应商调用审计写入失败，logisticsId={}, message={}", logisticsId,
                    exception.getMessage());
        }
    }

    private String hashTrackingNo(String trackingNo)
    {
        if (isBlank(trackingNo))
        {
            return null;
        }
        try
        {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(trackingNo.trim().getBytes(StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException exception)
        {
            throw new IllegalStateException("SHA-256不可用", exception);
        }
    }

    private boolean isTrackableDirection(Integer direction)
    {
        return RentalCode.DeliveryDirection.SHIP.getCode().equals(direction)
                || RentalCode.DeliveryDirection.RETURN.getCode().equals(direction);
    }

    /** 顺丰使用专用客户端，京东及其他快递公司统一通过快递100查询。 */
    static ExpressProvider resolveProvider(String carrier)
    {
        String normalized = carrier.trim().toLowerCase(Locale.ROOT);
        if (SHUNFENG_CODES.contains(normalized))
        {
            return ExpressProvider.SF;
        }
        return ExpressProvider.KUAIDI100;
    }

    /** 将字典中的京东展示名称转换为快递100要求的公司编码。 */
    static String resolveKuaidi100ShipperCode(String carrier)
    {
        String normalized = carrier.trim().toLowerCase(Locale.ROOT);
        return JD_KUAIDI100_CODES.contains(normalized) ? "jd" : normalized;
    }

    /** 将供应商状态归一化为物流表约定的数字枚举。 */
    static RentalCode.LogisticsRouteStatus resolveRouteStatus(ExpressProvider provider, ExpressTrackResult result)
    {
        if (result == null || !result.available())
        {
            return RentalCode.LogisticsRouteStatus.PENDING;
        }
        if (isDelivered(provider, result.status(), null))
        {
            return RentalCode.LogisticsRouteStatus.DELIVERED;
        }
        if (isAbnormal(provider, result.status(), null))
        {
            return RentalCode.LogisticsRouteStatus.ABNORMAL;
        }
        for (ExpressTrackNode node : safeNodes(result.nodes()))
        {
            if (node != null && isDelivered(provider, node.status(), node.description()))
            {
                return RentalCode.LogisticsRouteStatus.DELIVERED;
            }
        }
        for (ExpressTrackNode node : safeNodes(result.nodes()))
        {
            if (node != null && isAbnormal(provider, node.status(), node.description()))
            {
                return RentalCode.LogisticsRouteStatus.ABNORMAL;
            }
        }
        return RentalCode.LogisticsRouteStatus.IN_TRANSIT;
    }

    private static RentalCode.LogisticsRouteStatus resolveNodeStatus(ExpressProvider provider,
            ExpressTrackNode node)
    {
        if (node == null)
        {
            return RentalCode.LogisticsRouteStatus.IN_TRANSIT;
        }
        if (isDelivered(provider, node.status(), node.description()))
        {
            return RentalCode.LogisticsRouteStatus.DELIVERED;
        }
        if (isAbnormal(provider, node.status(), node.description()))
        {
            return RentalCode.LogisticsRouteStatus.ABNORMAL;
        }
        return RentalCode.LogisticsRouteStatus.IN_TRANSIT;
    }

    private static boolean isDelivered(ExpressProvider provider, String status, String description)
    {
        String code = status == null ? "" : status.trim();
        if (ExpressProvider.KUAIDI100 == provider && "3".equals(code))
        {
            return true;
        }
        if (ExpressProvider.SF == provider && "80".equals(code))
        {
            return true;
        }
        String text = statusText(status, description);
        if (text.contains("未签收") || text.contains("未妥投"))
        {
            return false;
        }
        return containsAny(text, "已签收", "签收", "已送达", "妥投", "delivered", "signed");
    }

    private static boolean isAbnormal(ExpressProvider provider, String status, String description)
    {
        String code = status == null ? "" : status.trim();
        if (ExpressProvider.KUAIDI100 == provider && Set.of("2", "4", "6").contains(code))
        {
            return true;
        }
        if (ExpressProvider.SF == provider && "99".equals(code))
        {
            return true;
        }
        return containsAny(statusText(status, description), "异常", "疑难", "退回", "拒收", "exception", "abnormal",
                "returned");
    }

    private static String statusText(String status, String description)
    {
        return ((status == null ? "" : status) + " " + (description == null ? "" : description))
                .toLowerCase(Locale.ROOT);
    }

    private static boolean containsAny(String value, String... candidates)
    {
        for (String candidate : candidates)
        {
            if (value.contains(candidate.toLowerCase(Locale.ROOT)))
            {
                return true;
            }
        }
        return false;
    }

    private List<LogisticsRouteRecord> toRouteRecords(Long logisticsId, String trackingNo, ExpressProvider provider,
            List<ExpressTrackNode> nodes)
    {
        List<LogisticsRouteRecord> records = new ArrayList<>();
        for (ExpressTrackNode node : safeNodes(nodes))
        {
            if (node == null)
            {
                continue;
            }
            LogisticsRouteRecord record = new LogisticsRouteRecord();
            record.logisticsId = logisticsId;
            record.trackingNo = trackingNo;
            record.routeStatus = resolveNodeStatus(provider, node).getCode();
            record.routeDesc = truncate(node.description(), 1000);
            record.routeTime = toLocalDateTime(node.time());
            // 当前路线表没有单独的原始位置字段，city 保存供应商返回的位置快照供轨迹弹窗展示。
            record.city = truncate(node.location(), 64);
            records.add(record);
        }
        return records;
    }

    private MerchantLogisticsTrackingVO toView(String orderNo, String direction, String carrier,
            String trackingNo, ExpressProvider provider, RentalCode.LogisticsRouteStatus routeStatus,
            ExpressTrackResult result)
    {
        MerchantLogisticsTrackingVO view = new MerchantLogisticsTrackingVO();
        view.orderNo = orderNo;
        view.direction = direction;
        view.carrier = carrier;
        view.trackingNo = trackingNo.trim();
        view.available = result.available();
        view.status = routeStatus.getLabel();
        view.message = result.message();
        view.events = safeNodes(result.nodes()).stream().map(node -> toEvent(provider, node))
                .collect(Collectors.toList());
        return view;
    }

    private MerchantLogisticsTrackingVO toCachedView(LogisticsOrderTrackingRecord logistics, String direction,
            List<LogisticsRouteRecord> routes)
    {
        MerchantLogisticsTrackingVO view = new MerchantLogisticsTrackingVO();
        view.orderNo = logistics.orderNo;
        view.direction = direction;
        view.carrier = logistics.carrier;
        view.trackingNo = logistics.trackingNo;
        view.available = Integer.valueOf(1).equals(logistics.routeAvailable);
        view.status = routeStatusLabel(logistics.routeStatus);
        view.message = logistics.routeMessage;
        view.events = (routes == null ? Collections.<LogisticsRouteRecord>emptyList() : routes).stream()
                .map(this::toCachedEvent).collect(Collectors.toList());
        return view;
    }

    private MerchantLogisticsTrackingVO.Event toEvent(ExpressProvider provider, ExpressTrackNode node)
    {
        MerchantLogisticsTrackingVO.Event event = new MerchantLogisticsTrackingVO.Event();
        event.time = formatTime(node == null ? null : node.time());
        event.status = resolveNodeStatus(provider, node).getLabel();
        event.description = node == null ? null : node.description();
        event.location = node == null ? null : node.location();
        return event;
    }

    private MerchantLogisticsTrackingVO.Event toCachedEvent(LogisticsRouteRecord route)
    {
        MerchantLogisticsTrackingVO.Event event = new MerchantLogisticsTrackingVO.Event();
        event.time = formatTime(route.routeTime);
        event.status = routeStatusLabel(route.routeStatus);
        event.description = route.routeDesc;
        event.location = firstNonBlank(route.city, route.province);
        return event;
    }

    private String routeStatusLabel(Integer code)
    {
        if (code != null)
        {
            for (RentalCode.LogisticsRouteStatus status : RentalCode.LogisticsRouteStatus.values())
            {
                if (status.getCode().equals(code))
                {
                    return status.getLabel();
                }
            }
        }
        return RentalCode.LogisticsRouteStatus.PENDING.getLabel();
    }

    private String formatTime(OffsetDateTime time)
    {
        return time == null ? null : EVENT_TIME_FORMAT.format(time);
    }

    private String formatTime(LocalDateTime time)
    {
        return time == null ? null : time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private LocalDateTime toLocalDateTime(OffsetDateTime time)
    {
        return time == null ? null : time.atZoneSameInstant(BUSINESS_ZONE).toLocalDateTime();
    }

    private static List<ExpressTrackNode> safeNodes(List<ExpressTrackNode> nodes)
    {
        return nodes == null ? Collections.emptyList() : nodes;
    }

    private static String truncate(String value, int maxLength)
    {
        if (value == null || value.length() <= maxLength)
        {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private static String firstNonBlank(String first, String second)
    {
        return !isBlank(first) ? first : (!isBlank(second) ? second : null);
    }

    private static boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }

    private String maskTrackingNo(String value)
    {
        if (value == null || value.length() <= 4)
        {
            return "****";
        }
        return value.substring(0, Math.min(2, value.length())) + "****"
                + value.substring(value.length() - 4);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean tryAcquireLock(String lockToken)
    {
        if (redisService == null)
        {
            return true;
        }
        RedisTemplate redisTemplate = redisService.redisTemplate;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, lockToken, LOCK_MINUTES,
                TimeUnit.MINUTES);
        return Boolean.TRUE.equals(acquired);
    }

    private void releaseLock(String lockToken)
    {
        if (redisService == null)
        {
            return;
        }
        Object currentToken = redisService.getCacheObject(LOCK_KEY);
        if (lockToken.equals(currentToken == null ? null : String.valueOf(currentToken)))
        {
            redisService.deleteObject(LOCK_KEY);
        }
    }
}
