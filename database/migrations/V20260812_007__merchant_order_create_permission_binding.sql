-- 商户订单新增权限必须写入商户独立菜单和角色权限表，不能只写平台 sys_menu。
USE `ry_rental`;

INSERT INTO `ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2250, 0, 0, '新增订单', 2121, 1, 'permission/2250', '', '', '', 1, 0, 'F', '1', '0',
       'merchant:order:add', '', 'migration', NOW(), '商户订单录入权限'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2250);

INSERT INTO `ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2251, 0, 0, '编辑订单', 2121, 2, 'permission/2251', '', '', '', 1, 0, 'F', '1', '0',
       'merchant:order:edit', '', 'migration', NOW(), '商户订单编辑权限'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2251);

UPDATE `ry_rental_merchant_menu`
SET is_delete = 0, perms = 'merchant:order:edit', parent_id = 2121
WHERE menu_id = 2251;

UPDATE `ry_rental_merchant_menu`
SET is_delete = 0, perms = 'merchant:order:add', parent_id = 2121
WHERE menu_id = 2250;

INSERT INTO `ry_rental_merchant_role_menu` (tenant_id, member_id, role_id, menu_id, is_delete)
SELECT r.tenant_id, r.member_id, r.role_id, 2250, 0
FROM `ry_rental_merchant_role` r
WHERE r.role_key = 'merchant_admin' AND r.is_delete = 0
ON DUPLICATE KEY UPDATE is_delete = 0;

INSERT INTO `ry_rental_merchant_role_menu` (tenant_id, member_id, role_id, menu_id, is_delete)
SELECT r.tenant_id, r.member_id, r.role_id, 2251, 0
FROM `ry_rental_merchant_role` r
WHERE r.role_key = 'merchant_admin' AND r.is_delete = 0
ON DUPLICATE KEY UPDATE is_delete = 0;
