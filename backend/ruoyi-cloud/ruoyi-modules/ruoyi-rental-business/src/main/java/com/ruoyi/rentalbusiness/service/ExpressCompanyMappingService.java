package com.ruoyi.rentalbusiness.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.rentalbusiness.model.ExpressCompanyMappingModel;
import com.ruoyi.system.api.RemoteExpressDictionaryService;
import com.ruoyi.system.api.domain.SysDictData;

/** 读取并缓存平台快递公司字典，集中负责系统编码和供应商编码的互转。 */
@Service
public class ExpressCompanyMappingService
{
    private static final Logger log = LoggerFactory.getLogger(ExpressCompanyMappingService.class);

    private static final String DICT_TYPE = "rental_express_company";

    private static final String CACHE_KEY = "rental:express:company:mappings:v1";

    private static final long CACHE_TTL_HOURS = 1L;

    private final RemoteExpressDictionaryService dictionaryService;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    /** 注入平台字典内部契约、Redis 和 JSON 解析器。 */
    public ExpressCompanyMappingService(RemoteExpressDictionaryService dictionaryService,
            RedisService redisService, ObjectMapper objectMapper)
    {
        this.dictionaryService = dictionaryService;
        this.redisService = redisService;
        this.objectMapper = objectMapper;
    }

    /** 按平台统一快递公司编码取得有效供应商映射。 */
    public ExpressCompanyMappingModel requireByCommonCode(String commonCode)
    {
        String normalizedCode = normalize(commonCode);
        if (normalizedCode == null)
        {
            throw new ServiceException("快递公司不能为空");
        }
        for (ExpressCompanyMappingModel mapping : loadMappings())
        {
            if (normalizedCode.equals(mapping.commonCode))
            {
                if (isBlank(mapping.kuaidi100Code))
                {
                    throw new ServiceException("该快递公司尚未配置快递100编码");
                }
                return mapping;
            }
        }
        throw new ServiceException("快递公司配置不存在或已停用");
    }

    /** 按供应商 kuaidicom 反向取得平台统一快递公司编码。 */
    public ExpressCompanyMappingModel findByKuaidi100Code(String providerCode)
    {
        String normalizedCode = normalize(providerCode);
        if (normalizedCode == null)
        {
            return null;
        }
        ExpressCompanyMappingModel matched = null;
        for (ExpressCompanyMappingModel mapping : loadMappings())
        {
            if (!normalizedCode.equalsIgnoreCase(mapping.kuaidi100Code))
            {
                continue;
            }
            if (matched != null && !matched.commonCode.equals(mapping.commonCode))
            {
                throw new ServiceException("平台快递公司配置存在重复供应商编码");
            }
            matched = mapping;
        }
        return matched;
    }

    /** 读取 Redis 或平台字典，返回已解析的受控映射列表。 */
    private List<ExpressCompanyMappingModel> loadMappings()
    {
        List<ExpressCompanyMappingModel> cached = readCache();
        if (cached != null)
        {
            return cached;
        }

        R<List<SysDictData>> response;
        try
        {
            response = dictionaryService.list(DICT_TYPE, SecurityConstants.INNER);
        }
        catch (RuntimeException exception)
        {
            log.warn("读取平台快递公司字典失败，message={}", exception.getMessage());
            throw new ServiceException("平台快递公司配置暂不可用");
        }
        if (response == null || !R.isSuccess(response) || response.getData() == null)
        {
            throw new ServiceException("平台快递公司配置暂不可用");
        }

        List<ExpressCompanyMappingModel> mappings = parseMappings(response.getData());
        if (mappings.isEmpty())
        {
            throw new ServiceException("平台未配置可用的快递公司");
        }
        writeCache(mappings);
        return mappings;
    }

    /** 解析字典键值中的通用编码、供应商编码和单号校验规则。 */
    private List<ExpressCompanyMappingModel> parseMappings(List<SysDictData> dictionary)
    {
        Map<String, ExpressCompanyMappingModel> mappings = new LinkedHashMap<String, ExpressCompanyMappingModel>();
        for (SysDictData dict : dictionary)
        {
            if (dict == null || (dict.getStatus() != null && dict.getStatus() != 0))
            {
                continue;
            }
            try
            {
                JsonNode value = objectMapper.readTree(dict.getDictValue());
                String commonCode = normalize(value.path("code").asText(null));
                String providerCode = normalize(value.path("kuaidi100Code").asText(null));
                if (commonCode == null)
                {
                    log.warn("平台快递公司字典缺少通用编码，label={}", dict.getDictLabel());
                    continue;
                }
                if (mappings.containsKey(commonCode))
                {
                    throw new com.ruoyi.common.core.exception.ServiceException("平台快递公司配置存在重复通用编码");
                }
                ExpressCompanyMappingModel mapping = new ExpressCompanyMappingModel();
                mapping.commonCode = commonCode;
                mapping.displayName = dict.getDictLabel();
                mapping.kuaidi100Code = providerCode;
                mapping.trackingPattern = normalize(value.path("pattern").asText(null));
                mappings.put(commonCode, mapping);
            }
            catch (ServiceException exception)
            {
                throw exception;
            }
            catch (Exception exception)
            {
                log.warn("平台快递公司字典键值解析失败，label={}, message={}", dict.getDictLabel(), exception.getMessage());
            }
        }
        return new ArrayList<ExpressCompanyMappingModel>(mappings.values());
    }

    private List<ExpressCompanyMappingModel> readCache()
    {
        if (redisService == null)
        {
            return null;
        }
        Object cachedObject;
        try
        {
            cachedObject = redisService.getCacheObject(CACHE_KEY);
        }
        catch (RuntimeException exception)
        {
            log.warn("读取快递公司映射缓存失败，message={}", exception.getMessage());
            return null;
        }
        if (cachedObject == null)
        {
            return null;
        }
        try
        {
            return objectMapper.readValue(String.valueOf(cachedObject), objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, ExpressCompanyMappingModel.class));
        }
        catch (Exception exception)
        {
            try
            {
                redisService.deleteObject(CACHE_KEY);
            }
            catch (RuntimeException deleteException)
            {
                log.warn("清理损坏的快递公司映射缓存失败，message={}", deleteException.getMessage());
            }
            log.warn("快递公司映射缓存解析失败，已清理缓存，message={}", exception.getMessage());
            return null;
        }
    }

    private void writeCache(List<ExpressCompanyMappingModel> mappings)
    {
        if (redisService == null)
        {
            return;
        }
        try
        {
            redisService.setCacheObject(CACHE_KEY, objectMapper.writeValueAsString(mappings),
                    CACHE_TTL_HOURS, TimeUnit.HOURS);
        }
        catch (Exception exception)
        {
            log.warn("写入快递公司映射缓存失败，message={}", exception.getMessage());
        }
    }

    private String normalize(String value)
    {
        if (value == null || value.trim().isEmpty())
        {
            return null;
        }
        return value.trim();
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }
}
