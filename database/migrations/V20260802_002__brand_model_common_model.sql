-- 平台标准品牌型号与商户常用型号。
-- 标准型号由平台维护，商户只保存常用关系；所有删除均为 is_delete 软删除。
USE `ry-cloud`;

CREATE TABLE IF NOT EXISTS `ry_rental`.`ry_rental_brand_model` (
  `model_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标准型号ID',
  `class_id` BIGINT NOT NULL COMMENT '平台产品类目ID',
  `brand` VARCHAR(64) NOT NULL COMMENT '品牌',
  `model_name` VARCHAR(128) NOT NULL COMMENT '型号',
  `memory_spec` VARCHAR(64) DEFAULT NULL COMMENT '内存/规格',
  `official_flag` CHAR(1) NOT NULL DEFAULT '1' COMMENT '是否官方标准：1是 0否',
  `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0正常 1停用',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  `active_class_id` BIGINT GENERATED ALWAYS AS (
    CASE WHEN `is_delete` = 0 THEN `class_id` ELSE NULL END
  ) STORED COMMENT '有效型号类目唯一键',
  `active_brand` VARCHAR(64) GENERATED ALWAYS AS (
    CASE WHEN `is_delete` = 0 THEN `brand` ELSE NULL END
  ) STORED COMMENT '有效型号品牌唯一键',
  `active_model_name` VARCHAR(128) GENERATED ALWAYS AS (
    CASE WHEN `is_delete` = 0 THEN `model_name` ELSE NULL END
  ) STORED COMMENT '有效型号名称唯一键',
  `active_memory_spec` VARCHAR(64) GENERATED ALWAYS AS (
    CASE WHEN `is_delete` = 0 THEN COALESCE(`memory_spec`, '') ELSE NULL END
  ) STORED COMMENT '有效型号规格唯一键',
  PRIMARY KEY (`model_id`),
  UNIQUE KEY `uk_rental_brand_model_active_identity`
    (`active_class_id`, `active_brand`, `active_model_name`, `active_memory_spec`),
  KEY `idx_rental_brand_model_brand` (`brand`, `model_name`),
  KEY `idx_rental_brand_model_class` (`class_id`, `status`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统标准品牌型号';

CREATE TABLE IF NOT EXISTS `ry_rental`.`ry_rental_member_common_model` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `model_id` BIGINT NOT NULL COMMENT '标准型号ID',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rental_member_model` (`member_id`, `model_id`),
  KEY `idx_rental_member_common_model_scope` (`tenant_id`, `member_id`, `is_delete`, `sort_no`),
  KEY `idx_rental_member_common_model_model` (`model_id`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户常用型号';

-- MySQL 8.4 不支持 ALTER TABLE ADD COLUMN IF NOT EXISTS，逐列使用元数据判断保证重复执行安全。
SET @rental_brand_model_class_column_sql = IF(
    (SELECT COUNT(1) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = 'ry_rental' AND TABLE_NAME = 'ry_rental_brand_model' AND COLUMN_NAME = 'class_id') = 0,
    'ALTER TABLE ry_rental.ry_rental_brand_model ADD COLUMN class_id BIGINT DEFAULT NULL COMMENT ''平台产品类目ID'' AFTER model_id',
    'SELECT 1'
);
PREPARE rental_brand_model_class_column_statement FROM @rental_brand_model_class_column_sql;
EXECUTE rental_brand_model_class_column_statement;
DEALLOCATE PREPARE rental_brand_model_class_column_statement;

SET @rental_brand_model_update_by_column_sql = IF(
    (SELECT COUNT(1) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = 'ry_rental' AND TABLE_NAME = 'ry_rental_brand_model' AND COLUMN_NAME = 'update_by') = 0,
    'ALTER TABLE ry_rental.ry_rental_brand_model ADD COLUMN update_by VARCHAR(64) DEFAULT '''' AFTER create_time',
    'SELECT 1'
);
PREPARE rental_brand_model_update_by_column_statement FROM @rental_brand_model_update_by_column_sql;
EXECUTE rental_brand_model_update_by_column_statement;
DEALLOCATE PREPARE rental_brand_model_update_by_column_statement;

SET @rental_brand_model_update_time_column_sql = IF(
    (SELECT COUNT(1) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = 'ry_rental' AND TABLE_NAME = 'ry_rental_brand_model' AND COLUMN_NAME = 'update_time') = 0,
    'ALTER TABLE ry_rental.ry_rental_brand_model ADD COLUMN update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP AFTER update_by',
    'SELECT 1'
);
PREPARE rental_brand_model_update_time_column_statement FROM @rental_brand_model_update_time_column_sql;
EXECUTE rental_brand_model_update_time_column_statement;
DEALLOCATE PREPARE rental_brand_model_update_time_column_statement;

SET @rental_brand_model_is_delete_column_sql = IF(
    (SELECT COUNT(1) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = 'ry_rental' AND TABLE_NAME = 'ry_rental_brand_model' AND COLUMN_NAME = 'is_delete') = 0,
    'ALTER TABLE ry_rental.ry_rental_brand_model ADD COLUMN is_delete TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''软删除标识：0正常 1删除'' AFTER remark',
    'SELECT 1'
);
PREPARE rental_brand_model_is_delete_column_statement FROM @rental_brand_model_is_delete_column_sql;
EXECUTE rental_brand_model_is_delete_column_statement;
DEALLOCATE PREPARE rental_brand_model_is_delete_column_statement;

SET @rental_common_model_update_by_column_sql = IF(
    (SELECT COUNT(1) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = 'ry_rental' AND TABLE_NAME = 'ry_rental_member_common_model' AND COLUMN_NAME = 'update_by') = 0,
    'ALTER TABLE ry_rental.ry_rental_member_common_model ADD COLUMN update_by VARCHAR(64) DEFAULT '''' AFTER create_time',
    'SELECT 1'
);
PREPARE rental_common_model_update_by_column_statement FROM @rental_common_model_update_by_column_sql;
EXECUTE rental_common_model_update_by_column_statement;
DEALLOCATE PREPARE rental_common_model_update_by_column_statement;

SET @rental_common_model_update_time_column_sql = IF(
    (SELECT COUNT(1) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = 'ry_rental' AND TABLE_NAME = 'ry_rental_member_common_model' AND COLUMN_NAME = 'update_time') = 0,
    'ALTER TABLE ry_rental.ry_rental_member_common_model ADD COLUMN update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP AFTER update_by',
    'SELECT 1'
);
PREPARE rental_common_model_update_time_column_statement FROM @rental_common_model_update_time_column_sql;
EXECUTE rental_common_model_update_time_column_statement;
DEALLOCATE PREPARE rental_common_model_update_time_column_statement;

SET @rental_common_model_is_delete_column_sql = IF(
    (SELECT COUNT(1) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = 'ry_rental' AND TABLE_NAME = 'ry_rental_member_common_model' AND COLUMN_NAME = 'is_delete') = 0,
    'ALTER TABLE ry_rental.ry_rental_member_common_model ADD COLUMN is_delete TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''软删除标识：0正常 1删除'' AFTER update_time',
    'SELECT 1'
);
PREPARE rental_common_model_is_delete_column_statement FROM @rental_common_model_is_delete_column_sql;
EXECUTE rental_common_model_is_delete_column_statement;
DEALLOCATE PREPARE rental_common_model_is_delete_column_statement;

-- 平台品牌型号菜单及操作权限。
INSERT INTO `ry-cloud`.`sys_menu`
(`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
 `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `client_type`, `icon`,
 `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_delete`)
VALUES
(2040, '标准型号', 2000, 7, 'brand-model', 'rental/brandModel/index', '', '', 1, 0, 'C', '0', '0', 'platform:brandModel:list', 'platform', 'list', 'migration', NOW(), 'migration', NOW(), '平台标准型号管理', 0),
(2041, '标准型号查询', 2040, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:brandModel:query', 'platform', '#', 'migration', NOW(), 'migration', NOW(), '', 0),
(2042, '标准型号新增', 2040, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:brandModel:add', 'platform', '#', 'migration', NOW(), 'migration', NOW(), '', 0),
(2043, '标准型号修改', 2040, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:brandModel:edit', 'platform', '#', 'migration', NOW(), 'migration', NOW(), '', 0),
(2044, '标准型号删除', 2040, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:brandModel:remove', 'platform', '#', 'migration', NOW(), 'migration', NOW(), '', 0)
ON DUPLICATE KEY UPDATE
    `menu_name` = VALUES(`menu_name`), `parent_id` = VALUES(`parent_id`), `order_num` = VALUES(`order_num`),
    `path` = VALUES(`path`), `component` = VALUES(`component`), `menu_type` = VALUES(`menu_type`),
    `visible` = VALUES(`visible`), `status` = VALUES(`status`), `perms` = VALUES(`perms`),
    `client_type` = VALUES(`client_type`), `icon` = VALUES(`icon`), `update_by` = 'migration',
    `update_time` = NOW(), `remark` = VALUES(`remark`), `is_delete` = 0;

INSERT INTO `ry-cloud`.`sys_role_menu` (`role_id`, `menu_id`, `is_delete`)
SELECT 1, menu_id, 0
FROM `ry-cloud`.`sys_menu`
WHERE menu_id IN (2040, 2041, 2042, 2043, 2044)
ON DUPLICATE KEY UPDATE `is_delete` = 0;

-- 商户常用型号菜单挂到商品管理下，商户端只能读取本商户经营类目范围内的型号。
INSERT INTO `ry_rental`.`ry_rental_merchant_menu`
(`menu_id`, `tenant_id`, `member_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`,
 `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
 `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_delete`)
