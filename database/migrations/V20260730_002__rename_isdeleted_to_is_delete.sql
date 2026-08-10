-- 软删除列重命名：isdeleted -> is_delete（保留已上线数据，幂等可重复执行）。
-- 覆盖 ry-cloud 与 ry_rental 两个库的全部表；会员表和产品类目表含依赖软删除列的
-- 生成列，需先删除生成列、重命名、再重建。

USE `ry_rental`;

DELIMITER $$

DROP PROCEDURE IF EXISTS `qianruo_drop_column_if_exists`$$
CREATE PROCEDURE `qianruo_drop_column_if_exists`(
    IN schema_name_value VARCHAR(64),
    IN table_name_value VARCHAR(64),
    IN column_name_value VARCHAR(64)
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = schema_name_value
          AND table_name = table_name_value
          AND column_name = column_name_value
    ) THEN
        SET @drop_col_sql = CONCAT(
            'ALTER TABLE `', schema_name_value, '`.`', table_name_value,
            '` DROP COLUMN `', column_name_value, '`'
        );
        PREPARE drop_col_statement FROM @drop_col_sql;
        EXECUTE drop_col_statement;
        DEALLOCATE PREPARE drop_col_statement;
    END IF;
END$$

DROP PROCEDURE IF EXISTS `qianruo_add_product_class_generated`$$
CREATE PROCEDURE `qianruo_add_product_class_generated`(
    IN schema_name_value VARCHAR(64),
    IN table_name_value VARCHAR(64),
    IN column_name_value VARCHAR(64),
    IN column_definition_value TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = schema_name_value
          AND table_name = table_name_value
          AND column_name = column_name_value
    ) THEN
        SET @add_gen_sql = CONCAT(
            'ALTER TABLE `', schema_name_value, '`.`', table_name_value,
            '` ADD COLUMN `', column_name_value, '` ', column_definition_value
        );
        PREPARE add_gen_statement FROM @add_gen_sql;
        EXECUTE add_gen_statement;
        DEALLOCATE PREPARE add_gen_statement;
    END IF;
END$$

-- 按名称补齐索引，避免迁移重跑时重复创建同名索引。
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
        SET @add_index_if_missing_sql = CONCAT(
            'ALTER TABLE `', schema_name_value, '`.`', table_name_value,
            '` ADD ', index_definition_value
        );
        PREPARE add_index_if_missing_statement FROM @add_index_if_missing_sql;
        EXECUTE add_index_if_missing_statement;
        DEALLOCATE PREPARE add_index_if_missing_statement;
    END IF;
END$$

-- 重建引用旧软删除字段的索引，并将索引列同步替换为 is_delete。
DROP PROCEDURE IF EXISTS `qianruo_rebuild_isdeleted_indexes`$$
CREATE PROCEDURE `qianruo_rebuild_isdeleted_indexes`(
    IN schema_name_value VARCHAR(64),
    IN table_name_value VARCHAR(64)
)
BEGIN
    DECLARE finished INT DEFAULT 0;
    DECLARE index_name_value VARCHAR(64);
    DECLARE index_non_unique_value INT DEFAULT 1;
    DECLARE index_columns_value TEXT;
    DECLARE old_indexes CURSOR FOR
        SELECT index_name, MIN(non_unique)
        FROM information_schema.statistics
        WHERE table_schema = schema_name_value
          AND table_name = table_name_value
          AND column_name = 'isdeleted'
          AND index_name <> 'PRIMARY'
        GROUP BY index_name
        ORDER BY index_name;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;

    OPEN old_indexes;
    rebuild_index_loop: LOOP
        FETCH old_indexes INTO index_name_value, index_non_unique_value;
        IF finished = 1 THEN
            LEAVE rebuild_index_loop;
        END IF;

        SELECT GROUP_CONCAT(
                   CASE
                       WHEN column_name = 'isdeleted' THEN '`is_delete`'
                       ELSE CONCAT('`', column_name, '`')
                   END
                   ORDER BY seq_in_index SEPARATOR ', '
               )
        INTO index_columns_value
        FROM information_schema.statistics
        WHERE table_schema = schema_name_value
          AND table_name = table_name_value
          AND index_name = index_name_value;

        SET @drop_index_sql = CONCAT(
            'ALTER TABLE `', schema_name_value, '`.`', table_name_value,
            '` DROP INDEX `', index_name_value, '`'
        );
        PREPARE drop_index_statement FROM @drop_index_sql;
        EXECUTE drop_index_statement;
        DEALLOCATE PREPARE drop_index_statement;

        SET @add_index_sql = CONCAT(
            'ALTER TABLE `', schema_name_value, '`.`', table_name_value,
            '` ADD ', IF(index_non_unique_value = 0, 'UNIQUE ', ''),
            'INDEX `', index_name_value, '` (', index_columns_value, ')'
        );
        PREPARE add_index_statement FROM @add_index_sql;
        EXECUTE add_index_statement;
        DEALLOCATE PREPARE add_index_statement;
    END LOOP;
    CLOSE old_indexes;
