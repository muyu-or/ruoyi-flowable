-- ============================================================
-- 修复 stock_in 表中入库类型为空/为0的数据
-- ============================================================
SET NAMES utf8mb4;
SELECT id, material_id, material_name, inbound_type, quantity, inbound_time
FROM stock_in
WHERE inbound_type IS NULL OR inbound_type = '' OR inbound_type = '0'
ORDER BY id;
-- 1. 先查看待修复的记录
SELECT '=== 入库类型为空的记录 ===' AS info;
SELECT id, material_id, material_name, inbound_type, quantity, inbound_time, operator
FROM stock_in
WHERE inbound_type IS NULL OR inbound_type = '' OR inbound_type = '0'
ORDER BY id;

-- 2. 修复：成品 → '4'（产品入库）
UPDATE stock_in
SET inbound_type = '4'
WHERE (inbound_type IS NULL OR inbound_type = '' OR inbound_type = '0')
  AND (material_name LIKE '%成品%' OR material_id LIKE 'PRD-%');

-- 3. 修复：返修相关 → '3'（返修入库）
UPDATE stock_in
SET inbound_type = '3'
WHERE (inbound_type IS NULL OR inbound_type = '' OR inbound_type = '0')
  AND (material_name LIKE '%返修%' OR material_name LIKE '%返工%' OR material_name LIKE '%维修%' OR remark LIKE '%返修%');

-- 4. 修复：调拨相关 → '2'（调拨入库）
UPDATE stock_in
SET inbound_type = '2'
WHERE (inbound_type IS NULL OR inbound_type = '' OR inbound_type = '0')
  AND (material_name LIKE '%调拨%' OR remark LIKE '%调拨%');

-- 5. 剩下的默认为毛坯入库 → '1'
UPDATE stock_in
SET inbound_type = '1'
WHERE (inbound_type IS NULL OR inbound_type = '' OR inbound_type = '0');

-- 6. 验证修复结果
SELECT '=== 修复后检查 ===' AS info;
SELECT
    inbound_type,
    CASE inbound_type
        WHEN '1' THEN '毛坯入库'
        WHEN '2' THEN '调拨入库'
        WHEN '3' THEN '返修入库'
        WHEN '4' THEN '产品入库'
        ELSE '未知'
    END AS type_label,
    COUNT(*) AS cnt
FROM stock_in
GROUP BY inbound_type
ORDER BY inbound_type;
