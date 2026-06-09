# AGENTS.md — ZY_Foundation_Yingzhi

## 项目简介
AI 模型 API 服务平台。Spring Boot 3.3.5 + Nuxt 3 + Vue 3。六边形架构。

## 远程中间件（默认连接）
所有后端服务默认连接远程服务器 `100.125.148.23`，无需本地 Docker 启动中间件。

| 服务 | 默认地址 |
|------|---------|
| PostgreSQL | `100.125.148.23:5432` (zhangyuan/zhangyuan) |
| Redis | `100.125.148.23:6379` |
| Nacos | `100.125.148.23:8848` (nacos/chengccn) |
| MinIO | `http://100.125.148.23:9000` (zhangyuan/zhangyuan-minio-secret) |

各服务 `application.yml` 中的 `${VAR:default}` 默认值已指向该地址，直接启动即可连接。

## 启动命令

### 后端
```bash
cd backend
./gradlew bootRun                                   # api (默认 8088)
./gradlew :gateway:bootRun                           # gateway (8080)
./gradlew :auth-service:bootRun                      # auth-service (8082)
./gradlew :user-service:bootRun                      # user-service (8085)
./gradlew :order-service:bootRun                     # order-service (8083)
./gradlew :payment-service:bootRun                   # payment-service (8084)
./gradlew :system-service:bootRun                    # system-service (8081)
./gradlew :ai-service:bootRun                        # ai-service (8086)
```

### 前端（不依赖中间件）
```bash
cd frontend/admin && npm run dev     # Admin (5173)
cd frontend/web && npm run dev       # Web (3000)
```

## 常用操作
- 后端全量测试: `cd backend && ./gradlew test`
- Admin 类型检查: `cd frontend/admin && npx vue-tsc -b --noEmit`
- Web 类型检查: `cd frontend/web && npx nuxt typecheck`
- 启动所有后端微服务: 每个服务开一个 tmux session，`./gradlew :模块名:bootRun`

## 项目结构要点
- `backend/api` — 单体 API（主要模块：auth, cms, product, order, payment, asset, system）
- `backend/order-service` — 订单微服务（微服务化，有独立 Order 领域模型）
- `backend/payment-service` — 支付微服务
- `frontend/admin` — Vue 3 + Naive UI 管理后台
- `frontend/web` — Nuxt 3 前台站点（含 CMS 动态渲染）

## 订单 userId 传播现状
- `backend/api` 模块: ✅ 已完整支持 userId（OrderResponse 含 userId）
- `backend/order-service` 模块: ✅ 已完整支持 userId（Phase B 已修复）

## 关键规则
- 不要启动本地 Docker 中间件，一律用远程 `100.125.148.23`
- 启动后端时不需要等中间件启动，远程已经运行
- RichTextBlock.vue 中通过 `props.props?.content` 访问区块内容（非 `props.content`）
- 一键本地全栈启动（含中间件）: `./scripts/start-dev.sh`（需要 tmux + Docker）
- AI 代理服务（8086）：`POST /v1/chat/completions`，OpenAI 兼容格式，支持 gpt-4o、claude-3 等
