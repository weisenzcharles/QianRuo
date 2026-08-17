-- 将商户端“文件配额审计”菜单统一更名为“文件配额”。
-- 按菜单主键和路由条件同时更新，兼容全局模板及已生成的历史菜单记录。
USE `ry_rental`;

UPDATE `ry_rental_merchant_menu`
SET `menu_name` = '文件配额',
    `update_by` = 'migration',
    `update_time` = NOW()
WHERE `menu_id` = 2203
   OR (`path` = 'merchant/user/file-quota' AND `perms` = 'merchant:fileQuota:list');
