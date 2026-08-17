-- 清理已经完全废弃的旧行政区划和天气表。
-- ry_rental_region 与 ry_rental_region_weather 已是唯一运行时数据源，旧表不再保留。

USE `ry_business`;

DROP TABLE IF EXISTS `ry_region_weather`;
DROP TABLE IF EXISTS `ry_region`;
