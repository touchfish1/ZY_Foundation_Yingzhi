create table audit_log (
  id bigserial primary key,
  user_id bigint,
  action varchar(64) not null,
  resource_type varchar(64),
  resource_id varchar(128),
  detail text,
  ip_address varchar(45),
  user_agent text,
  created_at timestamptz not null default now()
);

create index idx_audit_user on audit_log(user_id);
create index idx_audit_action on audit_log(action);
create index idx_audit_created on audit_log(created_at);
