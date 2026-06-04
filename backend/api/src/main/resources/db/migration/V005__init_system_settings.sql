create table system_setting (
  id bigserial primary key,
  setting_key varchar(128) not null unique,
  setting_value text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
