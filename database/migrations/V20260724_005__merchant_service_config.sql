-- 为已有环境补齐 merchant-service 独立 Nacos 配置，不在配置中心保存数据库密码。
USE `ry-config`;

INSERT INTO `config_info` (
    `id`, `data_id`, `group_id`, `content`, `md5`, `gmt_create`, `gmt_modified`,
    `src_user`, `src_ip`, `app_name`, `tenant_id`, `c_desc`, `type`, `encrypted_data_key`
)
SELECT existing.next_id, 'merchant-service-dev.yml', 'DEFAULT_GROUP', existing.content,
       MD5(existing.content), NOW(), NOW(), 'qianruo_migration', '127.0.0.1',
       'merchant-service', '', '商户独立服务配置', 'yaml', ''
FROM (
    SELECT (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `config_info`) AS next_id,
           'spring:\n  data:\n    redis:\n      host: localhost\n      port: 6379\n      password: \n\nmybatis:\n  typeAliasesPackage: com.ruoyi.merchant.domain\n  mapperLocations: classpath:mapper/**/*.xml\n\nmerchant:\n  account:\n    passwordValidateDays: 0\n' AS content
) existing
WHERE NOT EXISTS (
    SELECT 1 FROM `config_info`
    WHERE `data_id` = 'merchant-service-dev.yml' AND `group_id` = 'DEFAULT_GROUP' AND `tenant_id` = ''
);

UPDATE `config_info`
SET `content` = 'spring:\n  data:\n    redis:\n      host: localhost\n      port: 6379\n      password: \n\nmybatis:\n  typeAliasesPackage: com.ruoyi.merchant.domain\n  mapperLocations: classpath:mapper/**/*.xml\n\nmerchant:\n  account:\n    passwordValidateDays: 0\n',
    `md5` = MD5('spring:\n  data:\n    redis:\n      host: localhost\n      port: 6379\n      password: \n\nmybatis:\n  typeAliasesPackage: com.ruoyi.merchant.domain\n  mapperLocations: classpath:mapper/**/*.xml\n\nmerchant:\n  account:\n    passwordValidateDays: 0\n'),
    `gmt_modified` = NOW()
WHERE `data_id` = 'merchant-service-dev.yml'
  AND `group_id` = 'DEFAULT_GROUP'
  AND `tenant_id` = ''
  AND TRIM(`content`) = '';
