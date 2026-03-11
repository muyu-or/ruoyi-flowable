# 部署脚本执行顺序

> 新服务器上按以下顺序执行 SQL 文件，即可完整还原整个系统的数据库结构。

## 前置条件

- MySQL 5.7+ / 8.0
- 创建数据库：`CREATE DATABASE flowable DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;`
- Flowable 引擎表由应用启动时自动创建（`database-schema-update: true`）

## 执行顺序

| 步骤 | 文件 | 说明 |
|------|------|------|
| 1 | `../ry_20240629.sql` | RuoYi 基础系统表（用户/角色/菜单/字典等） |
| 2 | `../quartz.sql` | Quartz 调度器表 |
| 3 | `../tony-flowable.sql` | Flowable 扩展表 + 流程管理基础菜单 |
| 4 | `01_system_extension.sql` | sys_form / sys_deploy_form 字段扩展 |
| 5 | `02_org_and_roles.sql` | 业务部门 / 角色 / 所有业务菜单 / 角色菜单关联 / Quartz 任务 |
| 6 | `03_inventory.sql` | 库存管理（inventory / stock_in / stock_out + 字典） |
| 7 | `04_team.sql` | 班组管理（production_team / production_team_user + 字典） |
| 8 | `05_workflow_execution.sql` | 工作流执行追踪（process_node_binding / task_execution_record / task_node_execution） |
| 9 | `06_report.sql` | 测试报告（report_template / report_record + 字典） |
| 10 | `07_monitor.sql` | 监控管理（monitor_device / monitor_backup + 菜单 + 字典） |
| 11 | `08_warning.sql` | 预警系统（task_warning） |

## 快速执行（命令行）

```bash
mysql -u root -p flowable < ry_20240629.sql
mysql -u root -p flowable < quartz.sql
mysql -u root -p flowable < tony-flowable.sql
cd deploy
mysql -u root -p flowable < 01_system_extension.sql
mysql -u root -p flowable < 02_org_and_roles.sql
mysql -u root -p flowable < 03_inventory.sql
mysql -u root -p flowable < 04_team.sql
mysql -u root -p flowable < 05_workflow_execution.sql
mysql -u root -p flowable < 06_report.sql
mysql -u root -p flowable < 07_monitor.sql
mysql -u root -p flowable < 08_warning.sql
```

## 测试数据（可选）

`../testdata/` 目录下的文件提供示例数据，按编号顺序执行：

```bash
cd ../testdata
mysql -u root -p flowable < 01_inventory_data.sql
mysql -u root -p flowable < 02_stock_in_data.sql
mysql -u root -p flowable < 03_stock_out_data.sql
mysql -u root -p flowable < 04_task_execution_record.sql
mysql -u root -p flowable < 05_task_node_execution.sql
mysql -u root -p flowable < 06_stock_in_extra.sql
mysql -u root -p flowable < 07_stock_out_extra.sql
```

## 注意事项

- `flow_form_management.sql` 的 5 张表（flow_form_def 等）**暂未纳入部署脚本**，已移入 `../archive/` 备查
- 所有 `dict_id` / `dict_code` / `menu_id` 为固定值，与现有数据库保持一致
- `sys_listener_event_type` 字典（dict_id 104）已包含在 `tony-flowable.sql` 中，不重复插入
- 部署脚本均为幂等设计，重复执行不会产生副作用
