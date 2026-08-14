-- 移除商户端“发布商品”菜单，保留页面代码以便后续业务重新启用。
USE `ry_rental`;

-- 商户菜单统一采用软删除，避免破坏历史权限记录和操作审计关系。
UPDATE `ry_rental_merchant_menu`
SET `is_delete` = 1,
    `update_by` = 'migration',
    `update_time` = NOW(),
    `remark` = '已移除：商户端商品发布入口'
WHERE `menu_id` = 2113 AND `is_delete` = 0;

-- 同步软删除商户角色菜单副本，防止角色权限接口再次返回已移除菜单。
UPDATE `ry_rental_merchant_role_menu`
SET `is_delete` = 1
WHERE `menu_id` = 2113 AND `is_delete` = 0;

-- 同步软删除商户用户菜单副本，防止用户级权限绕过角色菜单继续显示入口。
UPDATE `ry_rental_merchant_user_menu`
SET `is_delete` = 1
WHERE `menu_id` = 2113 AND `is_delete` = 0;
