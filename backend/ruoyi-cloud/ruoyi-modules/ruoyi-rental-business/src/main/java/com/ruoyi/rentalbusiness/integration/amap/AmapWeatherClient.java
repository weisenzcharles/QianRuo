package com.ruoyi.rentalbusiness.integration.amap;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.GlobalConfigConstants;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.rentalbusiness.config.GlobalConfigService;

/**
 * 高德地图天气通用客户端。
 *
 * <p>高德天气接口的 city 参数只接受行政区划 adcode，调用方必须先通过统一行政区划解析器得到
 * 市级 adcode，不能把行政区划表中的 citycode 或区县 adcode 直接传入。接口密钥从平台全局配置
 * 读取，查询结果按市级 adcode 缓存一小时，避免订单列表重复调用高德。</p>
 */
@Service
public class AmapWeatherClient
{
    private static final Logger log = LoggerFactory.getLogger(AmapWeatherClient.class);

    private static final String WEATHER_CACHE_PREFIX = "weather:amap:";

    private static final String FORECAST_CACHE_PREFIX = "weather:amap:forecast:";

    /** 多个高德 Key 共用的外部请求计数键，使用 Redis 原子递增支持多实例轮换。 */
    private static final String API_KEY_CALL_COUNT_KEY = "weather:amap:api-key:call-count";

    /** 每完成 5000 次真实高德请求切换到下一个配置的 Key。 */
    private static final long API_KEY_ROTATION_CALLS = 5000L;

    /**
     * 高德天气免费接口通常按 QPS 限制请求频率；两次请求之间保留 500ms，避免批量刷新撞上限流。
     * 该限制同时作用于实时天气和预报请求，避免订单页面的即时查询挤占定时任务配额。
     */
    private static final long AMAP_REQUEST_INTERVAL_MILLIS = 500L;

    /** 高德返回 QPS 超限时最多进行三次短退避重试。 */
    private static final int MAX_RATE_LIMIT_RETRIES = 3;

    /** 高德返回 QPS 超限后的指数退避时长。 */
    private static final long RATE_LIMIT_RETRY_BASE_MILLIS = 1000L;

    private final GlobalConfigService globalConfigService;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final Object requestRateLock = new Object();
    private final AtomicLong fallbackApiCallCounter = new AtomicLong();
    private final AtomicBoolean fallbackCounterWarningLogged = new AtomicBoolean();
    private long nextRequestAtNanos;

    /**
     * 注入全局配置、缓存和 JSON 解析器，并创建带连接超时的 JDK HTTP 客户端。
     *
     * @param globalConfigService 全局配置读取服务
     * @param redisService Redis 服务
     * @param objectMapper Jackson JSON 解析器
     */
    public AmapWeatherClient(GlobalConfigService globalConfigService, RedisService redisService,
            ObjectMapper objectMapper)
    {
        this.globalConfigService = globalConfigService;
        this.redisService = redisService;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        this.nextRequestAtNanos = 0L;
    }

    /**
     * 按行政区划 adcode 查询天气。
     *
     * @param adcode 市级行政区划 adcode
     * @return 标准天气快照；未配置密钥、接口异常或城市编码为空时返回不可用结果
     */
    public AmapWeatherSnapshot query(String adcode)
    {
        if (adcode == null || adcode.trim().isEmpty())
        {
            return AmapWeatherSnapshot.unavailable(adcode, "缺少天气查询 adcode");
        }
        String normalizedAdcode = adcode.trim();
        String cacheKey = WEATHER_CACHE_PREFIX + normalizedAdcode;
        AmapWeatherSnapshot cached = readCached(cacheKey);
        if (cached != null)
        {
            return cached;
        }

        try
        {
            HttpResponse<String> response = requestWeather(normalizedAdcode, "all");
            if (response == null)
            {
                return AmapWeatherSnapshot.unavailable(normalizedAdcode, "平台未配置高德地图 API Key 列表");
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                log.warn("高德天气接口返回 HTTP {}，adcode={}", response.statusCode(), normalizedAdcode);
                return AmapWeatherSnapshot.unavailable(normalizedAdcode, "高德天气服务暂不可用");
            }
            AmapWeatherSnapshot snapshot = parseResponse(normalizedAdcode, response.body());
            if (snapshot.available)
            {
                writeCached(cacheKey, snapshot);
            }
            return snapshot;
        }
        catch (Exception exception)
        {
            log.warn("调用高德天气接口失败，adcode={}, message={}", normalizedAdcode, exception.getMessage());
            return AmapWeatherSnapshot.unavailable(normalizedAdcode, "高德天气查询失败");
        }
    }

