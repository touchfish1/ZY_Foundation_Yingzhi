create table saas_user (
  id bigserial primary key,
  email varchar(128) not null unique,
  password_hash varchar(255) not null,
  nickname varchar(64),
  avatar_url varchar(255),
  status varchar(32) not null default 'active',
  api_key varchar(64) unique,
  quota_used bigint not null default 0,
  quota_limit bigint not null default 0,
  balance decimal(20,8) not null default 0,
  concurrency int not null default 5,
  rpm_limit int not null default 0,
  role varchar(20) not null default 'user',
  total_recharged decimal(20,8) not null default 0,
  notes text,
  last_login_at timestamptz,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index idx_saas_user_email on saas_user(email);
create index idx_saas_user_api_key on saas_user(api_key);

create table balance_transaction (
  id bigserial primary key,
  user_id bigint not null,
  amount decimal(20,2) not null,
  balance_after decimal(20,2) not null,
  transaction_type varchar(32) not null,
  description text,
  created_at timestamptz not null default now()
);
create index idx_balance_user on balance_transaction(user_id);
