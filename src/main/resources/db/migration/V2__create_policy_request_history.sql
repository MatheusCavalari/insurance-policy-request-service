create table policy_request_history (
                                        id bigserial primary key,
                                        policy_request_id uuid not null,
                                        status varchar(30) not null,
                                        changed_at timestamptz not null,
                                        constraint fk_policy_request_history_request
                                            foreign key (policy_request_id)
                                                references policy_requests(id)
                                                on delete cascade
);

create index idx_policy_request_history_request_id
    on policy_request_history(policy_request_id);