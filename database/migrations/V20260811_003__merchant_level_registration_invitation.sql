-- 商户付费/邀请码注册、可配置会员等级和推广邀请关系。
USE `ry_rental`;
SET NAMES utf8mb4;

DELIMITER $$
DROP PROCEDURE IF EXISTS `qianruo_add_member_registration_column`$$
CREATE PROCEDURE `qianruo_add_member_registration_column`(IN column_name_value VARCHAR(64), IN definition_value VARCHAR(1000))
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='ry_rental'
        AND table_name='ry_rental_member' AND column_name=column_name_value) THEN
        SET @column_sql=CONCAT('ALTER TABLE `ry_rental`.`ry_rental_member` ADD COLUMN `', column_name_value, '` ', definition_value);
        PREPARE column_statement FROM @column_sql; EXECUTE column_statement; DEALLOCATE PREPARE column_statement;
    END IF;
END$$
DROP PROCEDURE IF EXISTS `qianruo_add_member_registration_index`$$
CREATE PROCEDURE `qianruo_add_member_registration_index`(IN index_name_value VARCHAR(64), IN definition_value VARCHAR(1000))
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema='ry_rental'
        AND table_name='ry_rental_member' AND index_name=index_name_value) THEN
        SET @index_sql=CONCAT('ALTER TABLE `ry_rental`.`ry_rental_member` ADD KEY `', index_name_value, '` ', definition_value);
        PREPARE index_statement FROM @index_sql; EXECUTE index_statement; DEALLOCATE PREPARE index_statement;
    END IF;
