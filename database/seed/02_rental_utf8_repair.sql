-- 租赁系统 UTF-8 数据修复脚本
-- 说明：用于修复早期初始化时未显式指定 MySQL 客户端字符集导致的菜单、字典中文乱码。

SET NAMES utf8mb4;

SET @add_client_type_sql = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE sys_menu ADD COLUMN client_type VARCHAR(20) NOT NULL DEFAULT ''platform'' COMMENT ''客户端类型：platform平台后台 merchant商家端 all共用'' AFTER perms',
    'SELECT 1'
  )
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'sys_menu'
    AND column_name = 'client_type'
);
PREPARE add_client_type_stmt FROM @add_client_type_sql;
EXECUTE add_client_type_stmt;
DEALLOCATE PREPARE add_client_type_stmt;

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
  WHEN 2007 THEN '商户用户'
  WHEN 2008 THEN '商户用户查询'
  WHEN 2009 THEN '商户用户新增'
  WHEN 2010 THEN '商户用户修改'
  WHEN 2011 THEN '商户用户删除'
  WHEN 2012 THEN '商户用户重置密码'
  WHEN 2020 THEN '商户权限'
  WHEN 2021 THEN '商户权限查询'
  WHEN 2022 THEN '商户权限新增'
  WHEN 2023 THEN '商户权限修改'
  WHEN 2024 THEN '商户权限删除'
  ELSE menu_name
END,
remark = CASE menu_id
  WHEN 2000 THEN '租赁商户后台管理目录'
  WHEN 2001 THEN '租赁商户主体管理'
  WHEN 2007 THEN '平台商户用户管理'
  WHEN 2020 THEN '平台商户角色权限管理'
  ELSE remark
END
WHERE menu_id BETWEEN 2000 AND 2024;

UPDATE sys_menu SET client_type = 'platform' WHERE client_type IS NULL OR client_type = '';
UPDATE sys_menu SET client_type = 'platform' WHERE menu_id BETWEEN 2000 AND 2024;
UPDATE sys_menu SET client_type = 'merchant' WHERE menu_id BETWEEN 2100 AND 2170;

UPDATE `ry_rental`.`ry_rental_merchant_menu`
SET menu_name = CASE menu_id
  WHEN 2100 THEN '商家工作台'
  WHEN 2110 THEN '订单中心'
  WHEN 2120 THEN '仓库库存'
  WHEN 2130 THEN '商品资料'
  WHEN 2140 THEN '物流归还'
  WHEN 2150 THEN '联盟协作'
  WHEN 2160 THEN '周转市场'
  WHEN 2170 THEN '商家设置'
  WHEN 2180 THEN '角色权限'
  WHEN 2181 THEN '角色查询'
  WHEN 2182 THEN '角色新增'
  WHEN 2183 THEN '角色修改'
  WHEN 2184 THEN '角色删除'
  WHEN 2190 THEN '操作日志'
  WHEN 2191 THEN '日志导出'
  ELSE menu_name
END,
remark = CASE menu_id
  WHEN 2100 THEN '商家工作台概览'
  WHEN 2110 THEN '商家订单中心'
  WHEN 2120 THEN '商家仓库库存'
  WHEN 2130 THEN '商家商品资料'
  WHEN 2140 THEN '商家物流归还'
  WHEN 2150 THEN '商家联盟协作'
  WHEN 2160 THEN '商家周转市场'
  WHEN 2170 THEN '商家设置'
  WHEN 2180 THEN '商户角色与菜单权限'
  WHEN 2181 THEN '商户角色查询'
  WHEN 2182 THEN '商户角色新增'
  WHEN 2183 THEN '商户角色修改'
  WHEN 2184 THEN '商户角色删除'
  WHEN 2190 THEN '当前商户操作日志'
  WHEN 2191 THEN '导出当前商户操作日志'
  ELSE remark
END
WHERE menu_id BETWEEN 2100 AND 2191;

UPDATE `ry_rental`.`ry_rental_merchant_role`
SET role_name = CASE role_key
  WHEN 'merchant_admin' THEN '商户管理员'
  ELSE role_name
END,
remark = CASE role_key
  WHEN 'merchant_admin' THEN '商户默认管理员角色'
  ELSE remark
END
WHERE role_key = 'merchant_admin';

UPDATE `ry_rental`.`ry_rental_member`
SET legal_subject_name = CASE member_id
  WHEN 1 THEN '迁渃演示商户'
  ELSE legal_subject_name
END,
nickname = CASE member_id
  WHEN 1 THEN '演示商户'
  ELSE nickname
END,
contact_name = CASE member_id
  WHEN 1 THEN '商家管理员'
  ELSE contact_name
END,
remark = CASE member_id
  WHEN 1 THEN '本地开发商家端演示商户'
  ELSE remark
END
WHERE member_id = 1;

UPDATE `ry_rental`.`ry_rental_merchant_user`
SET nick_name = CASE merchant_user_id
  WHEN 100000 THEN '商家管理员'
  ELSE nick_name
END,
remark = CASE merchant_user_id
  WHEN 100000 THEN '本地开发商家端账号'
  ELSE remark
END
WHERE merchant_user_id = 100000;
