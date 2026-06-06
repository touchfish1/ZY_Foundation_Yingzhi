-- order_main 和 payment_transaction 表所有权已迁移至 order-service 和 payment-service
-- api 模块通过 Feign 调用而非直接操作数据库
-- 保留已有表结构向后兼容，新增字段仅由各自治服务管理

alter table order_main add column if not exists fulfilled_at timestamptz;