END$$

DROP PROCEDURE IF EXISTS `qianruo_rename_isdeleted_generic`$$
CREATE PROCEDURE `qianruo_rename_isdeleted_generic`()
BEGIN
    DECLARE finished INT DEFAULT 0;
    DECLARE current_schema VARCHAR(64);
    DECLARE current_table VARCHAR(64);
    DECLARE has_old INT DEFAULT 0;
    DECLARE has_new INT DEFAULT 0;
    DECLARE application_tables CURSOR FOR
        SELECT table_schema, table_name
        FROM information_schema.tables
        WHERE table_schema IN ('ry-cloud', 'ry_rental')
          AND table_type = 'BASE TABLE'
          AND table_name NOT IN ('ry_rental_member', 'ry_rental_product_class')
        ORDER BY table_schema, table_name;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;

    OPEN application_tables;
    rename_loop: LOOP
        FETCH application_tables INTO current_schema, current_table;
        IF finished = 1 THEN
            LEAVE rename_loop;
        END IF;

        SELECT COUNT(1) INTO has_old
        FROM information_schema.columns
        WHERE table_schema = current_schema
          AND table_name = current_table
          AND column_name = 'isdeleted';

        SELECT COUNT(1) INTO has_new
        FROM information_schema.columns
        WHERE table_schema = current_schema
          AND table_name = current_table
          AND column_name = 'is_delete';

        IF has_old > 0 AND has_new = 0 THEN
            SET @rename_sql = CONCAT(
                'ALTER TABLE `', current_schema, '`.`', current_table,
                '` CHANGE COLUMN `isdeleted` `is_delete` TINYINT(1) NOT NULL DEFAULT 0 ',
                'COMMENT ''软删除标识：0正常 1删除'''
            );
            PREPARE rename_statement FROM @rename_sql;
            EXECUTE rename_statement;
            DEALLOCATE PREPARE rename_statement;
        ELSEIF has_old > 0 AND has_new > 0 THEN
            SET @merge_sql = CONCAT(
                'UPDATE `', current_schema, '`.`', current_table,
                '` SET `is_delete` = CASE WHEN COALESCE(`is_delete`, 0) <> 0 ',
                'OR COALESCE(`isdeleted`, 0) <> 0 THEN 1 ELSE 0 END'
            );
            PREPARE merge_statement FROM @merge_sql;
            EXECUTE merge_statement;
            DEALLOCATE PREPARE merge_statement;

            CALL `qianruo_rebuild_isdeleted_indexes`(current_schema, current_table);

            SET @drop_old_column_sql = CONCAT(
                'ALTER TABLE `', current_schema, '`.`', current_table,
                '` DROP COLUMN `isdeleted`'
            );
            PREPARE drop_old_column_statement FROM @drop_old_column_sql;
            EXECUTE drop_old_column_statement;
            DEALLOCATE PREPARE drop_old_column_statement;
        END IF;
    END LOOP;
    CLOSE application_tables;
END$$

