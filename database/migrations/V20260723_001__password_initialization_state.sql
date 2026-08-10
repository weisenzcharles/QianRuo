-- 用户初始密码状态迁移
-- 约定：历史平台用户默认视为已完成初始化；历史商户用户仅密码为空时需要初始化。

USE `ry-cloud`;

SET @admin_password_state_missing = (
    SELECT COUNT(*) = 0
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'ry-cloud'
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'must_change_password'
);
SET @add_admin_password_state_sql = IF(
        @admin_password_state_missing,
        'ALTER TABLE `sys_user` ADD COLUMN `must_change_password` TINYINT(1) NOT NULL DEFAULT 1 COMMENT ''是否必须修改初始密码：0否 1是'' AFTER `pwd_update_date`',
        'SELECT 1'
);
PREPARE add_admin_password_state_stmt FROM @add_admin_password_state_sql;
EXECUTE add_admin_password_state_stmt;
DEALLOCATE PREPARE add_admin_password_state_stmt;

UPDATE `sys_user`
SET `must_change_password` = 0
WHERE @admin_password_state_missing = 1;

UPDATE `sys_config`
SET `is_delete` = 1
WHERE `config_key` = 'sys.account.initPasswordModify' AND `is_delete` = 0;

USE `ry_rental`;

SET @merchant_password_state_missing = (
    SELECT COUNT(*) = 0
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'ry_rental'
      AND TABLE_NAME = 'ry_rental_merchant_user'
      AND COLUMN_NAME = 'must_change_password'
);
SET @add_merchant_password_state_sql = IF(
        @merchant_password_state_missing,
        'ALTER TABLE `ry_rental_merchant_user` ADD COLUMN `must_change_password` TINYINT(1) NOT NULL DEFAULT 1 COMMENT ''是否必须修改初始密码：0否 1是'' AFTER `pwd_update_date`',
        'SELECT 1'
);
PREPARE add_merchant_password_state_stmt FROM @add_merchant_password_state_sql;
EXECUTE add_merchant_password_state_stmt;
DEALLOCATE PREPARE add_merchant_password_state_stmt;

UPDATE `ry_rental_merchant_user`
SET `must_change_password` = CASE
    WHEN `password` IS NULL OR `password` = '' THEN 1
    ELSE 0
END
WHERE @merchant_password_state_missing = 1;
