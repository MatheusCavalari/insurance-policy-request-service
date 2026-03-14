create table policy_requests (
                                 id uuid primary key,
                                 customer_id uuid not null,
                                 product_id bigint not null,
                                 category varchar(30) not null,
                                 sales_channel varchar(30) not null,
                                 payment_method varchar(30) not null,
                                 status varchar(30) not null,
                                 created_at timestamptz not null,
                                 finished_at timestamptz null,
                                 total_monthly_premium_amount numeric(15,2) not null,
                                 insured_amount numeric(15,2) not null,
                                 coverages jsonb not null,
                                 assistances jsonb not null
);

create index idx_policy_requests_customer_id
    on policy_requests(customer_id);

create index idx_policy_requests_status
    on policy_requests(status);

create index idx_policy_requests_created_at
    on policy_requests(created_at);