package com.ruoyi.system.service.impl;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.constant.CacheConstants;
import com.ruoyi.common.core.constant.GlobalConfigConstants;
import com.ruoyi.common.core.constant.UserConstants;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.redis.service.RedisService;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.mapper.SysConfigMapper;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 参数配置 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class SysConfigServiceImpl implements ISysConfigService
{
    @Autowired
    private SysConfigMapper configMapper;

    @Autowired
    private RedisService redisService;

    /**
     * 项目启动时，初始化参数到缓存
     */
    @PostConstruct
    public void init()
    {
        loadingConfigCache();
    }

    /**
     * 查询参数配置信息
     * 
     * @param configId 参数配置ID
     * @return 参数配置信息
     */
    @Override
    public SysConfig selectConfigById(Long configId)
    {
        SysConfig config = new SysConfig();
        config.setConfigId(configId);
        return configMapper.selectConfig(config);
    }

    /**
     * 根据键名查询参数配置信息
     * 
     * @param configKey 参数key
     * @return 参数键值
     */
    @Override
    public String selectConfigByKey(String configKey)
    {
        Object cachedObject = redisService.getCacheObject(getCacheKey(configKey));
        if (cachedObject != null)
        {
            return Convert.toStr(cachedObject);
        }
        SysConfig config = new SysConfig();
        config.setConfigKey(configKey);
        SysConfig retConfig = configMapper.selectConfig(config);
        if (StringUtils.isNotNull(retConfig))
        {
            cacheConfigValue(configKey, retConfig.getConfigValue());
            return retConfig.getConfigValue();
        }
        return StringUtils.EMPTY;
    }

    /**
     * 查询参数配置列表
     * 
     * @param config 参数配置信息
     * @return 参数配置集合
     */
    @Override
    public List<SysConfig> selectConfigList(SysConfig config)
    {
        return configMapper.selectConfigList(config);
    }

    /**
     * 新增参数配置
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public int insertConfig(SysConfig config)
    {
        int row = configMapper.insertConfig(config);
        if (row > 0)
        {
            cacheConfigValue(config.getConfigKey(), config.getConfigValue());
        }
        return row;
    }

    /**
     * 修改参数配置
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public int updateConfig(SysConfig config)
    {
        SysConfig temp = configMapper.selectConfigById(config.getConfigId());
        if (!StringUtils.equals(temp.getConfigKey(), config.getConfigKey()))
        {
            redisService.deleteObject(getCacheKey(temp.getConfigKey()));
        }

        int row = configMapper.updateConfig(config);
        if (row > 0)
        {
            cacheConfigValue(config.getConfigKey(), config.getConfigValue());
        }
        return row;
    }

    /**
     * 批量删除参数信息
     * 
     * @param configIds 需要删除的参数ID
     */
    @Override
    public void deleteConfigByIds(Long[] configIds)
    {
        for (Long configId : configIds)
        {
            SysConfig config = selectConfigById(configId);
            if (StringUtils.equals(UserConstants.YES, config.getConfigType()))
            {
                throw new ServiceException(String.format("内置参数【%1$s】不能删除 ", config.getConfigKey()));
            }
            configMapper.deleteConfigById(configId);
            redisService.deleteObject(getCacheKey(config.getConfigKey()));
        }
    }

    /**
     * 加载参数缓存数据
     */
    @Override
    public void loadingConfigCache()
    {
        List<SysConfig> configsList = configMapper.selectConfigList(new SysConfig());
        for (SysConfig config : configsList)
        {
            cacheConfigValue(config.getConfigKey(), config.getConfigValue());
        }
    }

    /**
     * 清空参数缓存数据
     */
    @Override
    public void clearConfigCache()
    {
        Collection<String> keys = redisService.keys(CacheConstants.SYS_CONFIG_KEY + "*");
        redisService.deleteObject(keys);
    }

    /**
     * 重置参数缓存数据
     */
    @Override
    public void resetConfigCache()
    {
        clearConfigCache();
        loadingConfigCache();
    }

    /**
     * 校验参数键名是否唯一
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public boolean checkConfigKeyUnique(SysConfig config)
    {
        Long configId = StringUtils.isNull(config.getConfigId()) ? -1L : config.getConfigId();
        SysConfig info = configMapper.checkConfigKeyUnique(config.getConfigKey());
        if (StringUtils.isNotNull(info) && info.getConfigId().longValue() != configId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 设置cache key
     * 
     * @param configKey 参数键
     * @return 缓存键key
     */
    private String getCacheKey(String configKey)
    {
        return CacheConstants.GLOBAL_CONFIG_CACHE_KEY + configKey;
    }

    /**
     * 使用统一七天有效期写入全局配置缓存。
     *
     * <p>无论是启动加载、首次读取、新增还是修改，均通过该方法写入；修改配置时重新
     * set 会刷新 Redis TTL，避免平台与商户服务长期读取旧值。</p>
     *
     * @param configKey 配置键
     * @param configValue 配置值
     */
    private void cacheConfigValue(String configKey, String configValue)
    {
        long cacheTtlDays = GlobalConfigConstants.CACHE_TTL_DAYS.equals(configKey)
                ? CacheConstants.GLOBAL_CONFIG_CACHE_TTL_DAYS : getConfiguredCacheTtlDays();
        redisService.setCacheObject(getCacheKey(configKey), configValue,
                cacheTtlDays, TimeUnit.DAYS);
    }

    /**
     * 读取全局配置缓存有效期。
     *
     * <p>缓存有效期配置本身使用代码默认值读取，避免读取该配置时产生递归；其他配置
     * 优先使用 Redis 中的值，缓存未命中时再查询数据库。非法、零值或负值均回退到七天。</p>
     *
     * @return 配置缓存有效期，单位为天
     */
    private long getConfiguredCacheTtlDays()
    {
        String configuredValue = Convert.toStr(redisService.getCacheObject(
                getCacheKey(GlobalConfigConstants.CACHE_TTL_DAYS)));
        if (!StringUtils.isNotEmpty(configuredValue))
        {
            SysConfig ttlConfig = new SysConfig();
            ttlConfig.setConfigKey(GlobalConfigConstants.CACHE_TTL_DAYS);
            SysConfig storedConfig = configMapper.selectConfig(ttlConfig);
            configuredValue = storedConfig == null ? null : storedConfig.getConfigValue();
        }
        try
        {
            long cacheTtlDays = Long.parseLong(configuredValue);
            return cacheTtlDays > 0 ? cacheTtlDays : CacheConstants.GLOBAL_CONFIG_CACHE_TTL_DAYS;
        }
        catch (Exception exception)
        {
            return CacheConstants.GLOBAL_CONFIG_CACHE_TTL_DAYS;
        }
    }
}
