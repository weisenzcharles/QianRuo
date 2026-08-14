USE `ry_business`;

-- 为手机号 13552259921 所属商户生成可重复执行的五种订单状态测试数据。
-- 订单仅写入 ry_business，所有记录固定使用 tenant_id=1、member_id=1，并通过订单号保证幂等。
SET @merchant_channel_id = COALESCE(
    (
        SELECT channel_id
        FROM `ry_rental_order_channel`
        WHERE tenant_id = 1 AND member_id = 1 AND scope_type = 'merchant'
          AND status = 0 AND is_delete = 0
        ORDER BY sort_no, channel_id
        LIMIT 1
    ),
    (
        SELECT channel_id
        FROM `ry_rental_order_channel`
        WHERE scope_type = 'platform' AND status = 0 AND is_delete = 0
        ORDER BY sort_no, channel_id
        LIMIT 1
    )
);
SET @platform_channel_id = COALESCE(
    (
        SELECT channel_id
        FROM `ry_rental_order_channel`
        WHERE scope_type = 'platform' AND status = 0 AND is_delete = 0
        ORDER BY sort_no, channel_id
        LIMIT 1
    ),
    @merchant_channel_id
);
SET @test_merchant_user_id = (
    SELECT merchant_user_id
    FROM `ry_rental`.`ry_rental_merchant_user`
    WHERE tenant_id = 1 AND member_id = 1 AND phone = '13552259921' AND is_delete = 0
    ORDER BY merchant_user_id
    LIMIT 1
);

INSERT INTO `ry_rental_order`
    (tenant_id, member_id, order_no, external_order_no, channel_id, shop_name,
     customer_name, customer_phone, customer_address, warehouse_id, return_warehouse_id,
     lease_start_date, lease_end_date, ship_due_time, return_due_time, rent_amount,
     deposit_amount, order_status, pay_status, time_effect_status, weather_risk_flag,
     priority_flag, source_text_snapshot, create_by, create_time, update_by, update_time,
     remark, is_delete)
SELECT 1, 1, v.order_no, v.external_order_no,
       CASE WHEN v.channel_scope = 'platform' THEN @platform_channel_id ELSE @merchant_channel_id END,
       v.shop_name,
       v.customer_name, v.customer_phone, v.customer_address, w.warehouse_id, w.warehouse_id,
       v.lease_start_date, v.lease_end_date, v.ship_due_time, v.return_due_time, v.rent_amount,
       v.deposit_amount, v.order_status, v.pay_status, v.time_effect_status, v.weather_risk_flag,
       v.priority_flag, v.source_text_snapshot, @test_merchant_user_id, NOW(), @test_merchant_user_id, NOW(),
       '13552259921 商户订单联调测试数据', 0
FROM (
    SELECT 'TEST-ORDER-001' order_no, 'TEST-EXT-001' external_order_no, 'merchant' channel_scope, '商户自有渠道' shop_name,
           '待发货客户' customer_name, '13800000001' customer_phone, '杭州市西湖区测试路1号' customer_address,
           CURRENT_DATE lease_start_date, DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY) lease_end_date,
           DATE_ADD(NOW(), INTERVAL 1 DAY) ship_due_time, DATE_ADD(NOW(), INTERVAL 8 DAY) return_due_time,
           99.00 rent_amount, 500.00 deposit_amount, 'pending_ship' order_status, 'paid' pay_status,
           'normal' time_effect_status, 0 weather_risk_flag, 1 priority_flag, '待发货测试订单' source_text_snapshot
    UNION ALL SELECT 'TEST-ORDER-002', 'TEST-EXT-002', 'merchant', '商户自有渠道', '配送中客户', '13800000002', '北京市朝阳区测试街2号',
           DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 6 DAY),
           119.00, 600.00, 'shipping', 'paid', 'normal', 0, 0, '配送中测试订单'
    UNION ALL SELECT 'TEST-ORDER-003', 'TEST-EXT-003', 'platform', '租仓联盟渠道', '租用中客户', '13800000003', '深圳市南山区测试大道3号',
           DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), DATE_ADD(CURRENT_DATE, INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 4 DAY),
           149.00, 800.00, 'renting', 'paid', 'normal', 1, 0, '租用中并有天气风险测试订单'
    UNION ALL SELECT 'TEST-ORDER-004', 'TEST-EXT-004', 'merchant', '商户自有渠道', '待归还客户', '13800000004', '上海市浦东新区测试路4号',
           DATE_SUB(CURRENT_DATE, INTERVAL 10 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY),
           199.00, 1000.00, 'overdue_return', 'paid', 'overdue', 0, 1, '逾期归还测试订单'
    UNION ALL SELECT 'TEST-ORDER-005', 'TEST-EXT-005', 'merchant', '商户自有渠道', '已完成客户', '13800000005', '广州市天河区测试路5号',
           DATE_SUB(CURRENT_DATE, INTERVAL 20 DAY), DATE_SUB(CURRENT_DATE, INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY),
           89.00, 400.00, 'completed', 'paid', 'normal', 0, 0, '已完成测试订单'
) v
JOIN (SELECT MIN(warehouse_id) warehouse_id FROM `ry_rental_warehouse` WHERE tenant_id=1 AND member_id=1 AND is_delete=0) w
WHERE w.warehouse_id IS NOT NULL
  AND @merchant_channel_id IS NOT NULL
  AND @platform_channel_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `ry_rental_order` o WHERE o.member_id=1 AND o.order_no=v.order_no AND o.is_delete=0);

-- 为五条订单补充商品明细，优先使用该商户的第一个有效商品。
INSERT INTO `ry_rental_order_item`
    (tenant_id, member_id, order_id, product_id, quantity, item_status, create_by, create_time, remark, is_delete)
SELECT 1, 1, o.order_id, p.product_id, 1, o.order_status, @test_merchant_user_id, NOW(), '订单测试明细', 0
FROM `ry_rental_order` o
JOIN (SELECT MIN(product_id) product_id FROM `ry_rental_product` WHERE tenant_id=1 AND member_id=1 AND is_delete=0) p
WHERE o.member_id=1 AND o.order_no LIKE 'TEST-ORDER-%' AND o.is_delete=0 AND p.product_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `ry_rental_order_item` i WHERE i.order_id=o.order_id AND i.is_delete=0);
