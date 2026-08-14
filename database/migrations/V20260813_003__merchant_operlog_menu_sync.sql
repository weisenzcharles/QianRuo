-- 商户端操作日志菜单最终同步：挂载到“用户管理”，清理历史顶级日志菜单。
USE `ry_rental`;

-- 统一保留用户管理下的操作日志页面，避免历史菜单路径和新业务审计页面出现两套入口。
UPDATE `ry_rental_merchant_menu`
SET tenant_id = 0,
    member_id = 0,
    menu_name = '操作日志',
    parent_id = 2116,
    order_num = 2,
    path = 'merchant/user/operlog',
    component = 'merchant/monitor/operlog/index',
    `query` = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'merchant:operlog:list',
    icon = 'log',
    is_delete = 0,
    update_by = 'migration',
    update_time = NOW(),
    remark = '当前商户业务审计日志'
WHERE menu_id = 2118;

-- 复用历史导出权限节点，但将其挂到新的操作日志菜单下，避免新增重复权限编码。
UPDATE `ry_rental_merchant_menu`
SET tenant_id = 0,
    member_id = 0,
    menu_name = '日志导出',
    parent_id = 2118,
    order_num = 1,
    path = 'operlog-export',
    component = '',
    `query` = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '0',
    status = '0',
    perms = 'merchant:operlog:export',
    icon = '#',
    is_delete = 0,
    update_by = 'migration',
    update_time = NOW(),
    remark = '导出当前商户业务审计日志'
WHERE menu_id = 2191;

-- 旧的顶级操作日志菜单只保留历史数据，不再参与商户端菜单树和权限计算。
UPDATE `ry_rental_merchant_menu`
SET is_delete = 1, update_by = 'migration', update_time = NOW(), remark = '历史顶级日志菜单，已迁移到用户管理'
WHERE menu_id = 2190;

UPDATE `ry_rental_merchant_role_menu`
SET is_delete = 1
WHERE menu_id = 2190;

UPDATE `ry_rental_merchant_user_menu`
SET is_delete = 1
WHERE menu_id = 2190;

-- 修正历史角色授权残留的租户上下文，避免导出权限被当前商户的租户条件过滤。
UPDATE `ry_rental_merchant_role_menu` rm
INNER JOIN `ry_rental_merchant_role` r
    ON r.role_id = rm.role_id
SET rm.tenant_id = r.tenant_id,
    rm.member_id = r.member_id,
    rm.is_delete = 0
WHERE r.role_key = 'merchant_admin'
  AND r.status = '0'
  AND r.is_delete = '0'
  AND rm.menu_id IN (2118, 2191);

-- 商户管理员角色默认拥有新的日志页面和导出按钮；普通角色仍由商户管理员单独授权。
INSERT INTO `ry_rental_merchant_role_menu` (tenant_id, member_id, role_id, menu_id, is_delete)
SELECT r.tenant_id, r.member_id, r.role_id, m.menu_id, 0
FROM `ry_rental_merchant_role` r
INNER JOIN `ry_rental_merchant_menu` m
    ON m.menu_id IN (2118, 2191) AND m.tenant_id = 0 AND m.member_id = 0
WHERE r.role_key = 'merchant_admin' AND r.status = '0' AND r.is_delete = '0'
  AND m.status = '0' AND m.is_delete = '0'
ON DUPLICATE KEY UPDATE tenant_id = VALUES(tenant_id), member_id = VALUES(member_id), is_delete = 0;

-- 同步管理员用户的直接菜单副本，确保旧会话刷新后菜单和权限树一致。
INSERT INTO `ry_rental_merchant_user_menu` (tenant_id, member_id, merchant_user_id, menu_id, is_delete)
SELECT ur.tenant_id, ur.member_id, ur.merchant_user_id, rm.menu_id, 0
FROM `ry_rental_merchant_user_role` ur
INNER JOIN `ry_rental_merchant_role` r
    ON r.tenant_id = ur.tenant_id AND r.member_id = ur.member_id AND r.role_id = ur.role_id
INNER JOIN `ry_rental_merchant_role_menu` rm
    ON rm.tenant_id = ur.tenant_id AND rm.member_id = ur.member_id AND rm.role_id = ur.role_id
WHERE r.role_key = 'merchant_admin' AND r.status = '0' AND r.is_delete = '0'
  AND ur.is_delete = '0' AND rm.menu_id IN (2118, 2191) AND rm.is_delete = 0
ON DUPLICATE KEY UPDATE tenant_id = VALUES(tenant_id), member_id = VALUES(member_id), is_delete = 0;
