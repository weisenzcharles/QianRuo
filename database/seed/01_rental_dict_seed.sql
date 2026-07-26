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
  `merchant_user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商户用户ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `user_name` VARCHAR(64) NOT NULL COMMENT '登录账号',
  `nick_name` VARCHAR(64) NOT NULL COMMENT '用户昵称',
  `phone` VARCHAR(32) NOT NULL COMMENT '手机号',
  `email` VARCHAR(64) DEFAULT NULL COMMENT '邮箱',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `password` VARCHAR(100) DEFAULT NULL COMMENT '登录密码',
  `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '账号状态：0正常 1停用',
  `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0存在 2删除',
  `login_ip` VARCHAR(128) DEFAULT NULL COMMENT '最后登录IP',
  `login_date` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `pwd_update_date` DATETIME DEFAULT NULL COMMENT '密码最后更新时间',
  `must_change_password` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否必须修改初始密码：0否 1是',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`merchant_user_id`),
  UNIQUE KEY `uk_rental_merchant_user_name` (`user_name`),
  UNIQUE KEY `uk_rental_merchant_user_phone` (`phone`),
  KEY `idx_rental_merchant_user_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_merchant_user_status` (`status`, `del_flag`)
) ENGINE=InnoDB AUTO_INCREMENT=100000 DEFAULT CHARSET=utf8mb4 COMMENT='商户端用户';

CREATE TABLE IF NOT EXISTS `ry_rental`.`ry_rental_merchant_menu` (
  `menu_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商户菜单ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `menu_name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID',
  `order_num` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `path` VARCHAR(200) DEFAULT '' COMMENT '路由地址',
  `component` VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
  `query` VARCHAR(255) DEFAULT NULL COMMENT '路由参数',
  `route_name` VARCHAR(50) DEFAULT '' COMMENT '路由名称',
  `is_frame` INT NOT NULL DEFAULT 1 COMMENT '是否外链：0是 1否',
  `is_cache` INT NOT NULL DEFAULT 0 COMMENT '是否缓存：0缓存 1不缓存',
  `menu_type` CHAR(1) NOT NULL DEFAULT '' COMMENT '菜单类型：M目录 C菜单 F按钮',
  `visible` CHAR(1) NOT NULL DEFAULT '0' COMMENT '显示状态：0显示 1隐藏',
  `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '菜单状态：0正常 1停用',
  `perms` VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
  `icon` VARCHAR(100) DEFAULT '#' COMMENT '菜单图标',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
  `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0存在 2删除',
  PRIMARY KEY (`menu_id`),
  UNIQUE KEY `uk_rental_merchant_menu_path` (`member_id`, `parent_id`, `path`),
  KEY `idx_rental_merchant_menu_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_merchant_menu_parent` (`member_id`, `parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2100 DEFAULT CHARSET=utf8mb4 COMMENT='商户端菜单权限';

