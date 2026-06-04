create table admin_menu (
  id bigserial primary key,
  parent_id bigint references admin_menu(id),
  name varchar(64) not null,
  path varchar(255),
  icon varchar(64),
  menu_type varchar(16) not null default 'page',
  sort_order int not null default 0,
  status varchar(16) not null default 'enabled',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table admin_menu_permission (
  menu_id bigint not null references admin_menu(id) on delete cascade,
  permission_code varchar(128) not null references admin_permission(code) on delete cascade,
  primary key (menu_id, permission_code)
);
