-- 1. 产线班组表
DROP TABLE IF EXISTS `production_team`;
CREATE TABLE `production_team` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `team_name` varchar(100) NOT NULL COMMENT '班组名称',
  `leader_name` varchar(50) DEFAULT NULL COMMENT '班组长姓名',
  `leader_id` bigint(20) DEFAULT NULL COMMENT '班组长用户ID ',
  `team_status` char(1) NOT NULL DEFAULT '0' COMMENT '状态 (1=正常, 0=停用)',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='产线班组表';


-- 2. 班组成员关联表 (多对多关联)
DROP TABLE IF EXISTS `production_team_user`;
CREATE TABLE `production_team_user` (
  `team_id` bigint(20) NOT NULL COMMENT '班组ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`team_id`, `user_id`) COMMENT '联合主键防止重复关联'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班组-成员关联表';