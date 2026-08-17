-- 平台全局商户角色模板：角色定义不再绑定任何租户、商户或用户。
USE `ry_rental`;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_role_template` (
    `role_template_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '全局商户角色模板ID',
    `role_name` VARCHAR(30) NOT NULL COMMENT '角色名称',
    `role_key` VARCHAR(100) NOT NULL COMMENT '角色权限字符',
    `role_sort` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `menu_check_strictly` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '菜单树是否父子联动',
    `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '角色状态：0正常 1停用',
    `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '修改人',
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`role_template_id`),
    UNIQUE KEY `uk_merchant_role_template_name` (`role_name`, `is_delete`),
    UNIQUE KEY `uk_merchant_role_template_key` (`role_key`, `is_delete`),
    KEY `idx_merchant_role_template_status` (`status`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台全局商户角色模板';

CREATE TABLE IF NOT EXISTS `ry_rental_merchant_role_template_menu` (
    `role_template_id` BIGINT NOT NULL COMMENT '全局商户角色模板ID',
    `menu_id` BIGINT NOT NULL COMMENT '商户菜单ID',
    `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识：0正常 1删除',
    PRIMARY KEY (`role_template_id`, `menu_id`),
    KEY `idx_role_template_menu_menu` (`menu_id`, `is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局商户角色模板菜单授权';

-- 商户运行角色仍按 tenant_id + member_id 隔离；该字段只记录角色模板来源，不把平台模板绑定到某个商户。
SET @has_role_template_id = (
    SELECT COUNT(1)
    FROM information_schema.columns
    WHERE table_schema = 'ry_rental'
      AND table_name = 'ry_rental_merchant_role'
      AND column_name = 'role_template_id'
);
SET @add_role_template_id_sql = IF(
    @has_role_template_id = 0,
    'ALTER TABLE `ry_rental`.`ry_rental_merchant_role` ADD COLUMN `role_template_id` BIGINT DEFAULT NULL COMMENT ''全局角色模板ID'' AFTER `member_id`',
    'SELECT 1'
);
PREPARE add_role_template_id_statement FROM @add_role_template_id_sql;
EXECUTE add_role_template_id_statement;
DEALLOCATE PREPARE add_role_template_id_statement;

SET @has_role_template_index = (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = 'ry_rental'
      AND table_name = 'ry_rental_merchant_role'
      AND index_name = 'idx_rental_merchant_role_template'
);
SET @add_role_template_index_sql = IF(
    @has_role_template_index = 0,
    'ALTER TABLE `ry_rental`.`ry_rental_merchant_role` ADD KEY `idx_rental_merchant_role_template` (`role_template_id`, `is_delete`)',
    'SELECT 1'
);
PREPARE add_role_template_index_statement FROM @add_role_template_index_sql;
EXECUTE add_role_template_index_statement;
DEALLOCATE PREPARE add_role_template_index_statement;

-- 从已有商户运行角色中迁移去重后的全局角色定义，并固定默认的管理员、发货员角色。
INSERT INTO `ry_rental_merchant_role_template`
    (role_name, role_key, role_sort, menu_check_strictly, status, is_delete, create_by, create_time, remark)
SELECT source_role.role_name, source_role.role_key, source_role.role_sort,
       source_role.menu_check_strictly, source_role.status, 0, 'migration', NOW(),
       '由历史商户角色提取的全局角色模板'
FROM (
    SELECT role_name, role_key, role_sort, menu_check_strictly, status, role_id,
           ROW_NUMBER() OVER (PARTITION BY role_key ORDER BY role_id) AS row_no
    FROM `ry_rental_merchant_role`
    WHERE is_delete = 0
) source_role
WHERE source_role.row_no = 1
ON DUPLICATE KEY UPDATE
    role_sort = VALUES(role_sort),
    menu_check_strictly = VALUES(menu_check_strictly),
    status = VALUES(status),
    is_delete = 0,
    update_by = 'migration',
    update_time = NOW();

INSERT INTO `ry_rental_merchant_role_template`
    (role_name, role_key, role_sort, menu_check_strictly, status, is_delete, create_by, create_time, remark)
VALUES ('管理员', 'merchant_admin', 1, 1, '0', 0, 'migration', NOW(), '所有商户可用的默认管理员角色'),
       ('发货员', 'merchant_shipper', 2, 1, '0', 0, 'migration', NOW(), '所有商户可用的发货角色')
ON DUPLICATE KEY UPDATE
    status = '0',
    is_delete = 0,
    update_by = 'migration',
    update_time = NOW();

-- 现有商户运行角色绑定到同权限字符的全局模板，历史角色名称和已有商户权限不被覆盖。
UPDATE `ry_rental_merchant_role` r
INNER JOIN `ry_rental_merchant_role_template` t ON t.role_key = r.role_key AND t.is_delete = 0
SET r.role_template_id = t.role_template_id,
    r.update_by = 'migration',
    r.update_time = NOW()
WHERE r.is_delete = 0;

-- 管理员模板默认拥有平台当前启用的全部商户菜单；后续平台可以在角色页面调整模板菜单。
INSERT INTO `ry_rental_merchant_role_template_menu` (role_template_id, menu_id, is_delete)
SELECT t.role_template_id, m.menu_id, 0
FROM `ry_rental_merchant_role_template` t
INNER JOIN `ry_rental_merchant_menu` m
    ON m.tenant_id = 0 AND m.member_id = 0 AND m.status = '0' AND m.is_delete = 0
WHERE t.role_key = 'merchant_admin' AND t.is_delete = 0
ON DUPLICATE KEY UPDATE is_delete = 0;

-- 其他历史角色的菜单授权迁移到对应全局模板，管理员上面的全量菜单优先保持不变。
INSERT INTO `ry_rental_merchant_role_template_menu` (role_template_id, menu_id, is_delete)
SELECT DISTINCT t.role_template_id, rm.menu_id, 0
FROM `ry_rental_merchant_role_template` t
INNER JOIN `ry_rental_merchant_role` r
    ON r.role_key = t.role_key AND r.is_delete = 0
INNER JOIN `ry_rental_merchant_role_menu` rm
    ON rm.role_id = r.role_id AND rm.tenant_id = r.tenant_id AND rm.member_id = r.member_id
   AND rm.is_delete = 0
WHERE t.role_key <> 'merchant_admin' AND t.is_delete = 0
ON DUPLICATE KEY UPDATE is_delete = 0;

-- 已存在商户补齐新的全局角色模板。商户实际用户授权仍写入 tenant_id + member_id 关系表。
INSERT INTO `ry_rental_merchant_role`
    (tenant_id, member_id, role_template_id, role_name, role_key, role_sort,
     menu_check_strictly, status, is_delete, create_by, create_time, remark)
SELECT m.tenant_id, m.member_id, t.role_template_id, t.role_name, t.role_key, t.role_sort,
       t.menu_check_strictly, t.status, 0, 'migration', NOW(), '由平台全局角色模板初始化'
FROM `ry_rental_member` m
INNER JOIN `ry_rental_merchant_role_template` t ON t.is_delete = 0
WHERE m.is_delete = 0
  AND NOT EXISTS (
      SELECT 1
      FROM `ry_rental_merchant_role` existing_role
      WHERE existing_role.tenant_id = m.tenant_id
        AND existing_role.member_id = m.member_id
        AND existing_role.role_template_id = t.role_template_id
        AND existing_role.is_delete = 0
  );

-- 为新补齐的商户运行角色复制模板菜单，历史商户已有自定义授权不会被清空。
INSERT INTO `ry_rental_merchant_role_menu` (tenant_id, member_id, role_id, menu_id, is_delete)
SELECT r.tenant_id, r.member_id, r.role_id, tm.menu_id, 0
FROM `ry_rental_merchant_role` r
INNER JOIN `ry_rental_merchant_role_template_menu` tm
    ON tm.role_template_id = r.role_template_id AND tm.is_delete = 0
WHERE r.is_delete = 0
ON DUPLICATE KEY UPDATE is_delete = 0;

-- 平台菜单命名和接口路径彻底切换为全局商户角色模板语义，不再出现所属商户筛选。
USE `ry-cloud`;
UPDATE `sys_menu`
SET `menu_name` = CASE `menu_id`
        WHEN 2020 THEN '商户角色'
        WHEN 2021 THEN '商户角色查询'
        WHEN 2022 THEN '商户角色新增'
        WHEN 2023 THEN '商户角色修改'
        WHEN 2024 THEN '商户角色删除'
        ELSE `menu_name`
    END,
    `remark` = CASE WHEN `menu_id` = 2020 THEN '平台维护所有商户可用的全局角色模板' ELSE `remark` END,
    `update_by` = 'migration',
    `update_time` = NOW()
WHERE `menu_id` IN (2020, 2021, 2022, 2023, 2024)
  AND `client_type` = 'platform'
  AND `is_delete` = 0;
