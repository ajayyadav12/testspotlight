create table T_SUBMISSION_REQUEST
(
    id integer not null primary key,
    request varchar(4000),
    state varchar(255),
    start_time TIMESTAMP(6),
    process_id varchar(255)
);

create sequence S_SUBMISSION_REQUEST_ID MINVALUE 1 MAXVALUE 9999999999 START WITH 1 INCREMENT BY 1
NOCACHE NOCYCLE;

exit;