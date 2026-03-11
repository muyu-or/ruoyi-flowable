-- ============================================================
-- 02_org_and_roles.sql — 组织架构、角色、菜单、权限关联
-- 说明：初始化业务部门、角色、所有业务菜单、角色-菜单关联、Quartz任务
-- 前置：ry_20240629.sql + tony-flowable.sql 已执行
-- 幂等：DELETE/INSERT 模式，重复执行安全
-- ============================================================


-- ============================================================
-- 第一部分：部门（sys_dept）
-- 清理若依示例部门，保留根部门 dept_id=100 并重命名
-- ============================================================
DELETE FROM sys_dept WHERE dept_id IN (101,102,103,104,105,106,107,108,109);

UPDATE sys_dept
SET dept_name  = '自动化生产有限公司',
    leader     = '',
    phone      = '',
    email      = '',
    update_by  = 'admin',
    update_time = NOW()
WHERE dept_id = 100;

INSERT IGNORE INTO sys_dept
    (dept_id, parent_id, ancestors, dept_name, order_num,
     leader, phone, email, status, del_flag,
     create_by, create_time, update_by, update_time)
VALUES
    (200, 100, '0,100', '仓储部',       1, '', '', '', '0', '0', 'admin', NOW(), '', NULL),
    (201, 100, '0,100', '预处理部',     2, '', '', '', '0', '0', 'admin', NOW(), '', NULL),
    (202, 100, '0,100', '设备操作部',   3, '', '', '', '0', '0', 'admin', NOW(), '', NULL),
    (203, 100, '0,100', '质量检测部',   4, '', '', '', '0', '0', 'admin', NOW(), '', NULL),
    (204, 100, '0,100', '生产管理部',   5, '', '', '', '0', '0', 'admin', NOW(), '', NULL),
    (205, 100, '0,100', '总经理',       6, '', '', '', '0', '0', 'admin', NOW(), '', NULL),
    (206, 100, '0,100', '总裁办',       1, '', '', '', '0', '0', 'admin', NOW(), '', NULL),
    (207, 100, '0,100', '生产总管',     7, '', '', '', '0', '0', 'admin', NOW(), '', NULL);


-- ============================================================
-- 第二部分：角色（sys_role）
-- role_id: 10-14 业务角色, 100 生产总管
-- ============================================================
INSERT IGNORE INTO sys_role
    (role_id, role_name, role_key, role_sort,
     data_scope, menu_check_strictly, dept_check_strictly,
     status, del_flag, create_by, create_time, update_by, update_time, remark)
VALUES
    (10,  '仓库管理员',   'warehouse',         3, '1', 1, 1, '0', '0', 'admin', NOW(), '', NULL, '负责原料入库、出库、成品入库'),
    (11,  '预处理操作员', 'preprocess',         4, '1', 1, 1, '0', '0', 'admin', NOW(), '', NULL, '负责原料预处理工序'),
    (12,  '设备操作员',   'device_operator',    5, '1', 1, 1, '0', '0', 'admin', NOW(), '', NULL, '负责真空处理及烘烤链条设备操作'),
    (13,  '质量检测员',   'quality_inspector',  6, '1', 1, 1, '0', '0', 'admin', NOW(), '', NULL, '负责产品质量测试与判定'),
    (14,  '班组长',       'team_leader',        7, '1', 1, 1, '0', '0', 'admin', NOW(), '', NULL, '可审核/退回任意节点，查看班组统计'),
    (100, '生产总管',     'zongguan',           8, '1', 1, 1, '0', '0', 'admin', NOW(), '', NULL, NULL);


-- ============================================================
-- 第三部分：业务菜单（sys_menu）
-- 来源：数据库实际导出，menu_id 2020-3123
-- 注意：tony-flowable.sql 已包含 menu_id 2000-2019 的流程基础菜单
--       ry_20240629.sql 已包含系统管理菜单（menu_id < 2000）
-- ============================================================

