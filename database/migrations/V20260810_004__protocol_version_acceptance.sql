-- 协议版本和商户用户确认记录。

CREATE DATABASE IF NOT EXISTS `ry_business`
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_protocol` (
  `protocol_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '协议ID',
  `protocol_key` VARCHAR(64) NOT NULL COMMENT '协议业务键',
  `version_no` INT NOT NULL COMMENT '协议版本号',
  `protocol_title` VARCHAR(200) NOT NULL COMMENT '协议标题',
  `protocol_content` LONGTEXT NOT NULL COMMENT '协议正文',
  `content_hash` CHAR(64) NOT NULL COMMENT '正文哈希',
  `protocol_status` VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT 'draft/published/retired',
  `effective_time` DATETIME DEFAULT NULL COMMENT '生效时间',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标识',
  PRIMARY KEY (`protocol_id`),
  UNIQUE KEY `uk_rental_protocol_version` (`protocol_key`, `version_no`, `is_delete`),
  KEY `idx_rental_protocol_status` (`protocol_key`, `protocol_status`, `effective_time`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台协议版本';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_protocol_acceptance` (
  `acceptance_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '协议确认ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `merchant_user_id` BIGINT NOT NULL COMMENT '商户用户ID',
  `protocol_id` BIGINT NOT NULL COMMENT '协议ID',
  `protocol_key` VARCHAR(64) NOT NULL COMMENT '协议业务键快照',
  `version_no` INT NOT NULL COMMENT '确认的版本号',
  `content_hash` CHAR(64) NOT NULL COMMENT '确认时正文哈希',
  `accepted_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '确认时间',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '确认人',
  `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标识',
  PRIMARY KEY (`acceptance_id`),
  UNIQUE KEY `uk_rental_protocol_acceptance` (`member_id`, `merchant_user_id`, `protocol_id`, `version_no`, `is_delete`),
  KEY `idx_rental_protocol_acceptance_scope` (`tenant_id`, `member_id`, `accepted_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户用户协议确认记录';
