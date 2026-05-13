# Flowable 数据恢复说明

这份说明用于恢复完整的 `flowable` 业务库，重点是避免 MySQL 表名大小写和 Flowable 自动建表把库结构弄乱。

## 固定约束

本项目要求 MySQL 在**初始化数据目录之前**满足以下条件：

- `lower_case_table_names=1`
- `character-set-server=utf8mb4`
- `collation-server=utf8mb4_unicode_ci`

这些参数已经固化在：

- `docker/mysql/conf.d/flowable.cnf`
- `docker-compose.prod.yml`

如果 MySQL 的数据目录曾经用别的 `lower_case_table_names` 初始化过，只改配置不够，必须删除旧数据目录后重新初始化。

## Flowable 配置

完整恢复备份时，后端必须保持：

- `ruoyi-admin/src/main/resources/application.yml`
- `flowable.database-schema-update: false`

原因是完整备份已经包含 Flowable 表结构和元数据，启动时再让 Flowable 自动补表，容易出现重复约束、大小写混用和版本元数据异常。

## 推荐恢复步骤

1. 启动 MySQL

```bash
docker compose -f docker-compose.prod.yml up -d mysql
```

2. 如果你要切换或修正 `lower_case_table_names`，先删除旧数据目录，再重建 MySQL  
   例如默认路径是 `MYSQL_DATA_PATH`，生产 compose 默认值是 `/data/ruoyi/mysql`

3. 执行恢复脚本

```bash
chmod +x bin/restore-flowable-db.sh
./bin/restore-flowable-db.sh
```

也可以显式指定 SQL 文件：

```bash
./bin/restore-flowable-db.sh /absolute/path/to/flowable_full.sql
```

## 恢复脚本会做什么

- 等待 MySQL 就绪
- 校验 `lower_case_table_names=1`
- 校验 `flowable.database-schema-update=false`
- 重建 `flowable` 数据库
- 导入 SQL 备份
- 校验 `ACT_GE_PROPERTY.schema.version=6.8.0.0`

## 常见问题

### 1. `Tables missing for component(s) engine, history`

通常是导入库里的 Flowable 系统表是小写，而当前 MySQL 又按大小写敏感方式运行。先检查：

```sql
SHOW VARIABLES LIKE 'lower_case_table_names';
```

如果不是 `1`，不要继续导入，先重建 MySQL 数据目录。

### 2. `Duplicate foreign key constraint name`

通常是完整备份已经包含 Flowable 表结构，但应用启动时仍然尝试自动建表或升级。确认：

```yaml
flowable:
  database-schema-update: false
```

### 3. `db version is null`

通常是 `ACT_GE_PROPERTY` 导入不完整，或启动过程中被错误改写。重新按本说明执行完整恢复。
