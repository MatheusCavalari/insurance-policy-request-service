create table outbox_events (
                               id uuid primary key,
                               aggregate_id uuid not null,
                               event_type varchar(100) not null,
                               payload text not null,
                               status varchar(30) not null,
                               retries integer not null default 0,
                               created_at timestamptz not null,
                               published_at timestamptz null,
                               error_message text null
);

create index idx_outbox_events_status_created_at
    on outbox_events(status, created_at);
