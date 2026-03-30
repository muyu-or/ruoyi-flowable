-- 库存成本补录：对所有无单价的库存随机赋一个单价，再重算总成本
-- 单价范围：50 ~ 2000 元（随机），精度 2 位小数

-- 第一步：对 unit_cost 为空的记录随机赋单价
UPDATE inventory
SET unit_cost = ROUND(50 + (RAND() * 1950), 2)
WHERE (unit_cost IS NULL OR unit_cost = 0)
  AND status != '2';

-- 第二步：重算所有有单价记录的库存总成本
UPDATE inventory
SET total_cost = ROUND(unit_cost * current_quantity, 2)
WHERE unit_cost IS NOT NULL
  AND unit_cost > 0
  AND status != '2';

-- 验证
SELECT id, material_name, current_quantity, unit_cost, total_cost
FROM inventory
WHERE status != '2'
ORDER BY id;
