create table user_subscription (
  id bigserial primary key,
  user_id bigint not null,
  plan_code varchar(64) not null,
  plan_name varchar(128) not null,
  status varchar(32) not null default 'active',
  starts_at timestamptz not null default now(),
  expires_at timestamptz not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index idx_sub_user on user_subscription(user_id);
create index idx_sub_status on user_subscription(status);
