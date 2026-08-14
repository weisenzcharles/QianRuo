-- 历史迁移说明：该版本曾试图把商户业务日志改为独立表。
-- 经重新梳理，ry_rental_audit_log 才是项目原有的统一业务审计事实表；最终以 V20260814_006 为准。
-- 具体回迁和最终结构由 V20260814_001__restore_merchant_business_audit_source.sql 完成。
USE `ry_rental`;
