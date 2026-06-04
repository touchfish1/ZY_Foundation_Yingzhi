create table order_main (
  id bigserial primary key,
  order_no varchar(64) not null unique,
  plan_id bigint,
  price_id bigint,
  amount decimal(12,2) not null,
  currency varchar(8) not null default 'CNY',
  status varchar(32) not null default 'PENDING',
  user_id bigint,
  snapshot_json jsonb,
  created_at timestamptz not null default now(),
  paid_at timestamptz,
  cancelled_at timestamptz
);

create index idx_order_user on order_main(user_id);
create index idx_order_no on order_main(order_no);
create index idx_order_status on order_main(status);
