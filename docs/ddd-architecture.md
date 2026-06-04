# DDD 六边形架构规范

## 架构总览

每个微服务遵循 DDD 六边形架构（端口与适配器模式）：

```
com.zhangyuan.{service}
├── domain/
│   ├── model/              # 领域模型（纯 POJO，无框架注解）
│   │   ├── User.java       # 聚合根、实体、值对象
│   │   └── ...
│   ├── repository/         # 仓储接口（端口定义）
│   │   ├── UserRepository.java
│   │   └── ...
│   └── service/            # 领域服务（纯业务规则）
│       └── DomainService.java
├── application/
│   └── service/            # 应用服务（编排用例，不包含业务规则）
│       └── AppService.java
├── adapter/
│   ├── in/
│   │   └── rest/           # REST 控制器（入站适配器）
│   │       ├── UserController.java
│   │       └── ...
│   └── out/
│       └── persistence/    # JPA 实体 + 仓储实现（出站适配器）
│           ├── JpaUserRepositoryAdapter.java
│           ├── UserEntity.java
│           └── UserJpaRepository.java
├── dto/                    # 数据传输对象
│   ├── UserRequest.java
│   └── UserResponse.java
└── common/                 # 通用工具
    ├── ApiResponse.java
    └── SaTokenConfig.java
```

## 严格规则（必须遵守）

### 规则 1: 依赖方向
依赖必须从外向内，外层依赖内层：
```
adapter/in → application → domain
adapter/out → domain
```
**严禁**：adapter/in 直接依赖 adapter/out 或 JPA Repository

### 规则 2: 领域模型纯洁
`domain/model/` 中的类**不得**包含：
- JPA 注解 (@Entity, @Table, @Column, @Id)
- Spring 注解 (@Component, @Service)
- 任何框架特定代码

### 规则 3: 应用服务
`application/service/` 中的类只能依赖：
- ✅ `domain/model/`（领域模型）
- ✅ `domain/repository/`（仓储接口）
- ✅ `domain/service/`（领域服务）
- ✅ `dto/`（数据传输对象）
- ❌ **不能直接依赖** `adapter/out/` 中的 JPA 实体
- ❌ **不能直接注入** Spring Data JPA 接口

### 规则 4: 仓储实现
`adapter/out/persistence/` 中的类负责：
- 实现 `domain/repository/` 中定义的接口
- 在 JPA 实体和领域模型之间做转换
- Spring Data JPA 接口只应该在这里被注入

### 规则 5: 控制器
`adapter/in/rest/` 中的类：
- 只能依赖 `application/service/` 和 `dto/`
- 不能返回 JPA 实体
- 不能直接调用 repository

## 包命名规范

| 层 | 包路径 | 示例 |
|----|--------|------|
| 领域模型 | `domain.model` | `User.java` |
| 仓储接口 | `domain.repository` | `UserRepository.java` |
| 领域服务 | `domain.service` | `UserDomainService.java` |
| 应用服务 | `application.service` | `UserApplicationService.java` |
| REST 控制器 | `adapter.in.rest` | `UserController.java` |
| JPA 适配器 | `adapter.out.persistence` | `JpaUserRepositoryAdapter.java` |
| JPA 实体 | `adapter.out.persistence` | `UserEntity.java` |
| Spring Data JPA | `adapter.out.persistence` | `UserJpaRepository.java` |
| DTO | `dto` | `UserRequest.java` |

## 验证清单
- [ ] domain/model 没有 JPA 注解
- [ ] 控制器只依赖应用服务和 DTO
- [ ] 应用服务只依赖领域端口和领域服务
- [ ] 仓储适配器实现领域端口接口
- [ ] `./gradlew compileJava` 通过
- [ ] `./gradlew test` 通过
