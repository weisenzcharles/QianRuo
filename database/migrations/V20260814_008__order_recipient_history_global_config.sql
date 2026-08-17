-- 订单收件人历史快照、地址解析编码和平台全局配置。
--
-- 订单主表只保存当前收件信息，本表按订单编号保留每次保存的快照；
-- province_code/city_code/district_code 保存行政区划 adcode，amap_city_code 保存高德 citycode 快照。

USE `ry_business`;

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_order_recipient_history` (
  `recipient_history_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收件人历史快照ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单编号，作为订单关联业务键',
  `contact_name` VARCHAR(64) DEFAULT NULL COMMENT '联系人',
  `contact_phone` VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
  `contact_address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
  `province_code` VARCHAR(12) DEFAULT NULL COMMENT '省级行政区划adcode',
  `city_code` VARCHAR(12) DEFAULT NULL COMMENT '市级行政区划adcode',
  `district_code` VARCHAR(12) DEFAULT NULL COMMENT '区县级行政区划adcode',
  `amap_city_code` VARCHAR(8) DEFAULT NULL COMMENT '高德citycode快照，天气查询不使用',
  `province_name` VARCHAR(64) DEFAULT NULL COMMENT '省名称快照',
  `city_name` VARCHAR(64) DEFAULT NULL COMMENT '市名称快照',
  `district_name` VARCHAR(64) DEFAULT NULL COMMENT '区县名称快照',
  `parse_status` VARCHAR(20) NOT NULL DEFAULT 'failed' COMMENT '地址解析状态：success/partial/failed',
  `parse_message` VARCHAR(255) DEFAULT NULL COMMENT '地址解析说明',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人用户ID',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`recipient_history_id`),
  KEY `idx_recipient_history_order_no` (`tenant_id`, `member_id`, `order_no`, `is_delete`, `create_time`),
  KEY `idx_recipient_history_order_id` (`tenant_id`, `member_id`, `order_id`, `is_delete`),
  KEY `idx_recipient_history_amap_city` (`amap_city_code`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单收件人历史快照';

USE `ry-cloud`;

-- 平台全局配置使用现有 sys_config 表，不新增重复配置表；配置值通过统一缓存键共享。
INSERT INTO sys_config
    (config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, is_delete)
SELECT '全局配置缓存有效期', 'global.config.cache-ttl-days', '7', 'Y', 'migration', NOW(), 'migration', NOW(),
       '平台与商户服务全局配置缓存默认有效期（天）', 0
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'global.config.cache-ttl-days' AND is_delete = 0);

INSERT INTO sys_config
    (config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, is_delete)
SELECT '高德地图API Key', 'global.amap.api-key', '', 'N', 'migration', NOW(), 'migration', NOW(),
       '高德开放平台 Web 服务密钥，只在平台配置页面维护', 0
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'global.amap.api-key' AND is_delete = 0);

INSERT INTO sys_config
    (config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark, is_delete)
SELECT '高德天气接口地址', 'global.amap.weather-url', 'https://restapi.amap.com/v3/weather/weatherInfo', 'Y',
       'migration', NOW(), 'migration', NOW(), '高德天气查询接口地址', 0
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'global.amap.weather-url' AND is_delete = 0);

-- 将原“参数设置”页面正式命名为“系统配置”，沿用已有页面和权限编码。
UPDATE sys_menu
SET menu_name = '系统配置',
    parent_id = 1,
    order_num = 6,
    path = 'config',
    component = 'system/config/index',
    query = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'admin:config:list',
    icon = 'edit',
    client_type = 'platform',
    is_delete = 0,
    update_by = 'migration',
    update_time = NOW(),
    remark = '平台统一管理全局配置'
WHERE menu_id = 106;

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
     menu_type, visible, status, perms, icon, client_type, create_by, create_time, update_by, update_time, remark, is_delete)
SELECT 106, '系统配置', 1, 6, 'config', 'system/config/index', '', '', 1, 0,
       'C', '0', '0', 'admin:config:list', 'edit', 'platform', 'migration', NOW(), 'migration', NOW(),
       '平台统一管理全局配置', 0
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 106);

UPDATE sys_menu
SET menu_name = '系统配置查询', parent_id = 106, order_num = 1, path = '#', component = '', menu_type = 'F',
    visible = '0', status = '0', perms = 'admin:config:query', icon = '#', client_type = 'platform', is_delete = 0,
    update_by = 'migration', update_time = NOW()
WHERE menu_id = 1030;
UPDATE sys_menu
SET menu_name = '系统配置新增', parent_id = 106, order_num = 2, path = '#', component = '', menu_type = 'F',
    visible = '0', status = '0', perms = 'admin:config:add', icon = '#', client_type = 'platform', is_delete = 0,
    update_by = 'migration', update_time = NOW()
WHERE menu_id = 1031;
UPDATE sys_menu
SET menu_name = '系统配置修改', parent_id = 106, order_num = 3, path = '#', component = '', menu_type = 'F',
    visible = '0', status = '0', perms = 'admin:config:edit', icon = '#', client_type = 'platform', is_delete = 0,
    update_by = 'migration', update_time = NOW()
WHERE menu_id = 1032;
UPDATE sys_menu
SET menu_name = '系统配置删除', parent_id = 106, order_num = 4, path = '#', component = '', menu_type = 'F',
    visible = '0', status = '0', perms = 'admin:config:remove', icon = '#', client_type = 'platform', is_delete = 0,
    update_by = 'migration', update_time = NOW()
WHERE menu_id = 1033;
UPDATE sys_menu
SET menu_name = '系统配置导出', parent_id = 106, order_num = 5, path = '#', component = '', menu_type = 'F',
    visible = '0', status = '0', perms = 'admin:config:export', icon = '#', client_type = 'platform', is_delete = 0,
    update_by = 'migration', update_time = NOW()
WHERE menu_id = 1034;

-- 系统配置只授予平台 admin 角色，清理历史 common 角色残留授权。
UPDATE sys_role_menu
SET is_delete = 1
WHERE role_id <> 1 AND menu_id IN (106, 1030, 1031, 1032, 1033, 1034);

INSERT INTO sys_role_menu (role_id, menu_id, is_delete)
SELECT 1, menu_id, 0
FROM sys_menu
WHERE menu_id IN (106, 1030, 1031, 1032, 1033, 1034)
ON DUPLICATE KEY UPDATE is_delete = 0;