-- 迁移会员表的生成手机号列、软删除字段和手机号唯一索引。
DROP PROCEDURE IF EXISTS `qianruo_rename_isdeleted_member`$$
CREATE PROCEDURE `qianruo_rename_isdeleted_member`()
BEGIN
    DECLARE has_old INT DEFAULT 0;
    DECLARE has_new INT DEFAULT 0;

    SELECT COUNT(1) INTO has_old
    FROM information_schema.columns
    WHERE table_schema = 'ry_rental'
      AND table_name = 'ry_rental_member'
      AND column_name = 'isdeleted';

    SELECT COUNT(1) INTO has_new
    FROM information_schema.columns
    WHERE table_schema = 'ry_rental'
      AND table_name = 'ry_rental_member'
      AND column_name = 'is_delete';

    IF has_old > 0 AND has_new = 0 THEN
        CALL `qianruo_drop_column_if_exists`(
            'ry_rental', 'ry_rental_member', 'active_contact_phone'
        );
        ALTER TABLE `ry_rental`.`ry_rental_member`
            CHANGE COLUMN `isdeleted` `is_delete` TINYINT(1) NOT NULL DEFAULT 0
            COMMENT '软删除标识：0正常 1删除';

        CALL `qianruo_add_product_class_generated`(
            'ry_rental', 'ry_rental_member', 'active_contact_phone',
            'VARCHAR(32) GENERATED ALWAYS AS (CASE WHEN `is_delete` = 0 AND `contact_phone` IS NOT NULL AND `contact_phone` <> '''' THEN `contact_phone` ELSE NULL END) STORED COMMENT ''有效商户手机号唯一键'''
        );
        CALL `qianruo_add_index_if_missing`(
            'ry_rental', 'ry_rental_member', 'uk_rental_member_contact_phone',
            'UNIQUE INDEX `uk_rental_member_contact_phone` (`active_contact_phone`)'
        );
    ELSEIF has_old > 0 AND has_new > 0 THEN
        UPDATE `ry_rental`.`ry_rental_member`
        SET `is_delete` = CASE WHEN COALESCE(`is_delete`, 0) <> 0
                               OR COALESCE(`isdeleted`, 0) <> 0 THEN 1 ELSE 0 END;
        ALTER TABLE `ry_rental`.`ry_rental_member`
            MODIFY COLUMN `active_contact_phone` VARCHAR(32)
            GENERATED ALWAYS AS (
                CASE WHEN `is_delete` = 0 AND `contact_phone` IS NOT NULL
                     AND `contact_phone` <> '' THEN `contact_phone` ELSE NULL END
            ) STORED COMMENT '有效商户手机号唯一键';
        CALL `qianruo_rebuild_isdeleted_indexes`(
            'ry_rental', 'ry_rental_member'
        );
        ALTER TABLE `ry_rental`.`ry_rental_member`
            DROP COLUMN `isdeleted`;
    END IF;
END$$

