-- 租赁系统数据库初始化脚本
-- 技术基线：RuoYi-Cloud + RuoYi-Cloud-Vue3-TypeScript 二次开发
-- 说明：本脚本只创建业务库，不修改 RuoYi-Cloud 原生系统库表。

CREATE DATABASE IF NOT EXISTS `ry_rental`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 租赁核心业务服务专用数据库，后续商品、库存、订单等领域表只由 rental-business 所有。
CREATE DATABASE IF NOT EXISTS `ry_business`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `ry_rental`;
