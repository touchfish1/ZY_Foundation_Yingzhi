create table access_log (
    id bigserial primary key,
    request_method varchar(16) not null,
    request_path varchar(512) not null,
    response_status integer not null,
    user_id bigint,
    username varchar(64),
    ip_address varchar(64),
    user_agent varchar(512),
    duration_ms bigint not null,
    created_at timestamptz not null default now()
);

create index idx_access_log_created_at on access_log(created_at desc);
create index idx_access_log_user on access_log(user_id);
create index idx_access_log_status on access_log(response_status);
create index idx_access_log_path on access_log(request_path);
