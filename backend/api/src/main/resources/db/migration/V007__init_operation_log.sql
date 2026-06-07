create table operation_log (
    id bigserial primary key,
    operator_id bigint not null references admin_user(id),
    operator_name varchar(64),
    operation_type varchar(32) not null,
    resource_type varchar(64) not null,
    resource_id varchar(128),
    detail jsonb,
    ip_address varchar(64),
    result varchar(16) not null default 'SUCCESS',
    error_message text,
    created_at timestamptz not null default now()
);

create index idx_oplog_operator on operation_log(operator_id);
create index idx_oplog_resource on operation_log(resource_type, resource_id);
create index idx_oplog_created_at on operation_log(created_at desc);
create index idx_oplog_type on operation_log(operation_type);
