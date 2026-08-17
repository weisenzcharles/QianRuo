-- 市级三日天气扩展表的初始结构。
-- 每个市级行政区、每个预报日期只保留一条最新快照，保留软删除字段供统一数据治理使用。
CREATE DATABASE IF NOT EXISTS `ry_business` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_region_weather`
(
    `weather_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '天气记录ID',
    `region_code` VARCHAR(16) NOT NULL COMMENT '市级行政区划adcode',
    `region_name` VARCHAR(64) NOT NULL COMMENT '市级行政区划名称快照',
    `city_code` VARCHAR(16) DEFAULT NULL COMMENT '市级行政区划adcode',
    `city_name` VARCHAR(64) DEFAULT NULL COMMENT '市级行政区划名称快照',
    `province_code` VARCHAR(16) DEFAULT NULL COMMENT '所属省级行政区划adcode',
    `province_name` VARCHAR(64) DEFAULT NULL COMMENT '省名称快照',
    `amap_city_code` VARCHAR(16) NOT NULL COMMENT '高德citycode快照，天气查询不使用',
    `weather_date` DATE NOT NULL COMMENT '预报日期',
    `day_weather` VARCHAR(64) DEFAULT NULL COMMENT '白天天气',
    `night_weather` VARCHAR(64) DEFAULT NULL COMMENT '夜间天气',
    `day_temperature` VARCHAR(16) DEFAULT NULL COMMENT '白天温度',
    `night_temperature` VARCHAR(16) DEFAULT NULL COMMENT '夜间温度',
    `day_wind_direction` VARCHAR(64) DEFAULT NULL COMMENT '白天风向',
    `night_wind_direction` VARCHAR(64) DEFAULT NULL COMMENT '夜间风向',
    `day_wind_power` VARCHAR(32) DEFAULT NULL COMMENT '白天风力',
    `night_wind_power` VARCHAR(32) DEFAULT NULL COMMENT '夜间风力',
    `weather_icon` VARCHAR(32) DEFAULT NULL COMMENT '天气图标类型',
    `risk_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '天气风险标识：0否1是',
    `query_status` VARCHAR(20) NOT NULL DEFAULT 'failed' COMMENT '查询状态：success/failed',
    `query_message` VARCHAR(500) DEFAULT NULL COMMENT '查询说明',
    `report_time` VARCHAR(32) DEFAULT NULL COMMENT '高德报告时间',
    `raw_data_json` JSON DEFAULT NULL COMMENT '高德单日原始预报JSON',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人，0表示系统任务',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人，0表示系统任务',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标识',
    PRIMARY KEY (`weather_id`),
    UNIQUE KEY `uk_region_weather_region_date` (`region_code`, `weather_date`),
    KEY `idx_region_weather_date` (`weather_date`, `is_delete`),
    KEY `idx_region_weather_city` (`amap_city_code`, `weather_date`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='市级行政区三日天气预报扩展表';
