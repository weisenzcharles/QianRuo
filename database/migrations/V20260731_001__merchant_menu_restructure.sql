USE `ry_rental`;

-- ============================================================================
-- 商家端菜单树重构：首页 / 店铺 / 交易 / 数据 / 帮助
-- 完全替换原有模板菜单（商家工作台、订单中心、仓库库存、商品资料等）。
-- 同时重建各商户 merchant_admin 的角色菜单、用户菜单副本，使已开通商户也能看到新树。
-- 幂等：每次部署执行均会得到一致结果。
-- ============================================================================

-- 1) 软删除模板菜单与模板角色菜单绑定（tenant_id = 0, member_id = 0）。
UPDATE `ry_rental_merchant_role_menu`
SET is_delete = 1
WHERE tenant_id = 0 AND member_id = 0;
UPDATE `ry_rental_merchant_menu`
SET is_delete = 1
WHERE tenant_id = 0 AND member_id = 0;

-- 2) 插入新的商家端菜单树（模板）
INSERT INTO `ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2100, 0, 0, '首页', 0, 1, 'merchant/home', 'merchant/home/index', '', '', 1, 0, 'C', '0', '0', 'merchant:home:view', 'dashboard', 'migration', NOW(), '商家端首页'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2100);
INSERT INTO `ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2110, 0, 0, '店铺', 0, 2, 'merchant/store', 'merchant/store/index', '', '', 1, 0, 'M', '0', '0', 'merchant:store:view', 'shop', 'migration', NOW(), '店铺管理总入口'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2110);
INSERT INTO `ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2111, 0, 0, '商品管理', 2110, 1, 'merchant/product', 'merchant/product/index', '', '', 1, 0, 'M', '0', '0', 'merchant:product:view', 'goods', 'migration', NOW(), '商品管理'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2111);
INSERT INTO `ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2112, 0, 0, '我的商品', 2111, 1, 'merchant/product/my', 'merchant/product/my/index', '', '', 1, 0, 'C', '0', '0', 'merchant:product:my:list', 'list', 'migration', NOW(), '我的商品'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2112);
INSERT INTO `ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2113, 0, 0, '发布商品', 2111, 2, 'merchant/product/publish', 'merchant/product/publish/index', '', '', 1, 0, 'C', '0', '0', 'merchant:product:publish:add', 'upload', 'migration', NOW(), '发布商品'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2113);
INSERT INTO `ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2114, 0, 0, '仓库管理', 2110, 2, 'merchant/warehouse', 'merchant/warehouse/index', '', '', 1, 0, 'C', '0', '0', 'merchant:warehouse:list', 'house', 'migration', NOW(), '仓库管理'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2114);
INSERT INTO `ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2115, 0, 0, '店铺管理', 2110, 3, 'merchant/store/profile', 'merchant/store/profile/index', '', '', 1, 0, 'C', '0', '0', 'merchant:store:profile:view', 'shop', 'migration', NOW(), '店铺资料与设置'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2115);
INSERT INTO `ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2116, 0, 0, '用户管理', 2110, 4, 'merchant/user', 'merchant/user/index', '', '', 1, 0, 'M', '0', '0', 'merchant:user:view', 'user', 'migration', NOW(), '用户管理'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2116);
INSERT INTO `ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2117, 0, 0, '账号管理', 2116, 1, 'merchant/user/account', 'merchant/user/account/index', '', '', 1, 0, 'C', '0', '0', 'merchant:user:account:list', 'user', 'migration', NOW(), '账号管理'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2117);
INSERT INTO `ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2118, 0, 0, '操作日志', 2116, 2, 'merchant/user/operlog', 'merchant/user/operlog/index', '', '', 1, 0, 'C', '0', '0', 'merchant:operlog:list', 'log', 'migration', NOW(), '操作日志'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2118);
INSERT INTO `ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2120, 0, 0, '交易', 0, 3, 'merchant/trade', 'merchant/trade/index', '', '', 1, 0, 'M', '0', '0', 'merchant:trade:view', 'money', 'migration', NOW(), '交易管理总入口'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2120);
INSERT INTO `ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2121, 0, 0, '订单管理', 2120, 1, 'merchant/order', 'merchant/order/index', '', '', 1, 0, 'C', '0', '0', 'merchant:order:list', 'document', 'migration', NOW(), '订单管理'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2121);
INSERT INTO `ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2122, 0, 0, '库存管理', 2120, 2, 'merchant/inventory', 'merchant/inventory/index', '', '', 1, 0, 'C', '0', '0', 'merchant:inventory:list', 'box', 'migration', NOW(), '库存管理'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2122);
INSERT INTO `ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2123, 0, 0, '物流管理', 2120, 3, 'merchant/logistics', 'merchant/logistics/index', '', '', 1, 0, 'C', '0', '0', 'merchant:logistics:list', 'van', 'migration', NOW(), '物流管理'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2123);
INSERT INTO `ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2130, 0, 0, '数据', 0, 4, 'merchant/data', 'merchant/data/index', '', '', 1, 0, 'C', '0', '0', 'merchant:data:view', 'chart', 'migration', NOW(), '经营数据'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2130);
INSERT INTO `ry_rental_merchant_menu` (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 2140, 0, 0, '帮助', 0, 5, 'merchant/help', 'merchant/help/index', '', '', 1, 0, 'C', '0', '0', 'merchant:help:view', 'question', 'migration', NOW(), '帮助中心'
WHERE NOT EXISTS (SELECT 1 FROM `ry_rental_merchant_menu` WHERE menu_id = 2140);

-- 重新启用本次菜单树中的模板节点；保留历史菜单记录，禁止物理删除。
UPDATE `ry_rental_merchant_menu`
SET is_delete = 0
WHERE tenant_id = 0 AND member_id = 0
  AND menu_id IN (2100, 2110, 2111, 2112, 2113, 2114, 2115, 2116, 2117, 2118,
                  2120, 2121, 2122, 2123, 2130, 2140);

-- 3) 重建各商户 merchant_admin 的角色菜单副本（tenant_id != 0）。
-- 角色菜单表主键为（role_id, menu_id），模板角色与商户角色不能同时写入同一主键。
UPDATE `ry_rental_merchant_role_menu` rm
INNER JOIN `ry_rental_merchant_role` r
  ON r.tenant_id = rm.tenant_id AND r.member_id = rm.member_id AND r.role_id = rm.role_id
SET rm.is_delete = 1
WHERE r.role_key = 'merchant_admin' AND r.is_delete = '0' AND rm.tenant_id != 0;

INSERT INTO `ry_rental_merchant_role_menu` (tenant_id, member_id, role_id, menu_id)
SELECT r.tenant_id, r.member_id, r.role_id, m.menu_id
FROM `ry_rental_merchant_role` r
INNER JOIN `ry_rental_merchant_menu` m ON m.tenant_id = 0 AND m.member_id = 0 AND m.is_delete = '0'
WHERE r.role_key = 'merchant_admin' AND r.is_delete = '0' AND r.tenant_id != 0
ON DUPLICATE KEY UPDATE
    tenant_id = VALUES(tenant_id), member_id = VALUES(member_id), is_delete = 0;

-- 4) 重建各商户 merchant_admin 的用户菜单副本（tenant_id != 0）
UPDATE `ry_rental_merchant_user_menu` um
INNER JOIN `ry_rental_merchant_user_role` ur
  ON ur.tenant_id = um.tenant_id AND ur.member_id = um.member_id AND ur.merchant_user_id = um.merchant_user_id
INNER JOIN `ry_rental_merchant_role` r
  ON r.tenant_id = ur.tenant_id AND r.member_id = ur.member_id AND r.role_id = ur.role_id
SET um.is_delete = 1
WHERE r.role_key = 'merchant_admin' AND r.is_delete = '0' AND ur.is_delete = '0' AND um.tenant_id != 0;

INSERT INTO `ry_rental_merchant_user_menu` (tenant_id, member_id, merchant_user_id, menu_id)
SELECT ur.tenant_id, ur.member_id, ur.merchant_user_id, m.menu_id
FROM `ry_rental_merchant_user_role` ur
INNER JOIN `ry_rental_merchant_role` r
  ON r.tenant_id = ur.tenant_id AND r.member_id = ur.member_id AND r.role_id = ur.role_id
INNER JOIN `ry_rental_merchant_menu` m ON m.tenant_id = 0 AND m.member_id = 0 AND m.is_delete = '0'
WHERE r.role_key = 'merchant_admin' AND r.is_delete = '0' AND ur.is_delete = '0' AND ur.tenant_id != 0
ON DUPLICATE KEY UPDATE
    tenant_id = VALUES(tenant_id), member_id = VALUES(member_id), is_delete = 0;
