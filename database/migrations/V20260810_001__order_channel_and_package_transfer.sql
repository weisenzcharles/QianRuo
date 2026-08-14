-- 订单渠道、套餐渠道绑定与转移。
--
-- 设计约定：
-- 1. platform 范围的渠道使用 tenant_id=0、member_id=0，只能由平台端维护，商户只能绑定使用。
-- 2. merchant 范围的渠道使用真实 tenant_id/member_id，只能由当前商户维护。
-- 3. 套餐渠道关系和转移记录均不可物理删除；订单转移只改当前订单的 channel_id，并写入不可变审计记录。

CREATE DATABASE IF NOT EXISTS `ry_business`
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_order_channel` (
  `channel_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单渠道ID',
  `tenant_id` BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID，平台渠道为0',
  `member_id` BIGINT NOT NULL DEFAULT 0 COMMENT '商户ID，平台渠道为0',
  `channel_code` VARCHAR(64) NOT NULL COMMENT '订单渠道编码',
  `channel_name` VARCHAR(128) NOT NULL COMMENT '订单渠道名称',
  `channel_type` VARCHAR(20) NOT NULL COMMENT '渠道类型：alliance联盟 custom商户自定义',
  `scope_type` VARCHAR(20) NOT NULL COMMENT '归属范围：platform平台 merchant商户',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0启用 1停用',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建商户用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新商户用户ID',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标识',
  PRIMARY KEY (`channel_id`),
  UNIQUE KEY `uk_rental_order_channel_code` (`member_id`, `channel_code`, `is_delete`),
  KEY `idx_rental_order_channel_scope` (`tenant_id`, `member_id`, `scope_type`, `status`, `is_delete`),
  KEY `idx_order_channel_create_by` (`create_by`),
  KEY `idx_order_channel_update_by` (`update_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁订单渠道';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_package_channel` (
  `binding_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '套餐渠道绑定ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `package_id` BIGINT NOT NULL COMMENT '套餐模板ID',
  `channel_id` BIGINT NOT NULL COMMENT '订单渠道ID',
  `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认渠道',
  `effective_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建商户用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新商户用户ID',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标识',
  PRIMARY KEY (`binding_id`),
  UNIQUE KEY `uk_rental_package_channel` (`member_id`, `package_id`, `channel_id`, `is_delete`),
  KEY `idx_rental_package_channel_package` (`tenant_id`, `member_id`, `package_id`, `is_delete`),
  KEY `idx_rental_package_channel_channel` (`tenant_id`, `member_id`, `channel_id`, `is_delete`),
  KEY `idx_package_channel_create_by` (`create_by`),
  KEY `idx_package_channel_update_by` (`update_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐订单渠道绑定';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_package_channel_transfer` (
  `transfer_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '套餐渠道转移记录ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `package_id` BIGINT NOT NULL COMMENT '套餐模板ID',
  `from_channel_id` BIGINT DEFAULT NULL COMMENT '原渠道ID',
  `to_channel_id` BIGINT NOT NULL COMMENT '目标渠道ID',
  `idempotency_key` VARCHAR(128) NOT NULL COMMENT '幂等键',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '转移原因',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标识',
  PRIMARY KEY (`transfer_id`),
  UNIQUE KEY `uk_rental_package_channel_transfer_key` (`member_id`, `idempotency_key`, `is_delete`),
  KEY `idx_rental_package_channel_transfer_package` (`tenant_id`, `member_id`, `package_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐订单渠道转移历史';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_order_channel_transfer` (
  `transfer_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单渠道转移记录ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `from_channel_id` BIGINT DEFAULT NULL COMMENT '原渠道ID',
  `to_channel_id` BIGINT NOT NULL COMMENT '目标渠道ID',
  `idempotency_key` VARCHAR(128) NOT NULL COMMENT '幂等键',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '转移原因',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标识',
  PRIMARY KEY (`transfer_id`),
  UNIQUE KEY `uk_rental_order_channel_transfer_key` (`member_id`, `order_id`, `idempotency_key`, `is_delete`),
  KEY `idx_rental_order_channel_transfer_order` (`tenant_id`, `member_id`, `order_id`, `create_time`),
  KEY `idx_rental_order_transfer_channel` (`member_id`, `to_channel_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单渠道转移历史';

-- 将历史套餐的 platform_code 转换为商户自定义渠道和正式绑定关系，避免历史数据失去渠道。
INSERT INTO `ry_business`.`ry_rental_order_channel`
    (tenant_id, member_id, channel_code, channel_name, channel_type, scope_type, create_by, create_time, remark, is_delete)
SELECT p.tenant_id, p.member_id, p.platform_code, p.platform_code, 'custom', 'merchant', NULL, NOW(), '由历史套餐渠道字段迁移', 0
FROM `ry_business`.`ry_rental_package_template` p
WHERE p.is_delete = 0 AND p.platform_code IS NOT NULL AND p.platform_code <> ''
  AND NOT EXISTS (
      SELECT 1 FROM `ry_business`.`ry_rental_order_channel` c
      WHERE c.member_id = p.member_id AND c.channel_code = p.platform_code AND c.is_delete = 0
  )
GROUP BY p.tenant_id, p.member_id, p.platform_code;

INSERT INTO `ry_business`.`ry_rental_package_channel`
    (tenant_id, member_id, package_id, channel_id, is_default, create_by, create_time, remark, is_delete)
SELECT p.tenant_id, p.member_id, p.package_id, c.channel_id, 1, NULL, NOW(), '由历史套餐渠道字段迁移', 0
FROM `ry_business`.`ry_rental_package_template` p
INNER JOIN `ry_business`.`ry_rental_order_channel` c
  ON c.member_id = p.member_id AND c.channel_code = p.platform_code AND c.is_delete = 0
WHERE p.is_delete = 0 AND p.platform_code IS NOT NULL AND p.platform_code <> ''
  AND NOT EXISTS (
      SELECT 1 FROM `ry_business`.`ry_rental_package_channel` pc
      WHERE pc.member_id = p.member_id AND pc.package_id = p.package_id
        AND pc.channel_id = c.channel_id AND pc.is_delete = 0
  );

USE `ry-cloud`;
INSERT INTO `sys_code_rule`
    (`code_type`, `code_name`, `prefix_text`, `code_length`, `current_value`, `status`, `create_by`, `create_time`, `remark`, `is_delete`)
VALUES ('order_channel', '订单渠道编码', 'CH', 12, 0, 0, 'migration', NOW(), '平台和商户订单渠道编码', 0)
ON DUPLICATE KEY UPDATE `code_name` = VALUES(`code_name`), `is_delete` = 0;
INSERT INTO `sys_code_rule`
    (`code_type`, `code_name`, `prefix_text`, `code_length`, `current_value`, `status`, `create_by`, `create_time`, `remark`, `is_delete`)
VALUES ('inventory_reservation', '库存预占单号', 'RSV', 16, 0, 0, 'migration', NOW(), '库存预占生命周期单号', 0)
ON DUPLICATE KEY UPDATE `code_name` = VALUES(`code_name`), `is_delete` = 0;

USE `ry_rental`;
-- 商户菜单模板；新增商户角色按模板复制，已有自定义角色通过商户权限管理显式授权。
INSERT INTO `ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2200, 0, 0, '订单渠道', 2120, 4, 'merchant/order-channel', 'merchant/orderChannel/index', '', '', 1, 0, 'C', '0', '0', 'merchant:orderChannel:list', 'connection', 'migration', NOW(), '订单渠道与套餐渠道转移'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2200);
INSERT INTO `ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2201, 0, 0, '库存工作流', 2122, 1, 'merchant/inventory/workflow', 'merchant/inventory/workflow/index', '', '', 1, 0, 'C', '0', '0', 'merchant:inventory:workflow:list', 'operation', 'migration', NOW(), '库存状态机、预占与释放'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2201);
INSERT INTO `ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2202, 0, 0, '批量导入导出', 2111, 6, 'merchant/batch', 'merchant/batch/index', '', '', 1, 0, 'C', '0', '0', 'merchant:batch:list', 'upload', 'migration', NOW(), '主数据批量导入导出'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2202);
INSERT INTO `ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2203, 0, 0, '文件配额审计', 2116, 3, 'merchant/user/file-quota', 'merchant/user/fileQuota/index', '', '', 1, 0, 'C', '0', '0', 'merchant:fileQuota:list', 'folder', 'migration', NOW(), '商户文件空间使用与审计'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2203);
INSERT INTO `ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2204, 0, 0, '协议确认', 2116, 4, 'merchant/user/protocol', 'merchant/user/protocol/index', '', '', 1, 0, 'C', '0', '0', 'merchant:protocol:list', 'documentation', 'migration', NOW(), '协议版本和确认记录'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2204);

INSERT INTO `ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
    (2220, 0, 0, '订单渠道查询', 2200, 1, 'permission/2220', '', '', '', 1, 0, 'F', '1', '0', 'merchant:orderChannel:query', '', 'migration', NOW(), '接口权限'),
    (2221, 0, 0, '订单渠道新增', 2200, 2, 'permission/2221', '', '', '', 1, 0, 'F', '1', '0', 'merchant:orderChannel:add', '', 'migration', NOW(), '接口权限'),
    (2222, 0, 0, '订单渠道修改', 2200, 3, 'permission/2222', '', '', '', 1, 0, 'F', '1', '0', 'merchant:orderChannel:edit', '', 'migration', NOW(), '接口权限'),
    (2223, 0, 0, '订单渠道转移', 2200, 4, 'permission/2223', '', '', '', 1, 0, 'F', '1', '0', 'merchant:orderChannel:transfer', '', 'migration', NOW(), '接口权限'),
    (2224, 0, 0, '库存预占释放', 2201, 2, 'permission/2224', '', '', '', 1, 0, 'F', '1', '0', 'merchant:inventory:reserve', '', 'migration', NOW(), '接口权限'),
    (2225, 0, 0, '批量导入', 2202, 1, 'permission/2225', '', '', '', 1, 0, 'F', '1', '0', 'merchant:batch:import', '', 'migration', NOW(), '接口权限'),
    (2226, 0, 0, '批量导出', 2202, 2, 'permission/2226', '', '', '', 1, 0, 'F', '1', '0', 'merchant:batch:export', '', 'migration', NOW(), '接口权限'),
    (2227, 0, 0, '文件配额查询', 2203, 1, 'permission/2227', '', '', '', 1, 0, 'F', '1', '0', 'merchant:fileQuota:list', '', 'migration', NOW(), '接口权限'),
    (2228, 0, 0, '协议确认', 2204, 1, 'permission/2228', '', '', '', 1, 0, 'F', '1', '0', 'merchant:protocol:accept', '', 'migration', NOW(), '接口权限')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), parent_id = VALUES(parent_id), perms = VALUES(perms), is_delete = 0;