CREATE TABLE IF NOT EXISTS `ry_rental`.`ry_rental_merchant_role` (
  `role_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商户角色ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `role_name` VARCHAR(30) NOT NULL COMMENT '角色名称',
  `role_key` VARCHAR(100) NOT NULL COMMENT '角色权限字符串',
  `role_sort` INT NOT NULL COMMENT '显示顺序',
  `menu_check_strictly` TINYINT NOT NULL DEFAULT 1 COMMENT '菜单树选择项是否父子联动',
  `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '角色状态：0正常 1停用',
  `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0存在 2删除',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_rental_merchant_role_name` (`member_id`, `role_name`, `del_flag`),
  UNIQUE KEY `uk_rental_merchant_role_key` (`member_id`, `role_key`, `del_flag`),
  KEY `idx_rental_merchant_role_member` (`tenant_id`, `member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100000 DEFAULT CHARSET=utf8mb4 COMMENT='商户端角色';

CREATE TABLE IF NOT EXISTS `ry_rental`.`ry_rental_merchant_role_menu` (
  `role_id` BIGINT NOT NULL COMMENT '商户角色ID',
  `menu_id` BIGINT NOT NULL COMMENT '商户菜单ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  PRIMARY KEY (`role_id`, `menu_id`),
  KEY `idx_rental_merchant_role_menu_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_merchant_role_menu_menu` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户角色和菜单关联';

CREATE TABLE IF NOT EXISTS `ry_rental`.`ry_rental_merchant_user_role` (
  `merchant_user_id` BIGINT NOT NULL COMMENT '商户用户ID',
  `role_id` BIGINT NOT NULL COMMENT '商户角色ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  PRIMARY KEY (`merchant_user_id`, `role_id`),
  KEY `idx_rental_merchant_user_role_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_merchant_user_role_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户用户和角色关联';

CREATE TABLE IF NOT EXISTS `ry_rental`.`ry_rental_merchant_user_menu` (
  `merchant_user_id` BIGINT NOT NULL COMMENT '商户用户ID',
  `menu_id` BIGINT NOT NULL COMMENT '商户菜单ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  PRIMARY KEY (`merchant_user_id`, `menu_id`),
  KEY `idx_rental_merchant_user_menu_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_merchant_user_menu_menu` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户用户和菜单关联';

SET @rename_rental_user_member_sql = (
  SELECT IF(
    COUNT(*) > 0,
    'ALTER TABLE `ry_rental`.`ry_rental_user_member` CHANGE COLUMN `user_id` `merchant_user_id` BIGINT NOT NULL COMMENT ''商户用户ID''',
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
    'ALTER TABLE `ry_rental`.`ry_rental_user_warehouse_scope` CHANGE COLUMN `user_id` `merchant_user_id` BIGINT NOT NULL COMMENT ''商户用户ID''',
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
SELECT '租赁订单状态', 'rental_order_status', '0', 'system', NOW(), '租赁业务订单状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'rental_order_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '租赁SN状态', 'rental_asset_status', '0', 'system', NOW(), '租赁设备SN状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'rental_asset_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '租赁物流状态', 'rental_logistics_status', '0', 'system', NOW(), '租赁物流路由状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'rental_logistics_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '周转市场帖子状态', 'rental_market_post_status', '0', 'system', NOW(), '库存周转市场帖子状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'rental_market_post_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '租赁商户类型', 'rental_member_type', '0', 'system', NOW(), '租赁商户主体类型'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'rental_member_type');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '租赁商户认证状态', 'rental_member_auth_status', '0', 'system', NOW(), '租赁商户认证审核状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'rental_member_auth_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '租赁会员等级', 'rental_vip_level', '0', 'system', NOW(), '租赁商户会员套餐等级'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'rental_vip_level');

-- 商户类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '个人', 'personal', 'rental_member_type', '', 'default', 'Y', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_member_type' AND dict_value='personal');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '企业', 'enterprise', 'rental_member_type', '', 'success', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_member_type' AND dict_value='enterprise');

-- 商户认证状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '未认证', 'unverified', 'rental_member_auth_status', '', 'info', 'Y', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_member_auth_status' AND dict_value='unverified');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '待审核', 'pending', 'rental_member_auth_status', '', 'warning', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_member_auth_status' AND dict_value='pending');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '已认证', 'verified', 'rental_member_auth_status', '', 'success', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_member_auth_status' AND dict_value='verified');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '已驳回', 'rejected', 'rental_member_auth_status', '', 'danger', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_member_auth_status' AND dict_value='rejected');

-- 会员等级
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '免费版', 'free', 'rental_vip_level', '', 'info', 'Y', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_vip_level' AND dict_value='free');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '标准版', 'standard', 'rental_vip_level', '', 'default', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_vip_level' AND dict_value='standard');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '专业版', 'pro', 'rental_vip_level', '', 'success', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_vip_level' AND dict_value='pro');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '旗舰版', 'enterprise', 'rental_vip_level', '', 'warning', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_vip_level' AND dict_value='enterprise');

-- 订单状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '待发货', 'pending_ship', 'rental_order_status', '', 'default', 'Y', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='pending_ship');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已备货', 'prepared', 'rental_order_status', '', 'primary', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='prepared');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '发货在途', 'ship_in_transit', 'rental_order_status', '', 'primary', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='ship_in_transit');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '租用中', 'renting', 'rental_order_status', '', 'success', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='renting');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '归还中', 'returning', 'rental_order_status', '', 'warning', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='returning');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 6, '售后中', 'after_sales', 'rental_order_status', '', 'danger', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='after_sales');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 7, '已完结', 'completed', 'rental_order_status', '', 'success', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='completed');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 8, '已取消', 'cancelled', 'rental_order_status', '', 'info', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='cancelled');

