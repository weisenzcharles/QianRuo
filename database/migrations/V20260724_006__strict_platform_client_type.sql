USE `ry-cloud`;

ALTER TABLE `sys_menu`
    MODIFY COLUMN `client_type` VARCHAR(20) NOT NULL DEFAULT 'platform'
    COMMENT '客户端类型：platform平台后台 merchant商家端 all共用';

UPDATE `sys_menu`
SET `client_type` = 'platform'
WHERE `client_type` = 'admin'
   OR `client_type` IS NULL
   OR `client_type` = '';
