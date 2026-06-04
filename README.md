# Project ZHANGYUAN

AI 模型 API 服务平台 —— 聚合主流大模型能力，提供统一 API 接入、管理后台和开发者工具。

## 架构概览

```
┌─────────────────────────────────────────────────────┐
│                   前台站点 (Nuxt 3)                   │
│   静态页面 · CMS 动态渲染 · 交互式首页 · 开发者文档     │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP / REST
┌──────────────────────▼──────────────────────────────┐
│                  后端 API (Spring Boot)               │
│   认证授权 · CMS 引擎 · 商品套餐 · 订单支付 · 资产系统  │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────┬───────────┼───────────┬──────────┐
│PostgreSQL│   Redis   │   MinIO   │   Docker │
│ (主存储)  │  (缓存)   │ (文件存储) │ (编排)    │
└──────────┴───────────┴───────────┴──────────┘
```

## 模块目录

| 目录 | 职责 | 状态 |
| --- | --- | --- |
| `frontend/web` | 前台站点 — Nuxt 3 静态站 + CMS 动态渲染 | 开发中 |
| `frontend/admin` | 管理后台 — Vue 3 + Naive UI + Pinia | 开发中 |
| `frontend/shared` | 共享 UI 类型与 API 客户端 | 规划中 |
| `backend/api` | 后端 API — Spring Boot + JPA + Flyway | 活跃开发 |
| `backend/gateway` | API 网关 | 占位 |
| `domain` | 核心领域模型 | 占位（暂在 API 内部） |
| `database/migrations` | Flyway 数据库迁移 | 活跃开发 |
| `database/seeds` | 种子数据 | 活跃开发 |
| `database/backup` | 备份脚本 | 规划中 |
| `security/auth` | 认证模块 | 占位（暂在 API 内部） |
| `security/rbac` | 权限模块 | 占位（暂在 API 内部） |
| `security/crypto` | 加密模块 | 占位（暂在 API 内部） |
| `infrastructure/docker` | Docker Compose 配置与本地数据 | 运行中 |
| `infrastructure/kubernetes` | K8s 部署配置 | 规划中 |
| `infrastructure/helm` | Helm Charts | 规划中 |
| `infrastructure/ci` | CI/CD 配置 | 规划中 |
| `docs/` | 架构、数据库、API、部署文档 | 持续更新 |
| `scripts/` | 开发脚本（种子数据等） | 持续更新 |
| `.github/workflows` | GitHub Actions CI | 运行中 |

## 技术栈

### 后端

- **Java 21** + **Spring Boot 3.3.5** + **Gradle**
- **PostgreSQL** 主存储，**Redis** 缓存，**MinIO** 文件存储
- **JPA**（`ddl-auto: validate`）+ **Flyway** schema 管理
- Spring Modulith 模块边界 + ArchUnit 测试验证
- 六边形架构（Domain → Application → Adapter）

### 后端模块

| 模块 | 职责 |
| --- | --- |
| `auth` | 用户认证、角色权限管理（RBAC）、默认管理员初始化 |
| `cms` | 页面管理、多语言草稿、版本快照、发布渲染 |
| `product` | 套餐分组、套餐方案、定价与功能定义 |
| `order` | 订单管理、订单生命周期 |
| `payment` | 支付接入、支付回调、支付记录 |
| `asset` | 文件上传（MinIO）、素材管理 |
| `system` | 系统设置、健康检查、运营管理 |

### 前端

#### 管理后台（Admin）

- **Vue 3.5** + **TypeScript** + **Vite 6**
- **Naive UI** 组件库 + **Pinia** 状态管理
- **Vue Router** 路由，按业务分包（cms、orders、products 等）

#### 前台站点（Web）

- **Nuxt 3.15** + **TypeScript**
- **CMS 动态页面**：`pages/[...slug].vue` 根据区块类型映射组件渲染
- **独立首页**：`pages/index.vue` 含粒子动画、渐变背景等交互效果
- 动画组件库：`AnimReveal`、`AnimParticles`、`AnimWaves`、`AnimTrail` 等

## 快速开始

### 前置条件

- Java 21（API）、Node.js 22（前端）
- Docker（中间件）

### 1. 启动中间件

```bash
docker compose -f infrastructure/docker/docker-compose.yml up -d
```

启动 PostgreSQL（5432）、Redis（6379）、MinIO（9000/9001）。

### 2. 启动后端 API

```bash
cd backend/api
./gradlew bootRun
```

API 默认运行在 `http://localhost:8080`。

