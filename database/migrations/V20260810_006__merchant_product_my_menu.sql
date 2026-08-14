USE `ry_rental`;
SET NAMES utf8mb4;

-- 商品管理菜单的固定结构：商品管理为目录，我的商品直接进入当前商户商品列表。
-- 这里只维护全局菜单模板，不强制修改商户自定义角色或用户的授权范围。
UPDATE `ry_rental_merchant_menu`
SET menu_name = '商品管理',
    parent_id = 2110,
    order_num = 1,
    path = 'merchant/product',
    component = 'ParentView',
    `query` = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'M',
    visible = '0',
    status = '0',
    perms = 'merchant:product:list',
    icon = 'goods',
    is_delete = 0,
    update_by = 'migration',
    update_time = NOW(),
    remark = '商品管理总入口'
WHERE menu_id = 2111
  AND tenant_id = 0
  AND member_id = 0;

-- 使用主键幂等写入“我的商品”，避免依赖初始化种子是否已经执行。
-- 该菜单的组件与商户端商品列表页面保持一一对应，路由为 /merchant/product/my。
INSERT INTO `ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark, is_delete)
VALUES
    (2112, 0, 0, '我的商品', 2111, 1, 'merchant/product/my', 'merchant/product/my/index', '', '',
     1, 0, 'C', '0', '0', 'merchant:product:list', 'list', 'migration', NOW(), '当前商户商品列表', 0)
ON DUPLICATE KEY UPDATE
    tenant_id = VALUES(tenant_id),
    member_id = VALUES(member_id),
    menu_name = VALUES(menu_name),
    parent_id = VALUES(parent_id),
    order_num = VALUES(order_num),
    path = VALUES(path),
    component = VALUES(component),
    `query` = VALUES(`query`),
    route_name = VALUES(route_name),
    is_frame = VALUES(is_frame),
    is_cache = VALUES(is_cache),
    menu_type = VALUES(menu_type),
    visible = VALUES(visible),
    status = VALUES(status),
    perms = VALUES(perms),
    icon = VALUES(icon),
    is_delete = 0,
    update_by = 'migration',
    update_time = NOW(),
    remark = VALUES(remark);

-- 仅为已经拥有“商品管理”父菜单的角色补齐“我的商品”子菜单。
-- 通过父菜单关联作为授权前置条件，不给没有商品管理权限的角色扩大访问范围。
INSERT INTO `ry_rental_merchant_role_menu` (tenant_id, member_id, role_id, menu_id, is_delete)
SELECT parent_rm.tenant_id, parent_rm.member_id, parent_rm.role_id, 2112, 0
FROM `ry_rental_merchant_role_menu` parent_rm
INNER JOIN `ry_rental_merchant_menu` parent_menu
    ON parent_menu.menu_id = parent_rm.menu_id
   AND parent_menu.tenant_id = 0
   AND parent_menu.member_id = 0
   AND parent_menu.is_delete = 0
WHERE parent_rm.menu_id = 2111
  AND parent_rm.is_delete = 0
ON DUPLICATE KEY UPDATE is_delete = 0;

-- 仅为已经拥有“商品管理”父菜单的用户补齐子菜单，兼容直接授权和历史菜单副本。
INSERT INTO `ry_rental_merchant_user_menu` (tenant_id, member_id, merchant_user_id, menu_id, is_delete)
SELECT parent_um.tenant_id, parent_um.member_id, parent_um.merchant_user_id, 2112, 0
FROM `ry_rental_merchant_user_menu` parent_um
INNER JOIN `ry_rental_merchant_menu` parent_menu
    ON parent_menu.menu_id = parent_um.menu_id
   AND parent_menu.tenant_id = 0
   AND parent_menu.member_id = 0
   AND parent_menu.is_delete = 0
WHERE parent_um.menu_id = 2111
  AND parent_um.is_delete = 0
ON DUPLICATE KEY UPDATE is_delete = 0;
