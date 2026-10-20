package com.ruoyi.rentalbusiness.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.GlobalConfigConstants;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.rentalbusiness.config.GlobalConfigService;
import com.ruoyi.rentalbusiness.domain.ExpressTimeEstimateRecord;
import com.ruoyi.rentalbusiness.enums.ExpressTimeProviderCode;
import com.ruoyi.rentalbusiness.integration.express.time.ExpressTimeProvider;
import com.ruoyi.rentalbusiness.mapper.ExpressTimeEstimateMapper;
import com.ruoyi.rentalbusiness.model.ExpressCompanyMappingModel;
import com.ruoyi.rentalbusiness.model.ExpressTimeEstimateCommand;
import com.ruoyi.rentalbusiness.model.ExpressTimeEstimateModel;
import com.ruoyi.rentalbusiness.model.ExpressTimeProviderRequest;
import com.ruoyi.rentalbusiness.model.ExpressTimeProviderResult;
import com.ruoyi.rentalbusiness.model.RegionAddressModel;

/** 快递时效预估应用服务，按 Redis、数据库、供应商的顺序读取并回填结果。 */
@Service
public class ExpressTimeEstimateService
{
    private static final Logger log = LoggerFactory.getLogger(ExpressTimeEstimateService.class);

    private static final ZoneId BUSINESS_ZONE = DateUtils.DEFAULT_ZONE_ID;

    private static final DateTimeFormatter ORDER_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String CACHE_PREFIX = "rental:express:time:v1:";

    private static final String LOCK_PREFIX = "rental:express:time:lock:v1:";

    private static final long LOCK_SECONDS = 30L;

    private static final long DEFAULT_CACHE_TTL_HOURS = 24L;

    private final ExpressTimeEstimateMapper estimateMapper;
    private final RegionAddressParser regionAddressParser;
    private final ExpressCompanyMappingService companyMappingService;
    private final List<ExpressTimeProvider> providers;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final GlobalConfigService globalConfigService;

    /** 注入时效持久化、行政区划、字典映射、供应商、缓存和全局配置服务。 */
    public ExpressTimeEstimateService(ExpressTimeEstimateMapper estimateMapper,
            RegionAddressParser regionAddressParser, ExpressCompanyMappingService companyMappingService,
            List<ExpressTimeProvider> providers, RedisService redisService, ObjectMapper objectMapper,
            GlobalConfigService globalConfigService)
    {
        this.estimateMapper = estimateMapper;
        this.regionAddressParser = regionAddressParser;
        this.companyMappingService = companyMappingService;
        this.providers = providers;
        this.redisService = redisService;
        this.objectMapper = objectMapper;
        this.globalConfigService = globalConfigService;
    }

