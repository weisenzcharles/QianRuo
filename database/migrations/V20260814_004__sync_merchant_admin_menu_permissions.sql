-- 商户管理员始终拥有平台当前有效的全部商户菜单权限。
-- 代码侧会按 merchant_admin 角色动态读取有效权限，本迁移用于补齐已有账号的角色菜单快照，
-- 保证角色授权页面、按钮权限和登录时权限集合在当前数据库中保持一致。
USE `ry_rental`;

INSERT INTO `ry_rental_merchant_role_menu`
    (`tenant_id`, `member_id`, `role_id`, `menu_id`, `is_delete`)
SELECT r.`tenant_id`, r.`member_id`, r.`role_id`, m.`menu_id`, 0
FROM `ry_rental_merchant_role` r
INNER JOIN `ry_rental_merchant_menu` m
    ON m.`tenant_id` = 0
   AND m.`member_id` = 0
   AND m.`status` = '0'
   AND m.`is_delete` = 0
WHERE r.`role_key` = 'merchant_admin'
  AND r.`status` = '0'
  AND r.`is_delete` = 0
ON DUPLICATE KEY UPDATE
    `tenant_id` = VALUES(`tenant_id`),
    `member_id` = VALUES(`member_id`),
    `is_delete` = 0;
