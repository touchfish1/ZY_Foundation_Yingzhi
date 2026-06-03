# 部署与 CI/CD

## 部署路线

第一阶段使用 Docker Compose，降低部署和开发复杂度。后续业务稳定后迁移到 Kubernetes，并通过 CI/CD 自动构建和发布。

## Docker Compose 服务

第一版服务：

```text
postgres
redis
minio
api
admin
web
nginx
```

当前仓库已提供本地开发用 Compose 文件：

```text
ZY_Foundation_Yingzhi/Docker/docker-compose.yml
```

当前 Compose 先启动基础依赖：

```text
postgres
redis
minio
```

API 仍建议本地用 Gradle 启动，方便开发调试。等后端核心接口稳定后，再补 API、Admin、Web、Nginx 镜像化配置。

本地持久化目录：

```text
ZY_Foundation_Yingzhi/Docker/data/postgres
ZY_Foundation_Yingzhi/Docker/data/redis
ZY_Foundation_Yingzhi/Docker/data/minio
```

这些目录只用于本地开发，不提交真实数据。

## WSL Docker Compose

如果 Docker Compose 在 WSL 中启动，而 API 在 Windows 侧启动，可以使用后端的 `wsl` profile：

```bash
gradle bootRun --args='--spring.profiles.active=wsl'
```

当前约定 WSL 中间件地址：

```text
172.23.161.5
```

对应服务：

```text
PostgreSQL: 172.23.161.5:5432
Redis: 172.23.161.5:6379
MinIO: http://172.23.161.5:9000
```

如果 Windows 侧无法连接该 IP 的端口，说明容器端口没有从 WSL 暴露到 Windows，或被防火墙拦截。此时可以优先尝试 `localhost`，WSL2 通常会将发布端口转发到 Windows localhost。

RabbitMQ 暂不作为第一版必需组件。等发布事件、支付回调、异步日志和业务通知稳定后再加入。

## 推荐端口

```text
nginx:    80 / 443
api:      8080
admin:    静态资源，由 nginx 托管
web:      静态资源或 Nuxt server，由 nginx 代理
postgres: 5432
redis:    6379
minio:    9000 / 9001
```

## 环境变量

后端核心环境变量：

```text
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

DB_HOST=postgres
DB_PORT=5432
DB_NAME=zhangyuan
DB_USERNAME=zhangyuan
DB_PASSWORD=change-me

REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=

JWT_SECRET=change-me
JWT_EXPIRES_SECONDS=7200

MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=change-me
MINIO_SECRET_KEY=change-me
MINIO_BUCKET=zhangyuan-assets

PAYMENT_MOCK_ENABLED=true
```

生产环境要求：

- `JWT_SECRET` 必须使用高强度随机值。
- 数据库密码、MinIO 密钥不能提交到仓库。
- K8s 中敏感配置必须放入 Secret。

## 内存建议

单机 Docker 起步：

```text
最低：1C2G，可跑通 MVP，但需要控制组件数量。
推荐：2C4G，适合小规模上线和持续开发。
```

Spring Boot JVM 建议：

```text
JAVA_OPTS=-Xms256m -Xmx768m
```

低内存策略：

- API 先保持单体，减少服务数量。
- Admin 和 Web 构建静态资源，由 Nginx 托管。
- Redis 只缓存已发布页面快照和必要会话数据。
- 不引入 Elasticsearch、Kafka、重型低代码引擎。
- 图片和附件放 MinIO。

## Kubernetes 演进

K8s 组件：

```text
Ingress
api Deployment
admin Deployment 或静态资源镜像
web Deployment
ConfigMap
Secret
Service
PersistentVolumeClaim
```

生产环境建议：

- PostgreSQL 优先使用云数据库或独立高可用数据库。
- Redis 可先单实例，后续上高可用。
- MinIO 可先单节点，后续切换对象存储服务。
- API 至少 2 个副本。
- 所有服务配置 readinessProbe 和 livenessProbe。

## CI/CD 流程

推荐分支：

```text
main
feature/*
```

流水线：

```text
checkout
lint
test
build api
build admin
build web
docker build
docker push
deploy
health check
```

镜像命名：

```text
registry.example.com/zhangyuan-api:<commit-sha>
registry.example.com/zhangyuan-admin:<commit-sha>
registry.example.com/zhangyuan-web:<commit-sha>
```

不要在生产环境使用 `latest`。

## 发布策略

Docker Compose 阶段：

```text
构建镜像
拉取镜像
执行数据库 migration
重启 API
重启 Web/Admin
健康检查
```

K8s 阶段：

```text
构建镜像
推送镜像
helm upgrade
等待 rollout 完成
执行健康检查
失败自动回滚
```

## 健康检查

API 健康检查：

```http
GET /actuator/health
```

建议暴露：

```text
/actuator/health
/actuator/info
/actuator/metrics
```

不要公开敏感 actuator endpoint。
