-- 统一商户业务日志唯一事实来源。
-- ry_rental_audit_log 已经具备租户、商户、操作人、业务模块、操作对象和前后 JSON 快照字段，
-- 与平台请求级 sys_oper_log 的职责边界清晰；商户页面及所有业务记录器均以该表为准。
-- 之前创建的 ry_rental_merchant_business_audit_log 仅作为历史重复表保留，按项目规则软删除，禁止继续读写。
USE `ry_rental`;

UPDATE `ry_rental_merchant_business_audit_log`
SET `is_delete` = 1
WHERE `is_delete` = 0;

ALTER TABLE `ry_rental_audit_log`
    COMMENT = '租赁域统一业务审计日志（商户业务日志页面事实来源）';

ALTER TABLE `ry_rental_merchant_business_audit_log`
    COMMENT = '历史重复商户业务审计表（已停用，商户日志读取 ry_rental_audit_log）';
