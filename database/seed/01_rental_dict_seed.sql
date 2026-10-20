-- 租赁系统基础字典种子数据
-- 说明：适用于 RuoYi sys_dict_type/sys_dict_data 表。
-- 执行前请确认当前数据库已包含 RuoYi 原生系统表。

SET @add_client_type_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE sys_menu ADD COLUMN client_type VARCHAR(20) NOT NULL DEFAULT ''platform'' COMMENT ''客户端类型：platform平台后台 merchant商家端 all共用'' AFTER perms',
    'SELECT 1'
  )
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'sys_menu'
    AND column_name = 'client_type'
);
PREPARE add_client_type_stmt FROM @add_client_type_sql;
EXECUTE add_client_type_stmt;
DEALLOCATE PREPARE add_client_type_stmt;

UPDATE sys_menu SET client_type = 'platform' WHERE client_type IS NULL OR client_type = '';

CREATE TABLE IF NOT EXISTS `ry_rental`.`ry_rental_merchant_user` (
  `merchant_user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商户用户唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `user_name` VARCHAR(64) NOT NULL COMMENT '登录账号',
  `nick_name` VARCHAR(64) NOT NULL COMMENT '用户昵称',
  `phone` VARCHAR(32) NOT NULL COMMENT '手机号',
  `email` VARCHAR(64) DEFAULT NULL COMMENT '邮箱',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `password` VARCHAR(100) DEFAULT NULL COMMENT '登录密码',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '账号状态：0正常 1停用',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0正常 1删除',
  `login_ip` VARCHAR(128) DEFAULT NULL COMMENT '最后登录IP',
  `login_date` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `pwd_update_date` DATETIME DEFAULT NULL COMMENT '密码最后更新时间',
  `must_change_password` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否必须修改初始密码：0否 1是',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`merchant_user_id`),
  UNIQUE KEY `uk_rental_merchant_user_name` (`user_name`),
  UNIQUE KEY `uk_rental_merchant_user_phone` (`phone`),
  KEY `idx_rental_merchant_user_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_merchant_user_status` (`status`, `is_delete`)
) ENGINE=InnoDB AUTO_INCREMENT=100000 DEFAULT CHARSET=utf8mb4 COMMENT='商户端用户';

CREATE TABLE IF NOT EXISTS `ry_rental`.`ry_rental_merchant_menu` (
  `menu_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商户菜单唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `menu_name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单唯一编号',
  `order_num` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `path` VARCHAR(200) DEFAULT '' COMMENT '路由地址',
  `component` VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
  `query` VARCHAR(255) DEFAULT NULL COMMENT '路由参数',
  `route_name` VARCHAR(50) DEFAULT '' COMMENT '路由名称',
  `is_frame` INT NOT NULL DEFAULT 1 COMMENT '是否外链：0是 1否',
  `is_cache` INT NOT NULL DEFAULT 0 COMMENT '是否缓存：0缓存 1不缓存',
  `menu_type` CHAR(1) NOT NULL DEFAULT '' COMMENT '菜单类型：M目录 C菜单 F按钮',
  `visible` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '显示状态：0显示 1隐藏',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '菜单状态：0正常 1停用',
  `perms` VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
  `icon` VARCHAR(100) DEFAULT '#' COMMENT '菜单图标',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0正常 1删除',
  PRIMARY KEY (`menu_id`),
  UNIQUE KEY `uk_rental_merchant_menu_path` (`member_id`, `parent_id`, `path`),
  KEY `idx_rental_merchant_menu_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_merchant_menu_parent` (`member_id`, `parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2100 DEFAULT CHARSET=utf8mb4 COMMENT='商户端菜单权限';

CREATE TABLE IF NOT EXISTS `ry_rental`.`ry_rental_merchant_role` (
  `role_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商户角色唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `role_template_id` BIGINT DEFAULT NULL COMMENT '全局商户角色模板唯一编号',
  `role_name` VARCHAR(30) NOT NULL COMMENT '角色名称',
  `role_key` VARCHAR(100) NOT NULL COMMENT '角色权限字符串',
  `role_sort` INT NOT NULL COMMENT '显示顺序',
  `menu_check_strictly` TINYINT NOT NULL DEFAULT 1 COMMENT '菜单树选择项是否父子联动',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '角色状态：0正常 1停用',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0正常 1删除',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_rental_merchant_role_name` (`member_id`, `role_name`, `is_delete`),
  UNIQUE KEY `uk_rental_merchant_role_key` (`member_id`, `role_key`, `is_delete`),
  KEY `idx_rental_merchant_role_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_merchant_role_template` (`role_template_id`, `is_delete`)
) ENGINE=InnoDB AUTO_INCREMENT=100000 DEFAULT CHARSET=utf8mb4 COMMENT='商户端角色';

CREATE TABLE IF NOT EXISTS `ry_rental`.`ry_rental_merchant_role_template` (
  `role_template_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '全局商户角色模板唯一编号',
  `role_name` VARCHAR(30) NOT NULL COMMENT '角色名称',
  `role_key` VARCHAR(100) NOT NULL COMMENT '角色权限字符',
  `role_sort` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `menu_check_strictly` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '菜单树是否父子联动',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '角色状态：0正常 1停用',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_template_id`),
  UNIQUE KEY `uk_merchant_role_template_name` (`role_name`, `is_delete`),
  UNIQUE KEY `uk_merchant_role_template_key` (`role_key`, `is_delete`),
  KEY `idx_merchant_role_template_status` (`status`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台全局商户角色模板';

CREATE TABLE IF NOT EXISTS `ry_rental`.`ry_rental_merchant_role_template_menu` (
  `role_template_id` BIGINT NOT NULL COMMENT '全局商户角色模板唯一编号',
  `menu_id` BIGINT NOT NULL COMMENT '商户菜单唯一编号',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`role_template_id`, `menu_id`),
  KEY `idx_role_template_menu_menu` (`menu_id`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局商户角色模板菜单授权';

CREATE TABLE IF NOT EXISTS `ry_rental`.`ry_rental_merchant_role_menu` (
  `role_id` BIGINT NOT NULL COMMENT '商户角色唯一编号',
  `menu_id` BIGINT NOT NULL COMMENT '商户菜单唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  PRIMARY KEY (`role_id`, `menu_id`),
  KEY `idx_rental_merchant_role_menu_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_merchant_role_menu_menu` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户角色和菜单关联';

CREATE TABLE IF NOT EXISTS `ry_rental`.`ry_rental_merchant_user_role` (
  `merchant_user_id` BIGINT NOT NULL COMMENT '商户用户唯一编号',
  `role_id` BIGINT NOT NULL COMMENT '商户角色唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  PRIMARY KEY (`merchant_user_id`, `role_id`),
  KEY `idx_rental_merchant_user_role_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_merchant_user_role_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户用户和角色关联';

CREATE TABLE IF NOT EXISTS `ry_rental`.`ry_rental_merchant_user_menu` (
  `merchant_user_id` BIGINT NOT NULL COMMENT '商户用户唯一编号',
  `menu_id` BIGINT NOT NULL COMMENT '商户菜单唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  PRIMARY KEY (`merchant_user_id`, `menu_id`),
  KEY `idx_rental_merchant_user_menu_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_merchant_user_menu_menu` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户用户和菜单关联';

SET @rename_rental_user_member_sql = (
  SELECT IF(
    COUNT(*) > 0,
    'ALTER TABLE `ry_rental`.`ry_rental_user_member` CHANGE COLUMN `user_id` `merchant_user_id` BIGINT NOT NULL COMMENT ''商户用户唯一编号''',
    'SELECT 1'
  )
  FROM information_schema.columns
  WHERE table_schema = 'ry_rental'
    AND table_name = 'ry_rental_user_member'
    AND column_name = 'user_id'
);
PREPARE rename_rental_user_member_stmt FROM @rename_rental_user_member_sql;
EXECUTE rename_rental_user_member_stmt;
DEALLOCATE PREPARE rename_rental_user_member_stmt;

SET @rename_rental_user_warehouse_sql = (
  SELECT IF(
    COUNT(*) > 0,
    'ALTER TABLE `ry_rental`.`ry_rental_user_warehouse_scope` CHANGE COLUMN `user_id` `merchant_user_id` BIGINT NOT NULL COMMENT ''商户用户唯一编号''',
    'SELECT 1'
  )
  FROM information_schema.columns
  WHERE table_schema = 'ry_rental'
    AND table_name = 'ry_rental_user_warehouse_scope'
    AND column_name = 'user_id'
);
PREPARE rename_rental_user_warehouse_stmt FROM @rename_rental_user_warehouse_sql;
EXECUTE rename_rental_user_warehouse_stmt;
DEALLOCATE PREPARE rename_rental_user_warehouse_stmt;

-- 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '租赁订单状态', 'rental_order_status', '0', NULL, NOW(), '租赁业务订单状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'rental_order_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '租赁SN状态', 'rental_asset_status', '0', NULL, NOW(), '租赁设备SN状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'rental_asset_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '租赁物流状态', 'rental_logistics_status', '0', NULL, NOW(), '租赁物流路由状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'rental_logistics_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '周转市场帖子状态', 'rental_market_post_status', '0', NULL, NOW(), '库存周转市场帖子状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'rental_market_post_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '租赁商户类型', 'rental_member_type', '0', NULL, NOW(), '租赁商户主体类型'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'rental_member_type');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '租赁商户认证状态', 'rental_member_auth_status', '0', NULL, NOW(), '租赁商户认证审核状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'rental_member_auth_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '租赁会员等级', 'rental_vip_level', '0', NULL, NOW(), '租赁商户会员套餐等级'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'rental_vip_level');

-- 商户类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '个人', 'personal', 'rental_member_type', '', 'default', 'Y', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_member_type' AND dict_value='personal');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '企业', 'enterprise', 'rental_member_type', '', 'success', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_member_type' AND dict_value='enterprise');

-- 商户认证状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '未认证', 'unverified', 'rental_member_auth_status', '', 'info', 'Y', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_member_auth_status' AND dict_value='unverified');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '待审核', 'pending', 'rental_member_auth_status', '', 'warning', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_member_auth_status' AND dict_value='pending');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '已认证', 'verified', 'rental_member_auth_status', '', 'success', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_member_auth_status' AND dict_value='verified');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '已驳回', 'rejected', 'rental_member_auth_status', '', 'danger', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_member_auth_status' AND dict_value='rejected');

-- 会员等级
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '免费版', 'free', 'rental_vip_level', '', 'info', 'Y', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_vip_level' AND dict_value='free');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '初级会员', 'junior', 'rental_vip_level', '', 'primary', 0, '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_vip_level' AND dict_value='junior');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '高级会员', 'senior', 'rental_vip_level', '', 'success', 0, '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_vip_level' AND dict_value='senior');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '标准版', 'standard', 'rental_vip_level', '', 'default', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_vip_level' AND dict_value='standard');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '专业版', 'pro', 'rental_vip_level', '', 'success', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_vip_level' AND dict_value='pro');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '旗舰版', 'enterprise', 'rental_vip_level', '', 'warning', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_vip_level' AND dict_value='enterprise');

-- 订单状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '待发货', 'pending_ship', 'rental_order_status', '', 'default', 'Y', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='pending_ship');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已备货', 'prepared', 'rental_order_status', '', 'primary', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='prepared');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '发货在途', 'ship_in_transit', 'rental_order_status', '', 'primary', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='ship_in_transit');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '租用中', 'renting', 'rental_order_status', '', 'success', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='renting');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '归还中', 'returning', 'rental_order_status', '', 'warning', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='returning');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 6, '售后中', 'after_sales', 'rental_order_status', '', 'danger', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='after_sales');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 7, '已完结', 'completed', 'rental_order_status', '', 'success', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='completed');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 8, '已取消', 'cancelled', 'rental_order_status', '', 'info', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='cancelled');

-- SN状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '在库可售', 'available', 'rental_asset_status', '', 'success', 'Y', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_asset_status' AND dict_value='available');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已占用', 'reserved', 'rental_asset_status', '', 'warning', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_asset_status' AND dict_value='reserved');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '租用中', 'renting', 'rental_asset_status', '', 'primary', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_asset_status' AND dict_value='renting');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '归还在途', 'return_in_transit', 'rental_asset_status', '', 'warning', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_asset_status' AND dict_value='return_in_transit');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '报修', 'repairing', 'rental_asset_status', '', 'danger', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_asset_status' AND dict_value='repairing');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 6, '出售出库', 'sold', 'rental_asset_status', '', 'info', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_asset_status' AND dict_value='sold');

-- 物流状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '待下单', 'pending', 'rental_logistics_status', '', 'default', 'Y', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_logistics_status' AND dict_value='pending');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '待揽收', 'waiting_pickup', 'rental_logistics_status', '', 'warning', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_logistics_status' AND dict_value='waiting_pickup');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '运输中', 'in_transit', 'rental_logistics_status', '', 'primary', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_logistics_status' AND dict_value='in_transit');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '已签收', 'signed', 'rental_logistics_status', '', 'success', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_logistics_status' AND dict_value='signed');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '异常', 'abnormal', 'rental_logistics_status', '', 'danger', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_logistics_status' AND dict_value='abnormal');

-- 周转市场帖子状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '草稿', 'draft', 'rental_market_post_status', '', 'default', 'Y', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_market_post_status' AND dict_value='draft');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已发布', 'published', 'rental_market_post_status', '', 'success', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_market_post_status' AND dict_value='published');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '已下架', 'offline', 'rental_market_post_status', '', 'info', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_market_post_status' AND dict_value='offline');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '举报中', 'reported', 'rental_market_post_status', '', 'warning', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_market_post_status' AND dict_value='reported');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '违规下架', 'violation_offline', 'rental_market_post_status', '', 'danger', 'N', '0', NULL, NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_market_post_status' AND dict_value='violation_offline');

-- 平台后台商户管理菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2000, '商户后台', 0, 4, 'rental', NULL, '', '', 1, 0, 'M', '0', '0', '', 'peoples', NULL, NOW(), '', NULL, '租赁商户后台管理目录'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2000);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2000;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2001, '商户管理', 2000, 1, 'member', 'rental/member/index', '', '', 1, 0, 'C', '0', '0', 'platform:member:list', 'user', NULL, NOW(), '', NULL, '租赁商户主体管理'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2001);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2001;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2002, '商户查询', 2001, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:member:query', '#', NULL, NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2002);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2002;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2003, '商户新增', 2001, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:member:add', '#', NULL, NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2003);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2003;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2004, '商户修改', 2001, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:member:edit', '#', NULL, NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2004);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2004;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2005, '商户删除', 2001, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:member:remove', '#', NULL, NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2005);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2005;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2006, '商户导出', 2001, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:member:export', '#', NULL, NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2006);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2006;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2013, '商户审核', 2001, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:member:audit', '#', NULL, NOW(), '', NULL, '审核商户入驻申请'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2013);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2013;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2007, '商户用户', 2000, 2, 'merchant-user', 'rental/merchantUser/index', '', '', 1, 0, 'C', '0', '0', 'admin:merchantUser:list', 'user', NULL, NOW(), '', NULL, '平台商户用户管理'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2007);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2007;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2008, '商户用户查询', 2007, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'admin:merchantUser:query', '#', NULL, NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2008);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2008;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2009, '商户用户新增', 2007, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'admin:merchantUser:add', '#', NULL, NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2009);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2009;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2010, '商户用户修改', 2007, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'admin:merchantUser:edit', '#', NULL, NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2010);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2010;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2011, '商户用户删除', 2007, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'admin:merchantUser:remove', '#', NULL, NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2011);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2011;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2012, '商户用户重置密码', 2007, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'admin:merchantUser:resetPwd', '#', NULL, NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2012);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2012;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2020, '商户角色', 2000, 3, 'merchant-role', 'rental/merchantRole/index', '', '', 1, 0, 'C', '0', '0', 'admin:merchantRole:list', 'peoples', NULL, NOW(), '', NULL, '平台维护所有商户可用的全局角色模板'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2020);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2020;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2021, '商户角色查询', 2020, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'admin:merchantRole:query', '#', NULL, NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2021);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2021;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2022, '商户角色新增', 2020, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'admin:merchantRole:add', '#', NULL, NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2022);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2022;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2023, '商户角色修改', 2020, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'admin:merchantRole:edit', '#', NULL, NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2023);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2023;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2024, '商户角色删除', 2020, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'admin:merchantRole:remove', '#', NULL, NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2024);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2024;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2030, '产品分类', 2000, 6, 'product-class', 'rental/productClass/index', '', '', 1, 0, 'C', '0', '0', 'platform:class:list', 'tree-table', NULL, NOW(), '', NULL, '平台多级产品分类管理'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2030);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2030;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2031, '分类查询', 2030, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:class:query', '#', NULL, NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2031);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2031;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2032, '分类新增', 2030, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:class:add', '#', NULL, NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2032);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2032;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2033, '分类修改', 2030, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:class:edit', '#', NULL, NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2033);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2033;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2034, '分类删除', 2030, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:class:remove', '#', NULL, NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2034);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2034;

-- 商户端初始化只写入全局菜单模板，不创建演示商户账号。

INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2100, 0, 0, '首页', 0, 1, 'merchant/home', 'merchant/home/index', '', '', 1, 0, 'C', '0', '0', 'merchant:home:view', 'dashboard', NULL, NOW(), '商家端首页'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2100);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2110, 0, 0, '店铺', 0, 2, 'merchant/store', 'merchant/store/index', '', '', 1, 0, 'M', '0', '0', 'merchant:store:view', 'shop', NULL, NOW(), '店铺管理总入口'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2110);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2111, 0, 0, '商品管理', 2110, 1, 'merchant/product', 'merchant/product/index', '', '', 1, 0, 'M', '0', '0', 'merchant:product:view', 'goods', NULL, NOW(), '商品管理'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2111);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2112, 0, 0, '我的商品', 2111, 1, 'merchant/product/my', 'merchant/product/my/index', '', '', 1, 0, 'C', '0', '0', 'merchant:product:my:list', 'list', NULL, NOW(), '我的商品'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2112);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2113, 0, 0, '发布商品', 2111, 2, 'merchant/product/publish', 'merchant/product/publish/index', '', '', 1, 0, 'C', '0', '0', 'merchant:product:publish:add', 'upload', NULL, NOW(), '发布商品'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2113);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2114, 0, 0, '仓库管理', 2110, 2, 'merchant/warehouse', 'merchant/warehouse/index', '', '', 1, 0, 'C', '0', '0', 'merchant:warehouse:list', 'house', NULL, NOW(), '仓库管理'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2114);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2115, 0, 0, '店铺管理', 2110, 3, 'merchant/store/profile', 'merchant/profile/index', '', '', 1, 0, 'C', '0', '0', 'merchant:store:profile:view', 'shop', NULL, NOW(), '店铺资料与设置'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2115);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2116, 0, 0, '用户管理', 2110, 4, 'merchant/user', 'merchant/user/index', '', '', 1, 0, 'M', '0', '0', 'merchant:user:view', 'user', NULL, NOW(), '用户管理'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2116);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2117, 0, 0, '账号管理', 2116, 1, 'merchant/user/account', 'merchant/user/account/index', '', '', 1, 0, 'C', '0', '0', 'merchant:user:account:list', 'user', NULL, NOW(), '账号管理'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2117);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2118, 0, 0, '操作日志', 2116, 2, 'merchant/user/operlog', 'merchant/user/operlog/index', '', '', 1, 0, 'C', '0', '0', 'merchant:operlog:list', 'log', NULL, NOW(), '操作日志'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2118);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2120, 0, 0, '交易', 0, 3, 'merchant/trade', 'merchant/trade/index', '', '', 1, 0, 'M', '0', '0', 'merchant:trade:view', 'money', NULL, NOW(), '交易管理总入口'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2120);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2121, 0, 0, '订单管理', 2120, 1, 'merchant/order', 'merchant/order/index', '', '', 1, 0, 'C', '0', '0', 'merchant:order:list', 'document', NULL, NOW(), '订单管理'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2121);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2122, 0, 0, '库存管理', 2120, 2, 'merchant/inventory', 'merchant/inventory/index', '', '', 1, 0, 'C', '0', '0', 'merchant:inventory:list', 'box', NULL, NOW(), '库存管理'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2122);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2123, 0, 0, '物流管理', 2120, 3, 'merchant/logistics', 'merchant/logistics/index', '', '', 1, 0, 'C', '0', '0', 'merchant:logistics:list', 'van', NULL, NOW(), '物流管理'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2123);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2130, 0, 0, '数据', 0, 4, 'merchant/data', 'Layout', '', '', 1, 0, 'M', '0', '0', 'merchant:data:view', 'chart', NULL, NOW(), '经营数据目录'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2130);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2131, 0, 0, '订单概览', 2130, 1, 'merchant/data/order-overview', 'merchant/data/orderOverview/index', '', '', 1, 0, 'C', '0', '0', 'merchant:data:order-overview', 'document', NULL, NOW(), '订单概览'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2131);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2132, 0, 0, '经营趋势', 2130, 2, 'merchant/data/business-trend', 'merchant/data/businessTrend/index', '', '', 1, 0, 'C', '0', '0', 'merchant:data:business-trend', 'chart', NULL, NOW(), '经营趋势'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2132);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2133, 0, 0, '库存概览', 2130, 3, 'merchant/data/inventory-overview', 'merchant/data/index', '', '', 1, 0, 'C', '0', '0', 'merchant:trend:inventory', 'box', NULL, NOW(), '30 天内库存概况'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2133);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2140, 0, 0, '帮助', 0, 5, 'merchant/help', 'merchant/help/index', '', '', 1, 0, 'C', '0', '0', 'merchant:help:view', 'question', NULL, NOW(), '帮助中心'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2140);

INSERT IGNORE INTO `ry_rental`.`ry_rental_merchant_user_menu` (tenant_id, member_id, merchant_user_id, menu_id)
SELECT ur.tenant_id, ur.member_id, ur.merchant_user_id, rm.menu_id
FROM `ry_rental`.`ry_rental_merchant_user_role` ur
INNER JOIN `ry_rental`.`ry_rental_merchant_role_menu` rm
  ON rm.tenant_id = ur.tenant_id
 AND rm.member_id = ur.member_id
 AND rm.role_id = ur.role_id;

INSERT IGNORE INTO `ry_rental`.`ry_rental_merchant_user_menu` (tenant_id, member_id, merchant_user_id, menu_id)
SELECT um.tenant_id, um.member_id, um.merchant_user_id, m.parent_id
FROM `ry_rental`.`ry_rental_merchant_user_menu` um
INNER JOIN `ry_rental`.`ry_rental_merchant_menu` m
  ON m.menu_id = um.menu_id
WHERE m.tenant_id = 0
  AND m.member_id = 0
  AND m.is_delete = '0'
  AND m.parent_id <> 0;