    /**
     * 一次查询指定城市的完整天气预报列表。
     *
     * <p>行政区划天气任务按市级行政区调用该方法，二级以下行政区复用所属市级天气记录。
     * 返回结果通常包含高德提供的未来数日预报，调用方负责按日期写入数据库。</p>
     *
     * @param adcode 行政区划 adcode
     * @return 天气预报快照列表；配置缺失、接口异常或没有预报时返回空列表
     */
    public List<AmapWeatherSnapshot> queryForecasts(String adcode)
    {
        if (adcode == null || adcode.trim().isEmpty())
        {
            return Collections.emptyList();
        }
        String normalizedAdcode = adcode.trim();
        String cacheKey = FORECAST_CACHE_PREFIX + normalizedAdcode;
        List<AmapWeatherSnapshot> cached = readForecastCache(cacheKey);
        if (cached != null)
        {
            return cached;
        }

        try
        {
            HttpResponse<String> response = requestWeather(normalizedAdcode, "all");
            if (response == null)
            {
                log.warn("未配置高德天气 API Key 列表，无法查询城市预报，adcode={}", normalizedAdcode);
                return Collections.emptyList();
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                log.warn("高德天气预报接口返回 HTTP {}，adcode={}", response.statusCode(), normalizedAdcode);
                return Collections.emptyList();
            }
            List<AmapWeatherSnapshot> snapshots = parseForecasts(normalizedAdcode, response.body());
            if (!snapshots.isEmpty())
            {
                writeForecastCache(cacheKey, snapshots);
            }
            return snapshots;
        }
        catch (Exception exception)
        {
            log.warn("查询高德天气预报失败，adcode={}, message={}", normalizedAdcode, exception.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 调用高德天气接口。
     *
     * @param adcode 行政区划 adcode
     * @param extensions 查询类型，base 为实况，all 为预报
     * @return HTTP 响应；未配置 API Key 时返回 null
     * @throws Exception HTTP 请求或 URI 构造失败时抛出异常
     */
    private HttpResponse<String> requestWeather(String adcode, String extensions) throws Exception
    {
        List<String> apiKeys = parseApiKeys(globalConfigService.get(GlobalConfigConstants.AMAP_API_KEYS));
        if (apiKeys.isEmpty())
        {
            return null;
        }
        String endpoint = globalConfigService.get(GlobalConfigConstants.AMAP_WEATHER_URL,
                "https://restapi.amap.com/v3/weather/weatherInfo");
        String separator = endpoint.contains("?") ? "&" : "?";
        for (int attempt = 0; attempt <= MAX_RATE_LIMIT_RETRIES; attempt++)
        {
            String apiKey = nextApiKey(apiKeys);
            String url = endpoint + separator + "key=" + encode(apiKey) + "&city="
                    + encode(adcode) + "&extensions=" + encode(extensions) + "&output=JSON";
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            awaitRequestSlot();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (!isRateLimited(response) || attempt == MAX_RATE_LIMIT_RETRIES)
            {
                return response;
            }

            long backoffMillis = RATE_LIMIT_RETRY_BASE_MILLIS << attempt;
            log.warn("高德天气接口触发 QPS 限制，adcode={}，第 {}/{} 次重试，等待 {}ms", adcode,
                    attempt + 1, MAX_RATE_LIMIT_RETRIES, backoffMillis);
            TimeUnit.MILLISECONDS.sleep(backoffMillis);
        }
        return null;
    }

    /**
     * 解析平台配置的多 Key 文本，保留配置顺序并删除空值和重复值。
     *
     * @param configuredKeys 平台配置值
     * @return 可用 Key 列表
     */
    private List<String> parseApiKeys(String configuredKeys)
    {
        if (configuredKeys == null || configuredKeys.trim().isEmpty())
        {
            return Collections.emptyList();
        }
        Set<String> keys = new LinkedHashSet<String>();
        for (String value : configuredKeys.split("[,;\\r\\n]+"))
        {
            if (value != null && !value.trim().isEmpty())
            {
                keys.add(value.trim());
            }
        }
        return keys.isEmpty() ? Collections.emptyList() : new ArrayList<String>(keys);
    }

    /**
     * 按全局真实外部请求次数选择 API Key。
     *
     * <p>缓存命中不会进入该方法，因此计数只代表实际发往高德的请求；Redis 不可用时使用本地
     * 计数作为降级，保证天气查询仍可用，但多实例环境下应优先恢复 Redis。</p>
     *
     * @param apiKeys 按平台配置顺序排列的 API Key
     * @return 当前请求使用的 API Key
     */
    private String nextApiKey(List<String> apiKeys)
    {
        long callNumber = incrementApiCallCounter();
        return apiKeys.get(keyIndexForCall(callNumber, apiKeys.size()));
    }

    /**
     * 根据真实外部请求序号计算配置 Key 下标。
     *
     * @param callNumber 从 1 开始的真实外部请求序号
     * @param keyCount 配置的有效 Key 数量
     * @return 当前请求使用的 Key 下标
     */
    static int keyIndexForCall(long callNumber, int keyCount)
    {
        if (callNumber < 1L || keyCount < 1)
        {
            throw new IllegalArgumentException("callNumber and keyCount must be positive");
        }
        long keyIndex = ((callNumber - 1L) / API_KEY_ROTATION_CALLS) % keyCount;
        return (int) keyIndex;
    }

    /**
     * 使用 Redis 原子递增统计高德真实请求次数。
     *
     * @return 从 1 开始的请求序号
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private long incrementApiCallCounter()
    {
        try
        {
            Long callNumber = redisService.redisTemplate.opsForValue().increment(API_KEY_CALL_COUNT_KEY, 1L);
            if (callNumber != null && callNumber > 0L)
            {
                return callNumber;
            }
        }
        catch (RuntimeException exception)
        {
            if (fallbackCounterWarningLogged.compareAndSet(false, true))
            {
                log.warn("Redis 不可用，高德 API Key 轮换暂时降级为当前实例计数，message={}", exception.getMessage());
            }
        }
        return fallbackApiCallCounter.incrementAndGet();
    }

    /**
     * 在当前服务实例内按固定间隔发起高德请求。
     *
     * <p>天气刷新是串行任务，但订单列表也可能同时触发实时天气查询，因此限速放在公共客户端，
     * 而不是只放在定时任务中。多实例部署时仍由高德 Key 的服务端配额兜底，业务任务锁负责避免
     * 多实例重复刷新。</p>
     *
     * @throws InterruptedException 当前线程被中断
     */
    private void awaitRequestSlot() throws InterruptedException
    {
        long waitNanos;
        synchronized (requestRateLock)
        {
            long now = System.nanoTime();
            long requestStart = Math.max(now, nextRequestAtNanos);
            waitNanos = requestStart - now;
            nextRequestAtNanos = requestStart + TimeUnit.MILLISECONDS.toNanos(AMAP_REQUEST_INTERVAL_MILLIS);
        }
        if (waitNanos > 0L)
        {
            TimeUnit.NANOSECONDS.sleep(waitNanos);
        }
    }

    /**
     * 判断高德响应是否为 QPS 限制。
     *
     * @param response 高德 HTTP 响应
     * @return 返回 HTTP 429 或高德 infocode=10021 时为 true
     */
    private boolean isRateLimited(HttpResponse<String> response)
    {
        if (response == null || response.statusCode() == 429)
        {
            return response != null;
        }
        try
        {
            JsonNode root = objectMapper.readTree(response.body());
            return "10021".equals(text(root, "infocode"));
        }
        catch (Exception exception)
        {
            return false;
        }
    }

    /**
     * 读取天气缓存，并兼容缓存损坏场景。
     *
     * @param cacheKey 天气缓存键
     * @return 缓存快照或 null
     */
    private AmapWeatherSnapshot readCached(String cacheKey)
    {
        Object cachedObject = redisService.getCacheObject(cacheKey);
        if (cachedObject == null)
        {
            return null;
        }
        try
        {
            return objectMapper.readValue(String.valueOf(cachedObject), AmapWeatherSnapshot.class);
        }
        catch (Exception exception)
        {
            redisService.deleteObject(cacheKey);
            log.warn("高德天气缓存解析失败，已清理缓存，cacheKey={}", cacheKey);
            return null;
        }
    }

    /**
     * 读取完整天气预报缓存，并在缓存结构损坏时删除坏数据。
     *
     * @param cacheKey 预报缓存键
     * @return 预报快照列表；没有缓存或缓存不可解析时返回 null
     */
    private List<AmapWeatherSnapshot> readForecastCache(String cacheKey)
    {
        Object cachedObject = redisService.getCacheObject(cacheKey);
        if (cachedObject == null)
        {
            return null;
        }
        try
        {
            return objectMapper.readValue(String.valueOf(cachedObject), objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, AmapWeatherSnapshot.class));
        }
        catch (Exception exception)
        {
            redisService.deleteObject(cacheKey);
            log.warn("高德天气预报缓存解析失败，已清理缓存，cacheKey={}", cacheKey);
            return null;
        }
    }

    /**
     * 将天气快照写入一小时缓存。
     *
     * @param cacheKey 天气缓存键
     * @param snapshot 天气快照
     */
    private void writeCached(String cacheKey, AmapWeatherSnapshot snapshot)
    {
        try
        {
            redisService.setCacheObject(cacheKey, objectMapper.writeValueAsString(snapshot),
                    1L, TimeUnit.HOURS);
        }
        catch (Exception exception)
        {
            log.warn("写入高德天气缓存失败，cacheKey={}, message={}", cacheKey, exception.getMessage());
        }
    }

    /**
     * 将完整天气预报列表写入一小时缓存。
     *
     * @param cacheKey 预报缓存键
     * @param snapshots 预报快照列表
     */
    private void writeForecastCache(String cacheKey, List<AmapWeatherSnapshot> snapshots)
    {
        try
        {
            redisService.setCacheObject(cacheKey, objectMapper.writeValueAsString(snapshots),
                    1L, TimeUnit.HOURS);
        }
        catch (Exception exception)
        {
            log.warn("写入高德天气预报缓存失败，cacheKey={}, message={}", cacheKey, exception.getMessage());
        }
    }

    /**
     * 解析高德天气 JSON，并优先使用实时天气、缺失时回退到当天预报。
     *
     * @param adcode 请求行政区划 adcode
     * @param body 高德响应 JSON
     * @return 标准天气快照
     * @throws Exception JSON 结构错误时抛出解析异常，由上层统一转换为不可用结果
     */
    private AmapWeatherSnapshot parseResponse(String adcode, String body) throws Exception
    {
        JsonNode root = objectMapper.readTree(body);
        String status = text(root, "status");
        if (!"1".equals(status))
        {
            String info = defaultText(text(root, "info"), "高德天气返回失败");
            log.warn("高德天气查询失败，adcode={}, infocode={}, info={}", adcode, text(root, "infocode"), info);
            return AmapWeatherSnapshot.unavailable(adcode, info);
        }

        JsonNode live = root.path("lives").isArray() && root.path("lives").size() > 0
                ? root.path("lives").get(0) : null;
        JsonNode forecast = root.path("forecasts").isArray() && root.path("forecasts").size() > 0
                ? root.path("forecasts").get(0) : null;
        JsonNode cast = forecast != null && forecast.path("casts").isArray() && forecast.path("casts").size() > 0
                ? forecast.path("casts").get(0) : null;

        AmapWeatherSnapshot result = new AmapWeatherSnapshot();
        result.available = live != null || cast != null;
        result.adcode = adcode;
        result.city = firstNonBlank(text(live, "city"), text(forecast, "city"));
        result.forecastDate = text(cast, "date");
        result.dayWeather = text(cast, "dayweather");
        result.nightWeather = text(cast, "nightweather");
        result.dayTemperature = text(cast, "daytemp");
        result.nightTemperature = text(cast, "nighttemp");
        result.dayWindDirection = text(cast, "daywind");
        result.nightWindDirection = text(cast, "nightwind");
        result.dayWindPower = text(cast, "daypower");
        result.nightWindPower = text(cast, "nightpower");
        result.weather = firstNonBlank(text(live, "weather"), text(cast, "dayweather"), text(cast, "nightweather"));
        result.temperature = firstNonBlank(text(live, "temperature"), text(cast, "daytemp"), text(cast, "nighttemp"));
        result.reportTime = firstNonBlank(text(live, "reporttime"), text(cast, "date"));
        result.icon = iconOf(result.weather);
        result.risk = isRiskWeather(result.weather);
        result.message = result.available ? "天气查询成功" : "高德未返回天气数据";
        result.rawDataJson = cast == null ? null : cast.toString();
        return result;
    }

    /**
     * 解析高德完整预报响应中的所有预报日。
     *
     * @param adcode 请求行政区划 adcode
     * @param body 高德响应 JSON
     * @return 每个预报日一条标准快照
     * @throws Exception JSON 结构错误时抛出解析异常
     */
    private List<AmapWeatherSnapshot> parseForecasts(String adcode, String body) throws Exception
    {
        JsonNode root = objectMapper.readTree(body);
        String status = text(root, "status");
        if (!"1".equals(status))
        {
            log.warn("高德天气预报查询失败，adcode={}, infocode={}, info={}", adcode, text(root, "infocode"),
                    defaultText(text(root, "info"), "高德天气返回失败"));
            return Collections.emptyList();
        }
        JsonNode forecast = root.path("forecasts").isArray() && root.path("forecasts").size() > 0
                ? root.path("forecasts").get(0) : null;
        if (forecast == null || !forecast.path("casts").isArray())
        {
            log.warn("高德天气预报返回结构为空，adcode={}, infocode={}, info={}", adcode, text(root, "infocode"),
                    defaultText(text(root, "info"), "高德未返回预报数据"));
            return Collections.emptyList();
        }
        String city = text(forecast, "city");
        String reportTime = text(forecast, "reporttime");
        List<AmapWeatherSnapshot> result = new ArrayList<AmapWeatherSnapshot>();
        for (JsonNode cast : forecast.path("casts"))
        {
            result.add(snapshotFromCast(adcode, city, reportTime, cast));
        }
        return result;
    }

    /**
     * 将高德单日 cast 节点转换为服务内部天气快照。
     *
     * @param adcode 行政区划 adcode
     * @param city 高德返回城市名称
     * @param reportTime 高德报告时间
     * @param cast 单日预报节点
     * @return 标准天气快照
     */
    private AmapWeatherSnapshot snapshotFromCast(String adcode, String city, String reportTime, JsonNode cast)
    {
        AmapWeatherSnapshot result = new AmapWeatherSnapshot();
        result.available = cast != null;
        result.adcode = adcode;
        result.city = city;
        result.forecastDate = text(cast, "date");
        result.dayWeather = text(cast, "dayweather");
        result.nightWeather = text(cast, "nightweather");
        result.dayTemperature = text(cast, "daytemp");
        result.nightTemperature = text(cast, "nighttemp");
        result.dayWindDirection = text(cast, "daywind");
        result.nightWindDirection = text(cast, "nightwind");
        result.dayWindPower = text(cast, "daypower");
        result.nightWindPower = text(cast, "nightpower");
        result.weather = firstNonBlank(result.dayWeather, result.nightWeather);
        result.temperature = firstNonBlank(result.dayTemperature, result.nightTemperature);
        result.reportTime = reportTime;
        result.icon = iconOf(firstNonBlank(result.dayWeather, result.nightWeather));
        result.risk = isRiskWeather(result.dayWeather) || isRiskWeather(result.nightWeather);
        result.message = result.available ? "天气预报查询成功" : "高德未返回天气预报";
        result.rawDataJson = cast == null ? null : cast.toString();
        return result;
    }

    /**
     * 读取 JSON 节点文本，节点不存在或为空时返回 null。
     *
     * @param node JSON 节点
     * @param field 字段名
     * @return 字段文本
     */
    private String text(JsonNode node, String field)
    {
        if (node == null || node.path(field).isMissingNode() || node.path(field).isNull())
        {
            return null;
        }
        String value = node.path(field).asText();
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    /**
     * 对 URL 参数进行 UTF-8 编码。
     *
     * @param value 参数值
     * @return 编码后的参数值
     */
    private String encode(String value)
    {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * 根据天气描述选择前端图标类型。
     *
     * @param weather 天气描述
     * @return sunny、rain、snow、storm 或 cloudy
     */
    private String iconOf(String weather)
    {
        if (weather == null)
        {
            return "cloudy";
        }
        if (weather.contains("雷") || weather.contains("暴"))
        {
            return "storm";
        }
        if (weather.contains("雪"))
        {
            return "snow";
        }
        if (weather.contains("雨"))
        {
            return "rain";
        }
        if (weather.contains("晴"))
        {
            return "sunny";
        }
        return "cloudy";
    }

    /**
     * 判断天气是否需要业务人员关注。
     *
     * @param weather 天气描述
     * @return 需要关注返回 true
     */
    private boolean isRiskWeather(String weather)
    {
        return weather != null && (weather.contains("雨") || weather.contains("雪") || weather.contains("雷")
                || weather.contains("暴") || weather.contains("台风") || weather.contains("沙尘")
                || weather.contains("冰雹") || weather.contains("雾"));
    }

    /**
     * 返回第一个非空文本。
     *
     * @param values 候选文本
     * @return 第一个非空文本或 null
     */
    private String firstNonBlank(String... values)
    {
        for (String value : values)
        {
            if (value != null && !value.trim().isEmpty())
            {
                return value;
            }
        }
        return null;
    }

    /**
     * 返回默认文本。
     *
     * @param value 原始值
     * @param fallback 默认值
     * @return 原始值或默认值
     */
    private String defaultText(String value, String fallback)
    {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
