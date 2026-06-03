# Project_ZHANGYUAN

Project_ZHANGYUAN 采用《山海经》意象作为工程模块命名体系，在保留功能语义的同时，为目录结构加入统一的世界观与辨识度。

## 目录命名方案

本项目采用“功能后缀型”命名：`ZY_<Function>_<MythName>`。

这种方式保留模块职责，同时使用《山海经》词汇作为文化前缀/后缀，让新加入的开发者可以快速理解目录用途。

| 目录 | 典故 | 模块职责 |
| --- | --- | --- |
| `ZY_Foundation_Yingzhi` | 英鞮，产金玉之山 | 基础架构、云端配置、DevOps、Kubernetes、Terraform |
| `ZY_View_Migu` | 迷毂，佩之不迷 | 前端页面、UI、交互、导航界面 |
| `ZY_Nexus_Congcong` | 从从，敏捷之兽 | 网络层、接口、路由、后端服务、API 网关 |
| `ZY_Archive_Shirou` | 视肉，无尽再生 | 存储、数据库、迁移、备份、数据中心 |
| `ZY_Guard_Bo` | 驳，食虎豹之兽 | 安全、防火墙、认证、授权、加密与防护 |
| `ZY_Source_Origin` | Origin，源点 | 核心源码、领域逻辑、算法与公共能力 |

## 命名考据

- 英鞮：山中多金玉，象征贵重的底层资源与基础设施。
- 迷毂：其花可照亮路径，适合作为用户可见界面与导航体验的象征。
- 从从：六足之兽，速度极快，适合承载数据流转、接口与低延迟传输相关代码。
- 视肉：吃掉一块会自动长出一块，隐喻数据冗余、备份与持续增长。
- 驳：状如白马，以虎豹为食，象征低调但强力的安全防护能力。
- Origin：项目核心源点，用于承载不依赖具体外层形态的核心源码与算法。

## 模块说明规范

每个模块目录内应保留 `README.md`，说明该目录的职责边界、典型内容与不应放入的内容。

推荐在重要源码文件头部保留简短模块备注：

```text
Origin: ZHANGYUAN - Module: MIGU (Front-end Interface)
```

## 项目文档

CMS 子系统的架构、数据库、接口、实施路线与部署方案已整理到 `docs/` 目录：

- [文档索引](./docs/README.md)
- [CMS 架构方案](./docs/cms-architecture.md)
- [工程结构落地方案](./docs/project-structure.md)
- [CMS 数据库设计](./docs/cms-database.md)
- [CMS 接口设计](./docs/cms-api.md)
- [实施路线](./docs/implementation-roadmap.md)
- [部署与 CI/CD](./docs/deployment.md)
