alter table policy_requests
    add column payment_status varchar(30) not null default 'PENDING';

alter table policy_requests
    add column underwriting_status varchar(30) not null default 'PENDING';
