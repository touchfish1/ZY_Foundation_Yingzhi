create table usage_record (
  id bigserial primary key,
  user_id bigint not null,
  api_key varchar(64),
  api_path varchar(255),
  tokens_in bigint default 0,
  tokens_out bigint default 0,
  cost decimal(20,8) default 0,
  duration_ms int default 0,
  status varchar(16) not null default 'success',
  created_at timestamptz not null default now()
);

create index idx_usage_user on usage_record(user_id);
create index idx_usage_created on usage_record(created_at);

-- Daily usage summary for quick queries
create table usage_daily_summary (
  id bigserial primary key,
  user_id bigint not null,
  date date not null,
  total_calls int not null default 0,
  total_tokens_in bigint not null default 0,
  total_tokens_out bigint not null default 0,
  total_cost decimal(20,8) not null default 0,
  unique(user_id, date)
);

create index idx_summary_user_date on usage_daily_summary(user_id, date);