VALUES
(2194, 0, 0, '品牌型号', 2111, 1, 'merchant/brand-model', 'merchant/brandModel/index', '', '', 1, 0, 'C', '0', '0', 'merchant:brandModel:list', 'list', 'migration', NOW(), 'migration', NOW(), '维护商户常用品牌型号', 0),
(2195, 0, 0, '品牌型号保存', 2194, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'merchant:brandModel:edit', '#', 'migration', NOW(), 'migration', NOW(), '', 0)
ON DUPLICATE KEY UPDATE
    `menu_name` = VALUES(`menu_name`), `parent_id` = VALUES(`parent_id`), `order_num` = VALUES(`order_num`),
    `path` = VALUES(`path`), `component` = VALUES(`component`), `menu_type` = VALUES(`menu_type`),
    `visible` = VALUES(`visible`), `status` = VALUES(`status`), `perms` = VALUES(`perms`),
    `icon` = VALUES(`icon`), `update_by` = 'migration', `update_time` = NOW(),
    `remark` = VALUES(`remark`), `is_delete` = 0;

INSERT INTO `ry_rental`.`ry_rental_merchant_role_menu`
(`tenant_id`, `member_id`, `role_id`, `menu_id`, `is_delete`)
SELECT r.tenant_id, r.member_id, r.role_id, m.menu_id, 0
FROM `ry_rental`.`ry_rental_merchant_role` r
INNER JOIN `ry_rental`.`ry_rental_merchant_menu` m
    ON m.tenant_id = 0 AND m.member_id = 0 AND m.menu_id IN (2194, 2195) AND m.is_delete = 0
WHERE r.role_key = 'merchant_admin' AND r.status = '0' AND r.is_delete = 0
ON DUPLICATE KEY UPDATE `is_delete` = 0;

INSERT INTO `ry_rental`.`ry_rental_merchant_user_menu`
(`tenant_id`, `member_id`, `merchant_user_id`, `menu_id`, `is_delete`)
SELECT ur.tenant_id, ur.member_id, ur.merchant_user_id, rm.menu_id, 0
FROM `ry_rental`.`ry_rental_merchant_user_role` ur
INNER JOIN `ry_rental`.`ry_rental_merchant_role` r
    ON r.tenant_id = ur.tenant_id AND r.member_id = ur.member_id AND r.role_id = ur.role_id
   AND r.role_key = 'merchant_admin' AND r.is_delete = 0
INNER JOIN `ry_rental`.`ry_rental_merchant_role_menu` rm
    ON rm.tenant_id = ur.tenant_id AND rm.member_id = ur.member_id AND rm.role_id = ur.role_id
   AND rm.menu_id IN (2194, 2195) AND rm.is_delete = 0
WHERE ur.is_delete = 0
ON DUPLICATE KEY UPDATE `is_delete` = 0;
