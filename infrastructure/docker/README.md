# infrastructure/docker

Origin: ZHANGYUAN - Module: YINGZHI (Docker)

该目录用于 Docker Compose、Nginx 配置和本地部署说明。

MVP 阶段目标是一键启动：

```text
postgres
redis
minio
api
admin
web
nginx
```

## Local Infrastructure

当前 `docker-compose.yml` 先提供本地开发依赖服务：

```text
postgres
redis
minio
```

启动：

```bash
docker compose up -d
```

停止：

```bash
docker compose down
```

清理数据卷：

```bash
docker compose down
```

如需清理持久化数据，删除 `./data/postgres`、`./data/redis`、`./data/minio`。

默认连接信息：

```text
PostgreSQL: localhost:5432 / zhangyuan / zhangyuan / zhangyuan
Redis: localhost:6379
MinIO API: http://localhost:9000
MinIO Console: http://localhost:9001
MinIO User: zhangyuan
MinIO Password: zhangyuan-minio-secret
```

## Persistent Data

本地持久化目录：

```text
./data/postgres
./data/redis
./data/minio
```

这些目录由 `.gitignore` 忽略，不应提交真实数据。
