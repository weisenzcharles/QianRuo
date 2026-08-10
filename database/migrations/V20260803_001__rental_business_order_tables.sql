-- 将订单列表所需的核心业务表迁入 rental-business 独立数据库。
--
-- 说明：
-- 1. rental-business 只拥有 ry_business，后续订单写入和查询必须使用本库。
-- 2. 本次先按原表结构迁移订单列表依赖表，并幂等复制现有 ry_rental 数据。
-- 3. 复制完成后，商户身份、角色和菜单仍由 merchant-service 维护在 ry_rental。

CREATE DATABASE IF NOT EXISTS `ry_business`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_order`
  LIKE `ry_rental`.`ry_rental_order`;
CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_order_item`
  LIKE `ry_rental`.`ry_rental_order_item`;
CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_customer`
  LIKE `ry_rental`.`ry_rental_customer`;
CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_product`
  LIKE `ry_rental`.`ry_rental_product`;
CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_package_template`
  LIKE `ry_rental`.`ry_rental_package_template`;
CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_logistics_order`
  LIKE `ry_rental`.`ry_rental_logistics_order`;
CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_warehouse`
  LIKE `ry_rental`.`ry_rental_warehouse`;
CREATE TABLE IF NOT EXISTS `ry_business`.`ry_rental_order_note`
  LIKE `ry_rental`.`ry_rental_order_note`;

INSERT IGNORE INTO `ry_business`.`ry_rental_order`
SELECT * FROM `ry_rental`.`ry_rental_order`;
INSERT IGNORE INTO `ry_business`.`ry_rental_order_item`
SELECT * FROM `ry_rental`.`ry_rental_order_item`;
INSERT IGNORE INTO `ry_business`.`ry_rental_customer`
SELECT * FROM `ry_rental`.`ry_rental_customer`;
INSERT IGNORE INTO `ry_business`.`ry_rental_product`
    (`product_id`, `tenant_id`, `member_id`, `class_id`, `model_id`, `product_code`, `product_name`,
     `category_level1`, `category_level2`, `category_level3`, `brand`, `model_name`, `memory_spec`,
     `product_type`, `stock_mode`, `default_image_file_id`, `purchase_cost`, `sale_price`,
     `frontend_visible`, `stats_enabled`, `sort_no`, `status`, `create_by`, `create_time`,
     `update_by`, `update_time`, `remark`, `is_delete`)
SELECT `product_id`, `tenant_id`, `member_id`, `class_id`, `model_id`, `product_code`, `product_name`,
       `category_level1`, `category_level2`, `category_level3`, `brand`, `model_name`, `memory_spec`,
       `product_type`, `stock_mode`, `default_image_file_id`, `purchase_cost`, `sale_price`,
       `frontend_visible`, `stats_enabled`, `sort_no`, `status`, `create_by`, `create_time`,
       `update_by`, `update_time`, `remark`, `is_delete`
FROM `ry_rental`.`ry_rental_product`;
INSERT IGNORE INTO `ry_business`.`ry_rental_package_template`
SELECT * FROM `ry_rental`.`ry_rental_package_template`;
INSERT IGNORE INTO `ry_business`.`ry_rental_logistics_order`
SELECT * FROM `ry_rental`.`ry_rental_logistics_order`;
INSERT IGNORE INTO `ry_business`.`ry_rental_warehouse`
SELECT * FROM `ry_rental`.`ry_rental_warehouse`;
INSERT IGNORE INTO `ry_business`.`ry_rental_order_note`
SELECT * FROM `ry_rental`.`ry_rental_order_note`;
