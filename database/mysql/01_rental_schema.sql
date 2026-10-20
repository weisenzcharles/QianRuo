-- 租赁系统业务表结构 V1
-- 约定：
-- 1. tenant_id/member_id 用于租户/商户隔离，后端查询必须强制带租户条件。
-- 2. 金额字段统一 DECIMAL(18,2)，数量字段统一 INT 或 DECIMAL(18,4)。
-- 3. status 字段使用数值业务字典编码，避免数据库 enum 后期扩展困难。
-- 4. RuoYi 通用审计字段：create_by/create_time/update_by/update_time/remark/is_delete。
-- 5. 敏感信息字段如 customer_phone/customer_address 支持后续应用层加密或脱敏展示。

USE `ry_rental`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 商户、主体、员工扩展
-- ----------------------------

CREATE TABLE IF NOT EXISTS `ry_rental_member` (
  `member_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商户/会员唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_code` VARCHAR(64) NOT NULL COMMENT '商户编码',
  `member_type` TINYINT NOT NULL DEFAULT 1 COMMENT '商户类型：1个人 2企业',
  `membership_type` TINYINT NOT NULL DEFAULT 1 COMMENT '会员类型：1个人会员 2普通企业会员 3入驻会员',
  `auth_status` TINYINT NOT NULL DEFAULT 0 COMMENT '认证状态：0未认证 1待支付 2待审核 3已认证 4已驳回',
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
  `wechat_qr_file_id` BIGINT DEFAULT NULL COMMENT '微信二维码文件唯一编号',
  `vip_level` VARCHAR(32) NOT NULL DEFAULT 'free' COMMENT '会员等级',
  `storage_quota_mb` BIGINT NOT NULL DEFAULT 1024 COMMENT '存储额度MB',
  `external_accept_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否允许库存对外接单',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态：0正常 1停用',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标志：0正常 1删除',
  `active_contact_phone` VARCHAR(32) GENERATED ALWAYS AS (
    CASE WHEN `is_delete` = 0 AND `contact_phone` IS NOT NULL AND `contact_phone` <> ''
      THEN `contact_phone` ELSE NULL END
  ) STORED COMMENT '有效商户手机号唯一键',
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `uk_rental_member_code` (`member_code`),
  UNIQUE KEY `uk_rental_member_contact_phone` (`active_contact_phone`),
  KEY `idx_rental_member_tenant` (`tenant_id`),
  KEY `idx_rental_member_auth_status` (`auth_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁商户/会员主体';

CREATE TABLE IF NOT EXISTS `ry_rental_member_credit_account` (
  `credit_account_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '信用账户唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `credit_score` INT NOT NULL DEFAULT 100 COMMENT '当前信用分，范围0-100',
  `credit_level` TINYINT NOT NULL DEFAULT 1 COMMENT '信用等级：1正常 2关注 3风险',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remove_by` BIGINT DEFAULT NULL COMMENT '删除者用户唯一编号',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`credit_account_id`),
  UNIQUE KEY `uk_rental_member_credit_account` (`tenant_id`, `member_id`),
  KEY `idx_rental_member_credit_level` (`member_id`, `credit_level`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户信用账户';

CREATE TABLE IF NOT EXISTS `ry_rental_member_credit_record` (
  `credit_record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '信用记录唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `record_no` VARCHAR(48) NOT NULL COMMENT '信用记录编码',
  `event_type` TINYINT NOT NULL COMMENT '信用事件类型：1人工调整 2履约 3售后纠纷 4财务 5正向行为 6申诉冲正',
  `score_before` INT NOT NULL COMMENT '变更前信用分',
  `score_change` INT NOT NULL COMMENT '信用分变更值',
  `score_after` INT NOT NULL COMMENT '变更后信用分',
  `reason` VARCHAR(500) NOT NULL COMMENT '变更原因',
  `source_no` VARCHAR(128) DEFAULT NULL COMMENT '关联业务编码',
  `reversal_of_record_no` VARCHAR(48) DEFAULT NULL COMMENT '被冲正的原信用记录编码',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remove_by` BIGINT DEFAULT NULL COMMENT '删除者用户唯一编号',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`credit_record_id`),
  UNIQUE KEY `uk_rental_member_credit_record_no` (`record_no`),
  UNIQUE KEY `uk_rental_member_credit_reversal` (`tenant_id`, `member_id`, `reversal_of_record_no`, `is_delete`),
  KEY `idx_rental_member_credit_record_scope` (`tenant_id`, `member_id`, `create_time`),
  KEY `idx_rental_member_credit_record_source` (`source_no`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户信用变更流水';

CREATE TABLE IF NOT EXISTS `ry_rental_member_blacklist_record` (
  `blacklist_record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '黑名单记录唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `record_no` VARCHAR(48) NOT NULL COMMENT '黑名单记录编码',
  `reason_type` TINYINT NOT NULL COMMENT '列入原因：1认证 2履约 3售后纠纷 4财务 9其他',
  `blacklist_status` TINYINT NOT NULL DEFAULT 1 COMMENT '黑名单状态：1生效 2已解除',
  `reason` VARCHAR(500) NOT NULL COMMENT '列入原因说明',
  `effective_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
  `expires_time` DATETIME DEFAULT NULL COMMENT '到期时间，为空表示长期有效',
  `released_by` BIGINT DEFAULT NULL COMMENT '解除人用户唯一编号',
  `released_time` DATETIME DEFAULT NULL COMMENT '解除时间',
  `release_remark` VARCHAR(500) DEFAULT NULL COMMENT '解除说明',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remove_by` BIGINT DEFAULT NULL COMMENT '删除者用户唯一编号',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`blacklist_record_id`),
  UNIQUE KEY `uk_rental_member_blacklist_record_no` (`record_no`),
  KEY `idx_rental_member_blacklist_scope` (`tenant_id`, `member_id`, `blacklist_status`, `is_delete`),
  KEY `idx_rental_member_blacklist_expire` (`expires_time`, `blacklist_status`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户黑名单历史';

CREATE TABLE IF NOT EXISTS `ry_rental_member_certification` (
  `certification_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '认证记录唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `version_no` INT NOT NULL COMMENT '认证资料版本号',
  `certification_type` TINYINT NOT NULL COMMENT '认证类型：1个人 2企业',
  `certification_status` TINYINT NOT NULL DEFAULT 0 COMMENT '认证状态：0待审核 1已通过 2已驳回 3已过期',
  `legal_subject_name` VARCHAR(128) DEFAULT NULL COMMENT '认证主体名称',
  `license_no` VARCHAR(64) DEFAULT NULL COMMENT '营业执照/主体编号',
  `registered_capital` DECIMAL(18,2) DEFAULT NULL COMMENT '注册资本',
  `business_scope` VARCHAR(2000) DEFAULT NULL COMMENT '经营范围',
  `license_address` VARCHAR(500) DEFAULT NULL COMMENT '执照登记地址',
  `license_valid_from` DATE DEFAULT NULL COMMENT '执照有效期开始',
  `license_valid_to` DATE DEFAULT NULL COMMENT '执照有效期结束',
  `license_long_term` TINYINT NOT NULL DEFAULT 0 COMMENT '执照是否长期有效：0否 1是',
  `legal_representative_name` VARCHAR(64) DEFAULT NULL COMMENT '法人姓名',
  `legal_representative_id_no` VARCHAR(64) DEFAULT NULL COMMENT '法人身份证号',
  `submitted_by` VARCHAR(64) DEFAULT NULL COMMENT '提交人',
  `submitted_time` DATETIME DEFAULT NULL COMMENT '提交时间',
  `reviewed_by` VARCHAR(64) DEFAULT NULL COMMENT '审核人',
  `reviewed_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `review_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`certification_id`),
  UNIQUE KEY `uk_rental_member_certification_version` (`member_id`, `version_no`),
  KEY `idx_rental_member_certification_scope` (`tenant_id`, `member_id`, `certification_status`),
  KEY `idx_rental_member_certification_submitted` (`member_id`, `submitted_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户认证资料版本与审核历史';

CREATE TABLE IF NOT EXISTS `ry_rental_certification_material` (
  `material_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '认证材料唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `certification_id` BIGINT NOT NULL COMMENT '认证版本唯一编号',
  `material_type` TINYINT NOT NULL COMMENT '材料类型：1营业执照 2身份证人像面 3身份证国徽面 4合同 9其他',
  `material_name` VARCHAR(128) NOT NULL COMMENT '材料名称',
  `file_id` BIGINT DEFAULT NULL COMMENT '文件唯一编号',
  `file_url` VARCHAR(500) NOT NULL COMMENT '文件地址',
  `material_status` TINYINT NOT NULL DEFAULT 0 COMMENT '材料状态：0待审核 1已通过 2已驳回',
  `review_remark` VARCHAR(500) DEFAULT NULL COMMENT '材料审核意见',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '显示排序',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`material_id`),
  UNIQUE KEY `uk_rental_certification_material` (`certification_id`, `material_type`, `file_url`),
  KEY `idx_rental_certification_material_scope` (`tenant_id`, `member_id`, `certification_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户认证材料明细';

CREATE TABLE IF NOT EXISTS `ry_rental_member_membership_history` (
  `history_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会员类型历史唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `previous_type` TINYINT DEFAULT NULL COMMENT '变更前会员类型：1个人会员 2普通企业会员 3入驻会员',
  `membership_type` TINYINT NOT NULL COMMENT '变更后会员类型：1个人会员 2普通企业会员 3入驻会员',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '变更原因',
  `operator_name` VARCHAR(64) NOT NULL COMMENT '操作人',
  `operated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`history_id`),
  KEY `idx_rental_membership_history_scope` (`tenant_id`, `member_id`, `operated_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户会员类型变更历史';

CREATE TABLE IF NOT EXISTS `ry_rental_member_qualification` (
  `qualification_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '接单资格唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `qualification_code` VARCHAR(32) NOT NULL COMMENT '资格编码：external_accept对外接单 alliance_accept联盟接单 mini_program_accept小程序接单',
  `qualification_status` TINYINT NOT NULL DEFAULT 0 COMMENT '资格状态：0未开通 1待审核 2已开通 3已驳回',
  `certification_id` BIGINT DEFAULT NULL COMMENT '关联认证版本唯一编号',
  `effective_from` DATETIME DEFAULT NULL COMMENT '生效时间',
  `effective_to` DATETIME DEFAULT NULL COMMENT '失效时间',
  `reviewed_by` VARCHAR(64) DEFAULT NULL COMMENT '审核人',
  `reviewed_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `review_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`qualification_id`),
  UNIQUE KEY `uk_rental_member_qualification` (`member_id`, `qualification_code`, `is_delete`),
  KEY `idx_rental_member_qualification_scope` (`tenant_id`, `member_id`, `qualification_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户当前接单资格';

CREATE TABLE IF NOT EXISTS `ry_rental_member_qualification_history` (
  `history_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '接单资格历史唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `qualification_id` BIGINT DEFAULT NULL COMMENT '当前资格唯一编号',
  `qualification_code` VARCHAR(32) NOT NULL COMMENT '资格编码',
  `previous_status` TINYINT DEFAULT NULL COMMENT '变更前状态：0未开通 1待审核 2已开通 3已驳回',
  `qualification_status` TINYINT NOT NULL COMMENT '变更后状态：0未开通 1待审核 2已开通 3已驳回',
  `certification_id` BIGINT DEFAULT NULL COMMENT '关联认证版本唯一编号',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '变更原因',
  `operator_name` VARCHAR(64) NOT NULL COMMENT '操作人',
  `operated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`history_id`),
  KEY `idx_rental_qualification_history_scope` (`tenant_id`, `member_id`, `operated_time`),
  KEY `idx_rental_qualification_history_code` (`member_id`, `qualification_code`, `operated_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户接单资格变更历史';

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_user` (
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

CREATE TABLE IF NOT EXISTS `ry_rental_user_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `merchant_user_id` BIGINT NOT NULL COMMENT '商户用户唯一编号',
  `member_role` VARCHAR(32) NOT NULL DEFAULT 'staff' COMMENT '商户内角色',
  `warehouse_scope_type` TINYINT NOT NULL DEFAULT 1 COMMENT '仓库范围：1全部 2指定',
  `sensitive_scope` TINYINT NOT NULL DEFAULT 1 COMMENT '敏感字段权限：1脱敏 2明文 3导出',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rental_user_member` (`member_id`, `merchant_user_id`),
  KEY `idx_rental_user_member_user` (`merchant_user_id`),
  KEY `idx_rental_user_member_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户员工与商户用户绑定';

CREATE TABLE IF NOT EXISTS `ry_rental_user_warehouse_scope` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `merchant_user_id` BIGINT NOT NULL COMMENT '商户用户唯一编号',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库唯一编号',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rental_user_warehouse` (`member_id`, `merchant_user_id`, `warehouse_id`),
  KEY `idx_rental_user_warehouse_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户仓库数据权限';

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_menu` (
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

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_oper_log` (
  `oper_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志唯一编号',
  `tenant_id` BIGINT DEFAULT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT DEFAULT NULL COMMENT '商户唯一编号',
  `operator_user_id` BIGINT DEFAULT NULL COMMENT '商户用户唯一编号',
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
  `trace_id` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '请求链路ID',
  `http_status` INT DEFAULT NULL COMMENT 'HTTP响应状态码',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`oper_id`),
  KEY `idx_merchant_oper_log_scope_time` (`tenant_id`, `member_id`, `oper_time`),
  KEY `idx_merchant_oper_log_scope_status` (`tenant_id`, `member_id`, `status`, `oper_time`),
  KEY `idx_merchant_oper_log_scope_user` (`tenant_id`, `member_id`, `operator_user_id`, `oper_time`),
  KEY `idx_merchant_oper_log_trace` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户请求级日志兼容保留表（商户业务日志由独立审计表保存）';

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_business_audit_log` (
  `audit_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '审计唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `operator_user_id` BIGINT DEFAULT NULL COMMENT '商户用户唯一编号',
  `operator_name` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '操作人',
  `business_module` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '业务模块',
  `operation_type` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '操作类型',
  `target_type` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '操作对象类型',
  `target_id` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '操作对象唯一编号或业务编码',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='历史重复商户业务审计表（已停用，商户日志读取 ry_rental_audit_log）';

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_role` (
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

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_role_template` (
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

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_role_template_menu` (
  `role_template_id` BIGINT NOT NULL COMMENT '全局商户角色模板唯一编号',
  `menu_id` BIGINT NOT NULL COMMENT '商户菜单唯一编号',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`role_template_id`, `menu_id`),
  KEY `idx_role_template_menu_menu` (`menu_id`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局商户角色模板菜单授权';

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_role_menu` (
  `role_id` BIGINT NOT NULL COMMENT '商户角色唯一编号',
  `menu_id` BIGINT NOT NULL COMMENT '商户菜单唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`role_id`, `menu_id`),
  KEY `idx_rental_merchant_role_menu_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_merchant_role_menu_menu` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户角色和菜单关联';

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_user_role` (
  `merchant_user_id` BIGINT NOT NULL COMMENT '商户用户唯一编号',
  `role_id` BIGINT NOT NULL COMMENT '商户角色唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`merchant_user_id`, `role_id`),
  KEY `idx_rental_merchant_user_role_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_merchant_user_role_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户用户和角色关联';

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_user_menu` (
  `merchant_user_id` BIGINT NOT NULL COMMENT '商户用户唯一编号',
  `menu_id` BIGINT NOT NULL COMMENT '商户菜单唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`merchant_user_id`, `menu_id`),
  KEY `idx_rental_merchant_user_menu_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_merchant_user_menu_menu` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户用户和菜单关联';

CREATE TABLE IF NOT EXISTS `ry_rental_product_class` (
  `class_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '产品分类唯一编号',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类唯一编号，根节点为0',
  `ancestors` VARCHAR(500) NOT NULL DEFAULT '0' COMMENT '祖先分类路径',
  `class_code` VARCHAR(64) NOT NULL COMMENT '分类编码',
  `class_name` VARCHAR(128) NOT NULL COMMENT '分类名称',
  `class_level` INT NOT NULL DEFAULT 1 COMMENT '分类层级，根节点从1开始',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '同级显示顺序',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态：0正常 1停用',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  `active_class_code` VARCHAR(64) GENERATED ALWAYS AS (
    CASE WHEN `is_delete` = 0 THEN `class_code` ELSE NULL END
  ) STORED COMMENT '有效分类编码唯一键',
  `active_class_name` VARCHAR(128) GENERATED ALWAYS AS (
    CASE WHEN `is_delete` = 0 THEN `class_name` ELSE NULL END
  ) STORED COMMENT '有效同级分类名称唯一键',
  PRIMARY KEY (`class_id`),
  UNIQUE KEY `uk_rental_product_class_active_code` (`active_class_code`),
  UNIQUE KEY `uk_rental_product_class_active_parent_name` (`parent_id`, `active_class_name`),
  KEY `idx_rental_product_class_parent` (`parent_id`, `status`, `is_delete`),
  KEY `idx_rental_product_class_ancestors` (`ancestors`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台产品分类';

CREATE TABLE IF NOT EXISTS `ry_rental_member_product_class` (
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `class_id` BIGINT NOT NULL COMMENT '平台产品分类唯一编号',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`tenant_id`, `member_id`, `class_id`),
  KEY `idx_rental_member_class_member` (`member_id`, `is_delete`),
  KEY `idx_rental_member_class_class` (`class_id`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户经营产品分类';

-- ----------------------------
-- 2. 仓库与基础资料
-- ----------------------------

/* 历史执行面表已迁入 ry_business；新环境不再创建。 */
/* CREATE TABLE IF NOT EXISTS `ry_rental_warehouse` (
  `warehouse_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '仓库唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
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
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`warehouse_id`),
  UNIQUE KEY `uk_rental_warehouse_code` (`member_id`, `warehouse_code`),
  KEY `idx_rental_warehouse_tenant_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_warehouse_city` (`city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库'; */

CREATE TABLE IF NOT EXISTS `ry_rental_brand` (
  `brand_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '品牌唯一编号',
  `brand_name` VARCHAR(64) NOT NULL COMMENT '品牌名称',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态：0正常 1停用',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  `active_brand_name` VARCHAR(64) GENERATED ALWAYS AS (
    CASE WHEN `is_delete` = 0 THEN `brand_name` ELSE NULL END
  ) STORED COMMENT '有效品牌名称唯一键',
  PRIMARY KEY (`brand_id`),
  UNIQUE KEY `uk_rental_brand_active_name` (`active_brand_name`),
  KEY `idx_rental_brand_status` (`status`, `is_delete`, `sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台品牌主数据';

CREATE TABLE IF NOT EXISTS `ry_rental_brand_class` (
  `brand_id` BIGINT NOT NULL COMMENT '品牌唯一编号',
  `class_id` BIGINT NOT NULL COMMENT '产品分类唯一编号',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`brand_id`, `class_id`),
  KEY `idx_rental_brand_class_class` (`class_id`, `is_delete`),
  KEY `idx_rental_brand_class_brand` (`brand_id`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台品牌经营分类关系';

CREATE TABLE IF NOT EXISTS `ry_rental_model` (
  `model_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标准型号唯一编号',
  `class_id` BIGINT NOT NULL COMMENT '平台产品分类唯一编号',
  `brand_id` BIGINT DEFAULT NULL COMMENT '平台品牌唯一编号；有效型号必须关联品牌，已删除历史记录允许为空',
  `model_name` VARCHAR(128) NOT NULL COMMENT '型号',
  `alias` VARCHAR(128) DEFAULT NULL COMMENT '型号别名',
  `memory_spec` VARCHAR(64) DEFAULT NULL COMMENT '内存/规格',
  `official_flag` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否官方标准：0否 1是',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态：0正常 1停用',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  `active_class_id` BIGINT GENERATED ALWAYS AS (
    CASE WHEN `is_delete` = 0 THEN `class_id` ELSE NULL END
  ) STORED COMMENT '有效型号分类唯一键',
  `active_brand_id` BIGINT GENERATED ALWAYS AS (
    CASE WHEN `is_delete` = 0 THEN `brand_id` ELSE NULL END
  ) STORED COMMENT '有效型号品牌唯一键',
  `active_model_name` VARCHAR(128) GENERATED ALWAYS AS (
    CASE WHEN `is_delete` = 0 THEN `model_name` ELSE NULL END
  ) STORED COMMENT '有效型号名称唯一键',
  `active_memory_spec` VARCHAR(64) GENERATED ALWAYS AS (
    CASE WHEN `is_delete` = 0 THEN COALESCE(`memory_spec`, '') ELSE NULL END
  ) STORED COMMENT '有效型号规格唯一键',
  PRIMARY KEY (`model_id`),
  UNIQUE KEY `uk_rental_model_active_identity`
    (`active_class_id`, `active_brand_id`, `active_model_name`, `active_memory_spec`),
  KEY `idx_rental_model_brand_id` (`brand_id`, `status`, `is_delete`),
  KEY `idx_rental_model_class` (`class_id`, `status`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统标准型号';

CREATE TABLE IF NOT EXISTS `ry_rental_member_common_model` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `model_id` BIGINT NOT NULL COMMENT '标准型号唯一编号',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rental_member_model` (`member_id`, `model_id`),
  KEY `idx_rental_member_common_model_tenant` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户常用型号';

-- ----------------------------
-- 3. 商品、SN、配件、套餐
-- ----------------------------

/* 历史执行面表已迁入 ry_business；新环境不再创建。 */
/* CREATE TABLE IF NOT EXISTS `ry_rental_product` (
  `product_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `class_id` BIGINT DEFAULT NULL COMMENT '平台产品分类唯一编号',
  `model_id` BIGINT DEFAULT NULL COMMENT '标准型号唯一编号',
  `product_code` VARCHAR(64) NOT NULL COMMENT '商品编码',
  `product_name` VARCHAR(128) NOT NULL COMMENT '商品名称',
  `category_level1` VARCHAR(64) NOT NULL COMMENT '一级分类',
  `category_level2` VARCHAR(64) DEFAULT NULL COMMENT '二级分类',
  `category_level3` VARCHAR(64) DEFAULT NULL COMMENT '三级分类',
  `brand` VARCHAR(64) DEFAULT NULL COMMENT '品牌',
  `model_name` VARCHAR(128) DEFAULT NULL COMMENT '型号',
  `memory_spec` VARCHAR(64) DEFAULT NULL COMMENT '内存/规格',
  `description` VARCHAR(1000) DEFAULT NULL COMMENT '商品/配件说明',
  `product_type` VARCHAR(20) NOT NULL DEFAULT 'device' COMMENT '类型：device设备 accessory配件',
  `stock_mode` VARCHAR(20) NOT NULL DEFAULT 'sn' COMMENT '库存模式：sn序列号 qty总数',
  `default_image_file_id` BIGINT DEFAULT NULL COMMENT '默认图片文件唯一编号',
  `purchase_cost` DECIMAL(18,2) DEFAULT NULL COMMENT '默认采购成本',
  `sale_price` DECIMAL(18,2) DEFAULT NULL COMMENT '默认出售价格',
  `frontend_visible` TINYINT NOT NULL DEFAULT 1 COMMENT '前台是否展示',
  `stats_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否参与统计',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`product_id`),
  UNIQUE KEY `uk_rental_product_code` (`member_id`, `product_code`),
  KEY `idx_rental_product_tenant_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_product_member_class` (`member_id`, `class_id`, `status`, `is_delete`),
  KEY `idx_rental_product_model` (`model_id`),
  KEY `idx_rental_product_type` (`product_type`, `stock_mode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品'; */

/* 历史执行面表已迁入 ry_business；新环境不再创建。 */
/* CREATE TABLE IF NOT EXISTS `ry_rental_asset_sn` (
  `sn_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'SN设备唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `product_id` BIGINT NOT NULL COMMENT '商品唯一编号',
  `warehouse_id` BIGINT DEFAULT NULL COMMENT '当前仓库唯一编号',
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
  `latest_photo_file_id` BIGINT DEFAULT NULL COMMENT '最新照片文件唯一编号',
  `total_rent_count` INT NOT NULL DEFAULT 0 COMMENT '累计租赁次数',
  `recent_3m_rent_count` INT NOT NULL DEFAULT 0 COMMENT '近3个月租赁次数',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`sn_id`),
  UNIQUE KEY `uk_rental_asset_sn` (`member_id`, `sn_code`),
  KEY `idx_rental_asset_tenant_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_asset_product` (`product_id`),
  KEY `idx_rental_asset_warehouse_status` (`warehouse_id`, `asset_status`),
  KEY `idx_rental_asset_insurance` (`insurance_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备SN'; */

/* 历史执行面表已迁移到 ry_business；新环境不再创建。
CREATE TABLE IF NOT EXISTS `ry_rental_asset_photo` (
  `photo_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '照片唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `sn_id` BIGINT NOT NULL COMMENT 'SN设备唯一编号',
  `file_id` BIGINT NOT NULL COMMENT '文件唯一编号',
  `photo_type` VARCHAR(32) NOT NULL DEFAULT 'device' COMMENT '照片类型',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`photo_id`),
  KEY `idx_rental_asset_photo_sn` (`sn_id`, `create_time`),
  KEY `idx_rental_asset_photo_tenant` (`tenant_id`, `member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备照片'; */

/* 历史执行面表已迁入 ry_business；新环境不再创建。 */
/* CREATE TABLE IF NOT EXISTS `ry_rental_accessory_item` (
  `accessory_item_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配件实例唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `product_id` BIGINT NOT NULL COMMENT '配件商品唯一编号',
  `warehouse_id` BIGINT DEFAULT NULL COMMENT '仓库唯一编号',
  `item_code` VARCHAR(128) NOT NULL COMMENT '配件编码/SN/虚拟ID',
  `stock_mode` VARCHAR(20) NOT NULL DEFAULT 'qty' COMMENT '来源模式：sn qty',
  `item_status` VARCHAR(32) NOT NULL DEFAULT 'available' COMMENT '配件状态',
  `compatible_model_ids` VARCHAR(500) DEFAULT NULL COMMENT '兼容型号唯一编号列表',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  `status` TINYINT(1) NOT NULL DEFAULT 0,
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`accessory_item_id`),
  UNIQUE KEY `uk_rental_accessory_item` (`member_id`, `item_code`),
  KEY `idx_rental_accessory_product` (`product_id`),
  KEY `idx_rental_accessory_warehouse_status` (`warehouse_id`, `item_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配件实例'; */

/* 历史执行面表已迁入 ry_business；新环境不再创建。 */
/* CREATE TABLE IF NOT EXISTS `ry_rental_package_template` (
  `package_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '套餐唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `platform_code` VARCHAR(64) DEFAULT NULL COMMENT '适用平台/渠道',
  `package_code` VARCHAR(64) NOT NULL COMMENT '套餐编码',
  `package_name` VARCHAR(128) NOT NULL COMMENT '套餐名称',
  `main_product_id` BIGINT DEFAULT NULL COMMENT '主商品唯一编号',
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`package_id`),
  UNIQUE KEY `uk_rental_package_code` (`member_id`, `package_code`),
  KEY `idx_rental_package_tenant_member` (`tenant_id`, `member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐模板'; */

/* 历史执行面表已迁入 ry_business；新环境不再创建。 */
/* CREATE TABLE IF NOT EXISTS `ry_rental_package_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `package_id` BIGINT NOT NULL COMMENT '套餐唯一编号',
  `product_id` BIGINT NOT NULL COMMENT '商品/配件唯一编号',
  `item_role` VARCHAR(32) NOT NULL DEFAULT 'accessory' COMMENT '角色：main主机 accessory配件',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `required_flag` TINYINT NOT NULL DEFAULT 1 COMMENT '是否必选',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`id`),
  KEY `idx_rental_package_item_package` (`package_id`),
  KEY `idx_rental_package_item_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐物料明细'; */

-- ----------------------------
-- 4. 客户、订单、订单明细
-- ----------------------------

/* 历史执行面表已迁入 ry_business；新环境不再创建。 */
/* CREATE TABLE IF NOT EXISTS `ry_rental_customer` (
  `customer_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '客户唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
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
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`customer_id`),
  KEY `idx_rental_customer_member_phone` (`member_id`, `phone`),
  KEY `idx_rental_customer_tenant` (`tenant_id`, `member_id`),
  KEY `idx_rental_customer_openid` (`wechat_openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁客户'; */

/* 历史执行面表已迁入 ry_business；新环境不再创建。 */
/* CREATE TABLE IF NOT EXISTS `ry_rental_order` (
  `order_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `order_no` VARCHAR(64) NOT NULL COMMENT '系统订单编号',
  `external_order_no` VARCHAR(128) DEFAULT NULL COMMENT '外部订单编号',
  `channel_code` VARCHAR(64) DEFAULT NULL COMMENT '订单渠道',
  `shop_name` VARCHAR(128) DEFAULT NULL COMMENT '来源店铺',
  `customer_id` BIGINT DEFAULT NULL COMMENT '客户唯一编号',
  `customer_name` VARCHAR(64) DEFAULT NULL COMMENT '客户姓名快照',
  `customer_phone` VARCHAR(32) DEFAULT NULL COMMENT '客户手机号快照',
  `customer_address` VARCHAR(255) DEFAULT NULL COMMENT '客户地址快照',
  `warehouse_id` BIGINT DEFAULT NULL COMMENT '发货仓库唯一编号',
  `return_warehouse_id` BIGINT DEFAULT NULL COMMENT '归还仓库唯一编号',
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
  `source_order_id` BIGINT DEFAULT NULL COMMENT '转寄来源订单唯一编号',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_rental_order_no` (`member_id`, `order_no`),
  KEY `idx_rental_order_tenant_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_order_status` (`member_id`, `order_status`),
  KEY `idx_rental_order_ship_due` (`member_id`, `warehouse_id`, `ship_due_time`),
  KEY `idx_rental_order_return_due` (`member_id`, `return_warehouse_id`, `return_due_time`),
  KEY `idx_rental_order_customer` (`customer_id`),
  KEY `idx_rental_order_external` (`channel_code`, `external_order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁订单'; */

/* 历史执行面表已迁入 ry_business；新环境不再创建。 */
/* CREATE TABLE IF NOT EXISTS `ry_rental_order_item` (
  `order_item_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单明细唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `order_id` BIGINT NOT NULL COMMENT '订单唯一编号',
  `product_id` BIGINT DEFAULT NULL COMMENT '商品唯一编号',
  `package_id` BIGINT DEFAULT NULL COMMENT '套餐唯一编号',
  `sn_id` BIGINT DEFAULT NULL COMMENT '绑定SN唯一编号',
  `accessory_item_ids` JSON COMMENT '绑定配件唯一编号列表',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `item_status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '明细状态',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`order_item_id`),
  KEY `idx_rental_order_item_order` (`order_id`),
  KEY `idx_rental_order_item_sn` (`sn_id`),
  KEY `idx_rental_order_item_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品明细'; */

/* 历史执行面表已迁移到 ry_business；新环境不再创建。
CREATE TABLE IF NOT EXISTS `ry_rental_order_edit_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '编辑日志唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `order_id` BIGINT NOT NULL COMMENT '订单唯一编号',
  `field_name` VARCHAR(128) NOT NULL COMMENT '字段名',
  `before_value` TEXT COMMENT '修改前',
  `after_value` TEXT COMMENT '修改后',
  `change_reason` VARCHAR(500) DEFAULT NULL COMMENT '修改原因',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人唯一编号',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人',
  `operator_ip` VARCHAR(64) DEFAULT NULL COMMENT '操作IP',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`log_id`),
  KEY `idx_rental_order_edit_order` (`order_id`, `create_time`),
  KEY `idx_rental_order_edit_tenant` (`tenant_id`, `member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单编辑日志'; */

/* 历史执行面表已迁入 ry_business；新环境不再创建。 */
/* CREATE TABLE IF NOT EXISTS `ry_rental_order_note` (
  `note_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '批注唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `order_id` BIGINT NOT NULL COMMENT '订单唯一编号',
  `note_content` VARCHAR(1000) NOT NULL COMMENT '批注内容',
  `pinned_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶',
  `read_user_ids` JSON COMMENT '已阅用户唯一编号',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`note_id`),
  KEY `idx_rental_order_note_order` (`order_id`, `pinned_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单批注'; */

-- ----------------------------
-- 5. 库存、预占、调货、盘点
-- ----------------------------

/* 历史执行面表已迁入 ry_business；新环境不再创建。 */
/* CREATE TABLE IF NOT EXISTS `ry_rental_inventory_reservation` (
  `reservation_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存预占唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `order_id` BIGINT NOT NULL COMMENT '订单唯一编号',
  `product_id` BIGINT NOT NULL COMMENT '商品唯一编号',
  `package_id` BIGINT DEFAULT NULL COMMENT '套餐唯一编号',
  `warehouse_id` BIGINT DEFAULT NULL COMMENT '仓库唯一编号',
  `reserved_qty` INT NOT NULL DEFAULT 1 COMMENT '预占数量',
  `reservation_status` VARCHAR(32) NOT NULL DEFAULT 'reserved' COMMENT '状态：reserved locked released consumed expired',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`reservation_id`),
  KEY `idx_rental_reservation_order` (`order_id`),
  KEY `idx_rental_reservation_stock` (`member_id`, `warehouse_id`, `product_id`, `reservation_status`),
  KEY `idx_rental_reservation_expire` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预占'; */

/* 历史执行面表已迁入 ry_business；新环境不再创建。 */
/* CREATE TABLE IF NOT EXISTS `ry_rental_inventory_ledger` (
  `ledger_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存流水唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `warehouse_id` BIGINT DEFAULT NULL COMMENT '当前仓库唯一编号',
  `product_id` BIGINT DEFAULT NULL COMMENT '商品唯一编号',
  `sn_id` BIGINT DEFAULT NULL COMMENT 'SN唯一编号',
  `accessory_item_id` BIGINT DEFAULT NULL COMMENT '配件实例唯一编号',
  `change_type` VARCHAR(64) NOT NULL COMMENT '变更类型',
  `from_status` VARCHAR(32) DEFAULT NULL COMMENT '原状态',
  `to_status` VARCHAR(32) DEFAULT NULL COMMENT '新状态',
  `change_qty` INT NOT NULL DEFAULT 1 COMMENT '变更数量',
  `ref_type` VARCHAR(64) DEFAULT NULL COMMENT '关联类型',
  `ref_id` BIGINT DEFAULT NULL COMMENT '关联唯一编号',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '原因',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人唯一编号',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`ledger_id`),
  KEY `idx_rental_inventory_ledger_tenant` (`tenant_id`, `member_id`),
  KEY `idx_rental_inventory_ledger_sn` (`sn_id`, `create_time`),
  KEY `idx_rental_inventory_ledger_ref` (`ref_type`, `ref_id`),
  KEY `idx_rental_inventory_ledger_wh_product` (`warehouse_id`, `product_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存状态流水'; */

/* 历史执行面表已迁入 ry_business；新环境不再创建。 */
/* CREATE TABLE IF NOT EXISTS `ry_rental_inventory_snapshot` (
  `snapshot_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存快照唯一编号',
  `snapshot_date` DATE NOT NULL COMMENT '快照日期',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `warehouse_id` BIGINT DEFAULT NULL COMMENT '仓库唯一编号',
  `product_id` BIGINT NOT NULL COMMENT '商品唯一编号',
  `package_id` BIGINT DEFAULT NULL COMMENT '套餐唯一编号',
  `total_qty` INT NOT NULL DEFAULT 0 COMMENT '总库存',
  `free_qty` INT NOT NULL DEFAULT 0 COMMENT '在库空闲',
  `reserved_qty` INT NOT NULL DEFAULT 0 COMMENT '预占',
  `rented_qty` INT NOT NULL DEFAULT 0 COMMENT '租用中',
  `ship_in_transit_qty` INT NOT NULL DEFAULT 0 COMMENT '发货在途',
  `return_in_transit_qty` INT NOT NULL DEFAULT 0 COMMENT '归还在途',
  `expected_in_qty` INT NOT NULL DEFAULT 0 COMMENT '预计到库',
  `available_order_qty` INT NOT NULL DEFAULT 0 COMMENT '可接单数量',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`snapshot_id`),
  UNIQUE KEY `uk_rental_inventory_snapshot` (`snapshot_date`, `member_id`, `warehouse_id`, `product_id`, `package_id`),
  KEY `idx_rental_inventory_snapshot_tenant` (`tenant_id`, `member_id`, `snapshot_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日库存快照'; */

/* 历史执行面表已迁入 ry_business；新环境不再创建。 */
/* CREATE TABLE IF NOT EXISTS `ry_rental_transfer_order` (
  `transfer_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '调货唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `transfer_no` VARCHAR(64) NOT NULL COMMENT '调货单号',
  `from_warehouse_id` BIGINT NOT NULL COMMENT '发货仓',
  `to_warehouse_id` BIGINT NOT NULL COMMENT '到达仓',
  `ref_order_id` BIGINT DEFAULT NULL COMMENT '关联订单',
  `carrier` VARCHAR(64) DEFAULT NULL COMMENT '承运商',
  `tracking_no` VARCHAR(128) DEFAULT NULL COMMENT '快递单号',
  `sn_ids` JSON COMMENT 'SN列表',
  `transfer_status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '状态',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`transfer_id`),
  UNIQUE KEY `uk_rental_transfer_no` (`member_id`, `transfer_no`),
  KEY `idx_rental_transfer_wh` (`from_warehouse_id`, `to_warehouse_id`),
  KEY `idx_rental_transfer_status` (`member_id`, `transfer_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库调货单'; */

/* 历史执行面表已迁入 ry_business；新环境不再创建。 */
/* CREATE TABLE IF NOT EXISTS `ry_rental_stocktaking` (
  `stocktaking_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '盘点唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库唯一编号',
  `stocktaking_no` VARCHAR(64) NOT NULL COMMENT '盘点单号',
  `stocktaking_date` DATE NOT NULL COMMENT '盘点日期',
  `stocktaking_status` VARCHAR(32) NOT NULL DEFAULT 'processing' COMMENT '盘点状态',
  `abnormal_count` INT NOT NULL DEFAULT 0 COMMENT '异常数量',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`stocktaking_id`),
  UNIQUE KEY `uk_rental_stocktaking_no` (`member_id`, `stocktaking_no`),
  KEY `idx_rental_stocktaking_wh_date` (`warehouse_id`, `stocktaking_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点单'; */

/* 历史执行面表已迁入 ry_business；新环境不再创建。 */
/* CREATE TABLE IF NOT EXISTS `ry_rental_stocktaking_item` (
  `item_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '盘点明细唯一编号',
  `stocktaking_id` BIGINT NOT NULL COMMENT '盘点唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `sn_id` BIGINT DEFAULT NULL COMMENT 'SN唯一编号',
  `scan_code` VARCHAR(128) NOT NULL COMMENT '扫码内容',
  `system_status` VARCHAR(32) DEFAULT NULL COMMENT '系统状态',
  `scan_result` VARCHAR(32) NOT NULL DEFAULT 'normal' COMMENT '扫描结果',
  `matched_ref_type` VARCHAR(64) DEFAULT NULL COMMENT '匹配对象类型',
  `matched_ref_id` BIGINT DEFAULT NULL COMMENT '匹配对象唯一编号',
  `remark` VARCHAR(500) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`item_id`),
  KEY `idx_rental_stocktaking_item_main` (`stocktaking_id`),
  KEY `idx_rental_stocktaking_item_sn` (`sn_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点明细'; */

-- ----------------------------
-- 6. 物流、媒体、售后
-- ----------------------------

/* 历史执行面表已迁入 ry_business；新环境不再创建。 */
/* CREATE TABLE IF NOT EXISTS `ry_rental_logistics_order` (
  `logistics_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '物流单唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `order_id` BIGINT DEFAULT NULL COMMENT '订单唯一编号',
  `warehouse_id` BIGINT DEFAULT NULL COMMENT '仓库唯一编号',
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
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`logistics_id`),
  UNIQUE KEY `uk_rental_tracking_no` (`carrier`, `tracking_no`),
  KEY `idx_rental_logistics_order` (`order_id`, `direction`),
  KEY `idx_rental_logistics_member_status` (`member_id`, `route_status`),
  KEY `idx_rental_logistics_pickup` (`pickup_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流单'; */

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_logistics_route` (
  `route_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '路由唯一编号',
  `logistics_id` BIGINT NOT NULL COMMENT '物流单唯一编号',
  `tracking_no` VARCHAR(128) DEFAULT NULL COMMENT '快递单号',
  `route_status` TINYINT DEFAULT NULL COMMENT '节点状态：0待处理 1运输中 2已送达 3异常 4已关闭',
  `route_desc` VARCHAR(1000) DEFAULT NULL COMMENT '节点描述',
  `route_time` DATETIME DEFAULT NULL COMMENT '节点时间',
  `province` VARCHAR(64) DEFAULT NULL COMMENT '省',
  `city` VARCHAR(64) DEFAULT NULL COMMENT '市',
  `raw_json` JSON COMMENT '原始节点',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`route_id`),
  KEY `idx_rental_logistics_route_main` (`logistics_id`, `route_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流路由节点';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_media_asset` (
  `media_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '媒体唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `order_id` BIGINT DEFAULT NULL COMMENT '订单唯一编号',
  `sn_id` BIGINT DEFAULT NULL COMMENT 'SN唯一编号',
  `unit_id` BIGINT DEFAULT NULL COMMENT '库存单元唯一编号',
  `file_id` BIGINT DEFAULT NULL COMMENT '文件服务唯一编号',
  `media_type` TINYINT NOT NULL COMMENT '媒体类型：1发货视频 2收货视频 3开箱视频 4检查照片 5商品照片',
  `file_key` VARCHAR(255) DEFAULT NULL COMMENT '文件Key',
  `file_name` VARCHAR(255) DEFAULT NULL COMMENT '文件名',
  `file_size` BIGINT DEFAULT NULL COMMENT '文件大小',
  `mime_type` VARCHAR(128) DEFAULT NULL COMMENT 'MIME类型',
  `detected_sn` VARCHAR(128) DEFAULT NULL COMMENT '识别SN',
  `match_status` TINYINT NOT NULL DEFAULT 0 COMMENT '匹配状态：0待匹配 1已匹配 2未匹配',
  `uploaded_by_type` TINYINT NOT NULL DEFAULT 1 COMMENT '上传方：1员工 2客户 3系统',
  `retention_until` DATETIME DEFAULT NULL COMMENT '保留到期时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`media_id`),
  KEY `idx_rental_media_order` (`order_id`, `media_type`),
  KEY `idx_rental_media_sn` (`sn_id`),
  KEY `idx_rental_media_unit` (`tenant_id`, `member_id`, `unit_id`),
  KEY `idx_rental_media_match_scope` (`tenant_id`, `member_id`, `media_type`, `match_status`, `create_time`),
  KEY `idx_rental_media_detected_sn` (`tenant_id`, `member_id`, `detected_sn`, `match_status`),
  KEY `idx_rental_media_retention` (`retention_until`),
  KEY `idx_rental_media_tenant` (`tenant_id`, `member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='媒体文件业务索引';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_after_sales_case` (
  `case_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '售后唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `order_id` BIGINT NOT NULL COMMENT '订单唯一编号',
  `case_no` VARCHAR(64) NOT NULL COMMENT '售后单号',
  `case_type` TINYINT NOT NULL COMMENT '售后类型：1损坏 2丢失 3赔付 9其他',
  `case_status` TINYINT NOT NULL DEFAULT 0 COMMENT '售后状态：0处理中 1已完成 2已关闭',
  `problem_desc` VARCHAR(1000) DEFAULT NULL COMMENT '问题描述',
  `lost_items_json` JSON COMMENT '丢失配件',
  `compensation_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '赔付金额',
  `platform_ticket_no` VARCHAR(64) DEFAULT NULL COMMENT '关联平台客服工单编码',
  `customer_contacted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否联系客户',
  `progress_json` JSON COMMENT '进度记录',
  `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`case_id`),
  UNIQUE KEY `uk_rental_after_sales_no` (`member_id`, `case_no`),
  KEY `idx_rental_after_sales_order` (`order_id`),
  KEY `idx_rental_after_sales_status` (`member_id`, `case_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后单';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_order_return_inspection` (
  `inspection_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '归还验机记录唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `order_id` BIGINT NOT NULL COMMENT '订单唯一编号',
  `inspection_result` TINYINT NOT NULL COMMENT '验机结果：1验机正常 2转售后',
  `case_type` TINYINT DEFAULT NULL COMMENT '售后类型：1损坏 2丢失 3赔付 9其他',
  `problem_desc` VARCHAR(1000) DEFAULT NULL COMMENT '验机问题描述',
  `lost_items_json` JSON DEFAULT NULL COMMENT '缺失物品明细',
  `compensation_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '验机发现的赔付金额',
  `customer_contacted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已联系客户：0否 1是',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '验机备注',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '验机时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`inspection_id`),
  KEY `idx_rental_return_inspection_order` (`tenant_id`, `member_id`, `order_id`, `create_time`, `inspection_id`),
  KEY `idx_rental_return_inspection_result` (`tenant_id`, `member_id`, `inspection_result`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单归还验机记录';

-- ----------------------------
-- 7. 财务、套餐、推广
-- ----------------------------

CREATE TABLE IF NOT EXISTS `ry_rental_finance_ledger` (
  `ledger_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '财务流水唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT DEFAULT NULL COMMENT '商户唯一编号',
  `account_type` TINYINT NOT NULL DEFAULT 1 COMMENT '账户类型：1增值服务余额 2履约保证金 3租金/分账资金',
  `ledger_no` VARCHAR(64) NOT NULL COMMENT '流水号',
  `direction` TINYINT NOT NULL COMMENT '方向：1收入 2支出 3冻结 4解冻 5冲正',
  `biz_type` VARCHAR(64) NOT NULL COMMENT '业务类型',
  `amount` DECIMAL(18,2) NOT NULL COMMENT '金额',
  `balance_after` DECIMAL(18,2) DEFAULT NULL COMMENT '变更后余额',
  `ref_type` VARCHAR(64) DEFAULT NULL COMMENT '关联类型',
  `ref_id` BIGINT DEFAULT NULL COMMENT '关联唯一编号',
  `pay_channel` VARCHAR(64) DEFAULT NULL COMMENT '支付渠道',
  `pay_order_no` VARCHAR(128) DEFAULT NULL COMMENT '支付单号',
  `invoice_status` TINYINT DEFAULT NULL COMMENT '发票状态：0待开具 1已开具 2已驳回 3已红冲',
  `ledger_status` TINYINT NOT NULL DEFAULT 1 COMMENT '流水状态：0处理中 1成功 2失败 3已关闭',
  `original_ledger_id` BIGINT DEFAULT NULL COMMENT '原流水唯一编号',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`ledger_id`),
  UNIQUE KEY `uk_rental_finance_ledger_no` (`ledger_no`),
  KEY `idx_rental_finance_member_time` (`member_id`, `create_time`),
  KEY `idx_rental_finance_account_time` (`tenant_id`, `member_id`, `account_type`, `create_time`),
  KEY `idx_rental_finance_ref` (`ref_type`, `ref_id`),
  KEY `idx_rental_finance_invoice` (`invoice_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务不可变流水';

CREATE TABLE IF NOT EXISTS `ry_rental_invoice_application` (
  `invoice_application_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '开票申请唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `ledger_id` BIGINT NOT NULL COMMENT '财务流水唯一编号',
  `application_no` VARCHAR(64) NOT NULL COMMENT '开票申请编码',
  `idempotency_key` VARCHAR(128) NOT NULL COMMENT '请求幂等标识',
  `application_type` TINYINT NOT NULL DEFAULT 1 COMMENT '申请类型：1开票 2补票 3红冲',
  `application_status` TINYINT NOT NULL DEFAULT 0 COMMENT '申请状态：0待审核 1已驳回 2待开具 3已开具 4已红冲 9已取消',
  `invoice_type` TINYINT NOT NULL DEFAULT 1 COMMENT '发票类型：1企业 2个人',
  `invoice_title` VARCHAR(256) NOT NULL COMMENT '发票抬头',
  `tax_no` VARCHAR(64) DEFAULT NULL COMMENT '纳税人识别号',
  `invoice_email` VARCHAR(128) DEFAULT NULL COMMENT '接收发票邮箱',
  `amount` DECIMAL(18,2) NOT NULL COMMENT '申请开票金额',
  `ref_type` VARCHAR(64) DEFAULT NULL COMMENT '业务关联类型快照',
  `ref_id` BIGINT DEFAULT NULL COMMENT '业务关联唯一编号快照',
  `original_application_id` BIGINT DEFAULT NULL COMMENT '原开票申请唯一编号',
  `original_invoice_record_id` BIGINT DEFAULT NULL COMMENT '原发票记录唯一编号',
  `review_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
  `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核者用户唯一编号',
  `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`invoice_application_id`),
  UNIQUE KEY `uk_rental_invoice_application_no` (`application_no`),
  UNIQUE KEY `uk_rental_invoice_application_request` (`tenant_id`, `member_id`, `idempotency_key`, `is_delete`),
  KEY `idx_rental_invoice_application_member_status` (`tenant_id`, `member_id`, `application_status`, `create_time`),
  KEY `idx_rental_invoice_application_ledger` (`tenant_id`, `member_id`, `ledger_id`, `is_delete`),
  KEY `idx_rental_invoice_application_ref` (`tenant_id`, `member_id`, `ref_type`, `ref_id`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务开票申请';

CREATE TABLE IF NOT EXISTS `ry_rental_invoice_record` (
  `invoice_record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '发票记录唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `invoice_application_id` BIGINT NOT NULL COMMENT '开票申请唯一编号',
  `ledger_id` BIGINT NOT NULL COMMENT '财务流水唯一编号',
  `invoice_no` VARCHAR(128) NOT NULL COMMENT '发票号码',
  `invoice_code` VARCHAR(64) DEFAULT NULL COMMENT '发票代码',
  `file_url` VARCHAR(500) DEFAULT NULL COMMENT '发票文件地址',
  `record_status` TINYINT NOT NULL DEFAULT 1 COMMENT '发票状态：1有效 2已红冲',
  `issued_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开具时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`invoice_record_id`),
  UNIQUE KEY `uk_rental_invoice_record_application` (`invoice_application_id`, `is_delete`),
  UNIQUE KEY `uk_rental_invoice_record_no` (`tenant_id`, `invoice_no`, `is_delete`),
  KEY `idx_rental_invoice_record_ref` (`tenant_id`, `member_id`, `ledger_id`, `record_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='已开具发票记录';

CREATE TABLE IF NOT EXISTS `ry_rental_invoice_red_flush` (
  `red_flush_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '红冲记录唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `original_invoice_record_id` BIGINT NOT NULL COMMENT '原发票记录唯一编号',
  `invoice_application_id` BIGINT NOT NULL COMMENT '红冲申请唯一编号',
  `red_flush_no` VARCHAR(128) DEFAULT NULL COMMENT '红冲发票号码',
  `reason` VARCHAR(500) NOT NULL COMMENT '红冲原因',
  `red_flush_status` TINYINT NOT NULL DEFAULT 1 COMMENT '红冲状态：1已完成 2已驳回',
  `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核者用户唯一编号',
  `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`red_flush_id`),
  UNIQUE KEY `uk_rental_invoice_red_flush_application` (`invoice_application_id`, `is_delete`),
  KEY `idx_rental_invoice_red_flush_original` (`tenant_id`, `original_invoice_record_id`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票红冲记录';

CREATE TABLE IF NOT EXISTS `ry_rental_service_subscription` (
  `subscription_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订阅唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `service_code` VARCHAR(64) NOT NULL COMMENT '服务编码',
  `service_name` VARCHAR(128) NOT NULL COMMENT '服务名称',
  `plan_code` VARCHAR(64) DEFAULT NULL COMMENT '套餐编码',
  `billing_cycle` VARCHAR(32) DEFAULT NULL COMMENT '计费周期',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `quota_json` JSON COMMENT '权益额度',
  `subscription_status` TINYINT NOT NULL DEFAULT 1 COMMENT '订阅状态：1生效 2已过期 3已取消',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`subscription_id`),
  KEY `idx_rental_subscription_member` (`member_id`, `service_code`, `subscription_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='增值服务订阅';

CREATE TABLE IF NOT EXISTS `ry_rental_promotion_invite` (
  `invite_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '邀请唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `promoter_member_id` BIGINT NOT NULL COMMENT '推广员商户唯一编号',
  `invitee_member_id` BIGINT DEFAULT NULL COMMENT '被邀请商户唯一编号',
  `invite_code` VARCHAR(64) NOT NULL COMMENT '推广码',
  `active_hours` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '有效在线小时',
  `normal_order_count` INT NOT NULL DEFAULT 0 COMMENT '正常订单数',
  `effective_status` TINYINT NOT NULL DEFAULT 0 COMMENT '有效状态：0待生效 1已生效 2失效',
  `reward_amount` DECIMAL(18,2) DEFAULT NULL COMMENT '奖励金额',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `effective_time` DATETIME DEFAULT NULL COMMENT '成为有效用户时间',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`invite_id`),
  UNIQUE KEY `uk_rental_promotion_invitee` (`promoter_member_id`, `invitee_member_id`),
  KEY `idx_rental_promotion_code` (`invite_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推广邀请';

-- ----------------------------
-- 8. 多仓联盟
-- ----------------------------

CREATE TABLE IF NOT EXISTS `ry_rental_alliance` (
  `alliance_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '联盟唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `alliance_name` VARCHAR(128) NOT NULL COMMENT '联盟名称',
  `owner_member_id` BIGINT NOT NULL COMMENT '盟主商户唯一编号',
  `alliance_status` TINYINT NOT NULL DEFAULT 1 COMMENT '联盟状态：1正常 2停用',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`alliance_id`),
  KEY `idx_rental_alliance_owner` (`owner_member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多仓联盟';

CREATE TABLE IF NOT EXISTS `ry_rental_alliance_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `alliance_id` BIGINT NOT NULL COMMENT '联盟唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '成员商户唯一编号',
  `accept_order_limit` INT DEFAULT NULL COMMENT '可承接订单上限',
  `share_stock_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否共享库存',
  `member_status` TINYINT NOT NULL DEFAULT 1 COMMENT '成员状态：1正常 2停用 3退出',
  `join_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `exit_time` DATETIME DEFAULT NULL COMMENT '退出时间',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rental_alliance_member` (`alliance_id`, `member_id`),
  KEY `idx_rental_alliance_member_member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='联盟成员';

CREATE TABLE IF NOT EXISTS `ry_rental_compensation_rule` (
  `rule_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '赔付规则模板唯一编号',
  `rule_name` VARCHAR(128) NOT NULL COMMENT '规则名称',
  `dispute_type` TINYINT NOT NULL COMMENT '适用纠纷类型：0通用 1损坏 2丢失 3延迟履约 4费用 9其他',
  `max_amount` DECIMAL(18,2) NOT NULL COMMENT '单笔赔付金额上限',
  `priority` INT NOT NULL DEFAULT 100 COMMENT '匹配优先级，数值越小越优先',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0启用 1停用',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remove_by` BIGINT DEFAULT NULL COMMENT '删除者用户唯一编号',
  `remove_time` DATETIME DEFAULT NULL COMMENT '删除时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`rule_id`),
  KEY `idx_compensation_rule_match` (`dispute_type`, `status`, `priority`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台联盟赔付规则模板';

-- ----------------------------
-- 9. 库存周转市场
-- ----------------------------

CREATE TABLE IF NOT EXISTS `ry_rental_market_post` (
  `post_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '发布商户唯一编号',
  `board_type` TINYINT NOT NULL COMMENT '板块：1出售 2出租 3求购',
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
  `post_status` TINYINT NOT NULL DEFAULT 0 COMMENT '帖子状态：0草稿 1已发布 2已关闭 3已下架',
  `down_rank_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否降权',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`post_id`),
  UNIQUE KEY `uk_rental_market_post_no` (`member_id`, `post_no`),
  KEY `idx_rental_market_list` (`board_type`, `post_status`, `city`, `recent_30d_turnover`, `idle_qty`, `create_time`),
  KEY `idx_rental_market_member` (`member_id`, `post_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存周转市场帖子';

CREATE TABLE IF NOT EXISTS `ry_rental_market_post_inventory` (
  `post_inventory_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '市场帖子库存绑定唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '发布商户唯一编号',
  `post_id` BIGINT NOT NULL COMMENT '市场帖子唯一编号',
  `unit_id` BIGINT NOT NULL COMMENT '执行面库存单元唯一编号',
  `unit_code` VARCHAR(128) NOT NULL COMMENT '库存编码快照',
  `serial_number` VARCHAR(128) NOT NULL COMMENT '实物序列号快照',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`post_inventory_id`),
  UNIQUE KEY `uk_rental_market_post_inventory` (`post_id`, `unit_id`, `is_delete`),
  KEY `idx_rental_market_post_inventory_member` (`tenant_id`, `member_id`, `post_id`, `is_delete`),
  KEY `idx_rental_market_post_inventory_unit` (`unit_id`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='周转市场帖子实物库存绑定';

CREATE TABLE IF NOT EXISTS `ry_rental_market_message` (
  `message_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `post_id` BIGINT NOT NULL COMMENT '帖子唯一编号',
  `from_member_id` BIGINT NOT NULL COMMENT '发送商户',
  `to_member_id` BIGINT NOT NULL COMMENT '接收商户',
  `message_content` VARCHAR(1000) NOT NULL COMMENT '消息内容',
  `read_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读',
  `risk_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '风险标记',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`message_id`),
  KEY `idx_rental_market_msg_post` (`post_id`, `create_time`),
  KEY `idx_rental_market_msg_to` (`to_member_id`, `read_flag`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='市场站内私信';

CREATE TABLE IF NOT EXISTS `ry_rental_market_report` (
  `report_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '举报唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `post_id` BIGINT NOT NULL COMMENT '帖子唯一编号',
  `reporter_member_id` BIGINT NOT NULL COMMENT '举报商户',
  `reported_member_id` BIGINT NOT NULL COMMENT '被举报商户',
  `report_type` VARCHAR(64) NOT NULL COMMENT '举报类型',
  `evidence_json` JSON COMMENT '证据',
  `report_status` TINYINT NOT NULL DEFAULT 0 COMMENT '处理状态：0待处理 1已处理 2已驳回',
  `review_result` VARCHAR(1000) DEFAULT NULL COMMENT '处理结果',
  `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人',
  `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`report_id`),
  KEY `idx_rental_market_report_post` (`post_id`, `report_status`),
  KEY `idx_rental_market_report_member` (`reported_member_id`, `report_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='市场举报';

CREATE TABLE IF NOT EXISTS `ry_rental_market_deposit_ledger` (
  `deposit_ledger_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '保证金流水唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `amount` DECIMAL(18,2) NOT NULL COMMENT '金额',
  `direction` TINYINT NOT NULL COMMENT '方向：1缴纳 2扣除 3退回',
  `ref_type` VARCHAR(64) DEFAULT NULL COMMENT '关联类型',
  `ref_id` BIGINT DEFAULT NULL COMMENT '关联唯一编号',
  `ledger_status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0处理中 1成功 2失败 3已关闭',
  `idempotency_key` VARCHAR(128) DEFAULT NULL COMMENT '市场保证金操作幂等键',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`deposit_ledger_id`),
  UNIQUE KEY `uk_rental_market_deposit_idempotency` (`member_id`, `idempotency_key`, `is_delete`),
  KEY `idx_rental_market_deposit_member` (`member_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='市场保证金流水';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_protocol` (
  `protocol_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '协议唯一编号',
  `protocol_key` VARCHAR(64) NOT NULL COMMENT '协议业务键',
  `version_no` INT NOT NULL COMMENT '协议版本号',
  `protocol_title` VARCHAR(200) NOT NULL COMMENT '协议标题',
  `protocol_content` LONGTEXT NOT NULL COMMENT '协议正文',
  `content_hash` CHAR(64) NOT NULL COMMENT '正文哈希',
  `protocol_status` TINYINT NOT NULL DEFAULT 0 COMMENT '协议状态：0草稿 1已发布 2已退役',
  `effective_time` DATETIME DEFAULT NULL COMMENT '生效时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`protocol_id`),
  UNIQUE KEY `uk_rental_protocol_version` (`protocol_key`, `version_no`, `is_delete`),
  KEY `idx_rental_protocol_status` (`protocol_key`, `protocol_status`, `effective_time`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台协议版本';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_protocol_scope` (
  `scope_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '协议入口关系唯一编号',
  `protocol_id` BIGINT NOT NULL COMMENT '协议唯一编号',
  `scope_type` TINYINT NOT NULL COMMENT '适用入口：1商户网页端 2访客网页端 3小程序',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`scope_id`),
  UNIQUE KEY `uk_rental_protocol_scope` (`protocol_id`, `scope_type`, `is_delete`),
  KEY `idx_rental_protocol_scope_type` (`scope_type`, `protocol_id`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协议适用入口';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_protocol_acceptance` (
  `acceptance_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '协议确认唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `merchant_user_id` BIGINT NOT NULL COMMENT '商户用户唯一编号',
  `protocol_id` BIGINT NOT NULL COMMENT '协议唯一编号',
  `protocol_key` VARCHAR(64) NOT NULL COMMENT '协议业务键快照',
  `version_no` INT NOT NULL COMMENT '确认的版本号',
  `content_hash` CHAR(64) NOT NULL COMMENT '确认时正文哈希',
  `accepted_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '确认时间',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '确认人',
  `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`acceptance_id`),
  UNIQUE KEY `uk_rental_protocol_acceptance` (`member_id`, `merchant_user_id`, `protocol_id`, `version_no`, `is_delete`),
  KEY `idx_rental_protocol_acceptance_scope` (`tenant_id`, `member_id`, `accepted_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户用户协议确认记录';

-- ----------------------------
-- 10. 小程序、转发、免押
-- ----------------------------

CREATE TABLE IF NOT EXISTS `ry_rental_miniapp_auth` (
  `auth_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '授权唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT DEFAULT NULL COMMENT '商户唯一编号',
  `openid` VARCHAR(128) NOT NULL COMMENT 'openid',
  `unionid` VARCHAR(128) DEFAULT NULL COMMENT 'unionid',
  `phone` VARCHAR(32) DEFAULT NULL COMMENT '授权手机号',
  `auth_scene` VARCHAR(64) DEFAULT NULL COMMENT '授权场景',
  `consent_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否勾选协议',
  `last_auth_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近授权时间',
  `expire_time` DATETIME DEFAULT NULL COMMENT '会话过期时间',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`auth_id`),
  KEY `idx_rental_miniapp_openid` (`openid`),
  KEY `idx_rental_miniapp_phone` (`member_id`, `phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小程序授权记录';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_share_log` (
  `share_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '转发唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `order_id` BIGINT DEFAULT NULL COMMENT '订单唯一编号',
  `share_type` VARCHAR(64) NOT NULL COMMENT '转发类型',
  `share_target` VARCHAR(255) DEFAULT NULL COMMENT '转发目标',
  `template_id` BIGINT DEFAULT NULL COMMENT '模板唯一编号',
  `snapshot_json` JSON COMMENT '转发快照',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`share_id`),
  KEY `idx_rental_share_member_time` (`member_id`, `create_time`),
  KEY `idx_rental_share_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单转发记录';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_deposit_free_code` (
  `code_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '免押码唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `order_id` BIGINT DEFAULT NULL COMMENT '订单唯一编号',
  `customer_id` BIGINT DEFAULT NULL COMMENT '客户唯一编号',
  `deposit_free_code` VARCHAR(128) NOT NULL COMMENT '免押码',
  `code_status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0已创建 1已申请 2成功 3已过期 4失败 5已关闭',
  `agreement_id` VARCHAR(128) DEFAULT NULL COMMENT '协议唯一编号',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
  `success_time` DATETIME DEFAULT NULL COMMENT '免押成功时间',
  `raw_callback` JSON COMMENT '回调原文',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`code_id`),
  UNIQUE KEY `uk_rental_deposit_free_code` (`deposit_free_code`),
  KEY `idx_rental_deposit_free_order` (`order_id`),
  KEY `idx_rental_deposit_free_member` (`member_id`, `code_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='免押码';

-- ----------------------------
-- 11. 幂等、审计、风控
-- ----------------------------

/* 历史执行面表已迁入 ry_business；新环境不再创建。 */
/* CREATE TABLE IF NOT EXISTS `ry_rental_idempotent_request` (
  `request_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '幂等请求唯一编号',
  `idempotent_key` VARCHAR(128) NOT NULL COMMENT '幂等Key',
  `tenant_id` BIGINT DEFAULT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT DEFAULT NULL COMMENT '商户唯一编号',
  `request_path` VARCHAR(255) NOT NULL COMMENT '请求路径',
  `request_hash` VARCHAR(128) DEFAULT NULL COMMENT '请求哈希',
  `response_json` JSON COMMENT '响应快照',
  `request_status` VARCHAR(32) NOT NULL DEFAULT 'processing' COMMENT '状态',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`request_id`),
  UNIQUE KEY `uk_rental_idempotent_key` (`idempotent_key`),
  KEY `idx_rental_idempotent_expire` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接口幂等记录'; */

CREATE TABLE IF NOT EXISTS `ry_rental_audit_log` (
  `audit_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '审计唯一编号',
  `tenant_id` BIGINT DEFAULT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT DEFAULT NULL COMMENT '商户唯一编号',
  `actor_id` BIGINT DEFAULT NULL COMMENT '操作人唯一编号',
  `actor_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人',
  `actor_type` TINYINT DEFAULT NULL COMMENT '操作人类型：1商户用户 2平台用户 3系统',
  `action_code` VARCHAR(128) NOT NULL COMMENT '动作编码',
  `business_module` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '业务模块',
  `operation_type` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '业务操作类型',
  `object_type` VARCHAR(64) NOT NULL COMMENT '对象类型',
  `object_id` VARCHAR(64) DEFAULT NULL COMMENT '对象唯一编号',
  `target_name` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '业务对象名称',
  `description` VARCHAR(1000) NOT NULL DEFAULT '' COMMENT '业务操作描述',
  `before_json` JSON COMMENT '变更前',
  `after_json` JSON COMMENT '变更后',
  `before_display_json` JSON DEFAULT NULL COMMENT '商户展示用变更前快照（不含技术编号）',
  `after_display_json` JSON DEFAULT NULL COMMENT '商户展示用变更后快照（不含技术编号）',
  `sensitive_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否敏感操作',
  `client_ip` VARCHAR(64) DEFAULT NULL COMMENT 'IP',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '操作状态：0成功 1失败',
  `error_message` VARCHAR(2000) NOT NULL DEFAULT '' COMMENT '失败原因',
  `trace_id` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '调用链跟踪号',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT 'UA',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',

  PRIMARY KEY (`audit_id`),
  KEY `idx_rental_audit_actor` (`actor_id`, `create_time`),
  KEY `idx_rental_audit_object` (`object_type`, `object_id`),
  KEY `idx_rental_audit_tenant` (`tenant_id`, `member_id`, `create_time`),
  KEY `idx_rental_audit_business_scope_time` (`tenant_id`, `member_id`, `business_module`, `operation_type`, `create_time`, `is_delete`),
  KEY `idx_rental_audit_sensitive` (`sensitive_flag`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁域统一业务审计日志（商户业务日志页面事实来源）';

CREATE TABLE IF NOT EXISTS `ry_rental_knowledge_article_role` (
  `article_id` BIGINT NOT NULL COMMENT '知识文章唯一编号',
  `tenant_id` BIGINT NOT NULL COMMENT '租户唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `role_id` BIGINT NOT NULL COMMENT '商户员工组角色唯一编号',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remove_by` BIGINT DEFAULT NULL COMMENT '删除者用户唯一编号',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`article_id`, `role_id`),
  KEY `idx_rental_knowledge_article_role_scope` (`tenant_id`, `member_id`, `role_id`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户知识文章员工组受众';

CREATE TABLE IF NOT EXISTS `ry_rental_knowledge_version_attachment` (
  `attachment_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '知识附件唯一编号',
  `version_id` BIGINT NOT NULL COMMENT '知识版本唯一编号',
  `attachment_name` VARCHAR(255) NOT NULL COMMENT '附件名称',
  `file_url` VARCHAR(1000) NOT NULL COMMENT '附件地址',
  `file_type` VARCHAR(64) DEFAULT NULL COMMENT '附件类型',
  `file_size` BIGINT DEFAULT NULL COMMENT '附件大小（字节）',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remove_by` BIGINT DEFAULT NULL COMMENT '删除者用户唯一编号',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`attachment_id`),
  UNIQUE KEY `uk_rental_knowledge_version_attachment` (`version_id`, `file_url`(191), `is_delete`),
  KEY `idx_rental_knowledge_version_attachment_version` (`version_id`, `sort_no`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识文章版本附件';

CREATE TABLE IF NOT EXISTS `ry_rental_knowledge_violation` (
  `violation_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '知识违规处置唯一编号',
  `article_id` BIGINT NOT NULL COMMENT '知识文章唯一编号',
  `member_id` BIGINT NOT NULL COMMENT '商户唯一编号',
  `action_type` TINYINT NOT NULL COMMENT '处置动作：1违规下架',
  `reason` VARCHAR(500) NOT NULL COMMENT '违规原因',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建者用户唯一编号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新者用户唯一编号',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remove_by` BIGINT DEFAULT NULL COMMENT '删除者用户唯一编号',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`violation_id`),
  KEY `idx_rental_knowledge_violation_article` (`article_id`, `create_time`, `is_delete`),
  KEY `idx_rental_knowledge_violation_member` (`member_id`, `create_time`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户知识违规处置记录';

SET FOREIGN_KEY_CHECKS = 1;
