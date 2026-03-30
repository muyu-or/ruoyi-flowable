-- 入库记录成本补录：对所有无单价的记录随机赋单价，再重算入库金额
-- 单价范围：50 ~ 2000 元（随机），精度 2 位小数

-- 第一步：对 unit_cost 为空的记录随机赋单价
UPDATE stock_in
SET unit_cost = ROUND(50 + (RAND() * 1950), 2)
WHERE unit_cost IS NULL OR unit_cost = 0;

-- 第二步：重算入库金额 = 单价 × 入库数量
UPDATE stock_in
SET total_amount = ROUND(unit_cost * quantity, 2)
WHERE unit_cost IS NOT NULL
  AND unit_cost > 0;

-- 验证
SELECT id, material_name, quantity, unit_cost, total_amount
FROM stock_in
ORDER BY id DESC
LIMIT 20;
