USE `ry_rental`;

-- A merchant account belongs to exactly one tenant/member pair. Disable stale
-- bindings left by earlier fixed-ID seed data.
UPDATE `ry_rental_user_member` um
INNER JOIN `ry_rental_merchant_user` u
    ON u.merchant_user_id = um.merchant_user_id
SET um.status = '1', um.del_flag = '2', um.update_by = 'migration', um.update_time = NOW()
WHERE um.del_flag = '0'
  AND (um.tenant_id <> u.tenant_id OR um.member_id <> u.member_id);

UPDATE `ry_rental_user_member` um
INNER JOIN `ry_rental_merchant_user` u
    ON u.merchant_user_id = um.merchant_user_id
INNER JOIN `ry_rental_member` m
    ON m.tenant_id = u.tenant_id AND m.member_id = u.member_id
SET um.status = '0', um.del_flag = '0', um.update_by = 'migration', um.update_time = NOW()
WHERE um.tenant_id = u.tenant_id
  AND um.member_id = u.member_id
  AND u.status = '0' AND u.del_flag = '0'
  AND m.status = '0' AND m.del_flag = '0';

INSERT INTO `ry_rental_user_member`
    (tenant_id, member_id, merchant_user_id, member_role, warehouse_scope_type,
     sensitive_scope, status, create_by, create_time, remark, del_flag)
SELECT u.tenant_id, u.member_id, u.merchant_user_id,
       CASE WHEN u.phone = m.contact_phone THEN 'owner' ELSE 'staff' END,
       'all', CASE WHEN u.phone = m.contact_phone THEN 'detail' ELSE 'masked' END,
       '0', 'migration', NOW(), '修复商户用户主体绑定', '0'
FROM `ry_rental_merchant_user` u
INNER JOIN `ry_rental_member` m
    ON m.tenant_id = u.tenant_id AND m.member_id = u.member_id
WHERE u.status = '0' AND u.del_flag = '0'
  AND m.status = '0' AND m.del_flag = '0'
  AND NOT EXISTS (
      SELECT 1 FROM `ry_rental_user_member` um
      WHERE um.member_id = u.member_id AND um.merchant_user_id = u.merchant_user_id
  );

DELETE ur
FROM `ry_rental_merchant_user_role` ur
LEFT JOIN `ry_rental_merchant_user` u
    ON u.merchant_user_id = ur.merchant_user_id
WHERE u.merchant_user_id IS NULL
   OR ur.tenant_id <> u.tenant_id
   OR ur.member_id <> u.member_id;

DELETE um
FROM `ry_rental_merchant_user_menu` um
LEFT JOIN `ry_rental_merchant_user` u
    ON u.merchant_user_id = um.merchant_user_id
WHERE u.merchant_user_id IS NULL
   OR um.tenant_id <> u.tenant_id
   OR um.member_id <> u.member_id;

INSERT IGNORE INTO `ry_rental_merchant_user_role`
    (tenant_id, member_id, merchant_user_id, role_id)
SELECT b.tenant_id, b.member_id, b.merchant_user_id, r.role_id
FROM `ry_rental_user_member` b
INNER JOIN `ry_rental_merchant_role` r
    ON r.tenant_id = b.tenant_id AND r.member_id = b.member_id
WHERE b.member_role = 'owner' AND b.status = '0' AND b.del_flag = '0'
  AND r.role_key = 'merchant_admin' AND r.status = '0' AND r.del_flag = '0';

INSERT IGNORE INTO `ry_rental_merchant_user_menu`
    (tenant_id, member_id, merchant_user_id, menu_id)
SELECT ur.tenant_id, ur.member_id, ur.merchant_user_id, rm.menu_id
FROM `ry_rental_merchant_user_role` ur
INNER JOIN `ry_rental_merchant_role` r
    ON r.tenant_id = ur.tenant_id AND r.member_id = ur.member_id AND r.role_id = ur.role_id
INNER JOIN `ry_rental_merchant_role_menu` rm
    ON rm.tenant_id = ur.tenant_id AND rm.member_id = ur.member_id AND rm.role_id = ur.role_id
WHERE r.status = '0' AND r.del_flag = '0';
