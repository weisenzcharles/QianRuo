-- 商户完整认证材料、会员类型、接单资格和认证历史。
-- 所有新增表均使用 is_delete 软删除，重复执行不会覆盖业务数据。
USE `ry_rental`;

SET NAMES utf8mb4;

-- 认证资料页使用统一商户端组件路径，避免菜单路径与前端页面分叉。
UPDATE `ry_rental_merchant_menu`
SET `component` = 'merchant/profile/index', `update_time` = NOW(), `update_by` = 'migration'
WHERE `menu_id` = 2115 AND `tenant_id` = 0 AND `member_id` = 0 AND `is_delete` = 0;

-- MySQL 8.4 不支持 ALTER TABLE ADD COLUMN IF NOT EXISTS，使用元数据判断保证重复执行安全。
SET @rental_membership_type_column_exists = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ry_rental_member'
      AND COLUMN_NAME = 'membership_type'
);
SET @rental_membership_type_column_sql = IF(
    @rental_membership_type_column_exists = 0,
    'ALTER TABLE ry_rental_member ADD COLUMN membership_type VARCHAR(32) NOT NULL DEFAULT ''personal_member'' COMMENT ''会员类型：personal_member个人 ordinary_enterprise_member普通企业 settled_member入驻会员'' AFTER member_type',
    'SELECT 1'
);
PREPARE rental_membership_type_column_statement FROM @rental_membership_type_column_sql;
EXECUTE rental_membership_type_column_statement;
DEALLOCATE PREPARE rental_membership_type_column_statement;

UPDATE `ry_rental_member`
SET `membership_type` = CASE
    WHEN `membership_type` IS NULL OR `membership_type` = '' THEN
        CASE WHEN `member_type` = 'enterprise' THEN 'ordinary_enterprise_member' ELSE 'personal_member' END
    ELSE `membership_type`
END
WHERE `is_delete` = 0;

