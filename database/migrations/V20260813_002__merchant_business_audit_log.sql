-- 商户业务审计表：与平台请求级操作日志分离。
-- 平台 sys_oper_log 继续保存请求方式、请求地址、方法和响应等请求级信息；
-- 本迁移创建过一张独立业务审计表；后续已确认租赁域原有 ry_rental_audit_log
-- 具备同等业务字段并作为统一事实来源，独立表由 V20260814_006 软删除停用。
USE `ry_rental`;

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_business_audit_log` (
  `audit_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '审计主键',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `operator_user_id` BIGINT DEFAULT NULL COMMENT '商户用户ID',
  `operator_name` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '操作人',
  `business_module` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '业务模块',
  `operation_type` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '操作类型：CREATE/UPDATE/DELETE/LOGIN/CHANGE_PASSWORD等',
  `target_type` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '操作对象类型',
  `target_id` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '操作对象主键或业务编码',
  `target_name` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '操作对象名称',
  `description` VARCHAR(1000) NOT NULL DEFAULT '' COMMENT '业务描述',
  `before_content` JSON DEFAULT NULL COMMENT '变更前脱敏JSON',
  `after_content` JSON DEFAULT NULL COMMENT '变更后脱敏JSON',
  `ip_address` VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'IP地址',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '操作状态：0成功 1失败',
  `error_message` VARCHAR(2000) NOT NULL DEFAULT '' COMMENT '失败原因',
  `trace_id` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '调用链跟踪号',
  `operation_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`audit_id`),
  KEY `idx_merchant_business_audit_scope_time` (`tenant_id`, `member_id`, `operation_time`),
  KEY `idx_merchant_business_audit_module_type` (`tenant_id`, `member_id`, `business_module`, `operation_type`),
  KEY `idx_merchant_business_audit_operator` (`tenant_id`, `member_id`, `operator_user_id`, `operation_time`),
  KEY `idx_merchant_business_audit_target` (`tenant_id`, `member_id`, `target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户业务审计日志（商户端业务日志唯一数据源）';

-- 将历史请求日志表明确标识为兼容保留表，商户页面不再读取或新增该表记录。
ALTER TABLE `ry_rental_merchant_oper_log`
  COMMENT = '商户请求级日志兼容保留表（新日志由平台请求日志和商户业务审计日志分别保存）';
