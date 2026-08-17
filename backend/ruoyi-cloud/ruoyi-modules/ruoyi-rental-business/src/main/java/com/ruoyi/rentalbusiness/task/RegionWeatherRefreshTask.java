package com.ruoyi.rentalbusiness.task;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.rentalbusiness.domain.RegionWeatherRecord;
import com.ruoyi.rentalbusiness.integration.amap.AmapWeatherClient;
import com.ruoyi.rentalbusiness.integration.amap.AmapWeatherSnapshot;
import com.ruoyi.rentalbusiness.mapper.RegionMapper;
import com.ruoyi.rentalbusiness.mapper.RegionWeatherMapper;
import com.ruoyi.rentalbusiness.model.RegionWeatherRegion;

/**
 * 市级天气预报刷新任务。
 *
 * <p>任务只负责天气数据的批量获取和持久化，不依赖商户登录上下文。每日执行只查询有效的
 * 二级行政区，并把三日预报保存为市级天气记录；二级以下行政区通过所属市级行政区复用这些记录，
 * 不为每个区县单独请求高德。</p>
 */
@Component("regionWeatherTask")
public class RegionWeatherRefreshTask
{
    private static final Logger log = LoggerFactory.getLogger(RegionWeatherRefreshTask.class);

    /** 所有定时任务统一使用中国上海时区，避免服务器默认时区影响执行日期。 */
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");

    /** 每次任务保留当天起连续三天的预报。 */
    private static final int FORECAST_DAYS = 3;

    /** 系统任务不绑定具体用户，数据库审计字段统一使用 0 表示系统任务。 */
    private static final Long SYSTEM_OPERATOR_ID = 0L;

    /** 分布式任务锁，避免多实例同时刷新全部市级天气。 */
    private static final String LOCK_KEY = "rental-business:task:region-weather:refresh";

    /** 单次天气任务最长执行时间，超过后允许下一实例接管，避免永久死锁。 */
    private static final long LOCK_MINUTES = 30L;

    private final RegionMapper regionMapper;
    private final RegionWeatherMapper regionWeatherMapper;
    private final AmapWeatherClient amapWeatherClient;
    private final RedisService redisService;

    /**
     * 注入行政区划、天气持久化、高德客户端和 Redis 锁服务。
     *
     * @param regionMapper 行政区划数据访问对象
     * @param regionWeatherMapper 市级天气扩展表数据访问对象
     * @param amapWeatherClient 高德天气客户端
     * @param redisService Redis 服务
     */
    public RegionWeatherRefreshTask(RegionMapper regionMapper, RegionWeatherMapper regionWeatherMapper,
            AmapWeatherClient amapWeatherClient, RedisService redisService)
    {
        this.regionMapper = regionMapper;
        this.regionWeatherMapper = regionWeatherMapper;
        this.amapWeatherClient = amapWeatherClient;
        this.redisService = redisService;
    }

    /**
     * 刷新所有有效市级行政区的三日天气。
     *
     * <p>该方法由平台 {@code ruoyi-job} 的 Cron 任务通过内部接口调用，也允许平台调度页面
     * 执行一次。调度时间统一配置在平台任务记录中，业务服务只保留可复用的任务实现，避免
     * 平台调度和服务本地调度在同一时刻重复运行。</p>
     */
    public void refreshThreeDayWeather()
    {
        refreshThreeDayWeather(LocalDate.now(BUSINESS_ZONE));
    }

