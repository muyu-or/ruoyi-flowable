-- 库存成本价格字段 DDL
-- inventory 表加 unit_cost（加权平均单价）和 total_cost（库存总成本）
ALTER TABLE inventory ADD COLUMN unit_cost DECIMAL(12,2) DEFAULT NULL COMMENT '成本单价（加权平均）' AFTER current_quantity;
ALTER TABLE inventory ADD COLUMN total_cost DECIMAL(14,2) DEFAULT NULL COMMENT '库存总成本（数量×单价）' AFTER unit_cost;

-- stock_in 表加 unit_cost（本次入库单价）和 total_amount（本次入库金额）
ALTER TABLE stock_in ADD COLUMN unit_cost DECIMAL(12,2) DEFAULT NULL COMMENT '成本单价' AFTER quantity;
ALTER TABLE stock_in ADD COLUMN total_amount DECIMAL(14,2) DEFAULT NULL COMMENT '入库金额（数量×单价）' AFTER unit_cost;
