USE `ry_rental`;

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_oper_log` (
  `oper_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `operator_user_id` BIGINT NOT NULL COMMENT '商户用户ID',
  `title` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '模块标题',
  `business_type` INT NOT NULL DEFAULT 0 COMMENT '业务类型',
  `method` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '方法名称',
  `request_method` VARCHAR(10) NOT NULL DEFAULT '' COMMENT '请求方式',
  `operator_type` INT NOT NULL DEFAULT 0 COMMENT '操作类别',
  `oper_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '操作人员',
  `oper_url` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '请求URL',
  `oper_ip` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '主机地址',
  `oper_param` VARCHAR(2000) NOT NULL DEFAULT '' COMMENT '请求参数',
  `json_result` VARCHAR(2000) NOT NULL DEFAULT '' COMMENT '返回参数',
  `status` INT NOT NULL DEFAULT 0 COMMENT '操作状态：0正常 1异常',
  `error_msg` VARCHAR(2000) NOT NULL DEFAULT '' COMMENT '错误消息',
  `oper_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `cost_time` BIGINT NOT NULL DEFAULT 0 COMMENT '消耗时间（毫秒）',
  PRIMARY KEY (`oper_id`),
  KEY `idx_merchant_oper_log_scope_time` (`tenant_id`, `member_id`, `oper_time`),
  KEY `idx_merchant_oper_log_scope_status` (`tenant_id`, `member_id`, `status`, `oper_time`),
  KEY `idx_merchant_oper_log_scope_user` (`tenant_id`, `member_id`, `operator_user_id`, `oper_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户操作日志';

INSERT INTO `ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component,
     `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon,
     create_by, create_time, remark)
SELECT 2190, 0, 0, '操作日志', 0, 10, 'merchant-operlog',
       'merchant/monitor/operlog/index', '', '', 1, 0, 'C', '0', '0',
       'merchant:operlog:list', 'log', 'migration', NOW(), '当前商户操作日志'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2190);

INSERT INTO `ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component,
     `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon,
     create_by, create_time, remark)
SELECT 2191, 0, 0, '日志导出', 2190, 1, 'operlog-export', '', '', '', 1, 0, 'F',
       '0', '0', 'merchant:operlog:export', '#', 'migration', NOW(), '导出当前商户操作日志'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2191);

UPDATE `ry_rental_merchant_menu`
SET tenant_id = 0,
    member_id = 0,
    menu_name = '操作日志',
    parent_id = 0,
    order_num = 10,
    path = 'merchant-operlog',
    component = 'merchant/monitor/operlog/index',
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'merchant:operlog:list',
    icon = 'log',
    update_by = 'migration',
    update_time = NOW(),
    remark = '当前商户操作日志',
    isdeleted = '0'
WHERE menu_id = 2190;

UPDATE `ry_rental_merchant_menu`
SET tenant_id = 0,
    member_id = 0,
    menu_name = '日志导出',
    parent_id = 2190,
    order_num = 1,
    path = 'operlog-export',
    component = '',
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'merchant:operlog:export',
    icon = '#',
    update_by = 'migration',
    update_time = NOW(),
    remark = '导出当前商户操作日志',
    isdeleted = '0'
WHERE menu_id = 2191;

INSERT IGNORE INTO `ry_rental_merchant_role_menu` (tenant_id, member_id, role_id, menu_id)
SELECT r.tenant_id, r.member_id, r.role_id, m.menu_id
FROM `ry_rental_merchant_role` r
INNER JOIN `ry_rental_merchant_menu` m ON m.menu_id IN (2190, 2191)
WHERE r.role_key = 'merchant_admin' AND r.status = '0' AND r.isdeleted = '0'
  AND m.tenant_id = 0 AND m.member_id = 0 AND m.status = '0' AND m.isdeleted = '0';

INSERT IGNORE INTO `ry_rental_merchant_user_menu` (tenant_id, member_id, merchant_user_id, menu_id)
SELECT ur.tenant_id, ur.member_id, ur.merchant_user_id, rm.menu_id
FROM `ry_rental_merchant_user_role` ur
INNER JOIN `ry_rental_merchant_role` r
    ON r.tenant_id = ur.tenant_id AND r.member_id = ur.member_id AND r.role_id = ur.role_id
INNER JOIN `ry_rental_merchant_role_menu` rm
    ON rm.tenant_id = ur.tenant_id AND rm.member_id = ur.member_id AND rm.role_id = ur.role_id
WHERE r.role_key = 'merchant_admin' AND r.status = '0' AND r.isdeleted = '0'
  AND rm.menu_id IN (2190, 2191);
