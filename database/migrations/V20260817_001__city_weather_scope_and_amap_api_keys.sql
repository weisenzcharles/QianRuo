-- 天气任务只维护二级行政区天气，二级以下行政区通过所属市级行政区复用天气记录。
-- 同时将高德单 Key 配置调整为多 Key 配置，并清理旧的单 Key 活跃配置。

USE `ry_business`;

-- 历史任务曾按区县落库；按统一软删除规则停用非二级行政区天气快照，保留审计数据。
UPDATE `ry_business`.`ry_rental_region_weather` weather
LEFT JOIN `ry_business`.`ry_rental_region` region
    ON region.region_code = weather.region_code
SET weather.is_delete = 1,
    weather.update_time = NOW()
WHERE weather.is_delete = 0
  AND (region.region_level IS NULL OR region.region_level <> 2);

ALTER TABLE `ry_business`.`ry_rental_region_weather`
    MODIFY COLUMN `region_code` VARCHAR(16) NOT NULL COMMENT '市级行政区划adcode',
    MODIFY COLUMN `region_name` VARCHAR(64) NOT NULL COMMENT '市级行政区划名称快照',
    MODIFY COLUMN `city_code` VARCHAR(16) DEFAULT NULL COMMENT '市级行政区划adcode',
    MODIFY COLUMN `city_name` VARCHAR(64) DEFAULT NULL COMMENT '市级行政区划名称快照',
    COMMENT = '市级行政区三日天气预报扩展表';

USE `ry-cloud`;

-- 已有新配置时停用旧单 Key 配置，避免平台端继续展示或读取旧配置键。
SET @has_amap_api_keys_config := (
    SELECT COUNT(*)
    FROM `sys_config`
    WHERE `config_key` = 'global.amap.api-keys'
      AND `is_delete` = 0
);

UPDATE `sys_config`
SET `is_delete` = 1,
    `update_by` = 'migration',
    `update_time` = NOW()
WHERE `config_key` = 'global.amap.api-key'
  AND `is_delete` = 0
  AND @has_amap_api_keys_config > 0;

-- 没有新配置时直接改键，保留已经配置的 Key 内容，不兼容旧键读取逻辑。
UPDATE `sys_config`
SET `config_name` = '高德地图 API Key（多个）',
    `config_key` = 'global.amap.api-keys',
    `remark` = '高德开放平台 Web 服务密钥，多个 Key 使用逗号、分号或换行分隔，只在平台配置页面维护',
    `update_by` = 'migration',
    `update_time` = NOW()
WHERE `config_key` = 'global.amap.api-key'
  AND `is_delete` = 0
  AND @has_amap_api_keys_config = 0;

INSERT INTO `sys_config`
    (`config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`,
     `update_by`, `update_time`, `remark`, `is_delete`)
SELECT '高德地图 API Key（多个）', 'global.amap.api-keys', '', 'N', 'migration', NOW(),
       'migration', NOW(),
       '高德开放平台 Web 服务密钥，多个 Key 使用逗号、分号或换行分隔，只在平台配置页面维护', 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `sys_config`
    WHERE `config_key` = 'global.amap.api-keys'
      AND `is_delete` = 0
);

UPDATE `sys_job`
SET `job_name` = '市级行政区三日天气刷新',
    `remark` = '平台调度服务调用 rental-business 刷新全部市级行政区未来三日天气，区县复用所属市级天气，业务时区为 Asia/Shanghai',
    `update_by` = 'migration',
    `update_time` = NOW()
WHERE `invoke_target` = 'regionWeatherRefreshJob.refreshThreeDayWeather()'
  AND `is_delete` = 0;
