USE `ry_rental`;
SET NAMES utf8mb4;

-- 将经营类目恢复为商品管理的第一项业务配置，避免菜单树重构后页面和权限被软删除。
INSERT INTO `ry_rental_merchant_menu`
    (menu_id, tenant_id, member_id, menu_name, parent_id, order_num, path, component, `query`, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon,
     create_by, create_time, update_by, update_time, remark, is_delete)
VALUES
    (2192, 0, 0, '经营类目', 2111, 1, 'merchant/product/class', 'merchant/productClass/index', '', '',
     1, 0, 'C', '0', '0', 'merchant:class:list', 'tree-table',
     'migration', NOW(), 'migration', NOW(), '选择平台允许经营的产品类目', 0),
    (2193, 0, 0, '经营类目保存', 2192, 1, 'permission/2193', '', '', '',
     1, 0, 'F', '1', '0', 'merchant:class:edit', '',
     'migration', NOW(), 'migration', NOW(), '经营类目保存权限', 0)
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
    update_by = 'migration',
    update_time = NOW(),
    remark = VALUES(remark),
    is_delete = 0;

-- 固定商品管理的业务顺序：经营类目 -> 我的商品 -> 发布商品 -> 品牌型号 -> SN -> 配件 -> 套餐。
UPDATE `ry_rental_merchant_menu`
SET order_num = CASE menu_id
        WHEN 2192 THEN 1
        WHEN 2112 THEN 2
        WHEN 2113 THEN 3
        WHEN 2194 THEN 4
        WHEN 2151 THEN 5
        WHEN 2152 THEN 6
        WHEN 2153 THEN 7
        ELSE order_num
    END,
    update_by = 'migration',
    update_time = NOW()
WHERE tenant_id = 0
  AND member_id = 0
  AND menu_id IN (2192, 2112, 2113, 2194, 2151, 2152, 2153)
  AND is_delete = 0;

-- 只恢复历史上已经授予商户管理员的角色菜单记录，不向自定义角色追加新权限。
UPDATE `ry_rental_merchant_role_menu` role_menu
INNER JOIN `ry_rental_merchant_role` role_item
    ON role_item.tenant_id = role_menu.tenant_id
   AND role_item.member_id = role_menu.member_id
   AND role_item.role_id = role_menu.role_id
SET role_menu.is_delete = 0
WHERE role_menu.menu_id IN (2192, 2193)
  AND role_item.role_key = 'merchant_admin'
  AND role_item.status = '0'
  AND role_item.is_delete = 0
  AND role_item.tenant_id != 0;

-- 恢复历史上已经授予商户管理员的用户菜单副本。旧数据中部分已审核所有者只有
-- 用户级授权而没有角色副本，此处只恢复已存在的类目授权记录，不向其他用户追加新权限。
UPDATE `ry_rental_merchant_user_menu` user_menu
INNER JOIN `ry_rental_merchant_user_role` user_role
    ON user_role.tenant_id = user_menu.tenant_id
   AND user_role.member_id = user_menu.member_id
   AND user_role.merchant_user_id = user_menu.merchant_user_id
INNER JOIN `ry_rental_merchant_role` role_item
    ON role_item.tenant_id = user_role.tenant_id
   AND role_item.member_id = user_role.member_id
   AND role_item.role_id = user_role.role_id
SET user_menu.is_delete = 0
WHERE user_menu.menu_id IN (2192, 2193)
  AND role_item.role_key = 'merchant_admin'
  AND role_item.status = '0'
  AND role_item.is_delete = 0
  AND user_role.is_delete = 0
  AND user_menu.tenant_id != 0;

UPDATE `ry_rental_merchant_user_menu` user_menu
INNER JOIN `ry_rental_merchant_user` merchant_user
    ON merchant_user.tenant_id = user_menu.tenant_id
   AND merchant_user.member_id = user_menu.member_id
   AND merchant_user.merchant_user_id = user_menu.merchant_user_id
INNER JOIN `ry_rental_user_member` user_member
    ON user_member.tenant_id = user_menu.tenant_id
   AND user_member.member_id = user_menu.member_id
   AND user_member.merchant_user_id = user_menu.merchant_user_id
INNER JOIN `ry_rental_member` member_item
    ON member_item.tenant_id = user_menu.tenant_id
   AND member_item.member_id = user_menu.member_id
SET user_menu.is_delete = 0
WHERE user_menu.menu_id IN (2192, 2193)
  AND user_member.member_role = 'owner'
  AND user_member.status = '0'
  AND user_member.is_delete = 0
  AND member_item.auth_status = 'verified'
  AND member_item.status = '0'
  AND member_item.is_delete = 0
  AND merchant_user.status = '0'
  AND merchant_user.is_delete = 0
  AND user_menu.tenant_id != 0;
