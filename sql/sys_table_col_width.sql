-- 表格列宽全局配置表（admin 统一配置，所有角色同步使用）
CREATE TABLE IF NOT EXISTS `sys_table_col_width` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `route_path` varchar(100) NOT NULL COMMENT '路由路径，如 /system/user',
  `table_id` varchar(50) NOT NULL DEFAULT 'main' COMMENT '表格标识，同一页面多表格时区分',
  `columns_config` json NOT NULL COMMENT '列宽配置JSON，格式: {"version":"1.0","columns":{"列名":宽度}}',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_route_table` (`route_path`, `table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表格列宽全局配置表';