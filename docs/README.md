# Project ZHANGYUAN Documentation

本文档目录用于沉淀 Project ZHANGYUAN 的 CMS 子系统设计、数据库模型、接口约定、实施路线与部署方案。

CMS 的定位不是独立建站工具，而是 Project ZHANGYUAN 后续业务系统中的一个子系统。它负责管理前台页面、多语言内容、页面区块、发布版本和内容预览，并与套餐、订单、支付等业务模块联动。

## 文档清单

- [CMS 架构方案](./cms-architecture.md)
- [CMS 数据库设计](./cms-database.md)
- [CMS 接口设计](./cms-api.md)
- [工程结构落地方案](./project-structure.md)
- [实施路线](./implementation-roadmap.md)
- [部署与 CI/CD](./deployment.md)

## 核心结论

- 前台站点使用 Nuxt 3，满足 SEO、动态路由、多语言和静态化能力。
- 管理后台使用 Vue 3、TypeScript、Vite 和 Naive UI。
- 后端使用 Spring Boot 3 和 Java 21。
- 数据库使用 PostgreSQL，页面内容使用 JSONB。
- 缓存使用 Redis。
- 文件存储使用 MinIO，后续可切换到 S3、OSS、COS。
- 第一阶段采用模块化单体，后续按业务边界拆分为分布式服务。
- 工程目录沿用现有 `ZY_<Function>_<MythName>` 命名体系，不额外引入 `apps/`、`services/` 平行结构。
