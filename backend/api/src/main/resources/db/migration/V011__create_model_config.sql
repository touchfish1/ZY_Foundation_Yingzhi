-- Model/provider routes: maps model name to provider
create table if not exists ai_model_route (
    id            bigserial primary key,
    model_name    varchar(128) not null unique,
    provider      varchar(64) not null,
    provider_model_name varchar(255) not null,
    model_type    varchar(32) not null default 'chat',
    status        varchar(16) not null default 'active',
    created_at    timestamptz not null default now(),
    updated_at    timestamptz not null default now()
);

-- Per-model pricing for billing/usage cost calculation
create table if not exists ai_model_pricing (
    id              bigserial primary key,
    model_name      varchar(128) not null unique references ai_model_route(model_name),
    input_price     decimal(12,8) not null default 0,
    output_price    decimal(12,8) not null default 0,
    currency        varchar(8) not null default 'CNY',
    effective_from  timestamptz not null default now(),
    effective_to    timestamptz,
    created_at      timestamptz not null default now()
);

-- Plan-to-model access mapping
create table if not exists ai_plan_model_access (
    id              bigserial primary key,
    plan_code       varchar(64) not null,
    model_name      varchar(128) not null,
    max_concurrency int not null default 5,
    max_rpm         int not null default 0,
    unique(plan_code, model_name)
);

-- Seed data: common models
insert into ai_model_route (model_name, provider, provider_model_name, model_type) values
    ('gpt-4o', 'openai', 'gpt-4o', 'chat'),
    ('gpt-4o-mini', 'openai', 'gpt-4o-mini', 'chat'),
    ('gpt-4-turbo', 'openai', 'gpt-4-turbo', 'chat'),
    ('claude-3-haiku', 'openai', 'claude-3-haiku-20240307', 'chat'),
    ('text-embedding-3-small', 'openai', 'text-embedding-3-small', 'embedding')
on conflict (model_name) do nothing;

-- Seed pricing
insert into ai_model_pricing (model_name, input_price, output_price) values
    ('gpt-4o', 5.00, 15.00),
    ('gpt-4o-mini', 0.15, 0.60),
    ('gpt-4-turbo', 10.00, 30.00),
    ('claude-3-haiku', 0.25, 1.25),
    ('text-embedding-3-small', 0.02, 0.02)
on conflict (model_name) do nothing;

-- Seed plan-model access (maps to existing product_plan codes: free, pro, enterprise)
insert into ai_plan_model_access (plan_code, model_name, max_concurrency, max_rpm) values
    ('free', 'gpt-4o-mini', 1, 10),
    ('pro', 'gpt-4o-mini', 5, 60),
    ('pro', 'gpt-4o', 3, 30),
    ('pro', 'text-embedding-3-small', 5, 100),
    ('enterprise', 'gpt-4o-mini', 20, 300),
    ('enterprise', 'gpt-4o', 10, 200),
    ('enterprise', 'claude-3-haiku', 10, 200),
    ('enterprise', 'text-embedding-3-small', 20, 500)
on conflict (plan_code, model_name) do nothing;
