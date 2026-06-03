create table asset_file (
  id bigserial primary key,
  bucket varchar(64) not null,
  object_key varchar(512) not null,
  original_name varchar(255) not null,
  content_type varchar(128),
  size_bytes bigint not null,
  url varchar(1024),
  created_by bigint references admin_user(id),
  created_at timestamptz not null default now()
);

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
