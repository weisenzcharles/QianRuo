-- 文件配额账户和文件操作审计台账。

CREATE DATABASE IF NOT EXISTS `ry_business`
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_file_quota_account` (
  `quota_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配额账户ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `quota_bytes` BIGINT NOT NULL DEFAULT 1073741824 COMMENT '配额总字节数',
  `used_bytes` BIGINT NOT NULL DEFAULT 0 COMMENT '已使用字节数',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标识',
  PRIMARY KEY (`quota_id`),
  UNIQUE KEY `uk_rental_file_quota_account` (`member_id`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户文件配额账户';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_file_quota_ledger` (
  `ledger_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文件配额流水ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `file_id` BIGINT DEFAULT NULL COMMENT '文件服务ID',
  `file_name` VARCHAR(255) DEFAULT NULL COMMENT '文件名',
  `file_url` VARCHAR(500) DEFAULT NULL COMMENT '文件地址',
  `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小字节',
  `change_type` VARCHAR(32) NOT NULL COMMENT 'upload/import/export/delete',
  `ref_type` VARCHAR(64) DEFAULT NULL COMMENT '关联业务类型',
  `ref_id` VARCHAR(128) DEFAULT NULL COMMENT '关联业务ID',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标识',
  PRIMARY KEY (`ledger_id`),
  KEY `idx_rental_file_quota_ledger_scope` (`tenant_id`, `member_id`, `create_time`),
  KEY `idx_rental_file_quota_ledger_file` (`file_id`, `file_url`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户文件配额审计流水';
