create table order_main (
  id bigserial primary key,
  order_no varchar(64) not null unique,
  user_id bigint,
  plan_id bigint not null references product_plan(id),
  price_id bigint not null references product_price(id),
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
