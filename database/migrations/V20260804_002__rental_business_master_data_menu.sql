USE `ry_rental`;
SET NAMES utf8mb4;

-- 商户主数据菜单与接口权限。模板菜单仍放在 ry_rental，商户角色和用户菜单副本按已有机制同步。
INSERT INTO `ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2150, 0, 0, '资源池管理', 2110, 5, 'merchant/pool', 'merchant/pool/index', '', '',
       1, 0, 'C', '0', '0', 'merchant:pool:list', 'coin', 'migration', NOW(), '库存资源池管理'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2150);
INSERT INTO `ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2151, 0, 0, 'SN管理', 2111, 3, 'merchant/sn', 'merchant/sn/index', '', '',
       1, 0, 'C', '0', '0', 'merchant:sn:list', 'component', 'migration', NOW(), '商品序列号管理'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2151);
INSERT INTO `ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2152, 0, 0, '配件管理', 2111, 4, 'merchant/accessory', 'merchant/accessory/index', '', '',
       1, 0, 'C', '0', '0', 'merchant:accessory:list', 'component', 'migration', NOW(), '配件实例管理'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2152);
INSERT INTO `ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2153, 0, 0, '套餐管理', 2111, 5, 'merchant/package', 'merchant/package/index', '', '',
       1, 0, 'C', '0', '0', 'merchant:package:list', 'list', 'migration', NOW(), '套餐版本与BOM管理'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2153);

-- 接口操作权限使用隐藏功能菜单，确保商户管理员可完整执行 CRUD，商户自定义角色可按权限树分配。
INSERT INTO `ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2160, 0, 0, '仓库查询', 2114, 1, 'permission/2160', '', '', '', 1, 0, 'F', '1', '0', 'merchant:warehouse:query', '', 'migration', NOW(), '接口权限'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2160)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), parent_id = VALUES(parent_id), path = VALUES(path),
    perms = VALUES(perms), is_delete = 0;
INSERT INTO `ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2161, 0, 0, '仓库新增', 2114, 2, 'permission/2161', '', '', '', 1, 0, 'F', '1', '0', 'merchant:warehouse:add', '', 'migration', NOW(), '接口权限'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2161)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), parent_id = VALUES(parent_id), path = VALUES(path),
    perms = VALUES(perms), is_delete = 0;

-- 修复第一次执行旧脚本留下的空路径，保证同一父菜单下的隐藏权限路径唯一。
UPDATE `ry_rental_merchant_menu`
SET path = CASE menu_id WHEN 2160 THEN 'permission/2160' WHEN 2161 THEN 'permission/2161' END,
    update_by = 'migration', update_time = NOW(), is_delete = 0
WHERE tenant_id = 0 AND member_id = 0 AND menu_id IN (2160, 2161);
-- 使用统一临时表生成剩余接口权限，避免重复手写角色授权 SQL。
CREATE TEMPORARY TABLE IF NOT EXISTS `tmp_rental_master_menu_permission` (
    menu_id BIGINT NOT NULL,
    menu_name VARCHAR(64) NOT NULL,
    parent_id BIGINT NOT NULL,
    perms VARCHAR(128) NOT NULL,
    PRIMARY KEY (menu_id)
);
INSERT INTO `tmp_rental_master_menu_permission` (menu_id, menu_name, parent_id, perms) VALUES
    (2162, '仓库修改', 2114, 'merchant:warehouse:edit'),
    (2163, '仓库删除', 2114, 'merchant:warehouse:remove'),
    (2164, '资源池查询', 2150, 'merchant:pool:query'),
    (2165, '资源池新增', 2150, 'merchant:pool:add'),
    (2166, '资源池修改', 2150, 'merchant:pool:edit'),
    (2167, '资源池删除', 2150, 'merchant:pool:remove'),
    (2168, '商品查询', 2111, 'merchant:product:query'),
    (2169, '商品新增', 2111, 'merchant:product:add'),
    (2170, '商品修改', 2111, 'merchant:product:edit'),
    (2171, '商品删除', 2111, 'merchant:product:remove'),
    (2172, 'SN查询', 2151, 'merchant:sn:query'),
    (2173, 'SN新增', 2151, 'merchant:sn:add'),
    (2174, 'SN修改', 2151, 'merchant:sn:edit'),
    (2175, 'SN删除', 2151, 'merchant:sn:remove'),
    (2176, '配件查询', 2152, 'merchant:accessory:query'),
    (2177, '配件新增', 2152, 'merchant:accessory:add'),
    (2178, '配件修改', 2152, 'merchant:accessory:edit'),
    (2179, '配件删除', 2152, 'merchant:accessory:remove'),
    (2180, '套餐查询', 2153, 'merchant:package:query'),
    (2181, '套餐新增', 2153, 'merchant:package:add'),
    (2182, '套餐修改', 2153, 'merchant:package:edit'),
    (2183, '套餐删除', 2153, 'merchant:package:remove'),
    (2184, '库存调整', 2122, 'merchant:inventory:adjust'),
    (2185, '库存查询', 2122, 'merchant:inventory:query'),
    (2186, '库存删除', 2122, 'merchant:inventory:remove')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), parent_id = VALUES(parent_id), perms = VALUES(perms);

INSERT INTO `ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT menu_id, 0, 0, menu_name, parent_id, menu_id, CONCAT('permission/', menu_id), '', '', '', 1, 0, 'F', '1', '0', perms, '', 'migration', NOW(), '接口权限'
FROM `tmp_rental_master_menu_permission`
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), parent_id = VALUES(parent_id), path = VALUES(path),
    perms = VALUES(perms), is_delete = 0;

UPDATE `ry_rental_merchant_menu`
SET is_delete = 0
WHERE tenant_id = 0 AND member_id = 0 AND menu_id IN (2150, 2151, 2152, 2153, 2160, 2161, 2162, 2163,
    2164, 2165, 2166, 2167, 2168, 2169, 2170, 2171, 2172, 2173, 2174, 2175, 2176, 2177, 2178, 2179,
    2180, 2181, 2182, 2183, 2184, 2185, 2186);

-- 为已有 merchant_admin 角色和用户恢复模板菜单副本；用户自定义角色不被强制追加。
INSERT INTO `ry_rental_merchant_role_menu` (tenant_id, member_id, role_id, menu_id)
SELECT r.tenant_id, r.member_id, r.role_id, m.menu_id
FROM `ry_rental_merchant_role` r
INNER JOIN `ry_rental_merchant_menu` m ON m.tenant_id = 0 AND m.member_id = 0 AND m.is_delete = 0
WHERE r.role_key = 'merchant_admin' AND r.is_delete = 0 AND r.tenant_id != 0
ON DUPLICATE KEY UPDATE is_delete = 0;

INSERT INTO `ry_rental_merchant_user_menu` (tenant_id, member_id, merchant_user_id, menu_id)
SELECT ur.tenant_id, ur.member_id, ur.merchant_user_id, m.menu_id
FROM `ry_rental_merchant_user_role` ur
INNER JOIN `ry_rental_merchant_role` r
    ON r.tenant_id = ur.tenant_id AND r.member_id = ur.member_id AND r.role_id = ur.role_id
INNER JOIN `ry_rental_merchant_menu` m ON m.tenant_id = 0 AND m.member_id = 0 AND m.is_delete = 0
WHERE r.role_key = 'merchant_admin' AND r.is_delete = 0 AND ur.is_delete = 0 AND ur.tenant_id != 0
ON DUPLICATE KEY UPDATE is_delete = 0;

DROP TEMPORARY TABLE IF EXISTS `tmp_rental_master_menu_permission`;
