-- 库存状态机、预占释放、并发锁与幂等基础表。

CREATE DATABASE IF NOT EXISTS `ry_business`
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_inventory_pool_lock` (
  `lock_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存资源池锁ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `pool_id` BIGINT NOT NULL COMMENT '资源池ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `version` INT NOT NULL DEFAULT 0 COMMENT '锁行版本',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标识',
  PRIMARY KEY (`lock_id`),
  UNIQUE KEY `uk_rental_inventory_pool_lock` (`member_id`, `pool_id`, `warehouse_id`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存资源池并发锁';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_inventory_transition` (
  `transition_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存状态变更ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `product_id` BIGINT DEFAULT NULL COMMENT '商品ID',
  `unit_id` BIGINT DEFAULT NULL COMMENT '逐件库存单元ID',
  `sn_id` BIGINT DEFAULT NULL COMMENT '历史SN实例ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `from_status` VARCHAR(32) NOT NULL COMMENT '原状态',
  `to_status` VARCHAR(32) NOT NULL COMMENT '目标状态',
  `change_qty` INT NOT NULL DEFAULT 1 COMMENT '变更数量',
  `idempotency_key` VARCHAR(128) NOT NULL COMMENT '幂等键',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '变更原因',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标识',
  PRIMARY KEY (`transition_id`),
  UNIQUE KEY `uk_rental_inventory_transition_key` (`member_id`, `idempotency_key`, `is_delete`),
  KEY `idx_rental_inventory_transition_scope` (`tenant_id`, `member_id`, `warehouse_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存状态机变更历史';

SET @sql = IF((SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'ry_business' AND TABLE_NAME = 'ry_rental_inventory_transition' AND COLUMN_NAME = 'sn_id') = 0,
    'ALTER TABLE ry_business.ry_rental_inventory_transition ADD COLUMN sn_id BIGINT DEFAULT NULL COMMENT ''历史SN实例ID'' AFTER unit_id', 'SELECT 1'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_idempotent_request` (
  `request_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '幂等请求ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `request_scope` VARCHAR(64) NOT NULL COMMENT '业务范围',
  `idempotency_key` VARCHAR(128) NOT NULL COMMENT '幂等键',
  `request_hash` CHAR(64) NOT NULL COMMENT '请求体哈希',
  `request_status` VARCHAR(20) NOT NULL DEFAULT 'processing' COMMENT 'processing/succeeded/failed',
  `response_json` JSON DEFAULT NULL COMMENT '成功响应快照',
  `expire_time` DATETIME NOT NULL COMMENT '幂等记录有效期',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标识',
  PRIMARY KEY (`request_id`),
  UNIQUE KEY `uk_rental_idempotent_request` (`member_id`, `request_scope`, `idempotency_key`, `is_delete`),
  KEY `idx_rental_idempotent_expire` (`expire_time`, `request_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁业务幂等请求';

-- 现有预占表来自旧租赁表，按元数据补充新服务需要的幂等与生命周期字段。
SET @sql = IF((SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'ry_business' AND TABLE_NAME = 'ry_rental_inventory_reservation' AND COLUMN_NAME = 'reservation_no') = 0,
    'ALTER TABLE ry_business.ry_rental_inventory_reservation ADD COLUMN reservation_no VARCHAR(64) DEFAULT NULL COMMENT ''预占单号'' AFTER reservation_id', 'SELECT 1'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = IF((SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'ry_business' AND TABLE_NAME = 'ry_rental_inventory_reservation' AND COLUMN_NAME = 'idempotency_key') = 0,
    'ALTER TABLE ry_business.ry_rental_inventory_reservation ADD COLUMN idempotency_key VARCHAR(128) DEFAULT NULL COMMENT ''幂等键''', 'SELECT 1'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = IF((SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'ry_business' AND TABLE_NAME = 'ry_rental_inventory_reservation' AND COLUMN_NAME = 'release_reason') = 0,
    'ALTER TABLE ry_business.ry_rental_inventory_reservation ADD COLUMN release_reason VARCHAR(500) DEFAULT NULL COMMENT ''释放原因''', 'SELECT 1'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = IF((SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'ry_business' AND TABLE_NAME = 'ry_rental_inventory_reservation' AND COLUMN_NAME = 'version') = 0,
    'ALTER TABLE ry_business.ry_rental_inventory_reservation ADD COLUMN version INT NOT NULL DEFAULT 0 COMMENT ''乐观锁版本''', 'SELECT 1'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql = IF((SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'ry_business' AND TABLE_NAME = 'ry_rental_inventory_reservation_item' AND COLUMN_NAME = 'version') = 0,
    'ALTER TABLE ry_business.ry_rental_inventory_reservation_item ADD COLUMN version INT NOT NULL DEFAULT 0 COMMENT ''乐观锁版本''', 'SELECT 1'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql = IF((SELECT COUNT(1) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = 'ry_business' AND TABLE_NAME = 'ry_rental_inventory_reservation' AND INDEX_NAME = 'uk_rental_reservation_idempotency') = 0,
    'ALTER TABLE ry_business.ry_rental_inventory_reservation ADD UNIQUE KEY uk_rental_reservation_idempotency (member_id, idempotency_key, is_delete)', 'SELECT 1'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
