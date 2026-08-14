-- 租赁核心主数据与库存事实迁移。
--
-- 约定：
-- 1. 本脚本只创建 rental-business 使用的 ry_business 表，不再为新业务写入 ry_rental。
-- 2. 套餐只保存版本化 BOM，库存只按资源池、逐件库存和数量库存计算。
-- 3. 所有删除均为 is_delete 软删除；所有商户表必须同时带 tenant_id 和 member_id。
-- 4. 升级旧环境时只在旧源表存在的情况下复制数据；新环境不创建执行面旧表。

CREATE DATABASE IF NOT EXISTS `ry_business`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `ry_business`;

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

-- 主数据表与库存流水表直接创建在 ry_business，避免新环境先创建 ry_rental 执行面表。
CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_asset_sn` (
  `sn_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'SN设备ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `warehouse_id` BIGINT DEFAULT NULL COMMENT '当前仓库ID',
  `sn_code` VARCHAR(128) NOT NULL COMMENT 'SN编码',
  `barcode` VARCHAR(128) DEFAULT NULL COMMENT '条码',
  `qr_code` VARCHAR(255) DEFAULT NULL COMMENT '二维码内容',
  `asset_status` VARCHAR(32) NOT NULL DEFAULT 'available' COMMENT '设备状态',
  `condition_level` VARCHAR(32) DEFAULT NULL COMMENT '新旧程度/成色',
  `purchase_time` DATE DEFAULT NULL COMMENT '购买时间',
  `purchase_cost` DECIMAL(18,2) DEFAULT NULL COMMENT '采购成本',
  `activate_time` DATE DEFAULT NULL COMMENT '激活时间',
  `warranty_until` DATE DEFAULT NULL COMMENT '保修到期',
  `insurance_until` DATE DEFAULT NULL COMMENT '保险到期',
  `latest_photo_file_id` BIGINT DEFAULT NULL COMMENT '最新照片文件ID',
  `total_rent_count` INT NOT NULL DEFAULT 0 COMMENT '累计租赁次数',
  `recent_3m_rent_count` INT NOT NULL DEFAULT 0 COMMENT '近3个月租赁次数',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`sn_id`),
  UNIQUE KEY `uk_rental_asset_sn` (`member_id`, `sn_code`),
  KEY `idx_rental_asset_tenant_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_asset_product` (`product_id`),
  KEY `idx_rental_asset_warehouse_status` (`warehouse_id`, `asset_status`),
  KEY `idx_rental_asset_insurance` (`insurance_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备SN';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_accessory_item` (
  `accessory_item_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配件实例ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `product_id` BIGINT NOT NULL COMMENT '配件商品ID',
  `warehouse_id` BIGINT DEFAULT NULL COMMENT '仓库ID',
  `item_code` VARCHAR(128) NOT NULL COMMENT '配件编码/SN/虚拟ID',
  `stock_mode` VARCHAR(20) NOT NULL DEFAULT 'qty' COMMENT '来源模式：sn qty',
  `item_status` VARCHAR(32) NOT NULL DEFAULT 'available' COMMENT '配件状态',
  `compatible_model_ids` VARCHAR(500) DEFAULT NULL COMMENT '兼容型号ID列表',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  `status` TINYINT(1) NOT NULL DEFAULT 0,
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`accessory_item_id`),
  UNIQUE KEY `uk_rental_accessory_item` (`member_id`, `item_code`),
  KEY `idx_rental_accessory_product` (`product_id`),
  KEY `idx_rental_accessory_warehouse_status` (`warehouse_id`, `item_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配件实例';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_package_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `package_id` BIGINT NOT NULL COMMENT '套餐ID',
  `product_id` BIGINT NOT NULL COMMENT '商品/配件ID',
  `item_role` VARCHAR(32) NOT NULL DEFAULT 'accessory' COMMENT '角色：main主机 accessory配件',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `required_flag` TINYINT NOT NULL DEFAULT 1 COMMENT '是否必选',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_rental_package_item_package` (`package_id`),
  KEY `idx_rental_package_item_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐物料明细';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_inventory_ledger` (
  `ledger_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存流水ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `warehouse_id` BIGINT DEFAULT NULL COMMENT '当前仓库ID',
  `product_id` BIGINT DEFAULT NULL COMMENT '商品ID',
  `sn_id` BIGINT DEFAULT NULL COMMENT 'SN ID',
  `accessory_item_id` BIGINT DEFAULT NULL COMMENT '配件实例ID',
  `change_type` VARCHAR(64) NOT NULL COMMENT '变更类型',
  `from_status` VARCHAR(32) DEFAULT NULL COMMENT '原状态',
  `to_status` VARCHAR(32) DEFAULT NULL COMMENT '新状态',
  `change_qty` INT NOT NULL DEFAULT 1 COMMENT '变更数量',
  `ref_type` VARCHAR(64) DEFAULT NULL COMMENT '关联类型',
  `ref_id` BIGINT DEFAULT NULL COMMENT '关联ID',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '原因',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`ledger_id`),
  KEY `idx_rental_inventory_ledger_tenant` (`tenant_id`, `member_id`),
  KEY `idx_rental_inventory_ledger_sn` (`sn_id`, `create_time`),
  KEY `idx_rental_inventory_ledger_ref` (`ref_type`, `ref_id`),
  KEY `idx_rental_inventory_ledger_wh_product` (`warehouse_id`, `product_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存状态流水';

DELIMITER $$
DROP PROCEDURE IF EXISTS `qianruo_copy_master_business_table`$$
CREATE PROCEDURE `qianruo_copy_master_business_table`(IN table_name_value VARCHAR(128))
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'ry_rental'
          AND table_name = table_name_value
          AND table_type = 'BASE TABLE'
    ) THEN
        SET @qianruo_copy_master_sql = CONCAT(
            'INSERT IGNORE INTO `ry_business`.`', table_name_value,
            '` SELECT * FROM `ry_rental`.`', table_name_value, '`'
        );
        PREPARE qianruo_copy_master_statement FROM @qianruo_copy_master_sql;
        EXECUTE qianruo_copy_master_statement;
        DEALLOCATE PREPARE qianruo_copy_master_statement;
    END IF;
END$$
CALL `qianruo_copy_master_business_table`('ry_rental_asset_sn')$$
CALL `qianruo_copy_master_business_table`('ry_rental_accessory_item')$$
CALL `qianruo_copy_master_business_table`('ry_rental_package_item')$$
CALL `qianruo_copy_master_business_table`('ry_rental_inventory_ledger')$$
DROP PROCEDURE IF EXISTS `qianruo_copy_master_business_table`$$
DELIMITER ;

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
CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_inventory_reservation` (
  `reservation_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存预占ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `package_id` BIGINT DEFAULT NULL COMMENT '套餐ID',
  `warehouse_id` BIGINT DEFAULT NULL COMMENT '仓库ID',
  `reserved_qty` INT NOT NULL DEFAULT 1 COMMENT '预占数量',
  `reservation_status` VARCHAR(32) NOT NULL DEFAULT 'reserved' COMMENT '状态：reserved locked released consumed expired',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`reservation_id`),
  KEY `idx_rental_reservation_order` (`order_id`),
  KEY `idx_rental_reservation_stock` (`member_id`, `warehouse_id`, `product_id`, `reservation_status`),
  KEY `idx_rental_reservation_expire` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预占';