UPDATE `ry_rental_merchant_menu`
SET is_delete = 0
WHERE tenant_id = 0 AND member_id = 0 AND menu_id IN (2200, 2201, 2202, 2203, 2204, 2220, 2221, 2222, 2223, 2224, 2225, 2226, 2227, 2228);

USE `ry-cloud`;
INSERT INTO `sys_menu`
    (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
     `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `client_type`, `icon`,
     `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_delete`)
VALUES
    (2060, '订单渠道', 2000, 9, 'order-channel', 'rental/orderChannel/index', '', '', 1, 0, 'C', '0', '0', 'platform:orderChannel:list', 'platform', 'connection', 'migration', NOW(), 'migration', NOW(), '平台联盟订单渠道', 0),
    (2061, '订单渠道新增', 2060, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:orderChannel:add', 'platform', '#', 'migration', NOW(), 'migration', NOW(), '', 0),
    (2062, '订单渠道修改', 2060, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:orderChannel:edit', 'platform', '#', 'migration', NOW(), 'migration', NOW(), '', 0),
    (2063, '订单渠道删除', 2060, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:orderChannel:remove', 'platform', '#', 'migration', NOW(), 'migration', NOW(), '', 0),
    (2070, '协议版本', 2000, 10, 'protocol', 'rental/protocol/index', '', '', 1, 0, 'C', '0', '0', 'platform:protocol:list', 'platform', 'documentation', 'migration', NOW(), 'migration', NOW(), '平台协议版本管理', 0),
    (2071, '协议新增', 2070, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:protocol:add', 'platform', '#', 'migration', NOW(), 'migration', NOW(), '', 0),
    (2072, '协议修改', 2070, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:protocol:edit', 'platform', '#', 'migration', NOW(), 'migration', NOW(), '', 0),
    (2073, '协议发布', 2070, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:protocol:publish', 'platform', '#', 'migration', NOW(), 'migration', NOW(), '', 0)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), parent_id = VALUES(parent_id), order_num = VALUES(order_num),
    path = VALUES(path), component = VALUES(component), menu_type = VALUES(menu_type), visible = VALUES(visible),
    status = VALUES(status), perms = VALUES(perms), client_type = VALUES(client_type), icon = VALUES(icon),
    update_by = 'migration', update_time = NOW(), remark = VALUES(remark), is_delete = 0;
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `is_delete`)
SELECT 1, menu_id, 0 FROM `sys_menu`
WHERE menu_id IN (2060, 2061, 2062, 2063, 2070, 2071, 2072, 2073)
ON DUPLICATE KEY UPDATE is_delete = 0;
