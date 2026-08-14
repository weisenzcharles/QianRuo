-- 商户端业务日志不再读取历史请求级日志表。
-- 请求方式、请求地址、控制器方法等通用请求日志统一保留在 ry-cloud.sys_oper_log；
-- 商户业务日志最终统一写入 ry_rental.ry_rental_audit_log。
-- 旧表不物理删除，按项目软删除规则停用全部活动记录，避免旧数据继续被误当成商户业务日志来源。
USE `ry_rental`;

UPDATE `ry_rental_merchant_oper_log`
SET `is_delete` = 1
WHERE `is_delete` = 0;

ALTER TABLE `ry_rental_merchant_oper_log`
    COMMENT = '历史商户请求级日志停用表（平台请求日志在 ry-cloud.sys_oper_log）';