    /** 查询快递时效，缓存命中时不访问供应商。 */
    public ExpressTimeEstimateModel query(ExpressTimeEstimateCommand command)
    {
        if (command == null)
        {
            throw new ServiceException("时效查询参数不能为空");
        }
        String fromAddress = requiredText(command.getFromAddress(), "寄件地址");
        String toAddress = requiredText(command.getToAddress(), "收件地址");
        String commonCompanyCode = requiredText(command.getExpressCompanyCode(), "快递公司");
        if (fromAddress.length() > 500 || toAddress.length() > 500)
        {
            throw new ServiceException("地址长度不能超过500个字符");
        }

        RegionAddressModel requestedFrom = requireDistrict(regionAddressParser.parse(fromAddress), "寄件地址");
        RegionAddressModel requestedTo = requireDistrict(regionAddressParser.parse(toAddress), "收件地址");
        ExpressCompanyMappingModel company = companyMappingService.requireByCommonCode(commonCompanyCode);
        String normalizedCompanyCode = company.commonCode;
        String requestedExpressType = trim(command.getExpressType());
        LocalDateTime orderTime = parseOrderTime(command.getOrderTime());
        String providerCode = ExpressTimeProviderCode.KUAIDI100.getCode();
        String cacheKey = cacheKey(requestedFrom.districtCode, requestedTo.districtCode,
                normalizedCompanyCode, requestedExpressType);

        ExpressTimeEstimateModel cached = readCache(cacheKey);
        if (cached != null)
        {
            return cached;
        }
        ExpressTimeEstimateRecord stored = readDatabase(requestedFrom.districtCode, requestedTo.districtCode,
                normalizedCompanyCode, requestedExpressType, providerCode);
        if (stored != null)
        {
            ExpressTimeEstimateModel result = toModel(stored);
            writeCache(cacheKey, result, cacheTtlSeconds(stored));
            return result;
        }

        String lockKey = lockKey(requestedFrom.districtCode, requestedTo.districtCode,
                normalizedCompanyCode, requestedExpressType);
        String lockToken = UUID.randomUUID().toString();
        Boolean lockAcquired = tryAcquireLock(lockKey, lockToken);
        if (Boolean.FALSE.equals(lockAcquired))
        {
            ExpressTimeEstimateModel retryCached = readCache(cacheKey);
            if (retryCached != null)
            {
                return retryCached;
            }
            ExpressTimeEstimateRecord retryStored = readDatabase(requestedFrom.districtCode, requestedTo.districtCode,
                    normalizedCompanyCode, requestedExpressType, providerCode);
            if (retryStored != null)
            {
                return toModel(retryStored);
            }
            throw new ServiceException("该线路时效正在查询，请稍后重试");
        }

        try
        {
            ExpressTimeEstimateModel lockedCached = readCache(cacheKey);
            if (lockedCached != null)
            {
                return lockedCached;
            }
            ExpressTimeEstimateRecord lockedStored = readDatabase(requestedFrom.districtCode, requestedTo.districtCode,
                    normalizedCompanyCode, requestedExpressType, providerCode);
            if (lockedStored != null)
            {
                ExpressTimeEstimateModel result = toModel(lockedStored);
                writeCache(cacheKey, result, cacheTtlSeconds(lockedStored));
                return result;
            }

            ExpressTimeProvider provider = findProvider(ExpressTimeProviderCode.KUAIDI100);
            ExpressTimeProviderRequest request = new ExpressTimeProviderRequest(fromAddress, toAddress,
                    company.kuaidi100Code, requestedExpressType, orderTime);
            ExpressTimeProviderResult providerResult = provider.estimate(request);
            if (providerResult == null || !providerResult.available())
            {
                throw new ServiceException(providerResult == null || isBlank(providerResult.message())
                        ? "快递时效预估失败" : providerResult.message());
            }

            ExpressCompanyMappingModel returnedCompany = companyMappingService
                    .findByKuaidi100Code(providerResult.providerCompanyCode());
            if (returnedCompany == null || !normalizedCompanyCode.equals(returnedCompany.commonCode))
            {
                throw new ServiceException("快递100返回的快递公司与所选公司不一致");
            }
            RegionAddressModel returnedFrom = regionAddressParser.parse(providerResult.fromName());
            RegionAddressModel returnedTo = regionAddressParser.parse(providerResult.toName());
            String fromAcode = resolveDistrict("寄件地", returnedFrom, requestedFrom);
            String toAcode = resolveDistrict("收件地", returnedTo, requestedTo);
            String expressType = firstNonBlank(providerResult.expressType(), requestedExpressType);
            validateDurations(providerResult);

            ExpressTimeEstimateRecord record = new ExpressTimeEstimateRecord();
            record.fromAcode = fromAcode;
            record.toAcode = toAcode;
            record.expressCompanyCode = normalizedCompanyCode;
            record.expressType = expressType == null ? "" : expressType;
            record.deliveryExpendTime = providerResult.deliveryExpendTime();
            record.totalTime = providerResult.totalTime();
            record.remainTime = providerResult.remainTime();
            record.providerCode = providerCode;
            record.lastQueryTime = LocalDateTime.now(BUSINESS_ZONE);
            record.expireTime = record.lastQueryTime.plusHours(cacheTtlHours());
            if (estimateMapper.upsert(record) <= 0)
            {
                throw new ServiceException("快递时效预估结果保存失败");
            }

            ExpressTimeEstimateModel result = toModel(record);
            writeCache(cacheKey, result, cacheTtlSeconds(record));
            String canonicalCacheKey = cacheKey(fromAcode, toAcode, normalizedCompanyCode, expressType);
            if (!canonicalCacheKey.equals(cacheKey))
            {
                writeCache(canonicalCacheKey, result, cacheTtlSeconds(record));
            }
            return result;
        }
        finally
        {
            if (Boolean.TRUE.equals(lockAcquired))
            {
                releaseLock(lockKey, lockToken);
            }
        }
    }