END$$
DELIMITER ;
CALL `qianruo_add_member_registration_column`('level_id', 'BIGINT DEFAULT NULL COMMENT ''商户等级ID'' AFTER `vip_level`');
CALL `qianruo_add_member_registration_column`('registration_mode', 'VARCHAR(16) NOT NULL DEFAULT ''paid'' COMMENT ''注册方式：paid付费 invite邀请码'' AFTER `level_id`');
CALL `qianruo_add_member_registration_column`('registration_status', 'VARCHAR(24) NOT NULL DEFAULT ''completed'' COMMENT ''注册状态：awaiting_payment待支付 completed已完成'' AFTER `registration_mode`');
CALL `qianruo_add_member_registration_column`('inviter_member_id', 'BIGINT DEFAULT NULL COMMENT ''邀请商户ID'' AFTER `registration_status`');
CALL `qianruo_add_member_registration_index`('idx_rental_member_level', '(`level_id`, `is_delete`)');
CALL `qianruo_add_member_registration_index`('idx_rental_member_inviter', '(`tenant_id`, `inviter_member_id`, `is_delete`)');
DROP PROCEDURE IF EXISTS `qianruo_add_member_registration_column`;
DROP PROCEDURE IF EXISTS `qianruo_add_member_registration_index`;

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_level` (
  `level_id` BIGINT NOT NULL AUTO_INCREMENT,
  `level_code` VARCHAR(32) NOT NULL,
  `level_name` VARCHAR(64) NOT NULL,
  `level_order` INT NOT NULL DEFAULT 0,
  `monthly_order_requirement` INT NOT NULL DEFAULT 0,
  `inventory_requirement` INT NOT NULL DEFAULT 0,
  `invite_limit` INT NOT NULL DEFAULT 0,
  `registration_fee` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `status` CHAR(1) NOT NULL DEFAULT '0',
  `create_by` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`level_id`),
  UNIQUE KEY `uk_merchant_level_code` (`level_code`),
  KEY `idx_merchant_level_status` (`status`, `is_delete`, `level_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户等级配置';

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_invite_code` (
  `invite_code_id` BIGINT NOT NULL AUTO_INCREMENT,
  `tenant_id` BIGINT NOT NULL,
  `member_id` BIGINT NOT NULL,
  `invite_code` VARCHAR(32) NOT NULL,
  `used_count` INT NOT NULL DEFAULT 0,
  `status` CHAR(1) NOT NULL DEFAULT '0',
  `create_by` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`invite_code_id`),
  UNIQUE KEY `uk_merchant_invite_code` (`invite_code`),
  UNIQUE KEY `uk_merchant_active_inviter` (`tenant_id`, `member_id`, `is_delete`),
  KEY `idx_merchant_invite_status` (`status`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户邀请码';

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_invitation` (
  `invitation_id` BIGINT NOT NULL AUTO_INCREMENT,
  `tenant_id` BIGINT NOT NULL,
  `invite_code_id` BIGINT NOT NULL,
  `invite_code` VARCHAR(32) NOT NULL,
  `inviter_member_id` BIGINT NOT NULL,
  `invitee_member_id` BIGINT NOT NULL,
  `invitee_phone` VARCHAR(32) NOT NULL,
  `invite_status` VARCHAR(16) NOT NULL DEFAULT 'registered',
  `registered_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`invitation_id`),
  UNIQUE KEY `uk_merchant_invitation_invitee` (`tenant_id`, `invitee_member_id`, `is_delete`),
  KEY `idx_merchant_invitation_inviter` (`tenant_id`, `inviter_member_id`, `is_delete`),
  KEY `idx_merchant_invitation_code` (`invite_code`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户邀请记录';

CREATE TABLE IF NOT EXISTS `ry_rental_registration_payment` (
  `payment_id` BIGINT NOT NULL AUTO_INCREMENT,
  `payment_no` VARCHAR(64) NOT NULL,
  `member_id` BIGINT NOT NULL,
  `contact_phone` VARCHAR(32) NOT NULL,
  `amount` DECIMAL(12,2) NOT NULL,
  `payment_status` VARCHAR(16) NOT NULL DEFAULT 'pending',
  `paid_time` DATETIME DEFAULT NULL,
  `create_by` VARCHAR(64) NOT NULL DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) NOT NULL DEFAULT '',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`payment_id`),
  UNIQUE KEY `uk_registration_payment_no` (`payment_no`),
  UNIQUE KEY `uk_registration_payment_member` (`member_id`, `is_delete`),
  KEY `idx_registration_payment_status` (`payment_status`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户注册支付单';

INSERT INTO `ry_rental_merchant_level`
    (`level_id`, `level_code`, `level_name`, `level_order`, `monthly_order_requirement`,
     `inventory_requirement`, `invite_limit`, `registration_fee`, `status`, `create_by`, `update_by`, `remark`)
VALUES
    (1, 'junior', '初级会员', 1, 0, 0, 1, 99.00, '0', 'migration', 'migration', '默认注册等级'),
    (2, 'senior', '高级会员', 2, 100, 30, 10, 0.00, '0', 'migration', 'migration', '满足要求后可升级')
ON DUPLICATE KEY UPDATE
    `level_code` = `level_code`;

UPDATE `ry_rental_member`
SET `level_id` = CASE WHEN `vip_level` = 'senior' THEN 2 ELSE 1 END
WHERE `level_id` IS NULL AND `is_delete` = 0;

USE `ry-cloud`;
INSERT INTO `sys_menu`
    (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
     `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `client_type`,
     `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_delete`)
