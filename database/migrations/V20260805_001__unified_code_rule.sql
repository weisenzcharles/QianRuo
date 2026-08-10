-- 统一业务编码规则与原子自增序列。
--
-- 规则说明：
-- 1. code_length 表示最终编码总长度，包含 prefix；数字序号宽度为 code_length - prefix 长度。
-- 2. current_value 只能由平台编码服务在行锁事务内递增，管理端不能重置或直接修改。
-- 3. SN 是厂商实物序列号，属于外部标识，不纳入本表生成；系统内部逐件编码和业务编码必须纳入本表。
-- 4. 规则不提供物理删除，is_delete 仅用于全局软删除约定，避免历史配置被复用。

USE `ry-cloud`;

CREATE TABLE IF NOT EXISTS `sys_code_rule` (
  `rule_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编码规则ID',
  `code_type` VARCHAR(64) NOT NULL COMMENT '编码类型键',
  `code_name` VARCHAR(128) NOT NULL COMMENT '编码规则名称',
  `prefix_text` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '编码前缀',
  `code_length` INT NOT NULL COMMENT '最终编码总长度，包含前缀',
  `current_value` BIGINT NOT NULL DEFAULT 0 COMMENT '已分配的最大数字序号',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态：0启用 1停用',
  `create_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`rule_id`),
  UNIQUE KEY `uk_sys_code_rule_type` (`code_type`),
  KEY `idx_sys_code_rule_status` (`status`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统统一业务编码规则';

INSERT INTO `sys_code_rule`
(`code_type`, `code_name`, `prefix_text`, `code_length`, `current_value`, `status`, `create_by`, `create_time`, `remark`, `is_delete`)
VALUES
('merchant', '商户编码', 'M', 12, 0, 0, 'migration', NOW(), '平台商户主体编码', 0),
('product_class', '产品类目编码', 'CLS', 12, 0, 0, 'migration', NOW(), '平台多级产品类目编码', 0),
('warehouse', '仓库编码', 'WH', 12, 0, 0, 'migration', NOW(), '商户仓库编码', 0),
('stock_pool', '资源池编码', 'POOL', 12, 0, 0, 'migration', NOW(), '商户库存资源池编码', 0),
('product', '商品编码', 'P', 12, 0, 0, 'migration', NOW(), '商户商品SKU编码', 0),
('accessory_item', '逐件配件编码', 'ACC', 12, 0, 0, 'migration', NOW(), '逐件管理配件内部编码', 0),
('package', '套餐编码', 'PKG', 12, 0, 0, 'migration', NOW(), '商户套餐模板编码', 0),
('order', '订单号', 'ORD', 20, 0, 0, 'migration', NOW(), '租赁订单系统编码', 0),
('inventory_unit', '库存单元编码', 'UNIT', 16, 0, 0, 'migration', NOW(), '系统内部逐件库存单元编码', 0)
ON DUPLICATE KEY UPDATE
  `code_name` = VALUES(`code_name`),
  `is_delete` = 0;

-- 平台后台统一编码规则菜单及权限。
INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
 `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `client_type`, `icon`,
 `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_delete`)
VALUES
(2050, '统一编码规则', 2000, 8, 'code-rule', 'rental/codeRule/index', '', '', 1, 0, 'C', '0', '0', 'platform:code:list', 'platform', 'edit', 'migration', NOW(), 'migration', NOW(), '系统统一配置业务编码前缀和长度', 0),
(2051, '编码规则查询', 2050, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:code:query', 'platform', '#', 'migration', NOW(), 'migration', NOW(), '', 0),
(2052, '编码规则修改', 2050, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:code:edit', 'platform', '#', 'migration', NOW(), 'migration', NOW(), '', 0)
ON DUPLICATE KEY UPDATE
  `menu_name` = VALUES(`menu_name`), `parent_id` = VALUES(`parent_id`), `order_num` = VALUES(`order_num`),
  `path` = VALUES(`path`), `component` = VALUES(`component`), `menu_type` = VALUES(`menu_type`),
  `visible` = VALUES(`visible`), `status` = VALUES(`status`), `perms` = VALUES(`perms`),
  `client_type` = VALUES(`client_type`), `icon` = VALUES(`icon`), `update_by` = 'migration',
  `update_time` = NOW(), `remark` = VALUES(`remark`), `is_delete` = 0;

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `is_delete`)
SELECT 1, `menu_id`, 0
FROM `sys_menu`
WHERE `menu_id` IN (2050, 2051, 2052)
ON DUPLICATE KEY UPDATE `is_delete` = 0;
