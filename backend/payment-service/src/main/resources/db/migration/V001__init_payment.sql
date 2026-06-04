create table payment_transaction (
  id bigserial primary key,
  payment_no varchar(64) not null unique,
  order_id bigint not null,
  order_no varchar(64) not null,
  channel varchar(32) not null,
  amount decimal(12,2) not null,
  currency varchar(8) not null default 'CNY',
  status varchar(32) not null default 'PENDING',
  request_json jsonb,
  callback_json jsonb,
  created_at timestamptz not null default now(),
  paid_at timestamptz
);

create index idx_payment_order on payment_transaction(order_id);
create index idx_payment_no on payment_transaction(payment_no);
