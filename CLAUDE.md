# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

多模块单体仓库，包含 Spring Boot API + Vue 3 管理后台 + Nuxt 3 前台站点，以 CMS 为核心子系统（页面管理、区块编辑、发布快照）。

## 顶层模块

| 目录 | 职责 |
| --- | --- |
| `frontend/admin` | Vue 3 管理后台（Vite + Naive UI + Pinia） |
| `frontend/web` | Nuxt 3 前台站点 |
| `frontend/shared` | 共享 UI 类型 |
| `backend/api` | Spring Boot 后端 API（主力代码所在） |
| `backend/gateway` | 网关（当前为占位） |
| `domain` | 核心领域逻辑（当前为占位；领域模型暂在 API 内部） |
| `database/migrations` | Flyway 数据库迁移 |
| `database/seeds` | 种子数据 |
| `database/backup` | 备份脚本 |
| `security/{auth,rbac,crypto}` | 安全模块（当前为占位；JWT/RBAC 暂在 API 内部） |
| `infrastructure/docker` | Docker Compose 配置及数据目录 |
| `infrastructure/{kubernetes,helm,ci}` | K8s/Helm/CI 配置 |
| `docs/` | 架构、数据库、API、部署文档 |

## 常用命令

### 基础设施（中间件）

中间件（PostgreSQL、Redis、MinIO）已通过 Docker 在 WSL 中常驻运行，无需每次启动。
如需重启或从零启动：

```bash
# 启动 PostgreSQL + Redis + MinIO
docker compose -f infrastructure/docker/docker-compose.yml up -d

# 全栈启动（含 API / Admin / Web 容器）
docker compose -f infrastructure/docker/docker-compose.yml -f infrastructure/docker/docker-compose.app.yml up -d
```

### 后端（Spring Boot）
```bash
# 在 backend/api 目录下执行
gradle bootRun                    # 启动（默认 8080）
gradle test                       # 全部测试
gradle test --tests "com.zhangyuan.modules.auth.*"  # 指定模块测试
gradle test --tests "com.zhangyuan.architecture.*"  # 模块边界验证(ArchUnit)
gradle build -x test              # 构建（跳过测试）
```
使用项目自带的 `./gradlew`，无需系统安装 Gradle。

### 管理后台（Admin）
```bash
# 在 frontend/admin 目录下执行
npm install        # 安装依赖
npm run dev        # 开发模式（0.0.0.0:5173）
npm run build      # 构建（先 vue-tsc 类型检查）
```

### 前台站点（Web）
```bash
# 在 frontend/web 目录下执行
npm install        # 安装依赖
npm run dev        # 开发模式（0.0.0.0:3000）
npm run build      # 构建
```

### 其他
```bash
# 种子测试数据（PowerShell）
scripts/seed-test-data.ps1
```

## 后端架构约定

### 技术栈
- Spring Boot 3.3.5 + Java 21 + Gradle
- JPA (ddl-auto: validate) + Flyway 管理 schema
- Spring Security + JWT
- Spring Modulith 模块边界 + ArchUnit 测试
- Redis、MinIO

### DDD 六边形架构（每模块结构）
```
com.zhangyuan.modules.{auth,cms,product,order,payment,asset,system}
  ├── domain/          # 领域模型 + 仓储接口，不依赖 Spring/JPA
  ├── domain/model/    # 聚合根、实体、值对象
  ├── domain/repository/ # 仓储接口
  ├── domain/service/  # 领域服务
  ├── application/     # 应用服务（编排用），不包含业务规则
  ├── adapter/in/      # REST 控制器
  ├── adapter/out/     # JPA 仓储实现
  ├── dto/             # 数据传输对象
  └── repository/      # JPA Repository 接口
```

### API 规范
- 统一响应：`{ code, message, data }`
- 分页响应在 `data` 内：`{ items, page, pageSize, total }`
- `/admin/**` 后台接口（需认证），`/api/**` 公开接口，`/api/ddd/**` DDD 骨架验证
- 旧 CRUD 控制器与 DDD 适配器当前并行存在

### 配置与安全
- 默认管理员：`admin / admin123`（生产必须覆盖 `JWT_SECRET` 和 `DEFAULT_ADMIN_PASSWORD`）
- 本地默认连接：PostgreSQL `zhangyuan/zhangyuan@localhost:5432/zhangyuan`
- WSL 开发用 `application-wsl.yml` profile 或设置 `DB_HOST`/`REDIS_HOST`/`MINIO_ENDPOINT`

## 前端架构约定

### Admin（管理后台）
- Vue 3 + TypeScript + Vite + Naive UI + Pinia + Vue Router
- Vite 代理 `/admin`、`/api`、`/actuator` 到 `http://localhost:8080`
- `src/pages/` 下按业务组织：`login`、`dashboard`、`cms`、`assets`、`products`、`orders`、`system`
- `src/api/` 按模块封装 HTTP 调用
- CI 类型检查：`npx vue-tsc -b --noEmit`

### Web（前台站点）
- Nuxt 3 + TypeScript
- `NUXT_PUBLIC_API_BASE` 指定后端地址（默认 `http://localhost:8080`）
- `pages/[...slug].vue` 是 CMS 动态页面渲染入口：按 `block.type` 映射到 `components/blocks/*`，未知区块走 `UnknownBlock`
- `pages/index.vue` 是独立手写首页，**不走** CMS 动态渲染
- `composables/` 包含 `useCmsPage`、`useSiteSettings` 等
- 动画组件：`AnimCursorGlow`、`AnimMagnetic`、`AnimReveal`、`AnimRipple`、`AnimTilt`、`AnimTrail`、`AnimWaves`
- CI 类型检查：`npx nuxt typecheck`

## CMS 业务边界
- CMS 仅负责页面、区块、草稿、发布快照和前台渲染
- 套餐、订单、支付的核心规则属于 `product`/`order`/`payment` 模块，**不要**塞入 CMS
- 发布后的前台读取 snapshot，草稿不会影响线上

## CI/CD
- GitHub Actions（`.github/workflows/ci.yml`）
- 三个并行 job：`test-api`（JDK 21 + `gradle test`）、`lint-admin`、`lint-web`
- 全部通过后 `build` job 构建产出，`main` 分支自动构建 Docker 镜像并推送

## 环境注意事项
- Docker 在 WSL、API 在 Windows 时，用 `application-wsl.yml` 或环境变量指向 WSL IP
- Docker Compose 数据目录在 `infrastructure/docker/data/`，**不要**提交运行数据
- Nuxt 出现 `Failed to resolve import "#app-manifest"` 时，停止旧进程后删除 `.nuxt/` 和 `.output/` 再重启