    private ExpressTimeProvider findProvider(ExpressTimeProviderCode providerCode)
    {
        if (providers != null)
        {
            for (ExpressTimeProvider provider : providers)
            {
                if (provider != null && providerCode == provider.provider())
                {
                    return provider;
                }
            }
        }
        throw new ServiceException("暂不支持该快递时效服务");
    }

    private ExpressTimeEstimateRecord readDatabase(String fromAcode, String toAcode, String companyCode,
            String expressType, String providerCode)
    {
        return estimateMapper.selectValid(fromAcode, toAcode, companyCode, expressType, providerCode);
    }

    private ExpressTimeEstimateModel readCache(String key)
    {
        if (redisService == null)
        {
            return null;
        }
        Object cachedObject;
        try
        {
            cachedObject = redisService.getCacheObject(key);
        }
        catch (RuntimeException exception)
        {
            log.warn("读取快递时效 Redis 缓存失败，message={}", exception.getMessage());
            return null;
        }
        if (cachedObject == null)
        {
            return null;
        }
        try
        {
            ExpressTimeEstimateModel result = cachedObject instanceof ExpressTimeEstimateModel
                    ? (ExpressTimeEstimateModel) cachedObject
                    : objectMapper.readValue(String.valueOf(cachedObject), ExpressTimeEstimateModel.class);
            return isUsable(result) ? result : null;
        }
        catch (Exception exception)
        {
            try
            {
                redisService.deleteObject(key);
            }
            catch (RuntimeException deleteException)
            {
                log.warn("清理损坏的快递时效缓存失败，message={}", deleteException.getMessage());
            }
            log.warn("快递时效 Redis 缓存解析失败，已清理缓存，message={}", exception.getMessage());
            return null;
        }
    }

    private void writeCache(String key, ExpressTimeEstimateModel value, long ttlSeconds)
    {
        if (redisService == null || value == null || ttlSeconds <= 0)
        {
            return;
        }
        try
        {
            redisService.setCacheObject(key, objectMapper.writeValueAsString(value), ttlSeconds, TimeUnit.SECONDS);
        }
        catch (Exception exception)
        {
            log.warn("写入快递时效 Redis 缓存失败，message={}", exception.getMessage());
        }
    }

    private Boolean tryAcquireLock(String key, String token)
    {
        if (redisService == null || redisService.redisTemplate == null)
        {
            return null;
        }
        try
        {
            RedisTemplate redisTemplate = redisService.redisTemplate;
            return redisTemplate.opsForValue().setIfAbsent(key, token, LOCK_SECONDS, TimeUnit.SECONDS);
        }
        catch (RuntimeException exception)
        {
            log.warn("获取快递时效 Redis 锁失败，将跳过分布式锁，message={}", exception.getMessage());
            return null;
        }
    }

    private void releaseLock(String key, String token)
    {
        if (redisService == null)
        {
            return;
        }
        try
        {
            Object currentToken = redisService.getCacheObject(key);
            if (token.equals(currentToken == null ? null : String.valueOf(currentToken)))
            {
                redisService.deleteObject(key);
            }
        }
        catch (RuntimeException exception)
        {
            log.warn("释放快递时效 Redis 锁失败，message={}", exception.getMessage());
        }
    }

    private LocalDateTime parseOrderTime(String value)
    {
        if (isBlank(value))
        {
            return LocalDateTime.now(BUSINESS_ZONE);
        }
        String normalized = value.trim();
        try
        {
            return LocalDateTime.parse(normalized, ORDER_TIME_FORMAT);
        }
        catch (DateTimeParseException firstException)
        {
            try
            {
                return LocalDateTime.parse(normalized);
            }
            catch (DateTimeParseException secondException)
            {
                throw new ServiceException("下单时间格式必须为 yyyy-MM-dd HH:mm:ss");
            }
        }
    }

    private RegionAddressModel requireDistrict(RegionAddressModel address, String label)
    {
        if (address == null || isBlank(address.districtCode))
        {
            throw new ServiceException(label + "未解析到三级行政区划");
        }
        return address;
    }

    private String resolveDistrict(String label, RegionAddressModel returned, RegionAddressModel requested)
    {
        if (returned != null && !isBlank(returned.districtCode))
        {
            if (!consistent(returned, requested))
            {
                throw new ServiceException("快递100返回的" + label + "与查询地址不一致");
            }
            return returned.districtCode;
        }
        if (requested != null && !isBlank(requested.districtCode) && consistent(returned, requested))
        {
            return requested.districtCode;
        }
        throw new ServiceException("快递100返回的" + label + "未解析到三级行政区划");
    }

