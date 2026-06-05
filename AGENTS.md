# AGENTS.md — 中元项目

AI 模型 API 服务平台。多模块单体仓库，根目录无 `package.json`。

## 活跃入口

| 目录 | 技术栈 | 端口 |
|------|--------|------|
| `frontend/web` | Nuxt 3.15 | 3000 |
| `frontend/admin` | Vite 6 + Vue 3.5 + Naive UI | 5173 |
| `backend/api` | Spring Boot 3.3.5 + Java 21 | **8088** |
| `backend/gateway` | Spring Cloud Gateway | 8080 |
| `backend/auth-service` | Spring Boot | 8082 |
| `backend/system-service` | Spring Boot | 8081 |
| `backend/user-service` | Spring Boot | 8085 |
| `backend/order-service` | Spring Boot | 8083 |
| `backend/payment-service` | Spring Boot | 8084 |

`domain/` 和 `security/` 仅有 README（占位）。`docs/` 有架构/API/部署文档。

## 常用命令

```bash
# 后端 API（单体 DDD 模块）
cd backend/api && ./gradlew bootRun
./gradlew test
./gradlew test --tests "com.zhangyuan.architecture.*"

# 微服务（各自目录）
cd backend/{auth,system,user,order,payment}-service && ./gradlew bootRun
cd backend/gateway && ./gradlew bootRun

# system-service 测试在 CI 中单独运行
cd backend/system-service && ./gradlew test

# 前端
cd frontend/web && npm run dev          # 0.0.0.0:3000
cd frontend/admin && npm run dev        # 0.0.0.0:5173

# CI 类型检查（本地验证用）
npx nuxt typecheck                      # web
npx vue-tsc -b --noEmit                 # admin

# 种子数据（PowerShell）
scripts/seed-test-data.ps1 [-BaseUrl http://localhost:8080]
```

## 后端关键约定

- **API 端口 8088**（不是 8080）。Gateway 监听 8080 路由到各服务。
- **必须启动 Nacos**（`100.125.148.23:8848`），服务发现依赖它。已禁用配置导入检查。
- **Sa-Token** 负责鉴权：`/admin/**` 需登录 + `@SaCheckPermission`，`/api/**` 和 `/actuator/**` 公开。Spring Security 已配置为全部放行。
- **Flyway** 管理 API 的数据库迁移（`backend/api/src/main/resources/db/migration/V*.sql`）。auth-service 和 system-service 中已禁用 Flyway。
- **统一响应：** `{ code, message, data }`；分页：`data.items, page, pageSize, total`。
- **Admin** Vite 代理 → `http://localhost:8088`（直连 API，非 Gateway）。
- **Web** 通过 `NUXT_PUBLIC_API_BASE` 读后端地址（默认 `http://localhost:8080`，即 Gateway）。

## DDD 六边形架构

`backend/api` 内模块（`modules/{asset,cms,order,payment,product}`）遵循：
`domain/{model,repository,service}/` → `application/service/` → `adapter/in/rest/` + `adapter/out/persistence/`

- 领域模型为纯 POJO，**无**框架注解。**未继承** `common/dddframework/` 基类。
- 旧版 Spring Data JPA `repository/` 与新仓库并存。
- `auth` 和 `system` DDD 模块**尚未创建**（它们在独立微服务中）。

## 前端约定

- **Admin**：`vite.config.ts` 代理 `/admin`、`/api`、`/actuator` → `localhost:8088`。`request()` 封装，token 存 `localStorage: zhangyuan_admin_token`。
- **Web**：`pages/[...slug].vue` 调用 `GET /api/cms/pages/render?path=...&locale=...`，按 `block.type` 映射到 `components/blocks/*`，未知类型→ `UnknownBlock`。`pages/index.vue` 是手写首页，不受 CMS 控制。
- 共享类型位于 `frontend/shared/types/`，两前端通过路径别名引用。

## CI（GitHub Actions）

`.github/workflows/ci.yml`，仅 `main` 分支 push/PR 触发。Node 22 + JDK 21。

**四个并行检查：** `test-api`、`test-system-service`、`lint-admin`（`vue-tsc -b --noEmit`）、`lint-web`（`nuxt typecheck`）。全部通过后 `build` + `docker`（仅 main）。

**⚠ Docker 镜像构建上下文为 `ZY_View_Migu/{Admin,Web}`（CI 自有目录），非 `frontend/`。本地无此目录。**

## 环境坑点

- **WSL IP `100.125.148.23`** 硬编码在各配置文件中。可通过 `DB_HOST`、`REDIS_HOST`、`MINIO_ENDPOINT`、`NACOS_HOST` 环境变量覆盖。
- **Docker 中间件已在 WSL 常驻运行**，通常无需手动启动/停止。如需重建：`docker compose -f infrastructure/docker/docker-compose.yml up -d`；全栈追加 `-f ...docker-compose.app.yml`。
- **⚠ Nacos 日志（`standalone-logs/`）未被 .gitignore 覆盖**，注意不要提交。
- **Nuxt 缓存问题：** 报 `"Failed to resolve import \"#app-manifest\""` 时，终止旧 Node 进程，删除 `.nuxt/.output` 后重启。
- **生产必须覆盖 `JWT_SECRET` 和 `DEFAULT_ADMIN_PASSWORD`。**
