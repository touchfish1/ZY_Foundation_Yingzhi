# ZY_Nexus_Congcong/Services/Api

Origin: ZHANGYUAN - Module: CONGCONG (Backend API)

该目录用于承载 Project ZHANGYUAN 第一阶段的 Spring Boot API。

## Target Stack

- Java 21
- Spring Boot 3
- PostgreSQL
- Redis
- Flyway
- JWT

## Planned Modules

```text
auth
cms
asset
product
order
payment
system
```

第一阶段采用模块化单体，后续可按业务边界拆分服务。

## Auth MVP

当前已实现后台认证基础能力：

- 默认管理员初始化。
- 默认 `super_admin` 角色初始化。
- 默认权限码初始化。
- BCrypt 密码哈希。
- HMAC-SHA256 JWT。
- `/admin/auth/login` 登录接口。
- `/admin/auth/me` 当前用户接口。

默认开发管理员：

```text
username: admin
password: admin123
```

生产环境必须通过环境变量覆盖：

```text
JWT_SECRET
DEFAULT_ADMIN_USERNAME
DEFAULT_ADMIN_PASSWORD
DEFAULT_ADMIN_NICKNAME
```

登录：

```http
POST /admin/auth/login
```

请求：

```json
{
  "username": "admin",
  "password": "admin123"
}
```

当前用户：

```http
GET /admin/auth/me
Authorization: Bearer <accessToken>
```

## CMS MVP

当前已实现 CMS 后端最小闭环：

- 页面创建。
- 页面列表。
- 页面详情。
- 多语言草稿保存。
- 页面版本生成。
- 页面发布。
- 前台已发布页面渲染。
- 默认区块定义初始化。

后台接口需要 JWT：

```http
GET  /admin/cms/pages
POST /admin/cms/pages
GET  /admin/cms/pages/{pageId}
PUT  /admin/cms/pages/{pageId}/translations/{locale}/draft
POST /admin/cms/pages/{pageId}/translations/{locale}/publish
GET  /admin/cms/block-definitions
```

前台渲染接口公开访问：

```http
GET /api/cms/pages/render?path=/plans&locale=zh-CN
```

## Local Verification

需要本机安装 Java 21。

```bash
gradle test
```

如果 Gradle 提示找不到 Java 21，请先安装 JDK 21 并配置 `JAVA_HOME`。

## Local Run

先启动本地依赖服务：

```bash
docker compose -f ../../../ZY_Foundation_Yingzhi/Docker/docker-compose.yml up -d
```

再启动 API：

```bash
gradle bootRun
```

如果 Docker Compose 运行在 WSL，并且 Windows 需要连接 WSL IP，可以使用 `wsl` profile：

```bash
gradle bootRun --args='--spring.profiles.active=wsl'
```

默认 WSL 中间件地址：

```text
PostgreSQL: 172.23.161.5:5432
Redis: 172.23.161.5:6379
MinIO: http://172.23.161.5:9000
```

也可以通过环境变量覆盖：

```bash
$env:DB_HOST='172.23.161.5'
$env:REDIS_HOST='172.23.161.5'
$env:MINIO_ENDPOINT='http://172.23.161.5:9000'
gradle bootRun
```

健康检查：

```text
GET http://localhost:8080/api/system/ping
GET http://localhost:8080/actuator/health
```

## WSL Connectivity Check

如果 Windows 侧无法访问 WSL 中的 Docker Compose 服务，先检查端口：

```powershell
Test-NetConnection -ComputerName 172.23.161.5 -Port 5432
Test-NetConnection -ComputerName 172.23.161.5 -Port 6379
Test-NetConnection -ComputerName 172.23.161.5 -Port 9000
```

如果 `TcpTestSucceeded` 为 `False`，优先确认：

- WSL 内部 `docker compose ps` 显示端口已发布。
- Compose 使用了 `5432:5432`、`6379:6379`、`9000:9000`。
- Windows 防火墙没有拦截 WSL 端口。
- 尝试从 Windows 使用 `localhost:5432`、`localhost:6379`、`localhost:9000` 访问。
