USE `ry-cloud`;
SET NAMES utf8mb4;

-- 修复平台端商户菜单历史数据的字符集转换问题。
-- 这些记录曾经使用错误的客户端编码写入，导致中文被保存为 Latin-1/UTF-8 混合乱码。
-- 按固定菜单 ID 更新，迁移可重复执行，不影响其他平台菜单和商户菜单数据。
UPDATE `sys_menu`
SET `menu_name` = CASE `menu_id`
        WHEN 2095 THEN '商户菜单'
        WHEN 2096 THEN '商户菜单查询'
        WHEN 2097 THEN '商户菜单新增'
        WHEN 2098 THEN '商户菜单修改'
        WHEN 2099 THEN '商户菜单删除'
        ELSE `menu_name`
    END,
    `client_type` = 'platform',
    `update_by` = 'migration',
    `update_time` = NOW()
WHERE `menu_id` IN (2095, 2096, 2097, 2098, 2099)
  AND `is_delete` = 0;
