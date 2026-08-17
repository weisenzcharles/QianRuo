-- 全库软删除前置迁移。
-- 本脚本必须先于所有引用 is_delete 的业务迁移执行；迁移完成后不再保留 del_flag。

USE `ry-cloud`;

DELIMITER $$

DROP PROCEDURE IF EXISTS `qianruo_drop_index_if_exists`$$
CREATE PROCEDURE `qianruo_drop_index_if_exists`(
    IN schema_name_value VARCHAR(64),
    IN table_name_value VARCHAR(64),
    IN index_name_value VARCHAR(64)
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = schema_name_value
          AND table_name = table_name_value
          AND index_name = index_name_value
    ) THEN
        SET @drop_index_sql = CONCAT(
            'ALTER TABLE `', schema_name_value, '`.`', table_name_value,
            '` DROP INDEX `', index_name_value, '`'
        );
        PREPARE drop_index_statement FROM @drop_index_sql;
        EXECUTE drop_index_statement;
        DEALLOCATE PREPARE drop_index_statement;
    END IF;
END$$

DROP PROCEDURE IF EXISTS `qianruo_add_index_if_missing`$$
CREATE PROCEDURE `qianruo_add_index_if_missing`(
    IN schema_name_value VARCHAR(64),
    IN table_name_value VARCHAR(64),
    IN index_name_value VARCHAR(64),
    IN index_definition_value VARCHAR(1000)
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = schema_name_value
          AND table_name = table_name_value
          AND table_type = 'BASE TABLE'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = schema_name_value
          AND table_name = table_name_value
          AND index_name = index_name_value
    ) THEN
        SET @add_index_sql = CONCAT(
            'ALTER TABLE `', schema_name_value, '`.`', table_name_value,
            '` ADD ', index_definition_value
        );
        PREPARE add_index_statement FROM @add_index_sql;
        EXECUTE add_index_statement;
        DEALLOCATE PREPARE add_index_statement;
    END IF;
END$$

