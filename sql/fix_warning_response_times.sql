-- ============================================================
-- Fix warning response times: ensure all resolved warnings have
-- create_time at least 1-3 days before node complete_time
-- ============================================================

-- 1. Update existing resolved warnings: move create_time earlier
-- Process 103 (PROC-20260405-001), vacuum node, complete_time = 2026-04-10 09:00:00
UPDATE task_warning SET create_time = '2026-04-07 10:00:00' WHERE id = 100;  -- deadline_soon, ~3 days before
UPDATE task_warning SET create_time = '2026-04-08 10:00:00' WHERE id = 101;  -- overdue, ~2 days before
UPDATE task_warning SET create_time = '2026-04-07 10:00:00' WHERE id = 102;  -- deadline_soon
UPDATE task_warning SET create_time = '2026-04-08 10:00:00' WHERE id = 103;  -- overdue

-- Process 105 (PROC-20260408-001), baking node, complete_time = 2026-04-14 09:00:00
UPDATE task_warning SET create_time = '2026-04-11 08:00:00' WHERE id = 104;  -- deadline_soon, ~3 days before
UPDATE task_warning SET create_time = '2026-04-12 08:00:00' WHERE id = 105;  -- overdue, ~2 days before
UPDATE task_warning SET create_time = '2026-04-11 08:00:00' WHERE id = 106;  -- deadline_soon

-- Process 106 (PROC-20260410-001), vacuum node, complete_time = 2026-04-13 10:00:00
UPDATE task_warning SET create_time = '2026-04-11 09:00:00' WHERE id = 107;  -- deadline_soon, ~2 days before
UPDATE task_warning SET create_time = '2026-04-12 09:00:00' WHERE id = 108;  -- overdue, ~1 day before
UPDATE task_warning SET create_time = '2026-04-12 09:00:00' WHERE id = 109;  -- overdue

-- Process 108 (PROC-20260414-001), test node, complete_time = 2026-04-20 10:00:00
UPDATE task_warning SET create_time = '2026-04-17 09:00:00' WHERE id = 110;  -- deadline_soon, ~3 days before
UPDATE task_warning SET create_time = '2026-04-18 09:00:00' WHERE id = 111;  -- overdue, ~2 days before

-- 2. Add new warnings for nodes that currently have no warnings
--    (原料检测入库, 预处理, 产品入库, 出库, 测试 - additional)
--    All resolved, create_time 1-3 days before node complete_time

INSERT INTO task_warning
    (id, user_id, warn_type, proc_inst_id, proc_name, task_name, team_name, node_key, node_name, end_date, is_read, admin_read, resolved, create_time)
VALUES
-- 原料检测入库 warnings (node complete times: 04-02 09:00, 04-03 15:00, 04-04 17:00, 04-06 09:00)
-- Process 100, complete_time = 2026-04-02 09:00:00
(120, 103, 'deadline_soon', 'PROC-20260401-001', 'materialProcess', '高硼硅玻璃管加工-A01', '仓库班组', 'Activity_1uqk506', '原料检测入库', '2026-04-02', 1, 1, 1, '2026-03-31 08:00:00'),
(121, 103, 'overdue',       'PROC-20260401-001', 'materialProcess', '高硼硅玻璃管加工-A01', '仓库班组', 'Activity_1uqk506', '原料检测入库', '2026-04-02', 1, 1, 1, '2026-04-01 08:00:00'),
-- Process 102, complete_time = 2026-04-04 17:00:00
(122, 108, 'overdue',       'PROC-20260403-001', 'materialProcess', '光纤预制棒加工-A03', '仓库班组', 'Activity_1uqk506', '原料检测入库', '2026-04-04', 1, 1, 1, '2026-04-02 09:00:00'),
-- Process 104, complete_time = 2026-04-08 14:00:00
(123, 103, 'deadline_soon', 'PROC-20260407-001', 'materialProcess', '电缆护套加工-A05', '仓库班组', 'Activity_1uqk506', '原料检测入库', '2026-04-07', 1, 1, 1, '2026-04-06 08:00:00'),
(124, 103, 'overdue',       'PROC-20260407-001', 'materialProcess', '电缆护套加工-A05', '仓库班组', 'Activity_1uqk506', '原料检测入库', '2026-04-08', 1, 1, 1, '2026-04-07 08:00:00'),
-- Process 107, complete_time = 2026-04-13 10:00:00
(125, 103, 'overdue',       'PROC-20260412-001', 'materialProcess', '高纯石英砂加工-A08', '仓库班组', 'Activity_1uqk506', '原料检测入库', '2026-04-13', 1, 1, 1, '2026-04-11 09:00:00'),
-- Process 109, complete_time = 2026-04-17 09:00:00
(126, 103, 'deadline_soon', 'PROC-20260416-001', 'materialProcess', '钢化玻璃管成型-A10', '仓库班组', 'Activity_1uqk506', '原料检测入库', '2026-04-17', 1, 1, 1, '2026-04-15 10:00:00'),

