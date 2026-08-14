USE `ry_rental`;
SET NAMES utf8mb4;

-- 商品页面和 rental-business 控制器统一使用 merchant:product:* 权限，修复历史菜单权限与接口权限不一致。
-- 这里只更新全局商户菜单模板，不强行修改商户自定义角色的授权结果。
UPDATE `ry_rental_merchant_menu`
SET perms = CASE menu_id
        WHEN 2111 THEN 'merchant:product:list'
        WHEN 2112 THEN 'merchant:product:list'
        WHEN 2113 THEN 'merchant:product:add'
        WHEN 2168 THEN 'merchant:product:query'
        WHEN 2169 THEN 'merchant:product:add'
        WHEN 2170 THEN 'merchant:product:edit'
        WHEN 2171 THEN 'merchant:product:remove'
    END,
    update_by = 'migration',
    update_time = NOW()
WHERE tenant_id = 0
  AND member_id = 0
  AND is_delete = 0
  AND menu_id IN (2111, 2112, 2113, 2168, 2169, 2170, 2171);
