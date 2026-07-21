-- 租赁系统基础字典种子数据
-- 说明：适用于 RuoYi sys_dict_type/sys_dict_data 表。
-- 执行前请确认当前数据库已包含 RuoYi 原生系统表。

-- 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '租赁订单状态', 'rental_order_status', '0', 'system', NOW(), '租赁业务订单状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'rental_order_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '租赁SN状态', 'rental_asset_status', '0', 'system', NOW(), '租赁设备SN状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'rental_asset_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '租赁物流状态', 'rental_logistics_status', '0', 'system', NOW(), '租赁物流路由状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'rental_logistics_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '周转市场帖子状态', 'rental_market_post_status', '0', 'system', NOW(), '库存周转市场帖子状态'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'rental_market_post_status');

-- 订单状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '待发货', 'pending_ship', 'rental_order_status', '', 'default', 'Y', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='pending_ship');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已备货', 'prepared', 'rental_order_status', '', 'primary', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='prepared');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '发货在途', 'ship_in_transit', 'rental_order_status', '', 'primary', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='ship_in_transit');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '租用中', 'renting', 'rental_order_status', '', 'success', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='renting');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '归还中', 'returning', 'rental_order_status', '', 'warning', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='returning');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 6, '售后中', 'after_sales', 'rental_order_status', '', 'danger', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='after_sales');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 7, '已完结', 'completed', 'rental_order_status', '', 'success', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='completed');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 8, '已取消', 'cancelled', 'rental_order_status', '', 'info', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_order_status' AND dict_value='cancelled');

-- SN状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '在库可售', 'available', 'rental_asset_status', '', 'success', 'Y', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_asset_status' AND dict_value='available');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已占用', 'reserved', 'rental_asset_status', '', 'warning', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_asset_status' AND dict_value='reserved');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '租用中', 'renting', 'rental_asset_status', '', 'primary', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_asset_status' AND dict_value='renting');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '归还在途', 'return_in_transit', 'rental_asset_status', '', 'warning', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_asset_status' AND dict_value='return_in_transit');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '报修', 'repairing', 'rental_asset_status', '', 'danger', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_asset_status' AND dict_value='repairing');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 6, '出售出库', 'sold', 'rental_asset_status', '', 'info', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_asset_status' AND dict_value='sold');

-- 物流状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '待下单', 'pending', 'rental_logistics_status', '', 'default', 'Y', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_logistics_status' AND dict_value='pending');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '待揽收', 'waiting_pickup', 'rental_logistics_status', '', 'warning', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_logistics_status' AND dict_value='waiting_pickup');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '运输中', 'in_transit', 'rental_logistics_status', '', 'primary', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_logistics_status' AND dict_value='in_transit');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '已签收', 'signed', 'rental_logistics_status', '', 'success', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_logistics_status' AND dict_value='signed');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '异常', 'abnormal', 'rental_logistics_status', '', 'danger', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_logistics_status' AND dict_value='abnormal');

-- 周转市场帖子状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '草稿', 'draft', 'rental_market_post_status', '', 'default', 'Y', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_market_post_status' AND dict_value='draft');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已发布', 'published', 'rental_market_post_status', '', 'success', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_market_post_status' AND dict_value='published');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '已下架', 'offline', 'rental_market_post_status', '', 'info', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_market_post_status' AND dict_value='offline');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '举报中', 'reported', 'rental_market_post_status', '', 'warning', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_market_post_status' AND dict_value='reported');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '违规下架', 'violation_offline', 'rental_market_post_status', '', 'danger', 'N', '0', 'system', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='rental_market_post_status' AND dict_value='violation_offline');