DROP PROCEDURE IF EXISTS `qianruo_rename_isdeleted_product_class`$$
CREATE PROCEDURE `qianruo_rename_isdeleted_product_class`()
BEGIN
    DECLARE has_old INT DEFAULT 0;
    DECLARE has_new INT DEFAULT 0;

    SELECT COUNT(1) INTO has_old
    FROM information_schema.columns
    WHERE table_schema = 'ry_rental'
      AND table_name = 'ry_rental_product_class'
      AND column_name = 'isdeleted';

    SELECT COUNT(1) INTO has_new
    FROM information_schema.columns
    WHERE table_schema = 'ry_rental'
      AND table_name = 'ry_rental_product_class'
      AND column_name = 'is_delete';

    IF has_old > 0 AND has_new = 0 THEN
        -- 先删除依赖 isdeleted 的生成列，重命名后再重建
        CALL `qianruo_drop_column_if_exists`('ry_rental', 'ry_rental_product_class', 'active_class_code');
        CALL `qianruo_drop_column_if_exists`('ry_rental', 'ry_rental_product_class', 'active_class_name');

        SET @rename_pc_sql = CONCAT(
            'ALTER TABLE `ry_rental`.`ry_rental_product_class` ',
            'CHANGE COLUMN `isdeleted` `is_delete` TINYINT(1) NOT NULL DEFAULT 0 ',
            'COMMENT ''软删除标识：0正常 1删除'''
        );
        PREPARE rename_pc_statement FROM @rename_pc_sql;
        EXECUTE rename_pc_statement;
        DEALLOCATE PREPARE rename_pc_statement;

        CALL `qianruo_add_product_class_generated`(
            'ry_rental', 'ry_rental_product_class', 'active_class_code',
            'VARCHAR(64) GENERATED ALWAYS AS (CASE WHEN `is_delete` = 0 THEN `class_code` ELSE NULL END) STORED COMMENT ''有效类目编码唯一键'''
        );
        CALL `qianruo_add_product_class_generated`(
            'ry_rental', 'ry_rental_product_class', 'active_class_name',
            'VARCHAR(128) GENERATED ALWAYS AS (CASE WHEN `is_delete` = 0 THEN `class_name` ELSE NULL END) STORED COMMENT ''有效同级类目名称唯一键'''
        );
        CALL `qianruo_add_index_if_missing`(
            'ry_rental', 'ry_rental_product_class', 'uk_rental_product_class_active_code',
            'UNIQUE INDEX `uk_rental_product_class_active_code` (`active_class_code`)'
        );
        CALL `qianruo_add_index_if_missing`(
            'ry_rental', 'ry_rental_product_class', 'uk_rental_product_class_active_parent_name',
            'UNIQUE INDEX `uk_rental_product_class_active_parent_name` (`parent_id`, `active_class_name`)'
        );
    ELSEIF has_old > 0 AND has_new > 0 THEN
        UPDATE `ry_rental`.`ry_rental_product_class`
        SET `is_delete` = CASE WHEN COALESCE(`is_delete`, 0) <> 0
                               OR COALESCE(`isdeleted`, 0) <> 0 THEN 1 ELSE 0 END;
        CALL `qianruo_add_product_class_generated`(
            'ry_rental', 'ry_rental_product_class', 'active_class_code',
            'VARCHAR(64) GENERATED ALWAYS AS (CASE WHEN `is_delete` = 0 THEN `class_code` ELSE NULL END) STORED COMMENT ''有效类目编码唯一键'''
        );
        ALTER TABLE `ry_rental`.`ry_rental_product_class`
            MODIFY COLUMN `active_class_name` VARCHAR(128)
            GENERATED ALWAYS AS (
                CASE WHEN `is_delete` = 0 THEN `class_name` ELSE NULL END
            ) STORED COMMENT '有效同级类目名称唯一键';
        CALL `qianruo_rebuild_isdeleted_indexes`(
            'ry_rental', 'ry_rental_product_class'
        );
        ALTER TABLE `ry_rental`.`ry_rental_product_class`
            DROP COLUMN `isdeleted`;
        CALL `qianruo_add_index_if_missing`(
            'ry_rental', 'ry_rental_product_class', 'uk_rental_product_class_active_code',
            'UNIQUE INDEX `uk_rental_product_class_active_code` (`active_class_code`)'
        );
        CALL `qianruo_add_index_if_missing`(
            'ry_rental', 'ry_rental_product_class', 'uk_rental_product_class_active_parent_name',
            'UNIQUE INDEX `uk_rental_product_class_active_parent_name` (`parent_id`, `active_class_name`)'
        );
    END IF;
END$$

CALL `qianruo_rename_isdeleted_generic`()$$
CALL `qianruo_rename_isdeleted_member`()$$
CALL `qianruo_rename_isdeleted_product_class`()$$

DROP PROCEDURE IF EXISTS `qianruo_rename_isdeleted_generic`$$
DROP PROCEDURE IF EXISTS `qianruo_rename_isdeleted_member`$$
DROP PROCEDURE IF EXISTS `qianruo_rename_isdeleted_product_class`$$
DROP PROCEDURE IF EXISTS `qianruo_rebuild_isdeleted_indexes`$$
DROP PROCEDURE IF EXISTS `qianruo_add_index_if_missing`$$
DROP PROCEDURE IF EXISTS `qianruo_add_product_class_generated`$$
DROP PROCEDURE IF EXISTS `qianruo_drop_column_if_exists`$$

DELIMITER ;
