-- 订单收件人历史快照增加当前使用标识。
-- 同一订单只允许业务上存在一条 used = 1 的当前地址，其余记录继续保留用于追溯。
USE `ry_business`;

-- MySQL 8.4 不支持 ALTER TABLE ADD COLUMN IF NOT EXISTS，改用元数据判断保证重复执行安全。
SET @used_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = 'ry_business'
      AND table_name = 'ry_rental_order_recipient_history'
      AND column_name = 'used'
);

SET @add_used_column_sql := IF(
    @used_column_exists = 0,
    'ALTER TABLE `ry_business`.`ry_rental_order_recipient_history`
        ADD COLUMN `used` TINYINT(1) NOT NULL DEFAULT 0
            COMMENT ''是否为订单当前使用的地址：1是 0否''
            AFTER `parse_message`',
    'SELECT 1'
);
PREPARE add_used_column_statement FROM @add_used_column_sql;
EXECUTE add_used_column_statement;
DEALLOCATE PREPARE add_used_column_statement;

-- 历史表在增加字段前没有当前地址标识，迁移时将每个订单最新的未删除快照设为当前地址。
UPDATE `ry_business`.`ry_rental_order_recipient_history`
SET used = 0
WHERE is_delete = 0;

UPDATE `ry_business`.`ry_rental_order_recipient_history` history
INNER JOIN (
    SELECT tenant_id, member_id, order_no, MAX(recipient_history_id) AS recipient_history_id
    FROM `ry_business`.`ry_rental_order_recipient_history`
    WHERE is_delete = 0
    GROUP BY tenant_id, member_id, order_no
) latest
    ON latest.recipient_history_id = history.recipient_history_id
   AND latest.tenant_id = history.tenant_id
   AND latest.member_id = history.member_id
   AND latest.order_no = history.order_no
SET history.used = 1
WHERE history.is_delete = 0;
