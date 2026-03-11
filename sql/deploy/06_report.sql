-- ============================================================
-- 06_report.sql — 测试报告模块
-- 说明：报告模板表 + 报告记录表 + 相关字典数据
-- 来源：测试报告.sql + 测试报告管理.sql + dict_test_type.sql + 预警.sql(uploader字段)
-- 前置：02_org_and_roles.sql 已执行
-- 幂等：DROP/CREATE + INSERT IGNORE
-- ============================================================


-- ============================================================
-- 第一部分：业务表
-- ============================================================

DROP TABLE IF EXISTS `report_record`;
DROP TABLE IF EXISTS `report_template`;

-- 1. 测试报告模板配置表
CREATE TABLE `report_template` (
  `id`              bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_code`   varchar(64)  NOT NULL COMMENT '模板唯一编码',
  `template_name`   varchar(100) NOT NULL COMMENT '模板名称',
  `template_type`   varchar(32)  DEFAULT NULL COMMENT '模板引擎类型 (如: FREEMARKER, JASPER, POI)',
  `test_type`       varchar(32)  DEFAULT NULL COMMENT '业务测试类型',
  `storage_path`    varchar(500) NOT NULL COMMENT '模板文件存储路径/URL',
  `param_config`    json         DEFAULT NULL COMMENT '模板参数定义(JSON格式)',
  `report_status`   tinyint(4)   NOT NULL DEFAULT 1 COMMENT '状态 (1=启用, 0=停用)',
  `remark`          varchar(500) DEFAULT '' COMMENT '备注说明',
  `create_by`       varchar(64)  DEFAULT '' COMMENT '创建者',
  `create_time`     datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`       varchar(64)  DEFAULT '' COMMENT '更新者',
  `update_time`     datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_code` (`template_code`) COMMENT '模板编码唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试报告模板配置表';

-- 2. 测试报告管理表（最终态，含 material_name/material_quantity/node_name/uploader）
CREATE TABLE `report_record` (
  `id`                bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `report_code`       varchar(64)   NOT NULL COMMENT '报告唯一编码，格式 RPT-yyyyMMdd-XXXX',
  `report_name`       varchar(100)  NOT NULL COMMENT '报告名称',
  `test_type`         varchar(32)   DEFAULT NULL COMMENT '测试类型（复用 test_type 字典）',
  `storage_path`      varchar(500)  NOT NULL COMMENT '上传文件存储路径',
  `material_name`     varchar(100)  DEFAULT NULL COMMENT '关联物料名称',
  `material_quantity` decimal(12,2) DEFAULT NULL COMMENT '关联物料数量',
  `node_name`         varchar(100)  DEFAULT NULL COMMENT '上传所在节点名称',
  `uploader`          varchar(64)   DEFAULT '' COMMENT '实际上传人用户名',
  `remark`            varchar(500)  DEFAULT '' COMMENT '备注',
  `create_by`         varchar(64)   DEFAULT '' COMMENT '审批通过时操作人',
  `create_time`       datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`         varchar(64)   DEFAULT '' COMMENT '更新者',
  `update_time`       datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report_code` (`report_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试报告管理表';


-- ============================================================
-- 第二部分：字典类型
-- dict_id: 114 (report_status), 115 (template_type), 116 (test_type)
-- ============================================================
INSERT IGNORE INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(114, '报告模板状态', 'report_status', '0', 'admin', NOW(), NULL),
(115, '报告文件类型', 'template_type', '0', 'admin', NOW(), NULL),
(116, '业务测试类型', 'test_type',     '0', 'admin', NOW(), NULL);


-- ============================================================
-- 第三部分：字典数据
-- dict_code: 133-152
-- ============================================================
INSERT IGNORE INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
-- report_status
(133, 0, '停用', '0', 'report_status', '', 'danger',  'N', '0', 'admin', NOW(), ''),
(134, 0, '启用', '1', 'report_status', '', 'success', 'Y', '0', 'admin', NOW(), ''),
-- template_type
(135, 0, 'Excel表格',   'EXCEL',  'template_type', '', '', 'N', '0', 'admin', NOW(), ''),
(136, 0, 'Word文档',    'WORD',   'template_type', '', '', 'N', '0', 'admin', NOW(), ''),
(137, 0, 'PDF文档',     'PDF',    'template_type', '', '', 'N', '0', 'admin', NOW(), ''),
(138, 0, '自定义引擎',  'CUSTOM', 'template_type', '', '', 'N', '0', 'admin', NOW(), ''),
-- test_type（原 dict_test_type.sql 中的 UNIT 类型 + 7 个节点类型）
(139, 0, '单元测试', 'UNIT', 'test_type', '', '',        'N', '0', 'admin', NOW(), ''),
(146, 1, '原料检测入库', '原料检测入库', 'test_type', '', 'primary', 'N', '0', 'admin', NOW(), '原料检测入库节点'),
(147, 2, '出库',         '出库',         'test_type', '', 'warning', 'N', '0', 'admin', NOW(), '出库节点'),
(148, 3, '预处理',       '预处理',       'test_type', '', 'info',    'N', '0', 'admin', NOW(), '预处理节点'),
(149, 4, '真空处理',     '真空处理',     'test_type', '', 'info',    'N', '0', 'admin', NOW(), '真空处理节点'),
(150, 5, '烘烤',         '烘烤',         'test_type', '', 'info',    'N', '0', 'admin', NOW(), '烘烤节点'),
(151, 6, '检测',         '检测',         'test_type', '', 'success', 'N', '0', 'admin', NOW(), '检测节点'),
(152, 7, '产品入库',     '产品入库',     'test_type', '', 'primary', 'N', '0', 'admin', NOW(), '产品入库节点');
