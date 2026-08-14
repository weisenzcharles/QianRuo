-- 订单域统一使用 BIGINT 主键建立渠道和商户操作人关系。
--
-- 约束说明：
-- 1. ry_rental_order_channel.channel_code 是业务编码，继续使用 VARCHAR，不参与表间关联。
-- 2. 订单、套餐渠道关系和渠道转移记录统一使用 channel_id BIGINT。
-- 3. create_by、update_by 统一逻辑关联 ry_rental.ry_rental_merchant_user.merchant_user_id。
-- 4. 平台端没有商户用户身份，因此平台全局渠道的商户用户审计字段保持 NULL；平台操作由平台日志追踪。
-- 5. 跨微服务数据库不建立物理外键，通过 BIGINT 字段、索引、写入校验和本迁移的数据校验保证关系正确。
-- 6. 本脚本可重复执行，部署脚本每次运行迁移时不会重复添加或删除字段。

USE `ry_business`;

DROP PROCEDURE IF EXISTS `qianruo_migrate_order_channel_relation`;
DELIMITER $$

CREATE PROCEDURE `qianruo_migrate_order_channel_relation`()
BEGIN
    DECLARE unmapped_count BIGINT DEFAULT 0;
    DECLARE channel_data_type VARCHAR(64) DEFAULT NULL;
    DECLARE channel_nullable VARCHAR(3) DEFAULT NULL;
    DECLARE from_channel_data_type VARCHAR(64) DEFAULT NULL;
    DECLARE from_channel_nullable VARCHAR(3) DEFAULT NULL;
    DECLARE to_channel_data_type VARCHAR(64) DEFAULT NULL;
    DECLARE to_channel_nullable VARCHAR(3) DEFAULT NULL;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'ry_business' AND table_name = 'ry_rental_order' AND column_name = 'channel_id'
    ) THEN
        ALTER TABLE `ry_rental_order`
            ADD COLUMN `channel_id` BIGINT NULL COMMENT '订单渠道ID' AFTER `external_order_no`;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'ry_business' AND table_name = 'ry_rental_order' AND column_name = 'channel_code'
    ) THEN
        UPDATE `ry_rental_order` o
        SET o.channel_id = COALESCE(
            (
                SELECT c.channel_id
                FROM `ry_rental_order_channel` c
                WHERE c.tenant_id = o.tenant_id
                  AND c.member_id = o.member_id
                  AND c.scope_type = 'merchant'
                  AND c.channel_code = o.channel_code
                ORDER BY c.is_delete, c.channel_id DESC
                LIMIT 1
            ),
            (
                SELECT c.channel_id
                FROM `ry_rental_order_channel` c
                WHERE c.scope_type = 'platform'
                  AND c.channel_code = o.channel_code
                ORDER BY c.is_delete, c.channel_id DESC
                LIMIT 1
            )
        )
        WHERE o.channel_id IS NULL
          AND o.channel_code IS NOT NULL
          AND TRIM(o.channel_code) <> '';

        SELECT COUNT(*) INTO unmapped_count
        FROM `ry_rental_order`
        WHERE channel_id IS NULL;
        IF unmapped_count > 0 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = '订单渠道迁移失败：存在无法关联 channel_id 的订单';
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = 'ry_business' AND table_name = 'ry_rental_order'
              AND index_name = 'idx_rental_order_external'
        ) THEN
            ALTER TABLE `ry_rental_order` DROP INDEX `idx_rental_order_external`;
        END IF;

        ALTER TABLE `ry_rental_order` DROP COLUMN `channel_code`;
    END IF;

    SELECT COUNT(*) INTO unmapped_count
    FROM `ry_rental_order`
    WHERE channel_id IS NULL;
    IF unmapped_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '订单渠道校验失败：channel_id 不允许为空';
    END IF;

    SELECT data_type, is_nullable INTO channel_data_type, channel_nullable
    FROM information_schema.columns
    WHERE table_schema = 'ry_business' AND table_name = 'ry_rental_order'
      AND column_name = 'channel_id'
    LIMIT 1;
    IF channel_data_type <> 'bigint' OR channel_nullable <> 'NO' THEN
        ALTER TABLE `ry_rental_order`
            MODIFY COLUMN `channel_id` BIGINT NOT NULL COMMENT '订单渠道ID';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = 'ry_business' AND table_name = 'ry_rental_order'
          AND index_name = 'idx_rental_order_channel_id'
    ) THEN
        ALTER TABLE `ry_rental_order`
            ADD KEY `idx_rental_order_channel_id` (`tenant_id`, `member_id`, `channel_id`, `is_delete`);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = 'ry_business' AND table_name = 'ry_rental_order'
          AND index_name = 'idx_rental_order_external'
    ) THEN
        ALTER TABLE `ry_rental_order`
            ADD KEY `idx_rental_order_external` (`channel_id`, `external_order_no`);
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'ry_business' AND table_name = 'ry_rental_order_channel_transfer'
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'ry_business' AND table_name = 'ry_rental_order_channel_transfer'
              AND column_name = 'from_channel_id'
        ) THEN
            ALTER TABLE `ry_rental_order_channel_transfer`
                ADD COLUMN `from_channel_id` BIGINT NULL COMMENT '原渠道ID' AFTER `order_id`;
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'ry_business' AND table_name = 'ry_rental_order_channel_transfer'
              AND column_name = 'to_channel_id'
        ) THEN
            ALTER TABLE `ry_rental_order_channel_transfer`
                ADD COLUMN `to_channel_id` BIGINT NULL COMMENT '目标渠道ID' AFTER `from_channel_id`;
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'ry_business' AND table_name = 'ry_rental_order_channel_transfer'
              AND column_name = 'from_channel_code'
        ) THEN
            UPDATE `ry_rental_order_channel_transfer` t
            SET t.from_channel_id = COALESCE(
                (
                    SELECT c.channel_id
                    FROM `ry_rental_order_channel` c
                    WHERE c.tenant_id = t.tenant_id
                      AND c.member_id = t.member_id
                      AND c.scope_type = 'merchant'
                      AND c.channel_code = t.from_channel_code
                    ORDER BY c.is_delete, c.channel_id DESC
                    LIMIT 1
                ),
                (
                    SELECT c.channel_id
                    FROM `ry_rental_order_channel` c
                    WHERE c.scope_type = 'platform'
                      AND c.channel_code = t.from_channel_code
                    ORDER BY c.is_delete, c.channel_id DESC
                    LIMIT 1
                )
            )
            WHERE t.from_channel_id IS NULL
              AND t.from_channel_code IS NOT NULL
              AND TRIM(t.from_channel_code) <> '';
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'ry_business' AND table_name = 'ry_rental_order_channel_transfer'
              AND column_name = 'to_channel_code'
        ) THEN
            UPDATE `ry_rental_order_channel_transfer` t
            SET t.to_channel_id = COALESCE(
                (
                    SELECT c.channel_id
                    FROM `ry_rental_order_channel` c
                    WHERE c.tenant_id = t.tenant_id
                      AND c.member_id = t.member_id
                      AND c.scope_type = 'merchant'
                      AND c.channel_code = t.to_channel_code
                    ORDER BY c.is_delete, c.channel_id DESC
                    LIMIT 1
                ),
                (
                    SELECT c.channel_id
                    FROM `ry_rental_order_channel` c
                    WHERE c.scope_type = 'platform'
                      AND c.channel_code = t.to_channel_code
                    ORDER BY c.is_delete, c.channel_id DESC
                    LIMIT 1
                )
            )
            WHERE t.to_channel_id IS NULL
              AND t.to_channel_code IS NOT NULL
              AND TRIM(t.to_channel_code) <> '';
        END IF;

        SELECT COUNT(*) INTO unmapped_count
        FROM `ry_rental_order_channel_transfer`
        WHERE to_channel_id IS NULL;
        IF unmapped_count > 0 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = '订单渠道转移迁移失败：存在无法关联的目标渠道';
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'ry_business' AND table_name = 'ry_rental_order_channel_transfer'
              AND column_name = 'from_channel_code'
        ) THEN
            ALTER TABLE `ry_rental_order_channel_transfer` DROP COLUMN `from_channel_code`;
        END IF;

        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'ry_business' AND table_name = 'ry_rental_order_channel_transfer'
              AND column_name = 'to_channel_code'
        ) THEN
            ALTER TABLE `ry_rental_order_channel_transfer` DROP COLUMN `to_channel_code`;
        END IF;

        SELECT data_type, is_nullable INTO from_channel_data_type, from_channel_nullable
        FROM information_schema.columns
        WHERE table_schema = 'ry_business' AND table_name = 'ry_rental_order_channel_transfer'
          AND column_name = 'from_channel_id'
        LIMIT 1;
        SELECT data_type, is_nullable INTO to_channel_data_type, to_channel_nullable
        FROM information_schema.columns
        WHERE table_schema = 'ry_business' AND table_name = 'ry_rental_order_channel_transfer'
          AND column_name = 'to_channel_id'
        LIMIT 1;
        IF from_channel_data_type <> 'bigint' OR from_channel_nullable <> 'YES'
            OR to_channel_data_type <> 'bigint' OR to_channel_nullable <> 'NO' THEN
            ALTER TABLE `ry_rental_order_channel_transfer`
                MODIFY COLUMN `from_channel_id` BIGINT NULL COMMENT '原渠道ID',
                MODIFY COLUMN `to_channel_id` BIGINT NOT NULL COMMENT '目标渠道ID';
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = 'ry_business' AND table_name = 'ry_rental_order_channel_transfer'
              AND index_name = 'idx_rental_order_transfer_channel'
        ) THEN
            ALTER TABLE `ry_rental_order_channel_transfer`
                ADD KEY `idx_rental_order_transfer_channel` (`member_id`, `to_channel_id`, `create_time`);
        END IF;
    END IF;
