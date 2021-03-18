create table T_USER_NOTIFICATIONS (
	id integer not null primary key,
	schedule_id integer, 
	sso integer,
	submission_id integer,
	process_name varchar2(100),
	status varchar2(10),
	PROCESS_TYPE varchar2(100),
	process_id varchar2(100),
	start_time date,
	end_time date,
	scheduledef_id integer
);

create sequence NOTIFICATION_ID MINVALUE 1 MAXVALUE 9999999999 START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;