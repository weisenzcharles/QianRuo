-- 为现有商户补充本地联调测试数据。
-- 仅写入 ry_business 业务库，不修改平台用户、商户身份、角色或权限数据。
-- 固定业务编码保证脚本可重复执行，不会产生重复测试数据；软删除记录也视为已存在。
USE `ry_business`;

INSERT INTO `ry_business`.`ry_rental_warehouse`
    (tenant_id, member_id, warehouse_code, warehouse_name, contact_name, contact_phone,
     province, city, district, address, status, create_by, create_time, update_by, update_time, remark, is_delete)
SELECT m.tenant_id, m.member_id, v.warehouse_code, v.warehouse_name, v.contact_name, v.contact_phone,
       v.province, v.city, v.district, v.address, 0, 'test-data', NOW(), 'test-data', NOW(),
       '系统生成的本地联调测试数据', 0
FROM (SELECT DISTINCT tenant_id, member_id FROM `ry_rental`.`ry_rental_member` WHERE is_delete = 0) m
JOIN (
    SELECT 'WH-TEST-01' warehouse_code, '测试主仓' warehouse_name, '测试负责人甲' contact_name, '15500000001' contact_phone,
           '浙江省' province, '杭州市' city, '西湖区' district, '测试路 1 号' address
    UNION ALL SELECT 'WH-TEST-02', '测试备用仓', '测试负责人乙', '15500000002', '北京市', '北京市', '朝阳区', '测试街 2 号'
    UNION ALL SELECT 'WH-TEST-03', '测试中转仓', '测试负责人丙', '15500000003', '广东省', '深圳市', '南山区', '测试大道 3 号'
) v
WHERE NOT EXISTS (
    SELECT 1 FROM `ry_business`.`ry_rental_warehouse` w
    WHERE w.member_id = m.member_id AND w.warehouse_code = v.warehouse_code
);

INSERT INTO `ry_business`.`ry_rental_stock_pool`
    (tenant_id, member_id, pool_code, pool_name, resource_type, substitutable_flag, status,
     create_by, create_time, update_by, update_time, remark, is_delete)
SELECT m.tenant_id, m.member_id, v.pool_code, v.pool_name, v.resource_type, 1, 0,
       'test-data', NOW(), 'test-data', NOW(), '系统生成的本地联调测试数据', 0
FROM (SELECT DISTINCT tenant_id, member_id FROM `ry_rental`.`ry_rental_member` WHERE is_delete = 0) m
JOIN (
    SELECT 'POOL-TEST-DEVICE' pool_code, '测试设备资源池' pool_name, 'device' resource_type
    UNION ALL SELECT 'POOL-TEST-ACCESSORY', '测试通用配件资源池', 'accessory'
) v
WHERE NOT EXISTS (
    SELECT 1 FROM `ry_business`.`ry_rental_stock_pool` p
    WHERE p.member_id = m.member_id AND p.pool_code = v.pool_code
);

INSERT INTO `ry_business`.`ry_rental_product`
    (tenant_id, member_id, class_id, model_id, stock_pool_id, product_code, product_name,
     category_level1, category_level2, category_level3, brand, model_name, memory_spec,
     product_type, stock_mode, purchase_cost, sale_price, frontend_visible, stats_enabled,
     sort_no, status, create_by, create_time, update_by, update_time, remark, is_delete)
SELECT m.tenant_id, m.member_id, 1, 3, p.pool_id, v.product_code, v.product_name,
       '摄影器材', '手持云台', NULL, '大疆', v.model_name, v.memory_spec,
       v.product_type, v.stock_mode, v.purchase_cost, v.sale_price, 1, 1, v.sort_no, '0',
       'test-data', NOW(), 'test-data', NOW(), '系统生成的本地联调测试数据', 0
FROM (SELECT DISTINCT tenant_id, member_id FROM `ry_rental`.`ry_rental_member` WHERE is_delete = 0) m
JOIN (
    SELECT 'P-TEST-P3' product_code, 'Pocket 3 测试商品' product_name, 'Pocket 3' model_name, '标准版' memory_spec,
           'device' product_type, 'sn' stock_mode, 1800.00 purchase_cost, 99.00 sale_price, 1 sort_no, 'POOL-TEST-DEVICE' pool_code
    UNION ALL SELECT 'P-TEST-P4', 'Pocket 4 测试商品', 'Pocket 4', '标准版', 'device', 'sn', 2200.00, 119.00, 2, 'POOL-TEST-DEVICE'
    UNION ALL SELECT 'P-TEST-HANDLE', '续航手柄测试商品', '通用续航手柄', '通用版', 'accessory', 'quantity', 300.00, 29.00, 3, 'POOL-TEST-ACCESSORY'
    UNION ALL SELECT 'P-TEST-BAG', '收纳包测试商品', '通用收纳包', '标准版', 'accessory', 'quantity', 80.00, 9.00, 4, 'POOL-TEST-ACCESSORY'
) v
JOIN `ry_business`.`ry_rental_stock_pool` p
  ON p.tenant_id = m.tenant_id AND p.member_id = m.member_id AND p.pool_code = v.pool_code AND p.is_delete = 0
WHERE NOT EXISTS (
    SELECT 1 FROM `ry_business`.`ry_rental_product` x
    WHERE x.member_id = m.member_id AND x.product_code = v.product_code
);
