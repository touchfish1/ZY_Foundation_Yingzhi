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
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index idx_saas_user_email on saas_user(email);
create index idx_saas_user_api_key on saas_user(api_key);