-- ---- 3.1 流程管理 & 任务管理菜单（2020-2047）----
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
(2020, '生产流程管理', 0,    8, 'flowable',   NULL,                              NULL, NULL, 1, 0, 'M', '0', '0', '',                         'cascader',   'admin', NOW(), '', NULL, ''),
(2022, '生产流程定义', 2020, 2, 'definition', 'flowable/definition/index',        NULL, NULL, 1, 0, 'C', '0', '0', '',                         'job',        'admin', NOW(), '', NULL, ''),
(2023, '生产任务管理', 0,    9, 'task',       NULL,                              NULL, NULL, 1, 0, 'M', '0', '0', '',                         'dict',       'admin', NOW(), '', NULL, ''),
(2024, '待办生产任务', 2023, 2, 'todo',       'flowable/task/todo/index',         NULL, NULL, 1, 1, 'C', '0', '0', '',                         'cascader',   'admin', NOW(), '', NULL, ''),
(2025, '已发生产任务', 2023, 3, 'finished',   'flowable/task/finished/index',     NULL, NULL, 1, 1, 'C', '0', '0', '',                         'time-range', 'admin', NOW(), '', NULL, ''),
(2026, '生产任务下发', 2023, 1, 'process',    'flowable/task/myProcess/index',    NULL, NULL, 1, 1, 'C', '0', '0', '',                         'guide',      'admin', NOW(), '', NULL, ''),
(2027, '生产表单配置', 2020, 2, 'form',       'flowable/task/form/index',         NULL, NULL, 1, 1, 'C', '0', '0', 'flowable:form:list',       'form',       'admin', NOW(), '', NULL, ''),
(2028, '新增',         2027, 1, '',           NULL,                              NULL, NULL, 1, 0, 'F', '0', '0', 'flowable:form:add',        '#',          'admin', NOW(), '', NULL, ''),
(2029, '删除',         2027, 3, '',           NULL,                              NULL, NULL, 1, 0, 'F', '0', '0', 'flowable:form:remove',     '#',          'admin', NOW(), '', NULL, ''),
(2030, '编辑',         2027, 2, '',           NULL,                              NULL, NULL, 1, 0, 'F', '0', '0', 'flowable:form:edit',       '#',          'admin', NOW(), '', NULL, ''),
(2031, '新增',         2026, 1, '',           NULL,                              NULL, NULL, 1, 0, 'F', '0', '0', 'system:deployment:add',    '#',          'admin', NOW(), '', NULL, ''),
(2032, '编辑',         2026, 2, '',           NULL,                              NULL, NULL, 1, 0, 'F', '0', '0', 'system:deployment:edit',   '#',          'admin', NOW(), '', NULL, ''),
(2033, '删除',         2026, 3, '',           NULL,                              NULL, NULL, 1, 0, 'F', '0', '0', 'system:deployment:remove', '#',          'admin', NOW(), '', NULL, ''),
(2034, '查询',         2027, 4, '',           NULL,                              NULL, NULL, 1, 0, 'F', '0', '0', 'flowable:form:query',      '#',          'admin', NOW(), '', NULL, ''),
(2035, '修改密码',     100,  8, '',           NULL,                              NULL, NULL, 1, 0, 'F', '0', '0', 'system:user:updatePwd',    '#',          'admin', NOW(), '', NULL, ''),
(2036, '流程表达式',   2020, 3, 'expression', 'flowable/expression/index',        NULL, NULL, 1, 1, 'C', '0', '0', 'system:expression:list',  'list',       'admin', NOW(), '', NULL, ''),
(2037, '流程达式查询', 2036, 1, '#',          '',                                NULL, NULL, 1, 0, 'F', '0', '0', 'system:expression:query',  '#',          'admin', NOW(), '', NULL, ''),
(2038, '流程达式新增', 2036, 2, '#',          '',                                NULL, NULL, 1, 0, 'F', '0', '0', 'system:expression:add',    '#',          'admin', NOW(), '', NULL, ''),
(2039, '流程达式修改', 2036, 3, '#',          '',                                NULL, NULL, 1, 0, 'F', '0', '0', 'system:expression:edit',   '#',          'admin', NOW(), '', NULL, ''),
(2040, '流程达式删除', 2036, 4, '#',          '',                                NULL, NULL, 1, 0, 'F', '0', '0', 'system:expression:remove', '#',          'admin', NOW(), '', NULL, ''),
(2041, '流程达式导出', 2036, 5, '#',          '',                                NULL, NULL, 1, 0, 'F', '0', '0', 'system:expression:export', '#',          'admin', NOW(), '', NULL, ''),
(2042, '流程监听',     2020, 4, 'listener',   'flowable/listener/index',          NULL, NULL, 1, 0, 'C', '0', '0', 'system:listener:list',    'monitor',    'admin', NOW(), '', NULL, ''),
(2043, '流程监听查询', 2042, 1, '#',          '',                                NULL, NULL, 1, 0, 'F', '0', '0', 'system:listener:query',    '#',          'admin', NOW(), '', NULL, ''),
(2044, '流程监听新增', 2042, 2, '#',          '',                                NULL, NULL, 1, 0, 'F', '0', '0', 'system:listener:add',      '#',          'admin', NOW(), '', NULL, ''),
(2045, '流程监听修改', 2042, 3, '#',          '',                                NULL, NULL, 1, 0, 'F', '0', '0', 'system:listener:edit',     '#',          'admin', NOW(), '', NULL, ''),
(2046, '流程监听删除', 2042, 4, '#',          '',                                NULL, NULL, 1, 0, 'F', '0', '0', 'system:listener:remove',   '#',          'admin', NOW(), '', NULL, ''),
(2047, '流程监听导出', 2042, 5, '#',          '',                                NULL, NULL, 1, 0, 'F', '0', '0', 'system:listener:export',   '#',          'admin', NOW(), '', NULL, '');