CREATE TABLE IF NOT EXISTS `ry_rental_member_certification` (
  `certification_id` BIGINT NOT NULL AUTO_INCREMENT,
  `tenant_id` BIGINT NOT NULL,
  `member_id` BIGINT NOT NULL,
  `version_no` INT NOT NULL,
  `certification_type` VARCHAR(32) NOT NULL,
  `certification_status` VARCHAR(32) NOT NULL DEFAULT 'pending',
  `legal_subject_name` VARCHAR(128) DEFAULT NULL,
  `license_no` VARCHAR(64) DEFAULT NULL,
  `registered_capital` DECIMAL(18,2) DEFAULT NULL,
  `business_scope` VARCHAR(2000) DEFAULT NULL,
  `license_address` VARCHAR(500) DEFAULT NULL,
  `license_valid_from` DATE DEFAULT NULL,
  `license_valid_to` DATE DEFAULT NULL,
  `license_long_term` TINYINT NOT NULL DEFAULT 0,
  `legal_representative_name` VARCHAR(64) DEFAULT NULL,
  `legal_representative_id_no` VARCHAR(64) DEFAULT NULL,
  `submitted_by` VARCHAR(64) DEFAULT NULL,
  `submitted_time` DATETIME DEFAULT NULL,
  `reviewed_by` VARCHAR(64) DEFAULT NULL,
  `reviewed_time` DATETIME DEFAULT NULL,
  `review_remark` VARCHAR(500) DEFAULT NULL,
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`certification_id`),
  UNIQUE KEY `uk_rental_member_certification_version` (`member_id`, `version_no`),
  KEY `idx_rental_member_certification_scope` (`tenant_id`, `member_id`, `certification_status`),
  KEY `idx_rental_member_certification_submitted` (`member_id`, `submitted_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户认证资料版本与审核历史';

CREATE TABLE IF NOT EXISTS `ry_rental_certification_material` (
  `material_id` BIGINT NOT NULL AUTO_INCREMENT,
  `tenant_id` BIGINT NOT NULL,
  `member_id` BIGINT NOT NULL,
  `certification_id` BIGINT NOT NULL,
  `material_type` VARCHAR(32) NOT NULL,
  `material_name` VARCHAR(128) NOT NULL,
  `file_id` BIGINT DEFAULT NULL,
  `file_url` VARCHAR(500) NOT NULL,
  `material_status` VARCHAR(32) NOT NULL DEFAULT 'pending',
  `review_remark` VARCHAR(500) DEFAULT NULL,
  `sort_no` INT NOT NULL DEFAULT 0,
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`material_id`),
  UNIQUE KEY `uk_rental_certification_material` (`certification_id`, `material_type`, `file_url`),
  KEY `idx_rental_certification_material_scope` (`tenant_id`, `member_id`, `certification_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户认证材料明细';

CREATE TABLE IF NOT EXISTS `ry_rental_member_membership_history` (
  `history_id` BIGINT NOT NULL AUTO_INCREMENT,
  `tenant_id` BIGINT NOT NULL,
  `member_id` BIGINT NOT NULL,
  `previous_type` VARCHAR(32) DEFAULT NULL,
  `membership_type` VARCHAR(32) NOT NULL,
  `reason` VARCHAR(500) DEFAULT NULL,
  `operator_name` VARCHAR(64) NOT NULL,
  `operated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`history_id`),
  KEY `idx_rental_membership_history_scope` (`tenant_id`, `member_id`, `operated_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户会员类型变更历史';

CREATE TABLE IF NOT EXISTS `ry_rental_member_qualification` (
  `qualification_id` BIGINT NOT NULL AUTO_INCREMENT,
  `tenant_id` BIGINT NOT NULL,
  `member_id` BIGINT NOT NULL,
  `qualification_code` VARCHAR(32) NOT NULL,
  `qualification_status` VARCHAR(32) NOT NULL DEFAULT 'disabled',
  `certification_id` BIGINT DEFAULT NULL,
  `effective_from` DATETIME DEFAULT NULL,
  `effective_to` DATETIME DEFAULT NULL,
  `reviewed_by` VARCHAR(64) DEFAULT NULL,
  `reviewed_time` DATETIME DEFAULT NULL,
  `review_remark` VARCHAR(500) DEFAULT NULL,
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT '',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`qualification_id`),
  UNIQUE KEY `uk_rental_member_qualification` (`member_id`, `qualification_code`, `is_delete`),
  KEY `idx_rental_member_qualification_scope` (`tenant_id`, `member_id`, `qualification_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户当前接单资格';

CREATE TABLE IF NOT EXISTS `ry_rental_member_qualification_history` (
  `history_id` BIGINT NOT NULL AUTO_INCREMENT,
  `tenant_id` BIGINT NOT NULL,
  `member_id` BIGINT NOT NULL,
  `qualification_id` BIGINT DEFAULT NULL,
  `qualification_code` VARCHAR(32) NOT NULL,
  `previous_status` VARCHAR(32) DEFAULT NULL,
  `qualification_status` VARCHAR(32) NOT NULL,
  `certification_id` BIGINT DEFAULT NULL,
  `reason` VARCHAR(500) DEFAULT NULL,
  `operator_name` VARCHAR(64) NOT NULL,
  `operated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` VARCHAR(64) DEFAULT '',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`history_id`),
  KEY `idx_rental_qualification_history_scope` (`tenant_id`, `member_id`, `operated_time`),
  KEY `idx_rental_qualification_history_code` (`member_id`, `qualification_code`, `operated_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户接单资格变更历史';

-- 已有商户补齐当前资格快照，历史首条记录只在缺失时创建。
INSERT INTO `ry_rental_member_qualification`
    (`tenant_id`, `member_id`, `qualification_code`, `qualification_status`, `effective_from`, `reviewed_by`, `reviewed_time`, `review_remark`, `create_by`)
SELECT m.`tenant_id`, m.`member_id`, 'external_accept',
       CASE WHEN m.`auth_status` = 'verified' AND m.`external_accept_enabled` = 1 THEN 'enabled' ELSE 'disabled' END,
       CASE WHEN m.`auth_status` = 'verified' AND m.`external_accept_enabled` = 1 THEN COALESCE(m.`audit_time`, NOW()) ELSE NULL END,
       'migration', m.`audit_time`, '历史商户资格快照迁移', 'migration'
FROM `ry_rental_member` m
WHERE m.`is_delete` = 0
  AND NOT EXISTS (
      SELECT 1 FROM `ry_rental_member_qualification` q
      WHERE q.`member_id` = m.`member_id`
        AND q.`qualification_code` = 'external_accept'
        AND q.`is_delete` = 0
  );

INSERT INTO `ry_rental_member_qualification`
    (`tenant_id`, `member_id`, `qualification_code`, `qualification_status`, `create_by`)
SELECT m.`tenant_id`, m.`member_id`, codes.`qualification_code`, 'disabled', 'migration'
FROM `ry_rental_member` m
JOIN (
    SELECT 'alliance_accept' AS `qualification_code`
    UNION ALL SELECT 'mini_program_accept'
) codes
WHERE m.`is_delete` = 0
  AND NOT EXISTS (
      SELECT 1 FROM `ry_rental_member_qualification` q
      WHERE q.`member_id` = m.`member_id`
        AND q.`qualification_code` = codes.`qualification_code`
        AND q.`is_delete` = 0
  );

INSERT INTO `ry_rental_member_certification`
    (`tenant_id`, `member_id`, `version_no`, `certification_type`, `certification_status`, `legal_subject_name`, `license_no`,
     `registered_capital`, `business_scope`, `license_address`, `license_valid_from`, `license_valid_to`, `license_long_term`,
     `legal_representative_name`, `legal_representative_id_no`, `submitted_by`, `submitted_time`, `reviewed_by`, `reviewed_time`,
     `review_remark`, `create_by`)
SELECT m.`tenant_id`, m.`member_id`, 1, m.`member_type`,
       CASE m.`auth_status` WHEN 'verified' THEN 'approved' WHEN 'rejected' THEN 'rejected' ELSE 'pending' END,
       m.`legal_subject_name`, m.`license_no`, m.`registered_capital`, m.`business_scope`, m.`license_address`,
       m.`license_valid_from`, m.`license_valid_to`, m.`license_long_term`, m.`legal_representative_name`,
       m.`legal_representative_id_no`, m.`create_by`, m.`create_time`,
       CASE WHEN m.`auth_status` = 'verified' THEN m.`update_by` ELSE NULL END,
       CASE WHEN m.`auth_status` IN ('verified', 'rejected') THEN m.`audit_time` ELSE NULL END,
       m.`audit_remark`, 'migration'
FROM `ry_rental_member` m
WHERE m.`is_delete` = 0
  AND NOT EXISTS (
      SELECT 1 FROM `ry_rental_member_certification` c WHERE c.`member_id` = m.`member_id` AND c.`is_delete` = 0
  );

-- 将历史主体表中的文件引用迁移到首个认证版本，避免认证历史只有文本没有材料。
INSERT INTO `ry_rental_certification_material`
    (`tenant_id`, `member_id`, `certification_id`, `material_type`, `material_name`, `file_url`, `material_status`, `sort_no`, `create_by`)
SELECT m.`tenant_id`, m.`member_id`, c.`certification_id`, 'license', '营业执照', m.`license_image_url`,
       CASE WHEN m.`auth_status` = 'verified' THEN 'approved' ELSE 'pending' END, 1, 'migration'
FROM `ry_rental_member` m
INNER JOIN `ry_rental_member_certification` c
    ON c.`member_id` = m.`member_id` AND c.`version_no` = 1 AND c.`is_delete` = 0
WHERE m.`is_delete` = 0 AND m.`license_image_url` IS NOT NULL AND m.`license_image_url` <> ''
  AND NOT EXISTS (
      SELECT 1 FROM `ry_rental_certification_material` cm
      WHERE cm.`certification_id` = c.`certification_id` AND cm.`material_type` = 'license'
        AND cm.`file_url` = m.`license_image_url` AND cm.`is_delete` = 0
  );

INSERT INTO `ry_rental_certification_material`
    (`tenant_id`, `member_id`, `certification_id`, `material_type`, `material_name`, `file_url`, `material_status`, `sort_no`, `create_by`)
SELECT m.`tenant_id`, m.`member_id`, c.`certification_id`, 'legal_id_front', '法人身份证人像面', m.`legal_id_front_url`,
       CASE WHEN m.`auth_status` = 'verified' THEN 'approved' ELSE 'pending' END, 2, 'migration'
FROM `ry_rental_member` m
INNER JOIN `ry_rental_member_certification` c
    ON c.`member_id` = m.`member_id` AND c.`version_no` = 1 AND c.`is_delete` = 0
WHERE m.`is_delete` = 0 AND m.`legal_id_front_url` IS NOT NULL AND m.`legal_id_front_url` <> ''
  AND NOT EXISTS (
      SELECT 1 FROM `ry_rental_certification_material` cm
      WHERE cm.`certification_id` = c.`certification_id` AND cm.`material_type` = 'legal_id_front'
        AND cm.`file_url` = m.`legal_id_front_url` AND cm.`is_delete` = 0
  );

INSERT INTO `ry_rental_certification_material`
    (`tenant_id`, `member_id`, `certification_id`, `material_type`, `material_name`, `file_url`, `material_status`, `sort_no`, `create_by`)
SELECT m.`tenant_id`, m.`member_id`, c.`certification_id`, 'legal_id_back', '法人身份证国徽面', m.`legal_id_back_url`,
       CASE WHEN m.`auth_status` = 'verified' THEN 'approved' ELSE 'pending' END, 3, 'migration'
FROM `ry_rental_member` m
INNER JOIN `ry_rental_member_certification` c
    ON c.`member_id` = m.`member_id` AND c.`version_no` = 1 AND c.`is_delete` = 0
WHERE m.`is_delete` = 0 AND m.`legal_id_back_url` IS NOT NULL AND m.`legal_id_back_url` <> ''
  AND NOT EXISTS (
      SELECT 1 FROM `ry_rental_certification_material` cm
      WHERE cm.`certification_id` = c.`certification_id` AND cm.`material_type` = 'legal_id_back'
        AND cm.`file_url` = m.`legal_id_back_url` AND cm.`is_delete` = 0
  );

INSERT INTO `ry_rental_certification_material`
    (`tenant_id`, `member_id`, `certification_id`, `material_type`, `material_name`, `file_url`, `material_status`, `sort_no`, `create_by`)
SELECT m.`tenant_id`, m.`member_id`, c.`certification_id`, 'contract', '签约合同', m.`contract_file_url`,
       CASE WHEN m.`auth_status` = 'verified' THEN 'approved' ELSE 'pending' END, 4, 'migration'
FROM `ry_rental_member` m
INNER JOIN `ry_rental_member_certification` c
    ON c.`member_id` = m.`member_id` AND c.`version_no` = 1 AND c.`is_delete` = 0
WHERE m.`is_delete` = 0 AND m.`contract_file_url` IS NOT NULL AND m.`contract_file_url` <> ''
  AND NOT EXISTS (
      SELECT 1 FROM `ry_rental_certification_material` cm
      WHERE cm.`certification_id` = c.`certification_id` AND cm.`material_type` = 'contract'
        AND cm.`file_url` = m.`contract_file_url` AND cm.`is_delete` = 0
  );

-- 为迁移出的资格快照补齐第一条历史记录，使状态变化轨迹完整可审计。
INSERT INTO `ry_rental_member_qualification_history`
    (`tenant_id`, `member_id`, `qualification_id`, `qualification_code`, `previous_status`, `qualification_status`,
     `certification_id`, `reason`, `operator_name`, `operated_time`, `create_by`)
SELECT q.`tenant_id`, q.`member_id`, q.`qualification_id`, q.`qualification_code`, NULL, q.`qualification_status`,
       q.`certification_id`, '历史资格快照迁移', 'migration', COALESCE(q.`reviewed_time`, q.`create_time`), 'migration'
FROM `ry_rental_member_qualification` q
WHERE q.`is_delete` = 0
  AND NOT EXISTS (
      SELECT 1 FROM `ry_rental_member_qualification_history` h
      WHERE h.`qualification_id` = q.`qualification_id` AND h.`is_delete` = 0
  );

-- 为已有商户补齐当前会员类型的起始历史，后续审核变更会继续追加记录。
INSERT INTO `ry_rental_member_membership_history`
    (`tenant_id`, `member_id`, `previous_type`, `membership_type`, `reason`, `operator_name`, `operated_time`, `create_by`)
SELECT m.`tenant_id`, m.`member_id`, NULL, m.`membership_type`, '历史会员类型迁移', 'migration', m.`create_time`, 'migration'
FROM `ry_rental_member` m
WHERE m.`is_delete` = 0
  AND NOT EXISTS (
      SELECT 1 FROM `ry_rental_member_membership_history` h
      WHERE h.`member_id` = m.`member_id` AND h.`is_delete` = 0
  );