VALUES
    (2080, '商户等级', 2000, 8, 'merchant-level', 'rental/merchantLevel/index', '', '', 1, 0, 'C', '0', '0', 'platform:merchantLevel:list', 'star', 'platform', 'migration', NOW(), 'migration', NOW(), '商户等级配置', 0),
    (2081, '商户等级查询', 2080, 1, '#', '', '', '', 1, 0, 'F', '1', '0', 'platform:merchantLevel:query', '', 'platform', 'migration', NOW(), 'migration', NOW(), '', 0),
    (2082, '商户等级新增', 2080, 2, '#', '', '', '', 1, 0, 'F', '1', '0', 'platform:merchantLevel:add', '', 'platform', 'migration', NOW(), 'migration', NOW(), '', 0),
    (2083, '商户等级修改', 2080, 3, '#', '', '', '', 1, 0, 'F', '1', '0', 'platform:merchantLevel:edit', '', 'platform', 'migration', NOW(), 'migration', NOW(), '', 0),
    (2084, '商户等级删除', 2080, 4, '#', '', '', '', 1, 0, 'F', '1', '0', 'platform:merchantLevel:remove', '', 'platform', 'migration', NOW(), 'migration', NOW(), '', 0),
    (2090, '推广管理', 2000, 9, 'promotion', 'rental/promotion/index', '', '', 1, 0, 'C', '0', '0', 'platform:promotion:list', 'peoples', 'platform', 'migration', NOW(), 'migration', NOW(), '商户邀请码和邀请记录', 0)
ON DUPLICATE KEY UPDATE `menu_name`=VALUES(`menu_name`), `parent_id`=VALUES(`parent_id`), `order_num`=VALUES(`order_num`),
    `path`=VALUES(`path`), `component`=VALUES(`component`), `perms`=VALUES(`perms`), `icon`=VALUES(`icon`),
    `client_type`='platform', `update_by`='migration', `update_time`=NOW(), `is_delete`=0;

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `is_delete`)
SELECT role_item.role_id, menu_item.menu_id, 0
FROM `sys_role` role_item
CROSS JOIN (SELECT 2080 menu_id UNION ALL SELECT 2081 UNION ALL SELECT 2082 UNION ALL SELECT 2083
    UNION ALL SELECT 2084 UNION ALL SELECT 2090) menu_item
WHERE role_item.role_key='admin' AND role_item.is_delete=0
ON DUPLICATE KEY UPDATE `is_delete`=0;

USE `ry_rental`;
INSERT INTO `ry_rental_merchant_menu`
    (`menu_id`, `tenant_id`, `member_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
     `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_delete`)
VALUES
    (2210, 0, 0, '我的邀请', 2110, 9, 'merchant/invitation', 'merchant/invitation/index', '', '', 1, 0, 'C', '0', '0', 'merchant:invitation:list', 'peoples', 'migration', NOW(), 'migration', NOW(), '查看邀请码和已邀请商户', 0)
ON DUPLICATE KEY UPDATE `menu_name`=VALUES(`menu_name`), `parent_id`=VALUES(`parent_id`), `order_num`=VALUES(`order_num`),
    `path`=VALUES(`path`), `component`=VALUES(`component`), `perms`=VALUES(`perms`), `icon`=VALUES(`icon`),
    `update_by`='migration', `update_time`=NOW(), `is_delete`=0;

INSERT INTO `ry_rental_merchant_role_menu` (`role_id`, `menu_id`, `tenant_id`, `member_id`, `is_delete`)
SELECT role_item.role_id, 2210, role_item.tenant_id, role_item.member_id, 0
FROM `ry_rental_merchant_role` role_item
WHERE role_item.role_key='merchant_admin' AND role_item.status='0' AND role_item.is_delete=0
ON DUPLICATE KEY UPDATE `is_delete`=0;

INSERT INTO `ry_rental_merchant_user_menu` (`merchant_user_id`, `menu_id`, `tenant_id`, `member_id`, `is_delete`)
SELECT user_role.merchant_user_id, 2210, user_role.tenant_id, user_role.member_id, 0
FROM `ry_rental_merchant_user_role` user_role
INNER JOIN `ry_rental_merchant_role` role_item ON role_item.role_id=user_role.role_id
    AND role_item.tenant_id=user_role.tenant_id AND role_item.member_id=user_role.member_id
WHERE role_item.role_key='merchant_admin' AND role_item.status='0' AND role_item.is_delete=0 AND user_role.is_delete=0
ON DUPLICATE KEY UPDATE `is_delete`=0;
