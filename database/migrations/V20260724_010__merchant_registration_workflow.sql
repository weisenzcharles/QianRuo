-- 商户公开注册、平台审核和审核后开通管理员账号流程。

SET @contact_phone_index_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'ry_rental'
      AND TABLE_NAME = 'ry_rental_member'
      AND INDEX_NAME = 'uk_rental_member_contact_phone'
);
SET @contact_phone_index_sql = IF(
    @contact_phone_index_exists = 0,
    'ALTER TABLE `ry_rental`.`ry_rental_member` ADD UNIQUE KEY `uk_rental_member_contact_phone` (`contact_phone`)',
    'SELECT 1'
);
PREPARE contact_phone_index_stmt FROM @contact_phone_index_sql;
EXECUTE contact_phone_index_stmt;
DEALLOCATE PREPARE contact_phone_index_stmt;

-- 公开接口不要求登录，但仍由 merchant-service 和 file-service 做字段、文件类型与大小校验。
UPDATE `ry-config`.`config_info`
SET `content` = REPLACE(
        `content`,
        '      - /auth/register',
        '      - /auth/register\n      - /merchant/public/registration/**\n      - /file/public/merchant-registration/upload'
    ),
    `gmt_modified` = NOW()
WHERE `data_id` = 'ruoyi-gateway-dev.yml'
  AND `group_id` = 'DEFAULT_GROUP'
  AND `content` NOT LIKE '%/merchant/public/registration/**%';

UPDATE `ry-config`.`config_info`
SET `content` = REPLACE(
        `content`,
        '      - /auth/register',
        '      - /auth/register\n      - /file/public/merchant-registration/upload'
    ),
    `gmt_modified` = NOW()
WHERE `data_id` = 'ruoyi-gateway-dev.yml'
  AND `group_id` = 'DEFAULT_GROUP'
  AND `content` NOT LIKE '%/file/public/merchant-registration/upload%';

UPDATE `ry-config`.`config_info`
SET `md5` = MD5(`content`),
    `gmt_modified` = NOW()
WHERE `data_id` = 'ruoyi-gateway-dev.yml'
  AND `group_id` = 'DEFAULT_GROUP';

-- 商户管理权限使用 platform 命名空间，并增加独立审核权限。
UPDATE `ry-cloud`.`sys_menu`
SET `perms` = REPLACE(`perms`, 'admin:member:', 'platform:member:'),
    `update_by` = 'migration',
    `update_time` = NOW()
WHERE `perms` LIKE 'admin:member:%';

INSERT INTO `ry-cloud`.`sys_menu`
(`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
 `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`,
 `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2013, '商户审核', 2001, 5, '#', '', '', '', 1, 0, 'F', '0', '0',
       'platform:member:audit', '#', 'system', NOW(), '', NULL, '审核商户入驻申请'
WHERE NOT EXISTS (SELECT 1 FROM `ry-cloud`.`sys_menu` WHERE `menu_id` = 2013);

UPDATE `ry-cloud`.`sys_menu`
SET `client_type` = 'platform', `perms` = 'platform:member:audit'
WHERE `menu_id` = 2013;