END$$

CALL `qianruo_migrate_order_channel_relation`()$$
DROP PROCEDURE IF EXISTS `qianruo_migrate_order_channel_relation`$$

DROP PROCEDURE IF EXISTS `qianruo_migrate_merchant_audit_column`$$
CREATE PROCEDURE `qianruo_migrate_merchant_audit_column`(
    IN table_name_value VARCHAR(64),
    IN column_name_value VARCHAR(64)
)
BEGIN
    DECLARE column_type_value VARCHAR(64) DEFAULT NULL;
    DECLARE column_nullable_value VARCHAR(3) DEFAULT NULL;
    DECLARE temporary_column_name VARCHAR(128);
    DECLARE audit_index_name VARCHAR(128);

    SET temporary_column_name = CONCAT(column_name_value, '_merchant_user_id');

    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'ry_business' AND table_name = table_name_value
    ) THEN
        -- 上次执行若中断在删除旧列之后，可直接把已完成映射的临时列恢复为正式字段。
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'ry_business' AND table_name = table_name_value
              AND column_name = column_name_value
        ) AND EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'ry_business' AND table_name = table_name_value
              AND column_name = temporary_column_name
        ) THEN
            SET @audit_sql = CONCAT(
                'ALTER TABLE `', table_name_value, '` CHANGE COLUMN `', temporary_column_name, '` `',
                column_name_value, '` BIGINT NULL COMMENT ''商户用户ID'''
            );
            PREPARE audit_statement FROM @audit_sql;
            EXECUTE audit_statement;
            DEALLOCATE PREPARE audit_statement;
        END IF;

        SELECT data_type, is_nullable INTO column_type_value, column_nullable_value
        FROM information_schema.columns
        WHERE table_schema = 'ry_business' AND table_name = table_name_value
          AND column_name = column_name_value
        LIMIT 1;

        IF column_type_value IS NOT NULL AND column_type_value <> 'bigint' THEN
            IF NOT EXISTS (
                SELECT 1 FROM information_schema.columns
                WHERE table_schema = 'ry_business' AND table_name = table_name_value
                  AND column_name = temporary_column_name
            ) THEN
                SET @audit_sql = CONCAT(
                    'ALTER TABLE `', table_name_value, '` ADD COLUMN `', temporary_column_name,
                    '` BIGINT NULL COMMENT ''商户用户ID'''
                );
                PREPARE audit_statement FROM @audit_sql;
                EXECUTE audit_statement;
                DEALLOCATE PREPARE audit_statement;
            END IF;

            SET @audit_sql = CONCAT(
                'UPDATE `', table_name_value, '` t ',
                'LEFT JOIN `ry_rental`.`ry_rental_merchant_user` u ',
                'ON u.tenant_id = t.tenant_id AND u.member_id = t.member_id AND u.is_delete = 0 ',
                'AND (u.user_name = NULLIF(TRIM(t.`', column_name_value, '`), '''') ',
                'OR u.phone = NULLIF(TRIM(t.`', column_name_value, '`), '''') ',
                'OR (t.`', column_name_value, '` REGEXP ''^[0-9]+$'' ',
                'AND u.merchant_user_id = CAST(t.`', column_name_value, '` AS UNSIGNED))) ',
                'SET t.`', temporary_column_name, '` = u.merchant_user_id'
            );
            PREPARE audit_statement FROM @audit_sql;
            EXECUTE audit_statement;
            DEALLOCATE PREPARE audit_statement;

            SET @audit_sql = CONCAT(
                'ALTER TABLE `', table_name_value, '` DROP COLUMN `', column_name_value, '`'
            );
            PREPARE audit_statement FROM @audit_sql;
            EXECUTE audit_statement;
            DEALLOCATE PREPARE audit_statement;

            SET @audit_sql = CONCAT(
                'ALTER TABLE `', table_name_value, '` CHANGE COLUMN `', temporary_column_name, '` `',
                column_name_value, '` BIGINT NULL COMMENT ''商户用户ID'''
            );
            PREPARE audit_statement FROM @audit_sql;
            EXECUTE audit_statement;
            DEALLOCATE PREPARE audit_statement;
        ELSEIF column_type_value = 'bigint' AND column_nullable_value <> 'YES' THEN
            SET @audit_sql = CONCAT(
                'ALTER TABLE `', table_name_value, '` MODIFY COLUMN `', column_name_value,
                '` BIGINT NULL COMMENT ''商户用户ID'''
            );
            PREPARE audit_statement FROM @audit_sql;
            EXECUTE audit_statement;
            DEALLOCATE PREPARE audit_statement;
        END IF;

        -- 仅在发现无效主键时执行修复，正常重复部署不会产生无意义的数据更新。
        SET @invalid_audit_count = 0;
        SET @audit_sql = CONCAT(
            'SELECT COUNT(*) INTO @invalid_audit_count FROM `', table_name_value, '` t ',
            'LEFT JOIN `ry_rental`.`ry_rental_merchant_user` u ',
            'ON u.merchant_user_id = t.`', column_name_value, '` ',
            'AND u.tenant_id = t.tenant_id AND u.member_id = t.member_id ',
            'WHERE t.`', column_name_value, '` IS NOT NULL AND u.merchant_user_id IS NULL'
        );
        PREPARE audit_statement FROM @audit_sql;
        EXECUTE audit_statement;
        DEALLOCATE PREPARE audit_statement;

        IF COALESCE(@invalid_audit_count, 0) > 0 THEN
        SET @audit_sql = CONCAT(
            'UPDATE `', table_name_value, '` t ',
            'LEFT JOIN `ry_rental`.`ry_rental_merchant_user` u ',
            'ON u.merchant_user_id = t.`', column_name_value, '` ',
            'AND u.tenant_id = t.tenant_id AND u.member_id = t.member_id ',
            'SET t.`', column_name_value, '` = NULL ',
            'WHERE t.`', column_name_value, '` IS NOT NULL AND u.merchant_user_id IS NULL'
        );
        PREPARE audit_statement FROM @audit_sql;
        EXECUTE audit_statement;
        DEALLOCATE PREPARE audit_statement;
        END IF;

        SET audit_index_name = CONCAT(
            'idx_', REPLACE(table_name_value, 'ry_rental_', ''), '_', column_name_value
        );
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = 'ry_business' AND table_name = table_name_value
              AND index_name = audit_index_name
        ) THEN
            SET @audit_sql = CONCAT(
                'ALTER TABLE `', table_name_value, '` ADD KEY `', audit_index_name,
                '` (`', column_name_value, '`)'
            );
            PREPARE audit_statement FROM @audit_sql;
            EXECUTE audit_statement;
            DEALLOCATE PREPARE audit_statement;
        END IF;
    END IF;
