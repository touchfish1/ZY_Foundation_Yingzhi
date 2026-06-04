# 下单/支付/订阅业务实现计划

基于 sub2api 的业务逻辑分析，在我们的项目中实现完整的订单→支付→履约→订阅流程。

## 当前已具备

| 组件 | 状态 |
|------|------|
| Product/Plan/Price 实体 | ✅ api 模块已有 |
| OrderService 骨架 | ✅ 独立微服务 |
| PaymentService 骨架 | ✅ 独立微服务（仅 mock） |
| UserService (system-service) | ✅ 注册/登录 |
| 网关路由 | ✅ |

## 需要新增/完善的

### Phase 1: 订阅模型 + 订单履约
- 用户订阅实体 + 表 (order-service)
- 订阅分配/续期/检查逻辑
- 支付完成后履约（FulfillmentService）

### Phase 2: 完善下单支付流程
- 完善 OrderApplicationService.createOrder() — 套餐价格计算
- 完善 PaymentApplicationService.checkout() — 支持下单后支付
- 前端定价页对接真实数据

### Phase 3: 控制台完整对接
- 订单展示对接真实 API
- 订阅状态展示
- 使用量展示

## 技术要点
- 订阅幂等性：通过审计日志/订单状态防止重复履约
- 订单快照：创建订单时 snapshot 套餐信息
- 服务商抽象（后续）：参考 sub2api 的 Provider 接口模式
