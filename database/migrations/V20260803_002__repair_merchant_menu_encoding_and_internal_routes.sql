USE `ry_rental`;
SET NAMES utf8mb4;

-- 统一修复商户菜单模板的中文名称、路由字段和外链标记。
-- 商户端当前所有业务页面均为单页应用内部页面；只有后续明确配置 HTTP/HTTPS 地址的菜单才允许作为外链。

UPDATE `ry_rental_merchant_menu`
SET menu_name = CASE menu_id
        WHEN 2100 THEN '首页'
        WHEN 2110 THEN '店铺'
        WHEN 2111 THEN '商品管理'
        WHEN 2112 THEN '我的商品'
        WHEN 2113 THEN '发布商品'
        WHEN 2114 THEN '仓库管理'
        WHEN 2115 THEN '店铺管理'
        WHEN 2116 THEN '用户管理'
        WHEN 2117 THEN '账号管理'
        WHEN 2118 THEN '操作日志'
        WHEN 2120 THEN '交易'
        WHEN 2121 THEN '订单管理'
        WHEN 2122 THEN '库存管理'
        WHEN 2123 THEN '物流管理'
        WHEN 2130 THEN '数据'
        WHEN 2140 THEN '帮助'
        WHEN 2150 THEN '资源池管理'
        WHEN 2151 THEN 'SN管理'
        WHEN 2152 THEN '配件管理'
        WHEN 2153 THEN '套餐管理'
        WHEN 2160 THEN '仓库查询'
        WHEN 2161 THEN '仓库新增'
        WHEN 2162 THEN '仓库修改'
        WHEN 2163 THEN '仓库删除'
        WHEN 2164 THEN '资源池查询'
        WHEN 2165 THEN '资源池新增'
        WHEN 2166 THEN '资源池修改'
        WHEN 2167 THEN '资源池删除'
        WHEN 2168 THEN '商品查询'
        WHEN 2169 THEN '商品新增'
        WHEN 2170 THEN '商品修改'
        WHEN 2171 THEN '商品删除'
        WHEN 2172 THEN 'SN查询'
        WHEN 2173 THEN 'SN新增'
        WHEN 2174 THEN 'SN修改'
        WHEN 2175 THEN 'SN删除'
        WHEN 2176 THEN '配件查询'
        WHEN 2177 THEN '配件新增'
        WHEN 2178 THEN '配件修改'
        WHEN 2179 THEN '配件删除'
        WHEN 2180 THEN '套餐查询'
        WHEN 2181 THEN '套餐新增'
        WHEN 2182 THEN '套餐修改'
        WHEN 2183 THEN '套餐删除'
        WHEN 2184 THEN '库存调整'
        WHEN 2185 THEN '库存查询'
        WHEN 2186 THEN '库存删除'
        WHEN 2194 THEN '品牌型号'
        WHEN 2195 THEN '品牌型号保存'
        ELSE menu_name
    END,
    update_by = 'migration',
    update_time = NOW()
WHERE tenant_id = 0
  AND member_id = 0
  AND is_delete = 0
  AND menu_id IN (2100, 2110, 2111, 2112, 2113, 2114, 2115, 2116, 2117, 2118,
                  2120, 2121, 2122, 2123, 2130, 2140, 2150, 2151, 2152, 2153,
                  2160, 2161, 2162, 2163, 2164, 2165, 2166, 2167, 2168, 2169,
                  2170, 2171, 2172, 2173, 2174, 2175, 2176, 2177, 2178, 2179,
                  2180, 2181, 2182, 2183, 2184, 2185, 2186, 2194, 2195);

-- 主数据页面统一使用真实前端组件和完整业务路由，避免旧菜单字段把按钮误当成页面。
UPDATE `ry_rental_merchant_menu`
SET path = CASE menu_id
        WHEN 2100 THEN 'merchant/home'
        WHEN 2150 THEN 'merchant/pool'
        WHEN 2151 THEN 'merchant/sn'
        WHEN 2152 THEN 'merchant/accessory'
        WHEN 2153 THEN 'merchant/package'
        WHEN 2194 THEN 'merchant/brand-model'
        ELSE path
    END,
    component = CASE menu_id
        WHEN 2100 THEN 'merchant/dashboard/index'
        WHEN 2110 THEN 'ParentView'
        WHEN 2111 THEN 'ParentView'
        WHEN 2116 THEN 'ParentView'
        WHEN 2120 THEN 'ParentView'
        WHEN 2130 THEN 'merchant/data/index'
        WHEN 2140 THEN 'merchant/help/index'
        WHEN 2150 THEN 'merchant/pool/index'
        WHEN 2151 THEN 'merchant/sn/index'
        WHEN 2152 THEN 'merchant/accessory/index'
        WHEN 2153 THEN 'merchant/package/index'
        WHEN 2194 THEN 'merchant/brandModel/index'
        ELSE component
    END,
    menu_type = CASE WHEN menu_id = 2194 THEN 'C' ELSE menu_type END,
    visible = CASE WHEN menu_id = 2194 THEN '0' ELSE visible END,
    is_frame = 1,
    update_by = 'migration',
    update_time = NOW()
WHERE tenant_id = 0
  AND member_id = 0
  AND is_delete = 0
  AND menu_id IN (2100, 2110, 2111, 2116, 2120, 2130, 2140, 2150, 2151, 2152, 2153, 2194);

-- 接口权限菜单必须是隐藏按钮，使用唯一内部占位路径，不能参与动态页面路由。
UPDATE `ry_rental_merchant_menu`
SET path = CONCAT('permission/', menu_id),
    component = '',
    `query` = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '1',
    status = '0',
    icon = '',
    update_by = 'migration',
    update_time = NOW()
WHERE tenant_id = 0
  AND member_id = 0
  AND is_delete = 0
  AND menu_id BETWEEN 2160 AND 2186;

-- 品牌型号保存是隐藏按钮，不允许进入商户菜单树。
UPDATE `ry_rental_merchant_menu`
SET path = '#',
    component = '',
    `query` = '',
    route_name = '',
    is_frame = 1,
    is_cache = 0,
    menu_type = 'F',
    visible = '1',
    status = '0',
    icon = '',
    update_by = 'migration',
    update_time = NOW()
WHERE tenant_id = 0
  AND member_id = 0
  AND is_delete = 0
  AND menu_id = 2195;

-- 所有当前启用的商户页面强制保持站内打开；显式 HTTP/HTTPS 外链不被本次规则覆盖。
UPDATE `ry_rental_merchant_menu`
SET is_frame = 1,
    update_by = 'migration',
    update_time = NOW()
WHERE tenant_id = 0
  AND member_id = 0
  AND is_delete = 0
  AND menu_type IN ('M', 'C')
  AND path NOT LIKE 'http://%'
  AND path NOT LIKE 'https://%';
