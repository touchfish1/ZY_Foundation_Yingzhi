# CMS 数据库设计

数据库使用 PostgreSQL。CMS 页面内容、区块定义和发布快照使用 JSONB，以兼顾结构化查询和页面配置灵活性。

## 管理员和权限

```sql
create table admin_user (
  id bigserial primary key,
  username varchar(64) not null unique,
  password_hash varchar(255) not null,
  nickname varchar(64),
  email varchar(128),
  status varchar(32) not null default 'enabled',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table admin_role (
  id bigserial primary key,
  code varchar(64) not null unique,
  name varchar(64) not null,
  created_at timestamptz not null default now()
);

create table admin_permission (
  id bigserial primary key,
  code varchar(128) not null unique,
  name varchar(128) not null,
  module varchar(64) not null
);

create table admin_user_role (
  user_id bigint not null,
  role_id bigint not null,
  primary key (user_id, role_id)
);

create table admin_role_permission (
  role_id bigint not null,
  permission_id bigint not null,
  primary key (role_id, permission_id)
);
```

## CMS 页面

```sql
create table cms_page (
  id bigserial primary key,
  slug varchar(255) not null,
  page_type varchar(64) not null default 'custom',
  default_locale varchar(16) not null default 'zh-CN',
  status varchar(32) not null default 'enabled',
  created_by bigint,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (slug)
);

create table cms_page_translation (
  id bigserial primary key,
  page_id bigint not null references cms_page(id),
  locale varchar(16) not null,
  title varchar(255) not null,
  seo_title varchar(255),
  seo_description text,
  seo_keywords varchar(512),
  draft_version_id bigint,
  published_version_id bigint,
  status varchar(32) not null default 'draft',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (page_id, locale)
);

create table cms_page_version (
  id bigserial primary key,
  page_id bigint not null references cms_page(id),
  locale varchar(16) not null,
  version_no integer not null,
  content_json jsonb not null,
  snapshot_json jsonb,
  created_by bigint,
  created_at timestamptz not null default now(),
  remark varchar(255),
  unique (page_id, locale, version_no)
);

create table cms_block_definition (
  id bigserial primary key,
  type varchar(64) not null unique,
  name varchar(64) not null,
  schema_json jsonb not null,
  default_props_json jsonb not null default '{}'::jsonb,
  enabled boolean not null default true,
  sort_order integer not null default 0,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table cms_publish_record (
  id bigserial primary key,
  page_id bigint not null references cms_page(id),
  locale varchar(16) not null,
  version_id bigint not null references cms_page_version(id),
  operator_id bigint,
  published_at timestamptz not null default now(),
  remark varchar(255)
);
```

建议索引：

```sql
create index idx_cms_page_slug on cms_page(slug);
create index idx_cms_page_translation_locale on cms_page_translation(locale);
create index idx_cms_page_version_page_locale on cms_page_version(page_id, locale);
```

## 媒体资源

```sql
create table asset_file (
  id bigserial primary key,
  bucket varchar(64) not null,
  object_key varchar(512) not null,
  original_name varchar(255) not null,
  content_type varchar(128),
  size_bytes bigint not null,
  url varchar(1024),
  created_by bigint,
  created_at timestamptz not null default now()
);
```

## 套餐和价格

```sql
create table product_plan_group (
  id bigserial primary key,
  code varchar(64) not null unique,
  name varchar(128) not null,
  description text,
  status varchar(32) not null default 'enabled',
  sort_order integer not null default 0,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table product_plan (
  id bigserial primary key,
  group_id bigint not null references product_plan_group(id),
  code varchar(64) not null unique,
  name varchar(128) not null,
  description text,
  badge varchar(64),
  status varchar(32) not null default 'enabled',
  sort_order integer not null default 0,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table product_price (
  id bigserial primary key,
  plan_id bigint not null references product_plan(id),
  currency varchar(16) not null default 'CNY',
  billing_cycle varchar(32) not null,
  amount numeric(12, 2) not null,
  original_amount numeric(12, 2),
  status varchar(32) not null default 'enabled',
  created_at timestamptz not null default now()
);

create table product_feature (
  id bigserial primary key,
  plan_id bigint not null references product_plan(id),
  feature_name varchar(128) not null,
  feature_value varchar(255),
  included boolean not null default true,
  sort_order integer not null default 0
);
```

## 订单和支付

```sql
create table order_main (
  id bigserial primary key,
  order_no varchar(64) not null unique,
  user_id bigint,
  plan_id bigint not null,
  price_id bigint not null,
  amount numeric(12, 2) not null,
  currency varchar(16) not null,
  status varchar(32) not null,
  snapshot_json jsonb not null,
  created_at timestamptz not null default now(),
  paid_at timestamptz,
  cancelled_at timestamptz
);

create table payment_transaction (
  id bigserial primary key,
  payment_no varchar(64) not null unique,
  order_id bigint not null references order_main(id),
  channel varchar(64) not null,
  amount numeric(12, 2) not null,
  currency varchar(16) not null,
  status varchar(32) not null,
  request_json jsonb,
  callback_json jsonb,
  created_at timestamptz not null default now(),
  paid_at timestamptz
);
```

## 状态约定

页面状态：

```text
enabled
disabled
archived
```

翻译状态：

```text
draft
published
archived
```

订单状态：

```text
pending
paid
cancelled
expired
refunded
```

支付状态：

```text
pending
processing
success
failed
closed
refunded
```
