create table admin_user (
  id bigserial primary key,
  username varchar(64) not null unique,
  password_hash varchar(255) not null,
  nickname varchar(64),
  email varchar(128),
  status varchar(32) not null default 'enabled',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table admin_role (
  id bigserial primary key,
  code varchar(64) not null unique,
  name varchar(64) not null,
  created_at timestamptz not null default now()
);

create table admin_permission (
  id bigserial primary key,
  code varchar(128) not null unique,
  name varchar(128) not null,
  module varchar(64) not null
);

create table admin_user_role (
  user_id bigint not null references admin_user(id),
  role_id bigint not null references admin_role(id),
  primary key (user_id, role_id)
);

create table admin_role_permission (
  role_id bigint not null references admin_role(id),
  permission_id bigint not null references admin_permission(id),
  primary key (role_id, permission_id)
);