-- ---- 3.2 库存管理菜单（2048-2109）----
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
(2048, '库存管理',     0,    2, 'inventory', NULL,                     NULL, NULL, 1, 0, 'M', '0', '0', '',                       'tool',     'admin', NOW(), '', NULL, ''),
(2049, '库存信息',     2048, 1, 'inventory', 'manage/inventory/index',  NULL, NULL, 1, 0, 'C', '0', '0', 'manage:inventory:list',  'tab',      'admin', NOW(), '', NULL, ''),
(2050, '入库记录',     2048, 2, 'stockin',   'manage/stockin/index',    NULL, NULL, 1, 0, 'C', '0', '0', 'manage:stockin:list',    'build',    'admin', NOW(), '', NULL, ''),
(2051, '出库记录',     2048, 3, 'stockout',  'manage/stockout/index',   NULL, NULL, 1, 0, 'C', '0', '0', 'manage:stockout:list',   'checkbox',  'admin', NOW(), '', NULL, ''),
(2093, '库存信息查询', 2049, 1, '#',         '',                        NULL, NULL, 1, 0, 'F', '0', '0', 'manage:inventory:query', '#',        'admin', NOW(), '', NULL, ''),
(2094, '库存信息新增', 2049, 2, '#',         '',                        NULL, NULL, 1, 0, 'F', '0', '0', 'manage:inventory:add',   '#',        'admin', NOW(), '', NULL, ''),
(2095, '库存信息修改', 2049, 3, '#',         '',                        NULL, NULL, 1, 0, 'F', '0', '0', 'manage:inventory:edit',  '#',        'admin', NOW(), '', NULL, ''),
(2096, '库存信息删除', 2049, 4, '#',         '',                        NULL, NULL, 1, 0, 'F', '0', '0', 'manage:inventory:remove','#',        'admin', NOW(), '', NULL, ''),
(2097, '库存信息导出', 2049, 5, '#',         '',                        NULL, NULL, 1, 0, 'F', '0', '0', 'manage:inventory:export','#',        'admin', NOW(), '', NULL, ''),
(2099, '入库记录查询', 2050, 1, '#',         '',                        NULL, NULL, 1, 0, 'F', '0', '0', 'manage:stockin:query',   '#',        'admin', NOW(), '', NULL, ''),
(2100, '入库记录新增', 2050, 2, '#',         '',                        NULL, NULL, 1, 0, 'F', '0', '0', 'manage:stockin:add',     '#',        'admin', NOW(), '', NULL, ''),
(2101, '入库记录修改', 2050, 3, '#',         '',                        NULL, NULL, 1, 0, 'F', '0', '0', 'manage:stockin:edit',    '#',        'admin', NOW(), '', NULL, ''),
(2102, '入库记录删除', 2050, 4, '#',         '',                        NULL, NULL, 1, 0, 'F', '0', '0', 'manage:stockin:remove',  '#',        'admin', NOW(), '', NULL, ''),
(2103, '入库记录导出', 2050, 5, '#',         '',                        NULL, NULL, 1, 0, 'F', '0', '0', 'manage:stockin:export',  '#',        'admin', NOW(), '', NULL, ''),
(2105, '出库记录查询', 2051, 1, '#',         '',                        NULL, NULL, 1, 0, 'F', '0', '0', 'manage:stockout:query',  '#',        'admin', NOW(), '', NULL, ''),
(2106, '出库记录新增', 2051, 2, '#',         '',                        NULL, NULL, 1, 0, 'F', '0', '0', 'manage:stockout:add',    '#',        'admin', NOW(), '', NULL, ''),
(2107, '出库记录修改', 2051, 3, '#',         '',                        NULL, NULL, 1, 0, 'F', '0', '0', 'manage:stockout:edit',   '#',        'admin', NOW(), '', NULL, ''),
(2108, '出库记录删除', 2051, 4, '#',         '',                        NULL, NULL, 1, 0, 'F', '0', '0', 'manage:stockout:remove', '#',        'admin', NOW(), '', NULL, ''),
(2109, '出库记录导出', 2051, 5, '#',         '',                        NULL, NULL, 1, 0, 'F', '0', '0', 'manage:stockout:export', '#',        'admin', NOW(), '', NULL, '');

