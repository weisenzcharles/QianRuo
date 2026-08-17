USE `ry-cloud`;
SET NAMES utf8mb4;

-- 平台端新增两个一级菜单：产品管理、数据统计。
-- 产品管理作为产品类目、标准型号和编码配置的统一父级目录；数据统计先建立菜单入口，后续统计页面直接挂载到该目录。
INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
 `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `client_type`, `icon`,
 `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_delete`)
VALUES
(2300, '产品管理', 0, 1, 'product', NULL, '', '', 1, 0, 'M', '0', '0', '', 'platform', 'shopping',
 'migration', NOW(), 'migration', NOW(), '平台产品主数据管理目录', 0),
(2301, '数据统计', 0, 3, 'data-statistics', NULL, '', '', 1, 0, 'M', '0', '0', '', 'platform', 'chart',
 'migration', NOW(), 'migration', NOW(), '平台经营数据统计目录', 0)
ON DUPLICATE KEY UPDATE
    `menu_name` = VALUES(`menu_name`),
    `parent_id` = VALUES(`parent_id`),
    `order_num` = VALUES(`order_num`),
    `path` = VALUES(`path`),
    `component` = VALUES(`component`),
    `menu_type` = VALUES(`menu_type`),
    `visible` = VALUES(`visible`),
    `status` = VALUES(`status`),
    `perms` = VALUES(`perms`),
    `client_type` = VALUES(`client_type`),
    `icon` = VALUES(`icon`),
    `update_by` = 'migration',
    `update_time` = NOW(),
    `remark` = VALUES(`remark`),
    `is_delete` = 0;

-- 统一平台端一级菜单顺序：产品管理、商户管理、数据统计、系统管理、系统监控、系统工具、若依官网。
UPDATE `sys_menu`
SET `order_num` = CASE `menu_id`
        WHEN 2300 THEN 1
        WHEN 2000 THEN 2
        WHEN 2301 THEN 3
        WHEN 1 THEN 4
        WHEN 2 THEN 5
        WHEN 3 THEN 6
        WHEN 4 THEN 7
        ELSE `order_num`
    END,
    `update_by` = 'migration',
    `update_time` = NOW()
WHERE `menu_id` IN (2300, 2000, 2301, 1, 2, 3, 4)
  AND `parent_id` = 0
  AND `is_delete` = 0;

-- 按要求调整平台菜单名称，并将产品主数据菜单迁移到产品管理目录。
UPDATE `sys_menu`
SET `menu_name` = CASE `menu_id`
        WHEN 2000 THEN '商户管理'
        WHEN 2007 THEN '用户管理'
        WHEN 2050 THEN '编码配置'
        ELSE `menu_name`
    END,
    `parent_id` = CASE `menu_id`
        WHEN 2030 THEN 2300
        WHEN 2040 THEN 2300
        WHEN 2050 THEN 2300
        ELSE `parent_id`
    END,
    `order_num` = CASE `menu_id`
        WHEN 2030 THEN 1
        WHEN 2040 THEN 2
        WHEN 2050 THEN 3
        ELSE `order_num`
    END,
    `update_by` = 'migration',
    `update_time` = NOW()
WHERE `menu_id` IN (2000, 2007, 2030, 2040, 2050)
  AND `client_type` = 'platform'
  AND `is_delete` = 0;

-- 产品子菜单的权限编码保持不变，仅为拥有原产品权限的角色补齐新的父级目录权限。
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `is_delete`)
SELECT DISTINCT role_menu.`role_id`, 2300, 0
FROM `sys_role_menu` role_menu
INNER JOIN `sys_menu` product_menu ON product_menu.`menu_id` = role_menu.`menu_id`
WHERE product_menu.`menu_id` IN (2030, 2040, 2050)
  AND product_menu.`client_type` = 'platform'
  AND product_menu.`is_delete` = 0
  AND role_menu.`is_delete` = 0
ON DUPLICATE KEY UPDATE `is_delete` = 0;

-- 平台超级管理员可以看到新增的两个一级菜单。
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `is_delete`)
SELECT 1, `menu_id`, 0
FROM `sys_menu`
WHERE `menu_id` IN (2300, 2301)
  AND `client_type` = 'platform'
  AND `is_delete` = 0
ON DUPLICATE KEY UPDATE `is_delete` = 0;
