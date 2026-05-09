-- 测试类型字典数据更新
-- 只保留新流程节点名称，删除旧的"真空处理"、"烘烤镀膜"

-- 1. 删除旧的测试类型（真空处理、烘烤镀膜）
DELETE FROM sys_dict_data WHERE dict_type = 'test_type' AND dict_value IN ('真空处理', '烘烤镀膜');

-- 2. 新增测试类型（项目任务书、镀膜工装设计、膜系设计、开机真空烘烤）
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES
(200, 10, '项目任务书', '项目任务书', 'test_type', '', 'default', 'N', '0', 'admin', NOW(), '项目任务书报告模板'),
(201, 11, '镀膜工装设计', '镀膜工装设计', 'test_type', '', 'default', 'N', '0', 'admin', NOW(), '镀膜工装设计报告模板'),
(202, 12, '膜系设计', '膜系设计', 'test_type', '', 'default', 'N', '0', 'admin', NOW(), '膜系设计报告模板'),
(203, 13, '开机真空烘烤', '开机真空烘烤', 'test_type', '', 'default', 'N', '0', 'admin', NOW(), '开机真空烘烤报告模板')
ON DUPLICATE KEY UPDATE dict_label = VALUES(dict_label), dict_sort = VALUES(dict_sort), status = '0';

-- 3. 验证结果
SELECT dict_code, dict_sort, dict_label, dict_value, status FROM sys_dict_data WHERE dict_type = 'test_type' ORDER BY dict_sort;