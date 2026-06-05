# Gateway 服务设计文档

## 概述

新增 Spring Cloud Gateway 服务作为唯一外部入口，统一路由所有 API 请求，取代现有 Nginx 反向代理。

## 架构

```ascii
外部请求
  │
  ▼
┌──────────────────────────┐
│  Gateway (:8080)          │  ← 唯一外部入口
│  Spring Cloud Gateway     │
│  注册到 Nacos             │
└──────────┬───────────────┘
           │
     ┌─────┼─────────┐
     │     │         │
     ▼     ▼         ▼
┌────────┐ ┌──────────┐ ┌──────────┐
│ API    │ │ System   │ │ Web      │
│ :8088  │ │ :8081    │ │ :3000    │
│ (原    │ │          │ │ (Nuxt)   │
│ :8080) │ │          │ │          │
└────────┘ └──────────┘ └──────────┘

Admin SPA (:81) 独立容器提供静态文件
```

## 服务端口规划

| 服务 | 端口 | 说明 |
|---|---|---|
| **Gateway** | **8080** | 新增，取代 Nginx 80 |
| **zhangyuan-api** | **8088** | 从 8080 迁移，变为内部服务 |
| **system-service** | **8081** | 不变 |
| **Web (Nuxt)** | **3000** | 不变，Nuxt SSR |
| **Admin SPA** | **81** | 不变，独立容器 serve 静态文件 |

## 路由规则

### Gateway 路由表

| 路由 ID | 路径匹配 | 目标服务 | 说明 |
|---|---|---|---|
| `system-admin` | `/admin/system/**` | `lb://system-service` | 系统管理后台 API |
| `system-api` | `/api/system/**` | `lb://system-service` | 系统公开 API |
| `system-ddd` | `/api/ddd/settings/**` | `lb://system-service` | CMS 种子数据 |
| `api-admin` | `/admin/**` | `lb://zhangyuan-api` | 管理后台 API |
| `api-public` | `/api/**` | `lb://zhangyuan-api` | 公开 API |
| `api-actuator` | `/actuator/**` | `lb://zhangyuan-api` | 健康检查 |
| `web` | `/` | `http://web:3000` | 前台站点（直接 URI，Nuxt 不注册 Nacos）|

路由优先级：**system 路由 > api 路由 > web 路由**（精确匹配优先）。

### 与现有 Nginx 路由的对应关系

现有 Nginx 配置中 `/admin/system/`、`/api/system/`、`/api/ddd/settings` 的路由由 Gateway 通过 Nacos 服务发现替代，不再依赖 hostname 直连。

## 组件详细设计

### 1. Gateway 模块 (`backend/gateway/`)

新建 Spring Boot 项目：

- **build.gradle**: Spring Cloud Gateway + Nacos Discovery + LoadBalancer
- **GatewayApplication.java**: `@SpringBootApplication` + `@EnableDiscoveryClient`
- **application.yml**: server.port=8080, Nacos 注册, 路由定义
- **无业务逻辑**: 纯路由转发，不做任何业务处理

### 2. zhangyuan-api (`backend/api/`)

仅修改 `application.yml` 中：
- `server.port: ${SERVER_PORT:8088}`

### 3. Docker Compose (`infrastructure/docker/docker-compose.app.yml`)

- 新增 **gateway** 服务容器
- 移除 **nginx** 服务容器
- api 服务容器端口映射改为 `8088:8088`
- gateway 依赖 api、system-service、web

### 4. 前端 Vite 代理 (`frontend/admin/vite.config.ts`)

Dev 模式下：
- 代理目标从 `http://localhost:8080` → `http://localhost:8088`

## Nacos 集成

Gateway 注册到 Nacos（`spring.cloud.nacos.discovery`），路由通过 `lb://` 前缀使用客户端负载均衡解析服务实例。

## 安全

Gateway 不做认证/授权，所有请求透传。JWT/Sa-Token 校验仍在 `zhangyuan-api` 进行。
Gateway 可以通过 `spring.cloud.gateway.routes[].metadata.response-timeout` 配置超时。

## 需要创建/修改的文件清单

| 文件 | 操作 |
|---|---|
| `backend/gateway/build.gradle` | 新建 |
| `backend/gateway/settings.gradle` | 新建 |
| `backend/gateway/src/main/java/com/zhangyuan/gateway/GatewayApplication.java` | 新建 |
| `backend/gateway/src/main/resources/application.yml` | 新建 |
| `backend/api/src/main/resources/application.yml` | 修改 `server.port` |
| `infrastructure/docker/docker-compose.app.yml` | 新增 gateway，移除 nginx，改 api 端口 |
| `frontend/admin/vite.config.ts` | 修改代理目标 |

## 边界与限制

- Gateway 不 serve 静态文件，admin SPA 仍由独立容器提供
- Gateway 不做请求体修改或鉴权
- 开发模式下（`npm run dev`），前端直接走 Vite 代理到 zhangyuan-api:8088，不经过 Gateway
- 生产模式下所有请求通过 Gateway:8080
