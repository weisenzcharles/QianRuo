-- 历史迁移说明：该版本曾临时固化独立审计表为商户日志来源。
-- 当前最终规则由 V20260814_006 统一到 ry_rental_audit_log，平台请求级日志仍写入 ry-cloud.sys_oper_log。
USE `ry_rental`;

ALTER TABLE `ry_rental_merchant_business_audit_log`
    COMMENT = '商户业务审计日志（商户端业务日志唯一数据源）';

ALTER TABLE `ry_rental_audit_log`
    COMMENT = '租赁域通用审计日志（不作为商户业务日志页面数据源）';

ALTER TABLE `ry_rental_merchant_oper_log`
    COMMENT = '商户请求级日志历史留存表（请求级日志由平台 sys_oper_log 保存）';