-- ---- 3.3 报告管理菜单（2070-2115）----
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
(2070, '报告管理',               0,    3, 'report',   NULL,                       NULL, NULL, 1, 0, 'M', '0', '0', '',                      'list',          'admin', NOW(), '', NULL, ''),
(2071, '报告模板',               2070, 1, 'template', 'manage/template/index',     NULL, NULL, 1, 0, 'C', '0', '0', 'manage:template:list',  'documentation', 'admin', NOW(), '', NULL, ''),
(2111, '测试报告模板配置查询',   2071, 1, '#',        '',                          NULL, NULL, 1, 0, 'F', '0', '0', 'manage:template:query',  '#',            'admin', NOW(), '', NULL, ''),
(2112, '测试报告模板配置新增',   2071, 2, '#',        '',                          NULL, NULL, 1, 0, 'F', '0', '0', 'manage:template:add',    '#',            'admin', NOW(), '', NULL, ''),
(2113, '测试报告模板配置修改',   2071, 3, '#',        '',                          NULL, NULL, 1, 0, 'F', '0', '0', 'manage:template:edit',   '#',            'admin', NOW(), '', NULL, ''),
(2114, '测试报告模板配置删除',   2071, 4, '#',        '',                          NULL, NULL, 1, 0, 'F', '0', '0', 'manage:template:remove', '#',            'admin', NOW(), '', NULL, ''),
(2115, '测试报告模板配置导出',   2071, 5, '#',        '',                          NULL, NULL, 1, 0, 'F', '0', '0', 'manage:template:export', '#',            'admin', NOW(), '', NULL, '');

-- ---- 3.4 班组管理菜单（2078-2091）----
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
(2078, '班组管理',     0,    2, 'team', NULL,                   NULL, NULL, 1, 0, 'M', '0', '0', '',                   'job',   'admin', NOW(), '', NULL, ''),
(2079, '班组信息',     2078, 1, 'team', 'manage/team/index',    NULL, NULL, 1, 0, 'C', '0', '0', 'manage:team:list',   'build', 'admin', NOW(), '', NULL, ''),
(2087, '班组信息查询', 2079, 1, '#',    '',                     NULL, NULL, 1, 0, 'F', '0', '0', 'manage:team:query',  '#',     'admin', NOW(), '', NULL, ''),
(2088, '班组信息新增', 2079, 2, '#',    '',                     NULL, NULL, 1, 0, 'F', '0', '0', 'manage:team:add',    '#',     'admin', NOW(), '', NULL, ''),
(2089, '班组信息修改', 2079, 3, '#',    '',                     NULL, NULL, 1, 0, 'F', '0', '0', 'manage:team:edit',   '#',     'admin', NOW(), '', NULL, ''),
(2090, '班组信息删除', 2079, 4, '#',    '',                     NULL, NULL, 1, 0, 'F', '0', '0', 'manage:team:remove', '#',     'admin', NOW(), '', NULL, ''),
(2091, '班组信息导出', 2079, 5, '#',    '',                     NULL, NULL, 1, 0, 'F', '0', '0', 'manage:team:export', '#',     'admin', NOW(), '', NULL, '');

