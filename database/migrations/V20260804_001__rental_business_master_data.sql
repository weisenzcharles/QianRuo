-- 租赁核心主数据与库存事实迁移。
--
-- 约定：
-- 1. 本脚本只创建 rental-business 使用的 ry_business 表，不再为新业务写入 ry_rental。
-- 2. 套餐只保存版本化 BOM，库存只按资源池、逐件库存和数量库存计算。
-- 3. 所有删除均为 is_delete 软删除；所有商户表必须同时带 tenant_id 和 member_id。
-- 4. 旧表作为兼容数据源保留，但新接口不读取旧套餐库存快照和旧兼容型号字符串。

CREATE DATABASE IF NOT EXISTS `ry_business`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_stock_pool` (
  `pool_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存资源池ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `pool_code` VARCHAR(64) NOT NULL COMMENT '资源池编码',
  `pool_name` VARCHAR(128) NOT NULL COMMENT '资源池名称',
  `resource_type` VARCHAR(20) NOT NULL COMMENT '资源类型：device/accessory/consumable',
  `substitutable_flag` TINYINT NOT NULL DEFAULT 1 COMMENT '池内商品是否允许互相替代',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态：0启用 1停用',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  PRIMARY KEY (`pool_id`),
  UNIQUE KEY `uk_rental_stock_pool_code` (`member_id`, `pool_code`, `is_delete`),
  KEY `idx_rental_stock_pool_scope` (`tenant_id`, `member_id`, `status`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁库存资源池';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_stock_pool_compatibility` (
  `compatibility_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '资源池兼容关系ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `pool_id` BIGINT NOT NULL COMMENT '配件资源池ID',
  `model_id` BIGINT NOT NULL COMMENT '平台标准型号ID',
  `compatibility_type` VARCHAR(20) NOT NULL DEFAULT 'full' COMMENT '兼容类型：full/conditional',
  `condition_json` JSON DEFAULT NULL COMMENT '附加兼容条件',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  PRIMARY KEY (`compatibility_id`),
  UNIQUE KEY `uk_rental_pool_model` (`member_id`, `pool_id`, `model_id`, `is_delete`),
  KEY `idx_rental_pool_compatibility` (`tenant_id`, `member_id`, `pool_id`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存资源池型号兼容关系';

-- MySQL 8.4 不支持 ALTER TABLE ADD COLUMN/INDEX IF NOT EXISTS，使用元数据判断保证迁移幂等。
SET @rental_stock_pool_column_exists = (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'ry_business' AND TABLE_NAME = 'ry_rental_product'
      AND COLUMN_NAME = 'stock_pool_id'
);
SET @rental_stock_pool_column_sql = IF(
    @rental_stock_pool_column_exists = 0,
    'ALTER TABLE ry_business.ry_rental_product ADD COLUMN stock_pool_id BIGINT DEFAULT NULL COMMENT ''库存资源池ID'' AFTER model_id',
    'SELECT 1'
);
PREPARE rental_stock_pool_column_statement FROM @rental_stock_pool_column_sql;
EXECUTE rental_stock_pool_column_statement;
DEALLOCATE PREPARE rental_stock_pool_column_statement;

SET @rental_stock_pool_index_exists = (
    SELECT COUNT(1) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'ry_business' AND TABLE_NAME = 'ry_rental_product'
      AND INDEX_NAME = 'idx_rental_product_stock_pool'
);
SET @rental_stock_pool_index_sql = IF(
    @rental_stock_pool_index_exists = 0,
    'ALTER TABLE ry_business.ry_rental_product ADD INDEX idx_rental_product_stock_pool (member_id, stock_pool_id, is_delete)',
    'SELECT 1'
);
PREPARE rental_stock_pool_index_statement FROM @rental_stock_pool_index_sql;
EXECUTE rental_stock_pool_index_statement;
DEALLOCATE PREPARE rental_stock_pool_index_statement;

-- 主数据表与订单表分属同一业务服务，迁移时统一复制到 ry_business，避免新接口回读旧库。
CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_asset_sn`
  LIKE `ry_rental`.`ry_rental_asset_sn`;
CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_accessory_item`
  LIKE `ry_rental`.`ry_rental_accessory_item`;
CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_package_item`
  LIKE `ry_rental`.`ry_rental_package_item`;
CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_inventory_ledger`
  LIKE `ry_rental`.`ry_rental_inventory_ledger`;
INSERT IGNORE INTO `ry_business`.`ry_rental_asset_sn`
SELECT * FROM `ry_rental`.`ry_rental_asset_sn`;
INSERT IGNORE INTO `ry_business`.`ry_rental_accessory_item`
SELECT * FROM `ry_rental`.`ry_rental_accessory_item`;
INSERT IGNORE INTO `ry_business`.`ry_rental_package_item`
SELECT * FROM `ry_rental`.`ry_rental_package_item`;
INSERT IGNORE INTO `ry_business`.`ry_rental_inventory_ledger`
SELECT * FROM `ry_rental`.`ry_rental_inventory_ledger`;

-- 统一新服务使用的库存模式值；旧表中的 sn/qty 只作为历史数据输入。
UPDATE `ry_business`.`ry_rental_product`
SET stock_mode = 'serial'
WHERE stock_mode = 'sn';
UPDATE `ry_business`.`ry_rental_product`
SET stock_mode = 'quantity'
WHERE stock_mode = 'qty';
UPDATE `ry_business`.`ry_rental_accessory_item`
SET stock_mode = 'serial'
WHERE stock_mode = 'sn';
UPDATE `ry_business`.`ry_rental_accessory_item`
SET stock_mode = 'quantity'
WHERE stock_mode = 'qty';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_inventory_unit` (
  `unit_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '逐件库存单元ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品SKU ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `unit_code` VARCHAR(128) NOT NULL COMMENT 'SN或内部逐件编码',
  `unit_status` VARCHAR(32) NOT NULL DEFAULT 'available' COMMENT 'available/reserved/rented/repairing/lost/retired',
  `condition_level` VARCHAR(32) DEFAULT NULL COMMENT '成色',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  PRIMARY KEY (`unit_id`),
  UNIQUE KEY `uk_rental_inventory_unit_code` (`member_id`, `unit_code`, `is_delete`),
  KEY `idx_rental_inventory_unit_scope` (`tenant_id`, `member_id`, `warehouse_id`, `unit_status`, `is_delete`),
  KEY `idx_rental_inventory_unit_product` (`member_id`, `product_id`, `warehouse_id`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='逐件库存事实';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_inventory_balance` (
  `balance_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数量库存余额ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `product_id` BIGINT NOT NULL COMMENT '数量型商品SKU ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `stock_status` VARCHAR(32) NOT NULL DEFAULT 'available' COMMENT '库存状态',
  `on_hand_qty` INT NOT NULL DEFAULT 0 COMMENT '实物数量',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  PRIMARY KEY (`balance_id`),
  UNIQUE KEY `uk_rental_inventory_balance` (`member_id`, `warehouse_id`, `product_id`, `stock_status`, `is_delete`),
  KEY `idx_rental_inventory_balance_scope` (`tenant_id`, `member_id`, `warehouse_id`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数量库存余额';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_package_version` (
  `package_version_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '套餐版本ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `package_id` BIGINT NOT NULL COMMENT '套餐模板ID',
  `version_no` INT NOT NULL COMMENT '版本号',
  `version_status` VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT 'draft/active/retired',
  `effective_time` DATETIME DEFAULT NULL COMMENT '生效时间',
  `bom_hash` CHAR(64) NOT NULL COMMENT '规范化BOM哈希',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  PRIMARY KEY (`package_version_id`),
  UNIQUE KEY `uk_rental_package_version` (`member_id`, `package_id`, `version_no`, `is_delete`),
  KEY `idx_rental_package_version_active` (`member_id`, `package_id`, `version_status`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐版本';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_package_component` (
  `component_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '套餐组件ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `package_version_id` BIGINT NOT NULL COMMENT '套餐版本ID',
  `pool_id` BIGINT NOT NULL COMMENT '需求资源池ID',
  `component_role` VARCHAR(20) NOT NULL DEFAULT 'accessory' COMMENT 'main/accessory/consumable',
  `required_qty` INT NOT NULL DEFAULT 1 COMMENT '每套需求数量',
  `allocation_policy` VARCHAR(20) NOT NULL DEFAULT 'any' COMMENT 'any/fifo/fefo/manual',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT NULL,
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  PRIMARY KEY (`component_id`),
  UNIQUE KEY `uk_rental_package_component` (`member_id`, `package_version_id`, `pool_id`, `is_delete`),
  KEY `idx_rental_package_component_pool` (`member_id`, `pool_id`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐版本BOM组件';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_inventory_reservation_item` (
  `reservation_item_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存预占明细ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `reservation_id` BIGINT NOT NULL COMMENT '预占主记录ID',
  `pool_id` BIGINT NOT NULL COMMENT '资源池ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `required_qty` INT NOT NULL COMMENT '需求数量',
  `reservation_status` VARCHAR(32) NOT NULL DEFAULT 'reserved' COMMENT 'reserved/released/consumed/expired',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  PRIMARY KEY (`reservation_item_id`),
  KEY `idx_rental_reservation_item_pool` (`tenant_id`, `member_id`, `pool_id`, `warehouse_id`, `reservation_status`, `is_delete`),
  KEY `idx_rental_reservation_item_reservation` (`reservation_id`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预占资源池明细';

-- 兼容已有订单预占主表；新业务通过预占明细按资源池计算，不再按套餐数量重复统计。
CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_inventory_reservation` LIKE `ry_rental`.`ry_rental_inventory_reservation`;
INSERT IGNORE INTO `ry_business`.`ry_rental_inventory_reservation`
SELECT * FROM `ry_rental`.`ry_rental_inventory_reservation`;
