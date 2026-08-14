-- 将订单列表所需的核心业务表迁入 rental-business 独立数据库。
--
-- 说明：
-- 1. rental-business 只拥有 ry_business，后续订单写入和查询必须使用本库。
-- 2. 订单域直接创建最终结构；订单渠道和审计人一律使用 BIGINT 主键关系。
-- 3. 复制完成后，商户身份、角色和菜单仍由 merchant-service 维护在 ry_rental。

CREATE DATABASE IF NOT EXISTS `ry_business`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `ry_business`;

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_warehouse` (
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
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`warehouse_id`),
  UNIQUE KEY `uk_rental_warehouse_code` (`member_id`, `warehouse_code`),
  KEY `idx_rental_warehouse_tenant_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_warehouse_city` (`city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_product` (
  `product_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `class_id` BIGINT DEFAULT NULL COMMENT '平台产品类目ID',
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
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态',
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`product_id`),
  UNIQUE KEY `uk_rental_product_code` (`member_id`, `product_code`),
  KEY `idx_rental_product_tenant_member` (`tenant_id`, `member_id`),
  KEY `idx_rental_product_member_class` (`member_id`, `class_id`, `status`, `is_delete`),
  KEY `idx_rental_product_model` (`model_id`),
  KEY `idx_rental_product_type` (`product_type`, `stock_mode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_customer` (
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
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`customer_id`),
  KEY `idx_rental_customer_member_phone` (`member_id`, `phone`),
  KEY `idx_rental_customer_tenant` (`tenant_id`, `member_id`),
  KEY `idx_rental_customer_openid` (`wechat_openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁客户';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_order` (
  `order_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '系统订单号',
  `external_order_no` VARCHAR(128) DEFAULT NULL COMMENT '外部订单号',
  `channel_id` BIGINT NOT NULL COMMENT '订单渠道ID',
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
  `create_by` BIGINT DEFAULT NULL COMMENT '创建商户用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新商户用户ID',
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
  KEY `idx_rental_order_channel_id` (`tenant_id`, `member_id`, `channel_id`, `is_delete`),
  KEY `idx_rental_order_external` (`channel_id`, `external_order_no`),
  KEY `idx_order_create_by` (`create_by`),
  KEY `idx_order_update_by` (`update_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租赁订单';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_order_item` (
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
  `create_by` BIGINT DEFAULT NULL COMMENT '创建商户用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新商户用户ID',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`order_item_id`),
  KEY `idx_rental_order_item_order` (`order_id`),
  KEY `idx_rental_order_item_sn` (`sn_id`),
  KEY `idx_rental_order_item_product` (`product_id`),
  KEY `idx_order_item_create_by` (`create_by`),
  KEY `idx_order_item_update_by` (`update_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品明细';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_package_template` (
  `package_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '套餐ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `platform_code` VARCHAR(64) DEFAULT NULL COMMENT '适用平台/渠道',
  `package_code` VARCHAR(64) NOT NULL COMMENT '套餐编码',
  `package_name` VARCHAR(128) NOT NULL COMMENT '套餐名称',
  `main_product_id` BIGINT DEFAULT NULL COMMENT '主商品ID',
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建商户用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新商户用户ID',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`package_id`),
  UNIQUE KEY `uk_rental_package_code` (`member_id`, `package_code`),
  KEY `idx_rental_package_tenant_member` (`tenant_id`, `member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐模板';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_package_item` (
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
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_rental_package_item_package` (`package_id`),
  KEY `idx_rental_package_item_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐物料明细';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_order_note` (
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
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`note_id`),
  KEY `idx_rental_order_note_order` (`order_id`, `pinned_flag`),
  KEY `idx_order_note_create_by` (`create_by`),
  KEY `idx_order_note_update_by` (`update_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单批注';

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_logistics_order` (
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
  `create_by` BIGINT DEFAULT NULL COMMENT '创建商户用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` BIGINT DEFAULT NULL COMMENT '更新商户用户ID',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`logistics_id`),
  UNIQUE KEY `uk_rental_tracking_no` (`carrier`, `tracking_no`),
  KEY `idx_rental_logistics_order` (`order_id`, `direction`),
  KEY `idx_rental_logistics_member_status` (`member_id`, `route_status`),
  KEY `idx_rental_logistics_pickup` (`pickup_time`),
  KEY `idx_logistics_order_create_by` (`create_by`),
  KEY `idx_logistics_order_update_by` (`update_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流单';

-- 旧环境升级时只在源表存在的情况下复制数据；新环境没有旧源表时直接跳过。
DELIMITER $$

DROP PROCEDURE IF EXISTS `qianruo_copy_order_business_table`$$
CREATE PROCEDURE `qianruo_copy_order_business_table`(IN table_name_value VARCHAR(128))
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'ry_rental'
          AND table_name = table_name_value
          AND table_type = 'BASE TABLE'
    ) THEN
        SET @qianruo_copy_order_sql = CONCAT(
            'INSERT IGNORE INTO `ry_business`.`', table_name_value,
            '` SELECT * FROM `ry_rental`.`', table_name_value, '`'
        );
        PREPARE qianruo_copy_order_statement FROM @qianruo_copy_order_sql;
        EXECUTE qianruo_copy_order_statement;
        DEALLOCATE PREPARE qianruo_copy_order_statement;
    END IF;
END$$

CALL `qianruo_copy_order_business_table`('ry_rental_warehouse')$$
CALL `qianruo_copy_order_business_table`('ry_rental_product')$$
CALL `qianruo_copy_order_business_table`('ry_rental_customer')$$
CALL `qianruo_copy_order_business_table`('ry_rental_package_template')$$
CALL `qianruo_copy_order_business_table`('ry_rental_package_item')$$
DROP PROCEDURE IF EXISTS `qianruo_copy_order_business_table`$$

DELIMITER ;