-- ---- 3.5 统计/首页/日历菜单（3000-3001, 3114-3123）----
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
(3000, '生产任务统计', 2023, 4, 'dashboard', 'flowable/stat/dashboard/index', NULL, NULL, 1, 1, 'C', '0', '0', 'flowable:stat:dashboard', 'chart',     'admin', NOW(), '', NULL, ''),
(3001, '查看统计',     3000, 1, '#',         NULL,                            NULL, NULL, 1, 0, 'F', '0', '0', 'flowable:stat:dashboard', '#',         'admin', NOW(), '', NULL, ''),
(3114, '首页',         0,    0, 'index',     'home/index',                    NULL, NULL, 1, 0, 'M', '0', '0', '',                        'dashboard', 'admin', NOW(), '', NULL, ''),
(3116, '数据统计中心', 0,    1, 'bi/index',  'bi/index',                      NULL, NULL, 1, 0, 'C', '0', '0', '',                        'chart',     'admin', NOW(), '', NULL, ''),
(3117, '日历看板',     0,    7, 'calendar',  'calendar/index',                NULL, NULL, 1, 0, 'C', '0', '0', 'flowable:calendar:view',  'date',      'admin', NOW(), '', NULL, ''),
(3118, '日历查看',     3114, 1, '',          '',                              NULL, NULL, 1, 0, 'F', '0', '0', 'flowable:calendar:view',  '#',         'admin', NOW(), '', NULL, ''),
(3119, '首页查看',     3116, 1, '',          '',                              NULL, NULL, 1, 0, 'F', '0', '0', 'flowable:stat:dashboard', '#',         'admin', NOW(), '', NULL, ''),
(3120, '统计查看',     3117, 1, '',          '',                              NULL, NULL, 1, 0, 'F', '0', '0', 'flowable:stat:dashboard', '#',         'admin', NOW(), '', NULL, ''),
(3121, '查看全部数据', 3114, 2, '',          '',                              NULL, NULL, 1, 0, 'F', '0', '0', 'flowable:stat:all',       '#',         'admin', NOW(), '', NULL, ''),
(3122, '用户树查询',   2079, 6, '',          '',                              NULL, NULL, 1, 0, 'F', '0', '0', 'system:user:list',        '#',         'admin', NOW(), '', NULL, ''),
(3123, '角色下拉查询', 2079, 7, '',          '',                              NULL, NULL, 1, 0, 'F', '0', '0', 'system:role:query',       '#',         'admin', NOW(), '', NULL, '');

-- ---- 3.6 报告记录菜单（3002-3007）----
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
(3002, '报告管理', 2070, 2, 'record', 'manage/report/index', NULL, NULL, 1, 0, 'C', '0', '0', 'manage:report:list',  'form', 'admin', NOW(), '', NULL, ''),
(3003, '查询',     3002, 1, '#',      NULL,                  NULL, NULL, 1, 0, 'F', '0', '0', 'manage:report:list',  '#',    'admin', NOW(), '', NULL, ''),
(3004, '详情',     3002, 2, '#',      NULL,                  NULL, NULL, 1, 0, 'F', '0', '0', 'manage:report:query', '#',    'admin', NOW(), '', NULL, ''),
(3005, '新增',     3002, 3, '#',      NULL,                  NULL, NULL, 1, 0, 'F', '0', '0', 'manage:report:add',   '#',    'admin', NOW(), '', NULL, ''),
(3006, '修改',     3002, 4, '#',      NULL,                  NULL, NULL, 1, 0, 'F', '0', '0', 'manage:report:edit',  '#',    'admin', NOW(), '', NULL, ''),
(3007, '删除',     3002, 5, '#',      NULL,                  NULL, NULL, 1, 0, 'F', '0', '0', 'manage:report:remove','#',    'admin', NOW(), '', NULL, '');

-- 注意：监控菜单（3100-3113）在 07_monitor.sql 中随表一起插入


