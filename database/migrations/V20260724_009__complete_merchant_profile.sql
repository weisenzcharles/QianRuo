USE `ry_rental`;

SET SESSION group_concat_max_len = 32768;

SET @member_profile_columns = (
    SELECT GROUP_CONCAT(required_columns.column_sql ORDER BY required_columns.sort_order SEPARATOR ', ')
    FROM (
        SELECT 1 sort_order, 'service_phone' column_name,
               'ADD COLUMN `service_phone` VARCHAR(32) DEFAULT NULL COMMENT ''客服电话''' column_sql
        UNION ALL SELECT 2, 'contact_email',
               'ADD COLUMN `contact_email` VARCHAR(128) DEFAULT NULL COMMENT ''联系邮箱'''
        UNION ALL SELECT 3, 'alipay_real_name',
               'ADD COLUMN `alipay_real_name` VARCHAR(64) DEFAULT NULL COMMENT ''支付宝实名'''
        UNION ALL SELECT 4, 'alipay_account',
               'ADD COLUMN `alipay_account` VARCHAR(128) DEFAULT NULL COMMENT ''支付宝账号'''
        UNION ALL SELECT 5, 'store_logo_url',
               'ADD COLUMN `store_logo_url` VARCHAR(500) DEFAULT NULL COMMENT ''店铺logo地址'''
        UNION ALL SELECT 6, 'store_description',
               'ADD COLUMN `store_description` VARCHAR(1000) DEFAULT NULL COMMENT ''店铺描述'''
        UNION ALL SELECT 7, 'registered_capital',
               'ADD COLUMN `registered_capital` DECIMAL(18,2) DEFAULT NULL COMMENT ''企业注册资本'''
        UNION ALL SELECT 8, 'business_scope',
               'ADD COLUMN `business_scope` VARCHAR(2000) DEFAULT NULL COMMENT ''营业执照经营范围'''
        UNION ALL SELECT 9, 'license_address',
               'ADD COLUMN `license_address` VARCHAR(500) DEFAULT NULL COMMENT ''营业执照地址'''
        UNION ALL SELECT 10, 'license_valid_from',
               'ADD COLUMN `license_valid_from` DATE DEFAULT NULL COMMENT ''营业执照有效期开始'''
        UNION ALL SELECT 11, 'license_valid_to',
               'ADD COLUMN `license_valid_to` DATE DEFAULT NULL COMMENT ''营业执照有效期结束'''
        UNION ALL SELECT 12, 'license_long_term',
               'ADD COLUMN `license_long_term` TINYINT NOT NULL DEFAULT 0 COMMENT ''营业执照是否长期：0否 1是'''
        UNION ALL SELECT 13, 'license_image_url',
               'ADD COLUMN `license_image_url` VARCHAR(500) DEFAULT NULL COMMENT ''营业执照图片地址'''
        UNION ALL SELECT 14, 'legal_representative_name',
               'ADD COLUMN `legal_representative_name` VARCHAR(64) DEFAULT NULL COMMENT ''法人姓名'''
        UNION ALL SELECT 15, 'legal_representative_id_no',
               'ADD COLUMN `legal_representative_id_no` VARCHAR(64) DEFAULT NULL COMMENT ''法人身份证号'''
        UNION ALL SELECT 16, 'legal_id_front_url',
               'ADD COLUMN `legal_id_front_url` VARCHAR(500) DEFAULT NULL COMMENT ''法人身份证人像面'''
        UNION ALL SELECT 17, 'legal_id_back_url',
               'ADD COLUMN `legal_id_back_url` VARCHAR(500) DEFAULT NULL COMMENT ''法人身份证国徽面'''
        UNION ALL SELECT 18, 'signing_date',
               'ADD COLUMN `signing_date` DATE DEFAULT NULL COMMENT ''签约日期'''
        UNION ALL SELECT 19, 'contract_valid_from',
               'ADD COLUMN `contract_valid_from` DATE DEFAULT NULL COMMENT ''合同有效期开始'''
        UNION ALL SELECT 20, 'contract_valid_to',
               'ADD COLUMN `contract_valid_to` DATE DEFAULT NULL COMMENT ''合同有效期结束'''
        UNION ALL SELECT 21, 'contract_file_url',
               'ADD COLUMN `contract_file_url` VARCHAR(500) DEFAULT NULL COMMENT ''签约合同文件地址'''
        UNION ALL SELECT 22, 'audit_time',
               'ADD COLUMN `audit_time` DATETIME DEFAULT NULL COMMENT ''最后审核时间'''
        UNION ALL SELECT 23, 'audit_remark',
               'ADD COLUMN `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT ''审核备注'''
        UNION ALL SELECT 24, 'freeze_time',
               'ADD COLUMN `freeze_time` DATETIME DEFAULT NULL COMMENT ''冻结时间'''
        UNION ALL SELECT 25, 'freeze_reason',
               'ADD COLUMN `freeze_reason` VARCHAR(500) DEFAULT NULL COMMENT ''冻结原因'''
    ) required_columns
    LEFT JOIN information_schema.COLUMNS existing_columns
      ON existing_columns.TABLE_SCHEMA = 'ry_rental'
     AND existing_columns.TABLE_NAME = 'ry_rental_member'
     AND existing_columns.COLUMN_NAME = required_columns.column_name
    WHERE existing_columns.COLUMN_NAME IS NULL
);

SET @alter_member_profile_sql = IF(
    @member_profile_columns IS NULL,
    'SELECT 1',
    CONCAT('ALTER TABLE `ry_rental_member` ', @member_profile_columns)
);
PREPARE alter_member_profile_stmt FROM @alter_member_profile_sql;
EXECUTE alter_member_profile_stmt;
DEALLOCATE PREPARE alter_member_profile_stmt;

UPDATE `ry_rental_member`
SET audit_time = COALESCE(update_time, create_time)
WHERE auth_status IN ('verified', 'rejected')
  AND audit_time IS NULL;

UPDATE `ry_rental_member`
SET freeze_time = COALESCE(update_time, create_time)
WHERE status = '1'
  AND freeze_time IS NULL;
