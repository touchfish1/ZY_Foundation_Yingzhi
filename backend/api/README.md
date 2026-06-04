# backend/api — 后端 API

Spring Boot 3.3.5 + Java 21 + Gradle 后端 API 服务。

## 开发

```bash
./gradlew bootRun        # 启动（默认 8080）
./gradlew test           # 全部测试
./gradlew build -x test  # 构建
```

## 模块

| 模块 | 职责 |
| --- | --- |
| auth | 认证、角色、权限 |
| cms | 页面管理、区块、发布 |
| product | 套餐与定价 |
| order | 订单管理 |
| payment | 支付接入 |
| asset | 文件存储 |
| system | 设置、健康检查 |
