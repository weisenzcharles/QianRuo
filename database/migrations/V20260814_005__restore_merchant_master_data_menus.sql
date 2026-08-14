-- 恢复仍在使用的资源池和 SN 业务菜单。
-- 商品发布菜单仍按 V20260812_008 的要求保持停用；本迁移只恢复已有完整 CRUD 页面对应的菜单。
USE `ry_rental`;

UPDATE `ry_rental_merchant_menu`
SET `status` = '0',
    `is_delete` = 0,
    `update_by` = 'migration',
    `update_time` = NOW()
WHERE `tenant_id` = 0
  AND `member_id` = 0
  AND `menu_id` IN (2150, 2151);

UPDATE `ry_rental_merchant_role_menu` rm
INNER JOIN `ry_rental_merchant_role` r
    ON r.`tenant_id` = rm.`tenant_id`
   AND r.`member_id` = rm.`member_id`
   AND r.`role_id` = rm.`role_id`
SET rm.`is_delete` = 0
WHERE r.`role_key` = 'merchant_admin'
  AND r.`status` = '0'
  AND r.`is_delete` = 0
  AND rm.`menu_id` IN (2150, 2151);