    /**
     * 按指定基准日期刷新三日天气，供定时入口和测试复用。
     *
     * @param baseDate 三日预报起始日期
     */
    public void refreshThreeDayWeather(LocalDate baseDate)
    {
        LocalDate actualBaseDate = baseDate == null ? LocalDate.now(BUSINESS_ZONE) : baseDate;
        String lockToken = UUID.randomUUID().toString();
        if (!tryAcquireLock(lockToken))
        {
            log.info("市级天气刷新任务已由其他实例执行，跳过本次任务，baseDate={}", actualBaseDate);
            return;
        }

        try
        {
            List<RegionWeatherRegion> regions = safeRegions(regionMapper.selectWeatherRegions());
            log.info("开始刷新市级三日天气，baseDate={}，有效市级行政区数={}", actualBaseDate, regions.size());
            int savedRecordCount = 0;
            for (RegionWeatherRegion region : regions)
            {
                savedRecordCount += refreshCityWeather(actualBaseDate, region);
            }
            log.info("市级三日天气刷新完成，baseDate={}，写入天气记录数={}", actualBaseDate, savedRecordCount);
        }
        catch (Exception exception)
        {
            log.error("市级三日天气刷新任务执行失败，baseDate={}", actualBaseDate, exception);
        }
        finally
        {
            releaseLock(lockToken);
        }
    }

    /**
     * 刷新一个市级行政区对应的天气。
     *
     * @param baseDate 三日预报起始日期
     * @param region 市级行政区划投影
     * @return 成功写入或更新的天气记录数量
     */
    private int refreshCityWeather(LocalDate baseDate, RegionWeatherRegion region)
    {
        if (region == null || region.weatherAdcode == null || region.weatherAdcode.trim().isEmpty())
        {
            return 0;
        }
        String weatherAdcode = region.weatherAdcode.trim();
        List<AmapWeatherSnapshot> forecasts;
        try
        {
            forecasts = safeForecasts(amapWeatherClient.queryForecasts(weatherAdcode));
        }
        catch (Exception exception)
        {
            log.warn("查询市级天气失败，weatherAdcode={}, message={}", weatherAdcode, exception.getMessage());
            forecasts = Collections.emptyList();
        }

        Map<LocalDate, AmapWeatherSnapshot> forecastByDate = indexForecasts(forecasts);
        List<RegionWeatherRecord> records = new ArrayList<RegionWeatherRecord>(FORECAST_DAYS);
        for (int offset = 0; offset < FORECAST_DAYS; offset++)
        {
            LocalDate weatherDate = baseDate.plusDays(offset);
            AmapWeatherSnapshot snapshot = forecastByDate.get(weatherDate);
            if (snapshot == null)
            {
                snapshot = AmapWeatherSnapshot.unavailable(weatherAdcode, "高德未返回该预报日期天气");
                snapshot.forecastDate = weatherDate.toString();
            }
            records.add(toRecord(region, weatherDate, snapshot));
        }
        return saveCityWeatherRecords(weatherAdcode, records);
    }

    /**
     * 批量保存市级天气记录；批量失败时逐条兜底，确保单条脏数据不会阻断整个任务。
     *
     * @param weatherAdcode 高德天气接口使用的行政区划 adcode
     * @param records 待保存的市级三日天气记录
     * @return 成功写入或更新的记录数量
     */
    private int saveCityWeatherRecords(String weatherAdcode, List<RegionWeatherRecord> records)
    {
        if (records == null || records.isEmpty())
        {
            return 0;
        }
        try
        {
            regionWeatherMapper.upsertBatch(records, SYSTEM_OPERATOR_ID);
            return records.size();
        }
        catch (Exception batchException)
        {
            log.error("批量保存市级天气失败，weatherAdcode={}，记录数={}，转为逐条保存", weatherAdcode, records.size(),
                    batchException);
        }

        int savedCount = 0;
        for (RegionWeatherRecord record : records)
        {
            try
            {
                regionWeatherMapper.upsert(record, SYSTEM_OPERATOR_ID);
                savedCount++;
            }
            catch (Exception exception)
            {
                log.error("保存市级天气失败，regionCode={}, weatherDate={}", record.regionCode,
                        record.weatherDate, exception);
            }
        }
        return savedCount;
    }

