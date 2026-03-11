-- ============================================================
-- 05_workflow_execution.sql — 工作流执行追踪
-- 说明：流程节点绑定 + 流程执行记录 + 节点级执行记录（最终态DDL）
-- 来源：flow_team_execution.sql + database_schema_upgrade.sql 合并
-- 前置：04_team.sql 已执行
-- 幂等：DROP/CREATE
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `task_node_execution`;
DROP TABLE IF EXISTS `task_execution_record`;
DROP TABLE IF EXISTS `process_node_binding`;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. 流程节点班组绑定配置表
CREATE TABLE `process_node_binding` (
  `id`                   bigint(20)   NOT NULL AUTO_INCREMENT,
  `proc_def_key`         varchar(100) NOT NULL,
  `proc_def_version`     int(11)      NOT NULL DEFAULT 1,
  `node_key`             varchar(100) NOT NULL,
  `node_name`            varchar(100) DEFAULT NULL,
  `allow_team_selection` tinyint(1)   NOT NULL DEFAULT 1 COMMENT '是否允许发起时选择班组',
  `default_team_id`      bigint(20)   DEFAULT NULL COMMENT '默认班组ID',
  `form_field_name`      varchar(100) DEFAULT NULL,
  `create_by`            varchar(64)  DEFAULT NULL,
  `create_time`          datetime     DEFAULT NULL,
  `update_by`            varchar(64)  DEFAULT NULL,
  `update_time`          datetime     DEFAULT NULL,
  `remark`               varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_proc_node` (`proc_def_key`, `proc_def_version`, `node_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程节点班组绑定配置';

-- 2. 流程执行总记录表（每次发起一条）
CREATE TABLE `task_execution_record` (
  `id`               bigint(20)  NOT NULL AUTO_INCREMENT,
  `proc_inst_id`     varchar(64) NOT NULL,
  `proc_def_key`     varchar(100) DEFAULT NULL,
  `proc_def_version` int(11)     DEFAULT NULL,
  `initiator_id`     bigint(20)  DEFAULT NULL,
  `main_team_id`     bigint(20)  DEFAULT NULL,
  `status`           varchar(20) NOT NULL DEFAULT 'running'
                     COMMENT '流程状态：running/completed/rejected/abandoned/timeout',
  `complete_time`    datetime    DEFAULT NULL
                     COMMENT '流程完成时间',
  `total_duration`   bigint(20)  DEFAULT 0
                     COMMENT '流程总耗时（秒）',
  `create_by`        varchar(64)  DEFAULT NULL,
  `create_time`      datetime     DEFAULT NULL,
  `update_by`        varchar(64)  DEFAULT NULL,
  `update_time`      datetime     DEFAULT NULL,
  `remark`           varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_proc_inst` (`proc_inst_id`),
  KEY `idx_status` (`status`),
  KEY `idx_complete_time` (`complete_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程执行总记录';

-- 3. 节点级任务执行记录表（最终态，含所有升级字段）
CREATE TABLE `task_node_execution` (
  `id`                bigint(20)   NOT NULL AUTO_INCREMENT,
  `exec_record_id`    bigint(20)   NOT NULL,
  `task_id`           varchar(64)  DEFAULT NULL,
  `node_key`          varchar(100) DEFAULT NULL,
  `node_name`         varchar(100) DEFAULT NULL,
  `assigned_team_id`  bigint(20)   DEFAULT NULL,
  `assigned_user_id`  bigint(20)   DEFAULT NULL,
  `claim_user_id`     bigint(20)   DEFAULT NULL,
  `status`            varchar(20)  NOT NULL DEFAULT 'pending'
                      COMMENT 'pending/claimed/completed/rejected',
  `start_time`        datetime     DEFAULT NULL,
  `claim_time`        datetime     DEFAULT NULL,
  `complete_time`     datetime     DEFAULT NULL,
  `form_data`         text         COMMENT '节点表单数据JSON',
  `result`            varchar(20)  DEFAULT 'pending'
                      COMMENT '任务结果：pending/completed/rejected/abandoned/overdue',
  `approve_comment`   text         COMMENT '审批意见或完成说明',
  `process_duration`  bigint(20)   DEFAULT 0
                      COMMENT '处理耗时（秒）',
  `timeout_flag`      tinyint(4)   NOT NULL DEFAULT 0
                      COMMENT '是否超时: 0=正常, 1=已超时',
  `plan_end_date`     varchar(10)  DEFAULT NULL
                      COMMENT '计划结束日期 yyyy-MM-dd',
  `plan_start_date`   varchar(20)  DEFAULT NULL
                      COMMENT '计划开始日期',
  `create_by`         varchar(64)  DEFAULT NULL,
  `create_time`       datetime     DEFAULT NULL,
  `update_by`         varchar(64)  DEFAULT NULL,
  `update_time`       datetime     DEFAULT NULL,
  `remark`            varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_exec_record` (`exec_record_id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_result` (`result`),
  KEY `idx_assigned_team_id` (`assigned_team_id`),
  CONSTRAINT `fk_tne_record` FOREIGN KEY (`exec_record_id`)
    REFERENCES `task_execution_record` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点级任务执行记录';
