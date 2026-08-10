-- 建立 rental-business 独立业务库。
-- 当前服务只完成独立运行骨架，商品、库存、订单等领域表将在后续迁移中按服务所有权写入本库。
CREATE DATABASE IF NOT EXISTS `ry_business`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
