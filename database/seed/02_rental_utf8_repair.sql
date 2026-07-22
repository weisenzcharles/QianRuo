-- 租赁系统 UTF-8 数据修复脚本
-- 说明：用于修复早期初始化时未显式指定 MySQL 客户端字符集导致的菜单、字典中文乱码。

UPDATE sys_dict_type
SET dict_name = CASE dict_type
  WHEN 'rental_order_status' THEN '租赁订单状态'
  WHEN 'rental_asset_status' THEN '租赁SN状态'
  WHEN 'rental_logistics_status' THEN '租赁物流状态'
  WHEN 'rental_market_post_status' THEN '周转市场帖子状态'
  WHEN 'rental_member_type' THEN '租赁商户类型'
  WHEN 'rental_member_auth_status' THEN '租赁商户认证状态'
  WHEN 'rental_vip_level' THEN '租赁会员等级'
  ELSE dict_name
END,
remark = CASE dict_type
  WHEN 'rental_order_status' THEN '租赁业务订单状态'
  WHEN 'rental_asset_status' THEN '租赁设备SN状态'
  WHEN 'rental_logistics_status' THEN '租赁物流路由状态'
  WHEN 'rental_market_post_status' THEN '库存周转市场帖子状态'
  WHEN 'rental_member_type' THEN '租赁商户主体类型'
  WHEN 'rental_member_auth_status' THEN '租赁商户认证审核状态'
  WHEN 'rental_vip_level' THEN '租赁商户会员套餐等级'
  ELSE remark
END
WHERE dict_type IN (
  'rental_order_status',
  'rental_asset_status',
  'rental_logistics_status',
  'rental_market_post_status',
  'rental_member_type',
  'rental_member_auth_status',
  'rental_vip_level'
);

UPDATE sys_dict_data
SET dict_label = CASE CONCAT(dict_type, ':', dict_value)
  WHEN 'rental_member_type:personal' THEN '个人'
  WHEN 'rental_member_type:enterprise' THEN '企业'
  WHEN 'rental_member_auth_status:unverified' THEN '未认证'
  WHEN 'rental_member_auth_status:pending' THEN '待审核'
  WHEN 'rental_member_auth_status:verified' THEN '已认证'
  WHEN 'rental_member_auth_status:rejected' THEN '已驳回'
  WHEN 'rental_vip_level:free' THEN '免费版'
  WHEN 'rental_vip_level:standard' THEN '标准版'
  WHEN 'rental_vip_level:pro' THEN '专业版'
  WHEN 'rental_vip_level:enterprise' THEN '旗舰版'
  WHEN 'rental_order_status:pending_ship' THEN '待发货'
  WHEN 'rental_order_status:prepared' THEN '已备货'
  WHEN 'rental_order_status:ship_in_transit' THEN '发货在途'
  WHEN 'rental_order_status:renting' THEN '租用中'
  WHEN 'rental_order_status:returning' THEN '归还中'
  WHEN 'rental_order_status:after_sales' THEN '售后中'
  WHEN 'rental_order_status:completed' THEN '已完结'
  WHEN 'rental_order_status:cancelled' THEN '已取消'
  WHEN 'rental_asset_status:available' THEN '在库可售'
  WHEN 'rental_asset_status:reserved' THEN '已占用'
  WHEN 'rental_asset_status:renting' THEN '租用中'
  WHEN 'rental_asset_status:return_in_transit' THEN '归还在途'
  WHEN 'rental_asset_status:repairing' THEN '报修'
  WHEN 'rental_asset_status:sold' THEN '出售出库'
  WHEN 'rental_logistics_status:pending' THEN '待下单'
  WHEN 'rental_logistics_status:waiting_pickup' THEN '待揽收'
  WHEN 'rental_logistics_status:in_transit' THEN '运输中'
  WHEN 'rental_logistics_status:signed' THEN '已签收'
  WHEN 'rental_logistics_status:abnormal' THEN '异常'
  WHEN 'rental_market_post_status:draft' THEN '草稿'
  WHEN 'rental_market_post_status:published' THEN '已发布'
  WHEN 'rental_market_post_status:offline' THEN '已下架'
  WHEN 'rental_market_post_status:reported' THEN '举报中'
  WHEN 'rental_market_post_status:violation_offline' THEN '违规下架'
  ELSE dict_label
END
WHERE dict_type LIKE 'rental_%';

UPDATE sys_menu
SET menu_name = CASE menu_id
  WHEN 2000 THEN '商户后台'
  WHEN 2001 THEN '商户管理'
  WHEN 2002 THEN '商户查询'
  WHEN 2003 THEN '商户新增'
  WHEN 2004 THEN '商户修改'
  WHEN 2005 THEN '商户删除'
  WHEN 2006 THEN '商户导出'
  ELSE menu_name
END,
remark = CASE menu_id
  WHEN 2000 THEN '租赁商户后台管理目录'
  WHEN 2001 THEN '租赁商户主体管理'
  ELSE remark
END
WHERE menu_id BETWEEN 2000 AND 2006;
