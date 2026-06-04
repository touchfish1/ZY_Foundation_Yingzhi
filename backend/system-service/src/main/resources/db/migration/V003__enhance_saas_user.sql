ALTER TABLE saas_user ADD COLUMN IF NOT EXISTS balance decimal(20,8) not null default 0;
ALTER TABLE saas_user ADD COLUMN IF NOT EXISTS concurrency int not null default 5;
ALTER TABLE saas_user ADD COLUMN IF NOT EXISTS rpm_limit int not null default 0;
ALTER TABLE saas_user ADD COLUMN IF NOT EXISTS role varchar(20) not null default 'user';
ALTER TABLE saas_user ADD COLUMN IF NOT EXISTS total_recharged decimal(20,8) not null default 0;
ALTER TABLE saas_user ADD COLUMN IF NOT EXISTS notes text;
ALTER TABLE saas_user ADD COLUMN IF NOT EXISTS last_login_at timestamptz;

create table if not exists balance_transaction (
  id bigserial primary key,
  user_id bigint not null,
  amount decimal(20,2) not null,
  balance_after decimal(20,2) not null,
  transaction_type varchar(32) not null,
  description text,
  created_at timestamptz not null default now()
);
create index if not exists idx_balance_user on balance_transaction(user_id);
