
-- 1. 库存信息表（添加物料子类字段）
CREATE TABLE `inventory` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `material_id` VARCHAR(100) NOT NULL UNIQUE COMMENT '物料ID/唯一标识码',
    `material_name` VARCHAR(255) NOT NULL COMMENT '物料名称',
    `material_category` VARCHAR(100) NOT NULL COMMENT '物料大类（0原料，1产品，2废料）',
    `material_subcategory` VARCHAR(100) COMMENT '物料子类',
    `current_quantity` INT NOT NULL DEFAULT 0 COMMENT '当前库存数量',
    `warehouse_area` VARCHAR(100) NOT NULL COMMENT '库区',
    `status` VARCHAR(20) DEFAULT '在库' COMMENT '库存状态（1在库，2已出库）',
     `operator` VARCHAR(100) NOT NULL COMMENT '操作人员',
    `first_inbound_time` DATETIME COMMENT '首次入库时间',
    `last_inbound_time` DATETIME COMMENT '上次入库时间',
    `last_outbound_time` DATETIME COMMENT '上次出库时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) COMMENT='库存信息表';

-- 2. 入库记录表 - 入库类型：毛坯入库、调拨入库、返修入库
CREATE TABLE `stock_in` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '入库记录ID',
    `material_id` VARCHAR(100) NOT NULL COMMENT '物料ID',
    `material_name` VARCHAR(255) NOT NULL COMMENT '物料名称',
    `inbound_type` VARCHAR(100) NOT NULL COMMENT '入库类型（1.毛坯入库、2.调拨入库、3.返修入库）',
    `quantity` INT NOT NULL COMMENT '入库数量',
    `warehouse_area` VARCHAR(100) NOT NULL COMMENT '库区',
    `inbound_time` DATETIME NOT NULL COMMENT '入库时间',
    `operator` VARCHAR(100) NOT NULL COMMENT '操作人员',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) COMMENT='入库记录表';

-- 3. 出库记录表 - 出库类型：生产领料出库、销售出库、调拨出库、报废出库
CREATE TABLE `stock_out` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '出库记录ID',
    `material_id` VARCHAR(100) NOT NULL COMMENT '物料ID',
    `material_name` VARCHAR(255) NOT NULL COMMENT '物料名称',
    `outbound_type` VARCHAR(100) NOT NULL COMMENT '出库类型（1.生产领料出库、2.销售出库、3.调拨出库、4.报废出库）',
    `quantity` INT NOT NULL COMMENT '出库数量',
    `warehouse_area` VARCHAR(100) NOT NULL COMMENT '库区',
    `outbound_time` DATETIME NOT NULL COMMENT '出库时间',
    `operator` VARCHAR(100) NOT NULL COMMENT '操作人员',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) COMMENT='出库记录表';