-- ============================================================
-- 第四部分：角色-菜单关联（sys_role_menu）
-- 先清理再插入，保证幂等
-- ============================================================
DELETE FROM sys_role_menu WHERE role_id IN (10, 11, 12, 13, 14, 100);

-- 仓库管理员 (role_id=10) — 31 menus
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(10,2023),(10,2024),(10,2025),(10,2026),(10,2048),(10,2049),(10,2050),(10,2051),
(10,2070),(10,2071),(10,2093),(10,2094),(10,2097),(10,2099),(10,2100),(10,2103),
(10,2105),(10,2106),(10,2109),(10,2111),(10,2115),(10,3000),(10,3001),(10,3002),
(10,3003),(10,3004),(10,3005),(10,3006),(10,3007),(10,3114),(10,3117);

-- 预处理操作员 (role_id=11) — 18 menus
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(11,2023),(11,2024),(11,2025),(11,2026),(11,2070),(11,2071),(11,2111),(11,2115),
(11,3000),(11,3001),(11,3002),(11,3003),(11,3004),(11,3005),(11,3006),(11,3007),
(11,3114),(11,3117);

-- 设备操作员 (role_id=12) — 18 menus
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(12,2023),(12,2024),(12,2025),(12,2026),(12,2070),(12,2071),(12,2111),(12,2115),
(12,3000),(12,3001),(12,3002),(12,3003),(12,3004),(12,3005),(12,3006),(12,3007),
(12,3114),(12,3117);

-- 质量检测员 (role_id=13) — 18 menus
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(13,2023),(13,2024),(13,2025),(13,2026),(13,2070),(13,2071),(13,2111),(13,2115),
(13,3000),(13,3001),(13,3002),(13,3003),(13,3004),(13,3005),(13,3006),(13,3007),
(13,3114),(13,3117);

-- 班组长 (role_id=14) — 25 menus
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(14,2023),(14,2024),(14,2025),(14,2026),(14,2048),(14,2049),(14,2050),(14,2051),
(14,2070),(14,2071),(14,2093),(14,2099),(14,2105),(14,2111),(14,2115),(14,3000),
(14,3001),(14,3002),(14,3003),(14,3004),(14,3005),(14,3006),(14,3007),(14,3114),
(14,3117);

-- 生产总管 (role_id=100) — 91 menus
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(100,2),(100,2020),(100,2022),(100,2023),(100,2024),(100,2025),(100,2026),(100,2027),
(100,2028),(100,2029),(100,2030),(100,2031),(100,2032),(100,2033),(100,2034),(100,2036),
(100,2037),(100,2038),(100,2039),(100,2040),(100,2041),(100,2042),(100,2043),(100,2044),
(100,2045),(100,2046),(100,2047),(100,2048),(100,2049),(100,2050),(100,2051),(100,2070),
(100,2071),(100,2078),(100,2079),(100,2087),(100,2088),(100,2089),(100,2090),(100,2091),
(100,2093),(100,2094),(100,2095),(100,2096),(100,2097),(100,2099),(100,2100),(100,2101),
(100,2102),(100,2103),(100,2105),(100,2106),(100,2107),(100,2108),(100,2109),(100,2111),
(100,2112),(100,2113),(100,2114),(100,2115),(100,3000),(100,3001),(100,3002),(100,3003),
(100,3004),(100,3005),(100,3006),(100,3007),(100,3100),(100,3101),(100,3102),(100,3103),
(100,3104),(100,3105),(100,3106),(100,3107),(100,3108),(100,3109),(100,3110),(100,3111),
(100,3112),(100,3113),(100,3114),(100,3116),(100,3117),(100,3118),(100,3119),(100,3120),
(100,3121),(100,3122),(100,3123);


-- ============================================================
-- 第五部分：Quartz 预警扫描任务（sys_job）
-- ============================================================
INSERT IGNORE INTO sys_job
    (job_id, job_name, job_group, invoke_target, cron_expression,
     misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES
    (100, '任务超时预警扫描', 'DEFAULT', 'taskWarningTask.scanWarnings()', '0 5 0 * * ?',
     '1', '0', '0', 'admin', NOW(), '每天00:05扫描即将到期和已超期的节点，推送预警消息');