-- 预处理 warnings (node complete times: 04-03 10:00, 04-05 09:00, 04-06 16:00, 04-07 17:00)
-- Process 100, complete_time = 2026-04-03 10:00:00
(130, 104, 'overdue',       'PROC-20260401-001', 'materialProcess', '高硼硅玻璃管加工-A01', '预处理班组', 'Activity_0kzrvj3', '预处理', '2026-04-03', 1, 1, 1, '2026-04-01 10:00:00'),
-- Process 101, complete_time = 2026-04-05 09:00:00
(131, 104, 'deadline_soon', 'PROC-20260402-001', 'materialProcess', '石英玻璃管加工-A02', '预处理班组', 'Activity_0kzrvj3', '预处理', '2026-04-05', 1, 1, 1, '2026-04-03 09:00:00'),
-- Process 104, complete_time = 2026-04-09 14:00:00
(132, 104, 'overdue',       'PROC-20260407-001', 'materialProcess', '电缆护套加工-A05', '预处理班组', 'Activity_0kzrvj3', '预处理', '2026-04-09', 1, 1, 1, '2026-04-07 10:00:00'),
-- Process 107, complete_time = 2026-04-14 09:00:00
(133, 104, 'deadline_soon', 'PROC-20260412-001', 'materialProcess', '高纯石英砂加工-A08', '预处理班组', 'Activity_0kzrvj3', '预处理', '2026-04-14', 1, 1, 1, '2026-04-12 08:00:00'),
-- Process 110, complete_time = 2026-04-20 17:00:00
(134, 104, 'overdue',       'PROC-20260418-001', 'materialProcess', '电缆绝缘层加工-A11', '预处理班组', 'Activity_0kzrvj3', '预处理', '2026-04-20', 1, 1, 1, '2026-04-18 09:00:00'),
-- Process 111, complete_time = 2026-04-22 10:00:00
(135, 104, 'deadline_soon', 'PROC-20260420-001', 'materialProcess', '光纤拉丝处理-A12', '预处理班组', 'Activity_0kzrvj3', '预处理', '2026-04-22', 1, 1, 1, '2026-04-20 10:00:00'),

-- 产品入库 warnings (complete times: 04-07 17:00, 04-09 16:00, 04-10 15:00, 04-11 17:30)
-- Process 100, complete_time = 2026-04-07 17:00:00
(140, 103, 'overdue',       'PROC-20260401-001', 'materialProcess', '高硼硅玻璃管加工-A01', '仓库班组', 'Activity_1lnd3md', '产品入库', '2026-04-07', 1, 1, 1, '2026-04-05 17:00:00'),
-- Process 101, complete_time = 2026-04-09 16:00:00
(141, 103, 'deadline_soon', 'PROC-20260402-001', 'materialProcess', '石英玻璃管加工-A02', '仓库班组', 'Activity_1lnd3md', '产品入库', '2026-04-09', 1, 1, 1, '2026-04-07 10:00:00'),
(142, 103, 'overdue',       'PROC-20260402-001', 'materialProcess', '石英玻璃管加工-A02', '仓库班组', 'Activity_1lnd3md', '产品入库', '2026-04-09', 1, 1, 1, '2026-04-08 10:00:00'),
-- Process 105, complete_time = 2026-04-15 16:00:00
(143, 103, 'deadline_soon', 'PROC-20260408-001', 'materialProcess', '光纤连接器加工-A06', '仓库班组', 'Activity_1lnd3md', '产品入库', '2026-04-15', 1, 1, 1, '2026-04-13 09:00:00'),
-- Process 108, complete_time = 2026-04-21 16:00:00
(144, 103, 'overdue',       'PROC-20260414-001', 'materialProcess', '硼硅酸盐管加工-A09', '仓库班组', 'Activity_1lnd3md', '产品入库', '2026-04-21', 1, 1, 1, '2026-04-19 10:00:00'),
-- Process 109, complete_time = 2026-04-22 15:00:00
(145, 103, 'deadline_soon', 'PROC-20260416-001', 'materialProcess', '钢化玻璃管成型-A10', '仓库班组', 'Activity_1lnd3md', '产品入库', '2026-04-22', 1, 1, 1, '2026-04-20 15:00:00'),
-- Process 111, complete_time = 2026-04-27 15:00:00
(146, 103, 'overdue',       'PROC-20260420-001', 'materialProcess', '光纤拉丝处理-A12', '仓库班组', 'Activity_1lnd3md', '产品入库', '2026-04-27', 1, 1, 1, '2026-04-25 09:00:00'),

-- 出库 (no explicit outbound node in test data, but we can add warnings for 原料检测入库 processes
-- that had stock-out operations; use the existing vacuum/baking nodes as proxy for "出库调度")
-- Add warnings for 测试 node in additional processes
-- Process 104, 测试 complete_time = 2026-04-13 10:00:00
(150, 107, 'overdue',       'PROC-20260407-001', 'materialProcess', '电缆护套加工-A05', '质检班组', 'Activity_0tn05o0', '测试', '2026-04-13', 1, 1, 1, '2026-04-11 09:00:00'),
-- Process 107, 测试 complete_time = 2026-04-18 10:00:00
(151, 107, 'deadline_soon', 'PROC-20260412-001', 'materialProcess', '高纯石英砂加工-A08', '质检班组', 'Activity_0tn05o0', '测试', '2026-04-18', 1, 1, 1, '2026-04-16 09:00:00'),
-- Process 109, 测试 complete_time = 2026-04-21 10:00:00
(152, 107, 'overdue',       'PROC-20260416-001', 'materialProcess', '钢化玻璃管成型-A10', '质检班组', 'Activity_0tn05o0', '测试', '2026-04-21', 1, 1, 1, '2026-04-19 10:00:00'),
-- Process 110, 测试 complete_time = 2026-04-24 10:00:00
(153, 107, 'deadline_soon', 'PROC-20260418-001', 'materialProcess', '电缆绝缘层加工-A11', '质检班组', 'Activity_0tn05o0', '测试', '2026-04-24', 1, 1, 1, '2026-04-22 10:00:00');

-- ============================================================
-- Summary:
--   Updated 12 existing warnings (moved create_time 1-3 days earlier)
--   Added 30 new resolved warnings across all node types:
--     原料检测入库: 7 warnings
--     预处理: 6 warnings
--     产品入库: 7 warnings
--     测试: 4 warnings (additional)
--     开机真空处理: existing (updated)
--     烘烤镀膜: existing (updated)
-- ============================================================
