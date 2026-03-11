-- ============================================================
-- 03_inventory.sql — 库存管理模块
-- 说明：库存/入库/出库表 + 相关字典数据
-- 前置：02_org_and_roles.sql 已执行（菜单已就位）
-- 幂等：DROP/CREATE + INSERT IGNORE
-- ============================================================


-- ============================================================
-- 第一部分：业务表
-- ============================================================

DROP TABLE IF EXISTS `stock_out`;
DROP TABLE IF EXISTS `stock_in`;
DROP TABLE IF EXISTS `inventory`;

-- 1. 库存信息表
CREATE TABLE `inventory` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `material_id`         VARCHAR(100) NOT NULL UNIQUE COMMENT '物料ID/唯一标识码',
    `material_name`       VARCHAR(255) NOT NULL COMMENT '物料名称',
    `material_category`   VARCHAR(100) NOT NULL COMMENT '物料大类（0原料，1产品，2废料）',
    `material_subcategory` VARCHAR(100) COMMENT '物料子类',
    `current_quantity`    INT          NOT NULL DEFAULT 0 COMMENT '当前库存数量',
    `warehouse_area`      VARCHAR(100) NOT NULL COMMENT '库区',
    `status`              VARCHAR(20)  DEFAULT '在库' COMMENT '库存状态（1在库，2已出库）',
    `operator`            VARCHAR(100) NOT NULL COMMENT '操作人员',
    `first_inbound_time`  DATETIME     COMMENT '首次入库时间',
    `last_inbound_time`   DATETIME     COMMENT '上次入库时间',
    `last_outbound_time`  DATETIME     COMMENT '上次出库时间',
    `create_time`         DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存信息表';

-- 2. 入库记录表
CREATE TABLE `stock_in` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '入库记录ID',
    `material_id`     VARCHAR(100) NOT NULL COMMENT '物料ID',
    `material_name`   VARCHAR(255) NOT NULL COMMENT '物料名称',
    `inbound_type`    VARCHAR(100) NOT NULL COMMENT '入库类型（1毛坯入库，2调拨入库，3返修入库，4产品入库）',
    `quantity`        INT          NOT NULL COMMENT '入库数量',
    `warehouse_area`  VARCHAR(100) NOT NULL COMMENT '库区',
    `inbound_time`    DATETIME     NOT NULL COMMENT '入库时间',
    `operator`        VARCHAR(100) NOT NULL COMMENT '操作人员',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库记录表';

-- 3. 出库记录表
CREATE TABLE `stock_out` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '出库记录ID',
    `material_id`     VARCHAR(100) NOT NULL COMMENT '物料ID',
    `material_name`   VARCHAR(255) NOT NULL COMMENT '物料名称',
    `outbound_type`   VARCHAR(100) NOT NULL COMMENT '出库类型（1生产领料出库，2销售出库，3调拨出库，4报废出库）',
    `quantity`        INT          NOT NULL COMMENT '出库数量',
    `warehouse_area`  VARCHAR(100) NOT NULL COMMENT '库区',
    `outbound_time`   DATETIME     NOT NULL COMMENT '出库时间',
    `operator`        VARCHAR(100) NOT NULL COMMENT '操作人员',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库记录表';


-- ============================================================
-- 第二部分：字典类型（sys_dict_type）
-- dict_id: 107-113
-- ============================================================
INSERT IGNORE INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(107, '物料子类',   'material_subcategory', '0', 'admin', NOW(), NULL),
(108, '入库类型',   'inbound_type',         '0', 'admin', NOW(), NULL),
(109, '出库类型',   'outbound_type',        '0', 'admin', NOW(), NULL),
(110, '库位状态',   'status',               '0', 'admin', NOW(), NULL),
(111, '库存状态',   'inventory_status',     '0', 'admin', NOW(), NULL),
(112, '库房区域',   'warehouse_area',       '0', 'admin', NOW(), NULL),
(113, '物料大类',   'material_category',    '0', 'admin', NOW(), NULL);


-- ============================================================
-- 第三部分：字典数据（sys_dict_data）
-- dict_code: 114-132, 143, 145
-- ============================================================
INSERT IGNORE INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
-- material_subcategory
(114, 0, '玻璃', '0', 'material_subcategory', '', '', 'N', '0', 'admin', NOW(), ''),
(115, 0, '光纤', '1', 'material_subcategory', '', '', 'N', '0', 'admin', NOW(), ''),
(116, 0, '模具', '2', 'material_subcategory', '', '', 'N', '0', 'admin', NOW(), ''),
(117, 0, '电缆', '3', 'material_subcategory', '', '', 'N', '0', 'admin', NOW(), ''),
-- inbound_type
(118, 0, '毛坯入库', '1', 'inbound_type', '', '', 'N', '0', 'admin', NOW(), ''),
(119, 0, '调拨入库', '2', 'inbound_type', '', '', 'N', '0', 'admin', NOW(), ''),
(120, 0, '返修入库', '3', 'inbound_type', '', '', 'N', '0', 'admin', NOW(), ''),
(145, 0, '产品入库', '4', 'inbound_type', '', '', 'N', '0', 'admin', NOW(), ''),
-- outbound_type
(121, 0, '生产领料出库', '1', 'outbound_type', '', '', 'N', '0', 'admin', NOW(), ''),
(122, 0, '销售出库',     '2', 'outbound_type', '', '', 'N', '0', 'admin', NOW(), ''),
(123, 0, '调拨出库',     '3', 'outbound_type', '', '', 'N', '0', 'admin', NOW(), ''),
(124, 0, '报废出库',     '4', 'outbound_type', '', '', 'N', '0', 'admin', NOW(), ''),
-- inventory_status
(125, 0, '已出库', '2', 'inventory_status', '', '', 'N', '0', 'admin', NOW(), ''),
(126, 0, '在库',   '1', 'inventory_status', '', '', 'N', '0', 'admin', NOW(), ''),
(143, 0, '报废',   '3', 'inventory_status', '', '', 'N', '0', 'admin', NOW(), ''),
-- warehouse_area
(127, 0, 'A区', '0', 'warehouse_area', '', '', 'N', '0', 'admin', NOW(), ''),
(128, 0, 'B区', '1', 'warehouse_area', '', '', 'N', '0', 'admin', NOW(), ''),
(129, 0, 'C区', '2', 'warehouse_area', '', '', 'N', '0', 'admin', NOW(), ''),
-- material_category
(130, 0, '原料', '0', 'material_category', '', '', 'N', '0', 'admin', NOW(), ''),
(131, 0, '产品', '1', 'material_category', '', '', 'N', '0', 'admin', NOW(), ''),
(132, 0, '废料', '2', 'material_category', '', '', 'N', '0', 'admin', NOW(), '');
