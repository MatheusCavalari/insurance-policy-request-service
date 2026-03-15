create table processed_messages (
                                    id bigserial primary key,
                                    consumer_name varchar(100) not null,
                                    event_id uuid not null,
                                    processed_at timestamptz not null
);

create unique index uq_processed_messages_consumer_event
    on processed_messages(consumer_name, event_id);
