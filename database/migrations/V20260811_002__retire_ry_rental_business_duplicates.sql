-- 退役 ry_rental 中已经迁移到 ry_business 的租赁业务重复表。
--
-- 这不是业务数据软删除，而是数据库结构收口。脚本只处理明确列出的 14 张遗留表：
-- 1. 先确认 ry_business 中同名权威表存在；
-- 2. 再确认 ry_rental 遗留表为空，任何残留数据都中止迁移；
-- 3. 最后删除遗留表，不创建兼容视图、不保留双写或回退读取。

USE `ry_rental`;

DELIMITER $$

DROP PROCEDURE IF EXISTS `qianruo_retire_rental_business_duplicates`$$
CREATE PROCEDURE `qianruo_retire_rental_business_duplicates`()
BEGIN
    DECLARE finished INT DEFAULT 0;
    DECLARE source_table_name VARCHAR(128);
    DECLARE source_exists INT DEFAULT 0;
    DECLARE target_exists INT DEFAULT 0;
    DECLARE source_rows BIGINT DEFAULT 0;
    DECLARE failure_message VARCHAR(512);
    DECLARE legacy_tables CURSOR FOR
        SELECT table_name
        FROM (
            SELECT 'ry_rental_accessory_item' AS table_name
            UNION ALL SELECT 'ry_rental_asset_sn'
            UNION ALL SELECT 'ry_rental_customer'
            UNION ALL SELECT 'ry_rental_idempotent_request'
            UNION ALL SELECT 'ry_rental_inventory_ledger'
            UNION ALL SELECT 'ry_rental_inventory_reservation'
            UNION ALL SELECT 'ry_rental_logistics_order'
            UNION ALL SELECT 'ry_rental_order'
            UNION ALL SELECT 'ry_rental_order_item'
            UNION ALL SELECT 'ry_rental_order_note'
            UNION ALL SELECT 'ry_rental_package_item'
            UNION ALL SELECT 'ry_rental_package_template'
            UNION ALL SELECT 'ry_rental_product'
            UNION ALL SELECT 'ry_rental_warehouse'
        ) AS legacy_table_list;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;

    OPEN legacy_tables;
    retire_loop: LOOP
        FETCH legacy_tables INTO source_table_name;
        IF finished = 1 THEN
            LEAVE retire_loop;
        END IF;

        SELECT COUNT(1)
        INTO source_exists
        FROM information_schema.tables
        WHERE table_schema = 'ry_rental'
          AND table_name = source_table_name
          AND table_type = 'BASE TABLE';

        IF source_exists = 0 THEN
            ITERATE retire_loop;
        END IF;

        SELECT COUNT(1)
        INTO target_exists
        FROM information_schema.tables
        WHERE table_schema = 'ry_business'
          AND table_name = source_table_name
          AND table_type = 'BASE TABLE';

        IF target_exists = 0 THEN
            SET failure_message = CONCAT(
                '拒绝删除遗留表：ry_business.', source_table_name, ' 不存在'
            );
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = failure_message;
        END IF;

        SET @qianruo_source_rows = 0;
        SET @qianruo_count_sql = CONCAT(
            'SELECT COUNT(1) INTO @qianruo_source_rows FROM `ry_rental`.`',
            source_table_name, '`'
        );
        PREPARE qianruo_count_statement FROM @qianruo_count_sql;
        EXECUTE qianruo_count_statement;
        DEALLOCATE PREPARE qianruo_count_statement;
        SET source_rows = COALESCE(@qianruo_source_rows, 0);

        IF source_rows > 0 THEN
            SET failure_message = CONCAT(
                '拒绝删除遗留表：ry_rental.', source_table_name,
                ' 仍有 ', source_rows, ' 行数据，请先完成数据核对和迁移'
            );
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = failure_message;
        END IF;

        SET @qianruo_drop_sql = CONCAT(
            'DROP TABLE IF EXISTS `ry_rental`.`', source_table_name, '`'
        );
        PREPARE qianruo_drop_statement FROM @qianruo_drop_sql;
        EXECUTE qianruo_drop_statement;
        DEALLOCATE PREPARE qianruo_drop_statement;
    END LOOP;
    CLOSE legacy_tables;
END$$

CALL `qianruo_retire_rental_business_duplicates`()$$
DROP PROCEDURE IF EXISTS `qianruo_retire_rental_business_duplicates`$$

DELIMITER ;
