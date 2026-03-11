-- ============================================================
-- 04_team.sql — 班组管理模块
-- 说明：班组表 + 班组成员关联表 + 相关字典数据
-- 前置：02_org_and_roles.sql 已执行
-- 幂等：DROP/CREATE + INSERT IGNORE
-- ============================================================


-- ============================================================
-- 第一部分：业务表
-- ============================================================

DROP TABLE IF EXISTS `production_team_user`;
DROP TABLE IF EXISTS `production_team`;

-- 1. 产线班组表
CREATE TABLE `production_team` (
  `id`          bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `team_name`   varchar(100) NOT NULL COMMENT '班组名称',
  `leader_name` varchar(50)  DEFAULT NULL COMMENT '班组长姓名',
  `leader_id`   bigint(20)   DEFAULT NULL COMMENT '班组长用户ID',
  `team_status`  char(1)     NOT NULL DEFAULT '0' COMMENT '状态 (1=正常, 0=停用)',
  `create_by`   varchar(64)  DEFAULT '' COMMENT '创建者',
  `create_time`  datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`   varchar(64)  DEFAULT '' COMMENT '更新者',
  `update_time`  datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark`      varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='产线班组表';

-- 2. 班组成员关联表（多对多，含职位）
CREATE TABLE `production_team_user` (
  `team_id`  bigint(20)  NOT NULL COMMENT '班组ID',
  `user_id`  bigint(20)  NOT NULL COMMENT '用户ID',
  `position` varchar(50) DEFAULT NULL COMMENT '班组职位',
  PRIMARY KEY (`team_id`, `user_id`) COMMENT '联合主键防止重复关联'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班组-成员关联表';


-- ============================================================
-- 第二部分：字典类型
-- dict_id: 117 (team_status), 124 (team_position)
-- ============================================================
INSERT IGNORE INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(117, '班组状态', 'team_status',   '0', 'admin', NOW(), NULL),
(124, '班组职位', 'team_position', '0', 'admin', NOW(), '班组成员职位');


-- ============================================================
-- 第三部分：字典数据
-- ============================================================
INSERT IGNORE INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
-- team_status
(140, 0, '停用', '0', 'team_status', '', '', 'N', '0', 'admin', NOW(), ''),
(141, 0, '正常', '1', 'team_status', '', '', 'N', '0', 'admin', NOW(), ''),
(142, 3, '废弃', '2', 'team_status', '', '', 'N', '0', 'admin', NOW(), ''),
-- team_position
(169, 1, '班组长',     'team_leader',    'team_position', '', '', 'N', '0', 'admin', NOW(), ''),
(170, 2, '操作员',     'operator',       'team_position', '', '', 'N', '0', 'admin', NOW(), ''),
(171, 3, '质检员',     'inspector',      'team_position', '', '', 'N', '0', 'admin', NOW(), ''),
(172, 4, '设备管理员', 'equipment_mgr',  'team_position', '', '', 'N', '0', 'admin', NOW(), ''),
(173, 5, '安全员',     'safety_officer', 'team_position', '', '', 'N', '0', 'admin', NOW(), ''),
(174, 6, '技术员',     'technician',     'team_position', '', '', 'N', '0', 'admin', NOW(), ''),
(175, 7, '辅助工',     'assistant',      'team_position', '', '', 'N', '0', 'admin', NOW(), '');