END$$

CALL `qianruo_migrate_merchant_audit_column`('ry_rental_order', 'create_by')$$
CALL `qianruo_migrate_merchant_audit_column`('ry_rental_order', 'update_by')$$
CALL `qianruo_migrate_merchant_audit_column`('ry_rental_order_item', 'create_by')$$
CALL `qianruo_migrate_merchant_audit_column`('ry_rental_order_item', 'update_by')$$
CALL `qianruo_migrate_merchant_audit_column`('ry_rental_order_note', 'create_by')$$
CALL `qianruo_migrate_merchant_audit_column`('ry_rental_order_note', 'update_by')$$
CALL `qianruo_migrate_merchant_audit_column`('ry_rental_logistics_order', 'create_by')$$
CALL `qianruo_migrate_merchant_audit_column`('ry_rental_logistics_order', 'update_by')$$
CALL `qianruo_migrate_merchant_audit_column`('ry_rental_order_channel', 'create_by')$$
CALL `qianruo_migrate_merchant_audit_column`('ry_rental_order_channel', 'update_by')$$
CALL `qianruo_migrate_merchant_audit_column`('ry_rental_package_channel', 'create_by')$$
CALL `qianruo_migrate_merchant_audit_column`('ry_rental_package_channel', 'update_by')$$

DROP PROCEDURE IF EXISTS `qianruo_migrate_merchant_audit_column`$$
DELIMITER ;