DROP PROCEDURE IF EXISTS `qianruo_migrate_isdeleted`$$
CREATE PROCEDURE `qianruo_migrate_isdeleted`()
BEGIN
    DECLARE finished INT DEFAULT 0;
    DECLARE current_schema VARCHAR(64);
    DECLARE current_table VARCHAR(64);
    DECLARE has_isdeleted INT DEFAULT 0;
    DECLARE has_del_flag INT DEFAULT 0;
    DECLARE needs_normalize INT DEFAULT 0;
    DECLARE application_tables CURSOR FOR
        SELECT table_schema, table_name
        FROM information_schema.tables
        WHERE table_schema IN ('ry-cloud', 'ry_rental')
          AND table_type = 'BASE TABLE'
        ORDER BY table_schema, table_name;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;

    OPEN application_tables;
    migrate_loop: LOOP
        FETCH application_tables INTO current_schema, current_table;
        IF finished = 1 THEN
            LEAVE migrate_loop;
        END IF;

        SELECT COUNT(1)
        INTO has_isdeleted
        FROM information_schema.columns
        WHERE table_schema = current_schema
          AND table_name = current_table
          AND column_name = 'is_delete';

        IF has_isdeleted = 0 THEN
            SET @add_isdeleted_sql = CONCAT(
                'ALTER TABLE `', current_schema, '`.`', current_table,
                '` ADD COLUMN `is_delete` TINYINT(1) NOT NULL DEFAULT 0 ',
                'COMMENT ''软删除标识：0正常 1删除'''
            );
            PREPARE add_isdeleted_statement FROM @add_isdeleted_sql;
            EXECUTE add_isdeleted_statement;
            DEALLOCATE PREPARE add_isdeleted_statement;
        END IF;

        SELECT COUNT(1)
        INTO has_del_flag
        FROM information_schema.columns
        WHERE table_schema = current_schema
          AND table_name = current_table
          AND column_name = 'del_flag';

        IF has_del_flag > 0 THEN
            SET @backfill_isdeleted_sql = CONCAT(
                'UPDATE `', current_schema, '`.`', current_table,
                '` SET `is_delete` = CASE ',
                'WHEN CAST(COALESCE(`del_flag`, ''0'') AS CHAR) IN (''1'', ''2'') ',
                'OR COALESCE(`is_delete`, 0) <> 0 THEN 1 ELSE 0 END'
            );
            PREPARE backfill_isdeleted_statement FROM @backfill_isdeleted_sql;
            EXECUTE backfill_isdeleted_statement;
            DEALLOCATE PREPARE backfill_isdeleted_statement;

            SET @drop_del_flag_sql = CONCAT(
                'ALTER TABLE `', current_schema, '`.`', current_table,
                '` DROP COLUMN `del_flag`'
            );
            PREPARE drop_del_flag_statement FROM @drop_del_flag_sql;
            EXECUTE drop_del_flag_statement;
            DEALLOCATE PREPARE drop_del_flag_statement;
        END IF;

        -- 部署脚本会重复执行幂等迁移；没有新增字段或遗留 del_flag 时跳过全表 UPDATE/ALTER，
        -- 避免每次部署重复写 binlog 并长时间锁表。
        SET needs_normalize = CASE WHEN has_isdeleted = 0 OR has_del_flag > 0 THEN 1 ELSE 0 END;
        IF needs_normalize = 1 THEN
            SET @normalize_isdeleted_sql = CONCAT(
                'UPDATE `', current_schema, '`.`', current_table,
                '` SET `is_delete` = CASE WHEN COALESCE(`is_delete`, 0) = 0 THEN 0 ELSE 1 END'
            );
            PREPARE normalize_isdeleted_statement FROM @normalize_isdeleted_sql;
            EXECUTE normalize_isdeleted_statement;
            DEALLOCATE PREPARE normalize_isdeleted_statement;

            SET @normalize_isdeleted_type_sql = CONCAT(
                'ALTER TABLE `', current_schema, '`.`', current_table,
                '` MODIFY COLUMN `is_delete` TINYINT(1) NOT NULL DEFAULT 0 ',
                'COMMENT ''软删除标识：0正常 1删除'''
            );
            PREPARE normalize_isdeleted_type_statement FROM @normalize_isdeleted_type_sql;
            EXECUTE normalize_isdeleted_type_statement;
            DEALLOCATE PREPARE normalize_isdeleted_type_statement;
        END IF;
    END LOOP;
    CLOSE application_tables;
END$$

CALL `qianruo_drop_index_if_exists`('ry_rental', 'ry_rental_merchant_user', 'idx_rental_merchant_user_status')$$
CALL `qianruo_drop_index_if_exists`('ry_rental', 'ry_rental_member', 'uk_rental_member_contact_phone')$$
CALL `qianruo_drop_index_if_exists`('ry_rental', 'ry_rental_merchant_role', 'uk_rental_merchant_role_name')$$
CALL `qianruo_drop_index_if_exists`('ry_rental', 'ry_rental_merchant_role', 'uk_rental_merchant_role_key')$$

CALL `qianruo_migrate_isdeleted`()$$

CALL `qianruo_add_index_if_missing`(
    'ry_rental', 'ry_rental_merchant_user', 'idx_rental_merchant_user_status',
    'INDEX `idx_rental_merchant_user_status` (`status`, `is_delete`)'
)$$
CALL `qianruo_add_index_if_missing`(
    'ry_rental', 'ry_rental_merchant_role', 'uk_rental_merchant_role_name',
    'UNIQUE INDEX `uk_rental_merchant_role_name` (`member_id`, `role_name`, `is_delete`)'
)$$
CALL `qianruo_add_index_if_missing`(
    'ry_rental', 'ry_rental_merchant_role', 'uk_rental_merchant_role_key',
    'UNIQUE INDEX `uk_rental_merchant_role_key` (`member_id`, `role_key`, `is_delete`)'
)$$

DROP PROCEDURE IF EXISTS `qianruo_migrate_isdeleted`$$
DROP PROCEDURE IF EXISTS `qianruo_add_index_if_missing`$$
DROP PROCEDURE IF EXISTS `qianruo_drop_index_if_exists`$$

DELIMITER ;
