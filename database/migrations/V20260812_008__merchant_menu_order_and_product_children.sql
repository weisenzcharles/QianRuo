-- 商户端菜单最终结构：首页、交易、店铺、数据、帮助；商品管理只保留五个业务子菜单。
USE `ry_rental`;

UPDATE `ry_rental_merchant_menu`
SET order_num = CASE menu_id
        WHEN 2100 THEN 1
        WHEN 2120 THEN 2
        WHEN 2110 THEN 3
        WHEN 2130 THEN 4
        WHEN 2140 THEN 5
        ELSE order_num
    END,
    update_by = 'migration', update_time = NOW()
WHERE tenant_id = 0 AND member_id = 0 AND menu_id IN (2100, 2120, 2110, 2130, 2140);

UPDATE `ry_rental_merchant_menu`
SET menu_name = CASE menu_id
        WHEN 2192 THEN '经营类目'
        WHEN 2112 THEN '我的商品'
        WHEN 2152 THEN '我的配件'
        WHEN 2153 THEN '我的套餐'
        WHEN 2194 THEN '品牌型号'
        ELSE menu_name
    END,
    parent_id = 2111,
    order_num = CASE menu_id
        WHEN 2112 THEN 1
        WHEN 2152 THEN 2
        WHEN 2153 THEN 3
        WHEN 2194 THEN 4
        WHEN 2192 THEN 5
        ELSE order_num
    END,
    update_by = 'migration', update_time = NOW(), is_delete = 0
WHERE tenant_id = 0 AND member_id = 0 AND menu_id IN (2192, 2112, 2152, 2153, 2194);

-- 明确隐藏已经废弃的商品发布、SN 和批量导入菜单，避免旧缓存再次显示。
UPDATE `ry_rental_merchant_menu`
SET is_delete = 1, update_by = 'migration', update_time = NOW()
WHERE tenant_id = 0 AND member_id = 0 AND menu_id IN (2113, 2151, 2202);

UPDATE `ry_rental_merchant_role_menu` rm
INNER JOIN `ry_rental_merchant_role` r
    ON r.tenant_id = rm.tenant_id AND r.member_id = rm.member_id AND r.role_id = rm.role_id
SET rm.is_delete = 1
WHERE r.role_key = 'merchant_admin' AND rm.menu_id IN (2113, 2151, 2202);