    private boolean consistent(RegionAddressModel returned, RegionAddressModel requested)
    {
        if (returned == null || requested == null)
        {
            return false;
        }
        boolean hasRecognizedCode = false;
        if (!isBlank(returned.provinceCode))
        {
            hasRecognizedCode = true;
            if (!returned.provinceCode.equals(requested.provinceCode))
            {
                return false;
            }
        }
        if (!isBlank(returned.cityCode))
        {
            hasRecognizedCode = true;
            if (!returned.cityCode.equals(requested.cityCode))
            {
                return false;
            }
        }
        if (!isBlank(returned.districtCode))
        {
            hasRecognizedCode = true;
            if (!returned.districtCode.equals(requested.districtCode))
            {
                return false;
            }
        }
        return hasRecognizedCode;
    }

    private void validateDurations(ExpressTimeProviderResult result)
    {
        if (result.deliveryExpendTime() == null && result.totalTime() == null && result.remainTime() == null)
        {
            throw new ServiceException("快递100返回的时效数据为空");
        }
        if (isNegative(result.deliveryExpendTime()) || isNegative(result.totalTime()) || isNegative(result.remainTime()))
        {
            throw new ServiceException("快递100返回了无效的时效数据");
        }
    }

    private boolean isNegative(java.math.BigDecimal value)
    {
        return value != null && value.signum() < 0;
    }

    private ExpressTimeEstimateModel toModel(ExpressTimeEstimateRecord record)
    {
        ExpressTimeEstimateModel result = new ExpressTimeEstimateModel();
        result.expressCompanyCode = record.expressCompanyCode;
        result.expressType = record.expressType;
        result.deliveryExpendTime = record.deliveryExpendTime;
        result.totalTime = record.totalTime;
        result.remainTime = record.remainTime;
        return result;
    }

    private long cacheTtlSeconds(ExpressTimeEstimateRecord record)
    {
        if (record == null || record.expireTime == null)
        {
            return TimeUnit.HOURS.toSeconds(cacheTtlHours());
        }
        long seconds = java.time.Duration.between(LocalDateTime.now(BUSINESS_ZONE), record.expireTime).getSeconds();
        return Math.max(1L, seconds);
    }

    private long cacheTtlHours()
    {
        if (globalConfigService == null)
        {
            return DEFAULT_CACHE_TTL_HOURS;
        }
        try
        {
            String configured = globalConfigService.get(GlobalConfigConstants.EXPRESS_KUAIDI100_TIME_CACHE_TTL_HOURS,
                    String.valueOf(DEFAULT_CACHE_TTL_HOURS));
            long hours = Long.parseLong(configured);
            return hours > 0 ? hours : DEFAULT_CACHE_TTL_HOURS;
        }
        catch (Exception exception)
        {
            return DEFAULT_CACHE_TTL_HOURS;
        }
    }

    private boolean isUsable(ExpressTimeEstimateModel value)
    {
        return value != null && !isBlank(value.expressCompanyCode)
                && (value.deliveryExpendTime != null || value.totalTime != null || value.remainTime != null);
    }

    static String cacheKey(String fromAcode, String toAcode, String companyCode, String expressType)
    {
        return CACHE_PREFIX + keyPart(fromAcode) + ':' + keyPart(toAcode) + ':' + keyPart(companyCode) + ':'
                + keyPart(isBlank(expressType) ? "default" : expressType);
    }

    private String lockKey(String fromAcode, String toAcode, String companyCode, String expressType)
    {
        return LOCK_PREFIX + keyPart(fromAcode) + ':' + keyPart(toAcode) + ':' + keyPart(companyCode) + ':'
                + keyPart(isBlank(expressType) ? "default" : expressType);
    }

    private static String keyPart(String value)
    {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
                (value == null ? "" : value.trim()).getBytes(StandardCharsets.UTF_8));
    }

    private String requiredText(String value, String label)
    {
        if (isBlank(value))
        {
            throw new ServiceException(label + "不能为空");
        }
        return value.trim();
    }

    private String trim(String value)
    {
        return isBlank(value) ? null : value.trim();
    }

    private String firstNonBlank(String first, String second)
    {
        return !isBlank(first) ? first.trim() : trim(second);
    }

    private static boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }
}
