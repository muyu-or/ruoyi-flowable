-- 1.测试报告模板表
CREATE TABLE `report_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_code` varchar(64) NOT NULL COMMENT '模板唯一编码 ',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `template_type` varchar(32) DEFAULT NULL COMMENT '模板引擎类型 (如: FREEMARKER, JASPER, POI)',
  `test_type` varchar(32) DEFAULT NULL COMMENT '业务测试类型 (如: UNIT, INTEGRATION, API)',
  `storage_path` varchar(500) NOT NULL COMMENT '模板文件存储路径/URL',
  `param_config` json DEFAULT NULL COMMENT '模板参数定义(JSON格式，用于前端生成表单)',
  `report_status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 (1=启用, 0=停用)',
  `remark` varchar(500) DEFAULT '' COMMENT '备注说明',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_code` (`template_code`) COMMENT '模板编码唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试报告模板配置表';
