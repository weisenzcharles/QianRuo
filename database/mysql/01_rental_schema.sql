-- 租赁系统业务表结构 V1
-- 约定：
-- 1. tenant_id/member_id 用于租户/商户隔离，后端查询必须强制带租户条件。
-- 2. 金额字段统一 DECIMAL(18,2)，数量字段统一 INT 或 DECIMAL(18,4)。
-- 3. status 字段使用业务字典值，避免数据库 enum 后期扩展困难。
-- 4. RuoYi 通用审计字段：create_by/create_time/update_by/update_time/remark/del_flag。
-- 5. 敏感信息字段如 customer_phone/customer_address 支持后续应用层加密或脱敏展示。

USE `ry_rental`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 商户、主体、员工扩展
-- ----------------------------

CREATE TABLE IF NOT EXISTS `ry_rental_member` (
  `member_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商户/会员ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_code` VARCHAR(64) NOT NULL COMMENT '商户编码',
  `member_type` VARCHAR(20) NOT NULL DEFAULT 'personal' COMMENT '商户类型：personal个人 enterprise企业',
  `auth_status` VARCHAR(20) NOT NULL DEFAULT 'unverified' COMMENT '认证状态',
  `legal_subject_name` VARCHAR(128) DEFAULT NULL COMMENT '主体名称',
  `license_no` VARCHAR(64) DEFAULT NULL COMMENT '营业执照/主体编号',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '商户昵称',
  `contact_name` VARCHAR(64) DEFAULT NULL COMMENT '联系人',
  `contact_phone` VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
  `service_phone` VARCHAR(32) DEFAULT NULL COMMENT '客服电话',
  `contact_email` VARCHAR(128) DEFAULT NULL COMMENT '联系邮箱',
  `alipay_real_name` VARCHAR(64) DEFAULT NULL COMMENT '支付宝实名',
  `alipay_account` VARCHAR(128) DEFAULT NULL COMMENT '支付宝账号',
  `store_logo_url` VARCHAR(500) DEFAULT NULL COMMENT '店铺logo地址',
  `store_description` VARCHAR(1000) DEFAULT NULL COMMENT '店铺描述',
  `registered_capital` DECIMAL(18,2) DEFAULT NULL COMMENT '企业注册资本',
  `business_scope` VARCHAR(2000) DEFAULT NULL COMMENT '营业执照经营范围',
  `license_address` VARCHAR(500) DEFAULT NULL COMMENT '营业执照地址',
  `license_valid_from` DATE DEFAULT NULL COMMENT '营业执照有效期开始',
  `license_valid_to` DATE DEFAULT NULL COMMENT '营业执照有效期结束',
  `license_long_term` TINYINT NOT NULL DEFAULT 0 COMMENT '营业执照是否长期：0否 1是',
  `license_image_url` VARCHAR(500) DEFAULT NULL COMMENT '营业执照图片地址',
  `legal_representative_name` VARCHAR(64) DEFAULT NULL COMMENT '法人姓名',
  `legal_representative_id_no` VARCHAR(64) DEFAULT NULL COMMENT '法人身份证号',
  `legal_id_front_url` VARCHAR(500) DEFAULT NULL COMMENT '法人身份证人像面',
  `legal_id_back_url` VARCHAR(500) DEFAULT NULL COMMENT '法人身份证国徽面',
  `signing_date` DATE DEFAULT NULL COMMENT '签约日期',
  `contract_valid_from` DATE DEFAULT NULL COMMENT '合同有效期开始',
  `contract_valid_to` DATE DEFAULT NULL COMMENT '合同有效期结束',
  `contract_file_url` VARCHAR(500) DEFAULT NULL COMMENT '签约合同文件地址',
  `audit_time` DATETIME DEFAULT NULL COMMENT '最后审核时间',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
  `freeze_time` DATETIME DEFAULT NULL COMMENT '冻结时间',
  `freeze_reason` VARCHAR(500) DEFAULT NULL COMMENT '冻结原因',
  `wechat_qr_file_id` BIGINT DEFAULT NULL COMMENT '微信二维码文件ID',
  `vip_level` VARCHAR(32) NOT NULL DEFAULT 'free' COMMENT '会员等级',
  `storage_quota_mb` BIGINT NOT NULL DEFAULT 1024 COMMENT '存储额度MB',
  `external_accept_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否允许库存对外接单',
  `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0正常 1停用',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `del_flag` CHAR(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0存在 2删除',
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `uk_rental_member_code` (`member_code`),
  UNIQUE KEY `uk_rental_member_contact_phone` (`contact_phone`),
  KEY `idx_rental_member_tenant` (`tenant_id`),
  KEY `idx_rental_member_auth_status` (`auth_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁商户/会员主体';

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_user` (
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

CREATE TABLE IF NOT EXISTS `ry_rental_user_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `merchant_user_id` BIGINT NOT NULL COMMENT '商户用户ID',
  `member_role` VARCHAR(32) NOT NULL DEFAULT 'staff' COMMENT '商户内角色',
  `warehouse_scope_type` VARCHAR(20) NOT NULL DEFAULT 'all' COMMENT '仓库范围：all全部 specified指定',
  `sensitive_scope` VARCHAR(32) NOT NULL DEFAULT 'masked' COMMENT '敏感字段权限：masked脱敏 detail明文 export导出',
  `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `del_flag` CHAR(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rental_user_member` (`member_id`, `merchant_user_id`),
  KEY `idx_rental_user_member_user` (`merchant_user_id`),
  KEY `idx_rental_user_member_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户员工与商户用户绑定';

CREATE TABLE IF NOT EXISTS `ry_rental_user_warehouse_scope` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `merchant_user_id` BIGINT NOT NULL COMMENT '商户用户ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rental_user_warehouse` (`member_id`, `merchant_user_id`, `warehouse_id`),
  KEY `idx_rental_user_warehouse_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户仓库数据权限';

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_menu` (
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

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_role` (
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

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_role_menu` (
  `role_id` BIGINT NOT NULL COMMENT '商户角色ID',
  `menu_id` BIGINT NOT NULL COMMENT '商户菜单ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  PRIMARY KEY (`role_id`, `menu_id`),
  KEY `idx_rental_merchant_role_menu_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_merchant_role_menu_menu` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户角色和菜单关联';

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_user_role` (
  `merchant_user_id` BIGINT NOT NULL COMMENT '商户用户ID',
  `role_id` BIGINT NOT NULL COMMENT '商户角色ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  PRIMARY KEY (`merchant_user_id`, `role_id`),
  KEY `idx_rental_merchant_user_role_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_merchant_user_role_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户用户和角色关联';

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_user_menu` (
  `merchant_user_id` BIGINT NOT NULL COMMENT '商户用户ID',
  `menu_id` BIGINT NOT NULL COMMENT '商户菜单ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  PRIMARY KEY (`merchant_user_id`, `menu_id`),
  KEY `idx_rental_merchant_user_menu_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_merchant_user_menu_menu` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户用户和菜单关联';

-- ----------------------------
-- 2. 仓库与基础资料
-- ----------------------------

CREATE TABLE IF NOT EXISTS `ry_rental_warehouse` (
  `warehouse_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '仓库ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `warehouse_code` VARCHAR(64) NOT NULL COMMENT '仓库编码',
  `warehouse_name` VARCHAR(128) NOT NULL COMMENT '仓库名称',
  `contact_name` VARCHAR(64) DEFAULT NULL COMMENT '负责人',
  `contact_phone` VARCHAR(32) DEFAULT NULL COMMENT '负责人电话',
  `province` VARCHAR(64) DEFAULT NULL COMMENT '省',
  `city` VARCHAR(64) DEFAULT NULL COMMENT '市',
  `district` VARCHAR(64) DEFAULT NULL COMMENT '区',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
  `longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
  `latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
  `pickup_rule_json` JSON COMMENT '上门取件规则',
  `return_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可作为归还仓',
  `alliance_share_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否允许联盟共享',
  `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `del_flag` CHAR(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`warehouse_id`),
  UNIQUE KEY `uk_rental_warehouse_code` (`member_id`, `warehouse_code`),
  KEY `idx_rental_warehouse_tenant_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_warehouse_city` (`city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库';

CREATE TABLE IF NOT EXISTS `ry_rental_brand_model` (
  `model_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标准型号ID',
  `category_level1` VARCHAR(64) NOT NULL COMMENT '一级分类',
  `category_level2` VARCHAR(64) DEFAULT NULL COMMENT '二级分类',
  `category_level3` VARCHAR(64) DEFAULT NULL COMMENT '三级分类',
  `brand` VARCHAR(64) NOT NULL COMMENT '品牌',
  `model_name` VARCHAR(128) NOT NULL COMMENT '型号',
  `memory_spec` VARCHAR(64) DEFAULT NULL COMMENT '内存/规格',
  `official_flag` TINYINT NOT NULL DEFAULT 1 COMMENT '是否官方标准',
  `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `del_flag` CHAR(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`model_id`),
  KEY `idx_rental_brand_model_brand` (`brand`, `model_name`),
  KEY `idx_rental_brand_model_category` (`category_level1`, `category_level2`, `category_level3`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统标准品牌型号';

CREATE TABLE IF NOT EXISTS `ry_rental_member_common_model` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `model_id` BIGINT NOT NULL COMMENT '标准型号ID',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rental_member_model` (`member_id`, `model_id`),
  KEY `idx_rental_member_common_model_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户常用型号';

-- ----------------------------
-- 3. 商品、SN、配件、套餐
-- ----------------------------

CREATE TABLE IF NOT EXISTS `ry_rental_product` (
  `product_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `model_id` BIGINT DEFAULT NULL COMMENT '标准型号ID',
  `product_code` VARCHAR(64) NOT NULL COMMENT '商品编码',
  `product_name` VARCHAR(128) NOT NULL COMMENT '商品名称',
  `category_level1` VARCHAR(64) NOT NULL COMMENT '一级分类',
  `category_level2` VARCHAR(64) DEFAULT NULL COMMENT '二级分类',
  `category_level3` VARCHAR(64) DEFAULT NULL COMMENT '三级分类',
  `brand` VARCHAR(64) DEFAULT NULL COMMENT '品牌',
  `model_name` VARCHAR(128) DEFAULT NULL COMMENT '型号',
  `memory_spec` VARCHAR(64) DEFAULT NULL COMMENT '内存/规格',
  `product_type` VARCHAR(20) NOT NULL DEFAULT 'device' COMMENT '类型：device设备 accessory配件',
  `stock_mode` VARCHAR(20) NOT NULL DEFAULT 'sn' COMMENT '库存模式：sn序列号 qty总数',
  `default_image_file_id` BIGINT DEFAULT NULL COMMENT '默认图片文件ID',
  `purchase_cost` DECIMAL(18,2) DEFAULT NULL COMMENT '默认采购成本',
  `sale_price` DECIMAL(18,2) DEFAULT NULL COMMENT '默认出售价格',
  `frontend_visible` TINYINT NOT NULL DEFAULT 1 COMMENT '前台是否展示',
  `stats_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否参与统计',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `del_flag` CHAR(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`product_id`),
  UNIQUE KEY `uk_rental_product_code` (`member_id`, `product_code`),
  KEY `idx_rental_product_tenant_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_product_model` (`model_id`),
  KEY `idx_rental_product_type` (`product_type`, `stock_mode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品';

CREATE TABLE IF NOT EXISTS `ry_rental_asset_sn` (
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
  `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `del_flag` CHAR(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`sn_id`),
  UNIQUE KEY `uk_rental_asset_sn` (`member_id`, `sn_code`),
  KEY `idx_rental_asset_tenant_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_asset_product` (`product_id`),
  KEY `idx_rental_asset_warehouse_status` (`warehouse_id`, `asset_status`),
  KEY `idx_rental_asset_insurance` (`insurance_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备SN';

CREATE TABLE IF NOT EXISTS `ry_rental_asset_photo` (
  `photo_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '照片ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `sn_id` BIGINT NOT NULL COMMENT 'SN设备ID',
  `file_id` BIGINT NOT NULL COMMENT '文件ID',
  `photo_type` VARCHAR(32) NOT NULL DEFAULT 'device' COMMENT '照片类型',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`photo_id`),
  KEY `idx_rental_asset_photo_sn` (`sn_id`, `create_time`),
  KEY `idx_rental_asset_photo_tenant` (`tenant_id`, `member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备照片';

CREATE TABLE IF NOT EXISTS `ry_rental_accessory_item` (
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
  `status` CHAR(1) NOT NULL DEFAULT '0',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `del_flag` CHAR(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`accessory_item_id`),
  UNIQUE KEY `uk_rental_accessory_item` (`member_id`, `item_code`),
  KEY `idx_rental_accessory_product` (`product_id`),
  KEY `idx_rental_accessory_warehouse_status` (`warehouse_id`, `item_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配件实例';

CREATE TABLE IF NOT EXISTS `ry_rental_package_template` (
  `package_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '套餐ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `platform_code` VARCHAR(64) DEFAULT NULL COMMENT '适用平台/渠道',
  `package_code` VARCHAR(64) NOT NULL COMMENT '套餐编码',
  `package_name` VARCHAR(128) NOT NULL COMMENT '套餐名称',
  `main_product_id` BIGINT DEFAULT NULL COMMENT '主商品ID',
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `del_flag` CHAR(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`package_id`),
  UNIQUE KEY `uk_rental_package_code` (`member_id`, `package_code`),
  KEY `idx_rental_package_tenant_member` (`tenant_id`, `member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐模板';

CREATE TABLE IF NOT EXISTS `ry_rental_package_item` (
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
  PRIMARY KEY (`id`),
  KEY `idx_rental_package_item_package` (`package_id`),
  KEY `idx_rental_package_item_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐物料明细';

-- ----------------------------
-- 4. 客户、订单、订单明细
-- ----------------------------

CREATE TABLE IF NOT EXISTS `ry_rental_customer` (
  `customer_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `customer_name` VARCHAR(64) DEFAULT NULL COMMENT '客户姓名',
  `customer_nickname` VARCHAR(64) DEFAULT NULL COMMENT '客户昵称',
  `phone` VARCHAR(32) DEFAULT NULL COMMENT '收货手机号',
  `alipay_phone` VARCHAR(32) DEFAULT NULL COMMENT '支付宝手机号',
  `zhima_score` INT DEFAULT NULL COMMENT '芝麻分',
  `province` VARCHAR(64) DEFAULT NULL COMMENT '省',
  `city` VARCHAR(64) DEFAULT NULL COMMENT '市',
  `district` VARCHAR(64) DEFAULT NULL COMMENT '区',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
  `wechat_openid` VARCHAR(128) DEFAULT NULL COMMENT '微信openid',
  `last_verified_time` DATETIME DEFAULT NULL COMMENT '最近验证时间',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `del_flag` CHAR(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`customer_id`),
  KEY `idx_rental_customer_member_phone` (`member_id`, `phone`),
  KEY `idx_rental_customer_tenant` (`tenant_id`, `member_id`),
  KEY `idx_rental_customer_openid` (`wechat_openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁客户';

CREATE TABLE IF NOT EXISTS `ry_rental_order` (
  `order_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '系统订单号',
  `external_order_no` VARCHAR(128) DEFAULT NULL COMMENT '外部订单号',
  `channel_code` VARCHAR(64) DEFAULT NULL COMMENT '订单渠道',
  `shop_name` VARCHAR(128) DEFAULT NULL COMMENT '来源店铺',
  `customer_id` BIGINT DEFAULT NULL COMMENT '客户ID',
  `customer_name` VARCHAR(64) DEFAULT NULL COMMENT '客户姓名快照',
  `customer_phone` VARCHAR(32) DEFAULT NULL COMMENT '客户手机号快照',
  `customer_address` VARCHAR(255) DEFAULT NULL COMMENT '客户地址快照',
  `warehouse_id` BIGINT DEFAULT NULL COMMENT '发货仓库ID',
  `return_warehouse_id` BIGINT DEFAULT NULL COMMENT '归还仓库ID',
  `lease_start_date` DATE NOT NULL COMMENT '租期开始',
  `lease_end_date` DATE NOT NULL COMMENT '租期结束',
  `ship_due_time` DATETIME DEFAULT NULL COMMENT '应发货时间',
  `return_due_time` DATETIME DEFAULT NULL COMMENT '应归还时间',
  `rent_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '租金',
  `deposit_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '押金',
  `order_status` VARCHAR(32) NOT NULL DEFAULT 'pending_ship' COMMENT '订单状态',
  `pay_status` VARCHAR(32) NOT NULL DEFAULT 'unpaid' COMMENT '支付状态',
  `time_effect_status` VARCHAR(32) DEFAULT NULL COMMENT '时效状态',
  `weather_risk_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '天气风险标识',
  `priority_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '置顶/优先标识',
  `source_text_snapshot` TEXT COMMENT '复制解析原文快照',
  `share_snapshot_json` JSON COMMENT '转发快照',
  `source_order_id` BIGINT DEFAULT NULL COMMENT '转寄来源订单ID',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `del_flag` CHAR(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_rental_order_no` (`member_id`, `order_no`),
  KEY `idx_rental_order_tenant_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_order_status` (`member_id`, `order_status`),
  KEY `idx_rental_order_ship_due` (`member_id`, `warehouse_id`, `ship_due_time`),
  KEY `idx_rental_order_return_due` (`member_id`, `return_warehouse_id`, `return_due_time`),
  KEY `idx_rental_order_customer` (`customer_id`),
  KEY `idx_rental_order_external` (`channel_code`, `external_order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁订单';

CREATE TABLE IF NOT EXISTS `ry_rental_order_item` (
  `order_item_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单明细ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `product_id` BIGINT DEFAULT NULL COMMENT '商品ID',
  `package_id` BIGINT DEFAULT NULL COMMENT '套餐ID',
  `sn_id` BIGINT DEFAULT NULL COMMENT '绑定SN ID',
  `accessory_item_ids` JSON COMMENT '绑定配件ID列表',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `item_status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '明细状态',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `del_flag` CHAR(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`order_item_id`),
  KEY `idx_rental_order_item_order` (`order_id`),
  KEY `idx_rental_order_item_sn` (`sn_id`),
  KEY `idx_rental_order_item_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品明细';

CREATE TABLE IF NOT EXISTS `ry_rental_order_edit_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编辑日志ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `field_name` VARCHAR(128) NOT NULL COMMENT '字段名',
  `before_value` TEXT COMMENT '修改前',
  `after_value` TEXT COMMENT '修改后',
  `change_reason` VARCHAR(500) DEFAULT NULL COMMENT '修改原因',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人',
  `operator_ip` VARCHAR(64) DEFAULT NULL COMMENT '操作IP',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_rental_order_edit_order` (`order_id`, `create_time`),
  KEY `idx_rental_order_edit_tenant` (`tenant_id`, `member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单编辑日志';

CREATE TABLE IF NOT EXISTS `ry_rental_order_note` (
  `note_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '批注ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `note_content` VARCHAR(1000) NOT NULL COMMENT '批注内容',
  `pinned_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶',
  `read_user_ids` JSON COMMENT '已阅用户ID',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` CHAR(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`note_id`),
  KEY `idx_rental_order_note_order` (`order_id`, `pinned_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单批注';

-- ----------------------------
-- 5. 库存、预占、调货、盘点
-- ----------------------------

CREATE TABLE IF NOT EXISTS `ry_rental_inventory_reservation` (
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
  PRIMARY KEY (`reservation_id`),
  KEY `idx_rental_reservation_order` (`order_id`),
  KEY `idx_rental_reservation_stock` (`member_id`, `warehouse_id`, `product_id`, `reservation_status`),
  KEY `idx_rental_reservation_expire` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预占';

CREATE TABLE IF NOT EXISTS `ry_rental_inventory_ledger` (
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
  PRIMARY KEY (`ledger_id`),
  KEY `idx_rental_inventory_ledger_tenant` (`tenant_id`, `member_id`),
  KEY `idx_rental_inventory_ledger_sn` (`sn_id`, `create_time`),
  KEY `idx_rental_inventory_ledger_ref` (`ref_type`, `ref_id`),
  KEY `idx_rental_inventory_ledger_wh_product` (`warehouse_id`, `product_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存状态流水';

CREATE TABLE IF NOT EXISTS `ry_rental_inventory_snapshot` (
  `snapshot_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存快照ID',
  `snapshot_date` DATE NOT NULL COMMENT '快照日期',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `warehouse_id` BIGINT DEFAULT NULL COMMENT '仓库ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `package_id` BIGINT DEFAULT NULL COMMENT '套餐ID',
  `total_qty` INT NOT NULL DEFAULT 0 COMMENT '总库存',
  `free_qty` INT NOT NULL DEFAULT 0 COMMENT '在库空闲',
  `reserved_qty` INT NOT NULL DEFAULT 0 COMMENT '预占',
  `rented_qty` INT NOT NULL DEFAULT 0 COMMENT '租用中',
  `ship_in_transit_qty` INT NOT NULL DEFAULT 0 COMMENT '发货在途',
  `return_in_transit_qty` INT NOT NULL DEFAULT 0 COMMENT '归还在途',
  `expected_in_qty` INT NOT NULL DEFAULT 0 COMMENT '预计到库',
  `available_order_qty` INT NOT NULL DEFAULT 0 COMMENT '可接单数量',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`snapshot_id`),
  UNIQUE KEY `uk_rental_inventory_snapshot` (`snapshot_date`, `member_id`, `warehouse_id`, `product_id`, `package_id`),
  KEY `idx_rental_inventory_snapshot_tenant` (`tenant_id`, `member_id`, `snapshot_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日库存快照';

CREATE TABLE IF NOT EXISTS `ry_rental_transfer_order` (
  `transfer_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '调货ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `transfer_no` VARCHAR(64) NOT NULL COMMENT '调货单号',
  `from_warehouse_id` BIGINT NOT NULL COMMENT '发货仓',
  `to_warehouse_id` BIGINT NOT NULL COMMENT '到达仓',
  `ref_order_id` BIGINT DEFAULT NULL COMMENT '关联订单',
  `carrier` VARCHAR(64) DEFAULT NULL COMMENT '承运商',
  `tracking_no` VARCHAR(128) DEFAULT NULL COMMENT '快递单号',
  `sn_ids` JSON COMMENT 'SN列表',
  `transfer_status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '状态',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `del_flag` CHAR(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`transfer_id`),
  UNIQUE KEY `uk_rental_transfer_no` (`member_id`, `transfer_no`),
  KEY `idx_rental_transfer_wh` (`from_warehouse_id`, `to_warehouse_id`),
  KEY `idx_rental_transfer_status` (`member_id`, `transfer_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库调货单';

CREATE TABLE IF NOT EXISTS `ry_rental_stocktaking` (
  `stocktaking_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '盘点ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `stocktaking_no` VARCHAR(64) NOT NULL COMMENT '盘点单号',
  `stocktaking_date` DATE NOT NULL COMMENT '盘点日期',
  `stocktaking_status` VARCHAR(32) NOT NULL DEFAULT 'processing' COMMENT '盘点状态',
  `abnormal_count` INT NOT NULL DEFAULT 0 COMMENT '异常数量',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (`stocktaking_id`),
  UNIQUE KEY `uk_rental_stocktaking_no` (`member_id`, `stocktaking_no`),
  KEY `idx_rental_stocktaking_wh_date` (`warehouse_id`, `stocktaking_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点单';

CREATE TABLE IF NOT EXISTS `ry_rental_stocktaking_item` (
  `item_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '盘点明细ID',
  `stocktaking_id` BIGINT NOT NULL COMMENT '盘点ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `sn_id` BIGINT DEFAULT NULL COMMENT 'SN ID',
  `scan_code` VARCHAR(128) NOT NULL COMMENT '扫码内容',
  `system_status` VARCHAR(32) DEFAULT NULL COMMENT '系统状态',
  `scan_result` VARCHAR(32) NOT NULL DEFAULT 'normal' COMMENT '扫描结果',
  `matched_ref_type` VARCHAR(64) DEFAULT NULL COMMENT '匹配对象类型',
  `matched_ref_id` BIGINT DEFAULT NULL COMMENT '匹配对象ID',
  `remark` VARCHAR(500) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`item_id`),
  KEY `idx_rental_stocktaking_item_main` (`stocktaking_id`),
  KEY `idx_rental_stocktaking_item_sn` (`sn_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点明细';

-- ----------------------------
-- 6. 物流、媒体、售后
-- ----------------------------

CREATE TABLE IF NOT EXISTS `ry_rental_logistics_order` (
  `logistics_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '物流单ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `order_id` BIGINT DEFAULT NULL COMMENT '订单ID',
  `warehouse_id` BIGINT DEFAULT NULL COMMENT '仓库ID',
  `direction` VARCHAR(20) NOT NULL COMMENT '方向：ship发货 return归还 transfer调货',
  `carrier` VARCHAR(64) NOT NULL COMMENT '承运商',
  `tracking_no` VARCHAR(128) DEFAULT NULL COMMENT '快递单号',
  `source_type` VARCHAR(32) NOT NULL DEFAULT 'manual' COMMENT '来源：system系统下单 manual手填 external外部',
  `pickup_time` DATETIME DEFAULT NULL COMMENT '预约取件时间',
  `estimated_arrive_time` DATETIME DEFAULT NULL COMMENT '预计到达',
  `route_status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '路由状态',
  `abnormal_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '异常标记',
  `fee_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '费用',
  `discount_rate` DECIMAL(8,4) DEFAULT NULL COMMENT '折扣',
  `raw_response` JSON COMMENT '接口原始返回',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `del_flag` CHAR(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`logistics_id`),
  UNIQUE KEY `uk_rental_tracking_no` (`carrier`, `tracking_no`),
  KEY `idx_rental_logistics_order` (`order_id`, `direction`),
  KEY `idx_rental_logistics_member_status` (`member_id`, `route_status`),
  KEY `idx_rental_logistics_pickup` (`pickup_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流单';

CREATE TABLE IF NOT EXISTS `ry_rental_logistics_route` (
  `route_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '路由ID',
  `logistics_id` BIGINT NOT NULL COMMENT '物流单ID',
  `tracking_no` VARCHAR(128) DEFAULT NULL COMMENT '快递单号',
  `route_status` VARCHAR(32) DEFAULT NULL COMMENT '节点状态',
  `route_desc` VARCHAR(1000) DEFAULT NULL COMMENT '节点描述',
  `route_time` DATETIME DEFAULT NULL COMMENT '节点时间',
  `province` VARCHAR(64) DEFAULT NULL COMMENT '省',
  `city` VARCHAR(64) DEFAULT NULL COMMENT '市',
  `raw_json` JSON COMMENT '原始节点',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`route_id`),
  KEY `idx_rental_logistics_route_main` (`logistics_id`, `route_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流路由节点';

CREATE TABLE IF NOT EXISTS `ry_rental_media_asset` (
  `media_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '媒体ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `order_id` BIGINT DEFAULT NULL COMMENT '订单ID',
  `sn_id` BIGINT DEFAULT NULL COMMENT 'SN ID',
  `file_id` BIGINT DEFAULT NULL COMMENT '文件服务ID',
  `media_type` VARCHAR(32) NOT NULL COMMENT '类型：ship_video receive_video unbox_video check_photo product_photo',
  `file_key` VARCHAR(255) DEFAULT NULL COMMENT '文件Key',
  `file_name` VARCHAR(255) DEFAULT NULL COMMENT '文件名',
  `file_size` BIGINT DEFAULT NULL COMMENT '文件大小',
  `mime_type` VARCHAR(128) DEFAULT NULL COMMENT 'MIME类型',
  `detected_sn` VARCHAR(128) DEFAULT NULL COMMENT '识别SN',
  `match_status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '匹配状态',
  `uploaded_by_type` VARCHAR(32) NOT NULL DEFAULT 'staff' COMMENT '上传方：staff customer system',
  `retention_until` DATETIME DEFAULT NULL COMMENT '保留到期时间',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `del_flag` CHAR(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`media_id`),
  KEY `idx_rental_media_order` (`order_id`, `media_type`),
  KEY `idx_rental_media_sn` (`sn_id`),
  KEY `idx_rental_media_retention` (`retention_until`),
  KEY `idx_rental_media_tenant` (`tenant_id`, `member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='媒体文件业务索引';

CREATE TABLE IF NOT EXISTS `ry_rental_after_sales_case` (
  `case_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '售后ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `case_no` VARCHAR(64) NOT NULL COMMENT '售后单号',
  `case_type` VARCHAR(32) NOT NULL COMMENT '类型：damage损坏 lost丢失 compensation赔付 other其他',
  `case_status` VARCHAR(32) NOT NULL DEFAULT 'processing' COMMENT '状态',
  `problem_desc` VARCHAR(1000) DEFAULT NULL COMMENT '问题描述',
  `lost_items_json` JSON COMMENT '丢失配件',
  `compensation_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '赔付金额',
  `customer_contacted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否联系客户',
  `progress_json` JSON COMMENT '进度记录',
  `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `del_flag` CHAR(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`case_id`),
  UNIQUE KEY `uk_rental_after_sales_no` (`member_id`, `case_no`),
  KEY `idx_rental_after_sales_order` (`order_id`),
  KEY `idx_rental_after_sales_status` (`member_id`, `case_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后单';

-- ----------------------------
-- 7. 财务、套餐、推广
-- ----------------------------

CREATE TABLE IF NOT EXISTS `ry_rental_finance_ledger` (
  `ledger_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '财务流水ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT DEFAULT NULL COMMENT '商户ID',
  `ledger_no` VARCHAR(64) NOT NULL COMMENT '流水号',
  `direction` VARCHAR(16) NOT NULL COMMENT '方向：income收入 expense支出 freeze冻结 unfreeze解冻 reverse冲正',
  `biz_type` VARCHAR(64) NOT NULL COMMENT '业务类型',
  `amount` DECIMAL(18,2) NOT NULL COMMENT '金额',
  `balance_after` DECIMAL(18,2) DEFAULT NULL COMMENT '变更后余额',
  `ref_type` VARCHAR(64) DEFAULT NULL COMMENT '关联类型',
  `ref_id` BIGINT DEFAULT NULL COMMENT '关联ID',
  `pay_channel` VARCHAR(64) DEFAULT NULL COMMENT '支付渠道',
  `pay_order_no` VARCHAR(128) DEFAULT NULL COMMENT '支付单号',
  `invoice_status` VARCHAR(32) DEFAULT NULL COMMENT '发票状态',
  `ledger_status` VARCHAR(32) NOT NULL DEFAULT 'success' COMMENT '流水状态',
  `original_ledger_id` BIGINT DEFAULT NULL COMMENT '原流水ID',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (`ledger_id`),
  UNIQUE KEY `uk_rental_finance_ledger_no` (`ledger_no`),
  KEY `idx_rental_finance_member_time` (`member_id`, `create_time`),
  KEY `idx_rental_finance_ref` (`ref_type`, `ref_id`),
  KEY `idx_rental_finance_invoice` (`invoice_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务不可变流水';

CREATE TABLE IF NOT EXISTS `ry_rental_service_subscription` (
  `subscription_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订阅ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `service_code` VARCHAR(64) NOT NULL COMMENT '服务编码',
  `service_name` VARCHAR(128) NOT NULL COMMENT '服务名称',
  `plan_code` VARCHAR(64) DEFAULT NULL COMMENT '套餐编码',
  `billing_cycle` VARCHAR(32) DEFAULT NULL COMMENT '计费周期',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `quota_json` JSON COMMENT '权益额度',
  `subscription_status` VARCHAR(32) NOT NULL DEFAULT 'active' COMMENT '状态',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (`subscription_id`),
  KEY `idx_rental_subscription_member` (`member_id`, `service_code`, `subscription_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='增值服务订阅';

CREATE TABLE IF NOT EXISTS `ry_rental_promotion_invite` (
  `invite_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '邀请ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `promoter_member_id` BIGINT NOT NULL COMMENT '推广员商户ID',
  `invitee_member_id` BIGINT DEFAULT NULL COMMENT '被邀请商户ID',
  `invite_code` VARCHAR(64) NOT NULL COMMENT '推广码',
  `active_hours` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '有效在线小时',
  `normal_order_count` INT NOT NULL DEFAULT 0 COMMENT '正常订单数',
  `effective_status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '有效状态',
  `reward_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '奖励金额',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `effective_time` DATETIME DEFAULT NULL COMMENT '成为有效用户时间',
  PRIMARY KEY (`invite_id`),
  UNIQUE KEY `uk_rental_promotion_invitee` (`promoter_member_id`, `invitee_member_id`),
  KEY `idx_rental_promotion_code` (`invite_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推广邀请';

-- ----------------------------
-- 8. 多仓联盟
-- ----------------------------

CREATE TABLE IF NOT EXISTS `ry_rental_alliance` (
  `alliance_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '联盟ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `alliance_name` VARCHAR(128) NOT NULL COMMENT '联盟名称',
  `owner_member_id` BIGINT NOT NULL COMMENT '盟主商户ID',
  `alliance_status` VARCHAR(32) NOT NULL DEFAULT 'active' COMMENT '状态',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `del_flag` CHAR(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`alliance_id`),
  KEY `idx_rental_alliance_owner` (`owner_member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多仓联盟';

CREATE TABLE IF NOT EXISTS `ry_rental_alliance_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `alliance_id` BIGINT NOT NULL COMMENT '联盟ID',
  `member_id` BIGINT NOT NULL COMMENT '成员商户ID',
  `accept_order_limit` INT DEFAULT NULL COMMENT '可承接订单上限',
  `share_stock_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否共享库存',
  `member_status` VARCHAR(32) NOT NULL DEFAULT 'active' COMMENT '状态',
  `join_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rental_alliance_member` (`alliance_id`, `member_id`),
  KEY `idx_rental_alliance_member_member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='联盟成员';

CREATE TABLE IF NOT EXISTS `ry_rental_alliance_order` (
  `alliance_order_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '联盟订单ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `alliance_id` BIGINT NOT NULL COMMENT '联盟ID',
  `source_order_id` BIGINT DEFAULT NULL COMMENT '原订单ID',
  `initiator_member_id` BIGINT NOT NULL COMMENT '发起方',
  `receiver_member_id` BIGINT DEFAULT NULL COMMENT '承接方',
  `settlement_price` DECIMAL(18,2) DEFAULT NULL COMMENT '成员结算价',
  `masked_customer_json` JSON COMMENT '脱敏客户信息',
  `shipping_mode` VARCHAR(32) DEFAULT NULL COMMENT '发货方式',
  `alliance_order_status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '状态',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (`alliance_order_id`),
  KEY `idx_rental_alliance_order_receiver` (`receiver_member_id`, `alliance_order_status`),
  KEY `idx_rental_alliance_order_initiator` (`initiator_member_id`, `alliance_order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='联盟协助订单';

-- ----------------------------
-- 9. 库存周转市场
-- ----------------------------

CREATE TABLE IF NOT EXISTS `ry_rental_market_post` (
  `post_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '发布商户ID',
  `board_type` VARCHAR(32) NOT NULL COMMENT '板块：sale出售 rent出租 buy求购',
  `post_no` VARCHAR(64) NOT NULL COMMENT '帖子编号',
  `category` VARCHAR(64) DEFAULT NULL COMMENT '品类',
  `brand` VARCHAR(64) DEFAULT NULL COMMENT '品牌',
  `model_name` VARCHAR(128) DEFAULT NULL COMMENT '型号',
  `memory_spec` VARCHAR(64) DEFAULT NULL COMMENT '内存/规格',
  `condition_level` VARCHAR(32) DEFAULT NULL COMMENT '成色',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `price_range_code` VARCHAR(64) DEFAULT NULL COMMENT '系统价格区间',
  `city` VARCHAR(64) DEFAULT NULL COMMENT '发货城市',
  `service_tags` VARCHAR(255) DEFAULT NULL COMMENT '服务标签',
  `recent_30d_turnover` INT NOT NULL DEFAULT 0 COMMENT '近30天周转次数',
  `idle_qty` INT NOT NULL DEFAULT 0 COMMENT '闲置数量',
  `rented_qty` INT NOT NULL DEFAULT 0 COMMENT '出租中数量',
  `long_idle_qty` INT NOT NULL DEFAULT 0 COMMENT '长期未使用数量',
  `post_status` VARCHAR(32) NOT NULL DEFAULT 'draft' COMMENT '帖子状态',
  `down_rank_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否降权',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `del_flag` CHAR(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`post_id`),
  UNIQUE KEY `uk_rental_market_post_no` (`member_id`, `post_no`),
  KEY `idx_rental_market_list` (`board_type`, `post_status`, `city`, `recent_30d_turnover`, `idle_qty`, `create_time`),
  KEY `idx_rental_market_member` (`member_id`, `post_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存周转市场帖子';

CREATE TABLE IF NOT EXISTS `ry_rental_market_message` (
  `message_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `from_member_id` BIGINT NOT NULL COMMENT '发送商户',
  `to_member_id` BIGINT NOT NULL COMMENT '接收商户',
  `message_content` VARCHAR(1000) NOT NULL COMMENT '消息内容',
  `read_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读',
  `risk_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '风险标记',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`message_id`),
  KEY `idx_rental_market_msg_post` (`post_id`, `create_time`),
  KEY `idx_rental_market_msg_to` (`to_member_id`, `read_flag`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='市场站内私信';

CREATE TABLE IF NOT EXISTS `ry_rental_market_report` (
  `report_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `reporter_member_id` BIGINT NOT NULL COMMENT '举报商户',
  `reported_member_id` BIGINT NOT NULL COMMENT '被举报商户',
  `report_type` VARCHAR(64) NOT NULL COMMENT '举报类型',
  `evidence_json` JSON COMMENT '证据',
  `report_status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '处理状态',
  `review_result` VARCHAR(1000) DEFAULT NULL COMMENT '处理结果',
  `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人',
  `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`report_id`),
  KEY `idx_rental_market_report_post` (`post_id`, `report_status`),
  KEY `idx_rental_market_report_member` (`reported_member_id`, `report_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='市场举报';

CREATE TABLE IF NOT EXISTS `ry_rental_market_deposit_ledger` (
  `deposit_ledger_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '保证金流水ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `amount` DECIMAL(18,2) NOT NULL COMMENT '金额',
  `direction` VARCHAR(16) NOT NULL COMMENT '方向：pay缴纳 deduct扣除 refund退回',
  `ref_type` VARCHAR(64) DEFAULT NULL COMMENT '关联类型',
  `ref_id` BIGINT DEFAULT NULL COMMENT '关联ID',
  `ledger_status` VARCHAR(32) NOT NULL DEFAULT 'success' COMMENT '状态',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (`deposit_ledger_id`),
  KEY `idx_rental_market_deposit_member` (`member_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='市场保证金流水';

-- ----------------------------
-- 10. 小程序、转发、免押
-- ----------------------------

CREATE TABLE IF NOT EXISTS `ry_rental_miniapp_auth` (
  `auth_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '授权ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT DEFAULT NULL COMMENT '商户ID',
  `openid` VARCHAR(128) NOT NULL COMMENT 'openid',
  `unionid` VARCHAR(128) DEFAULT NULL COMMENT 'unionid',
  `phone` VARCHAR(32) DEFAULT NULL COMMENT '授权手机号',
  `auth_scene` VARCHAR(64) DEFAULT NULL COMMENT '授权场景',
  `consent_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否勾选协议',
  `last_auth_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近授权时间',
  `expire_time` DATETIME DEFAULT NULL COMMENT '会话过期时间',
  PRIMARY KEY (`auth_id`),
  KEY `idx_rental_miniapp_openid` (`openid`),
  KEY `idx_rental_miniapp_phone` (`member_id`, `phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小程序授权记录';

CREATE TABLE IF NOT EXISTS `ry_rental_share_log` (
  `share_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '转发ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `order_id` BIGINT DEFAULT NULL COMMENT '订单ID',
  `share_type` VARCHAR(64) NOT NULL COMMENT '转发类型',
  `share_target` VARCHAR(255) DEFAULT NULL COMMENT '转发目标',
  `template_id` BIGINT DEFAULT NULL COMMENT '模板ID',
  `snapshot_json` JSON COMMENT '转发快照',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`share_id`),
  KEY `idx_rental_share_member_time` (`member_id`, `create_time`),
  KEY `idx_rental_share_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单转发记录';

CREATE TABLE IF NOT EXISTS `ry_rental_deposit_free_code` (
  `code_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '免押码ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `order_id` BIGINT DEFAULT NULL COMMENT '订单ID',
  `customer_id` BIGINT DEFAULT NULL COMMENT '客户ID',
  `deposit_free_code` VARCHAR(128) NOT NULL COMMENT '免押码',
  `code_status` VARCHAR(32) NOT NULL DEFAULT 'created' COMMENT '状态',
  `agreement_id` VARCHAR(128) DEFAULT NULL COMMENT '协议ID',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
  `success_time` DATETIME DEFAULT NULL COMMENT '免押成功时间',
  `raw_callback` JSON COMMENT '回调原文',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`code_id`),
  UNIQUE KEY `uk_rental_deposit_free_code` (`deposit_free_code`),
  KEY `idx_rental_deposit_free_order` (`order_id`),
  KEY `idx_rental_deposit_free_member` (`member_id`, `code_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='免押码';

-- ----------------------------
-- 11. 幂等、审计、风控
-- ----------------------------

CREATE TABLE IF NOT EXISTS `ry_rental_idempotent_request` (
  `request_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '幂等请求ID',
  `idempotent_key` VARCHAR(128) NOT NULL COMMENT '幂等Key',
  `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
  `member_id` BIGINT DEFAULT NULL COMMENT '商户ID',
  `request_path` VARCHAR(255) NOT NULL COMMENT '请求路径',
  `request_hash` VARCHAR(128) DEFAULT NULL COMMENT '请求哈希',
  `response_json` JSON COMMENT '响应快照',
  `request_status` VARCHAR(32) NOT NULL DEFAULT 'processing' COMMENT '状态',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`request_id`),
  UNIQUE KEY `uk_rental_idempotent_key` (`idempotent_key`),
  KEY `idx_rental_idempotent_expire` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接口幂等记录';

CREATE TABLE IF NOT EXISTS `ry_rental_audit_log` (
  `audit_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '审计ID',
  `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
  `member_id` BIGINT DEFAULT NULL COMMENT '商户ID',
  `actor_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `actor_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人',
  `actor_type` VARCHAR(32) DEFAULT NULL COMMENT '操作人类型',
  `action_code` VARCHAR(128) NOT NULL COMMENT '动作编码',
  `object_type` VARCHAR(64) NOT NULL COMMENT '对象类型',
  `object_id` VARCHAR(64) DEFAULT NULL COMMENT '对象ID',
  `before_json` JSON COMMENT '变更前',
  `after_json` JSON COMMENT '变更后',
  `sensitive_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否敏感操作',
  `client_ip` VARCHAR(64) DEFAULT NULL COMMENT 'IP',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT 'UA',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`audit_id`),
  KEY `idx_rental_audit_actor` (`actor_id`, `create_time`),
  KEY `idx_rental_audit_object` (`object_type`, `object_id`),
  KEY `idx_rental_audit_tenant` (`tenant_id`, `member_id`, `create_time`),
  KEY `idx_rental_audit_sensitive` (`sensitive_flag`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁业务审计日志';

SET FOREIGN_KEY_CHECKS = 1;
