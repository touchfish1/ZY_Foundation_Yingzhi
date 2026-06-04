# AGENTS.md — 中元项目

## 项目布局
- 多模块单体仓库，根目录无 `package.json`。顶层目录按功能划分。
- **活跃入口：** `frontend/web`（Nuxt 3，端口 3000）、`frontend/admin`（Vite+Vue 3+NaiveUI，端口 5173）、`backend/api`（Spring Boot 3.3.5+Java21，**端口 8088**）、`infrastructure/docker/`。
- **真实微服务**（非占位符）：`backend/auth-service`（8082）、`backend/system-service`（8081）、`backend/gateway`（Spring Cloud Gateway，8080）。
- `domain/` 和 `security/` 仅有 README；`gateway` 有真实代码。
- DDD 模块位于 `backend/api/.../modules/{asset,cms,order,payment,product}`。`auth` 和 `system` 模块尚未创建。

## 常用命令
- **API：** `./gradlew bootRun` / `./gradlew test` / `./gradlew test --tests "com.zhangyuan.architecture.*"` — 在 `backend/api/` 目录下执行。
- **Auth/System/Gateway：** 各自目录下执行 `./gradlew bootRun`。
- **Web：** `npm run dev`（固定 `0.0.0.0:3000`）、`npm run build`、CI 类型检查：`npx nuxt typecheck`。
- **Admin：** `npm run dev`（固定 `0.0.0.0:5173`）、`npm run build`（含 `vue-tsc -b`）、CI 类型检查：`npx vue-tsc -b --noEmit`。
- **中间件（WSL Docker）：** `docker compose -f infrastructure/docker/docker-compose.yml up -d`。全栈：追加 `-f infrastructure/docker/docker-compose.app.yml`。
- **种子数据：** `scripts/seed-test-data.ps1 [-BaseUrl http://localhost:8080]`。

## 后端
- **API 端口为 8088**（不是 8080）。Gateway 监听 8080，路由到所有服务。
- **必须启动 Nacos**（服务发现+配置中心，地址 `100.125.148.23:8848`）。已禁用 Nacos 配置导入检查（`import-check.enabled: false`），但服务发现仍需 Nacos。
- **Sa-Token**（非 Spring Security）负责鉴权：`/admin/**` 需登录，`/api/**` 和 `/actuator/**` 公开。Spring Security 已配置为全部放行。
- Admin Vite 代理目标为 `http://localhost:8088`（直接连 API，非 Gateway）。
- **Web** 通过 `NUXT_PUBLIC_API_BASE` 读取后端地址（默认 `http://localhost:8080`，即 Gateway）。
- **Flyway** 管理数据库迁移，脚本位于 `backend/api/src/main/resources/db/migration/V*.sql`。auth-service 和 system-service 中已禁用 Flyway。
- **统一响应格式：** `{ code, message, data }`；分页数据中 `data` 使用 `{ items, page, pageSize, total }`。
- **三类端点：** `/admin/**`（Sa-Token + `@SaCheckPermission`）、`/api/**`（公开）、`/api/ddd/**`（DDD 骨架验证，公开）。

## DDD 与架构
- 模块遵循六边形架构：`domain/{model,repository,service}/` → `application/service/` → `adapter/in/rest/` + `adapter/out/persistence/`。旧版 `repository/`（Spring Data JPA）与新仓库并存。
- 领域模型为普通 POJO，**未继承** `common/dddframework/` 基类（框架存在但未使用）。
- **架构测试：** `com.zhangyuan.architecture.DddLayerTest`（ArchUnit）+ `ModularityTest`（Spring Modulith）。执行：`./gradlew test --tests "com.zhangyuan.architecture.*"`。
- `ModularityTest` 扫描 `com.zhangyuan.modules`，已发现模块：`asset`、`cms`、`order`、`payment`、`product`。

## 前端约定
- **Admin** 通过 `vite.config.ts` 代理 `/admin`、`/api`、`/actuator` → `http://localhost:8088`。使用 `request()` 封装，token 存 `localStorage` 的 `zhangyuan_admin_token` 键。
- **Web** 动态 CMS 页面：`pages/[...slug].vue` 调用 `GET /api/cms/pages/render?path=...&locale=...`，按 `block.type` 映射到 `components/blocks/*`，未知类型→ `UnknownBlock`。**`pages/index.vue` 是手写首页，不受 CMS 控制。**
- 共享类型位于 `frontend/shared/types/`，两个前端应用通过路径别名引用。

## 环境坑点
- **WSL IP `100.125.148.23`** 硬编码在各配置文件中。可通过环境变量覆盖：`DB_HOST`、`REDIS_HOST`、`MINIO_ENDPOINT`、`NACOS_HOST`。
- **Nuxt 缓存问题：** 出现 `"Failed to resolve import \"#app-manifest\""` 时，终止旧 Node 进程，删除 `.nuxt/.output` 后重启。
- **Docker 数据目录**（`infrastructure/docker/data/{postgres,redis,minio}/`）在 gitignore 中；Nacos 日志（`standalone-logs/`）**未**在 gitignore 中，注意不要提交。
- **CI** 使用 Node 22、JDK 21，仅在 `main` 分支的 push/PR 触发。前端 Docker 镜像从 `ZY_View_Migu/Admin` 和 `ZY_View_Migu/Web` 构建（非 `frontend/` 目录）。
- **生产必须覆盖** `JWT_SECRET` 和 `DEFAULT_ADMIN_PASSWORD`。