### 3. 启动管理后台

```bash
cd frontend/admin
npm install
npm run dev
```

Admin 默认运行在 `http://localhost:5173`（Vite 代理 API 到 8080）。

### 4. 启动前台站点

```bash
cd frontend/web
npm install
npm run dev
```

Web 默认运行在 `http://localhost:3000`。

### 默认管理员

| 用户名 | 密码 |
| --- | --- |
| `admin` | `admin123` |

生产环境必须通过环境变量覆盖 `JWT_SECRET`、`DEFAULT_ADMIN_USERNAME`、`DEFAULT_ADMIN_PASSWORD`。

## 常用命令

### 后端

```bash
cd backend/api

./gradlew test                              # 全部测试
./gradlew test --tests "com.zhangyuan.modules.auth.*"  # 模块测试
./gradlew test --tests "com.zhangyuan.architecture.*"  # 模块边界验证
./gradlew bootRun                           # 启动开发服务器
./gradlew build -x test                     # 构建（跳过测试）
```

### 管理后台

```bash
cd frontend/admin

npm install        # 安装依赖
npm run dev        # 开发模式（0.0.0.0:5173）
npm run build      # 构建（vue-tsc 类型检查 + Vite 打包）
```

### 前台站点

```bash
cd frontend/web

npm install        # 安装依赖
npm run dev        # 开发模式（0.0.0.0:3000）
npm run build      # 构建（Nuxt 静态生成）
```

### 全栈 Docker

```bash
docker compose -f infrastructure/docker/docker-compose.yml \
               -f infrastructure/docker/docker-compose.app.yml up -d
```

### 测试数据

```powershell
scripts/seed-test-data.ps1
```

## API 规范

### 路由约定

| 前缀 | 用途 | 认证 |
| --- | --- | --- |
| `/admin/**` | 后台管理接口 | 需登录 |
| `/api/**` | 公开接口 | 放行 |
| `/api/ddd/**` | DDD 六边形架构验证接口 | 视情况 |
| `/actuator/**` | Spring Boot Actuator | 放行 |

### 响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { }
}
```

分页响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [],
    "page": 1,
    "pageSize": 20,
    "total": 100
  }
}
```

## CMS 子系统

CMS 是项目核心子系统，实现页面 → 区块 → 多语言草稿 → 版本快照 → 发布的全流程。

### 渲染流程

```
用户请求 /{path}  →  Nuxt [...slug].vue
                    →  GET /api/cms/pages/render?path=/{path}&locale=zh-CN
                    →  服务端查找已发布快照
                    →  返回区块数据结构
                    →  Nuxt 按 block.type 映射到 components/blocks/* 渲染
```

### 业务边界

- CMS 仅负责页面、区块、草稿、发布快照和前台渲染
- 套餐、订单、支付的核心规则属于 `product`/`order`/`payment` 模块
- 发布后的前台读取 snapshot，草稿不影响线上

## 开发环境说明

- Docker 运行在 WSL 时，API 在 Windows 侧需用 `application-wsl.yml` profile：
  ```bash
  ./gradlew bootRun --args='--spring.profiles.active=wsl'
  ```
  或设置环境变量 `DB_HOST`、`REDIS_HOST`、`MINIO_ENDPOINT` 指向 WSL IP。
- Docker Compose 数据目录在 `infrastructure/docker/data/`，请勿提交运行数据。
- Nuxt 出现 `Failed to resolve import "#app-manifest"` 时，删除 `.nuxt/` 和 `.output/` 后重启。

## CI/CD

GitHub Actions（`.github/workflows/ci.yml`）：

1. **test-api** — JDK 21 + `gradle test`
2. **lint-admin** — Node 22 + `npx vue-tsc -b --noEmit`
3. **lint-web** — Node 22 + `npx nuxt typecheck`

全部通过后构建产出，main 分支自动构建 Docker 镜像并推送。

## 文档

详细文档见 [docs/](./docs/README.md) 目录：

| 文档 | 内容 |
| --- | --- |
| [架构方案](./docs/cms-architecture.md) | CMS 整体架构设计 |
| [工程结构](./docs/project-structure.md) | 目录组织与模块边界 |
| [数据库设计](./docs/cms-database.md) | CMS 数据模型 |
| [接口设计](./docs/cms-api.md) | API 接口定义 |
| [实施路线](./docs/implementation-roadmap.md) | 分阶段实施计划 |
| [部署方案](./docs/deployment.md) | 部署与 CI/CD |
