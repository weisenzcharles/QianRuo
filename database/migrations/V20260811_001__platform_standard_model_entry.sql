-- 完成平台标准型号录入结构。
-- 新结构仅使用 class_id 关联产品类目；已有环境的旧类目文本列保留原值但不再参与业务读写。

USE `ry_rental`;
SET NAMES utf8mb4;

DELIMITER $$

DROP PROCEDURE IF EXISTS `qianruo_standard_model_add_column_if_missing`$$
CREATE PROCEDURE `qianruo_standard_model_add_column_if_missing`(
    IN column_name_value VARCHAR(64),
    IN column_definition_value VARCHAR(2000)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'ry_rental'
          AND table_name = 'ry_rental_brand_model'
          AND column_name = column_name_value
    ) THEN
        SET @add_standard_model_column_sql = CONCAT(
            'ALTER TABLE `ry_rental`.`ry_rental_brand_model` ADD COLUMN `',
            column_name_value,
            '` ',
            column_definition_value
        );
        PREPARE add_standard_model_column_statement FROM @add_standard_model_column_sql;
        EXECUTE add_standard_model_column_statement;
        DEALLOCATE PREPARE add_standard_model_column_statement;
    END IF;
END$$

DROP PROCEDURE IF EXISTS `qianruo_standard_model_add_index_if_missing`$$
CREATE PROCEDURE `qianruo_standard_model_add_index_if_missing`(
    IN index_name_value VARCHAR(64),
    IN index_definition_value VARCHAR(1000)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = 'ry_rental'
          AND table_name = 'ry_rental_brand_model'
          AND index_name = index_name_value
    ) THEN
        SET @add_standard_model_index_sql = CONCAT(
            'ALTER TABLE `ry_rental`.`ry_rental_brand_model` ADD ',
            index_definition_value
        );
        PREPARE add_standard_model_index_statement FROM @add_standard_model_index_sql;
        EXECUTE add_standard_model_index_statement;
        DEALLOCATE PREPARE add_standard_model_index_statement;
    END IF;
END$$

DELIMITER ;

-- 旧环境仍存在该字段时补齐默认值，保证新代码不再提交冗余分类文本也能正常录入。
SET @legacy_category_column_exists = (
    SELECT COUNT(1)
    FROM information_schema.columns
    WHERE table_schema = 'ry_rental'
      AND table_name = 'ry_rental_brand_model'
      AND column_name = 'category_level1'
);
SET @legacy_category_default_sql = IF(
    @legacy_category_column_exists > 0,
    'ALTER TABLE `ry_rental`.`ry_rental_brand_model` MODIFY COLUMN `category_level1` VARCHAR(64) NOT NULL DEFAULT '''' COMMENT ''已停用的历史分类文本''',
    'SELECT 1'
);
PREPARE legacy_category_default_statement FROM @legacy_category_default_sql;
EXECUTE legacy_category_default_statement;
DEALLOCATE PREPARE legacy_category_default_statement;

ALTER TABLE `ry_rental`.`ry_rental_brand_model`
    MODIFY COLUMN `class_id` BIGINT NOT NULL COMMENT '平台产品类目ID',
    MODIFY COLUMN `official_flag` CHAR(1) NOT NULL DEFAULT '1' COMMENT '是否官方标准：1是 0否',
    MODIFY COLUMN `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态：0正常 1停用';

CALL `qianruo_standard_model_add_column_if_missing`(
    'active_class_id',
    'BIGINT GENERATED ALWAYS AS (CASE WHEN `is_delete` = 0 THEN `class_id` ELSE NULL END) STORED COMMENT ''有效型号类目唯一键'''
);
CALL `qianruo_standard_model_add_column_if_missing`(
    'active_brand',
    'VARCHAR(64) GENERATED ALWAYS AS (CASE WHEN `is_delete` = 0 THEN `brand` ELSE NULL END) STORED COMMENT ''有效型号品牌唯一键'''
);
CALL `qianruo_standard_model_add_column_if_missing`(
    'active_model_name',
    'VARCHAR(128) GENERATED ALWAYS AS (CASE WHEN `is_delete` = 0 THEN `model_name` ELSE NULL END) STORED COMMENT ''有效型号名称唯一键'''
);
CALL `qianruo_standard_model_add_column_if_missing`(
    'active_memory_spec',
    'VARCHAR(64) GENERATED ALWAYS AS (CASE WHEN `is_delete` = 0 THEN COALESCE(`memory_spec`, '''') ELSE NULL END) STORED COMMENT ''有效型号规格唯一键'''
);
CALL `qianruo_standard_model_add_index_if_missing`(
    'uk_rental_brand_model_active_identity',
    'UNIQUE KEY `uk_rental_brand_model_active_identity` (`active_class_id`, `active_brand`, `active_model_name`, `active_memory_spec`)'
);

DROP PROCEDURE IF EXISTS `qianruo_standard_model_add_column_if_missing`;
DROP PROCEDURE IF EXISTS `qianruo_standard_model_add_index_if_missing`;

-- 平台入口统一使用“标准型号”名称，避免与商户常用品牌型号功能混淆。
UPDATE `ry-cloud`.`sys_menu`
SET `menu_name` = CASE `menu_id`
        WHEN 2040 THEN '标准型号'
        WHEN 2041 THEN '标准型号查询'
        WHEN 2042 THEN '标准型号新增'
        WHEN 2043 THEN '标准型号修改'
        WHEN 2044 THEN '标准型号删除'
        ELSE `menu_name`
    END,
    `remark` = CASE WHEN `menu_id` = 2040 THEN '平台标准型号管理' ELSE `remark` END,
    `update_by` = 'migration',
    `update_time` = NOW()
WHERE `menu_id` BETWEEN 2040 AND 2044
  AND `client_type` = 'platform';
