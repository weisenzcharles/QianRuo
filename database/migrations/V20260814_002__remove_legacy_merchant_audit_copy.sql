-- 软删除历史版本复制到独立审计表的重复记录。
-- 该迁移执行后，V20260814_006 会继续软删除独立表全部活动行，并统一使用 ry_rental_audit_log。
USE `ry_rental`;

UPDATE `ry_rental_merchant_business_audit_log` target_log
INNER JOIN `ry_rental_audit_log` source_log
    ON source_log.`trace_id` <> ''
   AND source_log.`trace_id` = target_log.`trace_id`
   AND source_log.`actor_type` = 'merchant_user'
SET target_log.`is_delete` = 1
WHERE target_log.`is_delete` = 0;
