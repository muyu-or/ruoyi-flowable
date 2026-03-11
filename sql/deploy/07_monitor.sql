-- ============================================================
-- 07_monitor.sql — 监控管理模块
-- 说明：监控设备表 + 录像备份表 + 监控菜单 + 字典数据
-- 前置：02_org_and_roles.sql 已执行
-- 幂等：DROP/CREATE + INSERT IGNORE
-- ============================================================


-- ============================================================
-- 第一部分：业务表
-- ============================================================

DROP TABLE IF EXISTS `monitor_backup`;
DROP TABLE IF EXISTS `monitor_device`;

-- 1. 监控设备表
CREATE TABLE `monitor_device` (
  `id`            bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_name`   varchar(100) NOT NULL COMMENT '设备名称',
  `device_no`     varchar(64)  NOT NULL COMMENT '设备编号（如 CAM-001）',
  `location`      varchar(200) DEFAULT NULL COMMENT '安装位置',
  `area_type`     char(1)      DEFAULT NULL COMMENT '区域类型（字典 monitor_area_type）',
  `device_type`   char(1)      DEFAULT NULL COMMENT '设备类型（字典 monitor_device_type）',
  `brand`         varchar(100) DEFAULT NULL COMMENT '品牌型号',
  `ip_address`    varchar(50)  DEFAULT NULL COMMENT 'IP地址',
  `stream_url`    varchar(500) DEFAULT NULL COMMENT '视频流地址',
  `status`        char(1)      DEFAULT '1' COMMENT '设备状态（1在线 2离线 3故障）',
  `install_date`  date         DEFAULT NULL COMMENT '安装日期',
  `manager`       varchar(100) DEFAULT NULL COMMENT '负责人',
  `create_by`     varchar(64)  DEFAULT '' COMMENT '创建者',
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`     varchar(64)  DEFAULT '' COMMENT '更新者',
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark`        varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_no` (`device_no`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='监控设备表';

-- 2. 录像备份表
CREATE TABLE `monitor_backup` (
  `id`            bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id`     bigint(20)   NOT NULL COMMENT '关联设备ID',
  `device_name`   varchar(100) DEFAULT NULL COMMENT '设备名称（冗余）',
  `record_date`   date         NOT NULL COMMENT '录像日期',
  `start_time`    datetime     DEFAULT NULL COMMENT '录像开始时间',
  `end_time`      datetime     DEFAULT NULL COMMENT '录像结束时间',
  `file_name`     varchar(255) DEFAULT NULL COMMENT '文件名',
  `file_path`     varchar(500) DEFAULT NULL COMMENT '文件存储路径',
  `file_size`     bigint(20)   DEFAULT NULL COMMENT '文件大小（字节）',
  `backup_status` char(1)      DEFAULT '0' COMMENT '备份状态（0待备份 1已备份 2备份失败）',
  `create_by`     varchar(64)  DEFAULT '' COMMENT '创建者',
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`     varchar(64)  DEFAULT '' COMMENT '更新者',
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark`        varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_record_date` (`record_date`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='录像备份表';


-- ============================================================
-- 第二部分：监控菜单（menu_id 3100-3113）
-- 挂在"系统监控"(menu_id=2) 下
-- ============================================================
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
(3100, '监控管理', 2,    6, 'cameraMonitor', NULL,                   NULL, NULL, 1, 0, 'M', '1', '0', '',                     'button',        'admin', NOW(), '', NULL, ''),
(3101, '监控设备', 3100, 1, 'device',        'monitor/device/index',  NULL, NULL, 1, 0, 'C', '1', '0', 'monitor:device:list',  'druid',         'admin', NOW(), '', NULL, ''),
(3102, '设备查询', 3101, 1, '#',             NULL,                    NULL, NULL, 1, 0, 'F', '0', '0', 'monitor:device:query',  '#',            'admin', NOW(), '', NULL, ''),
(3103, '设备新增', 3101, 2, '#',             NULL,                    NULL, NULL, 1, 0, 'F', '0', '0', 'monitor:device:add',    '#',            'admin', NOW(), '', NULL, ''),
(3104, '设备修改', 3101, 3, '#',             NULL,                    NULL, NULL, 1, 0, 'F', '0', '0', 'monitor:device:edit',   '#',            'admin', NOW(), '', NULL, ''),
(3105, '设备删除', 3101, 4, '#',             NULL,                    NULL, NULL, 1, 0, 'F', '0', '0', 'monitor:device:remove', '#',            'admin', NOW(), '', NULL, ''),
(3106, '设备导出', 3101, 5, '#',             NULL,                    NULL, NULL, 1, 0, 'F', '0', '0', 'monitor:device:export', '#',            'admin', NOW(), '', NULL, ''),
(3107, '录像备份', 3100, 2, 'backup',        'monitor/backup/index',  NULL, NULL, 1, 0, 'C', '1', '0', 'monitor:backup:list',  'documentation', 'admin', NOW(), '', NULL, ''),
(3108, '备份查询', 3107, 1, '#',             NULL,                    NULL, NULL, 1, 0, 'F', '0', '0', 'monitor:backup:query',  '#',            'admin', NOW(), '', NULL, ''),
(3109, '备份新增', 3107, 2, '#',             NULL,                    NULL, NULL, 1, 0, 'F', '0', '0', 'monitor:backup:add',    '#',            'admin', NOW(), '', NULL, ''),
(3110, '备份修改', 3107, 3, '#',             NULL,                    NULL, NULL, 1, 0, 'F', '0', '0', 'monitor:backup:edit',   '#',            'admin', NOW(), '', NULL, ''),
(3111, '备份删除', 3107, 4, '#',             NULL,                    NULL, NULL, 1, 0, 'F', '0', '0', 'monitor:backup:remove', '#',            'admin', NOW(), '', NULL, ''),
(3112, '备份导出', 3107, 5, '#',             NULL,                    NULL, NULL, 1, 0, 'F', '0', '0', 'monitor:backup:export', '#',            'admin', NOW(), '', NULL, ''),
(3113, '实时监控', 2,    3, 'liveMonitor',   'monitor/live/index',    NULL, NULL, 1, 0, 'C', '0', '0', 'monitor:live:list',    'eye-open',     'admin', NOW(), '', NULL, '');


-- ============================================================
-- 第三部分：字典类型
-- dict_id: 120-123
-- ============================================================
INSERT IGNORE INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(120, '监控区域类型', 'monitor_area_type',     '0', 'admin', NOW(), '监控设备所在区域类型'),
(121, '监控设备类型', 'monitor_device_type',   '0', 'admin', NOW(), '监控设备摄像头类型'),
(122, '设备状态',     'monitor_device_status', '0', 'admin', NOW(), '监控设备在线状态'),
(123, '备份状态',     'monitor_backup_status', '0', 'admin', NOW(), '录像备份状态');


-- ============================================================
-- 第四部分：字典数据
-- dict_code: 153-168
-- ============================================================
INSERT IGNORE INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
-- monitor_area_type
(153, 1, '原料库',   '1', 'monitor_area_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(154, 2, '生产车间', '2', 'monitor_area_type', '', 'success', 'N', '0', 'admin', NOW(), ''),
(155, 3, '成品库',   '3', 'monitor_area_type', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(156, 4, '大门',     '4', 'monitor_area_type', '', 'info',    'N', '0', 'admin', NOW(), ''),
(157, 5, '办公区',   '5', 'monitor_area_type', '', 'info',    'N', '0', 'admin', NOW(), ''),
(158, 6, '其他',     '6', 'monitor_area_type', '', 'info',    'N', '0', 'admin', NOW(), ''),
-- monitor_device_type
(159, 1, '枪机', '1', 'monitor_device_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(160, 2, '球机', '2', 'monitor_device_type', '', 'success', 'N', '0', 'admin', NOW(), ''),
(161, 3, '半球', '3', 'monitor_device_type', '', 'info',    'N', '0', 'admin', NOW(), ''),
(162, 4, '全景', '4', 'monitor_device_type', '', 'warning', 'N', '0', 'admin', NOW(), ''),
-- monitor_device_status
(163, 1, '在线', '1', 'monitor_device_status', '', 'success', 'Y', '0', 'admin', NOW(), ''),
(164, 2, '离线', '2', 'monitor_device_status', '', 'danger',  'N', '0', 'admin', NOW(), ''),
(165, 3, '故障', '3', 'monitor_device_status', '', 'warning', 'N', '0', 'admin', NOW(), ''),
-- monitor_backup_status
(166, 1, '待备份',   '0', 'monitor_backup_status', '', 'info',    'N', '0', 'admin', NOW(), ''),
(167, 2, '已备份',   '1', 'monitor_backup_status', '', 'success', 'Y', '0', 'admin', NOW(), ''),
(168, 3, '备份失败', '2', 'monitor_backup_status', '', 'danger',  'N', '0', 'admin', NOW(), '');