    /**
     * 将高德预报列表按预报日期建立索引，忽略无法解析日期的异常数据。
     *
     * @param forecasts 高德预报列表
     * @return 预报日期到天气快照的索引
     */
    private Map<LocalDate, AmapWeatherSnapshot> indexForecasts(List<AmapWeatherSnapshot> forecasts)
    {
        Map<LocalDate, AmapWeatherSnapshot> result = new LinkedHashMap<LocalDate, AmapWeatherSnapshot>();
        for (AmapWeatherSnapshot forecast : forecasts)
        {
            if (forecast == null || forecast.forecastDate == null || forecast.forecastDate.trim().isEmpty())
            {
                continue;
            }
            try
            {
                result.put(LocalDate.parse(forecast.forecastDate.trim()), forecast);
            }
            catch (RuntimeException exception)
            {
                log.warn("忽略无法解析日期的高德天气预报，forecastDate={}", forecast.forecastDate);
            }
        }
        return result;
    }

    /**
     * 将市级行政区划和天气快照转换为数据库记录。
     *
     * @param region 市级行政区划投影
     * @param weatherDate 预报日期
     * @param snapshot 天气快照
     * @return 待持久化天气记录
     */
    private RegionWeatherRecord toRecord(RegionWeatherRegion region, LocalDate weatherDate,
            AmapWeatherSnapshot snapshot)
    {
        RegionWeatherRecord record = new RegionWeatherRecord();
        record.regionCode = region.regionCode;
        record.regionName = region.regionName;
        record.cityCode = region.cityCode;
        record.cityName = region.cityName;
        record.provinceCode = region.provinceCode;
        record.provinceName = region.provinceName;
        record.amapCityCode = region.amapCityCode;
        record.weatherDate = Date.valueOf(weatherDate);
        record.dayWeather = snapshot.dayWeather;
        record.nightWeather = snapshot.nightWeather;
        record.dayTemperature = snapshot.dayTemperature;
        record.nightTemperature = snapshot.nightTemperature;
        record.dayWindDirection = snapshot.dayWindDirection;
        record.nightWindDirection = snapshot.nightWindDirection;
        record.dayWindPower = snapshot.dayWindPower;
        record.nightWindPower = snapshot.nightWindPower;
        record.weatherIcon = snapshot.icon;
        record.riskFlag = snapshot.risk ? 1 : 0;
        record.queryStatus = snapshot.available ? "success" : "failed";
        record.queryMessage = snapshot.message;
        record.reportTime = snapshot.reportTime;
        record.rawDataJson = snapshot.rawDataJson;
        return record;
    }

    /**
     * 将 Mapper 返回的空集合统一转换为空列表，避免定时任务因 null 中断。
     *
     * @param regions Mapper 查询结果
     * @return 非空列表
     */
    private List<RegionWeatherRegion> safeRegions(List<RegionWeatherRegion> regions)
    {
        return regions == null ? Collections.emptyList() : regions;
    }

    /**
     * 将天气客户端返回的空集合统一转换为空列表。
     *
     * @param forecasts 天气预报查询结果
     * @return 非空列表
     */
    private List<AmapWeatherSnapshot> safeForecasts(List<AmapWeatherSnapshot> forecasts)
    {
        return forecasts == null ? Collections.emptyList() : forecasts;
    }

    /**
     * 使用 Redis 设置带过期时间的任务锁。
     *
     * @param lockToken 当前实例锁标识
     * @return 成功取得锁返回 true
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean tryAcquireLock(String lockToken)
    {
        RedisTemplate redisTemplate = redisService.redisTemplate;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, lockToken, LOCK_MINUTES,
                TimeUnit.MINUTES);
        return Boolean.TRUE.equals(acquired);
    }

    /**
     * 释放当前实例持有的天气任务锁，避免删除其他实例刚取得的锁。
     *
     * @param lockToken 当前实例锁标识
     */
    private void releaseLock(String lockToken)
    {
        Object currentToken = redisService.getCacheObject(LOCK_KEY);
        if (lockToken.equals(currentToken == null ? null : String.valueOf(currentToken)))
        {
            redisService.deleteObject(LOCK_KEY);
        }
    }
}
