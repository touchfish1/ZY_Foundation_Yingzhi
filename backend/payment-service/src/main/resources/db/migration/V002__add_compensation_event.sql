create table if not exists payment_compensation_event (
    id              bigserial primary key,
    payment_no      varchar(64) not null,
    event_type      varchar(32) not null,
    event_status    varchar(16) not null default 'PENDING',
    retry_count     int not null default 0,
    max_retries     int not null default 5,
    payload_json    jsonb,
    last_error      text,
    created_at      timestamptz not null default now(),
    last_retry_at   timestamptz
);

create index idx_compensation_status on payment_compensation_event(event_status);
