-- 平台服务运行时边界迁移：服务名、公开路由和权限码统一使用 platform/admin。

USE `ry-config`;

-- 迁移平台服务的 Nacos 配置 Data ID；目标为空时优先继承旧配置。
UPDATE `ry-config`.`config_info` new_config
INNER JOIN `ry-config`.`config_info` old_config
  ON old_config.`data_id` = 'ruoyi-system-dev.yml'
 AND old_config.`group_id` = new_config.`group_id`
 AND old_config.`tenant_id` = new_config.`tenant_id`
SET new_config.`content` = old_config.`content`,
    new_config.`md5` = MD5(old_config.`content`),
    new_config.`gmt_modified` = NOW()
WHERE new_config.`data_id` = 'platform-service-dev.yml'
  AND new_config.`group_id` = 'DEFAULT_GROUP'
  AND TRIM(new_config.`content`) = ''
  AND TRIM(old_config.`content`) <> '';

UPDATE `ry-config`.`config_info` old_config
LEFT JOIN `ry-config`.`config_info` new_config
  ON new_config.`data_id` = 'platform-service-dev.yml'
 AND new_config.`group_id` = old_config.`group_id`
SET old_config.`data_id` = 'platform-service-dev.yml',
    old_config.`gmt_modified` = NOW()
WHERE old_config.`data_id` = 'ruoyi-system-dev.yml'
  AND old_config.`group_id` = 'DEFAULT_GROUP'
  AND new_config.`id` IS NULL;

DELETE old_config
FROM `ry-config`.`config_info` old_config
INNER JOIN `ry-config`.`config_info` new_config
  ON new_config.`data_id` = 'platform-service-dev.yml'
 AND new_config.`group_id` = old_config.`group_id`
WHERE old_config.`data_id` = 'ruoyi-system-dev.yml'
  AND old_config.`group_id` = 'DEFAULT_GROUP';

-- 历史迁移可能已经删除旧 Data ID，同时留下空配置；此处幂等补齐最低运行配置。
INSERT INTO `ry-config`.`config_info` (
    `id`, `data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`,
    `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `type`, `encrypted_data_key`
)
SELECT existing.next_id, 'platform-service-dev.yml', 'DEFAULT_GROUP', existing.content,
       MD5(existing.content), NOW(), NOW(), 'qianruo_migration', '127.0.0.1',
       'platform-service', '', '平台独立服务配置', 'yaml', ''
FROM (
    SELECT (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `ry-config`.`config_info`) AS next_id,
           'spring:\n  data:\n    redis:\n      host: localhost\n      port: 6379\n      password: \n\nmybatis:\n  typeAliasesPackage: com.ruoyi.system\n  mapperLocations: classpath:mapper/**/*.xml\n' AS content
) existing
WHERE NOT EXISTS (
    SELECT 1 FROM `ry-config`.`config_info`
    WHERE `data_id` = 'platform-service-dev.yml'
      AND `group_id` = 'DEFAULT_GROUP'
      AND `tenant_id` = ''
);

UPDATE `ry-config`.`config_info`
SET `content` = 'spring:\n  data:\n    redis:\n      host: localhost\n      port: 6379\n      password: \n\nmybatis:\n  typeAliasesPackage: com.ruoyi.system\n  mapperLocations: classpath:mapper/**/*.xml\n',
    `md5` = MD5('spring:\n  data:\n    redis:\n      host: localhost\n      port: 6379\n      password: \n\nmybatis:\n  typeAliasesPackage: com.ruoyi.system\n  mapperLocations: classpath:mapper/**/*.xml\n'),
    `gmt_modified` = NOW()
WHERE `data_id` = 'platform-service-dev.yml'
  AND `group_id` = 'DEFAULT_GROUP'
  AND `tenant_id` = ''
  AND TRIM(`content`) = '';

-- 将平台网关入口从 /system/** 切换到 /admin/**。
UPDATE `ry-config`.`config_info`
SET `content` = REPLACE(
        REPLACE(
            REPLACE(
                REPLACE(`content`, '- id: ruoyi-system', '- id: platform-service'),
                'uri: lb://ruoyi-system', 'uri: lb://platform-service'
            ),
            'Path=/system/**', 'Path=/admin/**'
        ),
        '/system/notice', '/admin/notice'
    ),
    `gmt_modified` = NOW()
WHERE `data_id` = 'ruoyi-gateway-dev.yml'
  AND `group_id` = 'DEFAULT_GROUP';

UPDATE `ry-config`.`config_info`
SET `content` = REPLACE(`content`, '"resource": "ruoyi-system"', '"resource": "platform-service"'),
    `gmt_modified` = NOW()
WHERE `data_id` = 'sentinel-ruoyi-gateway'
  AND `group_id` = 'DEFAULT_GROUP';

UPDATE `ry-config`.`config_info`
SET `md5` = MD5(`content`),
    `gmt_modified` = NOW()
WHERE `data_id` IN ('ruoyi-gateway-dev.yml', 'platform-service-dev.yml', 'sentinel-ruoyi-gateway')
  AND `group_id` = 'DEFAULT_GROUP';

-- 平台权限全部进入 admin:* 命名空间。
USE `ry-cloud`;

UPDATE `ry-cloud`.`sys_menu`
SET `perms` = CONCAT('admin:', SUBSTRING(`perms`, LOCATE(':', `perms`) + 1)),
    `update_by` = 'migration',
    `update_time` = NOW()
WHERE `perms` LIKE 'system:%'
   OR `perms` LIKE 'rental:%';

-- 商户菜单和角色只保留在 ry_rental 独立表，清除平台表中的历史副本。
DELETE role_menu
FROM `ry-cloud`.`sys_role_menu` role_menu
INNER JOIN `ry-cloud`.`sys_menu` menu ON menu.`menu_id` = role_menu.`menu_id`
WHERE menu.`client_type` = 'merchant';

DELETE FROM `ry-cloud`.`sys_menu`
WHERE `client_type` = 'merchant';

DELETE user_role
FROM `ry-cloud`.`sys_user_role` user_role
INNER JOIN `ry-cloud`.`sys_role` role ON role.`role_id` = user_role.`role_id`
WHERE role.`role_key` = 'merchant_admin';

DELETE FROM `ry-cloud`.`sys_role`
WHERE `role_key` = 'merchant_admin';
