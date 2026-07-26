-- 将商户接口统一路由到独立 merchant-service，并保留 /merchant 前缀。
-- 该迁移仅修改 merchant-service 路由块，不影响平台 /system/** 路由。

UPDATE `ry-config`.`config_info`
SET `content` = REPLACE(
        `content`,
        '- id: ruoyi-merchant-system\n              uri: lb://ruoyi-system\n              predicates:\n                - Path=/merchant/**',
        '- id: merchant-service\n              uri: lb://merchant-service\n              predicates:\n                - Path=/merchant/**'
    ),
    `gmt_modified` = NOW()
WHERE `data_id` = 'ruoyi-gateway-dev.yml'
  AND `group_id` = 'DEFAULT_GROUP'
  AND `content` LIKE '%- id: ruoyi-merchant-system%'
  AND `content` LIKE '%Path=/merchant/**%';

-- 兼容已先修改路由 ID、但服务地址仍指向 ruoyi-system 的中间状态。
UPDATE `ry-config`.`config_info`
SET `content` = REPLACE(
        `content`,
        '- id: merchant-service\n              uri: lb://ruoyi-system\n              predicates:\n                - Path=/merchant/**',
        '- id: merchant-service\n              uri: lb://merchant-service\n              predicates:\n                - Path=/merchant/**'
    ),
    `gmt_modified` = NOW()
WHERE `data_id` = 'ruoyi-gateway-dev.yml'
  AND `group_id` = 'DEFAULT_GROUP'
  AND `content` LIKE '%- id: merchant-service%'
  AND `content` LIKE '%Path=/merchant/**%';

UPDATE `ry-config`.`config_info`
SET `md5` = MD5(`content`),
    `gmt_modified` = NOW()
WHERE `data_id` = 'ruoyi-gateway-dev.yml'
  AND `group_id` = 'DEFAULT_GROUP';
