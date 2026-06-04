# AGENTS.md — Project ZHANGYUAN

## 项目边界
- 这是多模块单体仓库，不是根目录 Node/Gradle 工作区；根目录没有 `package.json`，不要在根目录跑 `npm install`。
- 目录按用途命名（`frontend`、`backend`、`infrastructure` 等）；新功能优先落在现有顶层目录，不要随手新增顶层模块。
- 主要入口：前台 `frontend/web`，后台 `frontend/admin`，后端 API `backend/api`，基础设施 `infrastructure/docker`。
- `security`、`backend/gateway`、`domain` 当前多为占位/规划；认证、授权、领域逻辑实际仍在 API 单体内。

## 常用命令
- 中间件已在 WSL 常驻运行（PostgreSQL `5432`、Redis `6379`、MinIO `9000/9001`），一般无需手动启动。如需重启：`docker compose -f infrastructure/docker/docker-compose.yml up -d`。
- 全栈 Docker：`docker compose -f infrastructure/docker/docker-compose.yml -f infrastructure/docker/docker-compose.app.yml up -d`；第二个 compose 依赖第一个创建的外部网络。
- API：在 `backend/api` 运行 `./gradlew bootRun`、`./gradlew test`、单测 `./gradlew test --tests "com.zhangyuan...Test"`。
- Web：在 `frontend/web` 运行 `npm install`、`npm run dev`、`npm run build`；dev 固定 `0.0.0.0:3000`。
- Admin：在 `frontend/admin` 运行 `npm install`、`npm run dev`、`npm run build`；dev 固定 `0.0.0.0:5173`。
- CI 使用 Node 22、JDK 21；前端 CI 先 `npm ci`，Admin 跑 `npx vue-tsc -b --noEmit`，Web 跑 `npx nuxt typecheck`。

## 后端约定
- Spring Boot 3.3.5 + Java 21 + Gradle；数据库 schema 只由 Flyway 管理，JPA `ddl-auto: validate`，迁移放 `src/main/resources/db/migration/V*.sql`。
- 本地默认连接：PostgreSQL `zhangyuan/zhangyuan@localhost:5432/zhangyuan`，Redis `localhost:6379`，MinIO `http://localhost:9000`。
- 默认管理员来自配置：`admin / admin123`；生产必须覆盖 `JWT_SECRET` 和 `DEFAULT_ADMIN_PASSWORD`。
- 统一响应格式为 `{ code, message, data }`；分页数据在 `data` 内使用 `items/page/pageSize/total`。
- `/admin/**` 是后台接口，`/api/**` 是公开接口，`/api/ddd/**` 是 DDD 骨架验证接口；旧 CRUD 控制器和 DDD 适配器当前并行存在。
- DDD 模块在 `com.zhangyuan.modules.{auth,cms,product,order,payment,asset,system}`；`domain` 不依赖 Spring/JPA，`application` 只编排，`adapter` 做 REST/JPA 映射。
- 模块边界验证：`gradle test --tests "com.zhangyuan.architecture.*"`。

## 前端约定
- Admin Vite 代理 `/admin`、`/api`、`/actuator` 到 `http://localhost:8080`；调用后台接口时优先走这些相对路径。
- Web 后端地址从 `NUXT_PUBLIC_API_BASE` 读取，默认 `http://localhost:8080`。
- Web 的 `pages/[...slug].vue` 是 CMS 动态页：调用 `GET /api/cms/pages/render?path=...&locale=...`，按 `block.type` 映射到 `components/blocks/*`，未知区块走 `UnknownBlock`。
- `pages/index.vue` 目前是独立手写首页，不走 CMS 动态渲染；改首页 UI 时不要误以为 CMS 会覆盖它。

## CMS 与业务边界
- CMS 负责页面、区块、草稿、发布快照和前台渲染；发布后的前台读取 snapshot，避免草稿影响线上。
- CMS 的 pricing 区块可注入真实套餐组快照，但套餐、订单、支付的核心规则属于 product/order/payment 模块，不要塞进 CMS。
- 站点设置接口提供站点名、描述、备案号、footer 等，前台通过 `useSiteSettings` 使用。

## 环境坑点
- Docker 在 WSL、API 在 Windows 侧运行时，用 `application-wsl.yml` profile 或设置 `DB_HOST`、`REDIS_HOST`、`MINIO_ENDPOINT` 指向 WSL IP；文件内当前默认 WSL IP 是 `100.125.148.23`，实际环境可能变化。
- Nuxt 若出现 `Failed to resolve import "#app-manifest"`，通常是旧 dev 进程或 `.nuxt/.output` 缓存问题；停止旧 Node 进程后删除缓存再重启。
- Docker Compose 的数据目录在 `infrastructure/docker/data/*`，不要把运行数据当源码改动提交。
