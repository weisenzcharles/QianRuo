-- 产品类目、商户经营类目、通用审计字段和菜单权限。

USE `ry-cloud`;

CREATE TABLE IF NOT EXISTS `ry_rental`.`ry_rental_product_class` (
  `class_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '产品类目ID',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父类目ID，根节点为0',
  `ancestors` VARCHAR(500) NOT NULL DEFAULT '0' COMMENT '祖先类目路径',
  `class_code` VARCHAR(64) NOT NULL COMMENT '类目编码',
  `class_name` VARCHAR(128) NOT NULL COMMENT '类目名称',
  `class_level` INT NOT NULL DEFAULT 1 COMMENT '类目层级，根节点从1开始',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '同级显示顺序',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态：0正常 1停用',
  `create_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  `active_class_code` VARCHAR(64) GENERATED ALWAYS AS (
    CASE WHEN `is_delete` = 0 THEN `class_code` ELSE NULL END
  ) STORED COMMENT '有效类目编码唯一键',
  `active_class_name` VARCHAR(128) GENERATED ALWAYS AS (
    CASE WHEN `is_delete` = 0 THEN `class_name` ELSE NULL END
  ) STORED COMMENT '有效同级类目名称唯一键',
  PRIMARY KEY (`class_id`),
  UNIQUE KEY `uk_rental_product_class_active_code` (`active_class_code`),
  UNIQUE KEY `uk_rental_product_class_active_parent_name` (`parent_id`, `active_class_name`),
  KEY `idx_rental_product_class_parent` (`parent_id`, `status`, `is_delete`),
  KEY `idx_rental_product_class_ancestors` (`ancestors`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台产品类目';

CREATE TABLE IF NOT EXISTS `ry_rental`.`ry_rental_member_product_class` (
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `member_id` BIGINT NOT NULL COMMENT '商户ID',
  `class_id` BIGINT NOT NULL COMMENT '平台产品类目ID',
  `create_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
  PRIMARY KEY (`tenant_id`, `member_id`, `class_id`),
  KEY `idx_rental_member_class_member` (`member_id`, `is_delete`),
  KEY `idx_rental_member_class_class` (`class_id`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户经营产品类目';

DELIMITER $$

DROP PROCEDURE IF EXISTS `qianruo_add_column_if_missing`$$
CREATE PROCEDURE `qianruo_add_column_if_missing`(
    IN schema_name_value VARCHAR(64),
    IN table_name_value VARCHAR(64),
    IN column_name_value VARCHAR(64),
    IN column_definition_value VARCHAR(1000)
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = schema_name_value
          AND table_name = table_name_value
          AND table_type = 'BASE TABLE'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = schema_name_value
          AND table_name = table_name_value
          AND column_name = column_name_value
    ) THEN
        SET @add_column_sql = CONCAT(
            'ALTER TABLE `', schema_name_value, '`.`', table_name_value,
            '` ADD COLUMN `', column_name_value, '` ', column_definition_value
        );
        PREPARE add_column_statement FROM @add_column_sql;
        EXECUTE add_column_statement;
        DEALLOCATE PREPARE add_column_statement;
    END IF;
END$$

DROP PROCEDURE IF EXISTS `qianruo_add_index_if_missing`$$
CREATE PROCEDURE `qianruo_add_index_if_missing`(
    IN schema_name_value VARCHAR(64),
    IN table_name_value VARCHAR(64),
    IN index_name_value VARCHAR(64),
    IN index_definition_value VARCHAR(1000)
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = schema_name_value
          AND table_name = table_name_value
          AND table_type = 'BASE TABLE'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = schema_name_value
          AND table_name = table_name_value
          AND index_name = index_name_value
    ) THEN
        SET @add_index_sql = CONCAT(
            'ALTER TABLE `', schema_name_value, '`.`', table_name_value,
            '` ADD ', index_definition_value
        );
        PREPARE add_index_statement FROM @add_index_sql;
        EXECUTE add_index_statement;
        DEALLOCATE PREPARE add_index_statement;
    END IF;
END$$

DROP PROCEDURE IF EXISTS `qianruo_drop_index_if_exists`$$
CREATE PROCEDURE `qianruo_drop_index_if_exists`(
    IN schema_name_value VARCHAR(64),
    IN table_name_value VARCHAR(64),
    IN index_name_value VARCHAR(64)
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = schema_name_value
          AND table_name = table_name_value
          AND index_name = index_name_value
    ) THEN
        SET @drop_index_sql = CONCAT(
            'ALTER TABLE `', schema_name_value, '`.`', table_name_value,
            '` DROP INDEX `', index_name_value, '`'
        );
        PREPARE drop_index_statement FROM @drop_index_sql;
        EXECUTE drop_index_statement;
        DEALLOCATE PREPARE drop_index_statement;
    END IF;
END$$

CALL `qianruo_add_column_if_missing`(
    'ry_rental', 'ry_rental_product_class', 'active_class_code',
    'VARCHAR(64) GENERATED ALWAYS AS (CASE WHEN `is_delete` = 0 THEN `class_code` ELSE NULL END) STORED COMMENT ''有效类目编码唯一键'''
)$$
CALL `qianruo_add_column_if_missing`(
    'ry_rental', 'ry_rental_product_class', 'active_class_name',
    'VARCHAR(128) GENERATED ALWAYS AS (CASE WHEN `is_delete` = 0 THEN `class_name` ELSE NULL END) STORED COMMENT ''有效同级类目名称唯一键'''
)$$
CALL `qianruo_drop_index_if_exists`(
    'ry_rental', 'ry_rental_product_class', 'uk_rental_product_class_code'
)$$
CALL `qianruo_drop_index_if_exists`(
    'ry_rental', 'ry_rental_product_class', 'uk_rental_product_class_parent_name'
)$$
CALL `qianruo_add_index_if_missing`(
    'ry_rental', 'ry_rental_product_class', 'uk_rental_product_class_active_code',
    'UNIQUE INDEX `uk_rental_product_class_active_code` (`active_class_code`)'
)$$
CALL `qianruo_add_index_if_missing`(
    'ry_rental', 'ry_rental_product_class', 'uk_rental_product_class_active_parent_name',
    'UNIQUE INDEX `uk_rental_product_class_active_parent_name` (`parent_id`, `active_class_name`)'
)$$

CALL `qianruo_add_column_if_missing`(
    'ry_rental', 'ry_rental_brand_model', 'class_id',
    'BIGINT DEFAULT NULL COMMENT ''平台产品类目ID'' AFTER `model_id`'
)$$
CALL `qianruo_add_column_if_missing`(
    'ry_rental', 'ry_rental_product', 'class_id',
    'BIGINT DEFAULT NULL COMMENT ''平台产品类目ID'' AFTER `member_id`'
)$$

CALL `qianruo_add_column_if_missing`(
    'ry-cloud', 'sys_oper_log', 'operator_user_id',
    'BIGINT DEFAULT NULL COMMENT ''操作用户ID'' AFTER `oper_id`'
)$$
CALL `qianruo_add_column_if_missing`(
    'ry-cloud', 'sys_oper_log', 'client_type',
    'VARCHAR(20) NOT NULL DEFAULT ''platform'' COMMENT ''操作终端类型'' AFTER `operator_user_id`'
)$$
CALL `qianruo_add_column_if_missing`(
    'ry-cloud', 'sys_oper_log', 'tenant_id',
    'BIGINT DEFAULT NULL COMMENT ''租户ID'' AFTER `client_type`'
)$$
CALL `qianruo_add_column_if_missing`(
    'ry-cloud', 'sys_oper_log', 'member_id',
    'BIGINT DEFAULT NULL COMMENT ''商户ID'' AFTER `tenant_id`'
)$$
CALL `qianruo_add_column_if_missing`(
    'ry-cloud', 'sys_oper_log', 'trace_id',
    'VARCHAR(64) NOT NULL DEFAULT '''' COMMENT ''请求链路ID'' AFTER `member_id`'
)$$
CALL `qianruo_add_column_if_missing`(
    'ry-cloud', 'sys_oper_log', 'http_status',
    'INT DEFAULT NULL COMMENT ''HTTP响应状态码'' AFTER `trace_id`'
)$$

CALL `qianruo_add_column_if_missing`(
    'ry_rental', 'ry_rental_merchant_oper_log', 'trace_id',
    'VARCHAR(64) NOT NULL DEFAULT '''' COMMENT ''请求链路ID'' AFTER `cost_time`'
)$$
CALL `qianruo_add_column_if_missing`(
    'ry_rental', 'ry_rental_merchant_oper_log', 'http_status',
    'INT DEFAULT NULL COMMENT ''HTTP响应状态码'' AFTER `trace_id`'
)$$

CALL `qianruo_add_index_if_missing`(
    'ry_rental', 'ry_rental_brand_model', 'idx_rental_brand_model_class',
    'INDEX `idx_rental_brand_model_class` (`class_id`, `status`, `is_delete`)'
)$$
CALL `qianruo_add_index_if_missing`(
    'ry_rental', 'ry_rental_product', 'idx_rental_product_member_class',
    'INDEX `idx_rental_product_member_class` (`member_id`, `class_id`, `status`, `is_delete`)'
)$$
CALL `qianruo_add_index_if_missing`(
    'ry-cloud', 'sys_oper_log', 'idx_sys_oper_log_trace',
    'INDEX `idx_sys_oper_log_trace` (`trace_id`)'
)$$
CALL `qianruo_add_index_if_missing`(
    'ry-cloud', 'sys_oper_log', 'idx_sys_oper_log_client_time',
    'INDEX `idx_sys_oper_log_client_time` (`client_type`, `oper_time`, `is_delete`)'
)$$
CALL `qianruo_add_index_if_missing`(
    'ry_rental', 'ry_rental_merchant_oper_log', 'idx_merchant_oper_log_trace',
    'INDEX `idx_merchant_oper_log_trace` (`trace_id`)'
)$$

DROP PROCEDURE IF EXISTS `qianruo_add_index_if_missing`$$
DROP PROCEDURE IF EXISTS `qianruo_drop_index_if_exists`$$
DROP PROCEDURE IF EXISTS `qianruo_add_column_if_missing`$$

DELIMITER ;

ALTER TABLE `ry_rental`.`ry_rental_merchant_oper_log`
    MODIFY COLUMN `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
    MODIFY COLUMN `member_id` BIGINT DEFAULT NULL COMMENT '商户ID',
    MODIFY COLUMN `operator_user_id` BIGINT DEFAULT NULL COMMENT '商户用户ID';

INSERT INTO `ry-cloud`.`sys_menu`
(`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
 `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `client_type`, `icon`,
 `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_delete`)
VALUES
(2030, '产品类目', 2000, 6, 'product-class', 'rental/productClass/index', '', '', 1, 0, 'C', '0', '0', 'platform:class:list', 'platform', 'tree-table', 'migration', NOW(), 'migration', NOW(), '平台多级产品类目管理', 0),
(2031, '类目查询', 2030, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:class:query', 'platform', '#', 'migration', NOW(), 'migration', NOW(), '', 0),
(2032, '类目新增', 2030, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:class:add', 'platform', '#', 'migration', NOW(), 'migration', NOW(), '', 0),
(2033, '类目修改', 2030, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:class:edit', 'platform', '#', 'migration', NOW(), 'migration', NOW(), '', 0),
(2034, '类目删除', 2030, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'platform:class:remove', 'platform', '#', 'migration', NOW(), 'migration', NOW(), '', 0)
ON DUPLICATE KEY UPDATE
    `menu_name` = VALUES(`menu_name`), `parent_id` = VALUES(`parent_id`), `order_num` = VALUES(`order_num`),
    `path` = VALUES(`path`), `component` = VALUES(`component`), `menu_type` = VALUES(`menu_type`),
    `visible` = VALUES(`visible`), `status` = VALUES(`status`), `perms` = VALUES(`perms`),
    `client_type` = VALUES(`client_type`), `icon` = VALUES(`icon`), `update_by` = 'migration',
    `update_time` = NOW(), `remark` = VALUES(`remark`), `is_delete` = 0;

INSERT INTO `ry-cloud`.`sys_role_menu` (`role_id`, `menu_id`, `is_delete`)
SELECT 1, menu_id, 0
FROM `ry-cloud`.`sys_menu`
WHERE menu_id IN (2030, 2031, 2032, 2033, 2034)
ON DUPLICATE KEY UPDATE `is_delete` = 0;

INSERT INTO `ry_rental`.`ry_rental_merchant_menu`
(`menu_id`, `tenant_id`, `member_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`,
 `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
 `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_delete`)
VALUES
(2192, 0, 0, '经营类目', 0, 11, 'merchant-product-class', 'merchant/productClass/index', '', '', 1, 0, 'C', '0', '0', 'merchant:class:list', 'tree-table', 'migration', NOW(), 'migration', NOW(), '选择平台允许经营的产品类目', 0),
(2193, 0, 0, '经营类目保存', 2192, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'merchant:class:edit', '#', 'migration', NOW(), 'migration', NOW(), '', 0)
ON DUPLICATE KEY UPDATE
    `menu_name` = VALUES(`menu_name`), `parent_id` = VALUES(`parent_id`), `order_num` = VALUES(`order_num`),
    `path` = VALUES(`path`), `component` = VALUES(`component`), `menu_type` = VALUES(`menu_type`),
    `visible` = VALUES(`visible`), `status` = VALUES(`status`), `perms` = VALUES(`perms`),
    `icon` = VALUES(`icon`), `update_by` = 'migration', `update_time` = NOW(),
    `remark` = VALUES(`remark`), `is_delete` = 0;

INSERT INTO `ry_rental`.`ry_rental_merchant_role_menu`
(`tenant_id`, `member_id`, `role_id`, `menu_id`, `is_delete`)
SELECT role_item.tenant_id, role_item.member_id, role_item.role_id, menu_item.menu_id, 0
FROM `ry_rental`.`ry_rental_merchant_role` role_item
INNER JOIN `ry_rental`.`ry_rental_merchant_menu` menu_item ON menu_item.menu_id IN (2192, 2193)
WHERE role_item.role_key = 'merchant_admin'
  AND role_item.status = 0
  AND role_item.is_delete = 0
  AND menu_item.is_delete = 0
ON DUPLICATE KEY UPDATE `is_delete` = 0;

INSERT INTO `ry_rental`.`ry_rental_merchant_user_menu`
(`tenant_id`, `member_id`, `merchant_user_id`, `menu_id`, `is_delete`)
SELECT user_role.tenant_id, user_role.member_id, user_role.merchant_user_id, role_menu.menu_id, 0
FROM `ry_rental`.`ry_rental_merchant_user_role` user_role
INNER JOIN `ry_rental`.`ry_rental_merchant_role` role_item
    ON role_item.tenant_id = user_role.tenant_id
   AND role_item.member_id = user_role.member_id
   AND role_item.role_id = user_role.role_id
   AND role_item.is_delete = 0
INNER JOIN `ry_rental`.`ry_rental_merchant_role_menu` role_menu
    ON role_menu.tenant_id = user_role.tenant_id
   AND role_menu.member_id = user_role.member_id
   AND role_menu.role_id = user_role.role_id
   AND role_menu.menu_id IN (2192, 2193)
   AND role_menu.is_delete = 0
WHERE user_role.is_delete = 0
  AND role_item.role_key = 'merchant_admin'
ON DUPLICATE KEY UPDATE `is_delete` = 0;
