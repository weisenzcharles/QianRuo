-- 商户用户管理操作权限。
INSERT INTO `ry_rental`.`ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2230, 0, 0, '用户查询', 2117, 1, 'permission/2230', '', '', '', 1, 0, 'F', '1', '0', 'merchant:user:account:query', '', 'migration', NOW(), '商户用户查询'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id=2230);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2231, 0, 0, '用户修改', 2117, 2, 'permission/2231', '', '', '', 1, 0, 'F', '1', '0', 'merchant:user:account:edit', '', 'migration', NOW(), '商户用户修改'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id=2231);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2232, 0, 0, '重置密码', 2117, 3, 'permission/2232', '', '', '', 1, 0, 'F', '1', '0', 'merchant:user:account:resetPwd', '', 'migration', NOW(), '商户用户重置密码'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id=2232);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2233, 0, 0, '用户新增', 2117, 4, 'permission/2233', '', '', '', 1, 0, 'F', '1', '0', 'merchant:user:account:add', '', 'migration', NOW(), '商户用户新增'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id=2233);

-- 将新增用户管理权限授予现有商户管理员角色；普通角色仍由商户管理员在角色页面按需配置。
INSERT INTO `ry_rental`.`ry_rental_merchant_role_menu` (tenant_id, member_id, role_id, menu_id, is_delete)
SELECT r.tenant_id, r.member_id, r.role_id, m.menu_id, 0
FROM `ry_rental`.`ry_rental_merchant_role` r
JOIN `ry_rental`.`ry_rental_merchant_menu` m ON m.menu_id IN (2230, 2231, 2232)
WHERE r.role_key='merchant_admin' AND r.is_delete=0 AND m.is_delete=0
ON DUPLICATE KEY UPDATE is_delete=0;

-- 角色管理接口也属于商户用户管理的权限范围，供账号页面加载和分配角色。
INSERT INTO `ry_rental`.`ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2240, 0, 0, '角色查询', 2116, 4, 'permission/2240', '', '', '', 1, 0, 'F', '1', '0', 'merchant:role:list', '', 'migration', NOW(), '商户角色查询'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id=2240);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2241, 0, 0, '角色详情', 2116, 5, 'permission/2241', '', '', '', 1, 0, 'F', '1', '0', 'merchant:role:query', '', 'migration', NOW(), '商户角色详情'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id=2241);
INSERT INTO `ry_rental`.`ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2242, 0, 0, '角色修改', 2116, 6, 'permission/2242', '', '', '', 1, 0, 'F', '1', '0', 'merchant:role:edit', '', 'migration', NOW(), '商户角色修改'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental`.`ry_rental_merchant_menu` WHERE menu_id=2242);

-- 修复重复执行时可能被旧菜单重建脚本软删除的权限关联，并统一日志页面组件路径。
UPDATE `ry_rental`.`ry_rental_merchant_menu`
SET is_delete = 0
WHERE menu_id IN (2230, 2231, 2232, 2240, 2241, 2242);
UPDATE `ry_rental`.`ry_rental_merchant_role_menu`
SET is_delete = 0
WHERE menu_id IN (2230, 2231, 2232, 2240, 2241, 2242);
UPDATE `ry_rental`.`ry_rental_merchant_menu`
SET component = 'merchant/monitor/operlog/index'
WHERE menu_id = 2118 AND is_delete = 0;

INSERT INTO `ry_rental`.`ry_rental_merchant_role_menu` (tenant_id, member_id, role_id, menu_id, is_delete)
SELECT r.tenant_id, r.member_id, r.role_id, m.menu_id, 0
FROM `ry_rental`.`ry_rental_merchant_role` r
JOIN `ry_rental`.`ry_rental_merchant_menu` m ON m.menu_id IN (2240, 2241, 2242)
WHERE r.role_key='merchant_admin' AND r.is_delete=0 AND m.is_delete=0
ON DUPLICATE KEY UPDATE is_delete=0;
