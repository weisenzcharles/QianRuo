-- 商户公开注册、平台审核和审核后开通管理员账号流程。

USE `ry-cloud`;

SET @contact_phone_index_exists = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'ry_rental'
      AND TABLE_NAME = 'ry_rental_member'
      AND INDEX_NAME = 'uk_rental_member_contact_phone'
);
SET @contact_phone_index_sql = IF(
    @contact_phone_index_exists > 0,
    'ALTER TABLE `ry_rental`.`ry_rental_member` DROP INDEX `uk_rental_member_contact_phone`',
    'SELECT 1'
);
PREPARE contact_phone_index_stmt FROM @contact_phone_index_sql;
EXECUTE contact_phone_index_stmt;
DEALLOCATE PREPARE contact_phone_index_stmt;

SET @active_contact_phone_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'ry_rental'
      AND TABLE_NAME = 'ry_rental_member'
      AND COLUMN_NAME = 'active_contact_phone'
);
SET @active_contact_phone_sql = IF(
    @active_contact_phone_exists = 0,
    'ALTER TABLE `ry_rental`.`ry_rental_member` ADD COLUMN `active_contact_phone` VARCHAR(32) GENERATED ALWAYS AS (CASE WHEN `is_delete` = 0 AND `contact_phone` IS NOT NULL AND `contact_phone` <> '''' THEN `contact_phone` ELSE NULL END) STORED COMMENT ''有效商户手机号唯一键'' AFTER `is_delete`',
    'SELECT 1'
);
PREPARE active_contact_phone_stmt FROM @active_contact_phone_sql;
EXECUTE active_contact_phone_stmt;
DEALLOCATE PREPARE active_contact_phone_stmt;

DROP TEMPORARY TABLE IF EXISTS `qianruo_member_phone_keeper`;
CREATE TEMPORARY TABLE `qianruo_member_phone_keeper` AS
SELECT member_item.contact_phone,
       COALESCE(
           MAX(CASE WHEN EXISTS (
               SELECT 1
               FROM `ry_rental`.`ry_rental_merchant_user` user_item
               WHERE user_item.tenant_id = member_item.tenant_id
                 AND user_item.member_id = member_item.member_id
                 AND user_item.is_delete = 0
           ) THEN member_item.member_id END),
           MAX(CASE WHEN member_item.auth_status = 'verified' THEN member_item.member_id END),
           MAX(member_item.member_id)
       ) AS keep_member_id
FROM `ry_rental`.`ry_rental_member` member_item
WHERE member_item.is_delete = 0
  AND member_item.contact_phone IS NOT NULL
  AND member_item.contact_phone <> ''
GROUP BY member_item.contact_phone
HAVING COUNT(1) > 1;

UPDATE `ry_rental`.`ry_rental_member` member_item
INNER JOIN `qianruo_member_phone_keeper` keeper
        ON keeper.contact_phone = member_item.contact_phone
       AND keeper.keep_member_id <> member_item.member_id
SET member_item.is_delete = 1,
    member_item.update_by = 'migration',
    member_item.update_time = NOW(),
    member_item.remark = CONCAT_WS('; ', NULLIF(member_item.remark, ''), '重复手机号申请已归档')
WHERE member_item.is_delete = 0;

DROP TEMPORARY TABLE IF EXISTS `qianruo_member_phone_keeper`;

ALTER TABLE `ry_rental`.`ry_rental_member`
    ADD UNIQUE KEY `uk_rental_member_contact_phone` (`active_contact_phone`);

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
