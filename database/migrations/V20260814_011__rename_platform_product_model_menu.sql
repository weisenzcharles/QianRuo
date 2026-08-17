USE `ry-cloud`;
SET NAMES utf8mb4;

-- 平台端统一产品主数据术语：标准型号菜单改为产品型号。
UPDATE `sys_menu`
SET `menu_name` = CASE `menu_id`
        WHEN 2040 THEN '产品型号'
        WHEN 2041 THEN '产品型号查询'
        WHEN 2042 THEN '产品型号新增'
        WHEN 2043 THEN '产品型号修改'
        WHEN 2044 THEN '产品型号删除'
        ELSE `menu_name`
    END,
    `update_by` = 'migration',
    `update_time` = NOW()
WHERE `menu_id` IN (2040, 2041, 2042, 2043, 2044)
  AND `client_type` = 'platform'
  AND `is_delete` = 0;
