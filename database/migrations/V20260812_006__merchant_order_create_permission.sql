-- 为商户订单录单接口补充独立按钮权限，避免新增能力复用查询权限。
USE `ry-cloud`;

INSERT INTO `sys_menu`
(menu_id, parent_id, menu_name, order_num, path, component, is_frame, is_cache, menu_type,
 visible, status, perms, icon, create_by, create_time, remark)
SELECT 2250, 2121, '新增订单', 1, 'permission/2250', '', 1, 0, 'F', '0', '0',
       'merchant:order:add', '', 'migration', NOW(), '商户订单录入权限'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE menu_id = 2250);