-- SN状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '在库可售', 'available', 'rental_asset_status', '', 'success', 'Y', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_asset_status' AND dict_value='available');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已占用', 'reserved', 'rental_asset_status', '', 'warning', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_asset_status' AND dict_value='reserved');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '租用中', 'renting', 'rental_asset_status', '', 'primary', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_asset_status' AND dict_value='renting');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '归还在途', 'return_in_transit', 'rental_asset_status', '', 'warning', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_asset_status' AND dict_value='return_in_transit');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '报修', 'repairing', 'rental_asset_status', '', 'danger', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_asset_status' AND dict_value='repairing');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 6, '出售出库', 'sold', 'rental_asset_status', '', 'info', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_asset_status' AND dict_value='sold');

-- 物流状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '待下单', 'pending', 'rental_logistics_status', '', 'default', 'Y', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_logistics_status' AND dict_value='pending');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '待揽收', 'waiting_pickup', 'rental_logistics_status', '', 'warning', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_logistics_status' AND dict_value='waiting_pickup');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '运输中', 'in_transit', 'rental_logistics_status', '', 'primary', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_logistics_status' AND dict_value='in_transit');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '已签收', 'signed', 'rental_logistics_status', '', 'success', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_logistics_status' AND dict_value='signed');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '异常', 'abnormal', 'rental_logistics_status', '', 'danger', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_logistics_status' AND dict_value='abnormal');

-- 周转市场帖子状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '草稿', 'draft', 'rental_market_post_status', '', 'default', 'Y', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_market_post_status' AND dict_value='draft');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已发布', 'published', 'rental_market_post_status', '', 'success', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_market_post_status' AND dict_value='published');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '已下架', 'offline', 'rental_market_post_status', '', 'info', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_market_post_status' AND dict_value='offline');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '举报中', 'reported', 'rental_market_post_status', '', 'warning', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_market_post_status' AND dict_value='reported');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '违规下架', 'violation_offline', 'rental_market_post_status', '', 'danger', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_market_post_status' AND dict_value='violation_offline');

-- 平台后台商户管理菜单
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2000, '商户后台', 0, 4, 'rental', NULL, '', '', 1, 0, 'M', '0', '0', '', 'peoples', 'system', NOW(), '', NULL, '租赁商户后台管理目录'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2000);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2000;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2001, '商户管理', 2000, 1, 'member', 'rental/member/index', '', '', 1, 0, 'C', '0', '0', 'platform:member:list', 'user', 'system', NOW(), '', NULL, '租赁商户主体管理'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2001);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2001;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2002, '商户查询', 2001, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:member:query', '#', 'system', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2002);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2002;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2003, '商户新增', 2001, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:member:add', '#', 'system', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2003);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2003;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2004, '商户修改', 2001, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:member:edit', '#', 'system', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2004);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2004;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2005, '商户删除', 2001, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:member:remove', '#', 'system', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2005);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2005;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2006, '商户导出', 2001, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:member:export', '#', 'system', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2006);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2006;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2013, '商户审核', 2001, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:member:audit', '#', 'system', NOW(), '', NULL, '审核商户入驻申请'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2013);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2013;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2007, '商户用户', 2000, 2, 'merchant-user', 'rental/merchantUser/index', '', '', 1, 0, 'C', '0', '0', 'admin:merchantUser:list', 'user', 'system', NOW(), '', NULL, '平台商户用户管理'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2007);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2007;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2008, '商户用户查询', 2007, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'admin:merchantUser:query', '#', 'system', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2008);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2008;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2009, '商户用户新增', 2007, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'admin:merchantUser:add', '#', 'system', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2009);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2009;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2010, '商户用户修改', 2007, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'admin:merchantUser:edit', '#', 'system', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2010);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2010;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2011, '商户用户删除', 2007, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'admin:merchantUser:remove', '#', 'system', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2011);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2011;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2012, '商户用户重置密码', 2007, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'admin:merchantUser:resetPwd', '#', 'system', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2012);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2012;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2020, '商户权限', 2000, 3, 'merchant-role', 'rental/merchantRole/index', '', '', 1, 0, 'C', '0', '0', 'admin:merchantRole:list', 'peoples', 'system', NOW(), '', NULL, '平台商户角色权限管理'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2020);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2020;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2021, '商户权限查询', 2020, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'admin:merchantRole:query', '#', 'system', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2021);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2021;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2022, '商户权限新增', 2020, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'admin:merchantRole:add', '#', 'system', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2022);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2022;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2023, '商户权限修改', 2020, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'admin:merchantRole:edit', '#', 'system', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2023);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2023;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2024, '商户权限删除', 2020, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'admin:merchantRole:remove', '#', 'system', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2024);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2024;

