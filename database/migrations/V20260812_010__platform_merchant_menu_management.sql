-- 平台端商户菜单管理入口，以及商户管理员菜单重新初始化。
USE `ry-cloud`;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2095, '商户菜单', 2000, 11, 'merchant-menu', 'rental/merchantMenu/index', '', '', 1, 0, 'C', '0', '0', 'admin:merchantMenu:list', 'menu', 'system', NOW(), '', NULL, '平台维护商户端可显示菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2095);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2095;

INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2096, '商户菜单查询', 2095, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'admin:merchantMenu:query', '#', 'system', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2096);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2096;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2097, '商户菜单新增', 2095, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'admin:merchantMenu:add', '#', 'system', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2097);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2097;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2098, '商户菜单修改', 2095, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'admin:merchantMenu:edit', '#', 'system', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2098);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2098;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 2099, '商户菜单删除', 2095, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'admin:merchantMenu:remove', '#', 'system', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 2099);
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id = 2099;

INSERT INTO sys_role_menu (role_id, menu_id, is_delete)
SELECT 1, menu_id, 0 FROM sys_menu
WHERE menu_id IN (2095, 2096, 2097, 2098, 2099)
ON DUPLICATE KEY UPDATE is_delete = 0;

USE `ry_rental`;
-- 按手机号定位指定商户管理员，清理历史菜单快照后按当前平台模板全量恢复。
UPDATE ry_rental_merchant_user_menu um
INNER JOIN ry_rental_merchant_user u ON u.merchant_user_id = um.merchant_user_id
SET um.is_delete = 1
WHERE u.phone = '13552259921' AND u.is_delete = 0;

INSERT INTO ry_rental_merchant_user_menu (tenant_id, member_id, merchant_user_id, menu_id, is_delete)
SELECT u.tenant_id, u.member_id, u.merchant_user_id, m.menu_id, 0
FROM ry_rental_merchant_user u
INNER JOIN ry_rental_merchant_menu m ON m.tenant_id = 0 AND m.member_id = 0 AND m.is_delete = 0
WHERE u.phone = '13552259921' AND u.is_delete = 0
ON DUPLICATE KEY UPDATE is_delete = 0;

INSERT INTO ry_rental_merchant_user_role (tenant_id, member_id, merchant_user_id, role_id, is_delete)
SELECT u.tenant_id, u.member_id, u.merchant_user_id, r.role_id, 0
FROM ry_rental_merchant_user u
INNER JOIN ry_rental_merchant_role r ON r.tenant_id = u.tenant_id AND r.member_id = u.member_id
WHERE u.phone = '13552259921' AND u.is_delete = 0 AND r.role_key = 'merchant_admin' AND r.is_delete = 0
ON DUPLICATE KEY UPDATE is_delete = 0;
