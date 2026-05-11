# 生产部署说明

本项目新增了一套独立的生产部署配置，不会改动现有开发配置。

## 新增内容

- 后端生产配置：`ruoyi-admin/src/main/resources/application-prod.yml`
- 后端镜像：`docker/backend/Dockerfile`
- 前端镜像：`docker/frontend/Dockerfile`
- Nginx 反向代理：`docker/nginx/default.conf`
- Docker Compose：`docker-compose.prod.yml`
- 本地打包脚本：`bin/build-prod.sh`
- 上传脚本：`bin/deploy-prod.sh`

## 生产配置说明

生产环境通过 `druid,prod` 两个 profile 启动：

- `druid` 继续复用现有数据库连接池相关配置
- `prod` 覆盖生产环境参数

关键生产参数：

- MySQL：Compose 内默认服务名 `mysql`
- Redis：Compose 内默认服务名 `redis`
- MySQL/Redis 密码：默认 `123456`
- 上传目录：默认 `/data/ruoyi/uploadPath`
- 日志目录：默认 `/data/ruoyi/logs`

## 打包

在项目根目录执行：

```bash
chmod +x bin/build-prod.sh
./bin/build-prod.sh
```

打包产物：

- 后端：`ruoyi-admin/target/ruoyi-admin.jar`
- 前端：`ruoyi-ui/dist`

上传脚本示例：

```bash
chmod +x bin/deploy-prod.sh
REMOTE_USER=root REMOTE_HOST=8.136.186.107 REMOTE_PATH=/data/ruoyi-flowable ./bin/deploy-prod.sh
```

## 服务器部署

生产 Compose 默认把 `mysql`、`redis`、`backend`、`frontend` 一起部署。

先在服务器准备目录：

```bash
mkdir -p /data/ruoyi/uploadPath
mkdir -p /data/ruoyi/logs
mkdir -p /data/ruoyi/mysql
mkdir -p /data/ruoyi/redis
mkdir -p /data/ruoyi-flowable/ruoyi-admin/target
mkdir -p /data/ruoyi-flowable/ruoyi-ui
```

把下面这些文件传到服务器 `/data/ruoyi-flowable`：

- `docker-compose.prod.yml`
- `docker/`
- `ruoyi-admin/target/ruoyi-admin.jar`
- `ruoyi-ui/dist/`

目录整理为：

```text
/data/ruoyi-flowable
├── docker
├── docker-compose.prod.yml
├── ruoyi-admin
│   └── target
│       └── ruoyi-admin.jar
└── ruoyi-ui
    └── dist
```

然后执行：

```bash
cd /data/ruoyi-flowable
docker compose -f docker-compose.prod.yml up -d --build
```

## 访问方式

- 前端首页：`http://8.136.186.107/`
- 后端接口：`http://8.136.186.107/prod-api`
- 上传文件：`http://8.136.186.107/profile/...`

## 注意事项

1. 服务器需要已安装 Docker 和 Docker Compose。
2. 宿主机的 `80` 端口不能被其他服务占用。
3. Compose 会自动拉起 MySQL 8 和 Redis 7，并使用密码 `123456`。
4. Liquibase 首次启动会执行 `flowable_full.sql` 初始化表和数据，首次启动时间会更长。
