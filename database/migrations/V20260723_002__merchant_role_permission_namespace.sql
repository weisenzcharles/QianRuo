-- 商户角色权限必须使用独立 merchant:* 命名空间。
UPDATE `ry_rental`.`ry_rental_merchant_menu`
SET perms = CASE menu_id
    WHEN 2180 THEN 'merchant:role:list'
    WHEN 2181 THEN 'merchant:role:query'
    WHEN 2182 THEN 'merchant:role:add'
    WHEN 2183 THEN 'merchant:role:edit'
    WHEN 2184 THEN 'merchant:role:remove'
    ELSE perms
END,
update_by = 'migration',
update_time = NOW()
WHERE tenant_id = 0
  AND member_id = 0
  AND menu_id IN (2180, 2181, 2182, 2183, 2184);

UPDATE `ry_rental`.`ry_rental_merchant_menu`
SET component = 'merchant/access/role/index',
    update_by = 'migration',
    update_time = NOW()
WHERE tenant_id = 0
  AND member_id = 0
  AND menu_id = 2180;

-- 清理历史商户菜单中错误复用的平台权限码，防止旧数据继续进入登录态。
UPDATE `ry_rental`.`ry_rental_merchant_menu`
SET status = '1',
    update_by = 'migration',
    update_time = NOW()
WHERE tenant_id = 0
  AND member_id = 0
  AND perms LIKE 'system:%'
  AND menu_id NOT IN (2180, 2181, 2182, 2183, 2184);