-- 商户账号、菜单与角色只写入 ry_rental 独立表
INSERT INTO `ry_rental`.`ry_rental_member` (member_id, tenant_id, member_code, member_type, auth_status, legal_subject_name, nickname, contact_name, contact_phone, vip_level, storage_quota_mb, external_accept_enabled, status, create_by, create_time, remark)
SELECT 1, 10001, 'MERCHANT_DEMO', 'enterprise', 'verified', '迁渃演示商户', '演示商户', '商家管理员', '15500000000', 'standard', 10240, 1, '0', 'system', NOW(), '本地开发商家端演示商户'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_member` WHERE member_id = 1);

INSERT INTO `ry_rental`.`ry_rental_merchant_user` (tenant_id, member_id, user_name, nick_name, phone, email, password, status, del_flag, login_ip, login_date, pwd_update_date, must_change_password, create_by, create_time, remark)
SELECT 10001, 1, '15500000000', '商家管理员', '15500000000', 'merchant@example.com', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', NOW(), NOW(), 0, 'system', NOW(), '本地开发商家端账号'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_user` WHERE phone = '15500000000');

INSERT INTO `ry_rental`.`ry_rental_user_member` (tenant_id, member_id, merchant_user_id, member_role, warehouse_scope_type, sensitive_scope, status, create_by, create_time, remark)
SELECT u.tenant_id, u.member_id, u.merchant_user_id, 'owner', 'all', 'detail', '0', 'system', NOW(), '本地开发商家端账号绑定'
FROM `ry_rental`.`ry_rental_merchant_user` u
WHERE u.phone = '15500000000' AND u.del_flag = '0'
  AND NOT EXISTS (
    SELECT 1 FROM `ry_rental`.`ry_rental_user_member` um
    WHERE um.member_id = u.member_id AND um.merchant_user_id = u.merchant_user_id
  );

INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2100, 0, 0, '商家工作台', 0, 1, 'merchant-dashboard', 'merchant/dashboard/index', '', '', 1, 0, 'C', '0', '0', 'merchant:dashboard:view', 'dashboard', 'system', NOW(), '商家端首页'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2100);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2110, 0, 0, '订单中心', 0, 2, 'merchant-order', 'merchant/order/index', '', '', 1, 0, 'C', '0', '0', 'merchant:order:list', 'list', 'system', NOW(), '商家订单中心'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2110);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2120, 0, 0, '仓库库存', 0, 3, 'merchant-inventory', 'merchant/inventory/index', '', '', 1, 0, 'C', '0', '0', 'merchant:inventory:list', 'tree', 'system', NOW(), '商家仓库与库存'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2120);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2130, 0, 0, '商品资料', 0, 4, 'merchant-product', 'merchant/product/index', '', '', 1, 0, 'C', '0', '0', 'merchant:product:list', 'shopping', 'system', NOW(), '商家商品、SN、配件与套餐'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2130);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2140, 0, 0, '物流归还', 0, 5, 'merchant-logistics', 'merchant/logistics/index', '', '', 1, 0, 'C', '0', '0', 'merchant:logistics:list', 'international', 'system', NOW(), '商家物流与客户归还'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2140);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2150, 0, 0, '联盟协作', 0, 6, 'merchant-alliance', 'merchant/alliance/index', '', '', 1, 0, 'C', '0', '0', 'merchant:alliance:list', 'peoples', 'system', NOW(), '商家多仓联盟协作'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2150);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2160, 0, 0, '周转市场', 0, 7, 'merchant-market', 'merchant/market/index', '', '', 1, 0, 'C', '0', '0', 'merchant:market:list', 'table', 'system', NOW(), '商家库存周转市场'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2160);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2170, 0, 0, '商家设置', 0, 8, 'merchant-profile', 'merchant/profile/index', '', '', 1, 0, 'C', '0', '0', 'merchant:profile:view', 'user', 'system', NOW(), '商家资料与员工设置'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2170);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2180, 0, 0, '角色权限', 0, 9, 'merchant-role', 'merchant/access/role/index', '', '', 1, 0, 'C', '0', '0', 'merchant:role:list', 'peoples', 'system', NOW(), '商户角色与菜单权限'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2180);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2181, 0, 0, '角色查询', 2180, 1, 'role-query', '', '', '', 1, 0, 'F', '0', '0', 'merchant:role:query', '#', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2181);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2182, 0, 0, '角色新增', 2180, 2, 'role-add', '', '', '', 1, 0, 'F', '0', '0', 'merchant:role:add', '#', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2182);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2183, 0, 0, '角色修改', 2180, 3, 'role-edit', '', '', '', 1, 0, 'F', '0', '0', 'merchant:role:edit', '#', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2183);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2184, 0, 0, '角色删除', 2180, 4, 'role-remove', '', '', '', 1, 0, 'F', '0', '0', 'merchant:role:remove', '#', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2184);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2190, 0, 0, '操作日志', 0, 10, 'merchant-operlog', 'merchant/monitor/operlog/index', '', '', 1, 0, 'C', '0', '0', 'merchant:operlog:list', 'log', 'system', NOW(), '当前商户操作日志'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2190);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2191, 0, 0, '日志导出', 2190, 1, 'operlog-export', '', '', '', 1, 0, 'F', '0', '0', 'merchant:operlog:export', '#', 'system', NOW(), '导出当前商户操作日志'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id = 2191);

INSERT INTO `ry_rental`.`ry_rental_merchant_role` (tenant_id, member_id, role_name, role_key, role_sort, menu_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT 10001, 1, '商户管理员', 'merchant_admin', 1, 1, '0', '0', 'system', NOW(), '商户默认管理员角色'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_role` WHERE tenant_id = 10001 AND member_id = 1 AND role_key = 'merchant_admin' AND del_flag = '0');

INSERT IGNORE INTO `ry_rental`.`ry_rental_merchant_role_menu` (tenant_id, member_id, role_id, menu_id)
SELECT r.tenant_id, r.member_id, r.role_id, m.menu_id
FROM `ry_rental`.`ry_rental_merchant_role` r
INNER JOIN `ry_rental`.`ry_rental_merchant_menu` m ON m.tenant_id = 0 AND m.member_id = 0
WHERE r.tenant_id = 10001 AND r.member_id = 1 AND r.role_key = 'merchant_admin'
  AND r.del_flag = '0' AND m.del_flag = '0';

INSERT IGNORE INTO `ry_rental`.`ry_rental_merchant_user_role` (tenant_id, member_id, merchant_user_id, role_id)
SELECT u.tenant_id, u.member_id, u.merchant_user_id, r.role_id
FROM `ry_rental`.`ry_rental_merchant_user` u
INNER JOIN `ry_rental`.`ry_rental_merchant_role` r
  ON r.tenant_id = u.tenant_id AND r.member_id = u.member_id
WHERE u.phone = '15500000000' AND u.del_flag = '0'
  AND r.role_key = 'merchant_admin' AND r.del_flag = '0';

INSERT IGNORE INTO `ry_rental`.`ry_rental_merchant_user_menu` (tenant_id, member_id, merchant_user_id, menu_id)
SELECT u.tenant_id, u.member_id, u.merchant_user_id, m.menu_id
FROM `ry_rental`.`ry_rental_merchant_user` u
INNER JOIN `ry_rental`.`ry_rental_merchant_menu` m ON m.tenant_id = 0 AND m.member_id = 0
WHERE u.phone = '15500000000' AND u.del_flag = '0' AND m.del_flag = '0';

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
  AND m.del_flag = '0'
  AND m.parent_id <> 0;
