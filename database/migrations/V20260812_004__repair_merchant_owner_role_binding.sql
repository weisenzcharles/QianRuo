USE `ry_rental`;

-- 修复历史种子数据中商户管理员角色租户归属错误，确保 owner 使用本商户自己的权限空间。
UPDATE `ry_rental_merchant_role`
SET tenant_id = 1
WHERE role_id = 100000 AND member_id = 1 AND role_key = 'merchant_admin' AND is_delete = 0;

-- 角色菜单表历史记录可能保留旧租户号；主键不允许直接重插时，统一修正管理员权限归属。
UPDATE `ry_rental_merchant_role_menu`
SET tenant_id = 1, member_id = 1, is_delete = 0
WHERE role_id = 100000
  AND menu_id IN (2117, 2118, 2230, 2231, 2232, 2233, 2240, 2241, 2242);

-- 为当前商户 owner 补齐管理员角色绑定；重复执行不会产生重复关系。
INSERT INTO `ry_rental_merchant_user_role` (merchant_user_id, role_id, tenant_id, member_id, is_delete)
SELECT u.merchant_user_id, r.role_id, b.tenant_id, b.member_id, 0
FROM `ry_rental_merchant_user` u
JOIN `ry_rental_user_member` b ON b.merchant_user_id = u.merchant_user_id
    AND b.tenant_id = 1 AND b.member_id = 1 AND b.member_role = 'owner' AND b.is_delete = 0
JOIN `ry_rental_merchant_role` r ON r.tenant_id = b.tenant_id
    AND r.member_id = b.member_id AND r.role_key = 'merchant_admin' AND r.is_delete = 0
WHERE u.phone = '13552259921' AND u.is_delete = 0
ON DUPLICATE KEY UPDATE is_delete = 0;

-- 重新启用管理员角色的用户管理和操作日志权限。
INSERT INTO `ry_rental_merchant_role_menu` (tenant_id, member_id, role_id, menu_id, is_delete)
SELECT r.tenant_id, r.member_id, r.role_id, m.menu_id, 0
FROM `ry_rental_merchant_role` r
JOIN `ry_rental_merchant_menu` m ON m.menu_id IN (2117, 2118, 2230, 2231, 2232, 2233, 2240, 2241, 2242)
WHERE r.tenant_id = 1 AND r.member_id = 1 AND r.role_key = 'merchant_admin' AND r.is_delete = 0
  AND m.is_delete = 0
ON DUPLICATE KEY UPDATE is_delete = 0;

-- 用户权限装载同时读取用户菜单副本，为管理员用户补齐直接权限副本。
INSERT INTO `ry_rental_merchant_user_menu` (tenant_id, member_id, merchant_user_id, menu_id, is_delete)
SELECT b.tenant_id, b.member_id, b.merchant_user_id, m.menu_id, 0
FROM `ry_rental_user_member` b
JOIN `ry_rental_merchant_user_role` ur ON ur.merchant_user_id=b.merchant_user_id
  AND ur.tenant_id=b.tenant_id AND ur.member_id=b.member_id AND ur.is_delete=0
JOIN `ry_rental_merchant_role` r ON r.role_id=ur.role_id AND r.tenant_id=ur.tenant_id
  AND r.member_id=ur.member_id AND r.role_key='merchant_admin' AND r.is_delete=0
JOIN `ry_rental_merchant_menu` m ON m.menu_id IN (2117,2118,2230,2231,2232,2233,2240,2241,2242)
WHERE b.is_delete=0 AND m.is_delete=0
ON DUPLICATE KEY UPDATE is_delete=0;
