-- ============================================================
-- 08_warning.sql — 预警系统模块
-- 说明：任务节点超时预警消息表（最终态DDL，含 admin_read）
-- 前置：05_workflow_execution.sql 已执行
-- 幂等：DROP/CREATE
-- ============================================================

DROP TABLE IF EXISTS `task_warning`;

CREATE TABLE `task_warning` (
  `id`            bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`       bigint(20)   NOT NULL COMMENT '接收人用户ID',
  `warn_type`     varchar(20)  NOT NULL COMMENT '预警类型: deadline_soon=即将超时, overdue=已超时',
  `proc_inst_id`  varchar(64)  NOT NULL COMMENT '流程实例ID',
  `node_key`      varchar(100) NOT NULL COMMENT '节点key(BPMN ID)',
  `node_name`     varchar(100) DEFAULT NULL COMMENT '节点名称',
  `end_date`      varchar(10)  NOT NULL COMMENT '节点计划结束日期 yyyy-MM-dd',
  `is_read`       tinyint(4)   NOT NULL DEFAULT 0 COMMENT '是否已读: 0=未读, 1=已读',
  `admin_read`    tinyint(1)   DEFAULT 0 COMMENT '管理员已读: 0=未读, 1=已读',
  `create_time`   datetime     DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`),
  KEY `idx_proc_node` (`proc_inst_id`, `node_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务节点超时预警消息';
