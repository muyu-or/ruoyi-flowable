-- ============================================================
-- 01_system_extension.sql — 系统表字段扩展
-- 说明：为 sys_form / sys_deploy_form 添加自定义组件支持字段
-- 前置：ry_20240629.sql + tony-flowable.sql 已执行
-- 幂等：通过 INFORMATION_SCHEMA 判断列是否存在，重复执行安全
-- ============================================================

-- 1. sys_form 添加 form_type 字段
SET @db_name = DATABASE();
SET @tbl = 'sys_form';
SET @col = 'form_type';
SET @exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = @tbl AND COLUMN_NAME = @col);
SET @sql = IF(@exists = 0,
    'ALTER TABLE sys_form ADD COLUMN form_type VARCHAR(20) NOT NULL DEFAULT ''vform'' COMMENT ''表单类型：vform=vform设计器表单，component=自定义Vue组件''',
    'SELECT ''Column sys_form.form_type already exists, skipped.''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. sys_form 添加 form_component 字段
SET @col = 'form_component';
SET @exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = @tbl AND COLUMN_NAME = @col);
SET @sql = IF(@exists = 0,
    'ALTER TABLE sys_form ADD COLUMN form_component VARCHAR(100) NULL COMMENT ''自定义Vue组件名，form_type=component时有效，如StockOutForm''',
    'SELECT ''Column sys_form.form_component already exists, skipped.''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. sys_deploy_form 添加 form_component 字段
SET @tbl = 'sys_deploy_form';
SET @col = 'form_component';
SET @exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = @tbl AND COLUMN_NAME = @col);
SET @sql = IF(@exists = 0,
    'ALTER TABLE sys_deploy_form ADD COLUMN form_component VARCHAR(100) NULL COMMENT ''自定义Vue表单组件名（与form_id互斥，如 MainForm、StockInForm）''',
    'SELECT ''Column sys_deploy_form.form_component already exists, skipped.''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
