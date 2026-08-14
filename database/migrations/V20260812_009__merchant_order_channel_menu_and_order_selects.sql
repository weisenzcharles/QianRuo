-- 商户端订单渠道菜单归属店铺；订单渠道和启用仓库由订单页面统一以下拉方式选择。
-- 菜单权限编码保持不变，避免已授权商户用户失去访问权限。

USE `ry_rental`;

UPDATE `ry_rental_merchant_menu`
SET parent_id = 2110,
    order_num = 6,
    update_by = 'migration',
    update_time = NOW(),
    remark = '商户订单渠道管理，归属于店铺菜单'
WHERE menu_id = 2200
  AND is_delete = 0;

UPDATE `ry_rental_merchant_menu`
SET parent_id = 2110,
    order_num = 6,
    update_by = 'migration',
    update_time = NOW(),
    remark = '商户订单渠道管理，归属于店铺菜单'
WHERE menu_name = '订单渠道'
  AND path = 'merchant/order-channel'
  AND is_delete = 0;
